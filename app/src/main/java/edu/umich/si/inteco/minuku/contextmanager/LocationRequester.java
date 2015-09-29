package edu.umich.si.inteco.minuku.contextmanager;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import edu.umich.si.inteco.minuku.GlobalNames;
import edu.umich.si.inteco.minuku.services.CaptureProbeService;


public class LocationRequester implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{

	/** Tag for logging. */
    private static final String LOG_TAG = "LocationRequester";
	
	/**Location Requester**/
    private static LocationRequest mLocationRequest;
    
    private static Context mContext;
    
    private static LocationClient mLocationClient;

    PendingIntent mLocationPendingIntent;

    public static int LOCATION_UPDATE_FASTEST_INTERVAL_IN_SECONDS = 1 ;

    //the frequency of requesting location from the google play service
    public static int LOCATION_UPDATE_FAST_INTERVAL_IN_SECONDS = 2 ;

    //the frequency of requesting location from the google play service

    public static int LOCATION_UPDATE_SLOW_INTERVAL_IN_SECONDS = 60 ;


    //the accuracy of location update
    public static int LOCATION_UPDATE_LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;



    private static boolean mLocationUpdatesRequested;

    private static int mLocationUpdateInterval = LOCATION_UPDATE_FAST_INTERVAL_IN_SECONDS ;
    
    public LocationRequester(Context c){

    	mContext = c;
        
        mLocationClient = new LocationClient(mContext, this, this);

        mLocationUpdatesRequested = true;

    }

    /**
     * Start the activity recognition update request process by
     * getting a connection.
     */
    public void requestUpdates() {
        requestConnection();
    }

    public void removeUpdate() {
        Log.d(LOG_TAG,"[removeUpdates] going to remove location update ");
        getLocationClient().disconnect();
    }

    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    private void requestConnection() {
        getLocationClient().connect();
    }

    /**
     * Get a activity recognition client and disconnect from Location Services
     */
    private void requestDisconnection() {

        // Disconnect the client
        getLocationClient().disconnect();

    }

    /**
     * Send a request to remove location updates after the connection is built, s
     */
    private void continueRequestUpdates() {

        Log.d(LOG_TAG, "[test location update] the Google Play servce is connected, now start to request location");

        /**setup location update parameter**/
        // Create the LocationRequest object
        mLocationRequest = null;
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LOCATION_UPDATE_LOCATION_PRIORITY);
        // Set the update interval to
        Log.d(LOG_TAG,"[continueRequestUpdates][test location update] going to set location interval " + getLocationUpdateIntervalInMillis());


        mLocationRequest.setInterval(getLocationUpdateIntervalInMillis());
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL_IN_SECONDS*GlobalNames.MILLISECONDS_PER_SECOND);


        getLocationClient().requestLocationUpdates(
                mLocationRequest,
                this);

        Log.d(LOG_TAG,"[continueRequestUpdates][test location update] have requested activity update, with interval " + mLocationRequest.getInterval());


    }


    /**
     * Get the current activity recognition client, or create a new one if necessary.
     */
    public LocationClient getLocationClient() {

        //If a client doesn't already exist, create a new one
        if (mLocationClient == null) {
            // Create a new one
            setLocationClient(new LocationClient(mContext, this, this));
        }
        return mLocationClient;
    }


    public void setLocationClient(LocationClient locationClient) {
        mLocationClient = locationClient;
    }

	/**this function is where we got the updated location information**/
    @Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		 // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());

        Log.d(LOG_TAG, "in [onLocationChanged][testgetlocation] : onLocationChanged the lat lng is " + location.toString() );

        ContextExtractor.setLocation(location);

	}

	/**
	 * Google Activity Recognition Service*
	 * **/
	private boolean GooglePlayServiceConnected(){
		
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
	
		// If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
        	//Log.d("Location Updates","Google Play services is available.");
            return true;
        } else {      	
            //  Get the error code       
            return false;
        }
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		
        if (connectionResult.hasResolution()) {
        	
            try {
                connectionResult.startResolutionForResult( (Activity) mContext, CaptureProbeService.CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (SendIntentException e) {
            	Log.e(LOG_TAG,"[test location update] Google Play services canceled the original PendingIntent");
            	e.printStackTrace();
            }

        } else {
        	Log.e(LOG_TAG,"[test location update] No Google Play services is available");
        }

	}


	
	 /*
    * Called by Location Services when the request to connect the
    * client finishes successfully. At this point, you can
    * request the current location or start periodic updates
    */	
	@Override
	public void onConnected(Bundle dataBundle) {

        Log.d(LOG_TAG,"[test location update] the location servce is connected, going to request locaiton updates");
        continueRequestUpdates();

	}

	/*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
	@Override
	public void onDisconnected() {
		Log.d(LOG_TAG,"[test location update] the location servce is disconnected");
	}

    public int getLocationUpdateIntervalInMillis() {
        return mLocationUpdateInterval * GlobalNames.MILLISECONDS_PER_SECOND;
    }

    public void setLocationUpdateInterval(int updateInterval) {

        mLocationUpdateInterval = updateInterval;
        updateLocationClient();

    }

    private void updateLocationClient() {

        removeUpdate();
        mLocationClient = null;
        requestUpdates();

    }

}
