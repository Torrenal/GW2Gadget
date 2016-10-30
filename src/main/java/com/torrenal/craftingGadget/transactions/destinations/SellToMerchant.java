package com.torrenal.craftingGadget.transactions.destinations;

import com.torrenal.craftingGadget.dataModel.value.Value;

public class SellToMerchant extends Destination
{
	private Value value;
	
	public SellToMerchant(Value value)
   {
	   this.value = value;
   }

   @Override
   public Value getGrossValue()
   {
      return value;
   }

   @Override
   public Value getNetValue()
   {
      return value;
   }

   @Override
   public String getDestionationName()
   {
      return "Merchant";
   }

   @Override
   public String getDestionationFullName()
   {
      return "Sell to Merchant for " + getGrossValue();
   }

   @Override
   public String getFullDestinationDetails()
   {
      return getDestionationFullName();
   }

   public void setValue(Value merchValue)
   {
      value = merchValue;
   }

}
