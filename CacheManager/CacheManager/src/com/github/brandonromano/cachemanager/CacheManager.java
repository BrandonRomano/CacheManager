package com.github.brandonromano.cachemanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;

public class CacheManager {
	
	private static CacheManager mInstance;
	private static Context mContext;
	private static String mCacheDir;

	private CacheManager(Context applicationContext){
		mContext = applicationContext;
		mCacheDir = mContext.getCacheDir().toString();
	}
	
	public static CacheManager getInstance(Context applicationContext)
	{
		if(mInstance == null){
			mInstance = new CacheManager(applicationContext);
		}
		return mInstance;
	}
	
	/**
	 * Writes a string to the given file name.  The file will be placed
	 * in the current application's cache directory.
	 * 
	 * @param toWrite The String to write to a file.
	 * @param fileName The File name that will be written to.  
	 * @throws CacheTransactionException Throws the exception if writing failed.  Will 
	 * not throw an exception in the result of a successful write.
	 */
	public void write(String toWrite, String fileName) throws CacheTransactionException
	{
		File file = new File(mCacheDir, fileName);
		
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(file), 1024);
			out.write(toWrite);
		} catch (IOException e) {
			e.printStackTrace();
			throw new CacheTransactionException(CacheTransactionException.writeExceptionAlert);
		}finally{
			if(out != null)
			{
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	/**
	 * Reads a string from an existing file in the cache directory 
	 * and returns it.  
	 * 
	 * @param fileName The file name of an existing file in the 
	 * cache directory to be read.
	 * @return Returns whatever is read.  Null if read fails.
	 * @throws CacheTransactionException Throws the exception if reading failed.  
	 * Will not throw an exception in the result of a successful read.
	 */
	public String read(String fileName) throws CacheTransactionException
	{
		String readString = "";
		File file = new File(mCacheDir, fileName);
		
		BufferedReader in = null;
		try{
			in = new BufferedReader(new FileReader(file));
			
			String currentLine;
			while ((currentLine = in.readLine()) != null) {
				readString += currentLine;
			}
			return readString;
		}catch(IOException e){
			e.printStackTrace();
			throw new CacheTransactionException(CacheTransactionException.readExceptionAlert);
		}finally{
			if(in != null)
			{
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
