package org.aurora.speedometer;

import org.aurora.speedometer.utils.Log;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.BDNotifyListener;//假如用到位置提醒功能，需要import该类
//如果使用地理围栏功能，需要import如下类
import com.baidu.location.BDGeofence;
import com.baidu.location.BDLocationStatusCodes;
import com.baidu.location.GeofenceClient;
import com.baidu.location.GeofenceClient.OnAddBDGeofencesResultListener;
import com.baidu.location.GeofenceClient.OnGeofenceTriggerListener;
import com.baidu.location.GeofenceClient.OnRemoveBDGeofencesResultListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MapActivity extends Activity implements 
OnTouchListener, GestureDetector.OnGestureListener  {
    private static final String TAG = "MapActivity";
    
    private static final int FLING_MIN_DISTANCE = 30;
    
    private GestureDetector mGestureDetector;
    
    private RelativeLayout mMapLayout;
    private LinearLayout mMapLeftView;
    
    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient = null;
    private BDLocationListener myListener = new MyLocationListener();
    
    boolean isFirstLoc = true;// 是否首次定位

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	SDKInitializer.initialize(getApplicationContext());
	setContentView(R.layout.activity_map);
	
	mMapView = (MapView) findViewById(R.id.bmapView);
	
	mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
	mLocationClient.registerLocationListener( myListener );    //注册监听函数
	
	// 地图初始化
	mMapView = (MapView) findViewById(R.id.bmapView);
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
	
	mGestureDetector = new GestureDetector(this, this);
	
	mMapLayout = (RelativeLayout)findViewById(R.id.map_layout);
	mMapLeftView = (LinearLayout)findViewById(R.id.map_left);
	mMapLeftView.setOnTouchListener(this);
	mMapLeftView.setLongClickable(true);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.map, menu);
	return true;
    }*/
    
    @Override  
    protected void onDestroy() {  
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

	public void onReceivePoi(BDLocation poiLocation) {
	}
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
	return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	if(e2.getX() - e1.getX() > FLING_MIN_DISTANCE) {
	    Intent intent = new Intent(MapActivity.this, SpeedometerActivity.class);
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
}
