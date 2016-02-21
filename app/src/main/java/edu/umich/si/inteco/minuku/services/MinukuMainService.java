package edu.umich.si.inteco.minuku.services;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.AnalyticsMinuku;
import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.TransportationModeManager;
import edu.umich.si.inteco.minuku.context.EventManager;
import edu.umich.si.inteco.minuku.data.DataHandler;
import edu.umich.si.inteco.minuku.data.LocalDBHelper;
import edu.umich.si.inteco.minuku.data.RemoteDBHelper;
import edu.umich.si.inteco.minuku.model.Checkpoint;
import edu.umich.si.inteco.minuku.model.actions.Action;
import edu.umich.si.inteco.minuku.receivers.BatteryStatusReceiver;
import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.BatteryHelper;
import edu.umich.si.inteco.minuku.util.ConfigurationManager;
import edu.umich.si.inteco.minuku.util.FileHelper;
import edu.umich.si.inteco.minuku.util.GooglePlayServiceUtil;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.NotificationHelper;
import edu.umich.si.inteco.minuku.util.PreferenceHelper;
import edu.umich.si.inteco.minuku.util.QuestionnaireManager;
import edu.umich.si.inteco.minuku.util.RecordingAndAnnotateManager;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;
import edu.umich.si.inteco.minuku.util.TaskManager;
import edu.umich.si.inteco.minuku.util.TriggerManager;

public class MinukuMainService extends Service {

    private static MinukuMainService serviceInstance = null;

    /** Tag for logging. */
    private static final String LOG_TAG = "MinukuMainService";

    //Google Analytic
    private static Tracker mTracker;

    /**
     *
     * Configurable parameters
     *
     * **/

    /**Context Manager in General*/
    //the frequency of storing the cached records

    public static int DEFAULT_DEVICE_CHECKING_COUNT = 120;//5 minutes. it counts down for every 5 seconds

    public static int DEFAULT_LOCATION_CHECKING_COUNT = 120;//half an hour. it counts down for every 5 seconds

    public static int DEFAULT_ACTION_RATE_INTERVAL_IN_SECONDS = 5;

    public static long DEFAULT_ACTION_RATE_INTERVAL =
            DEFAULT_ACTION_RATE_INTERVAL_IN_SECONDS * Constants.MILLISECONDS_PER_SECOND;

    public static long DEFAULT_APP_MONITOR_RATE_INTERVAL = DEFAULT_ACTION_RATE_INTERVAL;


    /**Google Play (Location & Activity) Services*/
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    /**Supported Context Source number (for the Record use)**/



    /**Context Records**/
    // each Record object is uniqute to the ContextExtractor. The ContextExtractor updates each of the Record based on the RecordFrequency

    /**Handle repeating recoridng**/
    private Handler mRecordingHandler;

    private static TriggerManager mTriggerManager;

    private static ContextManager mContextManager;

    private static ConfigurationManager mConfigurationManager;

    private static NotificationHelper mNotificationHelper ;

    /**Data Handler**/
    private static DataHandler mDataHandler;

    /**Record Annottion Manager**/
    private static RecordingAndAnnotateManager mRecordingAndAnnotateManager;

    /**Task Manager**/
    private static TaskManager mTaskManager;

    /**Action Manager**/
    private static ActionManager mActionManager;

    /**Schedule Manager**/
    private static ScheduleAndSampleManager mScheduleAndAlarmManager;

    /**Questionnaire Manager**/
    private static QuestionnaireManager mQuestionnaireManager;

    /** File Helper**/
    private static FileHelper mFileHelper;

    //handle the local SQLite operation
    private static LocalDBHelper mLocalDBHelpder;

    /**Even Monitor**/
    private static EventManager mEventManager;

    private static PreferenceHelper mPreferenceHelper;

    private static Handler mMainThread;

    private static int mDeviceCheckingTimeCountDown = DEFAULT_DEVICE_CHECKING_COUNT ;
    private static int mDeviceLocationCheckingTimeCountDown = DEFAULT_LOCATION_CHECKING_COUNT ;


