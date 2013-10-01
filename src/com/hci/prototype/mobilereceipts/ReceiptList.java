package com.hci.prototype.mobilereceipts;

import java.io.File;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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


	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receipt_list);
				
		mDb = new ReceiptDbAdapter(getApplicationContext());
		setListAdapter(new ReceiptListAdapter(getApplicationContext(), null));
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		mDb.open();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
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
					DatabaseController controller = new DatabaseController();
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
		private final String[] viewFrom = new String[]{ReceiptDbAdapter.KEY_TITLE, ReceiptDbAdapter.KEY_AMOUNT};
		private final int[] viewTo={R.id.label,R.id.cost};
		
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
			for (int i =0; i < viewFrom.length; ++i){
				TextView field = (TextView)row.findViewById(viewTo[i]);
				int cursorIndex = cursor.getColumnIndex(viewFrom[i]);
				field.setText(cursor.getString(cursorIndex));
			}
			
			// Now perform some customization of the views. 
			// 1) Set cost colors to indicated credit or debit
			// 2) Set row colors for visual separation.
			final TextView size=(TextView)row.findViewById(R.id.label);
			final TextView cost=(TextView)row.findViewById(R.id.cost);
			
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
			resetCursor(cursor);
		}
	}

}
