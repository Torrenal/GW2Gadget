package com.torrenal.craftingGadget.dataModel.value;

public class ValueTears extends ValueAbstractToken
{

	public ValueTears(double tokens)
   {
	   super(tokens);
   }

	@Override
	public ValueType getType()
	{
		return ValueType.AC_TOKENS;
	}

}
