package edu.umich.si.inteco.minuku.contextmanager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.data.DataHandler;
import edu.umich.si.inteco.minuku.data.LocalDBHelper;
import edu.umich.si.inteco.minuku.model.record.Record;
import edu.umich.si.inteco.minuku.util.GooglePlayServiceUtil;

public class ContextManager {

    private static final String LOG_TAG = "ContextManager";

    //whether we want a background recording
    protected static boolean sIsSavingRecordingDefault = true;

    //flag of whether ContextManager is currently extracting Context
    protected static boolean sIsExtractingContext = false;

    //flag of whether ContextManager needs to extractContext
    protected static boolean sIsExtractingContextEnabled = true;

    //flag of whether BackgroundRecording is enabled
    protected static boolean sIsBackgroundRecordingEnabled  = false;

    //mContext is MinukuService
	private Context mContext;

    public static final int BACKGROUND_RECORDING_INITIAL_DELAY = 0;
    public static final int CONTEXT_MANAGER_REFRESH_FREQUENCY = 5 ;


    /**RecordPool is a List for temporarily storing records that will be stored into the database or files later**/
    private static ArrayList<Record> mRecordPool;

    //the threshold of life of a record
    public static int RECORD_PRESERVATION_THRESHOLD_IN_MILLISECONDS = 2 *
            Constants.MILLISECONDS_PER_SECOND * Constants.SECONDS_PER_MINUTE;   //2 minutes

    /*RECORD TYPE NAME*/
    public static final String CONTEXT_RECORD_TYPE_LOCATION_NAME = "Location";
    public static final String CONTEXT_RECORD_TYPE_ACTIVITY_NAME = "Activity";
    public static final String CONTEXT_RECORD_TYPE_SENSOR_NAME= "Sensor";
    public static final String CONTEXT_RECORD_TYPE_GEOFENCE_NAME= "Geofence";
    public static final String CONTEXT_RECORD_TYPE_APPLICATION_ACTIVITY_NAME= "AppActivity";

    public static final String RECORD_SHORTNAME_LOCATION_LATITUDE = "lat";
    public static final String RECORD_SHORTNAME_LOCATION_LONGITUDE = "lng";
    public static final String RECORD_SHORTNAME_LOCATION_ACCURACY = "acc";
    public static final String RECORD_SHORTNAME_ACTIVITY_ACTIVITY = "activity";
    public static final String RECORD_SHORTNAME_ACTIVITY_CONFIDENCE = "confidence";
    public static final String RECORD_SHORTNAME_APPLICATION_ACTIVITY = "appActivity";
    public static final String RECORD_SHORTNAME_APPLICATION_PACKAGE = "appPackage";


    public static final int CONTEXT_RECORD_TYPE_LOCATION = 1;
    public static final int CONTEXT_RECORD_TYPE_ACTIVITY = 2;
    public static final int CONTEXT_RECORD_TYPE_SENSOR= 3;
    public static final int CONTEXT_RECORD_TYPE_GEOFENCE= 4;
    public static final int CONTEXT_RECORD_TYPE_APPLICATION_ACTIVITY = 5;

    
    /**Turning on or off sensor**/
    public static boolean accelerometerSensorIsEnabled = true;
    public static boolean gravitySensorIsEnabled = true;
    public static boolean gyrscopeSensorIsEnabled = true;
    public static boolean linearAcceleerationSensorIsEnabled = true;
    public static boolean rotationVectorSensorIsEnabled = true;
    
    public static boolean magneticFieldSensorIsEnabled = true;    
    public static boolean orientationSensorIsEnabled = true;
    public static boolean proximitySensorIsEnabled = true;
    
    public static boolean ambientTemperatureSensorIsEnabled = true;
    public static boolean lightSensorIsEnabled = true;
    public static boolean pressureSensorIsEnabled = true;
    public static boolean relativeHumiditySensorIsEnabled = true;   

    private static int testActivityRecordIndex = 0;

    public static ArrayList<Integer> RECORD_TYPE_LIST;


    //handle the local SQLite operation
  	private static LocalDBHelper mLocalDBHelpder;

    /***sensor values***/
    private float mAccelationSquareRoot;

    private ActivityRecognitionManager mActivityRecognitionManager;

    //inspect transportation mode of the user
    private TransportationModeManager mTransportationModeManager;

    private GeofenceManager mGeofenceManager;

    // the location update manager
    private LocationManager mLocationManager;

    //the manager that manages the status of the phone (network, battery)
    private PhoneStatusManager  mPhoneStatusManager;

