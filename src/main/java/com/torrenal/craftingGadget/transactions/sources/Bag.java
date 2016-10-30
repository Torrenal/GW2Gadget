package com.torrenal.craftingGadget.transactions.sources;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.statistics.drops.DropsDataSet;

public class Bag extends DropsSource
{
   
   private final DropsDataSet dropsData;
   private final ItemQuantitySet[] inputs;
   private Value[] salePricing = null;

   public Bag(ItemQuantitySet[] inputs, DropsDataSet dropsData)
   {
      this.inputs = inputs;
      this.dropsData = dropsData;
      registerInputsAndOutputs();
   }

   @Override
   public String getSourceVerb()
   {
      return "open";
   }

   @Override
   public ItemQuantitySet[] getInputs()
   {
      return inputs.clone();
   }

   @Override
   public ItemQuantitySet[] getOutputs()
   {
      return dropsData.getAverageDrops();
   }
   
   @Override
   public ItemQuantitySet[] getOutputsMinimums()
   {
      return dropsData.getMinimumDrops();
   }
   
   @Override
   public ItemQuantitySet[] getOutputErrors()
   {
      return dropsData.getErrorBars();
   }



   @Override
   public String getMethodName()
   {
      return "Open ";
   }

   @Override
   public String getSourceType()
   {
      return "Loot Bag";
   }

   public Value getSalePrice()
   {
      Value[] prices = salePricing;
      
      if(prices == null)
      {
         prices = dropsData.getValueAndError();
         salePricing = prices;
      }
      return prices[0];
   }


   public Value getSalePriceError()
   {
      Value[] prices = salePricing;
      
      if(prices == null)
      {
         prices = dropsData.getValueAndError();
         salePricing = prices;
      }
      return prices[1];
   }
   
   public void  discardCalculatedValues()
   {
      super.discardCalculatedValues();

      salePricing = null;
   }

   public Object getSourceNameFor(Item item)
   {
      if(!(item instanceof APIItem))
      {
         APIItem apiItem = item.getAPIItem();
         if(apiItem != null)
         {
            item = apiItem;
         }
         for(ItemQuantitySet product : getOutputs())
         {
            if(item.equals(product))
            {
               double qty = product.getQuantity();
               if(qty == 0)
               {
                  return "Infinity " + getSourceName();
               }
               else
               {
                  return CookingCore.doubleToString(1/qty) + " " + getSourceName();
               }
            }
         }
      }
      
            
      return null;
   }
}
