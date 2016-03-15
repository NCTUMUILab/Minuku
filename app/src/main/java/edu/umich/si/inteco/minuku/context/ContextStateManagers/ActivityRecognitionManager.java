package edu.umich.si.inteco.minuku.context.ContextStateManagers;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.context.ActivityRecognitionService;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.ContextSource;
import edu.umich.si.inteco.minuku.model.Record.ActivityRecognitionRecord;

/**
 * Created by Armuro on 10/4/15.
 */
public class ActivityRecognitionManager extends ContextStateManager
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**Table Name**/
    public static final String RECORD_TABLE_NAME_ACTIVITY_RECOGNITION = "Record_Table_ActivityRecognition";


    /**ContextSourceType**/
    public static final int CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES = 0;
    public static final int CONTEXT_SOURCE_ACTIVITY_RECOGNITION_ALL_PROBABLE_ACTIVITIES = 1;

    public static final String CONTEXT_SOURCE_ACTIVITY_RECOGNITION = "ActivityRecognition";

    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_NAME = "DetectedActivities";

    public static final String STRING_CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES = "AR-MostProbableActivity";
    public static final String STRING_CONTEXT_SOURCE_ACTIVITY_RECOGNITION_ALL_PROBABLE_ACTIVITIES = "AR-AllProbableActivities";

    /**label **/
    public static final String STRING_DETECTED_ACTIVITY_IN_VEHICLE = "in_vehicle";
    public static final String STRING_DETECTED_ACTIVITY_ON_FOOT = "on_foot";
    public static final String STRING_DETECTED_ACTIVITY_WALKING = "walking";
    public static final String STRING_DETECTED_ACTIVITY_RUNNING = "running";
    public static final String STRING_DETECTED_ACTIVITY_TILTING = "tilting";
    public static final String STRING_DETECTED_ACTIVITY_STILL = "still";
    public static final String STRING_DETECTED_ACTIVITY_ON_BICYCLE = "on_bicycle";
    public static final String STRING_DETECTED_ACTIVITY_UNKNOWN = "unknown";
    public static final String STRING_DETECTED_ACTIVITY_NA = "NA";

    /** Tag for logging. */
    private static final String LOG_TAG = "ActRcgnManager";

    public static final int NO_ACTIVITY_TYPE = -1;

    //the frequency of requesting google activity from the google play service
    public static int ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS = 5;

    public static long ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL =
            ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS * Constants.MILLISECONDS_PER_SECOND;

    private static long sActivityRecognitionUpdateIntervalInSeconds = ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS;

    private static long sActivityRecognitionUpdateIntervalInMilliseconds =
            sActivityRecognitionUpdateIntervalInSeconds * Constants.MILLISECONDS_PER_SECOND;

    private static Context mContext;

    /***Activity Recognition Requester**/
    private PendingIntent mActivityRecognitionPendingIntent;

    // Store the current activity recognition client
    private static  GoogleApiClient mGoogleApiClient;

    private boolean mRequestingActivityRecognitionUpdates;

    private static List<DetectedActivity> sProbableActivities;

    private static DetectedActivity sMostProbableActivity;

    private static long sLatestDetectionTime = -1;

    protected static int sLocalRecordPoolMaxSize =
            3 * (Constants.SECONDS_PER_MINUTE / ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS); //36 = 3 mins * 12 times/min

    /** KeepAlive **/
    protected int KEEPALIVE_MINUTE = 3;

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

        setUpContextSourceList();

        //we use 3 minutes insead of 5 minutes
        setKeepalive(KEEPALIVE_MINUTE * Constants.MILLISECONDS_PER_MINUTE);

    }

    /** each ContextStateManager should override this static method
     * it adds a list of ContextSource that it will manage **/
    @Override
    protected void setUpContextSourceList(){

        boolean isAvailable;

        // Google Play Service is available after api level 15
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            isAvailable = true;
        }
        else
            isAvailable = false;


        //add two context sources: most probable activity and all probable activities, with defaul sampling rate.
        mContextSourceList.add(
                new ContextSource(
                        STRING_CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES,
                        CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES,
                        isAvailable,
                        sActivityRecognitionUpdateIntervalInMilliseconds
                ));

        mContextSourceList.add(
                new ContextSource(
                        STRING_CONTEXT_SOURCE_ACTIVITY_RECOGNITION_ALL_PROBABLE_ACTIVITIES,
                        CONTEXT_SOURCE_ACTIVITY_RECOGNITION_ALL_PROBABLE_ACTIVITIES,
                        isAvailable,
                        sActivityRecognitionUpdateIntervalInMilliseconds
                ));

        return;

    }


    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {

        if (mGoogleApiClient==null){

            mGoogleApiClient =
                    new GoogleApiClient.Builder(mContext)
                            .addApi(ActivityRecognition.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
        }

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
    @Override
    public void requestUpdates() {

        Log.d(LOG_TAG, "[requestUpdates] Activity Recognition going to request location update ");
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
    @Override
    public void removeUpdates() {

        mRequestingActivityRecognitionUpdates = false;

        //Log.d(LOG_TAG,"[removeUpdates] going to remove activity recognition ");
        //first check whether we have GoogleAPIClient connected. if yes, we request activity
        // recognition. Otherwise we connect the client and then in onConnected() we request location
        if (!mGoogleApiClient.isConnected()){
            connentClient();
        }
        else {
            stopActivityRecognitionUpdates();
//            startActivityRecognitionUpdates();

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
        if (ActivityRecognition.ActivityRecognitionApi!=null){
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    mGoogleApiClient,                     //GoogleApiClient client
                    mActivityRecognitionPendingIntent);                      //callbackIntent

            //Cancel the PendingIntent.
            mActivityRecognitionPendingIntent.cancel();
        }

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
        if (ActivityRecognition.ActivityRecognitionApi!=null){
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mGoogleApiClient,                    //GoogleApiClient client
                    ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL,//detectionIntervalMillis
                    mActivityRecognitionPendingIntent);   //callbackIntent
        }

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
            removeUpdates();
        }

    }



    /**
     * ContextStateMAnager needs to override this fundtion to implement writing a Record and save it to the LocalDataPool
     */
    @Override
    public void saveRecordToLocalRecordPool() {

        /** create a Record to save timestamp, session it belongs to, and Data**/
        ActivityRecognitionRecord record = new ActivityRecognitionRecord();
        record.setProbableActivities(sProbableActivities);

        /** create data in a JSON Object. Each CotnextSource will have different formats.
         * So we need each ContextSourceMAnager to implement this part**/
        JSONObject data = new JSONObject();

        //also set data:
        JSONArray activitiesJSON = new JSONArray();

        //add all activities to JSONArray
        for (int i=0; i<sProbableActivities.size(); i++){
            DetectedActivity detectedActivity =  sProbableActivities.get(i);
            String activityAndConfidence = getActivityNameFromType(detectedActivity.getType()) + Constants.ACTIVITY_DELIMITER + detectedActivity.getConfidence();
            activitiesJSON.put(activityAndConfidence);
        }

        //add activityJSON Array to data
        try {
            data.put(RECORD_DATA_PROPERTY_NAME, activitiesJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**we set data in Record**/
        record.setData(data);
        record.setTimestamp(sLatestDetectionTime);

        Log.d(LOG_TAG, "testing saving records at " + record.getTimeString() + " data: " + record.getData());


        /**add it to the LocalRecordPool**/
        addRecord(record);

    }

    public static List<DetectedActivity> getProbableActivities() {
        return sProbableActivities;
    }

    public void setActivities (List<DetectedActivity> probableActivities, DetectedActivity mostProbableActivity ) {
        //set activities
        Log.d(LOG_TAG, "set most probsble: inside setActivities : " + mostProbableActivity + " and " + probableActivities);

        //set a list of probable activities
        setProbableActivities(probableActivities);
        //set the most probable activity
        setMostProbableActivity(mostProbableActivity);

        //either Most probable or all probable activities is requested should we setactivities, and put activites in localPool
        boolean isRequested =
                checkRequestStatusOfContextSource(STRING_CONTEXT_SOURCE_ACTIVITY_RECOGNITION_ALL_PROBABLE_ACTIVITIES)
                 | checkRequestStatusOfContextSource(STRING_CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES);

        if (isRequested){
            saveRecordToLocalRecordPool();
        }
    }

    public void setProbableActivities(List<DetectedActivity> probableActivities) {

        sProbableActivities = probableActivities;
        updateStateValues(CONTEXT_SOURCE_ACTIVITY_RECOGNITION_ALL_PROBABLE_ACTIVITIES);

    }

    public void setMostProbableActivity(DetectedActivity mostProbableActivity) {

        sMostProbableActivity = mostProbableActivity;
        updateStateValues(CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES);

    }


    public static DetectedActivity getMostProbableActivity() {
        return sMostProbableActivity;
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
     * This function examines StateMappingRule with the data and returns a boolean pass.
     * @param sourceType
     * @param measure
     * @param relationship
     * @param targetValue
     * @return
     */
    @Override
    protected boolean examineStateRule(int sourceType, String measure, String relationship, String targetValue,  ArrayList<String> params){


        boolean pass = false;
        //1 first we need to get the right source based on the sourcetype.
        //so that we know where the get the source value.
        //
        if (sourceType== CONTEXT_SOURCE_ACTIVITY_RECOGNITION_ALL_PROBABLE_ACTIVITIES){


            if (measure.equals(CONTEXT_SOURCE_MEASURE_LATEST_ONE)){

            }
        }

        else if (sourceType== CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES){

            String sourceValue=null;

            /**get source value according to the measure type**/
            //if the measure is "latest value", get the latest saved data**/
            if (measure.equals(CONTEXT_SOURCE_MEASURE_LATEST_ONE)){
                sourceValue= getActivityNameFromType(getMostProbableActivity().getType());
            }

            /** examine the criterion after we get the source value**/
            if (sourceValue != null) {

                pass = satisfyCriterion(sourceValue, relationship, targetValue);
//                Log.d(LOG_TAG, "test smr examine statemappingrule, get measure "
//                        + getContextSourceNameFromType(sourceType) + " and get value : " +  sourceValue +
//                        "now examine target value : " + targetValue + " so the pass is : " + pass);

            }


        }

        return  pass;

    }

    /** this function allows ConfigurationManager to adjust the configuration of each ContextSource,
     * e.g sampling rate. */
    @Override
    public void updateContextSourceList(String source, long samplingRate){

        sActivityRecognitionUpdateIntervalInMilliseconds = samplingRate;

        //update all sources if the source name is a general name (e.g. ActivityRecognition)
        if (source.equals(CONTEXT_SOURCE_ACTIVITY_RECOGNITION)) {
            for (int i = 0; i < mContextSourceList.size(); i++) {
                getContextSourceBySourceName(mContextSourceList.get(i).getName()).setSamplingRate(sActivityRecognitionUpdateIntervalInMilliseconds);
            }
        }

        //if not using a general category, update individual sources by source name
        else {
            if (getContextSourceBySourceName(source)!=null){
                getContextSourceBySourceName(source).setSamplingRate(sActivityRecognitionUpdateIntervalInMilliseconds);
            }

        }
    }


    /**
     *For each ContextSource we check whether it is requested and update its request status.
     * Each ContextStateManager should override this function because they will need to determine
     * whether to stop or start extracting certain data source, depending on its Request Status
     */
    @Override
    protected void updateContextSourceListRequestStatus() {

        boolean isRequested = false;
        for (int i=0; i<mContextSourceList.size(); i++){
            mContextSourceList.get(i).setIsRequested(updateContextSourceRequestStatus(mContextSourceList.get(i)));
            Log.d(LOG_TAG, "[updateContextSourceListRequestStatus] check saving data the contextsource " + mContextSourceList.get(i).getName() + " requested: " + mContextSourceList.get(i).isRequested());

            //if any of MostProbableActiivty or AllProbableActiivty is requested, we still receive ActivityRecognitionUpdate
            isRequested = isRequested | mContextSourceList.get(i).isRequested();

        }

        if (isRequested) {
            requestUpdates();
        }

        //If neither AllProbableActivities nor MostProbableActivity are requested, we should stop requesting activity information
        else{
            removeUpdates();
        }

    }

    /**
     * This function takes a ContextSource and examines whether it will used by a loggingTask
     * @param contextSource
     * @return
     */
    @Override
    protected boolean isRequestedByActiveLoggingTasks(ContextSource contextSource) {

        for (int i=0; i<mLoggingTasks.size(); i++) {
//
//            Log.d(LOG_TAG, "[testing logging task and requested] isRequestedByActiveLoggingTasks " +
//                    "checking ContextSource " + contextSource.getName() + " with logging task" +
//                    mLoggingTasks.get(i).getSourceType());
//
            Log.d(LOG_TAG, "[testing logging task and requested] comparing " +
                    "ContextSourceName " + contextSource.getName() + " with source in LoggingTask Name" +
                    mLoggingTasks.get(i).getSource());

            //find the logging task containing the contextsource and see if the loggingTask is enabled
            if (CONTEXT_SOURCE_ACTIVITY_RECOGNITION.equals(mLoggingTasks.get(i).getSource())
                    &&  mLoggingTasks.get(i).isEnabled() ){
                Log.d(LOG_TAG, "[testing logging task and requested] the ContextSource " + contextSource.getName() +
                        " indeed is requested by the logging task" );

                return true;
            }
        }
        return false;
    }

    public static int getContextSourceTypeFromName(String sourceName) {

        switch (sourceName){

            case STRING_CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES:
                return CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES;
            case STRING_CONTEXT_SOURCE_ACTIVITY_RECOGNITION_ALL_PROBABLE_ACTIVITIES:
                return CONTEXT_SOURCE_ACTIVITY_RECOGNITION_ALL_PROBABLE_ACTIVITIES;
            //the default is most probable activities
            default:
                return CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES;
        }
    }

    public static String getContextSourceNameFromType(int sourceType) {

        switch (sourceType){

            case CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES:
                return STRING_CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES;
            case CONTEXT_SOURCE_ACTIVITY_RECOGNITION_ALL_PROBABLE_ACTIVITIES:
                return STRING_CONTEXT_SOURCE_ACTIVITY_RECOGNITION_ALL_PROBABLE_ACTIVITIES;
            default:
                return STRING_CONTEXT_SOURCE_ACTIVITY_RECOGNITION_MOST_PROBABLE_ACTIVITIES;

        }
    }


    /**Database table name should be defined by each ContextStateManager. So each CSM should overwrite this**/
    public static String getDatabaseTableNameBySourceName (String sourceName) {
        return RECORD_TABLE_NAME_ACTIVITY_RECOGNITION;
    }

    /**
     * this function should return a list of database table names for its contextsource. Must implement it
     * in order to create tables
     * @return
     */
    @Override
    public ArrayList<String> getAllDatabaseTableNames () {
        ArrayList<String> tablenames = new ArrayList<String>();

        tablenames.add(RECORD_TABLE_NAME_ACTIVITY_RECOGNITION);

        return tablenames;
    }

    /**
     * Map detected activity types to strings
     */
    public static String getActivityNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return STRING_DETECTED_ACTIVITY_IN_VEHICLE;
            case DetectedActivity.ON_BICYCLE:
                return STRING_DETECTED_ACTIVITY_ON_BICYCLE;
            case DetectedActivity.ON_FOOT:
                return STRING_DETECTED_ACTIVITY_ON_FOOT;
            case DetectedActivity.STILL:
                return STRING_DETECTED_ACTIVITY_STILL;
            case DetectedActivity.RUNNING:
                return STRING_DETECTED_ACTIVITY_RUNNING;
            case DetectedActivity.WALKING:
                return STRING_DETECTED_ACTIVITY_WALKING;
            case DetectedActivity.UNKNOWN:
                return STRING_DETECTED_ACTIVITY_UNKNOWN;
            case DetectedActivity.TILTING:
                return STRING_DETECTED_ACTIVITY_TILTING;
            case NO_ACTIVITY_TYPE:
                return STRING_DETECTED_ACTIVITY_NA;
        }
        return "NA";
    }


    public static int getActivityTypeFromName(String activityName) {

        if (activityName.equals(STRING_DETECTED_ACTIVITY_IN_VEHICLE)) {
            return DetectedActivity.IN_VEHICLE;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_ON_BICYCLE)) {
            return DetectedActivity.ON_BICYCLE;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_ON_FOOT)) {
            return DetectedActivity.ON_FOOT;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_STILL)) {
            return DetectedActivity.STILL;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_UNKNOWN)) {
            return DetectedActivity.UNKNOWN ;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_RUNNING)) {
            return DetectedActivity.RUNNING ;
        }else if (activityName.equals(STRING_DETECTED_ACTIVITY_WALKING)){
            return DetectedActivity.WALKING;
        }else if(activityName.equals(STRING_DETECTED_ACTIVITY_TILTING)) {
            return DetectedActivity.TILTING;
        }else {
            return NO_ACTIVITY_TYPE;
        }

    }


}
