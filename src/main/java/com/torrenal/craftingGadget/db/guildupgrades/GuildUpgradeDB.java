package com.torrenal.craftingGadget.db.guildupgrades;

import java.util.HashSet;
import java.util.Hashtable;

import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.apiInterface.guildupgrades.GuildUpgradeDetailRequest;
import com.torrenal.craftingGadget.apiInterface.guildupgrades.UpgradeListRequest;
import com.torrenal.craftingGadget.apiInterface.json.JSONArray;
import com.torrenal.craftingGadget.apiInterface.json.JSONNode;
import com.torrenal.craftingGadget.apiInterface.json.JSONPoint;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONNumber;
import com.torrenal.craftingGadget.db.items.ItemDB;
import com.torrenal.craftingGadget.db.recipes.RecipeRecord;

public class GuildUpgradeDB
{
   static private Hashtable<Long,GuildUpgradeRecord> upgrades = new Hashtable<>(400);
   private static long updateSequence = 0;
   private static HashSet<Long> validIDs = null;

   static
   {
      Runnable doRun = new Runnable() {
         
         @Override
         public void run()
         {
            init();
         }
      };
      Thread thread = new Thread(doRun, "GuildUpgradeQueryer");
      thread.setDaemon(true);
      thread.start();
   }
   
   private static void init()
   {
      ItemDB.blockForInit();
      
      updateValidUpgrades();
      fetchUpgradeDetails();
   }
   
   private static void updateValidUpgrades()
   {
      UpgradeListRequest upgradeListRequest = new UpgradeListRequest();
      upgradeListRequest.queueRequest();
      String reply = upgradeListRequest.getReplyString();
      JSONArray jsonData = (JSONArray)JSONNode.parseJSONArrays(reply);
      JSONArray validItemSet = (JSONArray)(jsonData.getValues()[0]);
      JSONPoint[] values = validItemSet.getValues();
      HashSet<Long> validIds = new HashSet<>();
      for(JSONPoint value : values)
      {
         long id = (long)((double)(((JSONNumber)value).getValue()));
         validIds.add(id);
      }
      GuildUpgradeDB.validIDs = validIds;
      synchronized(GuildUpgradeDB.class)
      {
         GuildUpgradeDB.class.notifyAll();
      }
   }

   private static void fetchUpgradeDetails()
   {
      Long[] ids = validIDs.toArray(new Long[0]);
      for(Long id : ids)
      {
         GuildUpgradeDetailRequest.requestUpgradeDetailFor(id, -1L);
      }
      
   }

   
   public static boolean haveUpgrade(Long upgradeID)
   {
      return upgrades.contains(upgradeID);
   }

   public static GuildUpgradeRecord getUpgrade(long upgradeID)
   {
      GuildUpgradeRecord upgrade = upgrades.get(upgradeID);
      if(upgrade == null)
      {
         upgrade = new GuildUpgradeRecord(upgradeID);
         upgrades.put(upgradeID, upgrade);
      }
      return upgrade;
   }
   
   public static void blockForValidIDs()
   {
      while(validIDs == null)
      {
         synchronized(GuildUpgradeDB.class)
         {
            try
            {
               GuildUpgradeDB.class.wait();
            } catch (InterruptedException e)
            { //harmless
            }
         }
      }
   }
   
   public static synchronized void registerUpgrade(RecipeRecord recipe, Long itemID, long upgradeID)
   {
      GuildUpgradeRecord record = upgrades.get(upgradeID);
      if(record == null)
      {
         record = new GuildUpgradeRecord(upgradeID);
         upgrades.put(upgradeID, record);
      }
      record.addRecipeDetails(itemID, recipe);
   }

   public static Item findItemByUpgradeID(Long upgradeID)
   {
      Item item = getUpgrade(upgradeID).getItem();
      return item;
   }

   public static void unregisterUpgrade(RecipeRecord recipeRecord,
         long outputItemID)
   {
      // TODO Needed, but impractial.
      // for now, restarting thhe client suffices.
      
   }

   public static long getUpdateSequence()
   {
      return updateSequence ;
   }
   
   public static void updateSequence()
   {
      updateSequence++;
   }

   public static void registerUpgrade(long guildUpgradeID, long itemID)
   {
      GuildUpgradeRecord record = getUpgrade(guildUpgradeID);
      record.registerStaticUpgrade(itemID);
   }

   public static boolean isUpgradeIDValid(long upgradeID)
   {
      blockForValidIDs();
      return validIDs.contains(upgradeID);
   }
}
