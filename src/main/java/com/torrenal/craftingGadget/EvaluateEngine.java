package com.torrenal.craftingGadget;

import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EvaluateEngine
{
	static private ConcurrentLinkedQueue<Item> queue = new ConcurrentLinkedQueue<Item>();
	static private HashSet<Item> preQueue = new HashSet<>();
	static private Thread worker = null;
	private static boolean reevaluateAll;
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	
//TODO: Rework, queue/preQueue current design is sloppy.  Add methods should not block except with other add methods.
	
	static synchronized void initWorker()
	{
		if(worker == null)
		{
			worker = new Thread(new WorkerRunnable(), "EvaluateEngine");
			worker.setDaemon(true);
			worker.start();
		}
	}

	/** Very expensive - do not use for more than a hanfull of items at a time,
	 * and never on the event thread
	 */
	public static void evaluate(Vector<Item> products)
   {
		if(worker == null)
		{
			initWorker();
		}
		
		synchronized(preQueue)
		{
			@SuppressWarnings("unchecked")
         Vector<Item> productsCopy = (Vector<Item>) products.clone();
			preQueue.addAll(productsCopy);
		}
   }
	
	/* Expensive - the queue does not handle high volume, and it
	 * can contain 1000s of items
	 */
	public static void evaluate(Item product)
   {
		if(worker == null)
		{
			initWorker();
		}

		if(product == null)
		{
		   return;
		}
		synchronized(preQueue)
		{
			preQueue.add(product);
		}
   }



	static private class WorkerRunnable implements Runnable
   {
	   
		@Override
	   public void run()
	   {
			boolean napTime = false;
	   	while(true)
		   {
	   		
	   		try
	   		{
	   			Item victim = null;
	   			synchronized(queue)
	   			{
	   				if(queue.isEmpty())
	   				{
	   					ContextUpdateNotifier.notifyContentUpdates();
	   					queue.notifyAll();
	   					while(queue.isEmpty() && preQueue.isEmpty())
	   					{
	   						try
	   						{
	   							queue.wait(100);
	   						} catch (InterruptedException e)
	   						{
	   						}
	   					}
	   					napTime = true;
	   					if(!preQueue.isEmpty())
	   					{
	   						HashSet<Item> oldPreQueue = preQueue;
	   						preQueue = new HashSet<>();
	   						synchronized(oldPreQueue)
	   						{
	   							queue.addAll(oldPreQueue);
	   						}
	   					}
	   					continue;
	   				}
	   				victim = queue.remove();
	   			}
	   			if(napTime)
	   			{
	   				/* Insert a pause before we start work, as there are evidently
	   				 * some costs around updating things as a result of our calculating prices.
	   				 */
	   				Object napObject = new Object();
	   				synchronized(napObject)
	   				{
	   					try
	   					{
	   						napObject.wait(100);
	   						napTime = false;
	   					} catch (InterruptedException e)
	   					{
	   					}
	   				}
	   			}
	   			if(victim != null)
	   			{
	   				victim.evaluatePricing();
	   			}
	   		}
	   		catch(Throwable err)
	   		{
	   			err.printStackTrace();
	   		}
		   }
	   }
   }

	public static void blockUntilDone()
	{
		synchronized(queue)
		{
			while(!queue.isEmpty() || !preQueue.isEmpty())
			{
				try
            {
	            queue.wait();
            } catch (InterruptedException e)
            {
            }
			}
		}
   }

	public static void reevaluateAll()
   {
	   reevaluateAll = true;
	   notifyThis();
   }

	private static void notifyThis()
   {
	   Runnable doRun = new Runnable()
	   {
	   	public void run()
	   	{
	   		synchronized(preQueue)
	   		{
	   			if(reevaluateAll)
	   			{
	   				preQueue.addAll(CookingCore.getStaticItems().values());
	   				preQueue.addAll(CookingCore.getAPIItems().values());
	   				reevaluateAll = false;
	   			}
	   		}
	   	}
	   };
	   executor.execute(doRun);
   }

}
