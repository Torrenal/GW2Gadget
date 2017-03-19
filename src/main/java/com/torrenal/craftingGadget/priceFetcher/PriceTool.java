package com.torrenal.craftingGadget.priceFetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.ItemUnknown;
import com.torrenal.craftingGadget.ResourceManager;
import com.torrenal.craftingGadget.apiInterface.http.SSLContextProvider;
import com.torrenal.craftingGadget.apiInterface.json.JSONArray;
import com.torrenal.craftingGadget.apiInterface.json.JSONNode;
import com.torrenal.craftingGadget.apiInterface.json.JSONPoint;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONNumber;
import com.torrenal.craftingGadget.db.items.ItemDB;
import com.torrenal.craftingGadget.transactions.sources.Recipe;
import com.torrenal.craftingGadget.transactions.sources.Source;

public class PriceTool
{

   private List<HttpCookie>	cookies;

   private static final long SPAM_WAIT = 31 * 1000;
   private static final long REQUEST_INTERVAL = 1000;

   private HashSet<Item> updateList = new HashSet<>(7000);
   private Object updateListLock = new Object();
   private HashSet<Long> additionsList = new HashSet<>(50);
   private Object additionsListLockObject = new Object();
   private final QueueComparator<Item> comparator = new QueueComparator<>();
   private PriorityQueue<Item> processQueue = new PriorityQueue<>(1, comparator);
   private boolean organizeQueue = false;
   private long startProcTimestamp = 0;
   private boolean interfaceEnabled = true;

   private float gemToGoldExchangeRate = 1F;
   private float goldToGemExchangeRate = 2F;
   private long exchangeRateTimeStamp = 0;
   private boolean updatingExchangeRates = false;
   final private Object exchangeLockObject = new Object();
   private HashSet<String> validIDs = new HashSet<>();


   private boolean hangDebuggers = ResourceManager.isReleaseVersion;

   private int failcount = 0;

   static private PriceTool instance = null;

   private static final int REQUEST_SIZE = 199;

   public PriceTool()
   {
      instance = this;
      //		initialize();
   }


   /** 
    * @param args
    * @throws MalformedURLException
    */
   @Deprecated
   public void initialize(String userID, String userPassword) // FIXME Fetch list if valid queries
   {
      //		cookies = fetchCookie(userID, userPassword);
      failcount = 0;
   }

   public void launchWorker()
   {
      Runnable doRun = new Runnable()
      {
         public void run()
         {
            ItemDB.blockForInit();

            while(true)
            {
               try
               {
                  workQueue();
               }
               catch (Throwable err)
               {
                  if(err instanceof InterruptedException)
                  {
                     continue;
                  }
                  err.printStackTrace();

                  synchronized(PriceTool.this)
                  {
                     try
                     {
                        PriceTool.this.wait(5 * 60 * 1000); /* 5 minutes */
                     } catch (InterruptedException e)
                     {
                        e.printStackTrace();
                     } 
                  }

               }
            }
         }
      };
      Thread thread = new Thread(doRun,"PriceTool");
      thread.setDaemon(true);
      thread.setPriority(Thread.NORM_PRIORITY-1);
      thread.start();
   }

   private void workQueue() throws InterruptedException
   {
      refreshQueue();

      while(!processQueue.isEmpty())
      {
         if(organizeQueue)
         {
            break;
         }
         ArrayList<Item> getList = new ArrayList<Item>();

         synchronized(updateListLock)
         {
            while(getList.size() < REQUEST_SIZE && !processQueue.isEmpty())
            {
               Item item = processQueue.poll();
//               if(item.getName().contains("Bag"))
//               {
//                  System.err.println(item.getName() + " up for pricing in the queue");
//                  item = item;
//               } else
//               {
//                 // System.out.println("Pricing: " + item.getName());
//               }
               if(item == null)
               { 
                  break; 
               }

               if(!isTradeable(item))
               {
                  updateList.remove(item);
                  continue;
               }
               if(!item.hasRecipeTies())
               {
                  continue;
               }
               if(item.isPriorityUpdate())
               {
                  getList.add(item);
                  continue;
               }
               if(item.getPricesUpdatedTimeStamp() + 30*60*1000 < System.currentTimeMillis())
               {
                  getList.add(item);
                  continue;
               }
               processQueue.clear();
            }
            updatePricesFor(getList);
            startProcTimestamp = 0;
            synchronized(this)
            {
               this.wait(REQUEST_INTERVAL);
            }
            startProcTimestamp = System.currentTimeMillis();
         }
         while((!organizeQueue && processQueue.isEmpty()) || !interfaceEnabled)
         {
            startProcTimestamp = 0;
            synchronized(PriceTool.this)
            {
               PriceTool.this.wait(100);
            }
            startProcTimestamp = System.currentTimeMillis();
         }
      }
   }

