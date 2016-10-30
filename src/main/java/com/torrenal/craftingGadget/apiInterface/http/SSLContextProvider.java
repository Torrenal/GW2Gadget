package com.torrenal.craftingGadget.apiInterface.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashSet;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SSLContextProvider
{
	static private SSLContext alternateContext = null;
	static private HashSet<String> badHosts = new HashSet<>(2);
	static private HashSet<String> alternateSSLHosts = new HashSet<>(2);
	
	
	static public SSLContext getPrimarySSLContext()
	{
		try
      {
	      return SSLContext.getDefault();
      } catch (NoSuchAlgorithmException e)
      {
	      e.printStackTrace();
      }
		return null;
	}
	
	static public SSLContext getAlternateSSLContext()
	{
		if(alternateContext == null)
		{
			alternateContext = createAlternateSSLContext();
		}
		return alternateContext;
	}
	
	static private SSLContext createAlternateSSLContext()
	{
      try
      {
		KeyManagerFactory keyManagerFactory;
	      keyManagerFactory = KeyManagerFactory
	      		.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		File keyStoreFile = new File(".keystore");
		InputStream keyInput = new FileInputStream(keyStoreFile);
		keyStore.load(keyInput, "abc124".toCharArray());
		keyInput.close();
		keyManagerFactory.init(keyStore, "abc124".toCharArray());

		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream trustInput = new FileInputStream(keyStoreFile);
		trustStore.load(trustInput, "abc124".toCharArray());
		trustInput.close();
		trustManagerFactory.init(trustStore);

		SSLContext sct = SSLContext.getInstance("SSL");
		sct.init(keyManagerFactory.getKeyManagers(),
				trustManagerFactory.getTrustManagers(), new SecureRandom());
		return sct;
      } catch (NoSuchAlgorithmException e)
      {
      	e.printStackTrace();
      } catch (KeyStoreException e)
      {
	      e.printStackTrace();
      } catch (KeyManagementException e)
      {
	      e.printStackTrace();
      } catch (IOException e)
      {
	      e.printStackTrace();
      } catch (CertificateException e)
      {
	      e.printStackTrace();
      } catch (UnrecoverableKeyException e)
      {
	      e.printStackTrace();
      }
      return null;
	}

	static public synchronized SSLContext getSSLContextForURL(URL url)
	{
		String host = url.getHost();
		if(badHosts.contains(host))
		{
			return null;
		}
		if(alternateSSLHosts.contains(host))
		{
			return getAlternateSSLContext();
		}
		return getPrimarySSLContext();
	}

	/* Note: Requests may be processed in parallel and fail in parallel.  We must allow for this */
	public static synchronized boolean handShakeErrorReceived(URL url, SSLContext sslContext)
	{
		String host = url.getHost();
		if(alternateContext != sslContext)
		{
			alternateSSLHosts.add(host);
			return false;
		} else
		{
			badHosts.add(host);
			return true;
		}
	}
}
