package com.torrenal.craftingGadget.dataModel.value;

public class ValueKnowledgeCrystals extends ValueAbstractToken
{

	public ValueKnowledgeCrystals(double tokens)
   {
	   super(tokens);
   }

	@Override
	public ValueType getType()
	{
		return ValueType.CoE_TOKENS;
	}

}
