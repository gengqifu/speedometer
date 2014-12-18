package org.aurora.speedometer.data;

public class Record {
    private static final String TAG = "Record";
    
    private long startTime = 0L;
    private long endTime =0L;
    private String runningTime = "";
    private String restTime = "";
    private float distance = 0.0f;
    private float maxSpeed = 0.0f;
    private float averageSpeed = 0.0f;
    
    public Record() {
	
    }
    
    public void setStartTime(long startTime) {
	this.startTime = startTime;
    }
    
    public void setEndTime(long endTime) {
	this.endTime = endTime;
    }
    
    public void setRunningTime(String runningTime) {
	this.runningTime = runningTime;
    }
    
    public void setRestTime(String restTime) {
	this.restTime = restTime;
    }
    
    public void setDistance(float distance) {
	this.distance = distance;
    }
    
    public void setMaxSpeed(float maxSpeed) {
	this.maxSpeed = maxSpeed;
    }
    
    public void setAverageSpeed(float averageSpeed) {
	this.averageSpeed = averageSpeed;
    }
    
    public long getStartTime() {
	return this.startTime;
    }
    
    public long getEndTime() {
	return this.endTime;
    }
    
    public String getRunningTime() {
	return this.runningTime;
    }
    
    public String getRestTime() {
	return this.restTime;
    }
    
    public float getDistance() {
	return this.distance;
    }
    
    public float getMaxSpeed() {
	return this.maxSpeed;
    }
    
    public float getAverageSpeed() {
	return this.averageSpeed;
    }
}