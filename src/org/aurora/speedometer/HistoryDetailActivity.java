package org.aurora.speedometer;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_history_detail);
	
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
}
