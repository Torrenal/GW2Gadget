package com.torrenal.craftingGadget.apiInterface.json;

import java.util.Iterator;
import java.util.Vector;

import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONNumber;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONString;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONToken;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONToken.TokenType;

public class JSONArray implements Iterable<JSONPoint>, JSONPoint
{

	private JSONTokenizer tokenizer;
	private Vector<JSONPoint> elements;

	public JSONArray(JSONTokenizer tokenizer)
   {
	   this.tokenizer = tokenizer;
	   elements = new Vector<>();
	   init();
   }

	/** Create an empty array */
	public JSONArray()
   {
	   elements = new Vector<>();
   }

	private void init()
   {
		while(true)
		{
			JSONToken token = tokenizer.getToken();
			switch(token.getType())
			{
				case ARRAY_END:
					return;
				case NESTED_START:
					elements.add(new JSONNode(tokenizer));
					token = tokenizer.peekNext();
					switch(token.getType())
					{
						case ARRAY_FIELD_MARKER:
							tokenizer.getToken();
							break;
						case ARRAY_END:
							break;
						default:
							throw new Error("Unexpected value parsing JSON Text @ " + tokenizer.getLastPos());
					}
					break;
				case NUMBER:
					elements.add((JSONNumber)token);
					token = tokenizer.peekNext();
					if(token.getType() == TokenType.ARRAY_FIELD_MARKER)
					{
						tokenizer.getToken();
					}
					break;
				case STRING:
					elements.add((JSONString)token);
					token = tokenizer.peekNext();
					if(token.getType() == TokenType.ARRAY_FIELD_MARKER)
					{
						tokenizer.getToken();
					}
					break;
				default:
					throw new Error("Error parsing JSON text @ " + tokenizer.getLastPos() + " expected ARRAY_END or NESTED_START, got " + token.getType());
			}
		}
   }

	public JSONPoint[] getValues()
   {
	   return elements.toArray(new JSONPoint[0]);
   }

	@Override
   public Iterator<JSONPoint> iterator()
   {
	   return elements.iterator();
   }
	
	public String toString()
	{
	   if(elements.isEmpty())
	   {
	      return "{}";
	   }
	   StringBuilder ret = new StringBuilder();
	   ret.append("[{");
	   for(JSONPoint element : elements)
	   {
	      ret.append(element).append(",");
	   }
	   ret.replace(ret.length()-1, ret.length()-1, "])");
	   return ret.toString();
	}

   protected void addElement(JSONPoint element)
   {
      elements.add(element);
   }
}
