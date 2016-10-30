package com.torrenal.craftingGadget.db.recipes;

import java.io.Serializable;
import java.util.Vector;

import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.apiInterface.json.JSONArray;
import com.torrenal.craftingGadget.apiInterface.json.JSONNode;
import com.torrenal.craftingGadget.apiInterface.json.JSONPoint;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONString;

public class RecipeRecord implements Serializable
{
   private static final long serialVersionUID = 3L;

   private long lastUpdateTimestamp = 0L;
   private CraftingDataPair productItem = null;
   private Long productItemId;
   private Long productUpgradeId = -1L;
   private Long recipeID = null;
   private int difficulty;
   private Vector<String> disciplines;
   private Vector<CraftingDataPair> components = null;
   private Vector<UpgradeDataPair> upgradeComponents = null;

   private Vector<Long> componentIDs = null;

   public RecipeRecord(Long recipeID)
   {
      this.recipeID = recipeID;
      this.lastUpdateTimestamp = 0L;
   }


   public void updateFromJSON(JSONNode replyJSON)
   {

      /* Verify ID */

      { // scope

         double idDouble = (Double) replyJSON.getValue("id");
         long parsedID = (long) idDouble;

         if(parsedID != recipeID)
         {
            return;
         }
      }

      /* parse product */
      {
         productItemId = (long)((double)((Double)replyJSON.getValue("output_item_id")));

         int outputItemCount = (int)((double)((Double)replyJSON.getValue("output_item_count")));
         productItem = new CraftingDataPair(productItemId, outputItemCount);
      }
      /* parse guild upgrade output detail */
      {
         Object rawUpgradeID = replyJSON.getValue("output_upgrade_id");
         if(rawUpgradeID != null)
         {
            productUpgradeId = (long)((double)((Double)rawUpgradeID));

         } else
         {
            //		      GuildUpgradeDB.unregisterUpgrade(this, outputItemID);  -- Needed, but how?
            // For now, restart after downloading to enforce any missed deletes.
            productUpgradeId = null;
         }

      }
      /* parse components */
      {
         JSONArray componentsJSON = (JSONArray) replyJSON.getValue("ingredients");
         JSONPoint[] componentsArray = componentsJSON.getValues();
         components = new Vector<>(4);
         for(JSONPoint component : componentsArray)
         {
            JSONNode compNode = (JSONNode)component;
            int count = (int)((double)((Double)compNode.getValue("count")));
            long compID = (long)((double)((Double)compNode.getValue("item_id")));
            components.add(new CraftingDataPair(compID, count));
         }
      }
      /* parse guild components */
      {
         Object componentsJSON = replyJSON.getValue("guild_ingredients");
         if(componentsJSON != null)
         {
            JSONPoint[] componentsArray = ((JSONArray)componentsJSON).getValues();
            upgradeComponents = new Vector<>(4);
            for(JSONPoint component : componentsArray)
            {
               JSONNode compNode = (JSONNode)component;
               int count = (int)((double)((Double)compNode.getValue("count")));
               long compID = (long)((double)((Double)compNode.getValue("upgrade_id")));
               upgradeComponents.add(new UpgradeDataPair(compID, count));
            }
         } else
         {
            upgradeComponents = new Vector<>(0);
         }
      }
      /* parse other data */
      {
         //Difficulty
         {
            difficulty = (int)((double)((Double)replyJSON.getValue("min_rating")));
         }
         //Disciplines
         disciplines = parseDisciplines((JSONArray) replyJSON.getValue("disciplines"));
      }
      lastUpdateTimestamp = System.currentTimeMillis();
      RecipeDB.getRecipe(recipeID).updateFromRecipe();
      //		System.out.println("Updated: " + this);
   }

   private Vector<String> parseDisciplines(JSONArray disciplinesJSON)
   {
      Vector<String> result = new Vector<>();
      if(disciplinesJSON == null)
      {
         result.add("??");
         return result;
      }
      JSONPoint[] values = disciplinesJSON.getValues();
      for(JSONPoint value : values)
      {
         result.add(((JSONString)value).getValue());
      }
      RecipeDB.recipeRecordUpdated(this);
      return result;
   }


   public long getLastUpdateTimestamp()
   {
      return lastUpdateTimestamp;
   }

   public ItemQuantitySet[] getOutputs()
   {
      ItemQuantitySet[] ret = new ItemQuantitySet[1];
      ret[0] = new ItemQuantitySet(productItem.getItem(), productItem.getQuantity());
      return ret;
      
   }
   
   public Long getRecipeID()
   {
      return recipeID;
   }


   public int getDifficulty()
   {
      return difficulty;
   }


   public String[] getDisciplines()
   {
      if(disciplines == null)
      {
         String[] result = { "??" };
         return result;
      }
      return disciplines.toArray(new String[0]);
   }


   public ItemQuantitySet[] getInputs()
   {
      ItemQuantitySet[] ret = new ItemQuantitySet[components.size()];
      for(int i = 0; i < components.size(); i++)
      {
         CraftingDataPair item = components.get(i);
         ret[i] = new ItemQuantitySet(item.getItem(), item.getQuantity());
      }
      return ret;
   }

   /**
    * Returns the list of guild upgrades neede for ingredients
    * @return
    */
   public Vector<UpgradeDataPair> getUpgradeComponents()
   {
      return upgradeComponents;
   }

   public Vector<Long> getComponentIDs()
   {
      return componentIDs;
   }

   public void resetTimestamp()
   {
      lastUpdateTimestamp = System.currentTimeMillis();
   }

   public Long getProductUpgradeID()
   {
      return productUpgradeId;
   }


   public long getProductItemId()
   {
      return productItemId;
   }

   @Override
   public String toString()
   {
      if(productItem != null)
      {
         Item item = productItem.getItem();
         String itemName = "Unk";
         if(item != null)
         {
            itemName = item.getName();
         }

         return "Recipe " + recipeID + " - " + itemName;
      }
      return "Recipe " + recipeID + " - Unk";
   }

}