    private static long baseForChronometer =0;
    private static boolean mCentralChrometerRunning;
    private static boolean mCentralChrometerPaused;
    private static String mCentralChrometerText="00:00:00";
    private static long mTimeWhenStopped = 0;

    //for testing checkpoint. this variable remember information of last checkpoint.
    public static Checkpoint mPreviousCheckpoint;


    public static boolean isServiceRunning() {
        return serviceInstance != null;
    }//met


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {


        LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                LogManager.LOG_TAG_SERVICE,
                "Service onCreate");

        super.onCreate();


        //this is for checking if the service instance is running
        serviceInstance=this;

        mPreferenceHelper = new PreferenceHelper(this);
        Log.d(LOG_TAG, "going to create the probe service");

        //initiate the utiliy classes
        mNotificationHelper = new NotificationHelper(this);


        /** We're going to set the DeviceID for the phone; To not collect PII, we create a user id that hashes a combined string of
         * the timestamp and a deviceID**/
        String phoneUID = "";

        Log.d(LOG_TAG, "[test permission] check preference device ID " + PreferenceHelper.getPreferenceString(PreferenceHelper.DEVICE_ID, "NA") ) ;

        //we've not got a Device_ID.
        if ( PreferenceHelper.getPreferenceString(PreferenceHelper.DEVICE_ID, "NA").equals("NA")) {
            //so get it
            Log.d(LOG_TAG, "[test permission] we've not got a deviceID ");
            getDeviceID();
        }
        //we've got one. so use the stored one
        else {

            Constants.DEVICE_ID = PreferenceHelper.getPreferenceString(PreferenceHelper.DEVICE_ID, "NA");
            Constants.USER_ID = PreferenceHelper.getPreferenceString(PreferenceHelper.USER_ID, "NA");
            Log.d(LOG_TAG, "[test permission] we've got a device id " + Constants.DEVICE_ID + " user id: " + Constants.USER_ID);
        }

        /** Google Analytic. Use the ID to track on Google Analytic **/
        // [START shared_tracker]
        AnalyticsMinuku application = (AnalyticsMinuku) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.set("&uid", Constants.USER_ID);
        // [END shared_tracker]

        //clean up the database for renewing configurations.
        //TODO: THis IS TEMPORARILY! After we have a remote database and use it to query configurations, we should kill this line.

        cleanUpDatabaseBeforeWeCanUpdateConfiguration();

        //handling trigger objects and relationship
        mTriggerManager = new TriggerManager(this);

        mContextManager = new ContextManager(this);


        mLocalDBHelpder = new LocalDBHelper(this, Constants.TEST_DATABASE_NAME);

        //this line is required to create the table.
        mLocalDBHelpder.getWritableDatabase();

        //set up the environment for background recording

        mFileHelper = new FileHelper (this);

        //initiate the TaskManager and load the tasks
        mTaskManager = new TaskManager(this);

        //initiate the event monitor
        mEventManager = new EventManager(this);

        //initiate the Action Manager
        mActionManager = new ActionManager(this, mContextManager);

        //initiate the schedule Manager
        mScheduleAndAlarmManager = new ScheduleAndSampleManager(this);

        //initiate the Questionnaire Manager
        mQuestionnaireManager = new QuestionnaireManager(this);

        mConfigurationManager = new ConfigurationManager(this, mContextManager);

        //initiate the Record and Annotation Manager
        mRecordingAndAnnotateManager = new RecordingAndAnnotateManager(this);

