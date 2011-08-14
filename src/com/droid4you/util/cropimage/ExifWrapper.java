package com.droid4you.util.cropimage;

import java.io.IOException;

import android.media.ExifInterface;
import android.util.Log;

public class ExifWrapper {
	private ExifInterface instance;
	
	static {
		try {
			Class.forName("android.media.ExifInterface");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void checkAvailable() {}
	
	public ExifWrapper(String filename){
		try {
			instance = new ExifInterface(filename);
		} catch (IOException e) {
			Log.e("ExifWrapper", "IOException", e);
		}
	}
	
	public int getAttributeInt(String tag, int defaultValue) {
		if (instance == null) {
			return defaultValue;
		}
		
		return instance.getAttributeInt(tag, defaultValue);
	}
	
}