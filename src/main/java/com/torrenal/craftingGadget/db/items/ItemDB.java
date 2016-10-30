package com.torrenal.craftingGadget.db.items;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.ResourceManager;
import com.torrenal.craftingGadget.apiInterface.items.ItemDetailRequest;
import com.torrenal.craftingGadget.apiInterface.items.ItemListRequest;
import com.torrenal.craftingGadget.apiInterface.json.JSONArray;
import com.torrenal.craftingGadget.apiInterface.json.JSONNode;
import com.torrenal.craftingGadget.apiInterface.json.JSONPoint;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONNumber;
import com.torrenal.craftingGadget.transactions.sources.Source;

public class ItemDB
{
   static final public long REFRESH_PERIOD = 17 * 24 * 60 * 60 * 1000; /* 17 days (prime number ) */
   static final public long DEMAND_REFRESH_WINDOW = 6 * 60 * 60 * 1000; /* 6 hours */

   static private Hashtable<Long,ItemRecord> items = null;
   static private HashSet<Long> validIDs = null;
   static private int retryCount = 0;
   static final public int MAX_RETRIES = 5; 

   static private Thread updateRunner = null;
   static private boolean initializing = true;
   static private boolean saving = false;
   static private Object saveLockOb = new Object();
   static private boolean matchingItems = false;
   static private boolean matchMoreItems = false;
   static private Object matchLockOb = new Object();
   private static boolean talkToServers = true;
   private static boolean isValidIDsSet = false;


   public static void initDB(boolean talkToServers)
   {
      items = new Hashtable<>(35000);
      validIDs = new HashSet<>(35000);
      ItemDB.talkToServers = talkToServers;
      startUpdate();
   }

   private static void startUpdate()
   {
      if(updateRunner == null || !updateRunner.isAlive())
      {
         Runnable doRun = new Runnable()
         {
            public void run()
            {
               doUpdate();
            }

         };
         updateRunner = new Thread(doRun, "ItemDB-Updater");
         updateRunner.setPriority(Thread.NORM_PRIORITY+1);
         updateRunner.start();
      }
   }


   private static void doUpdate()
   {
      loadItems();
      /* Free up blocked threads - we are open for business */
      synchronized(ItemDB.class)
      {
         initializing = false;
         ItemDB.class.notifyAll();
      }
      matchStaticItems();
      if(talkToServers)
      {
         updateValidItems();
         queuePriorityLoad();
      }
   }

   private static void queuePriorityLoad()
   {
      File quickLoadFile = ResourceManager.getQuickLoadFile();
      Scanner inputStream = ResourceManager.getTextInputStream(quickLoadFile);


      //		int lineNum = 0;

      while(inputStream.hasNext())
      {
         String line;
         line = inputStream.nextLine();
         //			lineNum++;
         line = line.trim();
         String[] bits = line.split(",");
         int priority = Integer.parseInt(bits[0]);
         long itemID = Long.parseLong(bits[1]);

         if(!validIDs.contains(itemID))
         {
            continue; 
         }

         ItemRecord item = items.get(itemID);
         if(item == null || item.getLastUpdateTimestamp() < 10)
         {
            ItemDetailRequest.requestItemDetailFor(itemID, priority, true, true);
         }
      }
   }

   public static boolean isItemValid(long itemID)
   {
      blockForValidIDs();
      return validIDs.contains(itemID);
   }

   private static void loadItems()
   {
      File itemDatFile = ResourceManager.getItemDBDatFile();
      if(!itemDatFile.canRead())
      {
         return;
      }
      ObjectInputStream itemDatStream = ResourceManager.getObjectInputStream(itemDatFile);
      if(itemDatStream == null)
      {
         return;
      }
      while(true)
      {
         Object next;
         try
         {
            next = itemDatStream.readObject();
         } catch (ClassNotFoundException | IOException err)
         {
            /* File corrupt?  Junk the record */
            err.printStackTrace();
            try
            {
               itemDatStream.close();
            } catch (IOException urk)
            { }
            itemDatFile.delete();
            break;
         }
         if(next instanceof EOF)
         {
            break;
         }
         ItemRecord item = (ItemRecord) next;
         if(item.getLastUpdateTimestamp() > System.currentTimeMillis())
         {
            item.resetTimestamp();
         }
         items.put(item.getItemID(), item);
//         if(item.getFullName().contains("Gift of Metal"))
//         {
//            System.out.println("Found Gift o Metal");
//         }
         item.linkToStaticRecords();
      }
      try
      {
         itemDatStream.close();
      } catch (IOException e)
      {
         e.printStackTrace();
      }

   }

