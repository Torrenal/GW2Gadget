package com.torrenal.craftingGadget.db.recipes;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.torrenal.craftingGadget.ContextUpdateNotifier;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.ResourceManager;
import com.torrenal.craftingGadget.apiInterface.json.JSONArray;
import com.torrenal.craftingGadget.apiInterface.json.JSONNode;
import com.torrenal.craftingGadget.apiInterface.json.JSONPoint;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONNumber;
import com.torrenal.craftingGadget.apiInterface.recipies.RecipeDetailRequest;
import com.torrenal.craftingGadget.apiInterface.recipies.RecipeListRequest;
import com.torrenal.craftingGadget.db.items.ItemDB;
import com.torrenal.craftingGadget.transactions.sources.APIRecipe;
import com.torrenal.craftingGadget.transactions.sources.Source;
import com.torrenal.craftingGadget.util.ThreadPool;

public class RecipeDB
{
   static final public long REFRESH_PERIOD = 5 * 24 * 60 * 60 * 1000; /* 5 days (prime number ) */
   static final public long DEMAND_REFRESH_WINDOW = 6 * 60 * 60 * 1000; /* 6 hours */

   static public Hashtable<Long,RecipeRecord> recipeRecords = null;
   static private Object recipeRecordsLock = new Object();
   static public Hashtable<Long,APIRecipe> recipes = null;

   static private Thread updateRunner = null;
   private static Object saveLockOb = new Object();
   private static boolean saving = false;
   private static boolean initializing = true;
   private static int retryCount = 0;
   private static final int MAX_RETRIES = 10;

