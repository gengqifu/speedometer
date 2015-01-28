package org.aurora.speedometer;

import java.util.ArrayList;
import java.util.List;

import org.aurora.speedometer.MapActivity.MyLocationListener;
import org.aurora.speedometer.data.DbAdapter;
import org.aurora.speedometer.data.Record;
import org.aurora.speedometer.data.Total;
import org.aurora.speedometer.utils.Log;
import org.aurora.speedometer.utils.Util;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HistoryDetailActivity extends Activity {
    private static final String TAG = "HistoryDetailActivity";
    
    private Button mSaveButton;
    private TextView mTotalDistanceView;
    private TextView mAverageSpeedView;
    private TextView mMaxSpeedView;
    private TextView mCostTimeView;
    private TextView mRestTimeView;
    
    private long mEndtime;
    private Record mRecord;
    
    private DbAdapter mDbAdapter;
    
    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient = null;
    private BDLocationListener myListener = new MyLocationListener();
    boolean isFirstLoc = true;// 是否首次定位

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	SDKInitializer.initialize(getApplicationContext());
	setContentView(R.layout.activity_history_detail);
	
	mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
	mLocationClient.registerLocationListener( myListener );    //注册监听函数
	
	// 地图初始化
	mMapView = (MapView) findViewById(R.id.record_map);
	mBaiduMap = mMapView.getMap();
	// 开启定位图层
	mBaiduMap.setMyLocationEnabled(true);
	// 定位初始化
	mLocationClient = new LocationClient(this);
	mLocationClient.registerLocationListener(myListener);
	LocationClientOption option = new LocationClientOption();
	option.setOpenGps(true);// 打开gps
	option.setCoorType("bd09ll"); // 设置坐标类型
	option.setScanSpan(1000);
	option.setIsNeedAddress(true);
	mLocationClient.setLocOption(option);
	mLocationClient.start();
	
	mDbAdapter = new DbAdapter(this);
	mDbAdapter.open();
	
	mSaveButton = (Button)findViewById(R.id.button_save);
	mTotalDistanceView = (TextView)findViewById(R.id.total_distance);
	mAverageSpeedView = (TextView)findViewById(R.id.average_speed);
	mMaxSpeedView = (TextView)findViewById(R.id.max_speed);
	mCostTimeView = (TextView)findViewById(R.id.running_time);
	mRestTimeView = (TextView)findViewById(R.id.rest_time);
	
	Intent intent=getIntent();  
        Bundle bundle=intent.getExtras();
        boolean showSaveButton = bundle.getBoolean("showSaveButton");
        if( showSaveButton ) {
            mSaveButton.setVisibility(View.VISIBLE);
        } else {
            mSaveButton.setVisibility(View.GONE);
        }
        
        mEndtime = Long.valueOf(bundle.getString("endtime"));
        
        getRecord(mEndtime);
        
        showRecordDetail(mEndtime);
    }
    
    @Override
    protected void onStop() {
	Log.d(TAG, "onStop");
	super.onStop();
	finish();
    }
    
    @Override
    protected void onDestroy() {
	Log.d(TAG, "onDestroy");
	super.onDestroy();
	//在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理  
        mMapView.onDestroy();
    }
    
    @Override  
    protected void onResume() {  
        super.onResume();  
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理  
        mMapView.onResume();
    }
    
    @Override  
    protected void onPause() {  
        super.onPause();  
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理  
        mMapView.onPause();  
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.history_detail, menu);
	return true;
    }
    
    private Record getRecord(long endtime) {
	mRecord = mDbAdapter.getRecord(endtime);
	return mRecord;
    }
    private void showRecordDetail(long endTime) {
	//Record record = mDbAdapter.getRecord(endTime);
	
	mTotalDistanceView.setText(Util.formatDistance(mRecord.getDistance()));
	mAverageSpeedView.setText(Util.formatDistance(mRecord.getAverageSpeed()));
	mMaxSpeedView.setText(Util.formatDistance(mRecord.getMaxSpeed()));
	mCostTimeView.setText(Util.formatTime(mRecord.getRunningTime()));
	mRestTimeView.setText(Util.formatTime(mRecord.getRestTime()));
    }

    public void saveRecord(View view) {
	Total total = mDbAdapter.getTotal();
	total.setDistance(total.getDistance() + (float)mRecord.getDistance());
	total.setTime(total.getTime() + mRecord.getRunningTime());
	total.setTimes(total.getTimes()+1);
	long newRowId;
	if(total.getTimes() == 1 ) {
	    newRowId = mDbAdapter.insertTotal(total);
	} else {
	    newRowId = mDbAdapter.updateTotal(total);
	}
	
	Intent intent = new Intent(HistoryDetailActivity.this, SpeedometerActivity.class);
	startActivity(intent);
    }
    
    public void discardRecord(View view) {
	mDbAdapter.delRecord(mEndtime);
	
	Intent intent = new Intent(HistoryDetailActivity.this, SpeedometerActivity.class);
	startActivity(intent);
    }
    
    public void shareRecord(View view) {
	
    }
    
    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {
	@Override
	public void onReceiveLocation(BDLocation location) {
	    // map view 销毁后不在处理新接收的位置
	    if (location == null || mMapView == null)
		return;
	    MyLocationData locData = new MyLocationData.Builder()
	    	.accuracy(location.getRadius())
	    	// 此处设置开发者获取到的方向信息，顺时针0-360
	    	.direction(100).latitude(location.getLatitude())
	    	.longitude(location.getLongitude()).build();
	    mBaiduMap.setMyLocationData(locData);
	    if (isFirstLoc) {
		isFirstLoc = false;
		LatLng ll = new LatLng(location.getLatitude(),
			location.getLongitude());
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
	    }
	}
    }
}
