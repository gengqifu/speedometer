package org.aurora.speedometer;

import java.text.DecimalFormat;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.location.GpsStatus;

public class SpeedometerActivity extends Activity implements 
	LocationManager.Listener, GpsStatus.Listener {
    
    private static final String TAG = "SpeedometerActivity";
    
    private int mSecond = 0;
    private int mMinute = 0;
    private int mHour = 0;
    private TextView mDurationView;
    private RingView mRingView;
    private TextView mDistanceView;
    private TextView mCurrentSpeedView;
    private TextView mMaxSpeedView;
    private TextView mAverageSpeedView;
    private Button mStartButton;
    
    private Location mPreviousLocation = null;
    private Location mCurrentLocation = null;
    private LocationManager mLocationManager;
    private float mSpeed = 0.0f;
    private double mDistance = 0.0d;
    private float mCurrentSpeed = 0.0f;
    private float mMaxSpeed = 0.0f;
    private float mAverageSpeed = 0.0f;
    
    private boolean mIfStartRecord = false;
    private boolean mIfGpsReady = false;
    
    private Handler mHandler = new Handler();
    
    Runnable runnable = new Runnable() {
        @Override 
        public void run() {
            mSecond++; 
            if( mSecond == 60 ) {
        	mMinute++;
        	mSecond = 0;
            }
            if( mMinute == 60 ) {
        	mHour++;
        	mMinute = 0;
            }
            
            mDurationView.setText(formatTime(mHour, mMinute, mSecond)); 
            mHandler.postDelayed(this, 1000); 
        }
        
        private String formatTime(int hour, int minute, int second) {
            String result = "";
            
            if( hour < 10 ) {
        	result += "0" + hour + ":";
            } else {
        	result += hour + ":";
            }
            
            if( minute < 10 ) {
        	result += "0" + minute + ":";
            } else {
        	result += minute + ":";
            }
            
            if( second < 10 ) {
        	result += "0" + second;
            } else {
        	result += second;
            }
            
            return result;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_speedometer);
	
	mRingView = (RingView)findViewById(R.id.ring);
	mDistanceView = (TextView)findViewById(R.id.distance);
	mDurationView = (TextView)findViewById(R.id.duration);
	mCurrentSpeedView = (TextView)findViewById(R.id.current_speed);
	mMaxSpeedView = (TextView)findViewById(R.id.max_speed);
	mAverageSpeedView = (TextView)findViewById(R.id.average_speed);
	mStartButton = (Button)findViewById(R.id.button_start);
	
	mStartButton.setClickable(false);
	//mHandler.postDelayed(runnable, 1000);
	
	mLocationManager = new LocationManager(this, this);
	
	// Start to get locations from gps services
	mLocationManager.recordLocation(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.speedometer, menu);
	return true;
    }

    public void onLocationInfoUpdated (Location location) {
	if( !mIfGpsReady ) {
	    mIfGpsReady = true;
	    mStartButton.setClickable(true);
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
	    mDistanceView.setText(formatDistance(mDistance));
	    mPreviousLocation = mCurrentLocation;
	    mCurrentSpeed = location.getSpeed() * 18/5;
	    if( mCurrentSpeed > mMaxSpeed ) {
		mMaxSpeed = mCurrentSpeed;
	    }
	    mAverageSpeed = (float)mDistance / ( (float)mHour + mMinute/60.0f + mSecond/3600.0f );
	}
	
	mCurrentSpeedView.setText(formatDistance(mCurrentSpeed));
	mMaxSpeedView.setText(formatDistance(mMaxSpeed));
	mAverageSpeedView.setText(formatDistance(mAverageSpeed));
    }
    
    private String formatDistance(double distance) {
	DecimalFormat df = new DecimalFormat( "0.00");
	return df.format(distance);
    }
    
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
    
    public void startRecord(View view) {
	mIfStartRecord = !mIfStartRecord;
	if( mIfStartRecord ) {
	    mStartButton.setText(R.string.button_stop);
	    mHandler.postDelayed(runnable, 1000);
	} else {
	    mStartButton.setText(R.string.button_start);
	    mHandler.removeCallbacks(runnable);
	}
	
	mLocationManager.recordLocation(mIfStartRecord);
    }
    
    // implement this method from interface GpsStatus.Listener
    public void onGpsStatusChanged(int event) {
	switch(event) {
		case GpsStatus.GPS_EVENT_FIRST_FIX:
		    Log.d(TAG, "GPS_EVENT_FIRST_FIX");
		    mStartButton.setClickable(true);
		case GpsStatus.GPS_EVENT_STARTED:
		    Log.d(TAG, "GPS_EVENT_STARTED");
		    mStartButton.setClickable(true);
	}
    }
}
