package com.torrenal.craftingGadget.dataModel.value;

import com.torrenal.craftingGadget.CookingCore;

public class ValueGem extends ValueElement<ValueGem>
{
	double gems;

	public ValueGem(double gems)
	{
		this.gems = gems;
	}

	@Override
	public ValueType getType()
	{
		return ValueType.GEM_TOKEN;
	}

	@Override
   void setQuantity(double gems)
   {
	   this.gems = gems;
   }

	@Override
	public double getQuantity()
	{
		return gems;
	}

	public String toString()
	{
		return CookingCore.doubleToString(gems) + (gems == 1 ? " gem" : " gems");
	}

	@Override

	public ValueElement<ValueGem> add(ValueElement<?> that)
	{
		if(!(that instanceof ValueGem))
		{
			throw new Error("Invalid Argument, expected ValueGem");
		}
		
		return new ValueGem(gems + that.getQuantity());
	}

	@Override
   public ValueElement<ValueGem> subtract(ValueElement<?> that)
   {
		if(!(that instanceof ValueGem))
		{
			throw new Error("Invalid Argument, expected ValueGem");
		}

		return new ValueGem(gems - that.getQuantity());
   }

}
