package com.torrenal.craftingGadget.dataModel.value;

public class ValueBlooms extends ValueAbstractToken
{

	public ValueBlooms(double tokens)
   {
	   super(tokens);
   }

	@Override
	public ValueType getType()
	{
		return ValueType.TA_TOKENS;
	}

}
