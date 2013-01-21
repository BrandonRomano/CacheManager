package com.github.brandonromano.cachemanagerexample;

import com.example.cachemanagerexample.R;
import com.github.brandonromano.cachemanager.CacheManager;
import com.github.brandonromano.cachemanager.CacheTransactionException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CacheExampleActivity extends Activity{
	
	final String mFileName = "exampleFileName.txt";
	CacheManager mCacheManager;
	Button mCacheButton, mDisplayButton;
	EditText mEditText;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cache_example);
		mCacheManager = CacheManager.getInstance(this);
		
		getViews();
	}

	private void getViews() {
		mCacheButton = (Button) findViewById(R.id.cacheButton);
		mDisplayButton = (Button) findViewById(R.id.displayButton);
		mEditText = (EditText) findViewById(R.id.editText);
	}
	
	
	
	//====== onClicks
	/**
	 * Activated when the cacheButton is clicked
	 */
	public void onCacheClick(View v)
	{
		String toCache = mEditText.getText().toString();
		if(toCache.equals("") || toCache == null)
		{
			Toast.makeText(getApplicationContext(), "Type something in the textbox to cache", Toast.LENGTH_SHORT).show();
		}else{
			
			
			//====== Here's where the write to the cache directory occurs.
			//====== Creates the file "exampleFileName.txt" in the App's cacheDir
			//====== and writes whatever was in EditText box.
			try {
				mCacheManager.write(toCache, mFileName);
			} catch (CacheTransactionException e) {
				// TODO The write failed... This is where you handle a unsuccessful write.
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	/**
	 * Activted when the display button is cicked.
	 */
	public void onDisplayClick(View v)
	{
		String toDisplay = null;
		
		//====== Here's where the read from the cache directory occurs.
		//====== the read will return whatever was read in the form of a string.
		try {
			toDisplay = mCacheManager.read(mFileName);
		} catch (CacheTransactionException e) {
			// TODO The read has failed... This is where you handle an unsuccessful read.
			// There's a possibility of cache being cleared by the android OS, so this is 
			// likely to happen at some point.
			e.printStackTrace();
		}
		
		if(toDisplay != null){
			Toast.makeText(getApplicationContext(), toDisplay, Toast.LENGTH_SHORT).show();
		}
	}

}
