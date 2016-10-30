package com.torrenal.craftingGadget.transactions.sources;

import java.util.Arrays;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.statistics.drops.DropsDataSet;

public class Salvage extends DropsSource
{
   
   private final DropsDataSet dropsData;
   private final ItemQuantitySet[] inputs;
   private Value[] salePricing = null;
   private SalvageType salvageType = null;

   public Salvage(SalvageType salvageType, ItemQuantitySet[] inputs, DropsDataSet dropsData)
   {
      inputs = Arrays.copyOf(inputs, inputs.length + 1);
      ItemQuantitySet kit = null;
      this.salvageType = salvageType;
      switch(salvageType)
      {
         case BASIC:
            kit = new ItemQuantitySet(23040L, 1d/25d);
            break;
         case FINE:
            kit = new ItemQuantitySet(23041L, 1d/25d);
            break;
         case JOURNEYMAN:
            kit = new ItemQuantitySet(23042L, 1d/25d);
            break;
         case MASTER:
            kit = new ItemQuantitySet(23043L, 1d/25d);
            break;
         case BLACK_LION:
            kit = new ItemQuantitySet(23045L, 1d/25d);
      }
      inputs[inputs.length-1] = kit;
      this.inputs = inputs;
      this.dropsData = dropsData;
      registerInputsAndOutputs();
   }

   @Override
   public String getSourceVerb()
   {
      return "salvage";
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
      return salvageType + " salvage ";
   }

   @Override
   public String getSourceType()
   {
      return "Salvage";
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
