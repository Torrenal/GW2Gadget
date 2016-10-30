package com.torrenal.craftingGadget.apiInterface.json.tokens;

public class ArrayStart extends JSONToken
{

	@Override
	public TokenType getType()
	{
		return TokenType.ARRAY_START;
	}

	@Override
   public Object getValue()
   {
	   return null;
   }

}