   private boolean isTradeable(Item item)
   {
      if(validIDs.isEmpty())
      {
         fetchValidIDs();
      }
      return validIDs.contains(item.getItemID());
   }



   private synchronized  void fetchValidIDs()
   {
      if(!validIDs.isEmpty())
      {
         return;
      }
      try
      {
         URL url = new URL(
               "https://api.guildwars2.com/v2/commerce/prices");
         SSLContext sslContext = null;

         HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

         sslContext = SSLContextProvider.getSSLContextForURL(url);
         conn.setSSLSocketFactory(sslContext.getSocketFactory());

         conn.setDoOutput(true);
         conn.setDoInput(true);

         conn.setRequestMethod("GET");

         conn.setRequestProperty("Accept", "*/*");

         conn.setRequestProperty("x-requested-with", "XMLHttpRequest");

         conn.setRequestProperty("Accept-Encoding", "deflate");

         conn.setRequestProperty("Connection", "keep-alive");

         conn.setRequestProperty("User-Agent",
               ResourceManager.PROGRAM_NAME + " " + ResourceManager.VERSION);

         // Get response from trading-post
         BufferedReader in = null;
         {
            InputStream conInStream = null;

            try
            {
               conInStream = conn.getInputStream();
               if(conInStream == null)
               {
                  System.err.println("Urk - HTTP Input stream is null?!");
               }
            } catch (SSLHandshakeException err)
            {
               if(!SSLContextProvider.handShakeErrorReceived(conn.getURL(), sslContext))
               {
                  fetchValidIDs();
               } 
            }
            catch (IOException err)
            {
               if(conn.getResponseCode() == 500)
               {
                  synchronized(this)
                  {
                     System.err.println("Hit an error during an HTTP call, could be we've spammed the server with a few too many requests.\n Pausing...");
                     try
                     {
                        Thread.sleep(SPAM_WAIT);
                     } catch (InterruptedException e)
                     { }
                  }
               } else if(conn.getResponseCode() == 401)
               {
                  System.err.println("Hit 401 responce (auth error) during an HTTP call, voiding cookies");
                  cookies = null;
               } else
               {
                  err.printStackTrace();

                  System.err.println("Got response " + conn.getResponseCode() + " - " + conn.getResponseMessage() + " during TP query.  Suspending TP Interface, voiding cookies.");
                  cookies = null;
                  synchronized(this)
                  {
                     try 
                     {
                        Thread.sleep(SPAM_WAIT * 5);
                     } catch (InterruptedException e)
                     { }
                  }

               }
               //              in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
               //              long cll = conn.getContentLengthLong();
               //              System.err.println("cll = " + cll);
               //              
               //              
               //              String errData = "";
               //              String inputLine;
               //              while ((inputLine = in.readLine()) != null)
               //                  errData += inputLine;
               //              in.close();
               //
               //              System.err.println("content = " + errData);
               throw err;
            }
            in = new BufferedReader(new InputStreamReader(conInStream));

         }
         String data = "";

         String inputLine;

         while ((inputLine = in.readLine()) != null)
            data += inputLine;
         in.close();
         conn.disconnect();

         // Parse returned data
         //      data = "[{" + data + "}]";
         JSONPoint mapping = JSONNode.parseJSONArrays(data);

         JSONArray array = (JSONArray) ((JSONArray) (mapping)).getValues()[0];
         HashSet<String> newValidIDs = new HashSet<>();

         for (JSONPoint element : array.getValues())
         {
            JSONNumber number = (JSONNumber) element;
            newValidIDs.add(number.toString());
         }
         validIDs = newValidIDs;
      } catch (IOException err)
      {
         throw new Error("Error retrieveing list of tradeables", err);
      }
   }


   public static boolean checkIsTradeable(long itemID)
   {
      return instance.validIDs.contains(""+itemID);
   }

