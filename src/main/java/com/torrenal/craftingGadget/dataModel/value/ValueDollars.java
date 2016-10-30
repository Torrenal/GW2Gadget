package com.torrenal.craftingGadget.dataModel.value;

import com.torrenal.craftingGadget.CookingCore;

public class ValueDollars extends ValueElement<ValueDollars>
{
	double dollars;

	public ValueDollars(double dollars)
	{
		this.dollars = dollars;
	}

	@Override
	public ValueType getType()
	{
		return ValueType.DOLLAR_TOKEN;
	}

	@Override
   void setQuantity(double dollars)
   {
	   this.dollars = dollars;
   }

	@Override
	public double getQuantity()
	{
		return dollars;
	}

	public String toString()
	{
		return CookingCore.dollarsToString(dollars) + (dollars == 1 ? " dollar" : " dollars");
	}

	@Override

	public ValueElement<ValueDollars> add(ValueElement<?> that)
	{
		if(!(that instanceof ValueDollars))
		{
			throw new Error("Invalid Argument, expected ValueDollars");
		}
		
		return new ValueDollars(dollars + that.getQuantity());
	}

	@Override
   public ValueElement<ValueDollars> subtract(ValueElement<?> that)
   {
		if(!(that instanceof ValueDollars))
		{
			throw new Error("Invalid Argument, expected ValueDollars");
		}

		return new ValueDollars(dollars - that.getQuantity());
   }

}