    private PhoneActivityManager mPhoneActivityManager;

    private MobilityManager mMobilityManager;

    private final ScheduledExecutorService mScheduledExecutorService;

	public ContextManager(Context context){

		mContext = context;

		mLocalDBHelpder = new LocalDBHelper(mContext, Constants.TEST_DATABASE_NAME);
        //initiate the RecordPool
        mRecordPool = new ArrayList<Record>();

        mScheduledExecutorService = Executors.newScheduledThreadPool(CONTEXT_MANAGER_REFRESH_FREQUENCY);

        RECORD_TYPE_LIST = new ArrayList<Integer>();
        
        //add the record types into the list
        initiateRecordTypeList();

        //initiate Context Source Managers
        mLocationManager = new LocationManager(mContext);

        mActivityRecognitionManager = new ActivityRecognitionManager(mContext);

        mTransportationModeManager = new TransportationModeManager(mContext);

        mPhoneStatusManager = new PhoneStatusManager(mContext);

        mPhoneActivityManager = new PhoneActivityManager(mContext);

        mMobilityManager = new MobilityManager(mContext, this);


      //  mActivityRecognitionRequester = new ActivityRecognitionRequester(mContext);

      //  mActivityRecognitionRemover = new ActivityRecognitionRemover(mContext);

	}

    /**
     * we start the main function of ContextManager here
     */
    public void startContextManager() {

        Log.d(LOG_TAG, "[startContextManager]");

        /**if extractign contextual information is enabled, extract information**/
        if (sIsExtractingContextEnabled) {
            startExtractingContext();
        }

        startContextManagerMainThread();

    }

    public boolean isExtractingContext() {
        return sIsExtractingContext;
    }

    public void setExtractingContext(boolean flag) {
        sIsExtractingContext = flag;
    }

    public boolean isExtractingContextEnabled() {
        return sIsExtractingContextEnabled;
    }

    public void setExtractingContextEnabled(boolean flag) {
        sIsExtractingContextEnabled = flag;
    }


    public static void initiateRecordTypeList() {

        RECORD_TYPE_LIST.add(CONTEXT_RECORD_TYPE_LOCATION);
        RECORD_TYPE_LIST.add(CONTEXT_RECORD_TYPE_ACTIVITY);
        RECORD_TYPE_LIST.add(CONTEXT_RECORD_TYPE_APPLICATION_ACTIVITY);

        //TODO: add more record type
    }

    public static void addRecordToPool(Record record){

        if(mRecordPool!=null){
            mRecordPool.add(record);
            //Log.d(LOG_TAG, "In the RecordPool, there are currently " + mRecordPool.size() + "records");
        }else
            return;
    }

    public static ArrayList<Record> getRecordPool (){
        return mRecordPool;
    }


    public LocationManager getLocationManager(){
        if (mLocationManager==null){
            mLocationManager = new LocationManager(mContext);
        }
        return mLocationManager;
    }


    public void requesLocationUpdate () {
        if (mLocationManager!=null){
            Log.d(LOG_TAG, "[startRequestingLocation] start to request location udpate");
            mLocationManager.requestLocationUpdate();
        }
    }

    public void removeLocationUpdate () {
        if (mLocationManager!=null){
            Log.d(LOG_TAG, "[stopRequestingActivityRecognition] stop to request location");
            mLocationManager.removeLocationUpdate();
        }
    }


    /***
     * Requesting and Removing Activity Recognition Service
     */
    private void requestActivityRecognitionUpdate(){
        Log.d(LOG_TAG, "[startRequestingActivityRecognition] start to request activity udpate");
        mActivityRecognitionManager.requestActivityRecognitionUpdates();
    }

    private void removeActivityRecognitionUpdate(){
        Log.d(LOG_TAG, "[stopRequestingActivityRecognition] stop to request activity udpate");
        //if Google Play service is available, stop the update
        // Pass the remove request to the remover object (the intent is the same as the request intent)
        mActivityRecognitionManager.removeActivityRecognitionUpdates();
    }


    /**functions called by the ContextManager**/


    public void startExtractingContext(){

        //TODO: ContextManager register each context source manager to extract contextual information

        //if this.sIsExtractingContextEnabled is false, we don't extract context
        if (!sIsExtractingContextEnabled){
            return;
        }

        //TODO: depending on the source requested in the configuration, determine the source to use

        //get location information
        requesLocationUpdate();

        //get activity information
        requestActivityRecognitionUpdate();

        //registerSensors();



        //get geofence transitions
        //startRequestingGeofence();

        //set sIsExtractingContext true to indicate that ContextManager is currently extracting
        // contextual information
        this.sIsExtractingContext = true;

    }


