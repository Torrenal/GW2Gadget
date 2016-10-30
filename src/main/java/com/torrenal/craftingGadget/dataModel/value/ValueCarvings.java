package com.torrenal.craftingGadget.dataModel.value;

public class ValueCarvings extends ValueAbstractToken
{

	public ValueCarvings(double tokens)
   {
	   super(tokens);
   }

	@Override
	public ValueType getType()
	{
		return ValueType.CF_TOKENS;
	}

}
