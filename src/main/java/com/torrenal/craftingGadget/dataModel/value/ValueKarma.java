package com.torrenal.craftingGadget.dataModel.value;

import com.torrenal.craftingGadget.CookingCore;

public class ValueKarma extends ValueElement<ValueKarma>
{
	double karma;

	public ValueKarma(double karma)
	{
		this.karma = karma;
	}

	@Override
	public ValueType getType()
	{
		return ValueType.KARMA_TOKEN;
	}

	@Override
   void setQuantity(double karma)
   {
	   this.karma = karma;
   }

	@Override
	public double getQuantity()
	{
		return karma;
	}

	public String toString()
	{
		return CookingCore.doubleToString(karma) + " karma";
	}

	@Override

	public ValueElement<ValueKarma> add(ValueElement<?> that)
	{
		if(!(that instanceof ValueKarma))
		{
			throw new Error("Invalid Argument, expected ValueKarma");
		}

		return new ValueKarma(karma + that.getQuantity());
	}

	@Override
   public ValueElement<ValueKarma> subtract(ValueElement<?> that)
   {
		if(!(that instanceof ValueKarma))
		{
			throw new Error("Invalid Argument, expected ValueKarma");
		}

		return new ValueKarma(karma - that.getQuantity());
   }

}
