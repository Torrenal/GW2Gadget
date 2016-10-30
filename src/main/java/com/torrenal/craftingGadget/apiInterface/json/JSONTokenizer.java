package com.torrenal.craftingGadget.apiInterface.json;

import com.torrenal.craftingGadget.apiInterface.json.tokens.ArrayEnd;
import com.torrenal.craftingGadget.apiInterface.json.tokens.ArrayStart;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONArrayElementMarker;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONNumber;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONString;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONToken;
import com.torrenal.craftingGadget.apiInterface.json.tokens.JSONValueMarker;
import com.torrenal.craftingGadget.apiInterface.json.tokens.NestedEnd;
import com.torrenal.craftingGadget.apiInterface.json.tokens.NestedStart;

public class JSONTokenizer
{

	private String data;
	private int length;
	private int pos;
	private int lastPos;
	private JSONToken next;

	public JSONTokenizer(String data)
   {
		this.data = data;
		length = data.length();
		pos = 0;
		next = null;
   }

	public boolean hasData()
   {
		skipWhiteSpace();
		return next != null || pos < length;
   }

	public JSONToken getToken()
   {
		if(next != null)
		{
			JSONToken result = next;
			next = null;
			return result;
		}		
		skipWhiteSpace();
		lastPos = pos;
		if(pos > length)
		{
			throw new Error("EOF Parsing JSON text");
		}
		char tokenByte = data.charAt(pos);
		
		switch(tokenByte)
		{
			case '[':
				pos++;
				return new ArrayStart();
			case ']':
				pos++;
				return new ArrayEnd();
			case '{':
				pos++;
				return new NestedStart();
			case '}':
				pos++;
				return new NestedEnd();
			case '"':
			{
				int stringEnd = data.indexOf('"', pos+1);
				while(stringEnd > 0)
				{
					char lastChar = data.charAt(stringEnd - 1);
					if(lastChar != '\\')
					{
						break;
					}
					stringEnd = data.indexOf('"', stringEnd+1);
				}
				if(stringEnd < pos)
				{
					throw new Error("EOF parsing JSON String Element at byte " + pos);
				}
				int stringStart = pos+1;
				pos = stringEnd+1;
				return new JSONString(data.substring(stringStart, stringEnd));
			}
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case '.':
			{
				int endByte = pos;
				char inspectionByte = data.charAt(endByte);
				while(inspectionByte == '.' || Character.isDigit(inspectionByte))
				{
					endByte++;
					if(endByte > length)
					{
						throw new Error("EOF parsing JSON Number Element at byte " + pos);
					}
					inspectionByte = data.charAt(endByte);
				}
				int startByte = pos;
				pos = endByte;
				return new JSONNumber(data.substring(startByte, endByte));
			}
			case ':':
				pos++;
				return new JSONValueMarker();
			case ',':
				pos++;
				return new JSONArrayElementMarker();
			case 'n':
				if("null".equals(data.substring(pos, pos+4)))
				pos += 4;
				return new JSONString("null");
			case 'f':
				if("false".equals(data.substring(pos, pos+5)))
				pos += 5;
				return new JSONString("false");
			case 't':
				if("true".equals(data.substring(pos, pos+4)))
				pos += 5;
				return new JSONString("true");
			default:
				int db_spos = pos - 10;
				int db_epos = pos + 10;
				if(db_spos < 0) db_spos = 0;
				if(db_spos > data.length())  db_spos = data.length();
				System.err.println("Unsupported text context: " + data.substring(db_spos, db_epos));
				System.err.println("                                    ^");
				throw new Error("Unrecognized byte at position " + pos);
		}
   }

	private void skipWhiteSpace()
   {
		if(pos >= length)
			return;
	   char character = data.charAt(pos);
	   while(Character.isWhitespace(character) && pos < length)
	   {
	   	pos++;
	   	character = data.charAt(pos);
	   }
   }

	public JSONToken peekNext()
   {
		if(next != null)
		{
			return next;
		}
		next = getToken();
	   return next;
   }

	public int getLastPos()
   {
	   return lastPos;
   }

}
