package org.aurora.speedometer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class HistoryDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_history_detail);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.history_detail, menu);
	return true;
    }

}
