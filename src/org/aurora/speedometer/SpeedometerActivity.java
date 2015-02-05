package org.aurora.speedometer;

import java.text.DecimalFormat;

import org.aurora.speedometer.CyclingService.LocalBinder;
import org.aurora.speedometer.data.DbAdapter;
import org.aurora.speedometer.ui.RingView;
import org.aurora.speedometer.utils.Log;
import org.aurora.speedometer.utils.Util;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.view.GestureDetector;
import android.view.KeyEvent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SpeedometerActivity extends Activity implements 
	OnTouchListener, GestureDetector.OnGestureListener {
    
    private static final String TAG = "SpeedometerActivity";
    
    private static final int FLING_MIN_DISTANCE = 60;
    private static final int FLING_MIN_VELOCITY = 120;
    
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
    private RelativeLayout mSpeedometerView;

    private Animation mGpsLoadingAnim;

    private DbAdapter mDbAdapter;

    private GestureDetector mGestureDetector;

    private CyclingService mService;    
    boolean mBound = false;
    
    public static Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	Log.d(TAG, "onCreate");
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
	
	mGestureDetector = new GestureDetector(this, this);
	mSpeedometerView = (RelativeLayout)findViewById(R.id.speedometer_layout);
	mSpeedometerView.setOnTouchListener(this);
	mSpeedometerView.setLongClickable(true);

	// Gps loading animation
	mGpsLoadingAnim = AnimationUtils.loadAnimation(this, R.anim.gps_loading);
	LinearInterpolator lin = new LinearInterpolator();
	mGpsLoadingAnim.setInterpolator(lin);

	// Start gps loading animation
	if (mGpsLoadingAnim != null) {
	    mGpsLoadingView.startAnimation(mGpsLoadingAnim);
	}

	mDbAdapter = new DbAdapter(this);
	mDbAdapter.open();
    }
    

    @Override
    public void onResume() {
	Log.d(TAG, "onResume");
        super.onResume();
        //在当前的activity中注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Util.EXIT_ACTION);
        this.registerReceiver(this.broadcastReceiver, filter);
    }
    
    @Override  
    protected void onStart() {
	Log.d(TAG, "onStart");
        super.onStart();  
        
        mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
		    switch(msg.what) {
		    case Constants.UPDATE_RUN_TIME:
			mDurationView.setText(Util.formatTime(mService.getRunningSeconds()));
			break;
		    case Constants.UPDATE_REST_TIME:
			mRestView.setText(Util.formatTime(mService.getRestSeconds()));
			break;
		    case Constants.UPDATE_CURRENT_SPEED:
			mCurrentSpeedView.setText(formatDistance(mService.getCurrentSpeed()));
			break;
		    case Constants.UPDATE_MAX_SPEED:
			mMaxSpeedView.setText(formatDistance(mService.getMaxSpeed()));
			break;
		    case Constants.UPDATE_AVERAGE_SPEED:
			mAverageSpeedView.setText(formatDistance(mService.getAverageSpeed()));
			break;
		    case Constants.UPDATE_DISTANCE:
			mDistanceView.setText(formatDistance(mService.getDistance()));
			break;
		    case Constants.GPS_READY:
			mStartButton.setClickable(true);
			mGpsLoadingView.clearAnimation();
			mGpsLoadingView.setVisibility(View.GONE);
			break;
		    }
		}
        };
    
        // 绑定Service
        Intent intent = new Intent(this, CyclingService.class);  
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);  
    }
    
    @Override  
    protected void onStop() {  
        super.onStop();
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {  	  
        @Override  
        public void onServiceConnected(ComponentName className,  
                IBinder service) {  
            // 已经绑定了LocalService，强转IBinder对象，调用方法得到LocalService对象  
            LocalBinder binder = (LocalBinder) service;  
            mService = binder.getService();  
            mBound = true;  
        }  
  
        @Override  
        public void onServiceDisconnected(ComponentName arg0) {  
            mBound = false;  
        }  
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.speedometer, menu);
	return true;
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
	mService.startRecord();
	if( mService.getIfStartRecord() ) {
	    mStartButton.setVisibility(View.INVISIBLE);
	    mPauseAndStopView.setVisibility(View.VISIBLE);
	} else {
	    mStartButton.setVisibility(View.VISIBLE);
	    mPauseAndStopView.setVisibility(View.INVISIBLE);
	}
    }
    
    public void pauseRecord(View view) {
	mService.pauseRecord();
	if( mService.getPaused() ) {
	    mPauseButton.setText(R.string.button_continue);
	} else {
	    mPauseButton.setText(R.string.button_pause);
	}
    }
    
    public void stopRecord(View view) {
	mService.stopRecord();
	resetUI();
	
	Intent intent = new Intent(SpeedometerActivity.this, HistoryDetailActivity.class);
	Bundle bundle = new Bundle();
	bundle.putBoolean("showSaveButton", true);
	bundle.putString("endtime", Long.toString(mService.getEndtime()));
	intent.putExtras(bundle);
	startActivity(intent);
    }
    
    private void resetUI() {
	mStartButton.setVisibility(View.VISIBLE);
	mPauseAndStopView.setVisibility(View.INVISIBLE);
	mCurrentSpeedView.setText(formatDistance(0.0f));
	mMaxSpeedView.setText(formatDistance(0.0f));
	mAverageSpeedView.setText(formatDistance(0.0f));
	mDurationView.setText(Util.formatTime(0)); 
	mRestView.setText(Util.formatTime(0));
	mDistanceView.setText(formatDistance(0d));
	mPauseButton.setText(R.string.button_pause);
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
	    overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
	    Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show();
	} else if (e2.getX()-e1.getX() > FLING_MIN_DISTANCE) {
	    Intent intent = new Intent(SpeedometerActivity.this, HistoryActivity.class);
	    startActivity(intent);
	    overridePendingTransition(R.anim.in_from_left, R.anim.out_from_right);
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

    @Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
	if ((keyCode == KeyEvent.KEYCODE_BACK)) { 
	    Log.d(TAG, "按下了back键 onKeyDown()");
	    Intent intent = new Intent();
	    intent.setAction(Util.EXIT_ACTION); // 退出动作
	    this.sendBroadcast(intent);// 发送广播
	    super.finish();
	    return true;
	}else { 
	    return super.onKeyDown(keyCode, event);
	}
    }
    
    //广播的内部类，当收到关闭事件时，调用finish方法结束activity
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
	@Override
        public void onReceive(Context context, Intent intent) {
	    Log.d(TAG, "onReceive - " + intent.getAction());
	    unregisterReceiver(this);
            finish();
        }
    };
}
