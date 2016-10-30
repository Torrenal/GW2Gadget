package com.torrenal.craftingGadget.dataModel.value;

public class ValueKoda extends ValueAbstractToken
{

	public ValueKoda(double tokens)
   {
	   super(tokens);
   }

	@Override
	public ValueType getType()
	{
		return ValueType.HotW_TOKENS;
	}

}
