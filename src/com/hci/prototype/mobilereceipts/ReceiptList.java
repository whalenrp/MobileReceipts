package com.hci.prototype.mobilereceipts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;



/*
 * This class will be the main entry point of the application. It will 
 * house a list of all color-coded transations as well as buttons in the
 * top corners to access the camera to log more receipts, and a side-menu
 * to give extended options
 */
public class ReceiptList extends Activity implements AdapterView.OnItemClickListener{
	
	private static final int ACTION_CAMERA_CAPTURE = 1337;
	private String tempFile;
	private ReceiptDbAdapter mDb;
	private SharedPreferences mPrefs;
	 
	private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ListView mReceiptList;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receipt_list);
				
		mPrefs = getPreferences(Context.MODE_PRIVATE);
		
		mReceiptList = (ListView)findViewById(R.id.list);
		mDb = new ReceiptDbAdapter(getApplicationContext());
		mReceiptList.setAdapter(new ReceiptListAdapter(getApplicationContext(), null));
		mReceiptList.setOnItemClickListener(this);
		mDb.open();
		
		mPlanetTitles = getResources().getStringArray(R.array.nav);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        // Set the list's click listener
       // mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	}
	
	/*
	 * Retrieve the most recently stored temporary filename for use in updating 
	 * our database.
	 */
	@Override
	protected void onResume(){
		super.onResume();
		mPrefs = getPreferences(Context.MODE_PRIVATE);
		tempFile = mPrefs.getString("tempFile", null);
		Log.i("ReceiptList","Retrieving tempFile as : " + tempFile);
		
		// Asynchronously updates the UI thread's list entries
		new AsyncCursor().execute();
	}
	
	/*
	 * Store the most recently used temporary filename for use in updating 
	 * our database when we receive a callback.
	 */
	@Override
	protected void onPause(){
		super.onPause();
		Log.i("ReceiptList","Storing tempFile as : " + tempFile);
		SharedPreferences.Editor edit = mPrefs.edit();
		edit.putString("tempFile", tempFile);
		edit.commit();
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
					
					// This adds the new file created by the FileDatabaseController to the intent to 
					// take a picture, letting the camera know where to save the image.
					Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					//takePictureIntent.putExtra("filename", outFile.getAbsolutePath());
					//takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));
					
					// Store the temporary filename so that we can properly add a reference to 
					// it in our database when the camera returns with a success.
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
	 * with a resulting image stored in the intent bundle. The intent is configured to store the 
	 * image in a temporary file retrieved from FileDatabaseController
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
				
				Bundle extras = intent.getExtras();
			    Bitmap mImageBitmap = (Bitmap) extras.get("data");
			    try {
					FileOutputStream fos = new FileOutputStream(tempFile);
					mImageBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
			    
				Log.e("ReceiptList","Tempfile : " + tempFile);
				
				mDb.createReceipt(resources.getString(R.string.temp_filename), tempFile);
				new AsyncCursor().execute();
			}else{
				// Image capture cancelled. Don't add an entry to the database.
			}
		}
	}
	
	
	/*
	 * Helper function used to trigger an update to the UI thread's
	 * list of receipt entries.
	 */
	private void resetCursor(Cursor c){
		((ReceiptListAdapter)mReceiptList.getAdapter()).changeCursor(c);
	}
	
	/*
	 * This inner class will be responsible for updating individual list entries
	 * from XML files.
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
			
			// Store the database fields for this particular entry in the view's tag field 
			// for easy lookup later
			final TextView category=(TextView)row.findViewById(R.id.category);
			final TextView date=(TextView)row.findViewById(R.id.date);
			final TextView filename=(TextView)row.findViewById(R.id.filename);
			final TextView id =(TextView)row.findViewById(R.id.key_id);
			
			final int categoryIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_CATEGORY);
			final int dateIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_TIME);
			final int filenameIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_FILENAME);
			final int idIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_ROWID);
			
			category.setText(cursor.getString(categoryIndex));
			date.setText(cursor.getString(dateIndex));
			filename.setText(cursor.getString(filenameIndex));
			id.setText(cursor.getString(idIndex).toString());
		}
		
	}


	/*
	 * This class is responsible for asyncronously retrieving a cursor to the receipts 
	 * database. Once it receives a new cursor, it will update the UI thread's list with
	 * the new data.
	 */
	private class AsyncCursor extends AsyncTask<Void,Void,Cursor>{

		@Override
		protected Cursor doInBackground(Void... arg0) {
			return mDb.fetchAllNotes();
		}
		
		@Override
		protected void onPostExecute(Cursor cursor){
			synchronized(mReceiptList){
				resetCursor(cursor);
			}
		}
	}


	/*
	 * This method will fire an intent for the ReceiptDetailEditActivity that contains the
	 * database values for the given row. 
	 * Each row has hidden fields that contain the database values for that row. These fields
	 * will be added to the intent before it is sent to minimize time spend querying the database.
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View row, int arg2, long arg3) {
		// TODO Auto-generated method stub
		final TextView label=(TextView)row.findViewById(R.id.label);
		final TextView cost=(TextView)row.findViewById(R.id.cost);
		final TextView category=(TextView)row.findViewById(R.id.category);
		final TextView date=(TextView)row.findViewById(R.id.date);
		final TextView filename=(TextView)row.findViewById(R.id.filename);
		final TextView key_id =(TextView)row.findViewById(R.id.key_id);
		
		Intent i = new Intent(this, ReceiptDetailEditActivity.class);
		i.putExtra("key_id", key_id.getText().toString());
		i.putExtra("label", label.getText().toString());
		i.putExtra("cost", cost.getText().toString());
		i.putExtra("category", category.getText().toString());
		i.putExtra("date", date.getText().toString());
		i.putExtra("filename", filename.getText().toString());
		startActivity(i);
	}

}
