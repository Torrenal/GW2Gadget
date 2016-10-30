package com.torrenal.craftingGadget.transactions.destinations;

import com.torrenal.craftingGadget.dataModel.value.Value;

public class SellOnMarket extends Destination
{
   private Value valueGrossHigh;
   private Value valueGrossLow;
   private Value valueNetHigh;
   private Value valueNetLow;
   private Value saleCost = null;

   public SellOnMarket(Value highValue, Value lowValue)
   {
      this.valueGrossHigh = highValue;
      this.valueGrossLow = lowValue;
      this.valueNetHigh = valueGrossHigh.lessMarketCut();
      this.valueNetLow = valueGrossLow.lessMarketCut();
   }

   @Override
   public Value getGrossValue()
   {
      return valueGrossHigh;
   }

   @Override
   public Value getNetValue()
   {
      return valueNetHigh;
   }

   @Override
   public String getDestionationName()
   {
      return "Sell";
   }

   @Override
   public String getDestionationFullName()
   {
      return "Market";
   }

   @Override
   public String getFullDestinationDetails()
   {
      if(saleCost == null)
      {
         saleCost = getNetValue().subtract(getGrossValue());
      }
      StringBuilder ret = new StringBuilder();
      ret.append("Sell to Market for " + getGrossValue()).append('\n');
      ret.append("Market Cut = " + saleCost).append('\n');
      ret.append("Net Sale = " + getNetValue()).append('\n');
      ret.append("Liquidation Gross value = " + getFastSaleGrossValue()).append('\n');
      ret.append("Liquidation Net value   = " + getFastSaleNetValue()).append('\n');
      return ret.toString();
   }

   public Value getFastSaleGrossValue()
   {
      return valueGrossLow;
   }

   public Value getFastSaleNetValue()
   {
      return valueNetLow;
   }
   
}
