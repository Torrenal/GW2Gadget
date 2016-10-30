package com.torrenal.craftingGadget.apiInterface.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import com.torrenal.craftingGadget.ResourceManager;
import com.torrenal.craftingGadget.apiInterface.json.JSONNode;

public abstract class HttpRequest implements Comparable<HttpRequest>
{
	private static final Charset charset = StandardCharsets.UTF_8;
	private final int MAX_RETRIES = 3;
	
	protected String reply = null;
	private boolean error = false;

	private long requestTimestamp = Long.MAX_VALUE;
	

	protected void processRequest() throws IOException
	{
		if(reply != null)
		{
			return;
		}
		URL url = getURL();
		if(url == null)
		{
			return;
		}
		//System.err.println("Performing Request " + getURL());
		
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

		connection.setDoOutput(true);
		connection.setDoInput(true);
		SSLContext sslContext = SSLContextProvider.getSSLContextForURL(url);
        connection.setSSLSocketFactory(sslContext.getSocketFactory());
		connection.setRequestMethod("GET");
		
		connection.setUseCaches(false);

		connection.setRequestProperty("User-Agent",
				ResourceManager.PROGRAM_NAME + " " + ResourceManager.VERSION);
		int retries = 3;
		BufferedReader connectionReader = null;
		IOException connError = null;
		while(retries-- > 0 && connectionReader == null)
		{
		   try
		   {

		      InputStreamReader isReader = new InputStreamReader(connection.getInputStream(), charset);
		      //System.err.println("Encoding = " + connection.getContentEncoding());
		      connectionReader = new BufferedReader(isReader);
		   }
		   catch (SSLHandshakeException err)
		   {
		      if(!SSLContextProvider.handShakeErrorReceived(url, sslContext))
		      {
		         processRequest();
		      }
		      return;
		   }
		   catch(IOException err)
		   {
		      connError = err;
		      // Pause 1 second before retrying (host might be throttling us).
		      synchronized(this)
		      {
			      try
			      {
			         wait(1000);
			      } catch (InterruptedException e)
			      { }

			   }
			}
		}
		if(connectionReader == null)
		{
			error = true;
			throw new Error("Non-recoverable Error processing request ", connError);
		}

		StringBuffer reply = new StringBuffer();
		{
			String data;
			while ((data = connectionReader.readLine()) != null)
			{
				reply.append('\n');
				reply.append(data);
			}
		}
		if(reply.length() == 0)
		{
			this.reply = "";
		} else
		{
			this.reply = reply.substring(1);
		}
	}
	
	/** This peroforms the request promptly on the current thread */
	public void performRequest()
	{
		int retryCounter = 0;

		try
		{
		   synchronized(this)
		   {
		      while(reply == null && ++retryCounter < MAX_RETRIES)
		      {
		         try
		         {
		            processRequest();
		         } catch (IOException e)
		         {
		            e.printStackTrace();
		         }
		      }
		   }
		} finally
		{
		   if(reply == null)
		   {
		      error = true;
		   }
		   processReply();
		   synchronized(this)
		   {
		      notifyAll();
		   }
		}
	}	

	/** This method will be invoked once the server reply is received */
	protected void processReply()
   {
		//Stub method.
		//Use for any updates that can occur after the reply is processed, without having a thread wait.
   }

	public void queueRequest()
   {
		requestTimestamp = System.currentTimeMillis();
		RequestHandler.queueRequest(this);
   }

	/** This goes through the request handler to perform the request via a queue, blocking until the request completes */
	public void fetchReply()
	{
		if(reply == null)
		{
			synchronized(this)
			{
				queueRequest();
				while(reply == null && !error)
				{
					try
               {
	               this.wait();
               } catch (InterruptedException e)
               { } // Normal Outcome.
				}
			}
		}
	}

	/* returns the timestamp of when the request was queued */
	public long getPriority()
	{
		return requestTimestamp;
	}

	public String getReplyString()
	{
		if(error)
		{
			return null;
		}

		if(reply == null)
		{
			fetchReply();
		}
		return reply;
	}
	
	public JSONNode getReplyJSON()
	{
		String reply = getReplyString();
		if(reply == null)
		{
			return null;
		}
		return new JSONNode(reply);
	}
	
	public boolean hasError()
	{
		return error;
	}

	@Override
   public int compareTo(HttpRequest o)
   {
	   if(o == null)
	   {
	   	return -1;
	   }
	   int queueDelta = getQueueLevel() - o.getQueueLevel();
	   if(queueDelta != 0)
	   {
	   	return queueDelta;
	   }
	   long priDelta = getPriority() - o.getPriority();
	   if(priDelta < 0)
	   {
	   	return -1;
	   }
	   if(priDelta > 1)
	   {
	   	return 1;
	   }
	   return 0;
   }

	public abstract URL getURL();

	/* Which queue the request is in */
	public abstract int getQueueLevel();
}
