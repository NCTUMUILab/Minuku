package edu.umich.si.inteco.minuku.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.GlobalNames;
import edu.umich.si.inteco.minuku.contextmanager.ContextExtractor;
import edu.umich.si.inteco.minuku.contextmanager.ContextManager;
import edu.umich.si.inteco.minuku.contextmanager.EventManager;
import edu.umich.si.inteco.minuku.data.DataHandler;
import edu.umich.si.inteco.minuku.data.LocalDBHelper;
import edu.umich.si.inteco.minuku.data.RemoteDBHelper;
import edu.umich.si.inteco.minuku.model.actions.Action;
import edu.umich.si.inteco.minuku.model.record.SensorRecord;
import edu.umich.si.inteco.minuku.receivers.BatteryStatusReceiver;
import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.BatteryHelper;
import edu.umich.si.inteco.minuku.util.ConfigurationManager;
import edu.umich.si.inteco.minuku.util.FileHelper;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.MobilityManager;
import edu.umich.si.inteco.minuku.util.NotificationHelper;
import edu.umich.si.inteco.minuku.util.PreferenceHelper;
import edu.umich.si.inteco.minuku.util.QuestionnaireManager;
import edu.umich.si.inteco.minuku.util.RecordingAndAnnotateManager;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;
import edu.umich.si.inteco.minuku.util.TaskManager;
import edu.umich.si.inteco.minuku.util.TriggerManager;

public class CaptureProbeService extends Service {

    private static CaptureProbeService serviceInstance = null;

    /** Tag for logging. */
    private static final String LOG_TAG = "CaptureProbeService";


    /**
     *
     * Configurable parameters
     *
     * **/

    /**Context Manager in General*/
    //the frequency of storing the cached records

    public static int DEFAULT_DEVICE_CHECKING_COUNT = 360;//half an hour. it counts down for every 5 seconds

    public static int DEFAULT_LOCATION_CHECKING_COUNT = 120;//half an hour. it counts down for every 5 seconds

    public static int DEFAULT_ACTION_RATE_INTERVAL_IN_SECONDS = 5;

    public static int DEFAULT_ACTION_RATE_INTERVAL =
            DEFAULT_ACTION_RATE_INTERVAL_IN_SECONDS * GlobalNames.MILLISECONDS_PER_SECOND;

    public static int DEFAULT_APP_MONITOR_RATE_INTERVAL = DEFAULT_ACTION_RATE_INTERVAL;


    /**Google Play (Location & Activity) Services*/
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    //the frequency of requesting google activity from the google play service
    public static int ACTIVITY_RECOGNITION_UPDATE_INTERVAL_IN_SECONDS = 30;

    public static int ACTIVITY_RECOGNITION_UPDATE_INTERVAL =
            ACTIVITY_RECOGNITION_UPDATE_INTERVAL_IN_SECONDS * GlobalNames.MILLISECONDS_PER_SECOND;



    /**Supported Context Source number (for the Record use)**/



    /**Context Records**/
    // each Record object is uniqute to the ContextExtractor. The ContextExtractor updates each of the Record based on the RecordFrequency
    private SensorRecord mSensorRecord;

    /**Handle repeating recoridng**/
    private Handler mRecordingHandler;

    /**Context Extractor**/
    private static ContextExtractor mContextExtractor;

    private static TriggerManager mTriggerManager;

    private static ContextManager mContextManager;

    private static ConfigurationManager mConfigurationManager;

    private static NotificationHelper mNotificationHelper ;

    private static MobilityManager mMobilityManager;

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

    private static BatteryHelper mBatteryHelper;

    private static Handler mMainThread;

    private static BatteryStatusReceiver mBatteryStatusReceiver;

    private static int mDeviceCheckingTimeCountDown = DEFAULT_DEVICE_CHECKING_COUNT ;
    private static int mDeviceLocationCheckingTimeCountDown = DEFAULT_LOCATION_CHECKING_COUNT ;


    private static long baseForChronometer =0;
    private static boolean mCentralChrometerRunning;
    private static boolean mCentralChrometerPaused;
    private static String mCentralChrometerText="00:00:00";
    private static long mTimeWhenStopped = 0;


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

