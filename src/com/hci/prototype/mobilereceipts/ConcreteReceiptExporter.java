package com.hci.prototype.mobilereceipts;

import java.nio.ByteBuffer;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/*
 * AbstractExporter subclass which is specialized for sending ReceiptDb contents
 * over the network to a server which keeps an updated copy of the database for
 * this particular app
 */
public class ConcreteReceiptExporter extends AbstractExporter {

	private Context mContext;
	
	// This integer is constant now, but can be changed to represent
	// a unique ID for this user's account.
	private int appNumber=1;
	
	public ConcreteReceiptExporter(Context ctxt) {
		super(ctxt);
		mContext = ctxt;
	}

	/*
	 * (non-Javadoc)
	 * @see com.hci.prototype.mobilereceipts.AbstractExporter#formatData()
	 * Formats the cursor data from the receipt database for network trasmission 
	 * and server backup.
	 */
	@Override
	protected byte[] formatData() {
		String[] mColumns = new String[]{ReceiptDbAdapter.KEY_ROWID, ReceiptDbAdapter.KEY_TITLE,
				ReceiptDbAdapter.KEY_AMOUNT, ReceiptDbAdapter.KEY_FILENAME, ReceiptDbAdapter.KEY_CATEGORY,
				ReceiptDbAdapter.KEY_TYPE, ReceiptDbAdapter.KEY_TIME};
		AbstractDbAdapter db = new ReceiptDbAdapter(mContext.getApplicationContext());
		
		db.open();
		Cursor mData = db.query(mColumns, null, null, null, null, null, null);
		
		String outputString = "" + appNumber;
		for (String str : mColumns)
			outputString += "\t" + str;
		outputString += "\n";
		
		mData.moveToFirst();
		while(!mData.isAfterLast()){
			
			for (int j=0; j < mData.getColumnCount(); ++j){
				outputString += "\t" + mData.getString(j);
			}
			outputString += "\n";
			mData.moveToNext();
		}
		
		Log.e("ConcreteReceiptExporter", outputString);
		db.close();
		
		byte[] byteOutput = outputString.getBytes();
		return ByteBuffer.allocate(2 + byteOutput.length).
				put((byte)appNumber).put((byte)1).put(byteOutput).array();
		
	}

}
