package com.torrenal.craftingGadget.apiInterface.json;

import java.util.Enumeration;
import java.util.Hashtable;

import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONString;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONToken;
import com.torrenal.craftingGadget.apiInterface.json.tokens.NestedStart;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONToken.TokenType;

public class JSONNode implements JSONPoint
{
	Hashtable<String,Object> children = new Hashtable<>();
	private JSONTokenizer tokenizer;
	public JSONNode(String data)
   {
		this(new JSONTokenizer(data));
		if(tokenizer.hasData())
		{
			throw new Error("Early end of data @ byte " + tokenizer.getLastPos());
		}
   }
	
	public JSONNode(JSONTokenizer tokenizer)
   {
		this.tokenizer = tokenizer;
		if(tokenizer.peekNext() instanceof NestedStart)
		{
			tokenizer.getToken();
		}
		
		while(tokenizer.hasData())
		{
         JSONToken token = tokenizer.getToken();
         switch(token.getType())
         {
         	case STRING:
         	{ /* Parsing a key/value pair */
         		String key = ((JSONString)token).getValue();
         		token = tokenizer.getToken();
         		if(token.getType() != JSONToken.TokenType.VALUE_MARKER)
         		{
         			throw new Error("Error parsing JSON reply @ " + tokenizer.getLastPos() + ", expected VALUE_MARKER, got " + token.getType());
         		}
         		token = tokenizer.getToken();
         		Object value = null;
         		switch(token.getType())
         		{
         			case ARRAY_START:
         				value = new JSONArray(tokenizer);
         				break;
         			case NESTED_START:
         				value = new JSONNode(tokenizer);
         				break;
         			case STRING:
         			case NUMBER:
         				value = token.getValue();
         				break;
         			default:
         				throw new Error("Error parsing JSON reply @ " + tokenizer.getLastPos() + ", unexpected token of " + token.getType());
         		}
         		children.put(key,value);
         		break;
         	}
         	case NESTED_END:
         		return;
         }
		}
   }

	public Enumeration<String> getKeys()
	{
		return children.keys();
	}	

	public Object getValue(String key)
	{
		return children.get(key);
	}

   public static JSONPoint parseJSONArrays(String data) // FIXME: Hack!  Redo the json parser
   {
      JSONTokenizer tokenizer = new JSONTokenizer(data);
      
      JSONArray result = new JSONArray();
      while(tokenizer.hasData())
      {
         JSONToken token = tokenizer.getToken();

         switch(token.getType())
         {
            case ARRAY_START:
               JSONArray array = new JSONArray(tokenizer);
               result.addElement(array);
               break;
         }
      }

      return result;
   }
}
