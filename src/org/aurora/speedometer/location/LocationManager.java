/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aurora.speedometer.location;

import org.aurora.speedometer.utils.Log;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * A class that handles everything about location.
 */
public class LocationManager {
    private static final String TAG = "LocationManager";

    private Context mContext;
    private static Listener mListener;
    private android.location.LocationManager mLocationManager;
    private boolean mRecordLocation;

    LocationListener [] mLocationListeners = new LocationListener[] {
            new LocationListener(android.location.LocationManager.GPS_PROVIDER),
            new LocationListener(android.location.LocationManager.NETWORK_PROVIDER)
    };

    public interface Listener {
//        public void showGpsOnScreenIndicator(boolean hasSignal);
//        public void hideGpsOnScreenIndicator();
        public void onLocationInfoUpdated (Location location);
        public void onGeoLocationStatusChange();
   }

    public LocationManager(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
    }

    public Location getCurrentLocation() {
        if (!mRecordLocation) return null;

        // go in best to worst order
        for (int i = 0; i < mLocationListeners.length; i++) {
            Location l = mLocationListeners[i].current();
            if (l != null) return l;
        }
        Log.d(TAG, "No location received yet.");
        return null;
    }
    
    public Location getCurrentBestLocation() {
	if (!mRecordLocation) return null;
	
	Location currentLocation;
	if(LocationUtils.isBetterLocation(mLocationListeners[0].current(), mLocationListeners[1].current())) {
	    currentLocation = mLocationListeners[0].current();
	} else {
	    currentLocation = mLocationListeners[1].current();
	}
	
	if( currentLocation != null ) {
	    Log.d(TAG, "currentLocation 1 - " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
	} else {
	    Log.d(TAG, "currentLocation 1 is null");
	}
	
	if( currentLocation == null ) {
	    Location lastKnownLocation1 = mLocationManager.getLastKnownLocation(
		    android.location.LocationManager.GPS_PROVIDER);
	    Location lastKnownLocation2 = mLocationManager.getLastKnownLocation(
		    android.location.LocationManager.NETWORK_PROVIDER);
	    if(LocationUtils.isBetterLocation(lastKnownLocation1, lastKnownLocation2)) {
		currentLocation = lastKnownLocation1;
	    } else {
		currentLocation = lastKnownLocation2;
	    }
	}
	
	if( currentLocation == null ) {
	    Log.d(TAG, "currentLocation 2 is null");
	} else {
	    Log.d(TAG, "currentLocation 2 - " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
	}
	
	Log.d(TAG, "current time : " + System.currentTimeMillis());
	
	if( currentLocation != null ) {
	    Log.d(TAG, "location time : " + currentLocation.getTime());
	    Log.d(TAG, "detla time : " + (System.currentTimeMillis() - currentLocation.getTime()));
	    if( System.currentTimeMillis() - currentLocation.getTime() > LocationUtils.TWO_MINUTES ) {
        	currentLocation = null;
	    }
	}
	
	if( currentLocation == null ) {
	    Log.d(TAG, "currentLocation 3 is null");
	} else {
	    Log.d(TAG, "currentLocation 3 - " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
	}
	return currentLocation;
    }

    public void recordLocation(boolean recordLocation) {
        if (mRecordLocation != recordLocation) {
            mRecordLocation = recordLocation;
            if (recordLocation) {
                startReceivingLocationUpdates();
            } else {
                stopReceivingLocationUpdates();
            }
        }
    }

    private void startReceivingLocationUpdates() {
        Log.d(TAG, "enter startReceivingLocationUpdates");
        if (mLocationManager == null) {
            mLocationManager = (android.location.LocationManager)
                    mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        if (mLocationManager != null) {
            try {
                mLocationManager.requestLocationUpdates(
                        android.location.LocationManager.NETWORK_PROVIDER,
                        1000,
                        0F,
                        mLocationListeners[1]);
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }
            try {
                mLocationManager.requestLocationUpdates(
                        android.location.LocationManager.GPS_PROVIDER,
                        1000,
                        0F,
                        mLocationListeners[0]);
//                if (mListener != null) mListener.showGpsOnScreenIndicator(false);
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }
            Log.d(TAG, "startReceivingLocationUpdates");
        }
    }

    private void stopReceivingLocationUpdates() {
    	Log.d(TAG, "enter of stopReceivingLocationUpdates");
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
            Log.d(TAG, "stopReceivingLocationUpdates");
        }
//        if (mListener != null) mListener.hideGpsOnScreenIndicator();
    }

    private class LocationListener
            implements android.location.LocationListener {
        Location mLastLocation;
        boolean mValid = false;
        String mProvider;
        int mProviderStatus = LocationProvider.OUT_OF_SERVICE;

        static final int LOCATION_CACHE_TIME = 5*60*1000;

        public LocationListener(String provider) {
            mProvider = provider;
            mLastLocation = new Location(mProvider);
        }

        @Override
        public void onLocationChanged(Location newLocation) {
//        	Log.d(TAG, "enter of onLocationChanged");
            if (newLocation.getLatitude() == 0.0
                    && newLocation.getLongitude() == 0.0) {
                // Hack to filter out 0.0,0.0 locations
                return;
            }
            // If GPS is available before start camera, we won't get status
            // update so update GPS indicator when we receive data.
//            if (mListener != null && mRecordLocation &&
//                    android.location.LocationManager.GPS_PROVIDER.equals(mProvider)) {
////                mListener.showGpsOnScreenIndicator(true);
//            	mListener.onLocationInfoUpdated(newLocation);
//            }
            if (!mValid) {
                Log.d(TAG, "Got first location.");
            }
            mLastLocation.set(newLocation);
            mValid = true;
            mProviderStatus = LocationProvider.AVAILABLE;
            //Util.setLocationCachedTime(System.currentTimeMillis());
            if (mListener == null) Log.d(TAG, "mListener is NULL");
            if (mListener != null)
                mListener.onLocationInfoUpdated(newLocation);
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            mValid = false;
        }

        @Override
        public void onStatusChanged(
                String provider, int status, Bundle extras) {
            switch(status) {
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                    mProviderStatus = status;
                    // Keep location info for a few minutes after location provider changed to unavailable.
                   /* if ((System.currentTimeMillis() - Util.getLocationCachedTime()) > LOCATION_CACHE_TIME) {
                        mValid = false;
                    }*/
                    if (mListener != null && mRecordLocation &&
                            android.location.LocationManager.GPS_PROVIDER.equals(provider)) {
//                        mListener.showGpsOnScreenIndicator(false);
                    }
                    break;
                }
            }
        }

        public Location current() {
            // Give up recording cached location info after a few minutes.
            if (mProviderStatus != LocationProvider.AVAILABLE) {
               /* if ((System.currentTimeMillis() - Util.getLocationCachedTime()) > LOCATION_CACHE_TIME) {
                    mValid = false;
                }*/
            }
            return mValid ? mLastLocation : null;
        }
    }
    
    
    public static boolean checkGpsAndNetworkStatus(Context context) {
        Context staticContext = context;
            android.location.LocationManager locationManager = (android.location.LocationManager)
                    staticContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            boolean gpsOn = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
            boolean networkOn = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
            return gpsOn || networkOn;
        }
        return false;
    }
}
