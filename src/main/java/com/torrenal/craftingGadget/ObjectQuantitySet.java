package com.torrenal.craftingGadget;

public class ObjectQuantitySet<T extends Object>
{

   protected T item;
   protected double quantity;

   public ObjectQuantitySet(T item, double quantity)
   {
      this.item = item;
      this.quantity = quantity;
   }

   public T getItem()
   {
      return item;
   }
   
   public double getQuantity()
   {
      return quantity;
   }

   
   protected void updateItem(T newItem)
   {
      item = newItem;
   }

}