   private static void updateValidItems()
   {
      String reply = new ItemListRequest().getReplyString();
      JSONArray replyJSON = (JSONArray) JSONNode.parseJSONArrays(reply);
      JSONArray itemsJSON = (JSONArray) (replyJSON.getValues()[0]);
      validIDs = new HashSet<>();
      for(JSONPoint value : itemsJSON.getValues())
      {
         if(value instanceof JSONNumber)
         {
            double newValue = ((JSONNumber)value).getValue();
            validIDs.add((long) newValue);
         }
      }
      
      isValidIDsSet  = true;
      synchronized(ItemDB.class)
      {
         ItemDB.class.notifyAll();
      }
      
      Enumeration<Long> itemKeys = items.keys();
      long now = System.currentTimeMillis();
      long threshold = now - REFRESH_PERIOD;
      while(itemKeys.hasMoreElements())
      {
         Long key = itemKeys.nextElement();
         if(validIDs.contains(key))
         {
            ItemRecord item = items.get(key);
            if(item.getLastUpdateTimestamp() < threshold)
            {
               fetchDataFor(key, false);
            }
            continue;
         }

         items.remove(key);
         CookingCore.removeItemByID(key);
      }
   }

   public static boolean haveItem(Long itemID)
   {
      blockForInit();
      return items.contains(itemID);
   }

   /* Blocks the current thread until the init completes */
   public static void blockForInit()
   {
      if("ItemDB-Updater".equals(Thread.currentThread().getName()))
      {
         return;
      }
      if(initializing)
      {
         synchronized(ItemDB.class)
         {
            while(initializing)
            {
               try
               {
                  ItemDB.class.wait();
               } catch (InterruptedException e)
               { // Normal Outcome
               }
            }
         }
      }
   }

   public static void blockForValidIDs()
   {
      if(!isValidIDsSet)
      {
         synchronized(ItemDB.class)
         {
            while(!isValidIDsSet)
            {
               try
               {
                  ItemDB.class.wait();
               } catch (InterruptedException e)
               { // Normal Outcome
               }
            }
         }
      }
   }


   public static void fetchDataFor(Long itemID, boolean gagHTTPRequest)
   {
      ItemRecord item = items.get(itemID);
      if(item == null)
      {
         blockForInit();
         items.put(itemID, new ItemRecord(itemID));
         if(!gagHTTPRequest)
         {
            ItemDetailRequest.requestItemDetailFor(itemID, 0L, false, true);
         }
         CookingCore.findItemByID(itemID); /* Forces the CookingCore to instantiate an item */
      } else
      {
         if(!gagHTTPRequest)
         {
            ItemDetailRequest.requestItemDetailFor(itemID, item.getLastUpdateTimestamp(), false, false);
         }
      }
   }

   private static void queueUnknownItems()
   {
      for(Long itemID : validIDs)
      {
         if(items.get(itemID) == null)
         {
            items.put(itemID, new ItemRecord(itemID));
            ItemDetailRequest.requestItemDetailFor(itemID, 9L, false, true);
         }
      }
   }

   public static ItemRecord getItem(long itemID)
   {
      return getItem(itemID, false);
   }

   public static ItemRecord getItem(long itemID, boolean gagHTTPRequest)
   {
      blockForInit();
      ItemRecord item = items.get(itemID);
      if(item == null)
      {
         item = new ItemRecord(itemID);
         items.put(itemID, item);
         fetchDataFor(itemID, gagHTTPRequest);
         return items.get(itemID);
      } else
      {
         return item;
      }
   }

   /** Used primarily to save the itemdb every 100th time an item is loaded. */
   public static void itemRecordUpdated(ItemRecord record)
   {
      saveRecords();
   }

   private static void saveRecords()
   {
      synchronized(saveLockOb)
      {
         if(saving)
         {
            return;
         }
         saving = true;
      }
      Runnable doRun = new Runnable()
      {
         public void run()
         {

            synchronized (saveLockOb)
            {
               try
               {
                  saveLockOb.wait(15000);
               } catch (InterruptedException e)
               { }
            }
            File saveFile = ResourceManager.getItemDBDatScratchFile();
            ObjectOutputStream saveObjectStream = ResourceManager.getObjectOutputStream(saveFile);

            ItemRecord[] saveUs = items.values().toArray(new ItemRecord[0]);
            for(ItemRecord item : saveUs)
            {
               try
               {
                  saveObjectStream.writeObject(item);
               } catch (IOException e)
               {
                  e.printStackTrace();
               }
            }
            try
            {
               saveObjectStream.writeObject(new EOF());
               saveObjectStream.close();
               ResourceManager.getItemDBDatFile().delete();
               saveFile.renameTo(ResourceManager.getItemDBDatFile());
            } catch (IOException err)
            {
               new Error("Error saving " + ResourceManager.getItemDBDatFile(), err).printStackTrace();
               System.exit(-1);
            }
            matchStaticItems();
            synchronized(saveLockOb)
            {
               saving = false;
            }
         }
      };
      Thread thread = new Thread(doRun, "Item-Save-Waiter");
      thread.setDaemon(true);
      thread.setPriority(Thread.NORM_PRIORITY-1);
      thread.start();
   }

