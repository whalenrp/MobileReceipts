package com.hci.prototype.mobilereceipts;

import java.io.File;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
/*
 * This class will be the main entry point of the application. It will 
 * house a list of all color-coded transations as well as buttons in the
 * top corners to access the camera to log more receipts, and a side-menu
 * to give extended options
 */
public class ReceiptList extends ListActivity {
	
	private static final int ACTION_CAMERA_CAPTURE = 1337;
	private String tempFile;
	private ReceiptDbAdapter mDb;
	private SharedPreferences mPrefs;


	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receipt_list);
				
		mPrefs = getPreferences(Context.MODE_PRIVATE);
		mDb = new ReceiptDbAdapter(getApplicationContext());
		setListAdapter(new ReceiptListAdapter(getApplicationContext(), null));
		mDb.open();
		new AsyncCursor().execute();
	}
	@Override
	protected void onResume(){
		super.onResume();
		mPrefs = getPreferences(Context.MODE_PRIVATE);
		tempFile = mPrefs.getString("tempFile", null);
		Log.e("ReceiptList","Retrieving tempFile as : " + tempFile);
	}
	@Override
	protected void onPause(){
		super.onPause();
		Log.e("ReceiptList","Storing tempFile as : " + tempFile);
		SharedPreferences.Editor edit = mPrefs.edit();
		edit.putString("tempFile", tempFile);
		edit.commit();
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		//mDb.open();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		//mDb.close();
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		mDb.close();
	}

	/*
	 * Configure the ActionBar to show the camera image.
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return true;
	}
	
	/*
	 * When the add_receipt button is pressed, an intent will be fired off to start
	 * the camera for a picture. 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		
			case R.id.receipt_add:
				
				if (Util.isIntentAvailable(MyApplication.getAppContext(), MediaStore.ACTION_IMAGE_CAPTURE)){
					FileDatabaseController controller = new FileDatabaseController();
					File outFile = controller.createImageFile();
					
					Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					takePictureIntent.putExtra("filename", outFile.getAbsolutePath());
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));
					
					tempFile = outFile.getAbsolutePath();
				    startActivityForResult(takePictureIntent,ACTION_CAMERA_CAPTURE);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
				
		}
		
	}
	
	/*
	 * This method will trigger when this activity receives a callback from the camera application 
	 * with a resulting image stored in the intent bundle. The intent was configured to store the 
	 * image in the 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent){
		if (requestCode == ACTION_CAMERA_CAPTURE){
			// We have successfully captured an image. 
			// Create an entry in the database with the filepath used to 
			// store the image
			if (resultCode != RESULT_CANCELED){
				// This should be run in a background thread.
				Resources resources = getApplicationContext().getResources();
				mPrefs = getPreferences(Context.MODE_PRIVATE);
				tempFile = mPrefs.getString("tempFile", null);
				Log.e("ReceiptList","Tempfile : " + tempFile);
				
				mDb.createReceipt(resources.getString(R.string.temp_filename), tempFile);
				new AsyncCursor().execute();
			}else{
				// Image capture cancelled. Don't add an entry to the database.
			}
		}
	}
	
	/*
	 * This inner class will be responsible for updating individual list entries
	 * from XML files. It performs some list-optimization for better scrolling.
	 */
	class ReceiptListAdapter extends SimpleCursorAdapter {
		
		ReceiptListAdapter(Context ctxt, Cursor cursor) {
			super(ctxt, R.layout.row, cursor, 
					new String[]{ReceiptDbAdapter.KEY_TITLE, ReceiptDbAdapter.KEY_AMOUNT}, 
					new int[]{R.id.label,R.id.cost},
					0);
		}

		@Override
		public void bindView(View row, Context context, Cursor cursor) {
			
			// Set all the fields in the given view to their corresponding
			// cursor values
			final TextView label=(TextView)row.findViewById(R.id.label);
			final TextView cost=(TextView)row.findViewById(R.id.cost);
			
			// Set the row's label text
			int labelIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_TITLE);
			label.setText(cursor.getString(labelIndex));
			
			// Set the row's total cost
			int costIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_AMOUNT);
			cost.setText(String.valueOf(cursor.getDouble(costIndex)));

			// Stylize each row and give the cost different colors for different amounts
			Resources resources = getApplicationContext().getResources();
			if (Double.parseDouble(cost.getText().toString()) < 0.0) {
				cost.setTextColor(resources.getColor(R.color.debit));
			}
			else {
				cost.setTextColor(resources.getColor(R.color.credit));
			}
			if (cursor.getPosition() % 2 == 0)
				row.setBackgroundColor(resources.getColor(R.color.dark_gray));
			else
				row.setBackgroundColor(resources.getColor(R.color.light_gray));
			
			// Store the database key for this particular entry in the view's tag field 
			// for easy lookup later
			int idIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_ROWID);
			row.setTag(cursor.getInt(idIndex));
		}
		
	}

	private void resetCursor(Cursor c){
		((ReceiptListAdapter)getListAdapter()).changeCursor(c);
	}
	
	private class AsyncCursor extends AsyncTask<Void,Void,Cursor>{

		@Override
		protected Cursor doInBackground(Void... arg0) {
			return mDb.fetchAllNotes();
		}
		
		@Override
		protected void onPostExecute(Cursor cursor){
			synchronized(getListAdapter()){
				resetCursor(cursor);
			}
		}
	}

}
