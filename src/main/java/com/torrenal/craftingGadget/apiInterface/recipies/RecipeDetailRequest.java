package com.torrenal.craftingGadget.apiInterface.recipies;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.PriorityQueue;

import com.torrenal.craftingGadget.apiInterface.http.HttpRequest;
import com.torrenal.craftingGadget.apiInterface.http.RequestTypes;
import com.torrenal.craftingGadget.apiInterface.json.JSONArray;
import com.torrenal.craftingGadget.apiInterface.json.JSONNode;
import com.torrenal.craftingGadget.apiInterface.json.JSONPoint;
import com.torrenal.craftingGadget.db.items.ItemDB;
import com.torrenal.craftingGadget.db.items.ItemRecord;
import com.torrenal.craftingGadget.db.recipes.RecipeDB;

public class RecipeDetailRequest extends HttpRequest
{

   private String recipeIDs;
   private long lastUpdateTimestamp;

   static final int BATCH_REQUEST_SIZE = 199;

   static final long RECENT_LOAD_INTERVAL = 10*60*1000;

   static private PriorityQueue<RecipeDetailRequestInfo> queue = new PriorityQueue<>();
   static private boolean requestInProgress = false;

   static public void requestRecipeDetailFor(long recipeID, long lastUpdateTimestamp)
   {
      RecipeDetailRequestInfo request = new RecipeDetailRequestInfo(recipeID, lastUpdateTimestamp);
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
         requestInProgress = true;
         try
         {
            if(queue.isEmpty())
            {
               return;
            }
            while(requestSize < BATCH_REQUEST_SIZE && !queue.isEmpty())
            {
               long now = System.currentTimeMillis();
               RecipeDetailRequestInfo request = queue.poll();
               long recipeID = request.getRecipeID();
               ItemRecord item = ItemDB.getItem(recipeID);
               if(!ItemDB.isItemValid(recipeID))
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
               requests = requests.append(",").append(recipeID);
               isPriority |= request.isPriority();
               long updateTimestamp = request.getLastUpdateTimestamp();
               oldestTimestamp = (oldestTimestamp > updateTimestamp ? updateTimestamp : oldestTimestamp);
            }
            if(requestSize == 0)
            {
               try
               {
                  queue.wait(1000);
               } catch (InterruptedException e)
               { // Harmless
               }
               return;
            }
            System.out.println("Processing recipe detail request for " + requestSize + " recipes.");
            requestString = requests.substring(1);
         } finally
         {
            requestInProgress = false;
         }
      }
      new RecipeDetailRequest(requestString, oldestTimestamp).queueRequest();
   }


   private RecipeDetailRequest(String recipeIDs, long lastUpdateTimestamp)
   {
      this.recipeIDs = recipeIDs;
      this.lastUpdateTimestamp = lastUpdateTimestamp;
   }

   @Override
   public URL getURL()
   {
      try
      {
         return new URL("https://api.guildwars2.com/v2/recipes?ids="+recipeIDs);
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
                  JSONNode recipeDetail = (JSONNode) iterator.next(); 
                  long id;

                  { // scope
                     double idDouble = (Double) recipeDetail.getValue("id");
                     id = (long) idDouble;
                  }
                  RecipeDB.getRecipeRecord(id).updateFromJSON(recipeDetail);
               } catch (Throwable err)
               {
                  err.printStackTrace();
               }
            }
            //			RecipeDB.getRecipeRecord(recipeID).updateFromJSON(getReplyJSON());
         } else
         {
            System.err.println("Error trying to get detail for Recipe IDs " + recipeIDs);
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

   static private class RecipeDetailRequestInfo implements Comparable<RecipeDetailRequestInfo>
   {
      private long lastUpdateTimestamp;
      private long recipeID;
      private boolean isPriority;
      private boolean isSkipifLoaded;

      /** At present, recipe requests don't use priorities or conditionally skip laoding */
      public RecipeDetailRequestInfo(long itemID, long lastUpdateTimestamp)
      {
         this(itemID, lastUpdateTimestamp, false, false);
      }

      public RecipeDetailRequestInfo(long itemID, long lastUpdateTimestamp, boolean isPriority, boolean isSkipIfLoaded)
      {
         this.lastUpdateTimestamp = lastUpdateTimestamp;
         this.recipeID = itemID;
         this.isPriority = isPriority;
         this.isSkipifLoaded = isSkipIfLoaded;
      }

      @Override
      public int compareTo(RecipeDetailRequestInfo that)
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

      public long getRecipeID()
      {
         return recipeID;
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
