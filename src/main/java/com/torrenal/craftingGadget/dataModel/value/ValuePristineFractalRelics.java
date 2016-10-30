package com.torrenal.craftingGadget.dataModel.value;

public class ValuePristineFractalRelics extends ValueAbstractToken
{
	public ValuePristineFractalRelics(double tokens)
   {
	   super(tokens);
   }

	@Override
	public ValueType getType()
	{
		return ValueType.PRISTINE_FRACTAL_TOKENS;
	}

}
