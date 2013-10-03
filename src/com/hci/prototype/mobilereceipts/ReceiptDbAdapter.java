package com.hci.prototype.mobilereceipts;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.DateFormat;
import android.util.Log;

/*
 * This class serves as a wrapper around our database. Each row represents
 * a receipt object that has been taken. If the amount is 0.0, our receipt
 * has not been updated, but an image file has been cached for it.
 */
public class ReceiptDbAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_FILENAME = "filename";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_TIME = "timestamp";
    public static final String DATABASE_TABLE = "receipts";
    
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
        KEY_FILENAME + " text not null, " + KEY_CATEGORY + " text, " + 
        KEY_TIME + " text not null);";

    

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public ReceiptDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    /**
     * Open the receipts database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ReceiptDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

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
    public long createReceipt(String title, String filename) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_FILENAME, filename);
        initialValues.put(KEY_AMOUNT, "0.0");
        
        Date curDate = new Date();
        DateFormat df = SimpleDateFormat.getDateInstance();
        initialValues.put(KEY_TIME, df.format(curDate));
        

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteReceipt(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_AMOUNT, KEY_FILENAME,KEY_CATEGORY, KEY_TIME}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                    KEY_AMOUNT, KEY_FILENAME,KEY_CATEGORY, KEY_TIME}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
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
    public boolean updateReceipt(long rowId, ContentValues values) {
        return mDb.update(DATABASE_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
