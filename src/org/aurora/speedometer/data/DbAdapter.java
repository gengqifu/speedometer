package org.aurora.speedometer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.aurora.speedometer.utils.Log;

public class DbAdapter
{
    private final static String TAG = "DbAdapter";
    
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "speedometer.db";
    
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_STARTTIME = "starttime";
    public static final String COLUMN_NAME_ENDTIME = "endtime";
    public static final String COLUMN_NAME_RUNNINGTIME = "runningtime";
    public static final String COLUMN_NAME_RESTTIME = "resttime";
    public static final String COLUMN_NAME_DISTANCE = "distance";
    public static final String COLUMN_NAME_MAXSPEED = "maxspeed";
    public static final String COLUMN_NAME_AVERAGESPEED = "averagespeed";
    
    public static final String RECORD_TABLE = "record";
    
    private static final String TEXT_TYPE = " TEXT";
    private static final String FLOAT_TYPE = " FLOAT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    
    private static final String SQL_CREATE_RECORD_TABLE = 
	    "CREATE TABLE " + RECORD_TABLE + " (" + 
		    COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    COLUMN_NAME_STARTTIME + TEXT_TYPE + COMMA_SEP + 
		    COLUMN_NAME_ENDTIME + TEXT_TYPE + COMMA_SEP + 
		    COLUMN_NAME_RUNNINGTIME + TEXT_TYPE + COMMA_SEP +
		    COLUMN_NAME_RESTTIME + TEXT_TYPE + COMMA_SEP + 
		    COLUMN_NAME_DISTANCE + FLOAT_TYPE + COMMA_SEP + 
		    COLUMN_NAME_MAXSPEED + FLOAT_TYPE + COMMA_SEP + 
		    COLUMN_NAME_AVERAGESPEED + FLOAT_TYPE + 
		    ")";
    
    private static final String SQL_DELETE_RECORD_TABLE = 
	    "DROP TABLE IF EXIST " + RECORD_TABLE;
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mContext;
    
    /**
     * Constructor.
     * @param ctx The current context.
     */
    public DbAdapter(Context ctx) {
        this.mContext = ctx;
    }
    
    /**
     * Open the database helper.
     * @return The current database adapter.
     */
    public DbAdapter open() {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();

        return this;
    }
    
    /**
     * Close the database helper.
     */
    public void close() {
        mDbHelper.close();
    }
    
    public SQLiteDatabase getDatabase() {
	return mDb;
    }
    
    public long insertRecord(Record record) {
	ContentValues values = new ContentValues();
	values.put(COLUMN_NAME_STARTTIME, record.getStartTime());
	values.put(COLUMN_NAME_ENDTIME, record.getEndTime());
	values.put(COLUMN_NAME_RESTTIME, record.getRestTime());
	values.put(COLUMN_NAME_RUNNINGTIME, record.getRunningTime());
	values.put(COLUMN_NAME_DISTANCE, record.getDistance());
	values.put(COLUMN_NAME_MAXSPEED, record.getMaxSpeed());
	values.put(COLUMN_NAME_AVERAGESPEED, record.getAverageSpeed());
	
	long newRowId;
	newRowId = mDb.insert(RECORD_TABLE, null, values);
	return newRowId;
    }
    
    
    
    public class DatabaseHelper extends SQLiteOpenHelper {
	
	public DatabaseHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	    Log.d(TAG, SQL_CREATE_RECORD_TABLE);
	    db.execSQL(SQL_CREATE_RECORD_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    db.execSQL(SQL_DELETE_RECORD_TABLE);
	    onCreate(db);
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    onUpgrade(db, oldVersion, newVersion);
	}
    }
}