package com.hci.prototype.mobilereceipts;

import android.app.ListActivity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
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

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receipt_list);
		setListAdapter(new ReceiptListAdapter());
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.receipt_list, menu);
		return true;
	}

}
