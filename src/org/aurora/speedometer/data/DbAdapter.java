package org.aurora.speedometer.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import org.aurora.speedometer.utils.Log;

import com.baidu.location.BDLocation;

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
    
    public static final String COLUMN_NAME_TOTAL_DISTANCE = "distance";
    public static final String COLUMN_NAME_TOTAL_TIME = "time";
    public static final String COLUMN_NAME_TOTAL_TIMES = "times";
    
    public static final String COLUMN_NAME_ROUTE_LNG = "lng"; // longitude
    public static final String COLUMN_NAME_ROUTE_LAT = "lat"; // latitude
    public static final String COLUMN_NAME_ROUTE_STARTTIME = "starttime"; // activity starting time
    
    public static final String RECORD_TABLE = "record";
    public static final String TOTAL_TABLE = "total";
    public static final String ROUTE_TABLE = "route";
    
    private static final String TEXT_TYPE = " TEXT";
    private static final String FLOAT_TYPE = " FLOAT";
    private static final String INT_TYPE = " INTEGER";
    private static final String DOUBLE_TYPE = " DOUBLE";
    private static final String COMMA_SEP = ",";
    
    private static final String SQL_CREATE_RECORD_TABLE = 
	    "CREATE TABLE " + RECORD_TABLE + " (" + 
		    COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    COLUMN_NAME_STARTTIME + INT_TYPE + COMMA_SEP + 
		    COLUMN_NAME_ENDTIME + INT_TYPE + COMMA_SEP + 
		    COLUMN_NAME_RUNNINGTIME + INT_TYPE + COMMA_SEP +
		    COLUMN_NAME_RESTTIME + INT_TYPE + COMMA_SEP + 
		    COLUMN_NAME_DISTANCE + FLOAT_TYPE + COMMA_SEP + 
		    COLUMN_NAME_MAXSPEED + FLOAT_TYPE + COMMA_SEP + 
		    COLUMN_NAME_AVERAGESPEED + FLOAT_TYPE + 
		    ")";
    
    private static String SQL_CREATE_TOTAL_TABLE = 
	    "CREATE TABLE " + TOTAL_TABLE + " (" +
		    COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    COLUMN_NAME_TOTAL_DISTANCE + FLOAT_TYPE + COMMA_SEP +
		    COLUMN_NAME_TOTAL_TIME + INT_TYPE + COMMA_SEP +
		    COLUMN_NAME_TOTAL_TIMES + INT_TYPE +
		    ")";
    
    private static String SQL_CREATE_ROUTE_TABLE = 
	    "CREATE TABLE " + ROUTE_TABLE + " (" +
		    COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    COLUMN_NAME_ROUTE_LNG + DOUBLE_TYPE + COMMA_SEP +
		    COLUMN_NAME_ROUTE_LAT + DOUBLE_TYPE + COMMA_SEP +
		    COLUMN_NAME_ROUTE_STARTTIME + INT_TYPE +
		    ")";
	
    private static final String SQL_DELETE_RECORD_TABLE = 
	    "DROP TABLE IF EXIST " + RECORD_TABLE;
    
    private static final String SQL_DELETE_TOTAL_TABLE = 
	    "DROP TABLE IF EXIST " + TOTAL_TABLE;
    
    private static final String SQL_DELETE_ROUTE_TABLE = 
	    "DROP TABLE IF EXIST " + ROUTE_TABLE;
    
    // A list to store records summary info query from db
    String[] mRecordList ={
	    COLUMN_NAME_DISTANCE,
	    COLUMN_NAME_RUNNINGTIME,
	    COLUMN_NAME_ENDTIME
    };
    
    String[] mRecordFull = {
	    COLUMN_NAME_ID,
	    COLUMN_NAME_STARTTIME,
	    COLUMN_NAME_ENDTIME,
	    COLUMN_NAME_RUNNINGTIME,
	    COLUMN_NAME_RESTTIME,
	    COLUMN_NAME_DISTANCE,
	    COLUMN_NAME_MAXSPEED,
	    COLUMN_NAME_AVERAGESPEED
    };
    
    String mSortOrder = COLUMN_NAME_ID + " DESC";
    String mLimitOptions = "5";
    
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
    
    public List<Record> getRecordSummary() {
	List<Record> records = new ArrayList<Record>();
	
	Cursor cursor = mDb.query(true, RECORD_TABLE, mRecordList, null, null, null, null, mSortOrder, mLimitOptions);
	if( cursor.moveToFirst() ) {
	    do {
		Record record = new Record();
		record.setEndTime(Long.parseLong(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ENDTIME))));
		record.setDistance(cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_DISTANCE)));
		record.setRunningTime(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_RUNNINGTIME)));
		records.add(record);
		Log.d(TAG, record.getDistance() + ", " + record.getRunningTime()  + ", " + record.getEndTime());
	    } while( cursor.moveToNext() );
	}
	
	return records;
    }
    
    public Record getRecord(long endTime) {
	Record record = new Record();
	
	//Cursor cursor = mDb.query(true, RECORD_TABLE, mRecordFull, COLUMN_NAME_ENDTIME, String.valueOf(endTime), null, null, mSortOrder, mLimitOptions);
	Cursor cursor = mDb.rawQuery("SELECT * FROM record WHERE endtime=?", new String[]{Long.toString(endTime)});
	if( cursor.moveToFirst() ) {
    		record.setAverageSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_AVERAGESPEED)));
    		record.setDistance(cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_DISTANCE)));
    		record.setEndTime(Long.parseLong(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ENDTIME))));
    		record.setMaxSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_MAXSPEED)));
    		record.setRestTime(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_RESTTIME)));
    		record.setRunningTime(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_RUNNINGTIME)));
    		record.setStartTime(Long.parseLong(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_STARTTIME))));
	}
	
	return record;
    }
    
    public void delRecord(long endtime) {
	Log.d(TAG, "endtime - " + endtime);
	mDb.execSQL("DELETE FROM record WHERE endtime=" + endtime);
    }
    
    public Total getTotal() {
	Total total = new Total();
	
	Cursor cursor = mDb.rawQuery("SELECT * FROM total", null);
	if( cursor.moveToFirst() ) {
	    total.setDistance(cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_TOTAL_DISTANCE)));
	    total.setTime(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_TOTAL_TIME)));
	    total.setTimes(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_TOTAL_TIMES)));
	} else {
	    total.setDistance(0);
	    total.setTime(0);
	    total.setTimes(0);
	}
	
	return total;
    }
    
    public long insertTotal(Total total) {
	ContentValues values = new ContentValues();
	
	values.put(COLUMN_NAME_TOTAL_DISTANCE, total.getDistance());
	values.put(COLUMN_NAME_TOTAL_TIME, total.getTime());
	values.put(COLUMN_NAME_TOTAL_TIMES, total.getTimes());
	long newRowId;
	newRowId = mDb.insert(TOTAL_TABLE, null, values);
	return newRowId;
    }
    
    public long updateTotal(Total total) {
	ContentValues values = new ContentValues();
	
	values.put(COLUMN_NAME_TOTAL_DISTANCE, total.getDistance());
	values.put(COLUMN_NAME_TOTAL_TIME, total.getTime());
	values.put(COLUMN_NAME_TOTAL_TIMES, total.getTimes());
	long newRowId;
	newRowId = mDb.update(TOTAL_TABLE, values, null, null);
	return newRowId;
    }
    
    public long insertRoute(BDLocation location, long starttime) {
	ContentValues values = new ContentValues();
	
	values.put(COLUMN_NAME_ROUTE_STARTTIME, starttime);
	values.put(COLUMN_NAME_ROUTE_LNG, location.getLongitude());
	values.put(COLUMN_NAME_ROUTE_LAT, location.getLatitude());
	long newRowId;
	newRowId = mDb.insert(ROUTE_TABLE, null, values);
	return newRowId;
    }
    
    public List<BDLocation> getRoute(long starttime) {
	List<BDLocation> route = new ArrayList<BDLocation>();
	
	Cursor cursor = mDb.rawQuery("SELECT * FROM " + ROUTE_TABLE +
		" WHERE " + COLUMN_NAME_ROUTE_STARTTIME +
		"=" + starttime, null);
	
	if( cursor.moveToFirst() ) {
	    do {
		BDLocation location = new BDLocation();
		location.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_NAME_ROUTE_LNG)));
		location.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_NAME_ROUTE_LAT)));
		route.add(location);
		Log.d(TAG, "lng: " + location.getLongitude() + ", lat: " + location.getLatitude());
	    } while( cursor.moveToNext() );
	}
	
	return route;
    }
    
    public class DatabaseHelper extends SQLiteOpenHelper {
	
	public DatabaseHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	    Log.d(TAG, SQL_CREATE_RECORD_TABLE);
	    db.execSQL(SQL_CREATE_RECORD_TABLE);
	    db.execSQL(SQL_CREATE_TOTAL_TABLE);
	    db.execSQL(SQL_CREATE_ROUTE_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    db.execSQL(SQL_DELETE_RECORD_TABLE);
	    db.execSQL(SQL_DELETE_TOTAL_TABLE);
	    db.execSQL(SQL_DELETE_ROUTE_TABLE);
	    onCreate(db);
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    onUpgrade(db, oldVersion, newVersion);
	}
    }
}