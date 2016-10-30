package com.torrenal.craftingGadget.apiInterface.json.tokens;

import com.torrenal.craftingGadget.apiInterface.json.JSONPoint;

public class JSONNumber extends JSONToken implements JSONPoint
{
	double value;
	String rawText;
	
	public JSONNumber(String stringValue)
   {
	   rawText = stringValue;
	   value = Double.parseDouble(stringValue);
   }

	@Override
	public TokenType getType()
	{
		return TokenType.NUMBER;
	}
	
	public Double getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
	   return rawText;
	}
}
