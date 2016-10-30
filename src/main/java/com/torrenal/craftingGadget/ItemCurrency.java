package com.torrenal.craftingGadget;

import com.torrenal.craftingGadget.dataModel.value.Value;
import com.torrenal.craftingGadget.dataModel.value.ValueType;
import com.torrenal.craftingGadget.transactions.sources.BuyFromMerchant;

public class ItemCurrency extends Item
{

   ValueType type;
   Value value;
   
   public ItemCurrency(ValueType type)
   {
      super(type.toString());
      this.type = type;
      value = type.getValue();
      setTradeable(false);
      addSource(new BuyFromMerchant(this, "n/a", value));
   }

   @Override
   public Value getBestObtainCost()
   {
      return value;
   }
   
   @Override
   public Value getSalePriceLessSaleCostBest()
   {
      return value;
   }
   
   @Override
   public Value getSalePriceLessSaleCostFast()
   {
      return value;
   }
   
   @Override
   public Value getSaleValueBest()
   {
      return value;
   }
   
   @Override
   public Value getSaleValueFast()
   {
      return value;
   }
   
   @Override
   public Value getProfitBest()
   {
      return value;
   }
   
   @Override
   public Value getProfitFast()
   {
      return value;
   }
   
   @Override
   public Value getMerchValue()
   {
      return value;
   }
}
