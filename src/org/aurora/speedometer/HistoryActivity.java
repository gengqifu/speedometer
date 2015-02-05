package org.aurora.speedometer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aurora.speedometer.data.DbAdapter;
import org.aurora.speedometer.data.Record;
import org.aurora.speedometer.data.Total;
import org.aurora.speedometer.utils.Log;
import org.aurora.speedometer.utils.Util;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.GestureDetector;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryActivity extends Activity implements 
	OnTouchListener, GestureDetector.OnGestureListener {
    private static final String TAG = "HistoryActivity";
    
    private static final int FLING_MIN_DISTANCE = 60;
    
    private GestureDetector mGestureDetector;
    
    private RelativeLayout mHistoryView;
    private ListView mHistoryListView;
    private TextView mTotalTimesView;
    private TextView mTotalTimeView;
    private TextView mTotalDistanceView;
    
    private DbAdapter mDbAdapter;
    
    private List<Map<String, String>> mHistoryListData = new ArrayList<Map<String,String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_history);
	
	mGestureDetector = new GestureDetector(this, this);
	
	mHistoryView = (RelativeLayout)findViewById(R.id.history_layout);
	mHistoryView.setOnTouchListener(this);
	mHistoryView.setLongClickable(true);
	
	mTotalTimesView = (TextView)findViewById(R.id.total_times);
	mTotalTimeView = (TextView)findViewById(R.id.total_time);
	mTotalDistanceView = (TextView)findViewById(R.id.total_distance);
	
	mDbAdapter = new DbAdapter(this);
	mDbAdapter.open();
	
	mHistoryListView = (ListView)findViewById(R.id.history_list);
	
	mHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> arg0,View arg1, int arg2, long arg3) {
		TextView endtimeView = (TextView)findViewById(R.id.text3);
		Log.d(TAG, "endtimeView - " + endtimeView.getText().toString());
		Intent intent = new Intent(HistoryActivity.this, HistoryDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean("showSaveButton", false);
		bundle.putString("endtime", endtimeView.getText().toString());
		intent.putExtras(bundle);
		startActivity(intent);
	    }
	});
    }
    
    @Override
    public void onResume() {
        super.onResume();
        //在当前的activity中注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Util.EXIT_ACTION);
        this.registerReceiver(this.broadcastReceiver, filter);
        
     // Show record summary list
     	showHistorySummary();
     	showHistoryList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.history, menu);
	return true;
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
	return mGestureDetector.onTouchEvent(event);
    }
    
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	if(e1.getX() - e2.getX() > FLING_MIN_DISTANCE) {
	    Intent intent = new Intent(HistoryActivity.this, SpeedometerActivity.class);
	    startActivity(intent);
	    overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
	    Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show();
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
	    unregisterReceiver(this);
	    Log.d(TAG, "onReceive - " + intent.getAction());
            finish();
        }
    };
    
    private void showHistoryList() {
	List<Record> records = new ArrayList<Record>();
	records = mDbAdapter.getRecordSummary();
	List<Map<String, String>> historyListData = new ArrayList<Map<String,String>>();
	for(int i=0; i<records.size(); i++) {
	    Record record = records.get(i);
	    Log.d(TAG, records.get(i).getRunningTime() + ", " + records.get(i).getDistance() + ", " + records.get(i).getEndTime());
	    Float distance = record.getDistance();
	    DecimalFormat df = new DecimalFormat("##0.00");
	    long runningTime = record.getRunningTime();
	    String runningTimeStr = Util.formatTime((int)runningTime); // May lost accuracy
	    Long endTime = record.getEndTime();
	    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	    String date = sdf.format(new Date(endTime));
	    String summary = getString(R.string.cycling_run) + df.format(distance) + getString(R.string.cycling_km) +
		    ", " + runningTimeStr;
	    Map<String, String> listItem = new HashMap<String, String>();
	    listItem.put("summary", summary);
	    listItem.put("date", date);
	    listItem.put("endtime", endTime.toString());
	    historyListData.add(listItem);
	}
	
	mHistoryListView.setAdapter(new SimpleAdapter(this,historyListData,R.layout.history_list_item,
		new String[]{"summary", "date", "endtime"},
		new int[]{R.id.text1,R.id.text2, R.id.text3}
	));
    }
    
    private void showHistorySummary() {
	Total total = new Total();
	
	total = mDbAdapter.getTotal();
	Log.d(TAG, "times " + total.getTimes());
	mTotalTimesView.setText("" + total.getTimes());
	mTotalTimeView.setText(Util.formatTime(total.getTime()));
	mTotalDistanceView.setText(Util.formatDistance(total.getDistance()));
    }
}