    public void stopExtractingContext(){

        Log.d("LOG_TAG", "[stopExtractingContext]");
        this.sIsExtractingContext = false;

        //unregister sensors
       // unRegisterSensors();

        //remove location update
        removeLocationUpdate();

        //remove activity update
        removeActivityRecognitionUpdate();

    }


    /**
     * The function starts a thread to run background recording to save records.
     */
    public void startContextManagerMainThread() {
        mScheduledExecutorService.scheduleAtFixedRate(
                ContextManagerRunnable,
                BACKGROUND_RECORDING_INITIAL_DELAY,
                CONTEXT_MANAGER_REFRESH_FREQUENCY,
                TimeUnit.SECONDS);
    }

    public void stopContextManagerMainThread() {
       // mScheduledExecutorService.shutdown();
    }

    Runnable ContextManagerRunnable = new Runnable() {
        @Override
        public void run() {
            try{

                Log.d(LOG_TAG, "Context Manager beginning");

                /** test transporation : feed datain to the datapool**/

            /*
                    if (testActivityRecordIndex<TransportationModeManager.getActivityRecords().size()){
                        Log.d(LOG_TAG, "[testing transportation] Feed the " + testActivityRecordIndex + " record :"
                        + TransportationModeManager.getActivityRecords().get(testActivityRecordIndex).getProbableActivities()
                        + TransportationModeManager.getActivityNameFromType(TransportationModeManager.getActivityRecords().get(testActivityRecordIndex).getProbableActivities().get(0).getType())  );

                        ContextExtractor.setProbableActivities(
                                TransportationModeManager.getActivityRecords().get(testActivityRecordIndex).getProbableActivities(),
                                TransportationModeManager.getActivityRecords().get(testActivityRecordIndex).getTimestamp());
                    }

                    testActivityRecordIndex+=1;
*/

                //Recording is one of the types of actions that users need to put into the configuration.
                //However, now we want to enable background recording so that we can monitor events.
                //eventually. If researachers do not monitor anything, this flag should be false.
                if (sIsBackgroundRecordingEnabled){
                    DataHandler.SaveRecordsToLocalDatabase(ContextManager.getRecordPool(), Constants.BACKGOUND_RECORDING_SESSION_ID);
                }


                /** update transportation mode. Transporation Manager will use the latet activity label
                 * saved in the ActivityRecognitionManager to infer the user's current transportation mode
                 * **/

                int transportationMode= mTransportationModeManager.examineTransportation();
                Log.d(LOG_TAG, "[examineTransportation] the transportation mdoe is "
                        + TransportationModeManager.getActivityNameFromType(transportationMode));


                /** after the transportationModeManager generate a transportation label, we update Mobility
                 * of the user. The mobility information, right now,  will be used to control the
                 * frequency of location udpate to save battery life***/
                MobilityManager.updateMobility();


            }catch (IllegalArgumentException e){
                //Log.e(LOG_TAG, "Could not unregister receiver " + e.getMessage()+"");
            }

        }
    };


    public static String getSensorTypeName(int recordType){

        switch(recordType){

            case CONTEXT_RECORD_TYPE_LOCATION:
                return CONTEXT_RECORD_TYPE_LOCATION_NAME;
            case CONTEXT_RECORD_TYPE_ACTIVITY:
                return CONTEXT_RECORD_TYPE_ACTIVITY_NAME;
            case CONTEXT_RECORD_TYPE_SENSOR:
                return CONTEXT_RECORD_TYPE_SENSOR_NAME;
            case CONTEXT_RECORD_TYPE_GEOFENCE:
                return CONTEXT_RECORD_TYPE_SENSOR_NAME;
            case CONTEXT_RECORD_TYPE_APPLICATION_ACTIVITY:
                return CONTEXT_RECORD_TYPE_APPLICATION_ACTIVITY_NAME;
        }
        return "unknown";
    }



    /**get the current time in milliseconds**/
    public static long getCurrentTimeInMillis(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        long t = cal.getTimeInMillis();
        return t;
    }

    /**get the current time in string (in the format of "yyyy-MM-dd HH:mm:ss" **/
    public static String getCurrentTimeString(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        String currentTimeString = sdf_now.format(cal.getTime());

        return currentTimeString;
    }


}
