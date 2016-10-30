package com.torrenal.craftingGadget.dataModel.value;

public class ValueHonorBadges extends ValueAbstractToken
{

	public ValueHonorBadges(double tokens)
   {
	   super(tokens);
   }

	@Override
	public ValueType getType()
	{
		return ValueType.WvW_TOKENS;
	}

}
