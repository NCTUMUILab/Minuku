package edu.umich.si.inteco.minuku.contextmanager;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.*;
import com.google.android.gms.location.ActivityRecognition;

import edu.umich.si.inteco.minuku.services.CaptureProbeService;

public class ActivityRecognitionRemover implements ConnectionCallbacks,
		OnConnectionFailedListener {

	/** Tag for logging. */
    private static final String LOG_TAG = "ActRcgnRemover";
    private Context mContext;
    // Store the current activity recognition client
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mCurrentIntent;

    //intiation
    public ActivityRecognitionRemover(Context context) {
        mContext = context;
        mGoogleApiClient = null;
    }
    
    
    /**
     * Remove the activity recognition updates. The PendingIntent is 
     * the one used in the request to add activity recognition updates.
     *
     * @param requestIntent The PendingIntent used to request activity recognition updates
     */
    public void removeUpdates(PendingIntent requestIntent) {

        Log.d(LOG_TAG,"[removeUpdates] going to remove activity recognition ");
        mCurrentIntent = requestIntent;

        // Requesting a connection and then remove the update
        requestConnection();
    }


    /**
     * Get the current activity recognition client, or create a new one if necessary.
     * @return An ActivityRecognitionClient object
     */
    private GoogleApiClient getGoogleApiClient() {

        if (mGoogleApiClient == null) {
            mGoogleApiClient =
                    new GoogleApiClient.Builder(mContext)
                            .addApi(ActivityRecognition.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
        }
        return mGoogleApiClient;

    }


    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    private void requestConnection() {
        getGoogleApiClient().connect();
    }
    
    
    /**
     * Get a activity recognition client and disconnect from Location Services
     */
    private void requestDisconnection() {

        // Disconnect the client
        getGoogleApiClient().disconnect();

        // Set the client to null
        setActivityRecognitionClient(null);
    }


    /**
     * Send a request to remove activity recognition updates after the connection is built, s
     */
    private void continueRemoveUpdates() {

        Log.d(LOG_TAG, "the Google Play servce is connected, now start to request removing activity");

    	/*
         * Request updates, using the default detection interval.
         * The PendingIntent sends updates to ActivityRecognitionIntentService
         */

        //request activity recognition update
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,                     //GoogleApiClient client
                mCurrentIntent);                      //callbackIntent

        //Cancel the PendingIntent. 
        mCurrentIntent.cancel();
        
        // Disconnect the client
        requestDisconnection();
    }



    //set activity client
    public void setActivityRecognitionClient(GoogleApiClient client) {
        mGoogleApiClient = client;
    }



	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
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
    public void onConnectionSuspended(int i) {

    }

}
