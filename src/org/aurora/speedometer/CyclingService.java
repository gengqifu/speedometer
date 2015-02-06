package org.aurora.speedometer;

import java.text.DecimalFormat;

import org.aurora.speedometer.data.DbAdapter;
import org.aurora.speedometer.data.Record;
import org.aurora.speedometer.location.LocationManager;
import org.aurora.speedometer.utils.Log;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.location.GpsStatus;

public class CyclingService extends Service implements 
	LocationManager.Listener, GpsStatus.Listener {
    private final String TAG = "CyclingService";
    
    private final IBinder mBinder = new LocalBinder();
    
    private int mSecond = 0;
    private int mMinute = 0;
    private int mHour = 0;
    private int mRestSecond = 0;
    private int mRestMinute = 0;
    private int mRestHour = 0;
    
    private int mRunningSeconds = 0; // running time in seconds
    private int mRestSeconds = 0; // rest time in seconds
    
    private Location mPreviousLocation = null;
    private Location mCurrentLocation = null;
    private LocationManager mLocationManager;
    private float mSpeed = 0.0f;
    private double mDistance = 0.0d;
    private float mCurrentSpeed = 0.0f;
    private float mMaxSpeed = 0.0f;
    private float mAverageSpeed = 0.0f;
    
    private long mStartTime;
    private long mEndTime;

    private DbAdapter mDbAdapter;
    
    private boolean mIfStartRecord = false; // If tracing started
    private boolean mIfGpsReady = false; // If GPS ready
    private boolean mPaused = false; // If tracing paused
    
    private static SpeedometerActivity speedometerActivity;
    
    private Handler mHandler = new Handler();
    
    public class LocalBinder extends Binder {
	public CyclingService getService() {
	    return CyclingService.this;
	}
    }
    
    @Override  
    public IBinder onBind(Intent intent) {
	mLocationManager = new LocationManager(this, this);
	
	// Start to get locations from gps services
	mLocationManager.recordLocation(true);
	
	mDbAdapter = new DbAdapter(this);
	mDbAdapter.open();
	
	return mBinder;  
    }
    
    Runnable running = new Runnable() {
        @Override 
        public void run() {
            mSecond++;
            mRunningSeconds++;
            if( mSecond == 60 ) {
        	mMinute++;
        	mSecond = 0;
            }
            if( mMinute == 60 ) {
        	mHour++;
        	mMinute = 0;
            }
            
            Message msg = new Message();
            msg.what = Constants.UPDATE_RUN_TIME;
            SpeedometerActivity.mHandler.sendMessage(msg);
            mHandler.postDelayed(this, 1000); 
        }
    };
    
    Runnable rest = new Runnable() {
        @Override 
        public void run() {
            mRestSecond++;
            mRestSeconds++;
            if( mRestSecond == 60 ) {
        	mRestMinute++;
        	mRestSecond = 0;
            }
            if( mRestMinute == 60 ) {
        	mRestHour++;
        	mRestMinute = 0;
            }
            
            Message msg = new Message();
            msg.what = Constants.UPDATE_REST_TIME;
            SpeedometerActivity.mHandler.sendMessage(msg);
            mHandler.postDelayed(this, 1000); 
        }
    };
    
    @Override
    public void onLocationInfoUpdated (Location location) {
	if( !mIfGpsReady ) {
	    mIfGpsReady = true;
	    Message msg = new Message();
            msg.what = Constants.GPS_READY;
            SpeedometerActivity.mHandler.sendMessage(msg);
	    return;
	}
	
	// Have not start tracing or tracing paused
	if( !mIfStartRecord || mPaused ) {
	    return;
	}
	
	Log.d(TAG, "location - " + location.getLongitude() + ", " + location.getLatitude());
	
	if( mPreviousLocation == null ) {
	    mPreviousLocation = mCurrentLocation = location;
	    mCurrentSpeed = mMaxSpeed = mAverageSpeed = location.getSpeed() * 18/5;
	} else {
	    mCurrentLocation = location;
	    double distance = gps2m(mPreviousLocation.getLatitude(), mPreviousLocation.getLongitude(),
		    mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
	    mDistance += distance/1000;
	    Log.d(TAG, "formatDistance - " + formatDistance(mDistance));

	    Message msg = new Message();
	    msg.what = Constants.UPDATE_DISTANCE;
	    SpeedometerActivity.mHandler.sendMessage(msg);
	    
	    mPreviousLocation = mCurrentLocation;
	    mCurrentSpeed = location.getSpeed() * 18/5;
	    if( mCurrentSpeed > mMaxSpeed ) {
		mMaxSpeed = mCurrentSpeed;
	    }
	    mAverageSpeed = (float)mDistance / ( (float)mHour + (float)mMinute/60.0f + (float)mSecond/3600.0f );
	}
	
	Message msg = new Message();
        msg.what = Constants.UPDATE_CURRENT_SPEED;
        SpeedometerActivity.mHandler.sendMessage(msg);
        Message msg1 = new Message();
        msg1.what = Constants.UPDATE_MAX_SPEED;
        SpeedometerActivity.mHandler.sendMessage(msg1);
        Message msg2 = new Message();
        msg2.what = Constants.UPDATE_AVERAGE_SPEED;
        SpeedometerActivity.mHandler.sendMessage(msg2);
    }
    
    @Override
    public void onGeoLocationStatusChange() {
	
    }
    
    private final double EARTH_RADIUS = 6378137.0;  
    private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
           double radLat1 = (lat_a * Math.PI / 180.0);
           double radLat2 = (lat_b * Math.PI / 180.0);
           double a = radLat1 - radLat2;
           double b = (lng_a - lng_b) * Math.PI / 180.0;
           double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                  + Math.cos(radLat1) * Math.cos(radLat2)
                  * Math.pow(Math.sin(b / 2), 2)));
           s = s * EARTH_RADIUS;
           s = Math.round(s * 10000) / 10000;
           return s;
    }
    
    private String formatDistance(double distance) {
	DecimalFormat df = new DecimalFormat( "0.00");
	return df.format(distance);
    }
    
    public void startRecord() {
	mIfStartRecord = !mIfStartRecord;
	if( mIfStartRecord ) {
	    mStartTime = System.currentTimeMillis();
	    mHandler.postDelayed(running, 1000);
	    mHandler.removeCallbacks(rest);
	} else {
	    mHandler.removeCallbacks(running);
	    mHandler.postDelayed(rest, 1000);
	}
	
	mLocationManager.recordLocation(mIfStartRecord);
    }
    
    public void pauseRecord() {
	mPaused = !mPaused;
	if( mPaused ) {
	    mHandler.removeCallbacks(running);
	    mHandler.postDelayed(rest, 1000);
	} else {
	    mHandler.removeCallbacks(rest);
	    mHandler.postDelayed(running, 1000);
	}
    }
    
    public void stopRecord() {
	mIfStartRecord = false;
	mHandler.removeCallbacks(running);
	mHandler.removeCallbacks(rest);

	Record record = new Record();
	record.setStartTime(mStartTime);
	mEndTime = System.currentTimeMillis();
	record.setEndTime(mEndTime);
	record.setDistance((float)mDistance);
	record.setRunningTime(mRunningSeconds);
	record.setRestTime(mRestSeconds);
	record.setMaxSpeed(mMaxSpeed);
	record.setAverageSpeed(mAverageSpeed);
	long newRowId = mDbAdapter.insertRecord(record);
	Log.d(TAG, "insert record newRowId " + newRowId);
	
	mHandler.removeCallbacks(running);
	mHandler.removeCallbacks(rest);
	initialize();
	
	Log.d(TAG, "insert total, new RowId " + newRowId);
    }
    
    public void initialize() {
	mSecond = 0;
	mMinute = 0;
	mHour = 0;
	mRestSecond = 0;
	mRestMinute = 0;
	mRestHour = 0;
	
	mRunningSeconds = 0;
	mRestSeconds = 0;
	
	mSpeed = 0.0f;
	mDistance = 0.0d;
	mCurrentSpeed = 0.0f;
	mMaxSpeed = 0.0f;
	mAverageSpeed = 0.0f;
	mIfStartRecord = false;
	mIfGpsReady = false;
	mPaused = false;
    }
    
    // implement this method from interface GpsStatus.Listener
    public void onGpsStatusChanged(int event) {
	switch(event) {
		case GpsStatus.GPS_EVENT_FIRST_FIX:
		    Log.d(TAG, "GPS_EVENT_FIRST_FIX");
		case GpsStatus.GPS_EVENT_STARTED:
		    Log.d(TAG, "GPS_EVENT_STARTED");
	}
    }
    
    public boolean getIfStartRecord() {
	return mIfStartRecord;
    }
    
    public void setIfStartRecord(boolean ifStart) {
	mIfStartRecord = ifStart;
    }
    
    public boolean getPaused() {
	return mPaused;
    }
    
    public long getEndtime() {
	return mEndTime;
    }
    
    public int getRunningSeconds() {
	return mRunningSeconds;
    }
    
    public int getRestSeconds() {
	return mRestSeconds;
    }
    
    public float getCurrentSpeed() {
	return mCurrentSpeed;
    }
    
    public float getMaxSpeed() {
	return mMaxSpeed;
    }
    
    public float getAverageSpeed() {
	return mAverageSpeed;
    }
    
    public double getDistance() {
	return mDistance;
    }
    
    public long getStarttime() {
	return mStartTime;
    }
}