        //if device id is not set yet, set device id to the shared prefernece
        if ( PreferenceHelper.getPreferenceString(PreferenceHelper.SHARED_PREFERENCE_PROPERTY_DEVICE_ID, "NA").equals("NA")) {
            TelephonyManager mngr = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);
            GlobalNames.DEVICE_ID = mngr.getDeviceId();
            PreferenceHelper.setPreferenceValue(PreferenceHelper.SHARED_PREFERENCE_PROPERTY_DEVICE_ID, mngr.getDeviceId());
        }
        else {
            GlobalNames.DEVICE_ID = PreferenceHelper.getPreferenceString(PreferenceHelper.SHARED_PREFERENCE_PROPERTY_DEVICE_ID, "NA");
        }

        mLocalDBHelpder = new LocalDBHelper(this, GlobalNames.TEST_DATABASE_NAME);

        //this line is required to create the table.
        mLocalDBHelpder.getWritableDatabase();

        //clean up the database for renewing configurations.
        //TODO: THis IS TEMPORARILY! After we have a remote database and use it to query configurations, we should kill this line.

        cleanUpDatabaseBeforeWeCanUpdateConfiguration();

        //handling trigger objects and relationship
        mTriggerManager = new TriggerManager(this);

        mContextManager = new ContextManager(this);
        //set up the environment for background recording

        mFileHelper = new FileHelper (this);

        //initiate the Record and Annotation Manager
        mRecordingAndAnnotateManager = new RecordingAndAnnotateManager(this);

        //initiate the TaskManager and load the tasks
        mTaskManager = new TaskManager(this);

        //initiate the event monitor
        mEventManager = new EventManager(this);

        //initiate the Action Manager
        mActionManager = new ActionManager(this);

        //initiate the utiliy classes
        mNotificationHelper = new NotificationHelper(this);

        //initiate the schedule Manager
        mScheduleAndAlarmManager = new ScheduleAndSampleManager(this);

        //initiate the Context Manager
        mContextManager = new ContextManager(this);

        //initiate the Questionnaire Manager
        mQuestionnaireManager = new QuestionnaireManager(this);
        //initiate the ContextManager
        mContextExtractor = new ContextExtractor (this);

        mMobilityManager = new MobilityManager(this);

        mConfigurationManager = new ConfigurationManager(this);

        //initiate the DataHandler
        mDataHandler = new DataHandler (this);

        mBatteryHelper = new BatteryHelper(this);

        mBatteryStatusReceiver = new BatteryStatusReceiver();

    }




    /**called when the service is started **/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        FileHelper.readTestFile();

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

        //register actions that should launch when the service starts, and schedule actions that need to be scheduled.
        mActionManager.registerActionControls();

        //When the ContextManager service starts, by default the ContextExtractor starts to run and extract context.
        startProbeService();

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

        //unregister receivers
        unregisterReceiver(mBatteryStatusReceiver);


        //stop getting context information
        mContextExtractor.stopExtractingContext();

        //stop main thread
        getMainThread().removeCallbacks(mMainThreadrunnable);

        //stop background recording
        ContextManager.stopBackgroundRecordingThread();

        super.onDestroy();
    }


    private void registerAlarmReceivers() {

        IntentFilter batteryStatus_filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryStatusReceiver, batteryStatus_filter);

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


    public void startProbeService(){

        Log.d(LOG_TAG, "[testing start service]  [startProbeService] star the probe service");

        ContextManager.isContextExtractorEnabled = true;
        if (!mContextExtractor.isExtractingContext() && ContextManager.isSavingRecordingDefault){

            //TODO: the contextextractor register sensors in this function
            mContextExtractor.startExtractingContext();

            //Recording is one of the types of actions that users need to put into the configuration.
            //However, now we want to enable background recording so that we can monitor events.
            //eventually. If researachers do not monitor anything, this flag should be false.
            if (GlobalNames.isBackgroundRecordingEnabled);
                ContextManager.startBackgroundRecordingThread();

        }
        //TODO: checking updated configurations


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

                //Log.d(LOG_TAG, "[test pause resume] examineTransportation running in  recordContextRunnable  ");

                for (int i=0; i < ActionManager.getRunningActionList().size(); i++){

                    Action action = ActionManager.getRunningActionList().get(i);

                    //if the action is not paused, run the action
                    if (!action.isPaused()){

               //         Log.d(LOG_TAG, "[test pause resume] examineTransportation running continuous and non-paused actions " + action.getId() + " " + action.getName() );
                        ActionManager.executeAction(action);
                    }
                }
                

              //  Log.d(LOG_TAG, "the deviceChecking countdown is " +mDeviceCheckingTimeCountDown );
                //device checking count down.
                if (mDeviceCheckingTimeCountDown==0){
                    RemoteDBHelper.deviceChecking();
                    mDeviceCheckingTimeCountDown=DEFAULT_DEVICE_CHECKING_COUNT ;
                }
                else {
                    mDeviceCheckingTimeCountDown-=1;
                }

       //         Log.d(LOG_TAG, "in [runMainThread][testgetlocation] : countdown " +  mDeviceLocationCheckingTimeCountDown + " there are " + RecordingAndAnnotateManager.getCurRecordingSessions().size() + "recording");
                ///device get location

                //we only request location ocasstionally when there's no current recording
                /*
                if (RecordingAndAnnotateManager.getCurRecordingSessions().size()==1){
                    //get location
                    if (mDeviceLocationCheckingTimeCountDown==0){

                        ContextExtractor.getLocationRequester().requestUpdates();
                        mDeviceLocationCheckingTimeCountDown=DEFAULT_LOCATION_CHECKING_COUNT ;

                    }
                    //and disconnet in the next 5 second
                    else if (mDeviceLocationCheckingTimeCountDown==12) {
                        //if there's no current session recording, then we can remove

                        ContextExtractor.getLocationRequester().removeUpdate();
                        mDeviceLocationCheckingTimeCountDown-=1;
                    }
                    else {
                        mDeviceLocationCheckingTimeCountDown-=1;
                    }

                }
*/



            }catch (IllegalArgumentException e){
                //Log.e(LOG_TAG, "Could not unregister receiver " + e.getMessage()+"");
            }
            // then send data to the datahandler
            mMainThread.postDelayed(this, CaptureProbeService.DEFAULT_ACTION_RATE_INTERVAL);
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
                        mDataHandler.SaveRecordsToLocalDatabase(ContextManager.getRecordPool(), GlobalNames.BACKGOUND_RECORDING_SESSION_ID);

                        //after writing the records into files or databases, clear the record pools
                        //ContextManager.getRecordPool().clear();
                    }

                    //send recrod to DataHandler

                }catch (IllegalArgumentException e){
                    //Log.e(LOG_TAG, "Could not unregister receiver " + e.getMessage()+"");
                }
                // then send data to the datahandler
                mRecordingHandler.postDelayed(this, CaptureProbeService.CONTEXT_RECORD_INTERVAL);
            }
        };

        //start repeatedly store the extracted contextual information into Record objects
        mRecordingHandler.post(recordContext);

    }
