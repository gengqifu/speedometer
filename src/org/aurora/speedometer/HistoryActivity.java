package org.aurora.speedometer;

import org.aurora.speedometer.utils.Log;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.GestureDetector;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class HistoryActivity extends Activity implements 
	OnTouchListener, GestureDetector.OnGestureListener {
    private static final String TAG = "HistoryActivity";
    
    private static final int FLING_MIN_DISTANCE = 60;
    
    private GestureDetector mGestureDetector;
    
    private RelativeLayout mHistoryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_history);
	
	mGestureDetector = new GestureDetector(this, this);
	
	mHistoryView = (RelativeLayout)findViewById(R.id.history_layout);
	mHistoryView.setOnTouchListener(this);
	mHistoryView.setLongClickable(true);
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
}
