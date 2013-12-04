package com.hci.prototype.mobilereceipts;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
/*
 * This class will be the main entry point of the application. It will
 * house a list of all color-coded transations as well as buttons in the
 * top corners to access the camera to log more receipts, and a side-menu
 * to give extended options
 */
public class ReceiptList extends ListActivity {

	/*
	 * This class is responsible for asyncronously retrieving a cursor to the receipts
	 * database. Once it receives a new cursor, it will update the UI thread's list with
	 * the new data.
	 */
	private class AsyncCursor extends AsyncTask<String,Void,Cursor>{

		@Override
		protected Cursor doInBackground(final String... args) {
			return mDb.fetchAllNotes(args[0], args[1], args[2]);
		}

		@Override
		protected void onPostExecute(final Cursor cursor){
			synchronized(getListAdapter()){
				resetCursor(cursor);
			}
		}
	}
	/*
	 * This inner class will be responsible for updating individual list entries
	 * from XML files.
	 */
	class ReceiptListAdapter extends SimpleCursorAdapter {

		ReceiptListAdapter(final Context ctxt, final Cursor cursor) {
			super(ctxt, R.layout.row, cursor,
					new String[]{ReceiptDbAdapter.KEY_TITLE, ReceiptDbAdapter.KEY_AMOUNT},
					new int[]{R.id.label,R.id.bottom},
					0);
		}

		@Override
		public void bindView(final View row, final Context context, final Cursor cursor) {

			// Set all the fields in the given view to their corresponding
			// cursor values
			final TextView label=(TextView)row.findViewById(R.id.label);
			final LinearLayout bottom =(LinearLayout)row.findViewById(R.id.bottom);
			final TextView cost = (TextView)row.findViewById(R.id.cost);
			final TextView cat = (TextView)row.findViewById(R.id.cat);


			// Set the row's label text
			final int labelIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_TITLE);
			label.setText(cursor.getString(labelIndex));

			// Set the row's total cost
			final int costIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_AMOUNT);
			cost.setText(String.valueOf(cursor.getDouble(costIndex)));

			final int catIndex = cursor.getColumnIndex(ReceiptDbAdapter.KEY_CATEGORY);
			cat.setText(cursor.getString(catIndex));

			// Stylize each row and give the cost different colors for different amounts
			final Resources resources = getApplicationContext().getResources();
			if (Double.parseDouble(cost.getText().toString()) < 0.0) {
				cost.setTextColor(resources.getColor(R.color.debit));
			}
			else {
				cost.setTextColor(resources.getColor(R.color.credit));
			}
			if (cursor.getPosition() % 2 == 0) {
				row.setBackgroundColor(resources.getColor(R.color.dark_gray));
			} else {
				row.setBackgroundColor(resources.getColor(R.color.light_gray));
			}

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
	
	/***************************************************
	 * Button Listeners
	 *************************************************/
	
	private class OCRDialogListener implements DialogInterface.OnClickListener{

		private String ocrText;
		
		public OCRDialogListener(String ocrText){
			this.ocrText = ocrText;
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch(which){
				case DialogInterface.BUTTON_POSITIVE:
					mDb.createReceipt(ocrText.substring(0, Math.min(ocrText.length(), 20)), tempFile);
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					final Resources resources = getApplicationContext().getResources();
					mDb.createReceipt(resources.getString(R.string.temp_filename), tempFile);
					break;
				case DialogInterface.BUTTON_NEUTRAL:
					break;
				default:
					throw new UnsupportedOperationException();
			}
			new AsyncCursor().execute(sort, filter, type);
		}
		
	}
	private static final int ACTION_CAMERA_CAPTURE = 1337;
	private String tempFile;

	private ReceiptDbAdapter mDb;

	private SharedPreferences mPrefs;
	private ExpandableListView mDrawerList;

	private List<String> navHeader;
	private Map<String, List<String>> navChild;
	
	// Sort and filter options for the list data.
	private String sort = "timestamp";
	private String filter = "No Filter";
	private String type = "All";

