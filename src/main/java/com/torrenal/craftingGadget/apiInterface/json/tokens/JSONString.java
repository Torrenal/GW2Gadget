package com.torrenal.craftingGadget.apiInterface.json.tokens;

import com.torrenal.craftingGadget.apiInterface.json.JSONPoint;

public class JSONString extends JSONToken implements JSONPoint
{

	private String data;

	public JSONString(String data)
   {
	   this.data = data; 
   }

	@Override
	public TokenType getType()
	{
		return TokenType.STRING;
	}
	
	public String getValue()
	{
		return data;
	}
	
	@Override
	public String toString()
	{
	   return "\""+data+"\"";
	}

}
