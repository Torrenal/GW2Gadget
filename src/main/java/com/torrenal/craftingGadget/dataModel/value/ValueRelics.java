package com.torrenal.craftingGadget.dataModel.value;

public class ValueRelics extends ValueAbstractToken
{
	public ValueRelics(double tokens)
   {
	   super(tokens);
   }

	@Override
	public ValueType getType()
	{
		return ValueType.FRACTAL_TOKENS;
	}

}
