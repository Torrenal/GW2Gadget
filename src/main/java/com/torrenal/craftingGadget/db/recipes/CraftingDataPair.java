package com.torrenal.craftingGadget.db.recipes;

import java.io.Serializable;

import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;


public class CraftingDataPair implements Serializable
{
   private static final long serialVersionUID = 1L;

   Long itemID;
   int quantity;

   public CraftingDataPair(Long itemID, int quantity)
   {
      super();
      this.itemID = itemID;
      this.quantity = quantity;
   }

   public Item getItem()
   {
      return CookingCore.findItemByID(itemID);
   }
   
   public int getQuantity()
   {
      return quantity;
   }
}
