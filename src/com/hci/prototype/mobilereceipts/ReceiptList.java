package com.hci.prototype.mobilereceipts;

import java.io.File;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/*
 * This class will be the main entry point of the application. It will 
 * house a list of all color-coded transations as well as buttons in the
 * top corners to access the camera to log more receipts, and a side-menu
 * to give extended options
 */
public class ReceiptList extends ListActivity {
	
	private static final int ACTION_CAMERA_CAPTURE = 1337;
	private static final String[] labels = {"Groceries", "Bed Bath Beyond", "Magiannos", "Debit", "Credit",
		"Groceries", "Bed Bath Beyond", "Magiannos", "Debit", "Credit",
		"Groceries", "Bed Bath Beyond", "Magiannos", "Debit", "Credit",
		"Groceries", "Bed Bath Beyond", "Magiannos", "Debit", "Credit",
		"Groceries", "Bed Bath Beyond", "Magiannos", "Debit", "Credit"};
	private static final double[] items={1.0, -1.0, 2.0,-2.0, 3.0,
		1.0, -1.0, 2.0,-2.0, 3.0,
		1.0, -1.0, 2.0,-2.0, 3.0,
		1.0, -1.0, 2.0,-2.0, 3.0,
		1.0, -1.0, 2.0,-2.0, 3.0};

	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receipt_list);
		setListAdapter(new ReceiptListAdapter());
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
		if (resultCode != RESULT_CANCELED){
			if (requestCode == ACTION_CAMERA_CAPTURE){
				// Image has been successfully stored in filesystem.
			}
		}
	}
	
	/*
	 * This inner class will be responsible for updating individual list entries
	 * from XML files. It performs some list-optimization for better scrolling.
	 */
	class ReceiptListAdapter extends ArrayAdapter<String> {
		ReceiptListAdapter() {
			super(ReceiptList.this, R.layout.row, R.id.label, labels);
		}
		@Override
		public View getView(final int position, final View convertView,
				final ViewGroup parent) {
			final View row=super.getView(position, convertView, parent);
			final TextView size=(TextView)row.findViewById(R.id.label);
			final TextView cost=(TextView)row.findViewById(R.id.cost);
			
			Resources resources = getApplicationContext().getResources();
			if (items[position] < 0.0) {
				cost.setTextColor(resources.getColor(R.color.debit));
			}
			else {
				cost.setTextColor(resources.getColor(R.color.credit));
			}
			
			if (position % 2 == 0)
				row.setBackgroundColor(resources.getColor(R.color.dark_gray));
			else
				row.setBackgroundColor(resources.getColor(R.color.light_gray));
			
			size.setText(labels[position]);
			
			cost.setText(Util.formatPrice(items[position]));
			return(row);
		}
	}

}
