package com.torrenal.craftingGadget;

import java.lang.ref.WeakReference;
import java.util.Vector;

public class ContextUpdateNotifier
{
	private static Vector<WeakReference<EvaluateListener>> contentUpdateListeners = new Vector<WeakReference<EvaluateListener>>();
	private static boolean contentUpdateEventQueued = false;
	private static boolean structureUpdateEventQueued = false;
	private static boolean lazyStructureUpdateEventQueued = false;
	private static long lastStructureUpdate = 0; 
	private static Thread notifierRunner = null;

	
	static
	{
		notifierRunner = new Thread(new NotifierRunner(),"notifierRunner" );
		notifierRunner.setDaemon(true);
		notifierRunner.start();
	}
	
	public synchronized static void notifyContentUpdates()
	{
		if(contentUpdateEventQueued)
		{
			return;
		}
		contentUpdateEventQueued = true;
		ContextUpdateNotifier.class.notifyAll();
   }
	
	public synchronized static void notifyStructureUpdates()
	{
		if(structureUpdateEventQueued == true)
		{
			return;
		}
		structureUpdateEventQueued = true;
		notifyContentUpdates();
	}

	public static void addContentUpdateListener(EvaluateListener evaluateListener)
   {
	   contentUpdateListeners.add(new WeakReference<EvaluateListener>(evaluateListener));
   }
	public static void removeContentUpdateListener(EvaluateListener evaluateListener)
   {
	   contentUpdateListeners.remove(new WeakReference<EvaluateListener>(evaluateListener));
   }

   public synchronized static void notifyLazyStructureUpdates()
   {
	   if(structureUpdateEventQueued)
	   {
	   	return;
	   }
	   if(lazyStructureUpdateEventQueued)
	   {
	   	return;
	   }
   }
//	   lazyStructureUpdateEventQueued = true;
//	   
//		Runnable dorun = new Runnable() {
//			@Override
//			public void run()
//			{
//				synchronized(ContextUpdateNotifier.class)
//				{
//					try
//               {
//						while(true)
//						{
//							boolean finishedWaiting = lastStructureUpdate + 10000 < System.currentTimeMillis(); 
//							if(finishedWaiting)
//							{
//								break;
//							}
//							if(!lazyStructureUpdateEventQueued)
//							{
//								break;
//							}
//							ContextUpdateNotifier.class.wait(1000);
//						}
//               } catch (InterruptedException e)
//               { } // Not really an error
//					
//					if(lazyStructureUpdateEventQueued)
//					{
//						lastStructureUpdate = System.currentTimeMillis();
//						lazyStructureUpdateEventQueued = false;
//					} else
//					{
//						return;
//					}
//				}
//				notifyStructureUpdates();
//			}
//		};
//      new Thread(dorun, "UI Lazy Update Idler").start();
//   }
   
	public static class NotifierRunner implements Runnable
   {
	   @Override
	   public void run()
	   {
	   	while(true)
	   	{
	   		try
	   		{
	   			synchronized(ContextUpdateNotifier.class)
	   			{
	   				while(!contentUpdateEventQueued)
	   				{
		   				try
		   				{
		   					ContextUpdateNotifier.class.wait(1000);
		   				}
		   				catch (InterruptedException e)
		   				{ } // Normal outcome
		   				if(lazyStructureUpdateEventQueued && System.currentTimeMillis() - lastStructureUpdate > 15000)
		   				{
		   					contentUpdateEventQueued = true;
		   					structureUpdateEventQueued = true;
		   				}
	   				}

	   				try
	   				{
	   					ContextUpdateNotifier.class.wait(1000);
	   				} catch (InterruptedException e)
	   				{ } // Not really an error
	   			}
	   			boolean structureUpdate = structureUpdateEventQueued; 
	   			synchronized(ContextUpdateNotifier.class)
	   			{
	   				contentUpdateEventQueued = false;
	   				if(structureUpdateEventQueued)
	   				{
	   					structureUpdateEventQueued = false;
	   					lazyStructureUpdateEventQueued = false;
	   				}

	   			}
	   			@SuppressWarnings("unchecked")
	   			Vector<WeakReference<EvaluateListener>> listenerWorkset = (Vector<WeakReference<EvaluateListener>>) contentUpdateListeners.clone();
	   			for(WeakReference<EvaluateListener> reference : listenerWorkset)
	   			{
	   				EvaluateListener listener = reference.get();
	   				if(listener == null)
	   				{
	   					contentUpdateListeners.remove(reference);
	   				} else
	   				{
	   					if(structureUpdate)
	   					{
	   						listener.structureUpdateEvent();
	   						lastStructureUpdate = System.currentTimeMillis();
	   					}
	   					listener.contentUpdateEvent();
	   				}
	   			}
	   		}
	   		catch(Throwable err)
	   		{
	   			err.printStackTrace();
	   		}
	   	}
	   }
   }

}
