package com.hci.prototype.mobilereceipts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
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
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
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

    private DrawerLayout mDrawerLayout;
    
    private ExpandableListView mDrawerList;
    private List<String> navHeader;
    private HashMap<String, List<String>> navChild;
    
    private String sort = "timestamp";
    private String filter = "No Filter";
    private String type = "All";
    
    
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receipt_list);
				
		mPrefs = getPreferences(Context.MODE_PRIVATE);
		mDb = new ReceiptDbAdapter(getApplicationContext());
		setListAdapter(new ReceiptListAdapter(getApplicationContext(), null));
		mDb.open();
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);
        
        prepareListData();
        
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ExpandableListAdapter(this,
                navHeader, navChild));
        // Set the list's click listener
        //mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Listview Group click listener
        mDrawerList.setOnGroupClickListener(new OnGroupClickListener() {
 
            @Override
            public boolean onGroupClick(ExpandableListView parent, View row,
                    int groupPosition, long id) {
               

                return false;
            }
        });
        
        // Listview Group expanded listener
        mDrawerList.setOnGroupExpandListener(new OnGroupExpandListener() {
 
            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        navHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
            }
        });
 
        // Listview Group collasped listener
        mDrawerList.setOnGroupCollapseListener(new OnGroupCollapseListener() {
 
            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        navHeader.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();
 
            }
        });
 
        // Listview on child click listener
        mDrawerList.setOnChildClickListener(new OnChildClickListener() {
 
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                    int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
                Toast.makeText(
                        getApplicationContext(),
                        navHeader.get(groupPosition)
                                + " : "
                                + navChild.get(
                                        navHeader.get(groupPosition)).get(
                                        childPosition), Toast.LENGTH_SHORT)
                        .show();
                if(groupPosition == 0){
                	sort = navChild.get(navHeader.get(groupPosition)).get(childPosition);
                	
                	if(sort == "Date"){
                		sort = "timestamp";
                	} else if(sort == "Name"){
                		sort = "title";
                	} 
                	
                	new AsyncCursor().execute(sort, filter, type);
                	
            	} else if(groupPosition == 1){
            		filter = navChild.get(navHeader.get(groupPosition)).get(childPosition);
            		
            		new AsyncCursor().execute(sort, filter, type);
            	} else if(groupPosition == 2){
            		type = navChild.get(navHeader.get(groupPosition)).get(childPosition);
            		
            		new AsyncCursor().execute(sort, filter, type);
            	}
                
                if(groupPosition == 3){

	 				Intent i = new Intent(ReceiptList.this, BudgetViewActivity.class);
	 				startActivity(i);
               }
                return false;
            }
        });
		
	}

	private void prepareListData() {
        navHeader = new ArrayList<String>();
        navChild = new HashMap<String, List<String>>();
 
        navHeader.add("Sort By");
        navHeader.add("Filter By Category");
        navHeader.add("Toggle Business/Casual");
        navHeader.add("View Budget");
       // navHeader.add("Export To CSV");
       // navHeader.add("Finish Incomplete Receipts");
        
        // Adding child data
        List<String> sort = new ArrayList<String>();
        sort.add("Date");
        sort.add("Category");
        sort.add("Amount");
        sort.add("Name");
        
        List<String> filter = new ArrayList<String>();
        filter.add("Payments");
        filter.add("Dining Out");
        filter.add("Entertainment");
        filter.add("Groceries");
        filter.add("Misc");
        filter.add("No Filter");

        List<String> business = new ArrayList<String>();
        business.add("Business");
        business.add("Casual");
        business.add("All");
        
        List<String> budget = new ArrayList<String>();
        budget.add("Begin");
        
        navChild.put(navHeader.get(0), sort); // Header, Child data
        navChild.put(navHeader.get(1), filter);
        navChild.put(navHeader.get(2), business);
        navChild.put(navHeader.get(3), budget);
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
		new AsyncCursor().execute(sort, filter, type);
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
					mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
			    
				Log.e("ReceiptList","Tempfile : " + tempFile);
				
				mDb.createReceipt(resources.getString(R.string.temp_filename), tempFile);
				new AsyncCursor().execute(sort, filter, type);
			}else{
				// Image capture cancelled. Don't add an entry to the database.
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
	protected void onListItemClick (ListView l, View row, int position, long id){
		final TextView label=(TextView)row.findViewById(R.id.label);
		final TextView cost=(TextView)row.findViewById(R.id.cost);
		final TextView category=(TextView)row.findViewById(R.id.category);
		final TextView type = (TextView)row.findViewById(R.id.type);
		final TextView date=(TextView)row.findViewById(R.id.date);
		final TextView filename=(TextView)row.findViewById(R.id.filename);
		final TextView key_id =(TextView)row.findViewById(R.id.key_id);
		
		Intent i = new Intent(this, ReceiptDetailEditActivity.class);
		i.putExtra("key_id", key_id.getText().toString());
		i.putExtra("label", label.getText().toString());
		i.putExtra("cost", cost.getText().toString());
		i.putExtra("category", category.getText().toString());
		i.putExtra("type", type.getText().toString());
		i.putExtra("date", date.getText().toString());
		i.putExtra("filename", filename.getText().toString());
		startActivity(i);
		
	}
	
	/*
	 * Helper function used to trigger an update to the UI thread's
	 * list of receipt entries.
	 */
	private void resetCursor(Cursor c){
		((ReceiptListAdapter)getListAdapter()).changeCursor(c);
	}
	
	/*
	 * This inner class will be responsible for updating individual list entries
	 * from XML files.
	 */
	class ReceiptListAdapter extends SimpleCursorAdapter {
		
		ReceiptListAdapter(Context ctxt, Cursor cursor) {
			super(ctxt, R.layout.row, cursor, 
					new String[]{ReceiptDbAdapter.KEY_TITLE, ReceiptDbAdapter.KEY_AMOUNT}, 
					new int[]{R.id.label,R.id.bottom},
					0);
		}

		@Override
		public void bindView(View row, Context context, Cursor cursor) {
			
			// Set all the fields in the given view to their corresponding
			// cursor values
			final TextView label=(TextView)row.findViewById(R.id.label);
			final LinearLayout bottom =(LinearLayout)row.findViewById(R.id.bottom);
			final TextView cost = (TextView)row.findViewById(R.id.cost);
			final TextView cat = (TextView)row.findViewById(R.id.cat);
			
			
			// Set the row's label text
			int labelIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_TITLE);
			label.setText(cursor.getString(labelIndex));
			
			// Set the row's total cost
			int costIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_AMOUNT);
			cost.setText(String.valueOf(cursor.getDouble(costIndex)));
			
			int catIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_CATEGORY);
			cat.setText(cursor.getString(catIndex));

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
			final TextView type = (TextView)row.findViewById(R.id.type);
			final TextView date=(TextView)row.findViewById(R.id.date);
			final TextView filename=(TextView)row.findViewById(R.id.filename);
			final TextView id =(TextView)row.findViewById(R.id.key_id);
			
			final int categoryIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_CATEGORY);
			final int typeIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_TYPE);
			final int dateIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_TIME);
			final int filenameIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_FILENAME);
			final int idIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_ROWID);
			
			category.setText(cursor.getString(categoryIndex));
			type.setText(cursor.getString(typeIndex));
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
	private class AsyncCursor extends AsyncTask<String,Void,Cursor>{

		@Override
		protected Cursor doInBackground(String... args) {
			return mDb.fetchAllNotes(args[0], args[1], args[2]);
		}
		
		@Override
		protected void onPostExecute(Cursor cursor){
			synchronized(getListAdapter()){
				resetCursor(cursor);
			}
		}
	}

}
