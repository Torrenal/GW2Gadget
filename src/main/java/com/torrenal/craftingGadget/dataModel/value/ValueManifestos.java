package com.torrenal.craftingGadget.dataModel.value;

public class ValueManifestos extends ValueAbstractToken
{

	public ValueManifestos(double tokens)
   {
	   super(tokens);
   }

	@Override
	public ValueType getType()
	{
		return ValueType.SE_TOKENS;
	}

}