        //initiate the DataHandler
        mDataHandler = new DataHandler (this);


    }


    /**
     * after level 23 we need to request permission at run time. So Minuku currently doesn't support Android 6!
     * http://developer.android.com/training/permissions/requesting.html
     */
    private void getDeviceID() {

        Log.d(LOG_TAG, "[test permission] we're attempting to get deviceID ");

        /** when the app starts, first obtain the participant ID **/
        TelephonyManager mngr = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);

        //check if device ID is granted the permission
        int permissionStatus= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        //if we did not get permission yet, we need to call a notification to ask users to give permission
        if (permissionStatus!= PackageManager.PERMISSION_GRANTED) {

            sendNotification();

            NotificationHelper.createPermissionRequestNotificaiton(
                    //permission
                    Manifest.permission.READ_PHONE_STATE,
                    //request code
                    Constants.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE,
                    //title
                    NotificationHelper.NOTIFICATION_TITLE_ASK_FOR_PERMISSION,
                    //message
                    NotificationHelper.NOTIFICATION_MESSAGE_ASK_FOR_PERMISSION
                    );
        }
        else {
            Constants.DEVICE_ID = mngr.getDeviceId();

            //combined device id, timestamp, and minuku
            Constants.USER_ID = (Constants.MINUKU_PREFIX + (ContextManager.getCurrentTimeInMillis() + Constants.DEVICE_ID).hashCode());

            //TODO: create user loggin and use that as the id.
            // add a unixtime to it and then hash the whole string. The purpose is try to create an unidentifiable user id

            Log.d(LOG_TAG, "[test permission] get the synTime is " + Constants.DEVICE_ID);

            PreferenceHelper.setPreferenceValue(PreferenceHelper.DEVICE_ID, Constants.DEVICE_ID);
            PreferenceHelper.setPreferenceValue(PreferenceHelper.USER_ID, Constants.USER_ID);

            Log.d(LOG_TAG, "[test permission] already set device ID " + PreferenceHelper.getPreferenceString(PreferenceHelper.DEVICE_ID, "NA")) ;
            Log.d(LOG_TAG, "[test permission] already set user ID " + PreferenceHelper.getPreferenceString(PreferenceHelper.USER_ID, "NA")) ;


        }
    }



    /**called when the service is started **/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

//        FileHelper.readTestFile();

        sendNotification();

        LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                LogManager.LOG_TAG_SERVICE,
                "Service onStartCommand");

        Log.d(LOG_TAG, "[test service running] going to start the probe service, isServiceRunning:  " + isServiceRunning());

        /**we first cancel all notifications and alarms that are left **/

        //cancel scheduled notification
        NotificationHelper.getNotificationManager().cancelAll();
        //cancel all scheduled action alarms
        ScheduleAndSampleManager.cancelAllActionAlarms();

        //cancel scheduled notification
        NotificationHelper.getNotificationManager().cancelAll();

        //then we setup TriggerLinks. This include connecting triggers
        //and triggered ProbeObjects. This will help the service to find the triggered action when a trigger is fired.
        TriggerManager.setUpTriggerLinks();

        startMinukuService();

        //register receiver
        registerAlarmReceivers();


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed

        LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                LogManager.LOG_TAG_SERVICE,
                "Service onDestroy");

        serviceInstance = null;

        Log.d(LOG_TAG, "[test service running]  going to cancel the probe service isServiceRunning:" + isServiceRunning());

        //cancel scheduled notification
        NotificationHelper.getNotificationManager().cancelAll();

        //cancel all scheduled action alarms
        ScheduleAndSampleManager.cancelAllActionAlarms();
        ScheduleAndSampleManager.unregisterAlarmReceivers();

