package com.torrenal.craftingGadget.dataModel.value;

public class ValueSeals extends ValueAbstractToken
{

	public ValueSeals(double tokens)
   {
	   super(tokens);
   }

	@Override
	public ValueType getType()
	{
		return ValueType.CM_TOKENS;
	}

}
