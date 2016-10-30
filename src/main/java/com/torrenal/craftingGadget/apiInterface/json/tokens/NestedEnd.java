package com.torrenal.craftingGadget.apiInterface.json.tokens;

public class NestedEnd extends JSONToken
{

	@Override
	public TokenType getType()
	{
		return TokenType.NESTED_END;
	}
	@Override
   public Object getValue()
   {
	   return null;
   }

}
