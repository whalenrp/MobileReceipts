package com.hci.prototype.mobilereceipts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.content.Context;
import android.database.Cursor;

public class JsonReceiptExporter extends AbstractExporter
{
	private Context mContext;

	public JsonReceiptExporter(Context ctxt)
	{
		super(ctxt);
		mContext = ctxt;
	}

	@Override
	protected byte[] formatData()
	{
		String[] mColumns = new String[]{ReceiptDbAdapter.KEY_ROWID, ReceiptDbAdapter.KEY_TITLE,
				ReceiptDbAdapter.KEY_AMOUNT, ReceiptDbAdapter.KEY_FILENAME, ReceiptDbAdapter.KEY_CATEGORY,
				ReceiptDbAdapter.KEY_TYPE, ReceiptDbAdapter.KEY_TIME};
		AbstractDbAdapter db = new ReceiptDbAdapter(mContext.getApplicationContext());
		
		db.open();
		Cursor mData = db.query(mColumns, null, null, null, null, null, null);
		
		// Build data structure for JSON marshalling
		Map<String, Object> json = new HashMap<String, Object>();
		
		// Add default user credentials for now
		json.put("username", "default");
		json.put("password", "password");
		
		// List we'll fill with receipt data
		List<Map<String, Object>> receiptList = new ArrayList<Map<String, Object>>(mData.getCount());
		json.put("receipts", receiptList);
		
		// Move all the receipt data into structures
		mData.moveToFirst();
		while(!mData.isAfterLast())
		{
			Map<String, Object> receipt = new HashMap<String, Object>(mData.getColumnCount());
			for (int i = 0; i < mData.getColumnCount(); i++)
			{
				receipt.put(mColumns[i], mData.getString(i));
			}
			receiptList.add(receipt);
			mData.moveToNext();
		}
		
		// Use magic to turn structure into json
		ObjectMapper om = new ObjectMapper();
		byte[] rtn = null;
		try
		{
			rtn = om.writeValueAsBytes(json);
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}
		
		return rtn;
	}

}
