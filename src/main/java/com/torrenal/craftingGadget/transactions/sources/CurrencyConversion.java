package com.torrenal.craftingGadget.transactions.sources;

import java.util.Vector;

import com.torrenal.craftingGadget.APIItem;
import com.torrenal.craftingGadget.CookingCore;
import com.torrenal.craftingGadget.Item;
import com.torrenal.craftingGadget.ItemQtyWrapper;
import com.torrenal.craftingGadget.ItemQuantitySet;
import com.torrenal.craftingGadget.dataModel.value.ExchangeRateManager;
import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.dataModel.value.ValueCoin;
import com.torrenal.craftingGadget.dataModel.value.ValueType;

public class CurrencyConversion extends Source
{

   public ValueType getToCurrency()
   {
      return toCurrency;
   }

   public ValueType getFromCurrency()
   {
      return fromCurrency;
   }

   private ValueType toCurrency;
   private ValueType fromCurrency;

   public CurrencyConversion(ValueType fromCurrency, ValueType toCurrency) 
   {
      if(fromCurrency == toCurrency)
      {
         throw new Error("Invalid Arguments, from/to currencies cannot be the same, got: " + fromCurrency + " and " + toCurrency);
      }
      this.fromCurrency = fromCurrency;
      this.toCurrency = toCurrency;
   }

   @Override
   public String toString()
   {
      return "Conversion from " + fromCurrency + " to " + toCurrency;
   }

   public Value getValueFor(Item product)
   {
      return new Value(false);
   }

   @Override
   public String getMethodName()
   {
      return "Conversion";
   }

   @Override
   public String getSourceName()
   {
      return "Currency Conversion";
   }

   @Override
   public boolean derivesFrom(Item item)
   {
      return false;
   }

   @Override
   public String getObtainString()
   {
      return getObtainString(null, 1);
   }

   @Override
   public String getObtainString(Item product, double qty)
   {
      double rate = ExchangeRateManager.getConversionRateFor(fromCurrency, toCurrency);
      String fromQty;
      switch(fromCurrency)
      {
         case COIN_TOKEN:
            fromQty = ValueCoin.getValueStringFor(qty);
            break;
         default:
            fromQty = CookingCore.doubleToString(qty);

      }
      String toQty;
      switch(toCurrency)
      {
         case COIN_TOKEN:
            toQty = ValueCoin.getValueStringFor(qty*rate);
            break;
         default:
            toQty = CookingCore.doubleToString(qty*rate);

      }
      return " Convert " + fromQty +" "+ fromCurrency + " to " + toQty + " " + toCurrency;
   }

   @Override
   public Vector<ItemQtyWrapper> getNestedSources(Item product)
   {
      return null;
   }

   @Override
   public String getIngredientDescriptionFor(double quantity)
   {
      return null;
   }

   @Override
   public boolean isRootStep()
   {
      return false;
   }

   @Override
   public String getSourceType()
   {
      return "Currency Conversion";
   }

   public void linkIngredientsToAPI()
   { // Not really working with products, but currencies 
   }

   public void convertItem(Item staticItem, APIItem dynamicItem)
   {
      // No Impl, not really working with products.
   }

   // For the moment, currencies have no proper outputs
   @Override
   public ItemQuantitySet[] getOutputs()
   {
      return null;
   }

   // N/A method for currencies
   @Override
   public double getOutputQty(Item output)
   {
      return 1;
   }

   @Override
   public Value getSourceUseCost()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Value getSalePriceLessCostBest()
   {
      return new Value(false);
   }

   @Override
   public Value getSalePriceLessCostFast()
   {
      return new Value(false);
   }

   @Override
   public Value getSalePriceBest()
   {
      return new Value(false);
   }

   @Override
   public Value getSalePriceFast()
   {
      return new Value(false);
   }

   @Override
   public void linkToTradingPost()
   {
   }

   @Override
   public String getFullMethodName()
   {
      return "Convert";
   }
}
