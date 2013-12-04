package com.hci.prototype.mobilereceipts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
 * This class serves as a wrapper around our database. Each row represents
 * a receipt object that has been taken. If the amount is 0.0, our receipt
 * has not been updated, but an image file has been cached for it.
 */
public class ReceiptDbAdapter extends AbstractDbAdapter {
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_AMOUNT = "amount";
	public static final String KEY_FILENAME = "filename";
	public static final String KEY_CATEGORY = "category";
	public static final String KEY_TYPE = "type";
	public static final String KEY_TIME = "timestamp";
	public static final String DATABASE_TABLE = "receipts";

	public static final String DATE_FORMAT = "dd-MM-yyyy";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_NAME = "data";
	private static final String TAG = "ReceiptDbAdapter";
	private DatabaseHelper mDbHelper;

	private SQLiteDatabase mDb;



	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE =
			"create table "+DATABASE_TABLE+" (" + KEY_ROWID + " integer primary key autoincrement, "
					+ KEY_TITLE + " text not null, "+ KEY_AMOUNT + " text, " +
					KEY_FILENAME + " text not null, " + KEY_CATEGORY + " text, " + KEY_TYPE + " text, " +
					KEY_TIME + " text not null);";

	private final Context mCtx;

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public ReceiptDbAdapter(final Context ctx) {
		mCtx = ctx;
	}
	
	@Override
	public void close() {
		mDbHelper.close();
	}

	/**
	 * Create a new note using the title and body provided. If the note is
	 * successfully created return the new rowId for that note, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param title the title of the note
	 * @param body the body of the note
	 * @param time the receipt was taken
	 * @return rowId or -1 if failed
	 */
	public long createReceipt(final String title, final String filename) {
		final ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_FILENAME, filename);
		initialValues.put(KEY_AMOUNT, "0.0");
		initialValues.put(KEY_CATEGORY, "Payments");
		initialValues.put(KEY_TYPE, "Casual");

		final Date curDate = new Date();
		//DateFormat df = SimpleDateFormat.getDateInstance();
		final DateFormat df = new SimpleDateFormat(DATE_FORMAT);
		initialValues.put(KEY_TIME, df.format(curDate));


		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}


	/**
	 * Delete the note with the given rowId
	 * 
	 * @param rowId id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteReceipt(final long rowId) {

		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllNotes(final String... args) {
		String order = null;
		String filter = null;

		if(args[0] == "Amount"){
			order = "CAST(" + args[0] + " AS REAL)";
		} else{
			order = "LOWER(" + args[0] + ")";
		}

		if(args[1] == "No Filter" && args[2] == "All"){
			filter = "";
		} else if(args[1] != "No Filter" && args[2] == "All"){
			filter = KEY_CATEGORY + "='" + args[1] + "'";
		} else if(args[1] == "No Filter" && args[2] != "All"){
			filter = KEY_TYPE + "='" + args[2] + "'";
		} else {
			filter = KEY_CATEGORY + "='" + args[1] + "' AND " + KEY_TYPE + "='" + args[2] + "'";
		}

		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
				KEY_AMOUNT, KEY_FILENAME,KEY_CATEGORY, KEY_TYPE, KEY_TIME}, filter, null, null, null, order);
	}
	
	@Override
	public Cursor query(String[] projection, String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy, String limit){
		return mDb.query(DATABASE_TABLE, projection, selection, selectionArgs, groupBy, having, orderBy, limit);
	}
	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException if note could not be found/retrieved
	 */
	public Cursor fetchNote(final long rowId) throws SQLException {

		final Cursor mCursor =

				mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
						KEY_AMOUNT, KEY_FILENAME,KEY_CATEGORY, KEY_TYPE, KEY_TIME}, KEY_ROWID + "=" + rowId, null,
						null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	/**
	 * Open the receipts database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @throws SQLException if the database could be neither opened or created
	 */
	@Override
	public void open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
	}

	public Cursor sumCol() throws SQLException {
		final Cursor mCursor = mDb.rawQuery("SELECT * FROM receipts", null);

		return mCursor;
	}
	/**
	 * Update the receipt using the values provided. The note to be updated is
	 * specified using the rowId, and it is altered to use the title and body
	 * values passed in
	 * 
	 * @param rowId id of note to update
	 * @param title value to set note title to
	 * @param body value to set note body to
	 * @return true if the note was successfully updated, false otherwise
	 */
	public boolean updateReceipt(final long rowId, final ContentValues values) {
		return mDb.update(DATABASE_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
