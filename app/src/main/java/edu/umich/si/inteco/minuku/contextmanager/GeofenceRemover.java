package edu.umich.si.inteco.minuku.contextmanager;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.util.GooglePlayServiceUtil;

public class GeofenceRemover implements ConnectionCallbacks,
		OnConnectionFailedListener, OnRemoveGeofencesResultListener {

	/** Tag for logging. */
    private static final String LOG_TAG = "GeofenceRemover";
    private Context mContext;
    // Stores the current list of geofences
    private ArrayList<String> mCurrentGeofenceIds;
    private LocationClient mLocationClient;
    // The PendingIntent sent in removeGeofencesByIntent
    private PendingIntent mRemoveGeofenceIntent;
    
    private GooglePlayServiceUtil.GEO_FENCE_REMOVE_TYPE mRequestType;
    
    private boolean mRemovingGeofenceInProgress;
    
    public GeofenceRemover (Context context) {
    	mContext = context;
        mCurrentGeofenceIds = null;
        mLocationClient = null;
        mRemovingGeofenceInProgress = false;
    }
    
    public void inProgressFlag(boolean flag) {
    	mRemovingGeofenceInProgress = flag;
    }

    public boolean isInProgress() {
        return mRemovingGeofenceInProgress;
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
    	
    	mRemovingGeofenceInProgress = false;
        getLocationClient().disconnect();
        
        if (mRequestType == GooglePlayServiceUtil.GEO_FENCE_REMOVE_TYPE.INTENT) {
            mRemoveGeofenceIntent.cancel();
        }
    }
    
    private void continueRemoveGeofences() {
        switch (mRequestType) {

            // If removeGeofencesByIntent was called
            case INTENT :
                mLocationClient.removeGeofences(mRemoveGeofenceIntent, this);
                break;

            // If removeGeofencesById was called
            case LIST :
                mLocationClient.removeGeofences(mCurrentGeofenceIds, this);
                break;
        }
    }
    
    /**
     * remove geofence by a list of ids ( the list needs to have at least one id )
     * @param geofenceIds
     * @throws IllegalArgumentException
     * @throws UnsupportedOperationException
     */
    public void removeGeofencesById(ArrayList<String> geofenceIds) throws
    IllegalArgumentException, UnsupportedOperationException {

    	
	    if ((null == geofenceIds) || (geofenceIds.size() == 0)) {
	        throw new IllegalArgumentException();
	
	    // Set the request type, store the List, and request a location client connection.
	    } 
	    else {
	
	        // If there's no removal in progress, continue
	        if (!mRemovingGeofenceInProgress) {
	            mRequestType =GooglePlayServiceUtil.GEO_FENCE_REMOVE_TYPE.LIST;
	            mCurrentGeofenceIds = geofenceIds;
	            requestConnection();
	
	        // If a removal request is in progress, throw an exception
	        } else {
	            throw new UnsupportedOperationException();
	        }
	    }
    }
    
    
    /**
     * remove geofence by intent
     * @param requestIntent
     */
    public void removeGeofencesByIntent(PendingIntent requestIntent) {

        // If a removal request is not in progress, continue
        if (!mRemovingGeofenceInProgress) {
            // Set the request type, store the List, and request a location client connection.
            mRequestType = GooglePlayServiceUtil.GEO_FENCE_REMOVE_TYPE.INTENT;
            mRemoveGeofenceIntent = requestIntent;
            requestConnection();

        // If a removal request is in progress, throw an exception
        } else {

            throw new UnsupportedOperationException();
        }
    }
    
    
    /**
     * When the request to remove geofences by PendingIntent returns, handle the result.
     *
     * @param statusCode the code returned by Location Services
     * @param requestIntent The Intent used to request the removal.
     */
	@Override
	public void onRemoveGeofencesByPendingIntentResult(int statusCode,
			PendingIntent requestIntent) {
		
		// If adding the geocodes was successful
        if (statusCode==LocationStatusCodes.SUCCESS) {

            Log.d(LOG_TAG, "[onAddGeofencesResult] the geofences are successfully added by intent, the status code is " + statusCode);
            
        // If we failed to add the geofence
        } else {

        	Log.d(LOG_TAG, "[onAddGeofencesResult] the geofences failed to be added by intent, the status code is " + statusCode);

        }

        // no matter what disconnect the location client
        requestDisconnection();
 

	}

	@Override
	public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
		
		
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

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		
		mRemovingGeofenceInProgress = false;

		 if (connectionResult.hasResolution()) {	        	
	        	Log.d(LOG_TAG,"[onConnectionFailed] Conntection to Google Play services is failed");
	        	
	     } else {
	        	Log.e(LOG_TAG,"[onConnectionFailed] No Google Play services is available, the error code is "
	        			 + connectionResult.getErrorCode());
	     }

	}

	@Override
	public void onConnected(Bundle arg0) {
		
		Log.d(LOG_TAG,"[onConnected] Google Play services is connected for removing the geofence update");
        
		// Continue the process of adding geofence
		 continueRemoveGeofences();

	}

	@Override
	public void onDisconnected() {
		
		// Turn off the request flag
		mRemovingGeofenceInProgress = false;

        // In debug mode, log the disconnection
        Log.d(LOG_TAG, "[onDisconnected] Google Play services is disconnected for requesting the geofence update");

        // Destroy the current location client
        mLocationClient = null;

	}

}
