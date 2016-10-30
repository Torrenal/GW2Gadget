package com.torrenal.craftingGadget.statistics.drops;

import java.util.List;
import java.util.Vector;

import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.ObjectQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;

public class DropDataRow<T extends Object>
{

   Vector<ObjectQuantitySet<T>> drops;
   int sampleSize;
   
   public DropDataRow(int sampleSize, List<? extends ObjectQuantitySet<T>> drops)
   {
      this.sampleSize = sampleSize;
      this.drops = new Vector<>(drops);
   }
   
   public int getSampleSize()
   {
      return sampleSize;
   }
   
   @SuppressWarnings("unchecked")
   public Vector<ObjectQuantitySet<T>> getDrops()
   {
      return (Vector<ObjectQuantitySet<T>>) drops.clone();
   }

   public DataPoint<Double> getDropsForProduct(T product)
   {
      for(ObjectQuantitySet<T> drop : drops)
      {
         if(drop.getItem().equals(product))
         {
            return new DataPoint<Double>(drop.getQuantity(), sampleSize);      
         }
      }
      return new DataPoint<Double>(0d, sampleSize);
   }
   
   public Value getValueOfDrops()
   {
      
      Value value = new Value(false);
      for(ObjectQuantitySet<T> drop : drops)
      {
         if(drop instanceof ItemQuantitySet)
         {
            value = value.add(((ItemQuantitySet) drop).getSalePriceBest());
         } else
         {
            throw new UnsupportedOperationException("Operation only supported for item quantity sets, not " + drop.getClass());
         }
      }
      return value;
   }
}
