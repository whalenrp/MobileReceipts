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

	public static Bitmap getBitmapFromFile(final String filepath){
		InputStream in = null;
		try {
			final File tempFile = new File(filepath);
			Log.e("FileDatabaseController","Filelength : " + tempFile.length());
			in = new FileInputStream(filepath);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return BitmapFactory.decodeStream(in);
	}

	public File createImageFile() {
		// Create an image file name in that directory to be used by the camera
		// to store received data.
		final String timeStamp =
				new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		final String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File image = null;
		try {
			image = File.createTempFile(
					imageFileName,
					JPEG_FILE_SUFFIX,
					new File(Util.DATA_PATH + "tessdata")
					);
		} catch (final IOException e) {
			Log.e("DatabaseController","Could not create temporary file for image storage");
		}
		//mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}

	public File getAlbumDir(){
		try {
			final PackageManager m = MyApplication.getAppContext().getPackageManager();
			String s = MyApplication.getAppContext().getPackageName();
			final PackageInfo p = m.getPackageInfo(s, 0);
			s = p.applicationInfo.dataDir;
			return new File(s);
		} catch (final NameNotFoundException e) {
			Log.e("DatabaseController", "Could not access application data directory");
		}
		return null;
	}

}