   private void updatePricesFor(List<Item> items)
   {
      if(items == null || items.size() <1)
      {
         return;
      }
      StringBuilder itemIDs= new StringBuilder();
      for(Item item : items)
      {
         String itemID = item.getItemID();
         if(itemID == null || itemID.isEmpty())
         {
            continue;
         }
         itemIDs.append(",").append(item.getItemID());
      }
      if(itemIDs.length() < 1)
      {
         return;
      }
      String itemString = itemIDs.substring(1);

      try
      {
         Vector<Result> data = getListingInfo(itemString);
         if(data == null)
         {
            if(failcount++ == 5)
               cookies = null;
            organizeQueue = true;
            return;
         }
         failcount = 0;
         for(Result result : data)
         {
            String itemID = result.getItemID();

            if(itemID == null || itemID.isEmpty())
            {
               continue;
            }
            for(Item item : items)
            {
               if(itemID.equals(item.getItemID()))
               {
                  try
                  {
                     int buyPrice      = Integer.parseInt(result.getBuyPrice());
                     int buyNum        = Integer.parseInt(result.getBuyNum());
                     int sellPrice     = Integer.parseInt(result.getSellPrice());
                     int sellNum       = Integer.parseInt(result.getSellNum());

                     CookingCore.updatePricingFor(item, buyPrice, sellPrice, buyNum, sellNum);
                  }
                  catch (NumberFormatException err)
                  {
                     System.err.println("Error parsing detail for " + item.getName());
                     System.err.println("detail = " + result);
                     err.printStackTrace();
                  }
                  continue;
               }
            }
         }
      } catch (MalformedURLException e)
      {
         System.err.println("Fault parsing data for " + itemIDs);
         e.printStackTrace();
         cookies = null;
      }
   }

   public void performPriorityPricingTreeUpdate(Item item, boolean force)
   {

      Vector<Item> components = new Vector<Item>();

      walkPricingTree(item, components);

      long ignoreAfter = System.currentTimeMillis() - 5 * 60 * 1000; /* 5 minute grace */
      long hardIgnoreAfter = System.currentTimeMillis() - 2 * 60 * 1000; /* Even forced stuff gets 2 min grace */
      for(Item component : components)
      {
         if(component instanceof ItemUnknown)
         {
            continue;
         }
         if(!force && component.getPricesUpdatedTimeStamp() > ignoreAfter)
         {
            continue;
         }
         if(component.getPricesUpdatedTimeStamp() > hardIgnoreAfter)
         {
            continue;
         }
         component.setPriorityUpdate(true);
      }
      if(!organizeQueue)
      {
         organizeQueue = true;
         Runnable runMe = new Runnable(){

            @Override
            public void run()
            {
               synchronized(PriceTool.this)
               {
                  PriceTool.this.notifyAll();
               }
            }
         };
         new Thread(runMe).start();
      }

      //TODO : Remove if unnecessary (I think it can go)
   }

   /**
    * Same as #performPriorityPricingTreeUpdate(Item, boolean), but it doesn't
    * walk the tree.
    * @param item
    * @param force
    */
   public void performPriorityPricingUpdate(Item item, boolean force)
   {
      long ignoreAfter = System.currentTimeMillis() - 5 * 60 * 1000; /* 5 minute grace */
      long hardIgnoreAfter = System.currentTimeMillis() - 2 * 60 * 1000; /* Even forced stuff gets 2 min grace */
      if(!force && item.getPricesUpdatedTimeStamp() > ignoreAfter)
      {
         return;
      }
      if(item.getPricesUpdatedTimeStamp() > hardIgnoreAfter)
      {
         return;
      }
      item.setPriorityUpdate(true);

      organizeQueue = true;
   }


   private void walkPricingTree(Item item, Vector<Item> components)
   {
      Collection<Source> sources = item.getSources();
      if(components.contains(item))
      {
         return;
      }
      components.add(item);
      for(Source source : sources)
      {
         if(source instanceof Recipe)
         {
            ItemQuantitySet[] inputs = ((Recipe) source).getInputs();
            
            for(ItemQuantitySet input : inputs)
            {
               walkPricingTree(input.getItem(), components);
            }
         }
      }
   }

   //	public void queueForStandardUpdates(Collection<? extends Item> items)
   //	{
   //		for (Iterator<? extends Item> iterator = items.iterator(); iterator.hasNext();)
   //		{
   //			Item item = (Item) iterator.next();
   //			synchronized(additionsListLockObject)
   //			{
   //				additionsList.add(item.getItemID());
   //			}
   //		}
   //		organizeQueue = true;
   //	}

   static public void queueItemForStandardUpdates(long itemID)
   {
      instance.queueForStandardUpdates(itemID);
   }

   public void queueForStandardUpdates(final long itemID)
   {
      if(hangDebuggers)
      {
         Runnable doRun = new Runnable(){

            @Override
            public void run()
            {
               synchronized(additionsListLockObject)
               {
                  if(!additionsList.contains(itemID))
                  {
                     additionsList.add(itemID);
                  }
               }
               organizeQueue = true;
            }
         };
         new Thread(doRun,"addMe").start();
      } else
      {
         synchronized(additionsListLockObject)
         {
            if(!additionsList.contains(itemID))
            {
               additionsList.add(itemID);
            }
         }
         organizeQueue = true;

      }
   }

