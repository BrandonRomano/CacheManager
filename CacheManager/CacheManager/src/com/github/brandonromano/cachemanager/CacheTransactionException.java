package com.github.brandonromano.cachemanager;

public class CacheTransactionException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public CacheTransactionException(){}
	
	public CacheTransactionException(String alert)
	{
		super(alert);
	}
}