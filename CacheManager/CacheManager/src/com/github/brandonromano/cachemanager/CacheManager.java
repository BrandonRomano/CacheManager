package com.github.brandonromano.cachemanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.jasypt.util.text.BasicTextEncryptor;

import android.content.Context;
import android.util.Log;

public class CacheManager {
	
	private static CacheManager mInstance;
	private Context mContext;
	private String mCacheDir;
	
	private CacheManager(Context applicationContext){
		mContext = applicationContext;
		mCacheDir = mContext.getCacheDir().toString();
		Log.d(Constants.Tag, "[CacheManager]: Initializing new instance");
	}
	
	public static CacheManager getInstance(Context applicationContext)
	{
		if(mInstance == null){
			mInstance = new CacheManager(applicationContext);
		}
		return mInstance;
	}
	
	
	//=======================================
	//========== String Read/Write ==========
	//=======================================
	
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
			Log.d(Constants.Tag, "[CacheManager]: Writing to " + mCacheDir + fileName);
		} catch (IOException e) {
			Log.d(Constants.Tag, "[CacheManager]: Unsuccessful write to " + mCacheDir + fileName);
			e.printStackTrace();
			throw new CacheTransactionException(Constants.writeExceptionAlert);
		}finally{
			if(out != null)
			{
				try {
					out.flush();
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
			Log.d(Constants.Tag, "[CacheManager]: Reading from " + mCacheDir + fileName);
			return readString;
		}catch(IOException e){
			Log.d(Constants.Tag, "[CacheManager]: Unsuccessful read from " + mCacheDir + fileName);
			e.printStackTrace();
			throw new CacheTransactionException(Constants.readExceptionAlert);
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
	
	
	/**
	 * Encrypts, and then writes a string to the given file name.  
	 * The file will be placed in the current application's cache directory.
	 * 
	 * @param toWrite The String to write to a file.
	 * @param fileName The File name that will be written to.
	 * @param key The encryption/decryption key that will be used to write + read from this file.
	 * @throws CacheTransactionException Throws the exception if writing failed.  Will 
	 * not throw an exception in the result of a successful write.
	 */
	public void writeEncrypted(String toWrite, String fileName, String key) throws CacheTransactionException{
		Log.d(Constants.Tag, "[CacheManager]: Encrypting for a write to " + mCacheDir + fileName);
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(key);
		String encrypted = textEncryptor.encrypt(toWrite);
		write(encrypted, fileName);
	}
	
	
	/**
	 * Reads a string from an existing file in the cache directory,
	 * decrypts it, then returns it.  
	 * 
	 * @param fileName The file name of an existing file in the 
	 * cache directory to be read.
	 * @param key The encryption/decryption key that was used to write to this file.
	 * @return Returns the decrypted version of what is read.  Null if read fails.
	 * @throws CacheTransactionException Throws the exception if reading failed.  
	 * Will not throw an exception in the result of a successful read.
	 */
	public String readEncrypted(String fileName, String key) throws CacheTransactionException{
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(key);
		String encrypted = read(fileName);
		String decrypted = textEncryptor.decrypt(encrypted);
		Log.d(Constants.Tag, "[CacheManager]: Decrypting for a read from " + mCacheDir + fileName);
		return decrypted;
	}
	
	
	//=======================================
	//========== Binary Read/Write ==========
	//=======================================
	
	/**
	 * Writes an array of bytes to the given file name.
	 * The file will be placed in the current application's cache directory.
	 * 
	 * @param toWrite The byte array to write to a file.
	 * @param fileName The File name that will be written to.
	 * @throws CacheTransactionException Throws the exception if writing failed.  Will 
	 * not throw an exception in the result of a successful write.
	 */
	public void write(byte[] toWrite, String fileName) throws CacheTransactionException{
		File file = new File(mCacheDir, fileName);
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(toWrite);
		} catch (Exception e) {
			Log.d(Constants.Tag, "[CacheManager]: Unsuccessful write to " + mCacheDir + fileName);
			e.printStackTrace();
			throw new CacheTransactionException(Constants.writeExceptionAlert);
		} finally{
			if(out != null)
			{
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * Reads an array of bytes from an existing file in the cache directory 
	 * and returns it.
	 * 
	 * @param fileName The file name of an existing file in the 
	 * cache directory to be read.
	 * @return The byte array that was read
	 * @throws CacheTransactionException Throws the exception if reading failed.  
	 * Will not throw an exception in the result of a successful read.
	 */
	public byte[] readBinaryFile(String fileName) throws CacheTransactionException{
		RandomAccessFile RAFile = null;
		try {
			File file = new File(mCacheDir, fileName);
			RAFile = new RandomAccessFile(file, "r");
			byte[] byteArray = new byte[(int)RAFile.length()];
			RAFile.read(byteArray);
			return byteArray;
		} catch (Exception e) {
			Log.d(Constants.Tag, "[CacheManager]: Unsuccessful read from " + mCacheDir + fileName);
			e.printStackTrace();
			throw new CacheTransactionException(Constants.readExceptionAlert);
		} finally{
			if(RAFile != null){
				try {
					RAFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	

	//===========================================
	//========== FileSystem Management ==========
	//===========================================

	/**
	 * Deletes a file in the cache directory.
	 * @param fileName The file to delete.
	 */
	public void deleteFile(String fileName){
		Log.d(Constants.Tag, "[CacheManager]: Deleting the file " + mCacheDir + fileName);
		File toDelete = new File(mCacheDir, fileName);
		toDelete.delete();
	}
	
}