   private void refreshQueue()
   {
      if(!interfaceEnabled)
      {
         return;
      }

      synchronized(updateListLock)
      {
         if(!additionsList.isEmpty())
         {

            HashSet<Long> additionsToAdd;
            synchronized(additionsListLockObject)
            {
               additionsToAdd = additionsList;
               additionsList = new HashSet<Long>();
            }
            HashSet<Item> additionItemsToAdd = new HashSet<Item>();
            for(Long itemID : additionsToAdd)
            {

               APIItem item = CookingCore.findItemByID(itemID);
               if(item != null && isTradeable(item))
               {
                  additionItemsToAdd.add(item);
               }
            }
            updateList.addAll(additionItemsToAdd);
         }
         organizeQueue = false;
         if(updateList.isEmpty())
         {
            return;
         }
         processQueue = new PriorityQueue<>(updateList.size(), comparator);
         processQueue.addAll(updateList);

      }
   }

   //	public List<HttpCookie> fetchCookie_deleteMeFunc(String email, String password) //FIXME
   //	{
   //		while(true)
   //		{
   //			try
   //			{
   //				cookies = fetchCookieImpl(email, password);
   //			} catch (MalformedURLException e)
   //			{
   //				e.printStackTrace();
   //			}
   //			if(cookies != null)
   //			{
   //				organizeQueue = true;
   ////				activateCookies();
   //				return cookies;
   //			}
   //			synchronized(this)
   //			{
   //				try
   //				{
   //					this.wait(10 * 60 * 1000); /* 10 minutes */
   //				} catch (InterruptedException e)
   //				{ } 
   //			}
   //		}
   //	}
   ////
   //	private void activateCookies_deleteMe() //FIXME
   //	{
   //
   //		URL url;
   //		HttpsURLConnection conn;
   //		CookieHandler manager = CookieManager.getDefault();
   //		SSLContext sslContext;
   //		try {
   //			String authID = null;
   //			for( HttpCookie cookie : cookies )
   //			{
   //				if("s".equals(cookie.getName()))
   //				{
   //					authID = URLDecoder.decode(cookie.getValue());
   //					if(authID.startsWith("\"") && authID.endsWith("\""))
   //					{
   //						authID = authID.substring(1,authID.length()-1);
   //					}
   //				}
   //				
   //			}
   //			url = new URL("https://tradingpost-live.ncplatform.net/authenticate?"+authID);
   //			//url = new URL("https://tradingpost-live.ncplatform.net/authenticate");
   //
   //			conn = (HttpsURLConnection) url.openConnection();
   //			sslContext = SSLContextProvider.getSSLContextForURL(url);
   //			conn.setSSLSocketFactory(sslContext.getSocketFactory());
   //
   //			conn.setDoOutput(true);
   //			conn.setDoInput(true);
   //
   //			conn.setRequestMethod("GET");
   //
   //			conn.setRequestProperty("Accept", "*/*");
   //
   //			conn.setRequestProperty("x-requested-with", "XMLHttpRequest");
   //
   //			conn.setRequestProperty("Accept-Encoding", "deflate");
   //
   //			conn.setRequestProperty("Connection", "keep-alive");
   //
   //			conn.setRequestProperty("User-Agent",
   //					ResourceManager.PROGRAM_NAME + " " + ResourceManager.VERSION);
   //
   //
   //		} catch (Throwable err)
   //		{
   //			throw new Error(err);
   //		}
   //
   //		// Get response from trading-post
   //		BufferedReader in = null;
   //		{
   //			InputStream conInStream = null;
   //
   //			try
   //			{
   //				conn.getContent();
   //				conInStream = conn.getInputStream();
   //			} catch (SSLHandshakeException err)
   //			{
   //				if(!SSLContextProvider.handShakeErrorReceived(conn.getURL(), sslContext))
   //				{
   ////					activateCookies();
   //					return;
   //				} 
   //			} catch (IOException e)
   //			{
   //				e.printStackTrace();
   //			}
   //			in = new BufferedReader(new InputStreamReader(conInStream));
   //
   //		}
   //		String data = "";
   //
   //		String inputLine;
   //
   //		try {
   //			while ((inputLine = in.readLine()) != null)
   //			data += inputLine;
   //			in.close();
   //		} catch (IOException e) 
   //		{
   //			e.printStackTrace();
   //		}
   //		System.err.println("Enable result: " + data);
   //		conn.disconnect();
   //	}
   //
   //
   //	public List<HttpCookie> fetchCookieImpl(String email, String password)
   //			throws MalformedURLException
   //   {
   //		List<HttpCookie> cookies = null;
   //		String encEmail = null;
   //		String encPassword = null;
   //		try
   //		{
   //			encEmail = URLEncoder.encode(email, "UTF-8");
   //			encPassword = URLEncoder.encode(password, "UTF-8");
   //		} catch (UnsupportedEncodingException e2)
   //		{
   //			e2.printStackTrace();
   //		}
   //
   //		System.setProperty("javax.net.ssl.keyStore", ".keystore");
   //		System.setProperty("javax.net.ssl.keyStorePassword", "abc124");
   //		System.setProperty("javax.net.ssl.trustStore", "newTrust.ks");
   //		System.setProperty("javax.net.ssl.trustStorePassword", "");
   //
   //		URL url = new URL("https://account.guildwars2.com/login");
   //		//URL url = new URL("https://account.guildwars2.com/login?redirect_uri=http%3A%2F%2Ftradingpost-live.ncplatform.net%2Fauthenticate%3Fsource%3D%252F&&game_code=gw2");
   //		//URL url = new URL("https://tradingpost-live.ncplatform.net/authenticate");
   //		SSLContext sslContext = null;
   //
   //		try
   //		{
   //			String login = "email=" + encEmail + "&password=" + encPassword;
   //
   //			CookieManager manager = new CookieManager();
   //			manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
   //			CookieHandler.setDefault(manager);
   //
   //			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
   //			conn.setDoOutput(true);
   //			conn.setDoInput(true);
   //			sslContext = SSLContextProvider.getSSLContextForURL(url);
   //			conn.setSSLSocketFactory(sslContext.getSocketFactory());
   //
   //			conn.setRequestMethod("POST");
   //
   //			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml");
   //			conn.setRequestProperty("Host", "account.guildwars2.com");
   //			conn.setRequestProperty("Accept-Encoding", "deflate");
   //			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
   //			//conn.setRequestProperty("User-Agent", ResourceManager.PROGRAM_NAME + " " + ResourceManager.VERSION);
   //			conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1");
   //			conn.setRequestProperty("Origin", "https://account.guildwars2.com");
   //			conn.setRequestProperty("Referer", "https://account.guildwars2.com/login?redirect_uri=http%3A%2F%2Ftradingpost-live.ncplatform.net%2Fauthenticate%3Fsource%3D%252F&game_code=gw2");
   //			conn.setRequestProperty("Connection", "keep-alive");
   //			conn.setRequestProperty("Content-length",
   //					String.valueOf(login.length()));
   //
   //			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
   //			wr.write(login);
   //			wr.flush();
   //
   //			conn.getContent();
   //
   //			CookieStore cookieJar = manager.getCookieStore();
   //			cookies = cookieJar.getCookies();
   //			synchronized(this)
   //			{
   //				//this.wait(60000);
   //			}
   //		}
   //		catch(SSLHandshakeException err)
   //		{
   //			if(SSLContextProvider.handShakeErrorReceived(url, sslContext))
   //			{
   //				err.printStackTrace();
   //				throw new Error("Fatal SSL Error", err);
   //			} else
   //			{
   //				return fetchCookieImpl(email, password);
   //			}
   //		}
   //		catch (Exception e)
   //		{
   //			e.printStackTrace();
   //		}
   //		return cookies;
   //   }



