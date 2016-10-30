package com.torrenal.craftingGadget.transactions.sources;

import java.util.Vector;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQtyWrapper;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.dataModel.value.ValueCoin;

public class BuyOnMarket extends Source
{
   private Item item;
   private Value cost;
   private ItemQuantitySet[] outputs;
   boolean overbid = false;

   public BuyOnMarket(Item item, int lowPrice, int highPrice)
   {
      this.item = item;
      int minCap = (int) Math.ceil(highPrice / 1.3);
      int price;
      if(minCap  > lowPrice)
      {
         price = minCap;
         overbid = true;
      } else
      {
         price = lowPrice;
      }
      
      this.cost = new Value(new ValueCoin(price));
      outputs = new ItemQuantitySet[1];
      outputs[0] = new ItemQuantitySet(item, 1);
   }

   @Override
   public Value getObtainOneCost(Item item)
   {
      return cost;
   }
   
   @Override
   public ItemQuantitySet[] getOutputs()
   {
      return outputs;
   }
   
   @Override
   public double getOutputQty(Item product)
   {
      return 1;
   }


   public String toString()
   {
      if(overbid)
      {
         return "Buy with overbid from marketplace: " + cost;
      } else
      {
         return "Buy from marketplace: " + cost;
      }
   }

   @Override
   public String getMethodName()
   {
      return "Buy";
   }

   @Override
   public String getSourceName()
   {
      return "Market";
   }

   @Override
   public boolean derivesFrom(Item item)
   {
      return false;
   }

   @Override
   public String getObtainString()
   {
      return getObtainString(item, 1);
   }


   @Override
   public String getIngredientDescriptionFor(double quantity)
   {
      StringBuilder ret = new StringBuilder();
      Value unitValue = cost;
      Value lineTotal = unitValue.multiply(quantity);

      String qty = CookingCore.doubleToString(quantity);

      ret.append(qty);
      ret.append(" ").append(item.getName());
      ret.append("\n   - Line Total: ").append(lineTotal);
      ret.append("\n   - Buy from Market for ").append(unitValue);
      return ret.toString();
   }

   @Override
   public String getObtainString(Item product, double obtainQuantity)
   {
      String qty = CookingCore.doubleToString(obtainQuantity * 1);
      return "   " + qty + "x - " + toString();
   }

   @Override
   public Vector<ItemQtyWrapper> getNestedSources(Item product)
   {
      if(!product.equals(item))
      {
         throw new IllegalStateException("Cannot get " + product.getName() +  " from " + this);
      }
      Vector<ItemQtyWrapper> result = new Vector<ItemQtyWrapper>();

      result.add(new ItemQtyWrapper(this, 1));

      ItemQtyWrapper[] conversions = cost.getConversions();
      if(conversions != null)
      {
         for (ItemQtyWrapper conversion : conversions)
         {
            result.add(conversion);		
         }
      }

      return result;
   }

   @Override
   public boolean isRootStep()
   {
      return true;
   }

   public String getSourceType()
   {
      return "Market";
   }

   public void linkIngredientsToAPI()
   { // No impl, this doesn't make it from other items
   }
   
   public void convertItem(Item staticItem, APIItem dynamicItem)
   {
      // No Impl, Buy-On-Market sources only exist for API items
   }

   @Override
   public Value getSourceUseCost()
   {
      return cost;
   }

   @Override
   public Value getSalePriceLessCostBest()
   {
      return item.getSalePriceLessSaleCostBest();
   }

   @Override
   public Value getSalePriceLessCostFast()
   {
      return item.getSalePriceLessSaleCostFast();
   }

   @Override
   public Value getSalePriceBest()
   {
      return item.getSaleValueBest();
   }

   @Override
   public Value getSalePriceFast()
   {
      return item.getSaleValueFast();
   }

   @Override
   public void linkToTradingPost()
   {
      // Nothing to link
   }

   @Override
   public String getFullMethodName()
   {
      return "Market";
   }
}

