package edu.umich.si.inteco.minuku.contextmanager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;
import java.util.List;

import edu.umich.si.inteco.minuku.util.GooglePlayServiceUtil;

public class GeofenceRequester implements OnAddGeofencesResultListener,
		ConnectionCallbacks, OnConnectionFailedListener {

	/** Tag for logging. */
    private static final String LOG_TAG = "GeofenceRequester";
    private final Context mContext;
    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Geofence> mCurrentGeofences;
    private LocationClient mLocationClient;

    ///Flag that indicates whether an add or remove request is underway. 
    private boolean mAddingGeofenceInProgress;
	
	public GeofenceRequester(Context context) {
        // Save the context
        mContext = context;
        mGeofencePendingIntent = null;
        mLocationClient = null;
        mAddingGeofenceInProgress = false;
    }
	 
    public void inProgressFlag(boolean flag) {
        mAddingGeofenceInProgress = flag;
    }

    public boolean isInProgress() {
        return mAddingGeofenceInProgress;
    }
    
    public PendingIntent getRequestPendingIntent() {
        return createRequestPendingIntent();
    }
    
    private GooglePlayServicesClient getLocationClient() {
        
    	if (mLocationClient == null) {

            mLocationClient = new LocationClient(mContext, this, this);
        }
        return mLocationClient;

    }
    
    private void requestConnection() {
        getLocationClient().connect();
    }
    
    private void requestDisconnection() {
        mAddingGeofenceInProgress = false;
        getLocationClient().disconnect();
    }
    
    private void continueAddGeofences() {


        mGeofencePendingIntent = createRequestPendingIntent();

        // Send a request to add the current geofences
        mLocationClient.addGeofences(mCurrentGeofences, mGeofencePendingIntent, this);
    }
    
    private PendingIntent createRequestPendingIntent() {

    	
        if (mGeofencePendingIntent !=null) {
            return mGeofencePendingIntent;
        } else {

            // Create an Intent pointing to the transition intent service
            Intent intent = new Intent(mContext, GeofenceTransitionService.class);

            return PendingIntent.getService(
                    mContext,
                    GooglePlayServiceUtil.GEOFENCE_TRANSITION_PENDING_INTENT_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    //whenever we add a geofence, we need to connect to the server and add geofence..
    public void addGeofences(List<Geofence> geofences) throws UnsupportedOperationException {

    	//save the geofences
        mCurrentGeofences = (ArrayList<Geofence>) geofences;

        // If a request is not already in progress
        if (!mAddingGeofenceInProgress) {

            // Toggle the flag and continue to request a connection to Location Service
            mAddingGeofenceInProgress = true;

            // Request a connection to Location Services
            requestConnection();

        // If a request is in progress
        } else {

            // Throw an exception and stop the request
            throw new UnsupportedOperationException();
        }
    }

    
    
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		 mAddingGeofenceInProgress = false;

		 if (connectionResult.hasResolution()) {	        	
	        	Log.d(LOG_TAG,"[onConnectionFailed] Conntection to Google Play services is failed");
	        	
	     } else {
	        	Log.e(LOG_TAG,"[onConnectionFailed] No Google Play services is available, the error code is "
	        			 + connectionResult.getErrorCode());
	     }
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.d(LOG_TAG,"[onConnected] Google Play services is connected for requesting the geofence update");
        
		// Continue the process of adding geofence
		continueAddGeofences();
	}

	@Override
	public void onDisconnected() {
		
        // Turn off the request flag
        mAddingGeofenceInProgress = false;

        // In debug mode, log the disconnection
        Log.d(LOG_TAG, "[onDisconnected] Google Play services is disconnected for requesting the geofence update");

        // Destroy the current location client
        mLocationClient = null;

	}
	

	/**
	 * Handle the result of adding the geofences
	 */
	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		
		
        // If adding the geocodes was successful
        if (statusCode==LocationStatusCodes.SUCCESS) {

            Log.d(LOG_TAG, "[onAddGeofencesResult] the following geofences are successfully added" + geofenceRequestIds + "the status code is " + statusCode);
            
        // If we failed to add the geofence
        } else {

        	Log.d(LOG_TAG, "[onAddGeofencesResult] the following geofences failed to be added" + geofenceRequestIds  + "the status code is " + statusCode);

        }

        // no matter what disconnect the location client
        requestDisconnection();
	}

}