   public Vector<Result> getListingInfo(String itemID)
         throws MalformedURLException
         {
      URL url = new URL(
            "https://api.guildwars2.com/v2/commerce/prices?ids="
                  + itemID);
      SSLContext sslContext = null;
      try
      {
         HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

         sslContext = SSLContextProvider.getSSLContextForURL(url);
         conn.setSSLSocketFactory(sslContext.getSocketFactory());

         conn.setDoOutput(true);
         conn.setDoInput(true);

         conn.setRequestMethod("GET");

         conn.setRequestProperty("Accept", "*/*");

         conn.setRequestProperty("x-requested-with", "XMLHttpRequest");

         conn.setRequestProperty("Accept-Encoding", "deflate");

         conn.setRequestProperty("Connection", "keep-alive");

         conn.setRequestProperty("User-Agent",
               ResourceManager.PROGRAM_NAME + " " + ResourceManager.VERSION);

         // Get response from trading-post
         BufferedReader in = null;
         {
            InputStream conInStream = null;

            try
            {
               System.out.println("Loading prices for " + itemID.split(",").length + " items");
               conInStream = conn.getInputStream();
               if(conInStream == null)
               {
                  System.err.println("Urk - HTTP Input stream is null?!");
               }
            } catch (SSLHandshakeException err)
            {
               if(!SSLContextProvider.handShakeErrorReceived(conn.getURL(), sslContext))
               {
                  return getListingInfo(itemID);
               } 
            }
            catch (IOException err)
            {

               if(conn.getResponseCode() == 500 || conn.getResponseCode() == 404 )
               {
                  synchronized(this)
                  {
                     System.err.println("Hit an error during an HTTP call, could be we've spammed the server with a few too many requests, or its having trouble.\n Pausing...");
                     try
                     {
                        Thread.sleep(SPAM_WAIT);
                     } catch (InterruptedException e)
                     { }
                  }
               } else if(conn.getResponseCode() == 401)
               {
                  System.err.println("Hit 401 responce (auth error) during an HTTP call, voiding cookies");
                  cookies = null;
               } else
               {
                  err.printStackTrace();

                  System.err.println("Got response " + conn.getResponseCode() + " - " + conn.getResponseMessage() + " during TP query.  Suspending TP Interface, voiding cookies.");
                  cookies = null;
                  synchronized(this)
                  {
                     try 
                     {
                        Thread.sleep(SPAM_WAIT * 5);
                     } catch (InterruptedException e)
                     { }
                  }

               }
               //					in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
               //					long cll = conn.getContentLengthLong();
               //					System.err.println("cll = " + cll);
               //					
               //					
               //					String errData = "";
               //					String inputLine;
               //					while ((inputLine = in.readLine()) != null)
               //						errData += inputLine;
               //					in.close();
               //
               //					System.err.println("content = " + errData);
               throw err;
            }
            in = new BufferedReader(new InputStreamReader(conInStream));

         }
         String data = "";

         String inputLine;

         while ((inputLine = in.readLine()) != null)
            data += inputLine;
         in.close();
         conn.disconnect();

         // Parse returned data
         //			data = "[{" + data + "}]";
         JSONPoint mapping = JSONNode.parseJSONArrays(data);

         JSONArray results;
         {

            if(mapping instanceof JSONArray)
            {
               results = (JSONArray) ((JSONArray)mapping).getValues()[0];
            } else if (mapping != null)
            {
               throw new Error("Unexpected results type of " + mapping.getClass().getSimpleName());
            }
            else
            {
               results = new JSONArray();
            }
         }
         Vector<Result> returnSet = new Vector<Result>(REQUEST_SIZE);
         {
            for(Object result : results.getValues())
            {
               if(result instanceof JSONNode)
               {
                  String buyPrice;
                  String salePrice;
                  String buyQuantity;
                  String saleQuantity;
                  String resultID;

                  JSONNode item = (JSONNode) result;

                  {
                     Double rawValue = (Double)(item.getValue("id"));
                     resultID = ""+rawValue.intValue();
                  }
                  {
                     JSONNode buys = (JSONNode) item.getValue("buys");
                     buyPrice = "" + ((Double)buys.getValue("unit_price")).intValue();
                     buyQuantity = "" + ((Double)buys.getValue("quantity")).intValue();
                  }
                  {
                     JSONNode sells = (JSONNode) item.getValue("sells");
                     salePrice = "" + ((Double)sells.getValue("unit_price")).intValue();
                     saleQuantity = "" + ((Double)sells.getValue("quantity")).intValue();
                  }

                  Result resultRow = new Result();

                  resultRow.setItemID(resultID);
                  resultRow.setBuyNum(buyQuantity);
                  resultRow.setSellNum(saleQuantity);
                  resultRow.setBuyPrice(buyPrice);
                  resultRow.setSellPrice(salePrice);
                  returnSet.add(resultRow);
               }
            }
         }
         return returnSet;
      } catch(SSLHandshakeException err)
      {
         if(SSLContextProvider.handShakeErrorReceived(url, sslContext))
         {
            err.printStackTrace();
            throw new Error("Fatal SSL Error", err);
         } else
         {
            return getListingInfo(itemID);
         }
      } catch (IOException e)
      {
         organizeQueue = true;
         e.printStackTrace();
         return null;
      }

         }


