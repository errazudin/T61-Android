package com.izambasiron.free.t61radio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BatteryDataDbAdapter {
	public static final String KEY_DATETIME = "datetime";
	public static final String KEY_BATTERY = "battery";
    public static final String KEY_DATA = "data";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "BatteryDataDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table batteryData (_id integer primary key autoincrement, "
        + "datetime NUMERIC not null, battery integer, data NUMERIC);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "batteryData";
    private static final int DATABASE_VERSION = 1;

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
            db.execSQL("DROP TABLE IF EXISTS batteryData");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public BatteryDataDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * 
     */
    public BatteryDataDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * 
     */
    public long insert(int battery, long data, long date) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_BATTERY, battery);
        initialValues.put(KEY_DATA, data);
        initialValues.put(KEY_DATETIME, date);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * 
     */
    public boolean delete(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * 
     */
    public Cursor fetchAll() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_BATTERY,
                KEY_DATA, KEY_DATETIME}, null, null, null, null, null);
    }

    /**
     * 
     */
    public Cursor fetch(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
            		KEY_BATTERY, KEY_DATA, KEY_DATETIME}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     *  
     */
    public boolean update(long rowId, int battery, long data, long date) {
        ContentValues args = new ContentValues();
        args.put(KEY_BATTERY, battery);
        args.put(KEY_DATA, data);
        args.put(KEY_DATETIME, date);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