//        //unregister receivers
//        unregisterReceiver(mBatteryStatusReceiver);

        //stop main thread
        getMainThread().removeCallbacks(mMainThreadrunnable);

        //stop background recording
        mContextManager.stopContextManagerMainThread();

        super.onDestroy();
    }


    private void registerAlarmReceivers() {

//        IntentFilter batteryStatus_filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//        registerReceiver(mBatteryStatusReceiver, batteryStatus_filter);

    }


    /**
     *
     * This function is called in two situations:
     * 1. When the app is first setup
     * 2.
     *
     */


    private void cleanUpDatabaseBeforeWeCanUpdateConfiguration() {

        ArrayList<String> res = LocalDBHelper.queryConfigurations();
        Log.d(LOG_TAG, "[cleanUpDatabaseBeforeWeCanUpdateConfiguration] there are " + res.size() + " configurations in the database");

        //for the labeling study, if there are existing configurations, we remove it and insert new ones...
        //TODO: remove this part after we can update the configuration from the database...
        if (res.size()>0) {

            Log.d(LOG_TAG, "[cleanUpDatabaseBeforeWeCanUpdateConfiguration] Clean tasks, configurations and questionnaire" );
            //remove the configurations, questionnaires, and tasks, which are stored in the databse.
            LocalDBHelper.removeQuestionnaires();
            LocalDBHelper.removeTasks();
            LocalDBHelper.removeConfigurations();

            //validate...
            res = LocalDBHelper.queryConfigurations();
       //     Log.d(LOG_TAG, "[cleanUpDatabaseBeforeWeCanUpdateConfiguration] there are " + res.size() + " configurations in the database");
            res = LocalDBHelper.queryTasks();
         //   Log.d(LOG_TAG, "[cleanUpDatabaseBeforeWeCanUpdateConfiguration] there are " + res.size() + " configurations in the database");

        }

    }

    /**
     *
     */
    private void updateConfigurations() {

    }


    //TODO: fix this
    public void startMinukuService(){

        Log.d(LOG_TAG, "[startMinukuService] star the probe service");
        //TODO: checking updated configurations

        //start the main functions of ContextManager
        mContextManager.startContextManager();

        //register actions that should launch when the service starts, and schedule actions that need to be scheduled.
        mActionManager.registerActionControls();

        //running mainthread for running continuous action
        runMainThread();
        //ActionManager.startRunningActionThread();
    }


    /**after the Probe service starts, ActionManager maintain a list of actions that continuously run in the background. Action Manager needs to
     * create threads for these continuoulsy running actions.
     */
    public static void runMainThread (){
        //Log.d(LOG_TAG, " [RUN] : action manager is setting up the main thread" );
        mMainThread = new Handler();

        /**start repeatedly store the extracted contextual information into Record objects**/
        mMainThread.post(mMainThreadrunnable);
    }

    static Runnable mMainThreadrunnable = new Runnable() {
        @Override
        public void run() {


            //the main thread use the default interval to run continuous actions
            //the continuous actions are listed in the RunningActionList maintained by the ActionManager.
            //Periodically the main thread call ActionManager to execute the contibuous actions.
            try{

                Log.d(LOG_TAG, "[test pause resume]  running in  recordContextRunnable  ");

                for (int i=0; i < ActionManager.getRunningActionList().size(); i++){

                    Action action = ActionManager.getRunningActionList().get(i);

                    //if the action is not paused, run the action
                    if (!action.isPaused()){

                        Log.d(LOG_TAG, "[test pause resume]running continuous and non-paused actions " + action.getId() + " " + action.getName() );
                        ActionManager.executeAction(action);
                    }
                }


                /***if we need to check whether the device is alive**/
                if (ConfigurationManager.MINUKU_SERVICE_CHECKIN_ENABLED) {

                    if (mDeviceCheckingTimeCountDown==0 ){

                        //if we want to use Google Analytic to check alive status
                        if (ConfigurationManager.MINUKU_SERVICE_CHECKIN_GOOGLE_ANALYTICS_ENABLED){
                            //If Google Analytic Tracker is enabled, we periodically send data to the
                            mTracker.send(new HitBuilders.EventBuilder()
                                    .setCategory(Constants.MINUKU_SERVICE_CHECKING_ISALIVE)
                                    .setAction(Constants.USER_ID)
                                    .setLabel(Constants.USER_ID)
                                    .build());
                        }

                        //if we want to use remote MongoDB to check alive status
                        if(ConfigurationManager.MINUKU_SERVICE_CHECKIN_MONGODB_ENABLED) {
                            RemoteDBHelper.MinukuServiceCheckIn();
                        }

                        //reset
                        mDeviceCheckingTimeCountDown=DEFAULT_DEVICE_CHECKING_COUNT ;

                    }
                    else {
                        //count down
                        mDeviceCheckingTimeCountDown-=1;
                    }

                }



            }catch (IllegalArgumentException e){
                //Log.e(LOG_TAG, "Could not unregister receiver " + e.getMessage()+"");
            }
            // then send data to the datahandler
            mMainThread.postDelayed(this, MinukuMainService.DEFAULT_ACTION_RATE_INTERVAL);
        }
    };