   public class Result
   {
      @Override
      public String toString()
      {
         return "Result [itemID=" + itemID + ", buyPrice=" + buyPrice + ", buyNum="
               + buyNum + ", sellPrice=" + sellPrice + ", sellNum=" + sellNum
               + "]";
      }
      private String buyPrice;
      private String buyNum;
      private String sellPrice;
      private String sellNum;
      private String itemID;

      public String getItemID()
      {
         return itemID;
      }
      public void setItemID(String itemID)
      {
         this.itemID = itemID;
      }

      public void setItemID(int itemID)
      {
         this.itemID = ""+itemID;
      }

      public String getBuyPrice()
      {
         if(buyPrice== null)
         {
            return "0";
         }
         return buyPrice;
      }
      public void setBuyPrice(String buyPrice)
      {
         this.buyPrice = buyPrice;
      }
      public void setBuyPrice(int buyPrice)
      {
         this.buyPrice = "" + buyPrice;
      }

      public String getBuyNum()
      {
         if(buyNum == null)
         {
            return "0";
         }

         return buyNum;
      }
      public void setBuyNum(String buyNum)
      {
         this.buyNum = buyNum;
      }
      public void setBuyNum(int buyNum)
      {
         this.buyNum = ""+buyNum;
      }
      public String getSellPrice()
      {
         if(sellPrice== null)
         {
            return "0";
         }

         return sellPrice;
      }
      public void setSellPrice(String sellPrice)
      {
         this.sellPrice = sellPrice;
      }
      public void setSellPrice(int sellPrice)
      {
         this.sellPrice = ""+sellPrice;
      }

