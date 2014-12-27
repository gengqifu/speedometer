package org.aurora.speedometer.data;

public class Total {
    private static final String TAG = "Total";
    
    private float distance = 0.0f; // total distance
    private int time = 0; // total time in seconds
    private int times = 0; // number of cycling times
    
    public Total() {
	
    }
    
    public float getDistance() {
	return distance;
    }
    
    public int getTime() {
	return time;
    }
    
    public int getTimes() {
	return times;
    }
    
    public void setDistance(float distance) {
	this.distance = distance;
    }
    
    public void setTime(int time) {
	this.time = time;
    }
    
    public void setTimes(int times) {
	this.times = times;
    }
}
