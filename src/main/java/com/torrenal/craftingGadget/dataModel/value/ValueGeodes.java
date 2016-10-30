package com.torrenal.craftingGadget.dataModel.value;

public class ValueGeodes extends ValueAbstractToken
{
	public ValueGeodes(double tokens)
   {
	   super(tokens);
   }

	@Override
	public ValueType getType()
	{
		return ValueType.GEODE_TOKENS;
	}

}
