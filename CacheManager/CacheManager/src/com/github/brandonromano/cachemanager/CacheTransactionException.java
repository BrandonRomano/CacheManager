package com.github.brandonromano.cachemanager;

public class CacheTransactionException extends Exception
{
	private static final long serialVersionUID = 1L;
	public static String writeExceptionAlert = "CacheManager failed to write to cache";
	public static String readExceptionAlert = "CacheManager failed to read from cache";
	
	public CacheTransactionException(){}
	
	public CacheTransactionException(String alert)
	{
		super(alert);
	}
}