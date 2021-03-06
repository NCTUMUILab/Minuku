package edu.umich.si.inteco.minuku.context;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.ActivityRecognitionManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.ContextStateManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.LocationManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.PhoneSensorManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.PhoneStatusManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.TransportationModeManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.UserInteractionManager;
import edu.umich.si.inteco.minuku.data.DataHandler;
import edu.umich.si.inteco.minuku.model.BackgroundLoggingSetting;
import edu.umich.si.inteco.minuku.model.Record.LocationRecord;
import edu.umich.si.inteco.minuku.model.Situation;
import edu.umich.si.inteco.minuku.model.Condition;
import edu.umich.si.inteco.minuku.model.LoggingTask;
import edu.umich.si.inteco.minuku.model.Record.ActivityRecognitionRecord;
import edu.umich.si.inteco.minuku.model.State;
import edu.umich.si.inteco.minuku.model.StateMappingRule;
import edu.umich.si.inteco.minuku.model.Record.Record;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.TriggerManager;

public class ContextManager {

    private static final String LOG_TAG = "ContextManager";

    //whether we want a background recording
    protected static boolean sIsSavingRecordingDefault = true;

    //flag of whether ContextManager is currently extracting Context
    protected static boolean sIsExtractingContext = false;

    //flag of whether ContextManager needs to extractContext
    protected static boolean sIsExtractingContextEnabled = true;

    //flag of whether BackgroundRecording is enabled
    protected static boolean sIsBackgroundLoggingEnabled = false;


    //flag to control whether the data will be saved to a local databases
    protected static boolean sIsSavingDataToLocalDatabase = true;

    //flat to control whether the data will be saved to log
    protected static boolean sIsSavingDataToFileSystem = true;


    //mContext is MinukuService
	private Context mContext;

    public static final int BACKGROUND_RECORDING_INITIAL_DELAY = 0;
    public static final int CONTEXT_MANAGER_REFRESH_FREQUENCY = 5 ;

    public static final int LOCAL_RECORD_POOL_MAX_SIZE = 50;
    public static final int PUBLIC_RECORD_POOL_MAX_SIZE = 500;

    /**RecordPool is a List for temporarily storing records that will be stored into the database or files later**/
    private static ArrayList<Record> mRecordPool;

    //the threshold of life of a record
    public static long RECORD_PRESERVATION_THRESHOLD_IN_MILLISECONDS = 2 *
            Constants.MILLISECONDS_PER_SECOND * Constants.SECONDS_PER_MINUTE;   //2 minutes

    public static final String CONTEXT_STATE_MANAGER_ACTIVITY_RECOGNITION = "ActivityRecognition";
    public static final String CONTEXT_STATE_MANAGER_LOCATION = "Location";
    public static final String CONTEXT_STATE_MANAGER_PHONE_SENSOR = "PhoneSensor";
    public static final String CONTEXT_STATE_MANAGER_PHONE_STATUS = "PhoneStatus";
    public static final String CONTEXT_STATE_MANAGER_TRANSPORTATION = "Transportation";
    public static final String CONTEXT_STATE_MANAGER_USER_INTERACTION = "UserInteraction";

    public static final String CONTEXT_SOURCE_NAME_ACTIVITY_RECOGNITION = "ActivityRecognition";
    public static final String CONTEXT_SOURCE_NAME_ACTIVITY_RECOGNITION_PREFIX = "AR-";
    public static final String CONTEXT_SOURCE_NAME_TRANSPORTATION = "Transportation";
    public static final String CONTEXT_SOURCE_NAME_PHONE_STATUS_PREFIX = "PhoneStatus-";
    public static final String CONTEXT_SOURCE_NAME_USER_INTERACTION_PREFIX = "UserInteraction-";
    public static final String CONTEXT_SOURCE_NAME_LOCATION = "Location";
    public static final String CONTEXT_SOURCE_NAME_SENSOR_PREFIX = "Sensor-";


    public static final int CONTEXT_SOURCE_INVALID_VALUE_INTEGER = -9999;
    public static final long CONTEXT_SOURCE_INVALID_VALUE_LONG_INTEGER = -9999;
    public static final long CONTEXT_SOURCE_INVALID_VALUE_FLOAT = -9999;
    public static final String CONTEXT_SOURCE_INVALID_VALUE_STRING = "NA";

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

//TODO: this should be defind in each ContextStateMAnager. Need to replace this.

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

    private static ArrayList<Situation> mSituationList;

    private static ArrayList<StateMappingRule> mStateMappingRuleList;

    private static ArrayList<LoggingTask> mLoggingTaskList;

    //stores a list of ids of LoggingTask that are running by an Action
    private static ArrayList<Integer> mLoggingTaskByActionList;

    private static ArrayList<ContextStateManager> mContextStateMangers;

    private static BackgroundLoggingSetting mBackgroundLoggingSetting;

    private static ActivityRecognitionManager mActivityRecognitionManager;

    //inspect transportation mode of the user
    private static TransportationModeManager mTransportationModeManager;

    private static GeofenceManager mGeofenceManager;

    // the location update manager
    private static LocationManager mLocationManager;

    //the manager that manages the status of the phone (network, battery)
    private static PhoneStatusManager mPhoneStatusManager;

    private static PhoneSensorManager mPhoneSensorManager;

    private static UserInteractionManager mUserInteractionManager;

