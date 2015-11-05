package edu.umich.si.inteco.minuku.context;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import edu.umich.si.inteco.minuku.Constants;


public class ActivityRecognitionRequester implements ConnectionCallbacks, OnConnectionFailedListener{

	/** Tag for logging. */
    private static final String LOG_TAG = "ActRcgnRequester";

	private Context mContext;   
    
    /***Activity Recognition Requester**/
    private PendingIntent mActivityRecognitionPendingIntent;


    //the frequency of requesting google activity from the google play service
    public static int ACTIVITY_RECOGNITION_UPDATE_INTERVAL_IN_SECONDS = 5;

    public static long ACTIVITY_RECOGNITION_UPDATE_INTERVAL =
            ACTIVITY_RECOGNITION_UPDATE_INTERVAL_IN_SECONDS * Constants.MILLISECONDS_PER_SECOND;
    
    // Store the current activity recognition client
    private GoogleApiClient mGoogleApiClient;
    
    // Flag that indicates if a request is underway.
    private boolean mActivityRecognitionRequestInProgress;
    
	public  ActivityRecognitionRequester(Context context){
    	
		 mContext= context;
		 mActivityRecognitionPendingIntent = null;
         mGoogleApiClient = null;

    }
	
	/**
	 * Returns the current PendingIntent to the caller.
	 * @return
	 */
	public PendingIntent getRequestPendingIntent() {
        return mActivityRecognitionPendingIntent;
    }
	
    
	/**
	 * Sets the PendingIntent of requesting activity recognition update
	 * @param intent The PendingIntent
	 */
	public void setRequestPendingIntent(PendingIntent intent) {
	    mActivityRecognitionPendingIntent = intent;
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
     * Start the activity recognition update request process by
     * getting a connection.
     */
    public void requestUpdates() {
        requestConnection();
    }
    
    /**
     * Request a connection to Location Services. 
     */
    private void requestConnection() {
        getGoogleApiClient().connect();
    }
    
    /**
     * Request a disconnection to Location Services. 
     */
    private void requestDisconnection() {
        getGoogleApiClient().disconnect();
    }
	

    //this function handles the actual activity request
    private void continueRequestActivityUpdates() {
    	
    	Log.d(LOG_TAG, "the Google Play servce is connected, now start to request activity");
		
    	/*
         * Request updates, using the default detection interval.
         * The PendingIntent sends updates to ActivityRecognitionIntentService
         */

        //create a pendingIntent to request activity update.
        PendingIntent activityRecognitionPendingIntent = createRequestPendingIntent();

        //request activity recognition update
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,                                       //GoogleApiClient client
                ACTIVITY_RECOGNITION_UPDATE_INTERVAL,//detectionIntervalMillis
                activityRecognitionPendingIntent);                      //callbackIntent
		
		Log.d(LOG_TAG,"requesting activity update!");
		
		// after request the update, disconnect the client

        requestDisconnection();

    }

    
	private PendingIntent createRequestPendingIntent() {

        // If the PendingIntent already exists
        if (null != getRequestPendingIntent()) {

            // Return the existing intent
            return mActivityRecognitionPendingIntent;

        // If no PendingIntent exists
        } else {

            // Create an Intent pointing to the IntentService
        	Intent intent = new Intent(
                    mContext, ActivityRecognitionService.class);

            /*
             * Return a PendingIntent to start the IntentService.
             * Always create a PendingIntent sent to Location Services
             * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
             * again updates the original. Otherwise, Location Services
             * can't match the PendingIntent to requests made with it.
             */
        	PendingIntent pendingIntent =
                     PendingIntent.getService(mContext, 
                    		 0,
                    		 //NotificationHelper.generatePendingIntentRequestCode(ContextManager.getCurrentTimeInMillis()),
                    		 intent,
                    		 PendingIntent.FLAG_UPDATE_CURRENT);
        	 

            setRequestPendingIntent(pendingIntent);
            return pendingIntent;
        }
	}

        
   
        
        
	/**
	 * Google Activity Recognition Service*
	 * **/
	private boolean GooglePlayServiceConnected(){
		
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
	
		// If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
        	
        	Log.d("Location Updates","Google Play services is available.");
            return true;

        } else {
      	
            //  Get the error code       
            return false;
        }
	}


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		
		 if (connectionResult.hasResolution()) {	        	
	        	Log.d(LOG_TAG,"[onConnectionFailed] Conntection to Google Play services is failed");
	        	
	     } else {
	        	Log.e(LOG_TAG,"[onConnectionFailed] No Google Play services is available, the error code is "
	        			 + connectionResult.getErrorCode());
	     }
		
	}
	 

	@Override
	public void onConnected(Bundle connectionHint) {

		Log.d(LOG_TAG,"[onConnected] Google Play services is connected for requesting the update, ");
        
		// Continue the process of requesting activity recognition updates
        continueRequestActivityUpdates();

	}

    @Override
    public void onConnectionSuspended(int i) {

    }
/*
    @Override
	public void onDisconnected() {

		Log.d(LOG_TAG,"[onConnected] Google Play services is disconnected for requesting the update, ");
		
		// Delete the client
        mGoogleApiClient = null;
	}

*/
	

}
