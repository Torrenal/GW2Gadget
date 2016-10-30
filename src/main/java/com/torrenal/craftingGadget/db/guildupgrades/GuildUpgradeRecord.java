package com.torrenal.craftingGadget.db.guildupgrades;

import java.io.Serializable;
import java.util.HashSet;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.EvaluateEngine;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemUnknown;
import com.torrenal.craftingGadget.db.recipes.RecipeRecord;

public class GuildUpgradeRecord implements Serializable
{
   private static final long serialVersionUID = 1L;

   private long upgradeID;
   private APIItem item;
   private Long itemId;
   private Long staticSourcedItemId = null;
   
   private HashSet<UpgradeInfoToken> detailTokens = new HashSet<>(1);

   public GuildUpgradeRecord(long upgradeID)
   {
      this.upgradeID = upgradeID;
      item = null;
      itemId = null;
   }

   public long getUpgradeID()
   {
      return upgradeID;
   }

   public void addRecipeDetails(long itemID, RecipeRecord recipe)
   {
      UpgradeInfoToken details = new UpgradeInfoToken(itemID, recipe);
      
      synchronized(this)
      {
         UpgradeInfoToken[] tokens = detailTokens.toArray(new UpgradeInfoToken[0]);
         
         // No dups, remove any existing entries 
         Long recipeID = recipe.getRecipeID();
         for(UpgradeInfoToken token : tokens)
         {
            if(token.getRecipe().getRecipeID().equals(recipeID))
            {
               if(token.getItemId() == itemID)
               {
                  return;
               }
               detailTokens.remove(token);
            }
         }

         detailTokens.add(details);
         GuildUpgradeDB.updateSequence();
         validateTokens();
      }
   }

   private void validateTokens()
   {
      synchronized(this)
      {
         Long itemId = staticSourcedItemId;
         for(UpgradeInfoToken token : detailTokens)
         {
            if(itemId == null)
            {
               itemId = token.getItemId();
               continue;
            }
            
            long nextId = token.getItemId();
            if(nextId != itemId)
            { // Different valid item IDs, consider all to be invalid.
               itemId = null;
               break;
            }
         }
         if(itemId == null && this.itemId == null)
         {
            return;
         }
         if(itemId != null && itemId.equals(this.itemId))
         {
            return;
         }
         this.itemId = itemId;
         this.item = CookingCore.findItemByID(itemId);
         EvaluateEngine.evaluate(item);
      }
   }

   public static class UpgradeInfoToken
   {
      long itemId;
      RecipeRecord recipe;

      public UpgradeInfoToken(long itemId, RecipeRecord recipe)
      {
         this.itemId = itemId;
         this.recipe = recipe;
      }

      public long getItemId()
      {
         return itemId;
      }

      public RecipeRecord getRecipe()
      {
         return recipe;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         long recipeID = -1;
         if(recipe != null)
         {
            recipeID = recipe.getRecipeID();
         }
         result = prime * result + (int) (itemId ^ (itemId >>> 32));
         result = prime * result + (int) (recipeID ^ (recipeID >>> 32));
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         UpgradeInfoToken other = (UpgradeInfoToken) obj;
         if (itemId != other.itemId)
            return false;
         if (recipe == null)
         {
            if (other.recipe != null)
               return false;
         } else if (!recipe.getRecipeID().equals(other.recipe.getRecipeID()))
            return false;
         return true;
      }
      
     
   }

   public Item getItem()
   {
      if(item != null)
      {
         return item;
      }
      return ItemUnknown.getInstance();
   }

   public void registerStaticUpgrade(long numericItemId)
   {
      if(staticSourcedItemId != null && staticSourcedItemId != numericItemId)
      {
         throw new IllegalStateException("Static numeric ID mismatch for Guild Upgrade " + upgradeID + ": " + staticSourcedItemId + " != " + numericItemId);
      }
      staticSourcedItemId = numericItemId;
      GuildUpgradeDB.updateSequence();
      validateTokens();
      
   }

      
}