/*
    private void startBackgroundRecording(){

        mRecordingHandler = new Handler();

        Runnable recordContext = new Runnable() {
            @Override
            public void run() {
                try{

                    //1. If the ContextExtractor is extracing context information, store context information to the SensorRecorde
                    if (mContextExtractor.isExtractingContext() ){

                        //write the temporarily stored records into files or databases
                        //mDataHandler.WriteRecordsToFile(mRecordPool);


                        //TODO: make writing to database an Action
                        mDataHandler.SaveRecordsToLocalDatabase(ContextManager.getPublicRecordPool(), Constants.BACKGOUND_LOGGING_SESSION_ID);

                        //after writing the records into files or databases, clear the record pools
                        //ContextManager.getPublicRecordPool().clear();
                    }

                    //send recrod to DataHandler

                }catch (IllegalArgumentException e){
                    //Log.e(LOG_TAG, "Could not unregister receiver " + e.getMessage()+"");
                }
                // then send data to the datahandler
                mRecordingHandler.postDelayed(this, MinukuMainService.CONTEXT_RECORD_INTERVAL);
            }
        };

        //start repeatedly store the extracted contextual information into Record objects
        mRecordingHandler.post(recordContext);

    }
*/

    /**for testing*/


    public static Handler getMainThread() {return mMainThread;}

    public static long getTimeWhenStopped() {
        return mTimeWhenStopped;
    }

    public static void setTimeWhenStopped(long timeWhenStopped) {
        MinukuMainService.mTimeWhenStopped = timeWhenStopped;
    }

    public static boolean isCentralChrometerPaused() {
        return mCentralChrometerPaused;
    }

    public static void setCentralChrometerPaused(boolean centralChrometerPaused) {
        MinukuMainService.mCentralChrometerPaused = centralChrometerPaused;
    }

    public static boolean isCentralChrometerRunning() {
        return mCentralChrometerRunning;
    }

    public static void setCentralChrometerRunning(boolean centralChrometerRunning) {
        MinukuMainService.mCentralChrometerRunning = centralChrometerRunning;
    }

    public static String getCentralChrometerText() {
        return mCentralChrometerText;
    }

    public static void setCentralChrometerText(String centralChrometerText) {
        MinukuMainService.mCentralChrometerText = centralChrometerText;
    }

    //maintain the base of the Chronemeter
    public static long getBaseForChronometer() {
        return baseForChronometer;
    }

    public static void setBaseForChronometer(long baseForChronometer) {
        MinukuMainService.baseForChronometer = baseForChronometer;
    }

    public static Checkpoint getPreviousCheckpoint() {
        return mPreviousCheckpoint;
    }

    public static void setPreviousCheckpoint(Checkpoint checkpoint) {
        MinukuMainService.mPreviousCheckpoint = checkpoint;
    }

    private void sendNotification() {

        // Create a notification builder that's compatible with platforms >= version 4
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this);


        String message = "NA";
        message = "test";

        // Set the title, text, and icon
        builder.setContentTitle("mobility")
                .setContentText( message)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        // Get the Intent that starts the Location settings panel
                .setContentIntent(getContentIntent());

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(9998, builder.build());
    }

    /**
     * Get a content Intent for the notification
     */
    private PendingIntent getContentIntent() {

        // Set the Intent action to open Location Settings
        Intent activityIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        // Create a PendingIntent to start an Activity
        return PendingIntent.getService(this.getApplicationContext(),
                576767,
                activityIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }
}