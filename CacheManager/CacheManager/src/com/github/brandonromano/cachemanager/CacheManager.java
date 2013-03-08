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
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

public class CacheManager {
	
	private static CacheManager mInstance;
	private Context mContext;
	private String mCacheDir;
	
	private CacheManager(Context applicationContext){
		mContext = applicationContext;
		mCacheDir = mContext.getCacheDir().toString()+ "/";
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
	public String readString(String fileName) throws CacheTransactionException
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
	 * @return Returns the decrypted version of what is read.
	 * @throws CacheTransactionException Throws the exception if reading failed.  
	 * Will not throw an exception in the result of a successful read.
	 */
	public String readStringEncrypted(String fileName, String key) throws CacheTransactionException{
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(key);
		String encrypted = readString(fileName); //Will throw here if nothing is read
		String decrypted = textEncryptor.decrypt(encrypted);
		Log.d(Constants.Tag, "[CacheManager]: Decrypting for a read from " + mCacheDir + fileName);
		return decrypted;
	}
	
	//=======================================
	//========== JSON Read/Write ============
	//=======================================
	
	/**
	 * Writes a JSONObject to cache as a readable string to cache.  If JSONObject stores sensitive data
	 * use writeEncrypted for the JSONObject.
	 * 
	 * @param obj The JSONObject to write.
	 * @param fileName The File name that will be written to.
	 * @throws CacheTransactionException Throws the exception if writing failed.  Will 
	 * not throw an exception in the result of a successful write.
	 */
	public void write(JSONObject obj, String fileName) throws CacheTransactionException{
		write(obj.toString(), fileName);
	}
	
	
	/**
	 * Reads a JSONObject from a string file.  Initially runs readString(), so
	 * there may be logs saying there was a successful read, but the log will be followed
	 * up by another log stating that it was unable to create a JSONObject from the read string.
	 * 
	 * @param fileName The file name that will be read from.
	 * @return The JSONObject the file was storing, in the result of a successful read.
	 * @throws CacheTransactionException Throws the exception if reading failed, or the
	 * creation of the JSONObject fails.
	 */
	public JSONObject readJSONObject(String fileName) throws CacheTransactionException{
		String JSONString = readString(fileName); //Will throw exception here if string read fails...
		try {
			JSONObject obj = new JSONObject(JSONString);
			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d(Constants.Tag, "[CacheManager]: Successfully read the file " + mCacheDir + fileName + 
					", but was unable to create a JSONObject from the String.");
			throw new CacheTransactionException(Constants.readExceptionAlert);
		}
	}
	
	
	/**
	 * Writes the JSONObject as an encrypted string to cache.
	 * 
	 * @param obj The JSONObject to write.
	 * @param fileName The File name that will be written to.
	 * @param key The encryption/decryption key that will be used to read from this file.
	 * @throws CacheTransactionException Throws the exception if writing failed.  Will 
	 * not throw an exception in the result of a successful write.
	 */
	public void writeEncrypted(JSONObject obj, String fileName, String key) throws CacheTransactionException{
		writeEncrypted(obj.toString(), fileName, key);
	}
	
	
	/**
	 * Reads an encrypted JSONObject from a string file.  Initially runs readString(), so
	 * there may be logs saying there was a successful read, but the log will be followed
	 * up by another log stating that it was unable to create a JSONObject from the read string.
	 * 
	 * @param fileName The file name that will be read from.
	 * @param key The encryption/decryption key that was used to write to this file.
	 * @return The JSONObject the file was storing, in the result of a successful read.
	 * @throws CacheTransactionException Throws the exception if reading failed, or the
	 * creation of the JSONObject fails.
	 */
	public JSONObject readJSONObjectEncrypted(String fileName, String key) throws CacheTransactionException {
		String JSONString = readStringEncrypted(fileName, key); //Will throw exception here if string read fails...
		try {
			JSONObject obj = new JSONObject(JSONString);
			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d(Constants.Tag, "[CacheManager]: Successfully read the file " + mCacheDir + fileName + 
					", but was unable to create a JSONObject from the String.");
			throw new CacheTransactionException(Constants.readExceptionAlert);
		}
	}
	
	
	//=======================================
	//========= Bitmap Read/Write ===========
	//=======================================
	
	/**
	 * Writes a Bitmap to the given file name.  The file will be placed
	 * in the current application's cache directory.
	 * 
	 * @param bitmap The Bitmap to be written to cache.
	 * @param format The format that the Bitmap will be written to cache. 
	 * 	(Either CompressFormat.PNG, CompressFormat.JPEG, or CompressFormat.WEBP) 
	 * @param quality The quality that the Bitmap will be written at.  0 is the lowest quality, 100
	 *  is the highest quality.  If you are writing as .PNG format, this parameter will not matter 
	 *  as PNG is lossless.
	 * @param fileName The File name that will be written to.
	 * @throws CacheTransactionException Throws the exception if writing failed.  Will 
	 * not throw an exception in the result of a successful write.
	 */
	public void write(Bitmap bitmap, CompressFormat format, int quality, String fileName) throws CacheTransactionException {     
	    
		File file = new File(mCacheDir, fileName);
		
		FileOutputStream out = null;
	    try {      
	        out = new FileOutputStream(file); 
	        bitmap.compress(format, quality, out);
	    } catch (Exception e) {
	    	Log.d(Constants.Tag, "[CacheManager]: Unsuccessful write to " + mCacheDir + fileName);
	    	e.printStackTrace();
			throw new CacheTransactionException(Constants.writeExceptionAlert);
	    } finally{
	    	if(out != null){
	    		try{
	    			out.flush();
	    			out.close();
	    		}catch(IOException e){
	    			e.printStackTrace();
	    		}
	    	}
	    }
	}
	
	
	/**
	 * Reads a bitmap from the specified file and returns the bitmap.
	 * 
	 * @param fileName The File name that will be read from.
	 * @return Returns the bitmap in the case of a successful read.
	 * @throws CacheTransactionException CacheTransactionException Throws the exception if reading failed.  
	 * Will not throw an exception in the result of a successful read.
	 */
	public Bitmap readBitmap(String fileName) throws CacheTransactionException {
		File file = new File(mCacheDir, fileName);
		Bitmap bitmap = BitmapFactory.decodeFile(file.toString());
		if(bitmap != null){
			return bitmap;
		}else{ // BitmapFactory.decodeFile returns null if it can't decode a bitmap.
			throw new CacheTransactionException(Constants.readExceptionAlert); 
		}
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
