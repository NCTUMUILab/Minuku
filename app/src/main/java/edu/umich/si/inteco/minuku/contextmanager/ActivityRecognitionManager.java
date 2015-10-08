package edu.umich.si.inteco.minuku.contextmanager;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.model.record.ActivityRecord;
import edu.umich.si.inteco.minuku.services.MinukuMainService;

/**
 * Created by Armuro on 10/4/15.
 */
public class ActivityRecognitionManager extends ContextSourceManager
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /** Tag for logging. */
    private static final String LOG_TAG = "ActRcgnManager";

    public static final int NO_ACTIVITY_TYPE = -1;

    //the frequency of requesting google activity from the google play service
    public static int ACTIVITY_RECOGNITION_UPDATE_INTERVAL_IN_SECONDS = 5;

    public static int ACTIVITY_RECOGNITION_UPDATE_INTERVAL =
            ACTIVITY_RECOGNITION_UPDATE_INTERVAL_IN_SECONDS * Constants.MILLISECONDS_PER_SECOND;

    private Context mContext;

    /***Activity Recognition Requester**/
    private PendingIntent mActivityRecognitionPendingIntent;

    // Store the current activity recognition client
    private GoogleApiClient mGoogleApiClient;

    private boolean mRequestingActivityRecognitionUpdates;

    private static List<DetectedActivity> sProbableActivities;

    private static DetectedActivity sMostProbableActivity;

    private static long sLatestDetectionTime = -1;

    public ActivityRecognitionManager(Context context) {

        mContext= context;

        sProbableActivities = null;

        sMostProbableActivity = null;

        mRequestingActivityRecognitionUpdates = false;
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();

        sLatestDetectionTime = -1;
    }


    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(LOG_TAG, "Building GoogleApiClient");
        mGoogleApiClient =
                new GoogleApiClient.Builder(mContext)
                        .addApi(ActivityRecognition.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
    }


    /**
     * Get the current activity recognition client, or create a new one if necessary.
     * @return An ActivityRecognitionClient object
     */
    private GoogleApiClient getGoogleApiClient() {

        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        return mGoogleApiClient;
    }


    /**
     * Start the activity recognition update request process by
     * getting a connection.
     */
    public void requestActivityRecognitionUpdates() {

        Log.d(LOG_TAG, "[requestUpdates] going to request location update ");
        //we need to get location. Set this true
        mRequestingActivityRecognitionUpdates = true;

        //first check whether we have GoogleAPIClient connected. if yes, we request activity
        // recognition. Otherwise we connect the client and then in onConnected() we request location
        if (!mGoogleApiClient.isConnected()){
            connentClient();
        }
        else {
            startActivityRecognitionUpdates();
        }
    }



    /**
     * Remove the activity recognition updates. The PendingIntent is
     * the one used in the request to add activity recognition updates.
     *
     *
     */
    public void removeActivityRecognitionUpdates() {

        mRequestingActivityRecognitionUpdates = false;

        Log.d(LOG_TAG,"[removeUpdates] going to remove activity recognition ");
        //first check whether we have GoogleAPIClient connected. if yes, we request activity
        // recognition. Otherwise we connect the client and then in onConnected() we request location
        if (!mGoogleApiClient.isConnected()){
            connentClient();
        }
        else {
            startActivityRecognitionUpdates();

            //after remove, disconnect the client.
            disconnectClient();
        }
    }


    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    private void connentClient() {
        mGoogleApiClient.connect();
    }

    private void disconnectClient() {
        // Disconnect the client
        mGoogleApiClient.disconnect();
    }

    private void stopActivityRecognitionUpdates() {

        //request activity recognition update
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,                     //GoogleApiClient client
                mActivityRecognitionPendingIntent);                      //callbackIntent

        //Cancel the PendingIntent.
        mActivityRecognitionPendingIntent.cancel();
    }

    //this function handles the actual activity request
    private void startActivityRecognitionUpdates() {

        Log.d(LOG_TAG, "[startActivityRecognitionUpdates]");

    	/*
         * Request updates, using the default detection interval.
         * The PendingIntent sends updates to ActivityRecognitionIntentService
         */

        //create a pendingIntent to request activity update.
        mActivityRecognitionPendingIntent = createRequestPendingIntent();

        //request activity recognition update
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,                    //GoogleApiClient client
                ACTIVITY_RECOGNITION_UPDATE_INTERVAL,//detectionIntervalMillis
                mActivityRecognitionPendingIntent);   //callbackIntent

    }



    private PendingIntent createRequestPendingIntent() {

        // If the PendingIntent already exists
        if (mActivityRecognitionPendingIntent != null) {
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

            mActivityRecognitionPendingIntent = pendingIntent;
            return pendingIntent;
        }
    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            Log.d(LOG_TAG,"[onConnectionFailed] Conntection to Google Play services is failed");

        } else {
            Log.e(LOG_TAG, "[onConnectionFailed] No Google Play services is available, the error code is "
                    + connectionResult.getErrorCode());
        }

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(LOG_TAG,"[test activity recognition update] the location servce is connected, going to request locaiton updates");

        // If Minuku requests location updates before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (See requestLocationUpdates). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingActivityRecognitionUpdates) {
            startActivityRecognitionUpdates();
        }
        //if the client is connected in order to stop activity recognition update, we remove the update
        //after the client is connected
        else{
            removeActivityRecognitionUpdates();
        }

    }


    public static List<DetectedActivity> getProbableActivities() {
        return sProbableActivities;
    }

    public static void setProbableActivities(List<DetectedActivity> probableActivities) {

        sProbableActivities = probableActivities;
        setLatestDetectionTime(ContextManager.getCurrentTimeInMillis());

        //store activityRecord
        ActivityRecord record = new ActivityRecord();
        record.setProbableActivities(sProbableActivities);
        record.setTimestamp(sLatestDetectionTime);
        record.setDetectionTime(sLatestDetectionTime);
        //add record to local record pool of ActivityRecognitionManager
        addRecord(record);

    }

    public static DetectedActivity getMostProbableActivity() {
        return sMostProbableActivity;
    }

    public static void setMostProbableActivity(DetectedActivity mostProbableActivity) {
        sMostProbableActivity = mostProbableActivity;
    }

    public static long getLatestDetectionTime() {
        return sLatestDetectionTime;
    }

    public static void setLatestDetectionTime(long latestDetectionTime) {
        sLatestDetectionTime = latestDetectionTime;
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(LOG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


    /**
     * Map detected activity types to strings
     */
    public static String getActivityNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.WALKING:
                return "walking";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
            case NO_ACTIVITY_TYPE:
                return "NA";
        }
        return "NA";
    }


    public static int getActivityTypeFromName(String activityName) {

        if (activityName.equals("in_vehicle")) {
            return DetectedActivity.IN_VEHICLE;
        }else if(activityName.equals("on_bicycle")) {
            return DetectedActivity.ON_BICYCLE;
        }else if(activityName.equals("on_foot")) {
            return DetectedActivity.ON_FOOT;
        }else if(activityName.equals("still")) {
            return DetectedActivity.STILL;
        }else if(activityName.equals("unknown")) {
            return DetectedActivity.UNKNOWN ;
        }else if(activityName.equals("running")) {
            return DetectedActivity.RUNNING ;
        }else if (activityName.equals("walking")){
            return DetectedActivity.WALKING;
        }else if(activityName.equals("tilting")) {
            return DetectedActivity.TILTING;
        }else {
            return NO_ACTIVITY_TYPE;
        }

    }


    @Override
    public void examineConditions() {

    }

    @Override
    public void stateChanged() {

    }

    @Override
    public void saveRecordsInLocalRecordPool() {

    }

}
