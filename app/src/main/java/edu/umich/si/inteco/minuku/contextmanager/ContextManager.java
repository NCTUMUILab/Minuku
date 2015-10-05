package edu.umich.si.inteco.minuku.contextmanager;

import android.content.Context;

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

public class ContextManager {

    private static final String LOG_TAG = "ContextManager";

    //whether we want a background recording
	public static boolean isSavingRecordingDefault = true;

    //flag of whether ContextManager needs to extractContext
    protected static boolean isExtractingContext = true;

    //flag of whether ContextManager has paused extracting context
//    protected static boolean hasStoppedExtractingContext = false;

    //mContext is MinukuService
	private Context mContext;

    private static final long BACKGROUND_RECORDING_INITIAL_DELAY = 0;
    private static final long BACKGROUND_RECORDING_INTERVAL_IN_SECONDS = 5 ;


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
    // The activity recognition update request object
    private ActivityRecognitionRequester mActivityRecognitionRequester;
    // The activity recognition update removal object
    private ActivityRecognitionRemover mActivityRecognitionRemover;
    // The geofence update request object
    private GeofenceManager mGeofenceManager;
    // the location update manager
    private LocationManager mLocationManager;

    //the manager that manages the status of the phone (network, battery)
    private PhoneStatusManager  mPhoneStatusManager;

    private PhoneActivityManager mPhoneActivityManager;

    private MobilityManager mMobilityManager;

    private ScheduledExecutorService mScheduledExecutorService;

	public ContextManager(Context context){

		mContext = context;

		mLocalDBHelpder = new LocalDBHelper(mContext, Constants.TEST_DATABASE_NAME);
        //initiate the RecordPool
        mRecordPool = new ArrayList<Record>();

        mScheduledExecutorService = Executors.newScheduledThreadPool(5);

        RECORD_TYPE_LIST = new ArrayList<Integer>();
        
        //add the record types into the list
        initiateRecordTypeList();

        //initiate Context Source Manager
        mLocationManager = new LocationManager(mContext);

        mPhoneStatusManager = new PhoneStatusManager(mContext);

        mPhoneActivityManager = new PhoneActivityManager(mContext);

        mMobilityManager = new MobilityManager(mContext, this);
		
	}


    public boolean isExtractingContext() {
        return isExtractingContext;
    }

    public void setExtractingContext(boolean flag) {
        isExtractingContext = flag;
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
            mLocationManager.requestLocationUpdate();
        }
    }


/*
    private void startRequestingLocation(){
        Log.d(LOG_TAG, "[startRequestingLocation] start to request location udpate");

        mLocationRequestType = GooglePlayServiceUtil.LOCATION_REQUEST_TYPE.ADD;

        //check Google Play first
        if (!servicesConnected()) {
            return;
        }

        mLocationManager.requestLocationUpdate();
    }

    private void stopRequestingLocation(){
        Log.d(LOG_TAG, "[stopRequestingActivityRecognition] stop to request location");

        // Check for Google Play services
        if (!servicesConnected()) {
            return;
        }

        //if Google Play service is available, stop the update
        mLocationRequestType= GooglePlayServiceUtil.LOCATION_REQUEST_TYPE.REMOVE;

        // Pass the remove request to the remover object (the intent is the same as the request intent)
        mLocationManager.removeLocationUpdate();

    }
*/

    /**functions called by the ContextManager**/

    /*
    public void startExtractingContext(){

        this.isExtractingContext = true;
        this.hasStoppedExtractingContext = false;

        //registerSensors();

        //get activity information
        startRequestingActivityRecognition();

        startRequestingLocation();

        //get geofence transitions
        startRequestingGeofence();

        //extrating app information
        if (isExtractinAppInfo){
            Log.d(LOG_TAG, "[startExtractingContext] ready to extract app info");
            mAppManager.runMonitoringAppThread();
        }

    }
*/
    /*
    public void stopExtractingContext(){

        Log.d("LOG_TAG", "[stopExtractingContext]");
        this.isExtractingContext = false;
        this.hasStoppedExtractingContext = true;

        //unregister sensors
        unRegisterSensors();

        //remove activity update
        stopRequestingActivityRecognition();

        //remove location update
        stopRequestingLocation();

    }
*/

    /**
     * The function starts a thread to run background recording to save records.
     */
    public void startBackgroundRecordingThread() {

        mScheduledExecutorService.scheduleAtFixedRate(recordContextRunnable,
                BACKGROUND_RECORDING_INITIAL_DELAY,
                BACKGROUND_RECORDING_INTERVAL_IN_SECONDS,
                TimeUnit.SECONDS);
    }

    public void stopBackgroundRecordingThread() {

        mScheduledExecutorService.shutdown();
    }

    static Runnable recordContextRunnable = new Runnable() {
        @Override
        public void run() {
            try{

                /** test transporation : feed datain to the datapool**/

            /*
                    if (testActivityRecordIndex<TransportationModeDetector.getActivityRecords().size()){
                        Log.d(LOG_TAG, "[testing transportation] Feed the " + testActivityRecordIndex + " record :"
                        + TransportationModeDetector.getActivityRecords().get(testActivityRecordIndex).getProbableActivities()
                        + TransportationModeDetector.getActivityNameFromType(TransportationModeDetector.getActivityRecords().get(testActivityRecordIndex).getProbableActivities().get(0).getType())  );

                        ContextExtractor.setProbableActivities(
                                TransportationModeDetector.getActivityRecords().get(testActivityRecordIndex).getProbableActivities(),
                                TransportationModeDetector.getActivityRecords().get(testActivityRecordIndex).getTimestamp());
                    }

                    testActivityRecordIndex+=1;
*/

                //save records to the database
                DataHandler.SaveRecordsToLocalDatabase(ContextManager.getRecordPool(), Constants.BACKGOUND_RECORDING_SESSION_ID);

                //update mobility
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