*/

    public static Handler getMainThread() {return mMainThread;}

    public static long getTimeWhenStopped() {
        return mTimeWhenStopped;
    }

    public static void setTimeWhenStopped(long timeWhenStopped) {
        CaptureProbeService.mTimeWhenStopped = timeWhenStopped;
    }

    public static boolean isCentralChrometerPaused() {
        return mCentralChrometerPaused;
    }

    public static void setCentralChrometerPaused(boolean centralChrometerPaused) {
        CaptureProbeService.mCentralChrometerPaused = centralChrometerPaused;
    }

    public static boolean isCentralChrometerRunning() {
        return mCentralChrometerRunning;
    }

    public static void setCentralChrometerRunning(boolean centralChrometerRunning) {
        CaptureProbeService.mCentralChrometerRunning = centralChrometerRunning;
    }

    public static String getCentralChrometerText() {
        return mCentralChrometerText;
    }

    public static void setCentralChrometerText(String centralChrometerText) {
        CaptureProbeService.mCentralChrometerText = centralChrometerText;
    }

    //maintain the base of the Chronemeter
    public static long getBaseForChronometer() {
        return baseForChronometer;
    }

    public static void setBaseForChronometer(long baseForChronometer) {
        CaptureProbeService.baseForChronometer = baseForChronometer;
    }


}