      public String getSellNum()
      {
         if(sellNum== null)
         {
            return "0";
         }

         return sellNum;
      }
      public void setSellNum(String sellNum)
      {
         this.sellNum = sellNum;
      }
      public void setSellNum(int sellNum)
      {
         this.sellNum = ""+sellNum;
      }
   }

   private class QueueComparator<T extends Item> implements Comparator<T>
   {

      @Override
      public int compare(Item o1, Item o2)
      {
         if(o1.isPriorityUpdate() && !o2.isPriorityUpdate())
            return -1;
         if(!o1.isPriorityUpdate() && o2.isPriorityUpdate())
            return 1;
         if(o1.getPricesUpdatedTimeStamp() < o2.getPricesUpdatedTimeStamp())
            return -1;
         if((o1.getPricesUpdatedTimeStamp() > o2.getPricesUpdatedTimeStamp()))
            return 1;
         return 0;

      }
   }

   static public boolean haveWorkQueued()
   {
      if(instance == null)
      {
         return false;
      }
      return instance.processQueue.size() != 0;
   }

   public static int getQueueSize()
   {
      return instance.processQueue.size();
   }


   static public Float getGemToGoldExchangeRate()
   {
      if(instance == null)
      {
         return null;
      }
      return instance.getGemToGoldExchangeRate(true);
   }
   private Float getGemToGoldExchangeRate(boolean askingInstance)
   {
      if(!interfaceEnabled)
      {
         return null;
      }

      synchronized(exchangeLockObject)
      {
         if(!updatingExchangeRates && exchangeRateTimeStamp + 600000 < System.currentTimeMillis())
         {
            fetchExchangeRates();
         }
      }
      return gemToGoldExchangeRate;
   }

   static public Float getGoldToGemExchangeRate()
   {

      if(instance == null)
      {
         return null;
      }
      return instance.getGoldToGemExchangeRate(true);
   }


   private Float getGoldToGemExchangeRate(boolean b)
   {
      if(!interfaceEnabled)
      {
         return null;
      }
      synchronized(exchangeLockObject)
      {
         if(!updatingExchangeRates && exchangeRateTimeStamp + 600000 < System.currentTimeMillis())
         {
            fetchExchangeRates();
         }
      }
      return goldToGemExchangeRate;
   }

