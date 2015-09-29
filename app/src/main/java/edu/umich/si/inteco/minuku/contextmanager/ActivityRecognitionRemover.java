package edu.umich.si.inteco.minuku.contextmanager;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.*;
import com.google.android.gms.location.ActivityRecognition;

public class ActivityRecognitionRemover implements ConnectionCallbacks,
		OnConnectionFailedListener {

	/** Tag for logging. */
    private static final String LOG_TAG = "ActivityRecognitionRemover";
    private Context mContext;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private PendingIntent mCurrentIntent;
    
    public ActivityRecognitionRemover(Context context) {
    	
        mContext = context;
        mActivityRecognitionClient = null;
    }
    
    
    /**
     * Remove the activity recognition updates. The PendingIntent is 
     * the one used in the request to add activity recognition updates.
     *
     * @param requestIntent The PendingIntent used to request activity recognition updates
     */
    public void removeUpdates(PendingIntent requestIntent) {

        Log.d(LOG_TAG,"[removeUpdates] going to remove activity recognition ");
        /*
         * Set the request type, store the List, and request a activity recognition client
         * connection.
         */
        mCurrentIntent = requestIntent;

        // Requesting a connection and then remove the update
        requestConnection();
    }

    
    /**
     * Get the current activity recognition client, or create a new one if necessary.
     */
    public ActivityRecognitionClient getActivityRecognitionClient() {

         //If a client doesn't already exist, create a new one
        if (mActivityRecognitionClient == null) {
            // Create a new one
            setActivityRecognitionClient(new ActivityRecognitionClient(mContext, this, this));
        }
        return mActivityRecognitionClient;
    }

    
    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    private void requestConnection() {
        getActivityRecognitionClient().connect();
    }
    
    
    /**
     * Get a activity recognition client and disconnect from Location Services
     */
    private void requestDisconnection() {

        // Disconnect the client
        getActivityRecognitionClient().disconnect();

        // Set the client to null
        setActivityRecognitionClient(null);
    }
    
    /**
     * Send a request to remove activity recognition updates after the connection is built, s
     */
    private void continueRemoveUpdates() {
        
        // Remove the updates
        mActivityRecognitionClient.removeActivityUpdates(mCurrentIntent);     

        //Cancel the PendingIntent. 
        mCurrentIntent.cancel();
        
        // Disconnect the client
        requestDisconnection();
    }

    //set activity client
    public void setActivityRecognitionClient(ActivityRecognitionClient client) {
        mActivityRecognitionClient = client;

    }
    
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */

        if (connectionResult.hasResolution()) {
        	Log.d(LOG_TAG,"Conntection to Google Play services is failed");
        } else {
        	Log.d(LOG_TAG,"No Google Play services is available");
        }

	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.d(LOG_TAG,"[onConnected] Google Play services is connected for removing the update, ");

		//send a request to remove the update
		continueRemoveUpdates();
	}

	@Override
	public void onDisconnected() {
		
		Log.d(LOG_TAG,"[onDisConnected]  Google Play services is disconnected for removing the update,");

		 // Destroy the current activity recognition client
        mActivityRecognitionClient = null;
	}

}
