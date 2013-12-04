package com.hci.prototype.mobilereceipts;

import android.database.Cursor;

/*
 * Abstract class defining general interaction with a database.
 */
public abstract class AbstractDbAdapter {
	public abstract void open();
	
	public abstract void close();
	
	/*
	 * Formats a sql query for database lookups.
	 */
	public abstract Cursor query(String[] projection, String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy, String limit);

}
