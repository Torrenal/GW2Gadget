package com.torrenal.craftingGadget.apiInterface.json.tokens;

public class ArrayEnd extends JSONToken
{

	@Override
	public TokenType getType()
	{
		return TokenType.ARRAY_END;
	}

	@Override
   public Object getValue()
   {
	   return null;
   }

}
