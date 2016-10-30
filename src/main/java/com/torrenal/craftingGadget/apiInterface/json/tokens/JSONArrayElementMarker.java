package com.torrenal.craftingGadget.apiInterface.json.tokens;


public class JSONArrayElementMarker extends JSONToken
{

	@Override
	public TokenType getType()
	{
		return TokenType.ARRAY_FIELD_MARKER;
	}

	@Override
	public Object getValue()
	{
		return null;
	}

}
