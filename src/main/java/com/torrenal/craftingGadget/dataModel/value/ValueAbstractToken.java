package com.torrenal.craftingGadget.dataModel.value;

import com.torrenal.craftingGadget.CookingCore;

public abstract class ValueAbstractToken extends ValueElement<ValueAbstractToken>
{
	double quantity;

	static
	{
		valueModifier.put(ValueType.AC_TOKENS, 140F);
		valueModifier.put(ValueType.CF_TOKENS, 140F);
		valueModifier.put(ValueType.CM_TOKENS, 140F);
		valueModifier.put(ValueType.CoE_TOKENS, 140F);
		valueModifier.put(ValueType.FRACTAL_TOKENS, 140F);
		valueModifier.put(ValueType.HotW_TOKENS, 140F);
		valueModifier.put(ValueType.SE_TOKENS, 140F);
		valueModifier.put(ValueType.TA_TOKENS, 140F);
		valueModifier.put(ValueType.WvW_TOKENS, 140F);
		
	}
	public ValueAbstractToken(double tokens)
	{
		this.quantity = tokens;
	}

	@Override
   void setQuantity(double tokens)
   {
	   this.quantity = tokens;
   }

	@Override
	public double getQuantity()
	{
		return quantity;
	}

	public String toString()
	{
		return CookingCore.doubleToString(quantity) + " " + getType();
	}

	@Override
	public ValueElement<ValueAbstractToken> add(ValueElement<?> that)
	{
		if(!(that instanceof ValueAbstractToken && that.getType() == this.getType()))
		{
			throw new Error("Invalid Argument, expected ValueAbstractToken for " + this.getType());
		}
		
		ValueElement<ValueAbstractToken> clone = this.clone();
		clone.setQuantity(quantity + that.getQuantity());
		return clone;
	}

	@Override
   public ValueElement<ValueAbstractToken> subtract(ValueElement<?> that)
   {
		if(!(that instanceof ValueAbstractToken && that.getType() == this.getType()))
		{
			throw new Error("Invalid Argument, expected ValueAbstractToken for " + this.getType());
		}
		
		ValueElement<ValueAbstractToken> clone = this.clone();
		clone.setQuantity(quantity - that.getQuantity());
		return clone;
   }

}
