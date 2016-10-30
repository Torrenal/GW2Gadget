package com.torrenal.craftingGadget.apiInterface.http;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.concurrent.PriorityBlockingQueue;

import javax.net.ssl.SSLContext;

public class RequestHandler
{
	private static PriorityBlockingQueue<HttpRequest> requestQueue = new PriorityBlockingQueue<>();
	private static QueueWorker queueWorker = null;
	private static SSLContext sslContext = null;

	static
	{
		configureSSL();
	}

	public static void queueRequest(HttpRequest httpRequest)
	{
		requestQueue.put(httpRequest);
		pokeWorker();
	}

	private static void configureSSL()
	{
			try {
				sslContext = SSLContext.getDefault();
			} catch (GeneralSecurityException e) {
				new RemoteException(e.getMessage(), e).printStackTrace();
			}
	
	}

	private static void pokeWorker()
	{
		getQueueWorker();
		queueWorker.wakeUp();
	}

	private synchronized static void getQueueWorker()
	{
		if(queueWorker == null)
		{
			queueWorker = new QueueWorker(requestQueue);
			Thread workerThread = new Thread(queueWorker,"HTTP-Request-Worker");
			workerThread.setDaemon(true);
			workerThread.start();
		}
	}
	
	public static SSLContext getSSLContext()
	{
		return sslContext;
	}

}
