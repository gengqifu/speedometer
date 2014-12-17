package org.aurora.speedometer;

import java.text.DecimalFormat;

import android.gesture.GestureOverlayView.OnGestureListener;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.location.GpsStatus;

public class SpeedometerActivity extends Activity implements 
	LocationManager.Listener, GpsStatus.Listener, OnTouchListener, 
	GestureDetector.OnGestureListener {
    
    private static final String TAG = "SpeedometerActivity";
    
    private static final int FLING_MIN_DISTANCE = 60;
    private static final int FLING_MIN_VELOCITY = 120;
    
    private int mSecond = 0;
    private int mMinute = 0;
    private int mHour = 0;
    private int mRestSecond = 0;
    private int mRestMinute = 0;
    private int mRestHour = 0;
    private TextView mDurationView;
    private RingView mRingView;
    private TextView mDistanceView;
    private TextView mCurrentSpeedView;
    private TextView mMaxSpeedView;
    private TextView mAverageSpeedView;
    private TextView mRestView;
    private Button mStartButton;
    private LinearLayout mPauseAndStopView;
    private Button mPauseButton;
    private Button mStopButton;
    private ImageView mGpsCircleView;
    private ImageView mGpsLoadingView;

    private Animation mGpsLoadingAnim;
    
    private Location mPreviousLocation = null;
    private Location mCurrentLocation = null;
    private LocationManager mLocationManager;
    private float mSpeed = 0.0f;
    private double mDistance = 0.0d;
    private float mCurrentSpeed = 0.0f;
    private float mMaxSpeed = 0.0f;
    private float mAverageSpeed = 0.0f;
    
    private GestureDetector mGestureDetector;
    
    private boolean mIfStartRecord = false; // If tracing started
    private boolean mIfGpsReady = false; // If GPS ready
    private boolean mPaused = false; // If tracing paused
    
    private Handler mHandler = new Handler();
    
    class myRunnable implements Runnable {
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
    
    }
    
    //myRunnable running = new myRunnable();
    //myRunnable rest = new myRunnable();
    
    Runnable running = new Runnable() {
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
    
    Runnable rest = new Runnable() {
        @Override 
        public void run() {
            mRestSecond++; 
            if( mRestSecond == 60 ) {
        	mRestMinute++;
        	mRestSecond = 0;
            }
            if( mRestMinute == 60 ) {
        	mRestHour++;
        	mRestMinute = 0;
            }
            
            mRestView.setText(formatTime(mRestHour, mRestMinute, mRestSecond)); 
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
	mRestView = (TextView)findViewById(R.id.rest_time);
	mPauseAndStopView = (LinearLayout)findViewById(R.id.pause_and_stop);
	mPauseButton = (Button)findViewById(R.id.button_pause);
	mStopButton = (Button)findViewById(R.id.button_stop);
	mGpsCircleView = (ImageView)findViewById(R.id.gps_circle);
	mGpsLoadingView = (ImageView)findViewById(R.id.gps_circle_loading);
	
	mStartButton.setClickable(false);
	//mHandler.postDelayed(runnable, 1000);
	
	mLocationManager = new LocationManager(this, this);
	
	// Start to get locations from gps services
	mLocationManager.recordLocation(true);
	
	mGestureDetector = new GestureDetector(this, this);
	LinearLayout speedometer = (LinearLayout)findViewById(R.id.speedometer_layout);
	speedometer.setOnTouchListener(this);
	speedometer.setLongClickable(true);

	// Gps loading animation
	mGpsLoadingAnim = AnimationUtils.loadAnimation(this, R.anim.gps_loading);
	LinearInterpolator lin = new LinearInterpolator();
	mGpsLoadingAnim.setInterpolator(lin);

	// Start gps loading animation
	if (mGpsLoadingAnim != null) {
	    mGpsLoadingView.startAnimation(mGpsLoadingAnim);
	}
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
	    mGpsLoadingView.clearAnimation();
	    mGpsLoadingView.setVisibility(View.GONE);
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
	    mDistanceView.setText(formatDistance(mDistance));
	    mPreviousLocation = mCurrentLocation;
	    mCurrentSpeed = location.getSpeed() * 18/5;
	    if( mCurrentSpeed > mMaxSpeed ) {
		mMaxSpeed = mCurrentSpeed;
	    }
	    mAverageSpeed = (float)mDistance / ( (float)mHour + (float)mMinute/60.0f + (float)mSecond/3600.0f );
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
	    //mStartButton.setText(R.string.button_stop);
	    mStartButton.setVisibility(View.INVISIBLE);
	    mPauseAndStopView.setVisibility(View.VISIBLE);
	    mHandler.postDelayed(running, 1000);
	    mHandler.removeCallbacks(rest);
	} else {
	    //mStartButton.setText(R.string.button_start);
	    mStartButton.setVisibility(View.VISIBLE);
	    mPauseAndStopView.setVisibility(View.INVISIBLE);
	    mHandler.removeCallbacks(running);
	    mHandler.postDelayed(rest, 1000);
	}
	
	mLocationManager.recordLocation(mIfStartRecord);
    }
    
    public void pauseRecord(View view) {
	mPaused = !mPaused;
	if( mPaused ) {
	    mPauseButton.setText(R.string.button_continue);
	    mHandler.removeCallbacks(running);
	    mHandler.postDelayed(rest, 1000);
	} else {
	    mPauseButton.setText(R.string.button_pause);
	    mHandler.removeCallbacks(rest);
	    mHandler.postDelayed(running, 1000);
	}
    }
    
    public void stopRecord(View viw) {
	mIfStartRecord = false;
	mHandler.removeCallbacks(running);
	mHandler.removeCallbacks(rest);
	initialize();
	resetUI();
    }
    
    public void initialize() {
	mSecond = 0;
	mMinute = 0;
	mHour = 0;
	mRestSecond = 0;
	mRestMinute = 0;
	mRestHour = 0;
	mSpeed = 0.0f;
	mDistance = 0.0d;
	mCurrentSpeed = 0.0f;
	mMaxSpeed = 0.0f;
	mAverageSpeed = 0.0f;
	mIfStartRecord = false;
	mIfGpsReady = false;
	mPaused = false;
    }
    
    private void resetUI() {
	mStartButton.setVisibility(View.VISIBLE);
	mPauseAndStopView.setVisibility(View.INVISIBLE);
	mCurrentSpeedView.setText(formatDistance(mCurrentSpeed));
	mMaxSpeedView.setText(formatDistance(mMaxSpeed));
	mAverageSpeedView.setText(formatDistance(mAverageSpeed));
	mDurationView.setText(formatTime(mHour, mMinute, mSecond)); 
	mRestView.setText(formatTime(mRestHour, mRestMinute, mRestSecond));
	mDistanceView.setText(formatDistance(mDistance));
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
    
    public String formatTime(int hour, int minute, int second) {
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
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
	return mGestureDetector.onTouchEvent(event);
    }
    
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	if(e1.getX() - e2.getX() > FLING_MIN_DISTANCE) {
	    Intent intent = new Intent(SpeedometerActivity.this,MapActivity.class);
	    startActivity(intent);
	    Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show();
	} else if (e2.getX()-e1.getX() > FLING_MIN_DISTANCE) {
	    Intent intent = new Intent(SpeedometerActivity.this, MapActivity.class);
	    startActivity(intent);
	    Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show();
	}
	
	return true;
    }
    
    @Override  
    public boolean onDown(MotionEvent e) {
        return false;
    }
  
    @Override  
    public void onLongPress(MotionEvent e) {  
    }  
  
    @Override  
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {  
        return false;  
    }  
  
    @Override  
    public void onShowPress(MotionEvent e) {  
    }  
  
    @Override  
    public boolean onSingleTapUp(MotionEvent e) {  
        return false;  
    }
}
