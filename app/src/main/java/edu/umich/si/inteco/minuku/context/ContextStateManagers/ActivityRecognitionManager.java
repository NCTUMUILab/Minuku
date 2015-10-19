package edu.umich.si.inteco.minuku.context.ContextStateManagers;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.context.ActivityRecognitionService;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.State;
import edu.umich.si.inteco.minuku.model.StateMappingRule;
import edu.umich.si.inteco.minuku.model.record.ActivityRecord;

/**
 * Created by Armuro on 10/4/15.
 */
public class ActivityRecognitionManager extends ContextStateManager
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    /**ContextSourceType**/
    public static final int CONTEXT_SOURCE_MOST_PROBABLE_ACTIVITIES = 0;
    public static final int CONTEXT_SOURCE_ALL_PROBABLE_ACTIVITIES = 1;


    public static final String CONTEXT_SOURCE_MOST_PROBABLE_ACTIVITIES_STRING = "mostProbableActivities";
    public static final String CONTEXT_SOURCE_ALL_PROBABLE_ACTIVITIES_STRING = "allProbableActivities";


    /**label **/
    public static final String IN_VEHICLE_STRING = "in_vehicle";
    public static final String ON_FOOT_STRING = "on_foot";
    public static final String WALKING_STRING = "walking";
    public static final String RUNNING_STRING = "running";
    public static final String TILTING_STRING = "tilting";
    public static final String STILL_STRING = "still";
    public static final String ON_BiCYCLE_STRING = "on_bicycle";
    public static final String UNKNOWN_STRING = "unknown";
    public static final String NA_STRING = "NA";

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

        super();
        mContext= context;

        setName(ContextManager.CONTEXT_STATE_MANAGER_ACTIVITY_RECOGNITION);

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

        //Log.d(LOG_TAG, "[requestUpdates] going to request location update ");
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

        //Log.d(LOG_TAG,"[removeUpdates] going to remove activity recognition ");
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
           // Log.d(LOG_TAG,"[onConnectionFailed] Conntection to Google Play services is failed");

        } else {
            Log.e(LOG_TAG, "[onConnectionFailed] No Google Play services is available, the error code is "
                    + connectionResult.getErrorCode());
        }

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //Log.d(LOG_TAG,"[test activity recognition update] the location servce is connected, going to request locaiton updates");

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

    public static void setActivities (List<DetectedActivity> probableActivities, DetectedActivity mostProbableActivity ) {
        //set activities
        //Log.d(LOG_TAG, "set most probsble: inside setActivities : " + mostProbableActivity + " and " + probableActivities);

        //set a list of probable activities
        setProbableActivities(probableActivities);
        //set the most probable activity
        setMostProbableActivity(mostProbableActivity);

        //after update activity information, update the values of the states
        updateStateValues(CONTEXT_SOURCE_MOST_PROBABLE_ACTIVITIES);
    }

    private static void setProbableActivities(List<DetectedActivity> probableActivities) {
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
//        Log.d(LOG_TAG, "set most probsble: inside sMostProbableActivity: " + mostProbableActivity);
        //after update activity information, update the values of the states
        updateStateValues(CONTEXT_SOURCE_MOST_PROBABLE_ACTIVITIES);
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
                return IN_VEHICLE_STRING;
            case DetectedActivity.ON_BICYCLE:
                return ON_BiCYCLE_STRING;
            case DetectedActivity.ON_FOOT:
                return ON_FOOT_STRING;
            case DetectedActivity.STILL:
                return STILL_STRING;
            case DetectedActivity.RUNNING:
                return RUNNING_STRING;
            case DetectedActivity.WALKING:
                return WALKING_STRING;
            case DetectedActivity.UNKNOWN:
                return UNKNOWN_STRING;
            case DetectedActivity.TILTING:
                return TILTING_STRING;
            case NO_ACTIVITY_TYPE:
                return NA_STRING;
        }
        return "NA";
    }


    public static int getActivityTypeFromName(String activityName) {

        if (activityName.equals(IN_VEHICLE_STRING)) {
            return DetectedActivity.IN_VEHICLE;
        }else if(activityName.equals(ON_BiCYCLE_STRING)) {
            return DetectedActivity.ON_BICYCLE;
        }else if(activityName.equals(ON_FOOT_STRING)) {
            return DetectedActivity.ON_FOOT;
        }else if(activityName.equals(STILL_STRING)) {
            return DetectedActivity.STILL;
        }else if(activityName.equals(UNKNOWN_STRING)) {
            return DetectedActivity.UNKNOWN ;
        }else if(activityName.equals(RUNNING_STRING)) {
            return DetectedActivity.RUNNING ;
        }else if (activityName.equals(WALKING_STRING)){
            return DetectedActivity.WALKING;
        }else if(activityName.equals(TILTING_STRING)) {
            return DetectedActivity.TILTING;
        }else {
            return NO_ACTIVITY_TYPE;
        }

    }


    @Override
    public void saveRecordsInLocalRecordPool() {

    }


    /**
     * Examine whether the context source is needed in order to monitor a state.
     * @param sourceType
     * @return
     */
    private static boolean isStateMonitored(int sourceType) {

        Log.d(LOG_TAG, "examine statemappingrule: in isStateMonitored");
        for (int i=0; i<getStateMappingRules().size(); i++){

            if (getStateMappingRules().get(i).getSource()==sourceType){
                return true;
            }
        }
        return false;
    }

    /**
     *the timing for ActivityRecognitionManager to updateStateValues is whenever it's activity information
     * is updated. We use the StateMappingRules to decide whether we should change the values of the states.
     * To to do this,we first get the relevant state based on the type.
     * @param sourceType
     */
    public static void updateStateValues(int sourceType) {


        //1. we first make sure whether the sourceType is being monitored. If not, we don't need to update
        //the state values
        //Log.d(LOG_TAG, "examine statemappingrule, the state is being monitored: " + isStateMonitored(sourceType));
        if (!isStateMonitored(sourceType)) {
            return;
        }

        //2. if yes, we get the stateMappingRule by the type to see it's threshold
        // source  = all probable activities

        for (int i=0; i<getStateMappingRules().size(); i++) {
            //get the rule
            StateMappingRule rule = getStateMappingRules().get(i);
            boolean pass= false;

            //1. get the targer value and relaionship
            int relationship = rule.getRelationship();
            String targetValue = rule.getStringTargetValue();
            int measure = rule.getMeasure();

            //Log.d(LOG_TAG, "examine statemappingrule, now examine " + rule.getName() + " meausre: " +  rule.getMeasure()  + " sourceType: " + sourceType );

            /** examine criteri on specified in the SateMappingRule **/
            //1 first we need to get the right source based on the sourcetype.
            //so that we know where the get the source value.
            //
            if (sourceType==CONTEXT_SOURCE_ALL_PROBABLE_ACTIVITIES){


            }

            else if (sourceType==CONTEXT_SOURCE_MOST_PROBABLE_ACTIVITIES){

                String sourceValue=null;


                /**2. get source value according to the measure type**/

                //if the measure is "latest value", get the latest saved data**/
                if (measure==CONTEXT_SOURCE_MEASURE_LATEST_ONE){
                    sourceValue= getActivityNameFromType(getMostProbableActivity().getType());
                   // Log.d(LOG_TAG, "examine statemappingrule, now examine " + rule.getName() + " source : " +  sourceValue);
                }
                else if (measure==CONTEXT_SOURCE_MEASURE_AVERAGE) {
                    //there is no average value for ACTIVITY_RECOGNITION
                }



                /**3. examine the criterion after we get the source value**/
                if (sourceValue != null) {
                   // Log.d(LOG_TAG, "examine statemappingrule " + rule.getName() + " with current source value " + sourceValue);
                    pass = satisfyCriterion(sourceValue, relationship, targetValue);
                }


            }

            Log.d(LOG_TAG, "examine statemappingrule, after the examination the criterion is " + pass);


            /** 4. if the criterion is passed, we set the state value based on the mappingRule **/
            if (pass){

                for (int j=0; j<getStateList().size(); j++){
                    //find the state corresponding to the StateMappingRule

                    boolean valueChanged = false;

                    if (getStateList().get(j).getName().equals(rule.getName())){

                        String stateValue = rule.getStateValue();
                        //change the value based on the mapping rule.

                        /** 5. now we need to check whether the new value is different from its current value
                         * if yes. we need to call StateChange Method later **/
                        if (!getStateList().get(j).getValue().equals(stateValue) ){
                            //the value is changed to the new value,
                            valueChanged = true;
                        }

                        getStateList().get(j).setValue(stateValue);

                        Log.d(LOG_TAG, "examine statemappingrule, the state " + getStateList().get(j).getName() + " value change to " + getStateList().get(j).getValue());

                    }

                    //if the state changes to a new value
                    if (valueChanged){
                        //we call this method to invoke ContextManager to inspect event conditions.
                        stateChanged(getStateList().get(j));
                    }

                }
            }





        }

    }



    public static int getContextSourceTypeFromName(String sourceName) {

        switch (sourceName){

            case CONTEXT_SOURCE_MOST_PROBABLE_ACTIVITIES_STRING:
                return CONTEXT_SOURCE_MOST_PROBABLE_ACTIVITIES;
            case CONTEXT_SOURCE_ALL_PROBABLE_ACTIVITIES_STRING:
                return CONTEXT_SOURCE_ALL_PROBABLE_ACTIVITIES;
            default:
                return -1;
        }
    }

    public static String getContextSourceNameFromType(int sourceType) {

        switch (sourceType){

            case CONTEXT_SOURCE_MOST_PROBABLE_ACTIVITIES:
                return CONTEXT_SOURCE_MOST_PROBABLE_ACTIVITIES_STRING;
            case CONTEXT_SOURCE_ALL_PROBABLE_ACTIVITIES:
                return CONTEXT_SOURCE_ALL_PROBABLE_ACTIVITIES_STRING;
            default:
                return "NA";

        }
    }

}
