/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.ActivityNotFoundException;

/**
 * Defines app-wide constants and utilities
 */
public final class LocationUtils {

    // Debugging tag for the application
    private static final String TAG = "LocationUtils";
    
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;
    
    public static final int TWO_MINUTES = 1000 * 60 * 2;
    
    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation
     The current Location fix, to which you want to compare the new one
    */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
	if (currentBestLocation == null) {
	    // A new location is always better than no location     
    	  	return true;
        } else if(location == null) {
    		return false;
        }

	// Check whether the new location fix is newer or older
	long timeDelta = location.getTime() - currentBestLocation.getTime();
	boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	boolean isNewer = timeDelta > 0;

	// If it's been more than two minutes since the current location, use the new location
	// because the user has likely moved
	if (isSignificantlyNewer) {
	    return true;
	    // If the new location is more than two minutes older, it must be worse
	} else if (isSignificantlyOlder) {
	    return false;
	}

	// Check whether the new location fix is more or less accurate
	int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	boolean isLessAccurate = accuracyDelta > 0;
	boolean isMoreAccurate = accuracyDelta < 0;
	boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	// Check if the old and new location are from the same provider
	boolean isFromSameProvider = isSameProvider(location.getProvider(),
              currentBestLocation.getProvider());

	// Determine location quality using a combination of timeliness and accuracy
	if (isMoreAccurate) {
	    return true;
	} else if (isNewer && !isLessAccurate) {
	    return true;
	} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	    return true;
	}
	return false;
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
	if (provider1 == null) {
	    return provider2 == null;
	}
        return provider1.equals(provider2);
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
