package com.torrenal.craftingGadget.apiInterface.items;

import java.net.MalformedURLException;
import java.net.URL;

import com.torrenal.craftingGadget.apiInterface.http.HttpRequest;
import com.torrenal.craftingGadget.apiInterface.http.RequestTypes;

public class ItemListRequest extends HttpRequest
{

	@Override
	public URL getURL()
	{
		try
      {
	      return new URL("https://api.guildwars2.com/v2/items");
      } catch (MalformedURLException e)
      {
	      return null;
      }
	}

	@Override
	public int getQueueLevel()
	{
		return RequestTypes.INDEXING_REQUEST.ordinal();
	}
}