	/*
	 * This method will trigger when this activity receives a callback from the camera application
	 * with a resulting image stored in the intent bundle. The intent is configured to store the
	 * image in a temporary file retrieved from FileDatabaseController
	 */
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent){
		if (requestCode == ACTION_CAMERA_CAPTURE){
			// We have successfully captured an image.
			// Create an entry in the database with the filepath used to
			// store the image
			if (resultCode != RESULT_CANCELED){

				
				mPrefs = getPreferences(Context.MODE_PRIVATE);
				tempFile = mPrefs.getString("tempFile", null);

				Log.e("ReceiptList","Tempfile : " + tempFile);
				
				String ocrParsedText = Util.getReceiptText(this, tempFile);
				
				Log.e("ReceiptList", ocrParsedText);
				
				createAndShowOCRConfirmationDialog(ocrParsedText);				
			}
		}
	}

	private void createAndShowOCRConfirmationDialog(String ocrParsedText) {
		OCRDialogListener listener = new OCRDialogListener(ocrParsedText);
		AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(this);
		mAlertBuilder.setMessage("Would you like to use the title, '" + ocrParsedText + 
				"' that we detected from your receipt?").
				setNeutralButton("Cancel", listener).
				setNegativeButton("No, thanks", listener).
				setPositiveButton("Yes!", listener).create().show();
		
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receipt_list);

		mPrefs = getPreferences(Context.MODE_PRIVATE);
		mDb = new ReceiptDbAdapter(getApplicationContext());
		setListAdapter(new ReceiptListAdapter(getApplicationContext(), null));
		mDb.open();

		mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);

		prepareListData();

		// Set the adapter for the list view
		mDrawerList.setAdapter(new ExpandableListAdapter(this,
				navHeader, navChild));
		
		// Navigation Drawer clickListener for main items
		mDrawerList.setOnGroupClickListener(new OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				
				
				switch(groupPosition){
					case 3: // View budget
						Intent budgetIntent = new Intent(ReceiptList.this, BudgetViewActivity.class);
						startActivity(budgetIntent);
						break;
					case 4: // Export to Server
						Intent exportIntent = new Intent(ReceiptList.this, ExportService.class);
						startService(exportIntent);
						break;
					default:
						return false;
				}
				return true;
			}
		});

		// Navigation Drawer clickListener for subitems
		mDrawerList.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(final ExpandableListView parent, final View v,
					final int groupPosition, final int childPosition, final long id) {
				switch(groupPosition){
					case 0: // Sort By
						sort = navChild.get(navHeader.get(groupPosition)).get(childPosition);
	
						if(sort == "Date"){
							sort = "timestamp";
						} else if(sort == "Name"){
							sort = "title";
						}
						break;
						
					case 1: // Filter By Category
						filter = navChild.get(navHeader.get(groupPosition)).get(childPosition);
						break;
						
					case 2: // Toggle Business/Casual
						type = navChild.get(navHeader.get(groupPosition)).get(childPosition);
						break;
					
					default:
						throw new UnsupportedOperationException("Unsupported Navigation Drawer item selected");
				}
				new AsyncCursor().execute(sort, filter, type);
				return true;
			}
		});

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

	@Override
	protected void onDestroy(){
		super.onDestroy();
		mDb.close();
	}

	/*
	 * This method will fire an intent for the ReceiptDetailEditActivity that contains the
	 * database values for the given row.
	 * Each row has hidden fields that contain the database values for that row. These fields
	 * will be added to the intent before it is sent to minimize time spend querying the database.
	 */
	@Override
	protected void onListItemClick (final ListView l, final View row, final int position, final long id){
		final TextView label=(TextView)row.findViewById(R.id.label);
		final TextView cost=(TextView)row.findViewById(R.id.cost);
		final TextView category=(TextView)row.findViewById(R.id.category);
		final TextView type = (TextView)row.findViewById(R.id.type);
		final TextView date=(TextView)row.findViewById(R.id.date);
		final TextView filename=(TextView)row.findViewById(R.id.filename);
		final TextView key_id =(TextView)row.findViewById(R.id.key_id);

		final Intent i = new Intent(this, ReceiptDetailEditActivity.class);
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
	 * When the add_receipt button is pressed, an intent will be fired off to start
	 * the camera for a picture.
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item){
		switch(item.getItemId()){

		case R.id.receipt_add:

			if (Util.isIntentAvailable(MyApplication.getAppContext(), MediaStore.ACTION_IMAGE_CAPTURE)){
				final FileDatabaseController controller = new FileDatabaseController();
				final File outFile = controller.createImageFile();

				// This adds the new file created by the FileDatabaseController to the intent to
				// take a picture, letting the camera know where to save the image.
				final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));

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
	 * Store the most recently used temporary filename for use in updating
	 * our database when we receive a callback.
	 */
	@Override
	protected void onPause(){
		super.onPause();
		Log.i("ReceiptList","Storing tempFile as : " + tempFile);
		final SharedPreferences.Editor edit = mPrefs.edit();
		edit.putString("tempFile", tempFile);
		edit.commit();
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

	private void prepareListData() {
		navHeader = Arrays.asList(getResources().getStringArray(R.array.navDrawerHeader));
		navChild = new HashMap<String, List<String>>();

		// Adding child data
		final List<String> sort = Arrays.asList(getResources().getStringArray(R.array.navDrawerSort));
		final List<String> filter = Arrays.asList(getResources().getStringArray(R.array.navDrawerFilter));
		final List<String> business = Arrays.asList(getResources().getStringArray(R.array.navDrawerBusiness));

		navChild.put(navHeader.get(0), sort); // Header, Child data
		navChild.put(navHeader.get(1), filter);
		navChild.put(navHeader.get(2), business);
	}


	/*
	 * Helper function used to trigger an update to the UI thread's
	 * list of receipt entries.
	 */
	private void resetCursor(final Cursor c){
		((ReceiptListAdapter)getListAdapter()).changeCursor(c);
	}

}
