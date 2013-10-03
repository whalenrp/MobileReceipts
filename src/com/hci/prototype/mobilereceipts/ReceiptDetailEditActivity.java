package com.hci.prototype.mobilereceipts;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

public class ReceiptDetailEditActivity extends Activity {
	
	private int key_id;
	private AspectRatioImage receiptImg;
	private EditText receiptTitle;
	private EditText receiptCost;
	private Spinner receiptCategory;
	private DatePicker receiptDate;
	
	
	@Override
	protected void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);
		setContentView(R.layout.receipt_detail_edit_activity);
		
		receiptImg = (AspectRatioImage)findViewById(R.id.receiptImage);
		receiptTitle = (EditText)findViewById(R.id.receiptTitle);
		receiptCost = (EditText)findViewById(R.id.receiptTotal);
		receiptCategory = (Spinner)findViewById(R.id.receiptCategory);
		receiptDate = (DatePicker)findViewById(R.id.receiptDate);
		
		//mDb = new ReceiptDbAdapter(getApplicationContext());
		
		Intent i = getIntent();
		key_id = Integer.parseInt(i.getStringExtra("key_id"));
		receiptTitle.setHint(i.getStringExtra("label"));
		receiptCost.setHint(i.getStringExtra("cost"));
		//receiptCategory.setSelection(position, animate)i.getStringExtra("category"));
		//receiptDate.set....("date", date.getText().toString());
		
		Bitmap image = FileDatabaseController.getBitmapFromFile(i.getStringExtra("filename"));
		Log.e("ReceiptDetailEditActivity",i.getStringExtra("filename"));
		
		receiptImg.setImageBitmap(image);
		
		// set up the spinner with the categories resources array
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.categories, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		receiptCategory.setAdapter(adapter);
	}
	
	/*
	 * This method will be called when the user clicks "Save" at the bottom of the activity.
	 * It will commit the changes currently stored in the activity fields to the database
	 * and return to the main activity.
	 */
	public void onSubmit(View v){
		int   day  = receiptDate.getDayOfMonth();
		int   month= receiptDate.getMonth();
		int   year = receiptDate.getYear();

		SimpleDateFormat sdf = new SimpleDateFormat(ReceiptDbAdapter.DATE_FORMAT);
		String formattedDate = sdf.format(new Date(year, month, day));
		// Send data to AsyncCursor
		ContentValues cv = new ContentValues();
		cv.put(ReceiptDbAdapter.KEY_TITLE, receiptTitle.getText().toString());
		cv.put(ReceiptDbAdapter.KEY_AMOUNT, receiptCost.getText().toString());
		cv.put(ReceiptDbAdapter.KEY_TIME, formattedDate);
		new AsyncCursor(this, key_id).execute(cv);
		finish();
	}
	
	/*
	 * This class is responsible for asyncronously retrieving a cursor to the receipts 
	 * database. Once it receives a new cursor, it will update the UI thread's list with
	 * the new data.
	 */
	private class AsyncCursor extends AsyncTask<ContentValues,Void,Void>{

		private ReceiptDbAdapter mDb;
		private long rowId;
		public AsyncCursor(Context ctxt, long id){
			mDb = new ReceiptDbAdapter(ctxt);
			rowId = id;
			mDb.open();
		}
		
		@Override
		protected Void doInBackground(ContentValues... arg0) {
			mDb.updateReceipt(rowId, arg0[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void nothing){
			mDb.close();
		}
	}

}
