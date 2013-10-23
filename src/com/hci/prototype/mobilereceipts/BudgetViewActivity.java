package com.hci.prototype.mobilereceipts;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;

public class BudgetViewActivity extends ListActivity {

	private ReceiptDbAdapter mDb;

	@Override
	protected void onCreate(final Bundle savedInstance){
		super.onCreate(savedInstance);
		setContentView(R.layout.budget_view_activity);

		//mPrefs = getPreferences(Context.MODE_PRIVATE);
		mDb = new ReceiptDbAdapter(getApplicationContext());
		mDb.open();

		final Cursor cursor = mDb.fetchAllNotes("Amount", "No Filter", "All");
		mDb.close();
	}


	@Override
	protected void onDestroy(){
		super.onDestroy();
		mDb.close();
	}
}