package com.torrenal.craftingGadget.apiInterface.items;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.BatchUpdateException;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

import com.torrenal.craftingGadget.apiInterface.http.HttpRequest;
import com.torrenal.craftingGadget.apiInterface.http.RequestTypes;
import com.torrenal.craftingGadget.apiInterface.json.JSONArray;
import com.torrenal.craftingGadget.apiInterface.json.JSONNode;
import com.torrenal.craftingGadget.apiInterface.json.JSONPoint;
import com.torrenal.craftingGadget.db.items.ItemDB;
import com.torrenal.craftingGadget.db.items.ItemRecord;

public class ItemDetailRequest extends HttpRequest
{
   static final int BATCH_REQUEST_SIZE = 199;

   static final long RECENT_LOAD_INTERVAL = 10*60*1000;

   static private PriorityQueue<ItemDetailRequestInfo> queue = new PriorityQueue<>();
   static private boolean requestInProgress = false;

   static public void requestItemDetailFor(long itemID, long lastUpdateTimestamp, boolean isPriorityRequest, boolean isSkipIfLoaded)
   {
      ItemDetailRequestInfo request = new ItemDetailRequestInfo(itemID, lastUpdateTimestamp, isPriorityRequest, isSkipIfLoaded);
      synchronized(queue)
      {
         queue.add(request);
         if(!requestInProgress)
         {
            processNextRequest();
         }
      }
   }
   
   static public int getQueueDepth()
   {
      synchronized(queue)
      {
         return queue.size();
      }
   }

   static private void processNextRequest()
   {
      StringBuilder requests = new StringBuilder();
      int requestSize = 0;

      String requestString;
      boolean isPriority = false;
      long  oldestTimestamp = Long.MAX_VALUE;

      synchronized(queue)
      {
         if(requestInProgress)
         {
            return;
         }
         if(queue.isEmpty())
         {
            return;
         }
         while(requestSize < BATCH_REQUEST_SIZE && !queue.isEmpty())
         {
            long now = System.currentTimeMillis();
            ItemDetailRequestInfo request = queue.poll();
            long itemID = request.getItemID();
            ItemRecord item = ItemDB.getItem(itemID);
            if(!ItemDB.isItemValid(itemID))
            {
               continue;
            }
            if(request.isSkipIfLoaded())
            {
               if(item.getLastUpdateTimestamp() > 1000)
               {
                  continue;
               }
            }
            // Always skip if recently loaded
            if(item.getLastUpdateTimestamp() + RECENT_LOAD_INTERVAL > now )
            {
               continue;
            }

            requestSize++;
            requests = requests.append(",").append(itemID);
            isPriority |= request.isPriority();
            long updateTimestamp = request.getLastUpdateTimestamp();
            oldestTimestamp = (oldestTimestamp > updateTimestamp ? updateTimestamp : oldestTimestamp);
         }
//         System.err.println("Processing item detail request for " + requestSize + " items.");
         if(requestSize == 0)
         {
            return;
         }
         requestString = requests.substring(1);
         requestInProgress = true;
      }
      new ItemDetailRequest(requestString, oldestTimestamp, isPriority).queueRequest();
   }



   String itemIDList;
   private long lastUpdateTimestamp;
   public boolean printReply = false;
   private boolean priorityLoad = false;

   private ItemDetailRequest(String itemIDList, long lastUpdateTimestamp, boolean priorityLoad)
   {
      this.itemIDList = itemIDList;
      this.lastUpdateTimestamp = lastUpdateTimestamp;
      this.priorityLoad = priorityLoad;
   }

   @Override
   public URL getURL()
   {
      try
      {
         return new URL("https://api.guildwars2.com/v2/items?ids=" + itemIDList);
      } catch (MalformedURLException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public long getPriority()
   {
      return lastUpdateTimestamp;
   }

   @Override
   public int getQueueLevel()
   {
      if(priorityLoad)
      {
         return RequestTypes.PRIORITY_REQUEST.ordinal();
      }
      return RequestTypes.BACKGROUND_REQUEST.ordinal();
   }

   /** This method will be invoked once the server reply is received */
   @Override
   protected void processReply()
   {
      try
      {
         if(reply != null)
         {
            if(printReply)
            {
               System.err.println(reply);
            }

            JSONArray arrayData = (JSONArray) JSONNode.parseJSONArrays(reply);
            arrayData = (JSONArray) arrayData.getValues()[0];
            Iterator<JSONPoint> iterator = arrayData.iterator();
            while(iterator.hasNext())
            {
               try
               {
                  JSONNode itemDetail = (JSONNode) iterator.next(); 
                  long id;
                  
                  { // scope
                     double idDouble = (Double) itemDetail.getValue("id");
                     id = (long) idDouble;
                  }
                  ItemDB.getItem(id, true).updateFromJSON(itemDetail);
               } catch (Throwable err)
               {
                  err.printStackTrace();
               }
            }
         }
         else
         { 
            return;
         }
      } finally
      {
         synchronized(queue)
         {
            requestInProgress = false;
            processNextRequest();
         }
      }
   }

   public void setPriorityLoad()
   {
      priorityLoad = true;
   }

   static private class ItemDetailRequestInfo implements Comparable<ItemDetailRequestInfo>
   {
      private long lastUpdateTimestamp;
      private long itemID;
      private boolean isPriority;
      private boolean isSkipifLoaded;

      public ItemDetailRequestInfo(long itemID, long lastUpdateTimestamp, boolean isPriority, boolean isSkipIfLoaded)
      {
         this.lastUpdateTimestamp = lastUpdateTimestamp;
         this.itemID = itemID;
         this.isPriority = isPriority;
         this.isSkipifLoaded = isSkipIfLoaded;
      }

      @Override
      public int compareTo(ItemDetailRequestInfo that)
      {
         if(this.isPriority == that.isPriority)
         {
            if(this.lastUpdateTimestamp > that.lastUpdateTimestamp)
            {
               return 1;
            }
            if(this.lastUpdateTimestamp < that.lastUpdateTimestamp)
            {
               return -1;
            }
         } else
         {
            if(this.isPriority)
            {
               return -1;
            }
            else
            {
               return 1;
            }
         }

         return 0;
      }

      public long getItemID()
      {
         return itemID;
      }

      public long getLastUpdateTimestamp()
      {
         return lastUpdateTimestamp;
      }
      public boolean isPriority()
      {
         return isPriority;
      }
      public boolean isSkipIfLoaded()
      {
         return isSkipifLoaded;
      }

   }

}