    private static MobilityManager mMobilityManager;

    private int testCount = 0;

    private final ScheduledExecutorService mScheduledExecutorService;

	public ContextManager(Context context){

		mContext = context;

        mContextStateMangers = new ArrayList<ContextStateManager>();

        mSituationList = new ArrayList<Situation>();

        mStateMappingRuleList  = new ArrayList<StateMappingRule>();

        mLoggingTaskList = new ArrayList<LoggingTask>();

        mLoggingTaskByActionList = new ArrayList<Integer>();

        //initiate the RecordPool
        mRecordPool = new ArrayList<Record>();

        mScheduledExecutorService = Executors.newScheduledThreadPool(CONTEXT_MANAGER_REFRESH_FREQUENCY);

        RECORD_TYPE_LIST = new ArrayList<Integer>();

        mBackgroundLoggingSetting = new BackgroundLoggingSetting();

        //initiate Context Source Managers
        mLocationManager = new LocationManager(mContext);

        mActivityRecognitionManager = new ActivityRecognitionManager(mContext);

        mTransportationModeManager = new TransportationModeManager(mContext, mActivityRecognitionManager);

        mPhoneStatusManager = new PhoneStatusManager(mContext);

        mPhoneSensorManager = new PhoneSensorManager(mContext);

        mUserInteractionManager = new UserInteractionManager(mContext);

        mMobilityManager = new MobilityManager(mContext, this);

        Log.d(LOG_TAG, "test creat tables before add csms");


        /***Add ContextStateManagers**/
        mContextStateMangers.add(mLocationManager);
        mContextStateMangers.add(mActivityRecognitionManager);
        mContextStateMangers.add(mTransportationModeManager);
        mContextStateMangers.add(mPhoneSensorManager);
        mContextStateMangers.add(mPhoneStatusManager);
        mContextStateMangers.add(mUserInteractionManager);


        Log.d(LOG_TAG, "test creat tables after add csms");
	}

