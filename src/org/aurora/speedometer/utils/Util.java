package org.aurora.speedometer.utils;

import java.text.DecimalFormat;

public class Util {
    private static final String TAG = "Util";
    
    public static String EXIT_ACTION = "org.aurora.speedometer.EXIT";
    
    static public String formatTime(int totalSeconds) {
	String result = "";

	int hour = totalSeconds / 3600;
	totalSeconds = totalSeconds % 3600;
	int minute = totalSeconds / 60;
	totalSeconds = totalSeconds % 60;
	int second = totalSeconds;
	
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
	
	Log.d(TAG, "result : " + result);
	return result;
    }
    
    public static String formatDistance(double distance) {
	DecimalFormat df = new DecimalFormat( "0.00");
	return df.format(distance);
    }
}