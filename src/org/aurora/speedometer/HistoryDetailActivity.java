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
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.nplatform.comapi.basestruct.GeoPoint;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryDetailActivity extends Activity {
    private static final String TAG = "HistoryDetailActivity";
    
    private Button mSaveButton;
    private TextView mTotalDistanceView;
    private TextView mAverageSpeedView;
    private TextView mMaxSpeedView;
    private TextView mCostTimeView;
    private TextView mRestTimeView;
    
    private long mEndtime;
    private long mStarttime;
    private Record mRecord;
    
    private DbAdapter mDbAdapter;
    
    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient = null;
    //private BDLocationListener myListener = new MyLocationListener();
    //private ImageView mImageView;
    boolean isFirstLoc = true;// 是否首次定位
    
    private UiSettings mUiSettings;
    
    private MyOnMapClickListener mOnMapClickListener = new MyOnMapClickListener();
    
    boolean mShowSaveButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	SDKInitializer.initialize(getApplicationContext());
	setContentView(R.layout.activity_history_detail);
	
	// 地图初始化
	mMapView = (MapView) findViewById(R.id.record_map);
	mBaiduMap = mMapView.getMap();
	/*// 开启定位图层
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
	mLocationClient.start();*/
	
	//mImageView = (ImageView) findViewById(R.id.record_map);
	
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
        mShowSaveButton = bundle.getBoolean("showSaveButton");
        if( mShowSaveButton ) {
            mSaveButton.setVisibility(View.VISIBLE);
        } else {
            mSaveButton.setVisibility(View.GONE);
        }
        
        mEndtime = Long.valueOf(bundle.getString("endtime"));
        Log.d(TAG, "mEndtime - " + mEndtime);
        getRecord(mEndtime);
        mStarttime = mRecord.getStartTime();
        Log.d(TAG, "mStarttime: " + mStarttime);
        
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
        
        mBaiduMap.setOnMapClickListener(mOnMapClickListener);
        
        mMapView.showZoomControls(false);
        mUiSettings = mBaiduMap.getUiSettings();
        
        mUiSettings.setZoomGesturesEnabled(false); // 禁用地图缩放
        mUiSettings.setScrollGesturesEnabled(false); // 禁用地图平移
        mUiSettings.setRotateGesturesEnabled(false); // 禁用地图旋转
        mUiSettings.setOverlookingGesturesEnabled(false); // 禁用地图俯视
        
        List<BDLocation> routes = mDbAdapter.getRoute(mStarttime);
        if( routes.size() > 0 ) {
            LatLng cenpt = new LatLng(routes.get(0).getLatitude(), routes.get(0).getLongitude());
            MapStatus mMapStatus = new MapStatus.Builder()
            	.target(cenpt)
            	.zoom(14)
            	.build();
            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            mBaiduMap.setMapStatus(mMapStatusUpdate);
            
            drawRoutes(routes);
        }
        
        
        
        /*mMapView.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
        	Log.d(TAG, "image view click");
        	Toast.makeText(HistoryDetailActivity.this, "image view click", Toast.LENGTH_SHORT).show();
        	Intent intent = new Intent(HistoryDetailActivity.this, MapActivity.class);
        	Bundle bundle = new Bundle();
        	bundle.putBoolean("history", true);
        	Log.d(TAG, "mStarttime: " + mStarttime);
        	bundle.putString("starttime", Long.toString(mStarttime));
        	intent.putExtras(bundle);
        	startActivity(intent);
            }
        });*/
    }
    
    @Override  
    protected void onPause() {  
        super.onPause();  
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理  
        //mMapView.onPause();  
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
	if( !mShowSaveButton ) {
	    Total total = mDbAdapter.getTotal();
	    total.setDistance(total.getDistance() - mRecord.getDistance());
	    total.setTime(total.getTime() - mRecord.getRunningTime());
	    total.setTimes(total.getTimes() - 1);
	    long newRowId = mDbAdapter.updateTotal(total);
	}
	mDbAdapter.delRoute(mStarttime);
	mDbAdapter.delRecord(mEndtime);
	
	if( mShowSaveButton ) {
	    Intent intent = new Intent(HistoryDetailActivity.this, SpeedometerActivity.class);
	    startActivity(intent);
	} else {
	    Intent intent = new Intent(HistoryDetailActivity.this, HistoryActivity.class);
	    startActivity(intent);
	}
    }
    
    public void shareRecord(View view) {
	
    }
    
    /**
     * 定位SDK监听函数
     */
    /*public class MyLocationListener implements BDLocationListener {
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
    }*/
    
    private void drawRoutes(List<BDLocation> routes) {
	for(int i=0; i<routes.size()-1; i++) {
	    LatLng p1 = new LatLng(routes.get(i).getLatitude(), routes.get(i).getLongitude());
	    LatLng p2 = new LatLng(routes.get(i+1).getLatitude(), routes.get(i+1).getLongitude());
	    List<LatLng> points = new ArrayList<LatLng>();
	    points.add(p1);
	    points.add(p2);
	    OverlayOptions ooPolyline = new PolylineOptions().width(10)
			.color(0xAAFF0000).points(points);
	    mBaiduMap.addOverlay(ooPolyline);
	}
    }
    
    private class MyOnMapClickListener implements BaiduMap.OnMapClickListener {
	@Override
	public void onMapClick(LatLng point) {
	    Log.d(TAG, "Map view click");
	    Toast.makeText(HistoryDetailActivity.this, "image view click", Toast.LENGTH_SHORT).show();
	    Intent intent = new Intent(HistoryDetailActivity.this, MapActivity.class);
	    Bundle bundle = new Bundle();
	    bundle.putBoolean("history", true);
	    Log.d(TAG, "mStarttime: " + mStarttime);
	    bundle.putString("starttime", Long.toString(mStarttime));
	    intent.putExtras(bundle);
	    startActivity(intent);
	}
	
	@Override
	public boolean onMapPoiClick(MapPoi poi) {
	    return true;
	}
    }
}