   public static void initDB()
   {
      recipeRecords = new Hashtable<>(7000);
      recipes = new Hashtable<>(7000);

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
         updateRunner = new Thread(doRun, "RecipeDB-Updater");
         updateRunner.setPriority(Thread.NORM_PRIORITY+2);
         updateRunner.start();
      }
   }

   private static void doUpdate()
   {
      loadRecipes();
      synchronized(RecipeDB.class)
      {
         initializing = false;
         RecipeDB.class.notifyAll();
      }

      updateValidRecipes();


      ItemDB.matchStaticItems();

   }

   private static void loadRecipes()
   {
      File recipeDatFile = ResourceManager.getRecipeDBDatFile();
      if(!recipeDatFile.canRead())
      {
         return;
      }
      ObjectInputStream recipeDatStream = ResourceManager.getObjectInputStream(recipeDatFile);
      if(recipeDatStream == null)
      {
         return;
      }
      while(true)
      {
         Object next;
         try
         {
            next = recipeDatStream.readObject();
         } catch (ClassNotFoundException | IOException e)
         {
            /* File corrupt?  Junk the record */
            e.printStackTrace();
            try
            {
               recipeDatStream.close();
            } catch (IOException e1)
            { }
            recipeDatFile.delete();
            break;
         }
         if(next instanceof EOF)
         {
            break;
         }
         RecipeRecord recipeRecord = (RecipeRecord) next;
         if(recipeRecord.getLastUpdateTimestamp() > System.currentTimeMillis())
         {
            recipeRecord.resetTimestamp();
         }
         if(recipeRecord.getLastUpdateTimestamp() > 1)
         {
            synchronized(recipeRecordsLock)
            {
               recipeRecords.put(recipeRecord.getRecipeID(), recipeRecord);
            }
            APIRecipe recipe = new APIRecipe(recipeRecord);
            synchronized(recipeRecordsLock)
            {
               recipes.put(recipeRecord.getRecipeID(), recipe);
            }
         }

      }
      try
      {
         recipeDatStream.close();
      } catch (IOException e)
      {
         e.printStackTrace();
      }
      ContextUpdateNotifier.notifyStructureUpdates(); /* Signal the Discipline filter to update choices */

   }



   private static void updateValidRecipes()
   {
      synchronized(recipeRecordsLock)
      {

         JSONNode replyJSON = new RecipeListRequest().getReplyJSON();
         JSONArray recipeJSON = (JSONArray) replyJSON.getValue("recipes");
         HashSet<Long> validRecipeIDs = new HashSet<>();
         for(JSONPoint value : recipeJSON.getValues())
         {
            if(value instanceof JSONNumber)
            {
               double newValue = ((JSONNumber)value).getValue();
               validRecipeIDs.add((long) newValue);
            }
         }
         Enumeration<Long> recipeKeys = recipeRecords.keys();
         while(recipeKeys.hasMoreElements())
         {
            Long key = recipeKeys.nextElement();
            if(validRecipeIDs.contains(key))
            {
               continue;
            }

            recipeRecords.remove(key);
            recipes.remove(key);
         }

         long now = System.currentTimeMillis();
         long threshold = now - REFRESH_PERIOD;
         for(final Long recipeID : validRecipeIDs)
         {
            RecipeRecord recipe = recipeRecords.get(recipeID);
            if(recipe == null)
            {
               RecipeRecord recipeRecord = new RecipeRecord(recipeID);
               recipeRecords.put(recipeID, recipeRecord);
               Runnable doRun = new Runnable() {
                  public void run()
                  {
                     RecipeDetailRequest.requestRecipeDetailFor(recipeID, 1L);
                  }
               };
               ThreadPool.submitTask(doRun);
            } else
            {
               Long lastUpdate = recipe.getLastUpdateTimestamp();
               if(lastUpdate < threshold)
               {
                  RecipeDetailRequest.requestRecipeDetailFor(recipeID, lastUpdate);
               }
            }
         }
      }
   }

   public static RecipeRecord getRecipeRecord(long recipeID)
   {
      synchronized(recipeRecordsLock)
      {
         return recipeRecords.get(recipeID);
      }
   }
   public static APIRecipe getRecipe(long recipeID)
   {
      APIRecipe recipe;
      synchronized(recipeRecordsLock)
      {
         recipe = recipes.get(recipeID);
      }
      if(recipe == null)
      {
         RecipeRecord recipeRecord;
         synchronized(recipeRecordsLock)
         {
            recipeRecord = recipeRecords.get(recipeID);
            if(recipeRecord == null)
            {
               recipeRecord = new RecipeRecord(recipeID);
               RecipeDetailRequest.requestRecipeDetailFor(recipeID, 1L);
            }
            recipe = new APIRecipe(recipeRecord);
            recipes.put(recipeID, recipe);
         }
         return recipe;
      }
      return recipe;
   }

   /* Blocks the current thread until the init completes */
   @SuppressWarnings("unused")
   private static void blockForInit()
   {
      if(initializing)
      {
         synchronized(RecipeDB.class)
         {
            while(initializing)
            {
               try
               {
                  RecipeDB.class.wait();
               } catch (InterruptedException e)
               { // Normal Outcome
               }
            }
         }
      }
   }

   /** Used primarily to save the recipedb every 100th time an item is loaded. */
   public static void recipeRecordUpdated(RecipeRecord record)
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
         saving  = true;
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

            File saveFile = ResourceManager.getRecipeDBDatScratchFile();
            ObjectOutputStream saveObjectStream = ResourceManager.getObjectOutputStream(saveFile);

            Hashtable<Long, RecipeRecord> recipeCopy;
            synchronized(recipeRecordsLock)
            {
               recipeCopy = recipeRecords;
            }
            Collection<RecipeRecord> saveUs = recipeCopy.values();
            for(RecipeRecord recipe : saveUs)
            {
               try
               {
                  saveObjectStream.writeObject(recipe);
               } catch (IOException e)
               {
                  e.printStackTrace();
               }
            }
            try
            {
               saveObjectStream.writeObject(new EOF());
               saveObjectStream.close();
               ResourceManager.getRecipeDBDatFile().delete();
               saveFile.renameTo(ResourceManager.getRecipeDBDatFile());
            } catch (IOException err)
            {
               new Error("Error saving " + ResourceManager.getRecipeDBDatFile(), err).printStackTrace();
               System.exit(-1);
            }
            ItemDB.matchStaticItems();
            synchronized(saveLockOb)
            {
               saving = false;
            }
         }
      };
      Thread thread = new Thread(doRun,"RecipeDB");
      thread.setDaemon(true);
      thread.setPriority(Thread.NORM_PRIORITY-1);
      thread.start();
   }

   public static void retryMissingRecipes()
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

      Hashtable<Long, RecipeRecord> recipeClone = (Hashtable<Long, RecipeRecord>) new Hashtable<>(recipeRecords);

      Collection<RecipeRecord> recipeSet = recipeClone.values();
      for(RecipeRecord recipe : recipeSet)
      {
         if(recipe.getLastUpdateTimestamp() < 10)
         {
            RecipeDetailRequest.requestRecipeDetailFor(recipe.getRecipeID(), recipe.getLastUpdateTimestamp());
         }
      }
   }

   public static boolean isInitializing()
   {
      return initializing;
   }

   public static void updateDetailsFor(Source rootSource)
   {
      ItemQuantitySet[] outputs = rootSource.getOutputs();

      // Use the source tree for one of the outputs, since they'll all include the necessary items.
      Vector<Item> treeSet = Item.walkPricingTree(outputs[0].getItem());
      for(Item item : treeSet)
      {
         Collection<Source> sources = item.getSources();
         for(Source source : sources)
         {
            if(source instanceof APIRecipe)
            {
               long lastUpdate = ((APIRecipe) source).getLastUpdateTimestamp();
               if(lastUpdate + DEMAND_REFRESH_WINDOW < System.currentTimeMillis())
               {
                  ((APIRecipe) source).updateRecipeFromAPI();
               }
            }
         }
      }
   }

   public static void updateDetailsFor(Item product)
   {
      Vector<Item> treeSet = Item.walkPricingTree(product);
      for(Item item : treeSet)
      {
         Collection<Source> sources = item.getSources();
         for(Source source : sources)
         {
            if(source instanceof APIRecipe)
            {
               long lastUpdate = ((APIRecipe) source).getLastUpdateTimestamp();
               if(lastUpdate + DEMAND_REFRESH_WINDOW < System.currentTimeMillis())
               {
                  ((APIRecipe) source).updateRecipeFromAPI();
               }
            }
         }
      }
   }
}
