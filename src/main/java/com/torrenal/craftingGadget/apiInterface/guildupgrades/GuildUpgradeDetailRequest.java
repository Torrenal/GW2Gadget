package com.torrenal.craftingGadget.apiInterface.guildupgrades;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.PriorityQueue;

import com.torrenal.craftingGadget.apiInterface.http.HttpRequest;
import com.torrenal.craftingGadget.apiInterface.http.RequestTypes;
import com.torrenal.craftingGadget.apiInterface.json.JSONArray;
import com.torrenal.craftingGadget.apiInterface.json.JSONNode;
import com.torrenal.craftingGadget.apiInterface.json.JSONPoint;
import com.torrenal.craftingGadget.db.guildupgrades.GuildUpgradeDB;

public class GuildUpgradeDetailRequest extends HttpRequest
{

   private String upgradeIDs;
   private long lastUpdateTimestamp;

   static final int BATCH_REQUEST_SIZE = 199;

   static final long RECENT_LOAD_INTERVAL = 10*60*1000;

   static private PriorityQueue<GuildUpgradeDetailRequestInfo> queue = new PriorityQueue<>();
   static private Object queueLock = new Object();
   static private boolean requestInProgress = false;

   static public void requestUpgradeDetailFor(long upgradeID, long lastUpdateTimestamp)
   {
      GuildUpgradeDetailRequestInfo request = new GuildUpgradeDetailRequestInfo(upgradeID, lastUpdateTimestamp);
      synchronized(queueLock)
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
      synchronized(queueLock)
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

      synchronized(queueLock)
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
            GuildUpgradeDetailRequestInfo request = queue.poll();
            long upgradeID = request.getUpgradeID();
            if(!GuildUpgradeDB.isUpgradeIDValid(upgradeID))
            {
               continue;
            }
//            if(request.isSkipIfLoaded())
//            {
//               if(item.getLastUpdateTimestamp() > 1000)
//               {
//                  continue;
//               }
//            }
//            // Always skip if recently loaded
//            if(item.getLastUpdateTimestamp() + RECENT_LOAD_INTERVAL > now )
//            {
//               continue;
//            }

            requestSize++;
            requests = requests.append(",").append(upgradeID);
            isPriority |= request.isPriority();
            long updateTimestamp = request.getLastUpdateTimestamp();
            oldestTimestamp = (oldestTimestamp > updateTimestamp ? updateTimestamp : oldestTimestamp);
         }
         if(requestSize == 0)
         {
            try
            {
               Thread.sleep(1000);
            } catch (InterruptedException e)
            { // Harmless
            }
            return;
         }
         System.out.println("Processing guild upgrade detail request for " + requestSize + " recipes.");
         requestString = requests.substring(1);
         requestInProgress = true;
      }
      new GuildUpgradeDetailRequest(requestString, oldestTimestamp).queueRequest();
   }


   private GuildUpgradeDetailRequest(String upgradeIDs, long lastUpdateTimestamp)
   {
      this.upgradeIDs = upgradeIDs;
      this.lastUpdateTimestamp = lastUpdateTimestamp;
   }

   @Override
   public URL getURL()
   {
      try
      {
         return new URL("https://api.guildwars2.com/v2/guild/upgrades?ids="+upgradeIDs);
      } catch (MalformedURLException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   @Override
   public long getPriority()
   {
      return lastUpdateTimestamp;
   }

   @Override
   public int getQueueLevel()
   {
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
            JSONArray arrayData = (JSONArray) JSONNode.parseJSONArrays(reply);
            arrayData = (JSONArray) arrayData.getValues()[0];
            Iterator<JSONPoint> iterator = arrayData.iterator();
            while(iterator.hasNext())
            {
               try
               {
                  JSONNode guildUpgradeDetail = (JSONNode) iterator.next(); 
                  long id;

                  { // scope
                     double idDouble = (Double) guildUpgradeDetail.getValue("id");
                     id = (long) idDouble;
                  }
                  { // Check type
                     String type = (String)guildUpgradeDetail.getValue("type");
                     if(!"Decoration".equals(type))
                     {
                        continue;
                     }
                  }
                  { // Check for source item
                     JSONArray costs = (JSONArray)guildUpgradeDetail.getValue("costs");
                     if(costs == null)
                     {
                        continue;
                     }
                     {
                        JSONPoint[] values = costs.getValues();
                        if(values.length == 0)
                        {
                           continue;
                        }
                        JSONNode value = (JSONNode) values[0];
                        String type = (String) value.getValue("type");
                        if(!"Item".equals(type))
                        {
                           continue;
                        }
                        Double itemId = (Double) value.getValue("item_id");
                        if(itemId == null)
                        {
                           continue;
                        }
                        GuildUpgradeDB.registerUpgrade(id, (long)((double)itemId));
                     }
                     
                     costs.toString();
                  }
               } catch (Throwable err)
               {
                  err.printStackTrace();
               }
            }
            //			RecipeDB.getRecipeRecord(recipeID).updateFromJSON(getReplyJSON());
         } else
         {
            System.err.println("Error trying to get detail for Recipe IDs " + upgradeIDs);
         }
      } finally
      {
         synchronized(queueLock)
         {
            requestInProgress = false;
            processNextRequest();
         }
      }
   }

   static private class GuildUpgradeDetailRequestInfo implements Comparable<GuildUpgradeDetailRequestInfo>
   {
      private long lastUpdateTimestamp;
      private long upgradeID;
      private boolean isPriority;
      private boolean isSkipifLoaded;

      /** At present, recipe requests don't use priorities or conditionally skip laoding */
      public GuildUpgradeDetailRequestInfo(long itemID, long lastUpdateTimestamp)
      {
         this(itemID, lastUpdateTimestamp, false, false);
      }

      public GuildUpgradeDetailRequestInfo(long upgradeID, long lastUpdateTimestamp, boolean isPriority, boolean isSkipIfLoaded)
      {
         this.lastUpdateTimestamp = lastUpdateTimestamp;
         this.upgradeID = upgradeID;
         this.isPriority = isPriority;
         this.isSkipifLoaded = isSkipIfLoaded;
      }

      @Override
      public int compareTo(GuildUpgradeDetailRequestInfo that)
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

      public long getUpgradeID()
      {
         return upgradeID;
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
