package com.torrenal.craftingGadget.db.items;

import java.io.Serializable;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemKey;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.apiInterface.json.JSONArray;
import com.torrenal.craftingGadget.apiInterface.json.JSONNode;
import com.torrenal.craftingGadget.apiInterface.json.JSONPoint;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONString;
import com.torrenal.craftingGadget.db.recipes.RecipeDB;
import com.torrenal.craftingGadget.transactions.sources.APIRecipe;
import com.torrenal.craftingGadget.transactions.sources.Source;

public class ItemRecord implements Serializable
{
   private static final long serialVersionUID = 1L;
   static HashSet<String> flagSet = new HashSet<>();  

   private long itemID;
   private String itemName = "??";
   private String itemRarity = "";
   private int itemLevel = 0;
   private String itemType = "";
   private Long merchValue = null;
   private long lastUpdateTimestamp;
   private boolean isTradeable = true;
   private boolean isMerchable = true;
   private boolean isConsumable = false;
   private Long recipeID = null;

   public ItemRecord(long itemID)
   {
      this.itemID = itemID;
      lastUpdateTimestamp = 0;
   }

   public long getLastUpdateTimestamp()
   {
      return lastUpdateTimestamp;
   }

   public void updateFromJSON(JSONNode replyJSON)
   {
      itemName = (String) replyJSON.getValue("name");
      itemRarity = (String) replyJSON.getValue("rarity");
      itemLevel = (int)((double)(Double)replyJSON.getValue("level"));
      itemType = (String) replyJSON.getValue("type");
      merchValue = (long)((double)(Double)replyJSON.getValue("vendor_value"));
      String description = (String) replyJSON.getValue("description");

      //		if(itemName.contains("Recipe: Satch"))
      //		{
      //			System.err.println("Found " + itemName + "(" + getItemID() + ")");
      //		}

      if(itemLevel == 0)
      {
         itemLevel = parseLevelFromDescription(description);
      }

      //String description =  (String) replyJSON.getValue("description");
      Vector<String> gameTypes = new Vector<>();
      {
         JSONArray gameTypesJSON = (JSONArray) replyJSON.getValue("game_types");
         if(gameTypesJSON != null)
         {
            for(JSONPoint flag : gameTypesJSON)
            {
               String typeString = ((JSONString)flag).getValue(); 
               gameTypes.add(typeString);
               if(!flagSet.contains(typeString))
               {
                  synchronized(flagSet)
                  {
                     flagSet.add(typeString);
                  }
                  System.err.println("Flags found: "  + flagSet);
               }
            }
         }
         if(gameTypes.contains("Pvp") && !gameTypes.contains("Pve"))
         {
            itemName = "PvP " + itemName;
         }
      }
      Vector<String> flags = new Vector<>(5);
      {
         JSONArray flagsJSON = (JSONArray) replyJSON.getValue("flags");
         if(flagsJSON != null)
         {
            for(JSONPoint flag : flagsJSON)
            {
               String flagString = ((JSONString)flag).getValue(); 
               flags.add(flagString);
               if(!flagSet.contains(flagString))
               {
                  synchronized(flagSet)
                  {
                     flagSet.add(flagString);
                  }
                  System.err.println("Flags found: "  + flagSet);
               }
            }
         }

      }

      {
         boolean unTradeable = false;
         if(flags.contains("AccountBound"))
         {
            unTradeable = true;
         }
         if(flags.contains("SoulbindOnAcquire"))
         {
            unTradeable = true;
         }
         isTradeable = !unTradeable;
      }

      if(replyJSON.getValue("consumable") != null)
      {
         isConsumable = true;
      } else
      {
         isConsumable = false;
      }

      if(isConsumable)
      {
         JSONNode consumableJSON = (JSONNode) replyJSON.getValue("consumable");
         String recipeIDString = (String) consumableJSON.getValue("recipe_id");
         if(recipeIDString != null && !recipeIDString.isEmpty())
         {
            recipeID = Long.parseLong(recipeIDString);
         }
      }

      boolean isNoSell = flags.contains("NoSell");  

      if(merchValue <=  0 || isNoSell)
      {
         merchValue = null;
         isMerchable = false;
      } else
      {
         isMerchable = true;
      }

      linkToStaticRecords();
      CookingCore.findItemByID(itemID).updateFromRecord();
      lastUpdateTimestamp = System.currentTimeMillis();
      ItemDB.itemRecordUpdated(this);
   }

