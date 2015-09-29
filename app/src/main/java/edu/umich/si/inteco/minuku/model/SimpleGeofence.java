package edu.umich.si.inteco.minuku.model;

import android.util.Log;

import com.google.android.gms.location.Geofence;

public class SimpleGeofence {

	/** Tag for logging. */
    private static final String LOG_TAG = "SimpleGeofence Class";
    private final String mId;
    private final double mLatitude;
    private final double mLongitude;
    private final float mRadius;
    private long mExpirationDuration;
    // the delay between GEOFENCE_TRANSITION_ENTER and GEOFENCE_TRANSITION_DWELLING in milliseconds.
    //This value is ignored if the transition types don't include a GEOFENCE_TRANSITION_DWELL filter.
    private int mLoiteringDelay; 
    private int mTransitionType;
    
	public SimpleGeofence(String geofenceId, double latitude, double longitude, float radius, long expiration,int transition){
	
	    // An identifier for the geofence
	    this.mId = geofenceId;	
	    this.mLatitude = latitude;
	    this.mLongitude = longitude;	
	    // Radius in meters
	    this.mRadius = radius;	
	    // Expiration time in milliseconds
	    this.mExpirationDuration = expiration;	
	    // Transition type
	    this.mTransitionType = transition;
	    this.mLoiteringDelay = 3000;//set the default to 5 minute
	}
	
    public String getId() {
        return mId;
    }


    public double getLatitude() {
        return mLatitude;
    }

    public int getLoiteringDelay() {
    	return mLoiteringDelay;
    }
    
    public void setLoiteringDelay(int delay){
    	mLoiteringDelay = delay;
    }

    public double getLongitude() {
        return mLongitude;
    }

    //in meters
    public float getRadius() {
        return mRadius;
    }

    //in milliseconds
    public long getExpirationDuration() {
        return mExpirationDuration;
    }


    public int getTransitionType() {
        return mTransitionType;
    }


    public Geofence toGeofence() {
    	
    	Log.d(LOG_TAG, "[toGeofence] converting the SimpleGeofence to geofence");
    	
        // Build a new Geofence object
        return new Geofence.Builder()
                       .setRequestId(getId())
                       .setTransitionTypes(mTransitionType)
                       .setCircularRegion(
                               getLatitude(),
                               getLongitude(),
                               getRadius())
                       .setExpirationDuration(mExpirationDuration)
                       .setLoiteringDelay(30000)
                       .build();
    }

	
}
