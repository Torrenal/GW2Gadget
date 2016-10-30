package com.torrenal.craftingGadget.apiInterface.json.tokens;

public class JSONValueMarker extends JSONToken
{

	@Override
	public TokenType getType()
	{
		return TokenType.VALUE_MARKER;
	}

	@Override
   public Object getValue()
   {
	   return null;
   }
}
