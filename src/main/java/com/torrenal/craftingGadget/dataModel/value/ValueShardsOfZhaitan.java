package com.torrenal.craftingGadget.dataModel.value;

public class ValueShardsOfZhaitan extends ValueAbstractToken
{

	public ValueShardsOfZhaitan(double tokens)
   {
	   super(tokens);
   }

	@Override
	public ValueType getType()
	{
		return ValueType.Arah_TOKENS;
	}

}
