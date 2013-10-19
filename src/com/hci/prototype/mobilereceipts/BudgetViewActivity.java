package com.hci.prototype.mobilereceipts;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class BudgetViewActivity extends ListActivity {
	
	private ReceiptDbAdapter mDb;
	
	@Override
	protected void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);
		setContentView(R.layout.budget_view_activity);
		
		//mPrefs = getPreferences(Context.MODE_PRIVATE);
		mDb = new ReceiptDbAdapter(getApplicationContext());
		mDb.open();
		
		Cursor cursor = mDb.fetchAllNotes("Amount", "No Filter", "All");
		mDb.close();
	}


	@Override
	protected void onDestroy(){
		super.onDestroy();
		mDb.close();
	}
}	