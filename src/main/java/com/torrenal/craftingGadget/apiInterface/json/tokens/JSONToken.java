package com.torrenal.craftingGadget.apiInterface.json.tokens;

public abstract class JSONToken
{
	public enum TokenType
	{
		ARRAY_START,
		ARRAY_END,
		NESTED_START,
		NESTED_END,
		STRING,
		NUMBER, 
		VALUE_MARKER,
		ARRAY_FIELD_MARKER;
	}

	public abstract TokenType getType();
	public abstract Object getValue();
}
