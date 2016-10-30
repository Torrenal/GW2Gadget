package com.torrenal.craftingGadget;

import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.db.recipes.UpgradeDataPair;

/**
 * Strictly an item + quantity, with some utility added.
 * 
 * @author Eric
 */
public class ItemQuantitySet extends ObjectQuantitySet<Item>
{
   boolean isAPI = false;
   boolean isGuildUpgrade = false;
   UpgradeDataPair upgradeItem = null;
   
   public ItemQuantitySet(Item item, double quantity)
   {
      super(item, quantity);
   }

   public ItemQuantitySet(UpgradeDataPair input)
   {
      super(null, input.getQuantity());
      isGuildUpgrade = true;
      upgradeItem = input;
   }

   public ItemQuantitySet(long itemId, double quantity)
   {
      this(CookingCore.findItemByID(itemId), quantity);
   }

   public Value getSalePriceLessSaleCostBest()
   {
      return getItem().getSalePriceLessSaleCostBest().multiply(quantity);
   }

   public Value getSalePriceLessSaleCostFast()
   {
      return getItem().getSalePriceLessSaleCostBest().multiply(quantity);
   }
   
   public Value getSalePriceBest()
   {
      Value value = getItem().getSaleValueBest();
      return value.multiply(quantity);
   }

   public Value getSalePriceFast()
   {
      Value value = getItem().getSaleValueFast();
      return value.multiply(quantity);
   }

   public String toString()
   {
      if(getItem().getClass() == Item.class)
      {
         // Non API items get quotes
         return CookingCore.doubleToString(quantity) + " \"" + item.getName() + "\"";
      }
      return CookingCore.doubleToString(quantity) + " " + item.getName();
   }

   public Object toPrecisionString()
   {
      if(getItem().getClass() == Item.class)
      {
         // Non API items get quotes
         return CookingCore.doubleToPrecisionString(quantity) + " \"" + item.getName() + "\"";
      }
      return CookingCore.doubleToPrecisionString(quantity) + " " + item.getName();
   }

   public Item getItem()
   {
      if(isGuildUpgrade && item == null)
      {
         Item resolvedItem = upgradeItem.getItem();
         if(resolvedItem instanceof ItemUnknown)
         {
            return resolvedItem;
         }
         item = resolvedItem;
         isAPI = true;
      }
         
      if(isAPI)
      {
         return super.getItem();
      }
      Item item = super.getItem();

//      if(item.getName().contains("Gift of Metal"))
//      {
//         System.out.println("Found metal!");
//      }
      APIItem newItem = item.getAPIItem();
      if(newItem != null)
      {
         updateItem(newItem);
         item = newItem;
      }
      isAPI = item instanceof APIItem;
      return item;
   }

   public String toStringWithMult(double multiplier)
   {
      return CookingCore.doubleToString(quantity * multiplier) + " " + getItem().getName();
   }

   public Value getObtainCost()
   {
      Value value = getItem().getBestObtainCost();
      return value.multiply(quantity);
   }

   public String getItemName()
   {
      if(getItem().getClass() == Item.class)
      {
         // Non API items get quotes
         return "\"" + item.getName() + "\"";
      }
      return item.getName();
   }
}