   private void fetchExchangeRates()
   {
      synchronized (exchangeLockObject)
      {
         if(updatingExchangeRates)
         {
            return;
         }
         updatingExchangeRates = true;
      }
      Runnable doRun = new Runnable(){

         @Override
         public void run()
         {
            try
            {
               fetchGemToGoldExchangeRate();
               fetchGoldToGemsExchangeRate();
               exchangeRateTimeStamp = System.currentTimeMillis();
               synchronized(exchangeLockObject)
               {
                  updatingExchangeRates = false;

               }
            } catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      };
      new Thread(doRun, "QuizGemStore").start();
   }

   @SuppressWarnings("unused")
   private void fetchGoldToGemsExchangeRate() throws IOException
   {
      if(true) // FIXME
      {
         return;
      }
      URL url = null;
      SSLContext sslContext = null;
      try
      {
         url = new URL(
               "https://exchange-live.ncplatform.net/ws/trends.json?type=ReceivingGems");
         HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

         sslContext = SSLContextProvider.getSSLContextForURL(url);
         conn.setSSLSocketFactory(sslContext.getSocketFactory());

         conn.setDoOutput(true);
         conn.setDoInput(true);

         conn.setRequestMethod("GET");

         conn.setRequestProperty("Accept", "*/*");

         conn.setRequestProperty("x-requested-with", "XMLHttpRequest");

         conn.setRequestProperty("Accept-Encoding", "deflate");

         conn.setRequestProperty("Connection", "keep-alive");

         conn.setRequestProperty("User-Agent",
               ResourceManager.PROGRAM_NAME + " " + ResourceManager.VERSION);

         // Get response from trading-post
         BufferedReader in = new BufferedReader(new InputStreamReader(
               conn.getInputStream()));

         String data = "";

         String inputLine;

         while ((inputLine = in.readLine()) != null)
            data += inputLine;
         in.close();
         conn.disconnect();

         JSONNode mapping = new JSONNode(data);
         JSONArray plots = (JSONArray) mapping.getValue("plots");
         JSONPoint[] plotPoints = plots.getValues();
         String latestValue = "";
         String latestTime = "";
         for(JSONPoint plotPoint : plotPoints)
         {
            JSONNode plotNode = (JSONNode) plotPoint;
            String currentValue = (String) plotNode.getValue("average");
            String currentTime = (String) plotNode.getValue("last_updated");
            if(currentTime != null && currentValue != null && latestTime.compareTo(currentTime) < 0)
            {
               latestValue = currentValue;
               latestTime = currentTime;
            }
         }
         if(!latestValue.isEmpty())
         {
            float newValue = Float.parseFloat(latestValue);
            if(newValue != goldToGemExchangeRate)
            {
               goldToGemExchangeRate = newValue;
               ContextUpdateNotifier.notifyContentUpdates();
            }
         }
      } catch(SSLHandshakeException err)
      {
         if(SSLContextProvider.handShakeErrorReceived(url, sslContext))
         {
            err.printStackTrace();
            throw new Error("Fatal SSL Error", err);
         } else
         {
            fetchGoldToGemsExchangeRate();
            return;
         }
      }
   }

   @SuppressWarnings("unused")
   private void fetchGemToGoldExchangeRate() throws IOException
   {
      if(true) // FIXME
      {
         return;
      }
      URL url = null;
      SSLContext sslContext = null;
      try
      {
         url = new URL(
               "https://exchange-live.ncplatform.net/ws/trends.json?type=ReceivingCoins");
         HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

         sslContext = SSLContextProvider.getSSLContextForURL(url);
         conn.setSSLSocketFactory(sslContext.getSocketFactory());

         conn.setDoOutput(true);
         conn.setDoInput(true);

         conn.setRequestMethod("GET");

         conn.setRequestProperty("Accept", "*/*");

         conn.setRequestProperty("x-requested-with", "XMLHttpRequest");

         conn.setRequestProperty("Accept-Encoding", "deflate");

         conn.setRequestProperty("Connection", "keep-alive");

         conn.setRequestProperty("User-Agent",
               ResourceManager.PROGRAM_NAME + " " + ResourceManager.VERSION);

         // Get response from trading-post
         BufferedReader in = new BufferedReader(new InputStreamReader(
               conn.getInputStream()));

         String data = "";

         String inputLine;

         while ((inputLine = in.readLine()) != null)
            data += inputLine;
         in.close();
         conn.disconnect();

         JSONNode mapping = new JSONNode(data);
         JSONArray plots = (JSONArray) mapping.getValue("plots");
         JSONPoint[] plotPoints = plots.getValues();
         String latestValue = "";
         String latestTime = "";
         for(JSONPoint plotPoint : plotPoints)
         {
            JSONNode plotNode = (JSONNode) plotPoint;
            String currentValue = (String) plotNode.getValue("average");
            String currentTime = (String) plotNode.getValue("last_updated");
            if(currentTime != null && currentValue != null && latestTime.compareTo(currentTime) < 0)
            {
               latestValue = currentValue;
               latestTime = currentTime;
            }
         }
         if(!latestValue.isEmpty())
         {
            float newValue = Float.parseFloat(latestValue);
            if(newValue != gemToGoldExchangeRate)
            {
               gemToGoldExchangeRate = newValue;
               ContextUpdateNotifier.notifyContentUpdates();
            }
         }
      } catch(SSLHandshakeException err)
      {
         if(SSLContextProvider.handShakeErrorReceived(url,sslContext))
         {
            err.printStackTrace();
            throw new Error("Fatal SSL Error", err);
         } else
         {
            fetchGemToGoldExchangeRate();
            return;
         }
      } 
   }


   public static boolean haveCookie()
   {
      if(instance == null)
      {
         return false;
      }
      return instance.cookies != null && instance.cookies.size() > 1;
   }

   public static boolean isStuck()
   {
      if(instance == null)
      {
         return false;
      }
      return instance.isStuckImpl();
   }

   public boolean isStuckImpl()
   {
      if(startProcTimestamp == 0)
      {
         return false;
      }
      if(System.currentTimeMillis() - startProcTimestamp  > 30*1000)
      {
         return true;
      }
      return false;
   }


   public static boolean isInterfaceEnabled()
   {
      if(instance == null)
      {
         return false;
      }
      return instance.interfaceEnabled;
   }


   public static void setInterfaceEnabled(boolean interfaceEnabled)
   {
      if(instance != null)
      {
         instance.interfaceEnabled = interfaceEnabled;

         if(interfaceEnabled)
         {
            synchronized(instance)
            {
               instance.notifyAll();
            }
         } else
         {
            instance.cookies = null;
         }
      }
   }
}