   public void linkToStaticRecords()
   {
      if(lastUpdateTimestamp < 10)
      {
         return;
      }

//      if(itemName.toLowerCase().contains("lettuce") )
//      {
//         System.err.println("Found: " + itemName);
//         itemName.toString();
//      }
      if(itemLevel == 0 && recipeID != null)
      {
         if(!RecipeDB.isInitializing())
         {
            APIRecipe recipe = RecipeDB.getRecipe(recipeID);
            ItemQuantitySet[] outputs = recipe.getOutputs();
            if(outputs != null && outputs.length == 1)
            {
               Item product = outputs[0].getItem();
               Integer level = product.getItemLevel();
               itemLevel = (level == null) ? 0 : level;
               if(product.isConsumable())
               {
                  itemLevel = 0;
               }
            }
            ItemDB.itemRecordUpdated(this);
         }
      }

      String fullName = getFullName(itemName, itemLevel, itemRarity);

      //	   if(itemName.toLowerCase().contains("savory spin"))
      //	   {
      //	   	System.err.println("Found: " + fullName);
      //	   }
      ItemKey key = new ItemKey(fullName);
      Item staticItem = CookingCore.getStaticItem(key);
      
      if(staticItem != null)
      {

         // Some items can't link properly, they literally have multiple soulbound/account bound matches.
         // Since they all work the same, and are soulbound, we don't worry too much about
         // how they are obtained.
         
         // About 8 different bags of jewels exist 
         if("Bag of Jewels".equals(staticItem.getName()))
         {
            return;
         }
         //2 different Cultivated Vines exist 
         if("Cultivated Vine [80](Exotic)".equals(staticItem.getName()))
         {
            return;
         }
         //2 different fractal capacitors exist - One allows for an offensive infusion, the other defensive
         if("Fractal Capacitor (Infused) [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("Prototype Fractal Capacitor [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("Prototype Fractal Capacitor [80](Exotic)".equals(staticItem.getName()))
         {
            return;
         }
         if("Quiver of Swift Flight (Infused) [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("Book of Secrets (Infused) [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("Bowyer's Delight (Infused) [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("Tome of the Rubicon (Infused) [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("Endless Quiver (Infused) [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("Symon's History of Ascalon (Infused) [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("Sights Be True (Infused) [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("Yakkington: A Traveler's Tale (Infused) [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("There with Yakkington: A Traveler's Tale (Infused) [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("Quiver of a Thousand Arrows (Infused) [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("Koss on Koss (Infused) [80](Ascended)".equals(staticItem.getName()))
         {
            return;
         }
         if("Kryta's Salvation [80](Exotic)".equals(staticItem.getName()))
         {
            return;
         }
         if(staticItem.getName().endsWith("(Legendary)") && !staticItem.getName().startsWith("Gift of"))
         {
            return;
         }
         if("Mystery Tonic".equals(staticItem.getName()))
         {
            return;
         }
         //2 different Sclerite Karka Shells exist, one cannot be upgraded
         if("Sclerite Karka Shell [80](Exotic)".equals(staticItem.getName()))
         {
            return;
         }
         //FIXME: Multiple leataher bags, one is tradable
         if("Leather Bag".equals(staticItem.getName()))
         {
            return;
         }         

         APIItem dynamicItem = CookingCore.findItemByID(itemID);
         staticItem.linkTo(dynamicItem);
         Source[] sources = staticItem.getSources().toArray(new Source[0]);

         for(Source source : sources)
         {
            dynamicItem.addSource(source);
            source.convertItem(staticItem, dynamicItem);
         }
         Item[] products = staticItem.getProducts().toArray(new Item[0]);
         for(Item product : products)
         {
            if(product == null)
            {
               new NullPointerException("item " + this + " has null products").printStackTrace();
            }
            product.convertItem(staticItem, dynamicItem);
            dynamicItem.addProduct(product);
         }
      }
   }

   public long getItemID()
   {
      return itemID;
   }

   public String getItemName()
   {
      return itemName;
   }

   public String getItemRarity()
   {
      return itemRarity;
   }

   public int getItemLevel()
   {
      return itemLevel;
   }

   public String getItemType()
   {
      return itemType;
   }

   public Long getMerchValue()
   {
      return merchValue;
   }

   public boolean isTradeable()
   {
      return isTradeable;
   }

   public boolean isMerchable()
   {
      return isMerchable;
   }

   /** Used to set the timestamp to now.
    * Used if the timestamp is in the future.
    */
   public void resetTimestamp()
   {
      lastUpdateTimestamp = System.currentTimeMillis();
   }


   public String getFullName(String name, int level, String rarity)
   {
      return ItemRecord.getFullName(name, level, rarity, isConsumable);
   }

   public static String getFullName(String name, int level, String rarity, boolean isConsumable)
   {
      //		if(name.contains("lnir"))
      //		{
      //			System.err.println("Meep");
      //		}
      String pad = " ";
      if(level > 0 && !isConsumable)
      {
         name = name + pad + "["+level+"]";
         pad = "";
      }
      if(rarity != null && !rarity.isEmpty() && !"Basic".equals(rarity))
      {
         name += pad + "(" + rarity + ")";
      }
      return name;
   }

   public Long getRecipeID()
   {
      return recipeID;
   }

   public boolean isConsumable()
   {
      return isConsumable;
   }

   private int parseLevelFromDescription(String description)
   {
      if(description == null)
      {
         return 0;
      }
      int level = 0;
      StringTokenizer tokenizer = new StringTokenizer(description, "., \n\t\"");
      while(tokenizer.hasMoreTokens() && level == 0)
      {
         String textToken = tokenizer.nextToken();
         if(!"level".equalsIgnoreCase(textToken))
         {
            continue;
         }
         while(tokenizer.hasMoreTokens() && level == 0)
         {
            String levelString = tokenizer.nextToken();
            if(levelString.isEmpty())
            {
               continue;
            }
            try
            {
               level = Integer.parseInt(levelString);
            } catch(NumberFormatException err)
            {
               level = 0;
               break;
            }
            if(level > 80 || level < 0)
            {
               level = 0;
            }
            break;
         }
      }
      //System.err.println("level = " + level + " / " + description);
      return level;
   }

   public String getFullName()
   {
      if(itemName == null || "??".equals(itemName))
      {
         return "??";
      }
      return getFullName(itemName, itemLevel, itemRarity);
   }
}
