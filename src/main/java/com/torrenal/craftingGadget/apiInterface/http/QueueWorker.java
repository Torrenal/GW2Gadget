package com.torrenal.craftingGadget.apiInterface.http;

import java.util.concurrent.PriorityBlockingQueue;

import com.torrenal.craftingGadget.db.items.ItemDB;
import com.torrenal.craftingGadget.db.recipes.RecipeDB;

public class QueueWorker implements Runnable
{
	private PriorityBlockingQueue<HttpRequest> requestQueue;
	static private QueueWorker instance = null; 

	public QueueWorker(PriorityBlockingQueue<HttpRequest> requestQueue)
   {
	   this.requestQueue = requestQueue;
	   instance = this;
   }

	@Override
   public void run()
   {
		while(true)
		{
			try
			{
				processOneFromQueue();
			}
			catch(Throwable err)
			{
				err.printStackTrace();
			}
		}
   }

	private void processOneFromQueue()
   {
	   ItemDB.blockForInit();
		if(requestQueue.isEmpty())
		{
			ItemDB.matchStaticItems();
			ItemDB.retryMissingItems();
			RecipeDB.retryMissingRecipes();
		}
		synchronized(this)
		{
			while(requestQueue.isEmpty())
			{
				try
				{
					wait();
				} catch (InterruptedException e)
				{ }  //Normal outcome
			}
		}
	   requestQueue.remove().performRequest();
   }

	public synchronized void wakeUp()
   {
	   notifyAll();
   }
	
	static public int getQueueDepth()
	{
		if(instance == null)
		{
			return 0;
		}

		return instance.requestQueue.size();
	}
}
