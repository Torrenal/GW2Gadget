package com.torrenal.craftingGadget;

import java.io.IOException;

import com.torrenal.craftingGadget.ui.UIManager;

public class CookingLauncher
{

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args)
	{
		boolean captureData = false;

		for(String arg :args)
		{
			if("-capturePriorityList".equals(arg))
			{
				captureData = true;
			}
		}
		if(captureData)
		{
			CookingCore.setCaptureData();
			CookingCore.loadMarketData();
			CookingCore.printPriorityLoadList();
			System.exit(0);
		} else
		{
			UIManager.launchUI();
			CookingCore.loadMarketData();
		}
	}
}
