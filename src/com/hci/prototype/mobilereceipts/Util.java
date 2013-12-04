package com.hci.prototype.mobilereceipts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public final class Util {
	
	private final static String TAG = "Util.java";
	private final static String lang = "eng";
	public final static String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/";
	

	public static String formatPrice(final double price){
		final NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return formatter.format(price);
	}

	public static boolean isIntentAvailable(final Context context, final String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		final List<ResolveInfo> list =
				packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	public static String getReceiptText(Context ctxt, String filename){
		// lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
        	if (!(new File(DATA_PATH + "tessdata")).exists()){
        		new File(DATA_PATH + "tessdata").mkdir();
        	}
                try {

                        AssetManager assetManager = ctxt.getAssets();
                        InputStream in = assetManager.open(lang + ".traineddata");
                        OutputStream out = new FileOutputStream(DATA_PATH
                                        + "tessdata/" + lang + ".traineddata");

                        // Transfer bytes from in to out
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                        }
                        in.close();
                        out.close();
                        
                        Log.v(TAG, "Copied " + lang + " traineddata");
                } catch (IOException e) {
                        Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
                }
        }
        
		BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile(filename, options);

        try {
                ExifInterface exif = new ExifInterface(filename);
                int exifOrientation = exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL);

                Log.v(TAG, "Orient: " + exifOrientation);

                int rotate = 0;

                switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                        rotate = 90;
                        break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                        rotate = 180;
                        break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                        rotate = 270;
                        break;
                }

                Log.v(TAG, "Rotation: " + rotate);

                if (rotate != 0) {

                        // Getting width & height of the given image.
                        int w = bitmap.getWidth();
                        int h = bitmap.getHeight();

                        // Setting pre rotate
                        Matrix mtx = new Matrix();
                        mtx.preRotate(rotate);

                        // Rotating Bitmap
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                }

                // Convert to ARGB_8888, required by tess
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        } catch (IOException e) {
                Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }

        
        Log.v(TAG, "Before baseApi");

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, "eng");
        baseApi.setImage(bitmap);
        
        String recognizedText = baseApi.getUTF8Text();
        
        baseApi.end();

        // You now have the text in recognizedText var, you can do anything with it.
        // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
        // so that garbage doesn't make it to the display.

        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");

        
        return recognizedText.trim();

	}
}
