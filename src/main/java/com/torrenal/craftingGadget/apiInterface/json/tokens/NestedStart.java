package com.torrenal.craftingGadget.apiInterface.json.tokens;

public class NestedStart extends JSONToken
{

	@Override
	public TokenType getType()
	{
		return TokenType.NESTED_START;
	}

	@Override
   public Object getValue()
   {
	   return null;
   }
}
