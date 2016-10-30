package com.torrenal.craftingGadget.dataModel.value;

import com.torrenal.craftingGadget.CookingCore;

public class ValueLaurel extends ValueElement<ValueLaurel>
{
	double gems;

	
	static
	{
		valueModifier.put(ValueType.AC_TOKENS, 140F/15F);
	}
	
	public ValueLaurel(double gems)
	{
		this.gems = gems;
	}

	@Override
	public ValueType getType()
	{
		return ValueType.LAURELS_TOKEN;
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
		return CookingCore.doubleToString(gems) + (gems == 1 ? " Laurel" : " Laurels");
	}

	@Override

	public ValueElement<ValueLaurel> add(ValueElement<?> that)
	{
		if(!(that instanceof ValueLaurel))
		{
			throw new Error("Invalid Argument, expected ValueLaurel");
		}
		
		return new ValueLaurel(gems + that.getQuantity());
	}

	@Override
   public ValueElement<ValueLaurel> subtract(ValueElement<?> that)
   {
		if(!(that instanceof ValueLaurel))
		{
			throw new Error("Invalid Argument, expected ValueLaurel");
		}

		return new ValueLaurel(gems - that.getQuantity());
   }

}