   public static void retryMissingItems()
   {
      if(initializing)
      {
         return;
      }
      if(retryCount > MAX_RETRIES)
      {
         return;
      }
      retryCount++;
      @SuppressWarnings("unchecked")
      Collection<ItemRecord> itemSet = ((Hashtable<Long, ItemRecord>) items.clone()).values();

      for(ItemRecord item : itemSet)
      {
         if(item.getLastUpdateTimestamp() < 10)
         {
            ItemDetailRequest.requestItemDetailFor(item.getItemID(), item.getLastUpdateTimestamp(), false, false);
         }
      }

      queueUnknownItems();
   }

   public static boolean isInitializing()
   {
      return initializing;
   }

   public static void matchStaticItems()
   {
      synchronized(matchLockOb)
      {
         if(matchingItems)
         {
            matchMoreItems = true;
            return;
         }
         matchingItems = true;
      }
      Runnable doRun = new Runnable()
      {
         public void run()
         {
            try
            {
               synchronized(matchLockOb)
               {
                  try
                  {
                     /* Throttle */
                     matchLockOb.wait(10000);
                  } catch (InterruptedException e)
                  { }
               }
               ItemRecord itemSet[] = items.values().toArray(new ItemRecord[0]);
               for(ItemRecord item : itemSet)
               {
                  if(item.getLastUpdateTimestamp() < 10)
                  {
                     continue;
                  }
                  item.linkToStaticRecords();
                  Thread.yield();
               }
            } finally
            {
               matchingItems = false;
               if(matchMoreItems)
               {
                  matchStaticItems();
               }
            }
         }
      };
      Thread thread = new Thread(doRun, "Matchy");
      thread.setDaemon(true);
      thread.setPriority(Thread.NORM_PRIORITY-1);
      thread.start();

   }

   public static void reportMatchesWith(String regex)
   {
      if(regex == null || regex.isEmpty())
      {
         return;
      }
      if(!regex.startsWith("^"))
      {
         regex = "^.*" + regex;
      }
      if(!regex.endsWith("$"))
      {
         regex = regex+".*$";
      }

      System.out.println("-----------------");
      ItemRecord[] itemSet = items.values().toArray(new ItemRecord[0]);
      for(ItemRecord item : itemSet)
      {
         String fullName = item.getFullName();
         if(fullName.matches(regex))
         {
//            boolean valid = ItemDB.isItemValid(item.getItemID());
//            APIItem itemData = CookingCore.findItemByID(item.getItemID());
            
//            boolean tradeable = PriceTool.checkIsTradeable(item.getItemID());
//            PriceTool.performPriorityPricingUpdate(CookingCore.findItemByID(item.getItemID()), true);
            System.out.println(fullName + "  - " + item.getItemID() + " " + Item.getPingCode(""+item.getItemID()));
            
            
            //				ItemDetailRequest itemRequest = new ItemDetailRequest(item.getItemID(),0L);
            //				itemRequest.printReply = true;
            //				itemRequest.queueRequest();
         }
      }

   }

   public static void updateDetailsFor(Source rootSource)
   {
      ItemQuantitySet[] outputs = rootSource.getOutputs();
      Vector<Item> treeSet = Item.walkPricingTree(outputs[0].getItem());
      for(Item item : treeSet)
      {
         if(item instanceof APIItem)
         {
            long lastUpdate = ((APIItem) item).getLastUpdateTimestamp();
            if(lastUpdate + DEMAND_REFRESH_WINDOW < System.currentTimeMillis())
            {
               ((APIItem) item).updateItemFromAPI();
            }
         }
      }
   }

   public static void updateDetailsFor(Item product)
   {
      Vector<Item> treeSet = Item.walkPricingTree(product);
      for(Item item : treeSet)
      {
         if(item instanceof APIItem)
         {
            long lastUpdate = ((APIItem) item).getLastUpdateTimestamp();
            if(lastUpdate + DEMAND_REFRESH_WINDOW < System.currentTimeMillis())
            {
               ((APIItem) item).updateItemFromAPI();
            }
         }
      }
   }
}