    /**
     * we start the main function of ContextManager here: extracting information and monitoring states
     */
    public void startContextManager() {

        Log.d(LOG_TAG, "[startContextManager]");


        /**
         * Setup the task in ContextStateManager, including
         * determining what contextual information to record and monitor.
         * According to the recording and the monitoring task, ContextManager determines which
         * contextual information to extract
         */
        assignTasksToContextStateManager();

        /**ContextManager has the control to start and stop extracting contextual information*/
        if (sIsExtractingContextEnabled) {
            startExtractingContext();
        }

        /**the final task is to run the ContextManager mainthread
         * Here it actively updates contextual information in contextStateManager that
         * pull information from other contextStateManager, e.g. TransportationManager pulls information
         * from ACtivityRecognition Manager.
         * **/
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



    public void requesLocationUpdate () {
        if (mLocationManager!=null){
            Log.d(LOG_TAG, "[startRequestingLocation] start to request location udpate");
            mLocationManager.requestUpdates();
        }
    }

    public void removeLocationUpdate () {
        if (mLocationManager!=null){
            Log.d(LOG_TAG, "[stopRequestingActivityRecognition] stop to request location");
            mLocationManager.removeUpdates();
        }
    }


    /***
     * Requesting and Removing Activity Recognition Service
     */
    private void requestActivityRecognitionUpdate(){
        Log.d(LOG_TAG, "[startRequestingActivityRecognition] start to request activity udpate");
        mActivityRecognitionManager.requestUpdates();
    }

    private void removeActivityRecognitionUpdate(){
        Log.d(LOG_TAG, "[stopRequestingActivityRecognition] stop to request activity udpate");
        //if Google Play service is available, stop the update
        // Pass the remove request to the remover object (the intent is the same as the request intent)
        mActivityRecognitionManager.removeUpdates();
    }

    /***
     * Requesting and Removing Sensor information update
     */
    private void requestSensorUpdate() {
        Log.d(LOG_TAG, "[testing Sensor] start to request sensor udpate");
        mPhoneSensorManager.requestUpdates();
    }

    /**
     * Remove update
     */
    private void removeSensorUpdate() {
        Log.d(LOG_TAG, "[testing Sensor] stop requesting sensor udpate");
        mPhoneSensorManager.removeUpdates();
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
       // requesLocationUpdate();

        //get activity information
        //requestActivityRecognitionUpdate();

        //get sensor information from PhoneSensorManager
        //requestSensorUpdate();


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


    /**
     * according to the source name we go to each ContextStateManager to update each ContextSource.
     * A ContextManager will maintain a list of ContextSouce that it manages to get information.
     * @param source
     * @param samplingRate
     */
    public void configureContextStateSource(String source, long samplingRate) {

        if (source.equals(ContextManager.CONTEXT_SOURCE_NAME_ACTIVITY_RECOGNITION)){
            mActivityRecognitionManager.updateContextSourceList(source, samplingRate);
        }
        else if (source.contains(ContextManager.CONTEXT_SOURCE_NAME_SENSOR_PREFIX)){
            mPhoneSensorManager.updateContextSourceList(source, samplingRate);
        }
        else if (source.equals(ContextManager.CONTEXT_SOURCE_NAME_LOCATION)) {
            mLocationManager.updateContextSourceList(source, samplingRate);;

        }
        else if (source.equals(ContextManager.CONTEXT_SOURCE_NAME_PHONE_STATUS_PREFIX)) {
            mPhoneStatusManager.updateContextSourceList(source, samplingRate);
        }
        else if (source.equals(ContextManager.CONTEXT_SOURCE_NAME_USER_INTERACTION_PREFIX)) {
            mUserInteractionManager.updateContextSourceList(source, samplingRate);
        }
        else if (source.equals(ContextManager.CONTEXT_SOURCE_NAME_TRANSPORTATION)) {
            mTransportationModeManager.updateContextSourceList(source, samplingRate);

        }




    }

    public void configureContextStateSource(String source, String samplingMode) {

        if (source.equals(ContextManager.CONTEXT_SOURCE_NAME_ACTIVITY_RECOGNITION)){
            mActivityRecognitionManager.updateContextSourceList(source, samplingMode);
        }
        else if (source.contains(ContextManager.CONTEXT_SOURCE_NAME_SENSOR_PREFIX)){
            mPhoneSensorManager.updateContextSourceList(source, samplingMode);
        }
        else if (source.equals(ContextManager.CONTEXT_SOURCE_NAME_LOCATION)) {
            mLocationManager.updateContextSourceList(source, samplingMode);

        }
        else if (source.equals(ContextManager.CONTEXT_SOURCE_NAME_PHONE_STATUS_PREFIX)) {
            mPhoneStatusManager.updateContextSourceList(source, samplingMode);

        }
        else if (source.equals(ContextManager.CONTEXT_SOURCE_NAME_USER_INTERACTION_PREFIX)) {
            mUserInteractionManager.updateContextSourceList(source, samplingMode);

        }
        else if (source.equals(ContextManager.CONTEXT_SOURCE_NAME_TRANSPORTATION)) {
            mTransportationModeManager.updateContextSourceList(source, samplingMode);

        }

    }


    /**
     * this function updates the tasks that each ContextStateManager need to perform
     */
    public void assignTasksToContextStateManager() {

        Log.d(LOG_TAG, "[test SMR] situation:  " + getSituationList().size()
         + " loggingTaskssize " + mLoggingTaskList.size());

        /**
         * 1. assign monitoring task to contextStateManagers
         * **/
        for (int i=0; i< getSituationList().size(); i++){

            //creating StateMappingRule and add to the relevant ContextStateManagers
            Situation situation = getSituationList().get(i);

            Log.d(LOG_TAG, "[test SMR] situation:  has "  + situation.getConditionList().size() + " conditions");

           /**find the statemappingrule and assign to contextStateManager the **/
            //get conditions in each situation
            for (int j=0; j< situation.getConditionList().size(); j++) {

                Condition condition = situation.getConditionList().get(j);

                //for each condition, we need to know which ContextStateManager will need to generate a state
                // for that condition.

                Log.d(LOG_TAG, "[test SMR] find contextStatemanger: condition  " + condition.getSource() );

                String contextStateManagerName = getContextStateManagerName(condition.getSource());

                assignMonitoringSituationTask(contextStateManagerName, condition.getStateMappingRule());

            }

        }

        /**2. assign logging task to contextStateManagers **/
        for (int j=0; j<mLoggingTaskList.size(); j++) {

            //get loggingTask
            LoggingTask loggingTask= mLoggingTaskList.get(j);

            //then we find the ContextStateManager for loggintask according to the sourceType
            String contextStateManagerName = getContextStateManagerName(loggingTask.getSource());


            Log.d(LOG_TAG, "[test source being requested] assign loggingtask " + loggingTask.getSource()
                     + " to " +contextStateManagerName);

            //then we add the loggingTask to the right ContextStateManager's active Logging Task
            assignLoggingTask(contextStateManagerName, loggingTask);

        }


        /**3. Assigne BackgroundLoggingTask to ContextStateMananger**/
        //we update the status of the LoggingTask in the ContextStateManager

        //for each loggingTask ids, we first locate the LoggingTask from ContextManager
        for (int m=0; m<getBackgroundLoggingSetting().getLoggingTasks().size(); m++){

            LoggingTask loggingTask= getLoggingTask(getBackgroundLoggingSetting().getLoggingTasks().get(m));

            //then we find the ContextStateManager for the sourceType
            String contextStateManagerName = getContextStateManagerName(loggingTask.getSource());

            //we update the status of the LoggingTask in the ContextStateManager
            updateLoggingTasksInContextStateManager(contextStateManagerName, loggingTask);

        }

    }


    /*******************************************************************************************/
    /************************************** Main Thread ****************************************/
    /*******************************************************************************************/

    public void stopContextManagerMainThread() {
        // mScheduledExecutorService.shutdown();
    }

    Runnable ContextManagerRunnable = new Runnable() {
        @Override
        public void run() {
            try{

                testCount +=1;
                Log.d(LOG_TAG, "[testCount]"  + testCount);

                /** test transporation : feed datain to the datapool**/

                //REPLAY ACTIIVITY LOG

                /**
                 if (testActivityRecordIndex<TransportationModeManager.getActivityRecords().size()){
                 Log.d(LOG_TAG, "[testing transportation] Feed the " + testActivityRecordIndex + " record :"
                 + TransportationModeManager.getActivityRecords().get(testActivityRecordIndex).getProbableActivities()
                 + TransportationModeManager.getActivityNameFromType(TransportationModeManager.getActivityRecords().get(testActivityRecordIndex).getProbableActivities().get(0).getType())  );

                 ActivityRecognitionManager.setProbableActivities(TransportationModeManager.getActivityRecords().get(testActivityRecordIndex).getProbableActivities());

                 ActivityRecognitionManager.setLatestDetectionTime(
                 TransportationModeManager.getActivityRecords().get(testActivityRecordIndex).getTimestamp());
                 }
                 testActivityRecordIndex+=1;
                 **/


                /**1.
                 * The first task is to update transportation mode. Transporation Manager will use the latet activity label
                 * saved in the ActivityRecognitionManager to infer the user's current transportation mode
                 * **/

                //logging the activity information using its latest activity information. Also log with location
                String message = "";


                if (mActivityRecognitionManager.getLastSavedRecord()!=null && mLocationManager.getLastSavedRecord()!=null ){

                    ActivityRecognitionRecord record = (ActivityRecognitionRecord) mActivityRecognitionManager.getLastSavedRecord();


                    //append activity string
                    for (int i=0; i<record.getProbableActivities().size(); i++){
                        message += ActivityRecognitionManager.getActivityNameFromType(record.getProbableActivities().get(i).getType()) + ":" + record.getProbableActivities().get(i).getConfidence();
                        if (i<record.getProbableActivities().size()-1){
                            message+= ";;";
                        }
                    }

                    Log.d(LOG_TAG, "[testactivitylog] activity " + record.getProbableActivities());

                    //append location data
                    LocationRecord locationRecord = (LocationRecord) mLocationManager.getLastSavedRecord();
                    //append location data
                    message += "\t" + locationRecord.getLat() + ";" + locationRecord.getLng() + ";" + locationRecord.getAccuracy();

                    Log.d(LOG_TAG, "[testactivitylog] location" + locationRecord.getLat() + " , " + locationRecord.getLng());

                    Log.d(LOG_TAG, "[testactivitylog] message" + message);

                    //log activity recognition
                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            message);


                    if (record!=null){
                        mTransportationModeManager.examineTransportation(record);
                        Log.d(LOG_TAG, "[testactivitylog] transoprtatopn: " + TransportationModeManager.getConfirmedActvitiyString());

                    }

                }



                /**2. The second  task is to update Mobility of the user after the transportationModeManager generate a transportation label,
                 * The mobility information, is also used to control the frequency of location udpate to save battery life**/
                MobilityManager.updateMobility();



                /**
                 * 3. The third task of ContextManager is to execute BackgroundLogging if it is enabled.
                 */

                //Recording is one of the types of actions that users need to put into the configuration.
                //circumstanceually. If researachers do not monitor anything, this flag should be false.
                if (getBackgroundLoggingSetting().isEnabled()){

                    ArrayList<Integer> loggingTaskIds = getBackgroundLoggingSetting().getLoggingTasks();

                    //we skip starting action and directly save Records to the DB
                    copyRecordFromLocalRecordPoolToPublicRecordPool(loggingTaskIds);

                    //save data from publie record pool to database and/or file system, based on the configuration
                    if (sIsSavingDataToLocalDatabase)
                        DataHandler.SaveRecordsToLocalDatabase(ContextManager.getPublicRecordPool(), Constants.BACKGOUND_RECORDING_SESSION_ID);

                    if(sIsSavingDataToLocalDatabase){
                        DataHandler.SaveRecordsToFileSystem(mLoggingTaskList);
                    }



                }


            }catch (IllegalArgumentException e){
                //Log.e(LOG_TAG, "Could not unregister receiver " + e.getMessage()+"");
            }

        }
    };



    /*******************************************************************************************/
    /********************************* Logging Task Related ************************************/
    /*******************************************************************************************/


    public static boolean isLoggingTaskContainedInBackGroundLogging(int id) {

        //if we find the loggingTask id in the BackgroundLogging Task List, we return true.
        for (int i=0; i<getBackgroundLoggingSetting().getLoggingTasks().size() ; i++){

            if (id==getBackgroundLoggingSetting().getLoggingTasks().get(i))
//                Log.d(LOG_TAG, "[testing logging task and requested] the loggingTask " + id
//                        + " in FOUND in BackgroundLogging ");
                return true;
        }
        return false;
    }


    public static void addRecordToPublicRecordPool(Record record){

        if(mRecordPool!=null){
            mRecordPool.add(record);
            //Log.d(LOG_TAG, "In the RecordPool, there are currently " + mRecordPool.size() + "records");
        }else
            return;
    }

    public static ArrayList<Record> getPublicRecordPool(){
        return mRecordPool;
    }


    /**
     * Based on the logging task, ContextManager will call appropriate ContextSTateMAnager to move
     * its data record in the local data record pool to ContextManager's public record pool
     * @param loggingTaskIds
     */
    public static void copyRecordFromLocalRecordPoolToPublicRecordPool(ArrayList<Integer> loggingTaskIds) {

//        Log.d(LOG_TAG, "testing saving records [moveRecordFromLocalRecordPoolToPublicRecordPool] for logging tasks" + loggingTaskIds.toString());
        //1. find the corresponding contextStateManager based on the loggingTask Id
        for (int i=0; i<loggingTaskIds.size();i++){

            //get loggingTask by id
            LoggingTask  loggingTask = getLoggingTask(loggingTaskIds.get(i));

            //find the contextStateManager responsible for the loggingTask because we need to copy record from that localRecordPool to the Public Record Pool
            String contextStateManagerName = getContextStateManagerName(loggingTask.getSource());

//            Log.d (LOG_TAG, "[testBackgroundLogging] copy data for contextsource:  " + loggingTask.getSourceType() + " from " + contextStateManagerName);


            //after finding the ContextStateManager, we copy the Record from the LocalRecordPool of that ContextStatManager to the PublicRecordPool
            copyRecordFromLocalRecordPoolToPublicRecordPool(contextStateManagerName, loggingTask);
        }

    }


    /**
     * This function copy records from the specified ContextStateManager to ContextManager's Public Data Pool
     * @param contextStateManagerName
     * @param loggingTask
     */
    private static void copyRecordFromLocalRecordPoolToPublicRecordPool(String contextStateManagerName, LoggingTask loggingTask) {

//        Log.d(LOG_TAG, "testBackgroundLogging testing saving records moving records in " + contextStateManagerName + " for logging tasks " + loggingTask.getSourceType());

        //1.
        if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_ACTIVITY_RECOGNITION))
            mActivityRecognitionManager.copyRecordsToPublicRecordPool();
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_PHONE_STATUS))
            mPhoneStatusManager.copyRecordsToPublicRecordPool();
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_LOCATION))
            mLocationManager.copyRecordsToPublicRecordPool();
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_PHONE_SENSOR))
            mPhoneSensorManager.copyRecordsToPublicRecordPool();
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_TRANSPORTATION))
            mTransportationModeManager.copyRecordsToPublicRecordPool();
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_USER_INTERACTION))
            mUserInteractionManager.copyRecordsToPublicRecordPool();

    }


    public static BackgroundLoggingSetting getBackgroundLoggingSetting() {
        return mBackgroundLoggingSetting;
    }



    /**
   *
   * ContextMAnager assigns loggingTask to the right contextStateManagers with the LoggingTaskIDs.
   * @param loggingTaskIds
   */
    public void executeLoggingTasksRequestedByAction(ArrayList<Integer> loggingTaskIds) {

        //for each loggingTask ids, we first locate the LoggingTask from ContextManager
        for (int i=0; i<loggingTaskIds.size(); i++){

            //if the id has not been added to the list, add it
            if (!mLoggingTaskByActionList.contains(loggingTaskIds.get(i)))
                mLoggingTaskByActionList.add(loggingTaskIds.get(i));

            LoggingTask loggingTask= getLoggingTask(loggingTaskIds.get(i));

            //then we find the ContextStateManager for the sourceType
            String contextStateManagerName = getContextStateManagerName(loggingTask.getSource());

            //we update the status of the LoggingTask in the ContextStateManager
            updateLoggingTasksInContextStateManager(contextStateManagerName, loggingTask);

        }
    }

    /**
     * When a savingRecordAction is stopped we remove the loggingTasks from the mLoggingTaskByActionList
     * @param loggingTaskIds
     */
    public void stopLoggingTasksRequestedByAction(ArrayList<Integer> loggingTaskIds){

        for (int i=0; i<loggingTaskIds.size(); i++){
            for (int j=0; j<mLoggingTaskByActionList.size(); j++) {
                if (mLoggingTaskByActionList.get(j) == loggingTaskIds.get(i) ){
                    //we first remove the loggingTask
                    mLoggingTaskByActionList.remove(j);

                    //we get the loggingTask object
                    LoggingTask loggingTask= getLoggingTask(loggingTaskIds.get(i));

                    //then we find the ContextStateManager for the sourceType
                    String contextStateManagerName = getContextStateManagerName(loggingTask.getSource());

                    //we update the status of the LoggingTask in the ContextStateManager
                    updateLoggingTasksInContextStateManager(contextStateManagerName, loggingTask);

                }
            }

        }
    }


    /**
     * this function determines whether to enable or disable LoggingTask in a ContextStateManager,
     * based on whether the loggingTask is requrested by a BackgroundLogging or by an Action
     * @param contextStateManagerName
     * @param loggingTask
     */
    private void updateLoggingTasksInContextStateManager(String contextStateManagerName, LoggingTask loggingTask) {

        boolean isPerformedByBackgroundLogging = false;
        boolean isPerformedByAction = false;
        boolean isRequested = false;


        //check if the loggintask is still in a BackgroundLogging
        if (getBackgroundLoggingSetting().isEnabled() && getBackgroundLoggingSetting().getLoggingTasks().contains(loggingTask.getId())){
            isPerformedByBackgroundLogging = true;
           Log.d(LOG_TAG, " [testing logging task and requested] " + loggingTask.getSource() + " is included in BackgroundRecording " );

        }

        //check if the loggingtask is requested by an action
        if (mLoggingTaskByActionList.contains(loggingTask.getId())){
            Log.d(LOG_TAG, " [testing logging task and requested] " + loggingTask.getSource() + " is performed by an Action " );
            isPerformedByAction = true;
        }

        isRequested = isPerformedByAction | isPerformedByBackgroundLogging;
        Log.d(LOG_TAG, " [testing logging task and requested] " + loggingTask.getSource() + " should be enabled!! " );


        if (isRequested){
            enableLoggingTask(contextStateManagerName, loggingTask);
        }
        else {
            disableLoggingTask(contextStateManagerName, loggingTask);
        }

    }


    /**
     * add the input loggingTask to the activeLoggingTaskList of the corresponding ContextStateManasger
     * @param contextStateManagerName
     * @param loggingTask
     */
    private void enableLoggingTask(String contextStateManagerName, LoggingTask loggingTask) {

        Log.d(LOG_TAG, " [testing logging task and requested] enable logging task: " +
                loggingTask.getSource() + " to " + contextStateManagerName);
//
//        //to execute a logging task is to set its Enagled to True.
        if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_ACTIVITY_RECOGNITION))
            mActivityRecognitionManager.updateLoggingTask(loggingTask, true);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_PHONE_STATUS))
            mPhoneStatusManager.updateLoggingTask(loggingTask, true);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_LOCATION))
            mLocationManager.updateLoggingTask(loggingTask, true);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_PHONE_SENSOR))
            mPhoneSensorManager.updateLoggingTask(loggingTask, true);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_TRANSPORTATION))
            mTransportationModeManager.updateLoggingTask(loggingTask, true);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_USER_INTERACTION))
            mUserInteractionManager.updateLoggingTask(loggingTask, true);

    }


    /**
     *
     * ContextMAnager assigns loggingTask to the right contextStateManagers with the LoggingTaskIDs.
     * @param loggingTaskIds
     */
    public void disableLoggingTasks(ArrayList<Integer> loggingTaskIds) {

        //for each loggingTask ids, we first locate the LoggingTask from ContextManager
        for (int i=0; i<loggingTaskIds.size(); i++){

            LoggingTask loggingTask= getLoggingTask(loggingTaskIds.get(i));

            //then we find the ContextStateManager for the sourceType
            String contextStateManagerName = getContextStateManagerName(loggingTask.getSource());

            //then we add the loggingTask to the right ContextStateManager's active Logging Task
            disableLoggingTask(contextStateManagerName, loggingTask);

        }
    }

    /**
     * add the input loggingTask to the activeLoggingTaskList of the corresponding ContextStateManasger
     * @param contextStateManagerName
     * @param loggingTask
     */
    private void disableLoggingTask(String contextStateManagerName, LoggingTask loggingTask) {

        Log.d(LOG_TAG, " [testing logging task and requested] disable logging task: " +
                loggingTask.getSource() + " to " + contextStateManagerName);

        //to execute a logging task is to set its Enagled to True.
        if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_ACTIVITY_RECOGNITION))
            mActivityRecognitionManager.updateLoggingTask(loggingTask, false);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_PHONE_STATUS))
            mPhoneStatusManager.updateLoggingTask(loggingTask, false);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_LOCATION))
            mLocationManager.updateLoggingTask(loggingTask, false);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_PHONE_SENSOR))
            mPhoneSensorManager.updateLoggingTask(loggingTask, false);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_TRANSPORTATION))
            mTransportationModeManager.updateLoggingTask(loggingTask, false);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_USER_INTERACTION))
            mUserInteractionManager.updateLoggingTask(loggingTask, false);

    }




    /**
     * add the input loggingTask to the activeLoggingTaskList of the corresponding ContextStateManasger
     * @param contextStateManagerName
     * @param loggingTask
     */
    private void assignLoggingTask(String contextStateManagerName, LoggingTask loggingTask) {

        Log.d(LOG_TAG, " [test source being requested] assign  logging task: " +
                loggingTask.getSource() + " to " + contextStateManagerName);

        if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_ACTIVITY_RECOGNITION))
            mActivityRecognitionManager.addLoggingTask(loggingTask);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_PHONE_STATUS))
            mPhoneStatusManager.addLoggingTask(loggingTask);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_LOCATION))
            mLocationManager.addLoggingTask(loggingTask);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_PHONE_SENSOR))
            mPhoneSensorManager.addLoggingTask(loggingTask);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_TRANSPORTATION))
            mTransportationModeManager.addLoggingTask(loggingTask);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_USER_INTERACTION))
            mUserInteractionManager.addLoggingTask(loggingTask);

    }


    public static void setIsBackgroundLoggingEnabled(boolean enabled) {
        sIsBackgroundLoggingEnabled = enabled;
    }

    /**
     * Add Logging Task
     * @param task
     */
    public static void addLoggingTask(LoggingTask task){
        if (mLoggingTaskList == null) {
            mLoggingTaskList = new ArrayList<LoggingTask>();
        }
        mLoggingTaskList.add(task);
    }

    /**
     * Remove Logging Task
     * @param task
     */
    public static void removeLoggingTask(LoggingTask task) {
        if (mLoggingTaskList != null) {
            mLoggingTaskList.remove(task);
        }
    }

    public static void removeLoggingTask(int index) {
        if (mLoggingTaskList != null) {
            mLoggingTaskList.remove(index);
        }
    }

    public static LoggingTask getLoggingTask (int id) {

        for (int i=0; i<mLoggingTaskList.size(); i++){
            if (mLoggingTaskList.get(i).getId()==id){

                return mLoggingTaskList.get(i);
            }
        }

        return null;
    }

    public static ArrayList<Integer> getLoggingTasks() {


        ArrayList<Integer> loggingTasks = new ArrayList<>();
        for (int i=0; i<mLoggingTaskList.size();i++) {
            loggingTasks.add(mLoggingTaskList.get(i).getId());
        }

        return loggingTasks;
    }


    /*******************************************************************************************/
    /********************************* Monitoring Related** ************************************/
    /*******************************************************************************************/


    /**
     *
     * ContextMAnager assigns  the task to the right contextStateManagers
     * @param contextStateManagerName
     */
    private void assignMonitoringSituationTask(String contextStateManagerName, StateMappingRule rule) {

        if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_ACTIVITY_RECOGNITION))
            mActivityRecognitionManager.addStateMappingRule(rule);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_PHONE_STATUS))
            mPhoneStatusManager.addStateMappingRule(rule);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_LOCATION))
            mLocationManager.addStateMappingRule(rule);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_PHONE_SENSOR))
            mPhoneSensorManager.addStateMappingRule(rule);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_TRANSPORTATION))
            mTransportationModeManager.addStateMappingRule(rule);
        else if (contextStateManagerName.equals(CONTEXT_STATE_MANAGER_USER_INTERACTION))
            mUserInteractionManager.addStateMappingRule(rule);

    }


    private static ArrayList<Situation> getRelatedSituation(State state) {

        ArrayList<Situation> situations = new ArrayList<Situation>();

        //we find any situation that uses the state
        for (int i=0; i< getSituationList().size(); i++){
            if (getSituationList().get(i).isUsingState(state)){
                //we find any condition in the situation using the state, so we add that situation.
                situations.add(getSituationList().get(i));
            }
        }

        //at the end we returns a list of situations that involve using the state.
        return situations;

    }

    /**
     * This function receives notifications from ContextSTateManager about a value change of a state,
     * It then examines any sitautions of which conditions involve using the value of the state, and determines whether a
     * specified sitaution has occurred.
     * @param state
     */
    public static void examineSituations(State state) {

        /**get any conditions that use the state. **/

        Log.d(LOG_TAG, "test SMR situ [examineCircumstanceConditions]");

        ArrayList<Situation> relatedSituations = getRelatedSituation(state);

        Log.d(LOG_TAG, "test SMR situ [examineCircumstanceConditions] there are " + relatedSituations.size() + " sitautions monitoring the state");

        //for each sitaution, get all of the conditions, and check whether the condition has been met.
        for (int i=0; i < relatedSituations.size(); i++) {

            Situation sitaution = relatedSituations.get(i);

            Log.d(LOG_TAG, "test SMR situ [examineCircumstanceConditions] now check sitaution " + sitaution.getName());

            /** an sitaution contains a set of conditions. An sitaution occurs only when all conditions are met **/
            boolean pass = true;

            ArrayList<Condition> conditions = sitaution.getConditionList();

            //we use "&" operation for all condition. As long as there is one false for one condition
            //pass is false.
                for (int j=0 ; j<conditions.size(); j++){
                    Condition condition = conditions.get(j);
                    //the final pass is true only when all the conditions are true.
                    pass = pass & state.getValue().equals(condition.getStateTargetValue());
                    Log.d(LOG_TAG, "test SMR situ [examineCircumstanceConditions] now the sitaution's condition:  " +condition.getStateName() +
                    "-" +condition.getStateTargetValue() + " pasS: " + pass);

                }

            /** for any sitaution for which the conditions are true, we let TriggerManager to see which action/action control to trigger.**/

            //if the conditions of the sitaution is satisfied.
            if (pass) {

                //log when an sitaution is detected
                LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                        LogManager.LOG_TAG_EVENT_DETECTED,
                        "Situation detected:\t" + sitaution.getId() + "\t" + sitaution.getName());

                //check the triggerlinks of the current sitaution to see if it would trigger anything.
                Log.d(LOG_TAG, "test SMR situ [examineCircumstanceConditions] The sitaution " + sitaution.getId() + "  condition is satisfied, check its triggerLinks! "
                        + " the sitaution has " + sitaution.getTriggerLinks().size() + " triggerlinks ");

                //the sitaution will trigger something, we call TriggerManager to manage its trigger.
                if (sitaution.getTriggerLinks().size() > 0) {
                    TriggerManager.executeTriggers(sitaution.getTriggerLinks());
                }
            }

        }


    }


    public static void addSituation(Situation situation){
        if (mSituationList ==null){
            mSituationList = new ArrayList<Situation>();
        }
        mSituationList.add(situation);
    }


    public static void removeSituation(Situation sitaution){
        if (mSituationList !=null){
            mSituationList.remove(sitaution);
        }
    }

    public static void removeSituation(int index){
        if (mSituationList !=null){
            mSituationList.remove(index);
        }
    }


    public static ArrayList<StateMappingRule> getStateMappingRuleList(){return mStateMappingRuleList;}

    public static void addStateMappingRule (StateMappingRule stateMappingRule) {mStateMappingRuleList.add(stateMappingRule);};

    public static ArrayList<Situation> getSituationList(){
        return mSituationList;
    }

    /**
     * This function returns list of SourceNames by loggingTaskIds (it is used by Session, because a Session
     * needs to know which ContextSources it is associated with. It will put the information in the metadata.
     * @param loggingTaskIds
     * @return
     */
    public static ArrayList<String> getSourceNamesFromLoggingTasks(ArrayList<Integer> loggingTaskIds) {

        ArrayList<String> names = new ArrayList<String>();
        for (int i=0; i<loggingTaskIds.size(); i++) {

            //first get LoggingTask
            LoggingTask loggingTask = getLoggingTask(loggingTaskIds.get(i));

            names.add(loggingTask.getSource());
        }
        return names;
    }



    /*******************************************************************************************/
    /************************************** ContextSource ****************************************/
    /*******************************************************************************************/


    /**
     * For ActivityRecognitionService to get the ActivityRecognitionManager from ContextManager
     * @return
     */
    public LocationManager getLocationManager(){
        if (mLocationManager==null){
            mLocationManager = new LocationManager(mContext);
        }
        return mLocationManager;
    }


    public static ActivityRecognitionManager getActivityRecognitionManager() {
        return mActivityRecognitionManager;
    }


    public static UserInteractionManager getUserInteractionManager() {
        return mUserInteractionManager;
    }


    public static String getContextStateManagerName(String source) {

        String name = null;

        if (source.equals(CONTEXT_SOURCE_NAME_ACTIVITY_RECOGNITION)){
            return CONTEXT_STATE_MANAGER_ACTIVITY_RECOGNITION;
        }
        else if (source.equals(CONTEXT_SOURCE_NAME_TRANSPORTATION)){
            return CONTEXT_STATE_MANAGER_TRANSPORTATION;
        }

        else if (source.equals(CONTEXT_SOURCE_NAME_LOCATION)){
            return CONTEXT_STATE_MANAGER_LOCATION;
        }

        //as long as the name of the ContextSource contain "Sensor.", the ContextStateManager is PhoneSensorManager
        else if (source.contains(CONTEXT_SOURCE_NAME_SENSOR_PREFIX) ) {
            return CONTEXT_STATE_MANAGER_PHONE_SENSOR;
        }

        else if (source.contains(CONTEXT_SOURCE_NAME_ACTIVITY_RECOGNITION_PREFIX) ) {
            return CONTEXT_STATE_MANAGER_ACTIVITY_RECOGNITION;
        }

        else if (source.contains(CONTEXT_SOURCE_NAME_PHONE_STATUS_PREFIX) ) {
            return CONTEXT_STATE_MANAGER_PHONE_STATUS;
        }
        else if (source.contains(CONTEXT_SOURCE_NAME_USER_INTERACTION_PREFIX) ) {
            return CONTEXT_STATE_MANAGER_USER_INTERACTION;
        }



        return name;
    }


    public static String getSourceNameFromType (String contextStateManager, int sourceType){

        if (contextStateManager.equals(CONTEXT_STATE_MANAGER_ACTIVITY_RECOGNITION)){
            return ActivityRecognitionManager.getContextSourceNameFromType(sourceType);
        }
        else if (contextStateManager.equals(CONTEXT_STATE_MANAGER_TRANSPORTATION)){
            return TransportationModeManager.getContextSourceNameFromType(sourceType);
        }
        else if (contextStateManager.equals(CONTEXT_STATE_MANAGER_LOCATION)){
            return LocationManager.getContextSourceNameFromType(sourceType);
        }
        else if (contextStateManager.equals(CONTEXT_STATE_MANAGER_PHONE_SENSOR)){
            return PhoneSensorManager.getContextSourceNameFromType(sourceType);
        }
        else if (contextStateManager.equals(CONTEXT_STATE_MANAGER_PHONE_STATUS)){
            return PhoneStatusManager.getContextSourceNameFromType(sourceType);
        }
        else if (contextStateManager.equals(CONTEXT_STATE_MANAGER_USER_INTERACTION)){
            return UserInteractionManager.getContextSourceNameFromType(sourceType);
        }
        else{
            return  null;
        }
    }

    public static ArrayList<ContextStateManager> getContextStateManagerList() {
        return mContextStateMangers;
    }


    public static int getSourceTypeFromName (String contextStateManager, String sourceName){

        if (contextStateManager.equals(CONTEXT_STATE_MANAGER_ACTIVITY_RECOGNITION)){
            return ActivityRecognitionManager.getContextSourceTypeFromName(sourceName);
        }
        else if (contextStateManager.equals(CONTEXT_STATE_MANAGER_TRANSPORTATION)){
            return TransportationModeManager.getContextSourceTypeFromName(sourceName);
        }
        else if (contextStateManager.equals(CONTEXT_STATE_MANAGER_LOCATION)){
            return LocationManager.getContextSourceTypeFromName(sourceName);
        }
        else if (contextStateManager.equals(CONTEXT_STATE_MANAGER_PHONE_SENSOR)){
            return PhoneSensorManager.getContextSourceTypeFromName(sourceName);
        }
        else if (contextStateManager.equals(CONTEXT_STATE_MANAGER_PHONE_STATUS)){
            return PhoneStatusManager.getContextSourceTypeFromName(sourceName);
        }
        else if (contextStateManager.equals(CONTEXT_STATE_MANAGER_USER_INTERACTION)){
            return UserInteractionManager.getContextSourceTypeFromName(sourceName);
        }
        else{
            return -1;
        }
    }


    /*******************************************************************************************/
    /************************************** Utility Funditons ****************************************/
    /*******************************************************************************************/


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


    /**get the current time in string (in the format of "yyyy-MM-dd HH:mm:ss" **/
    public static String getCurrentTimeStringNoTimezone(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_NO_ZONE);
        String currentTimeString = sdf_now.format(cal.getTime());

        return currentTimeString;
    }




}
