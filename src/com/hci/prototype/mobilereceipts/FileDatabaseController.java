package com.hci.prototype.mobilereceipts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/*
 * This class contains utility functions for working with the file field
 * from the database.
 */
public class FileDatabaseController {

	private static String JPEG_FILE_PREFIX = "receipt_";
	private static final String JPEG_FILE_SUFFIX=".jpg";
	
	public File getAlbumDir(){
		try {
			PackageManager m = MyApplication.getAppContext().getPackageManager();
			String s = MyApplication.getAppContext().getPackageName();
			PackageInfo p = m.getPackageInfo(s, 0);
			s = p.applicationInfo.dataDir;
			return new File(s);
		} catch (NameNotFoundException e) {
			Log.e("DatabaseController", "Could not access application data directory");
		}
		return null;
	}
	
	public File createImageFile() {
		// Create an image file name in that directory to be used by the camera
		// to store received data.
	    String timeStamp = 
	        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
	    File image = null;
		try {
			image = File.createTempFile(
			    imageFileName, 
			    JPEG_FILE_SUFFIX, 
			    getAlbumDir()
			);
		} catch (IOException e) {
			Log.e("DatabaseController","Could not create temporary file for image storage");
		}
	    //mCurrentPhotoPath = image.getAbsolutePath();
	    return image;
	}
	
	public static Bitmap getBitmapFromFile(String filepath){
		InputStream in = null;
		try {
			File tempFile = new File(filepath);
			Log.e("FileDatabaseController","Filelength : " + tempFile.length());
			in = new FileInputStream(filepath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return BitmapFactory.decodeStream(in);
	}

}
