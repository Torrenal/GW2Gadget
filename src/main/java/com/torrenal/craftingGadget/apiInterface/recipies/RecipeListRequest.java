package com.torrenal.craftingGadget.apiInterface.recipies;

import java.net.MalformedURLException;
import java.net.URL;

import com.torrenal.craftingGadget.apiInterface.http.HttpRequest;
import com.torrenal.craftingGadget.apiInterface.http.RequestTypes;

public class RecipeListRequest extends HttpRequest
{

	@Override
	public URL getURL()
	{
		try
      {
	      return new URL("https://api.guildwars2.com/v1/recipes.json");
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
