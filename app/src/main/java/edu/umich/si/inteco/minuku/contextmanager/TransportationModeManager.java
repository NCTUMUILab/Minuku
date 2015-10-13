package edu.umich.si.inteco.minuku.contextmanager;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.model.record.ActivityRecord;
import edu.umich.si.inteco.minuku.model.record.Record;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;

/**
 * Created by Armuro on 7/8/14.
 */
public class TransportationModeManager extends ContextStateManager {

    /**ContextSourceType**/
    public static final int CONTEXT_SOURCE_TRANSPORTATION = 0;
    public static final int CONTEXT_SOURCE_DETECTION_STATE = 1;

    public static final String CONTEXT_SOURCE_TRANSPORTATION_STRING = "transportation";
    public static final String CONTEXT_SOURCE_DETECTION_STATE_STRING = "detectoinState";

    public static final int STATE_STATIC = 0;
    public static final int STATE_SUSPECTING_START = 1;
    public static final int STATE_CONFIRMED = 2;
    public static final int STATE_SUSPECTING_STOP = 3;

    private static final float CONFIRM_START_ACTIVITY_THRESHOLD_IN_VEHICLE = (float) 0.6;
    private static final float CONFIRM_START_ACTIVITY_THRESHOLD_ON_FOOT = (float)0.6;
    private static final float CONFIRM_START_ACTIVITY_THRESHOLD_ON_BICYCLE =(float) 0.6;
    private static final float CONFIRM_STOP_ACTIVITY_THRESHOLD_IN_VEHICLE = (float)0.2;
    private static final float CONFIRM_STOP_ACTIVITY_THRESHOLD_ON_FOOT = (float)0.2;
    private static final float CONFIRM_STOP_ACTIVITY_THRESHOLD_ON_BICYCLE =(float) 0.2;

    public static final String TRANSPORTATION_MODE_NAME_IN_VEHICLE = "in_vehicle";
    public static final String TRANSPORTATION_MODE_NAME_ON_FOOT = "on_foot";
    public static final String TRANSPORTATION_MODE_NAME_ON_BICYCLE = "on_bicycle";
    public static final String TRANSPORTATION_MODE_NAME_IN_NO_TRANSPORTATION = "NA";

    private static final long WINDOW_LENGTH_START_ACTIVITY_DEFAULT = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_DEFAULT = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_START_ACTIVITY_IN_VEHICLE = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_START_ACTIVITY_ON_FOOT = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_START_ACTIVITY_ON_BICYCLE = 20 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_IN_VEHICLE = 150 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_ON_FOOT = 60 * Constants.MILLISECONDS_PER_SECOND;
    private static final long WINDOW_LENGTH_STOP_ACTIVITY_ON_BICYCLE = 90 * Constants.MILLISECONDS_PER_SECOND;

    public static final int NO_ACTIVITY_TYPE = -1;
    public static final int IN_VEHICLE = DetectedActivity.IN_VEHICLE;
    public static final int ON_FOOT = DetectedActivity.ON_FOOT;
    public static final int ON_BICYCLE = DetectedActivity.ON_BICYCLE;
    public static final int UNKNOWN = DetectedActivity.UNKNOWN;
    public static final int STILL = DetectedActivity.STILL;
    public static final int TILTING = DetectedActivity.TILTING;

    /**Constant **/
    public static final String IN_VEHICLE_STRING = "in_vehicle";
    public static final String ON_FOOT_STRING = "on_foot";
    public static final String WALKING_STRING = "walking";
    public static final String RUNNING_STRING = "running";
    public static final String TILTING_STRING = "tilting";
    public static final String STILL_STRING = "still";
    public static final String ON_BiCYCLE_STRING = "on_bicycle";
    public static final String UNKNOWN_STRING = "unknown";
    public static final String NA_STRING = "NA";

    private static int mSuspectedStartActivityType = -1;
    private static int mSuspectedStopActivityType = -1;
    private static int mConfirmedActivityType = NO_ACTIVITY_TYPE;// the initial value of activity is STILL.
    private static long mSuspectTime = 0;
    private static int mCurrentState = STATE_STATIC;

    private Context mContext;

    private static ArrayList<ActivityRecord> mActivityRecords;

    /** Tag for logging. */
    private static final String LOG_TAG = "TransModeDetector";

    public TransportationModeManager(Context context) {

        setName(ContextManager.CONTEXT_STATE_MANAGER_TRANSPORTATION);
        mContext = context;
    }

    /**
     * use ActivityRecognition Service's latest activity labels to examine transportaiton mode.
     * @return transportation mode. We make this synchronized because we don't want other classes
     * to use the transportation label before this function generates a new label
     */
    public int examineTransportation(ActivityRecord record) {

   //     Log.d(LOG_TAG, "[examineTransportation] enter" );
//        Log.d(LOG_TAG, "[examineTransportation] enter" + ActivityRecognitionManager.getProbableActivities() );

        List<DetectedActivity> probableActivities = record.getProbableActivities();
        long detectionTime = record.getDetectionTime();


        Log.d(LOG_TAG, "[testing transportation] before examineTransportation. at  " +
                ScheduleAndSampleManager.getTimeString(detectionTime) + " the acitivty is: " +
                getActivityNameFromType(probableActivities.get(0).getType()) + ":" +
                probableActivities.get(0).getConfidence());


        //if in the static state, we try to suspect new activity
        if (getCurrentState()==STATE_STATIC) {
            //getLatestActivityRecord();

            //Log.d (LOG_TAG, " examineTransportation at STATE_STATIC " +
            //        getActivityNameFromType(getSuspectedStartActivityType()) );


            //if the detected activity is vehicle, bike or on foot, then we suspect the activity from now
            if (probableActivities.get(0).getType()== DetectedActivity.ON_BICYCLE ||
                    probableActivities.get(0).getType()== DetectedActivity.IN_VEHICLE ||
                    probableActivities.get(0).getType()== DetectedActivity.ON_FOOT ) {

                //set current state to suspect stop
                setCurrentState(STATE_SUSPECTING_START);

                //set suspected Activity type
                setSuspectedStartActivityType(probableActivities.get(0).getType());

                //set suspect time
                setSuspectTime(detectionTime);

           //     Log.d (LOG_TAG, " examineTransportation [detected start possible activity] " + getActivityNameFromType(getSuspectedStartActivityType()) + " entering state " + getStateName(getCurrentState()) );

                LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                        LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                        "Suspect Start Transportation:\t" + getActivityNameFromType(getSuspectedStartActivityType()) + "\t" + "state:" + getStateName(getCurrentState()) );
            }

        }
        else if (getCurrentState()==STATE_SUSPECTING_START) {
            boolean isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(detectionTime, getSuspectTime(), getWindowLengh(getSuspectedStartActivityType(), getCurrentState()) );

            if (isTimeToConfirm) {

                long startTime = detectionTime - getWindowLengh(getSuspectedStartActivityType(), getCurrentState());
                long endTime = detectionTime;
                boolean isNewTransportationModeConfirmed = confirmStartPossibleTransportation(getSuspectedStartActivityType(), getWindowData(startTime, endTime));

                if (isNewTransportationModeConfirmed) {

                    //change the state to Confirmed
                    setCurrentState(STATE_CONFIRMED);
                    //set confirmed activity type
                    setConfirmedActivityType(getSuspectedStartActivityType());
                    //no suspect
                    setSuspectedStartActivityType(NO_ACTIVITY_TYPE);

                    //set the suspect time so that other class can access it.(startTime is when we think the transportation starts)
                    setSuspectTime(startTime);

//                    Log.d (LOG_TAG, " examineTransportation [confiremd start activity]  " + getActivityNameFromType(getConfirmedActivityType()) + " entering state " + getStateName(getCurrentState()) );

                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                            "Confirm Transportation:\t" +  getActivityNameFromType(getConfirmedActivityType())  + "\t" + "state:" + getStateName(getCurrentState()) );

                    return getConfirmedActivityType();
                }
                //if the suspection is wrong, back to the static state
                else {

                    //change the state to Confirmed
                    setCurrentState(STATE_STATIC);
                    //set confirmed activity type
                    setConfirmedActivityType(NO_ACTIVITY_TYPE);

                    setSuspectTime(0);

            //        Log.d (LOG_TAG, " examineTransportation [cancel activity suspection], back to state " + getStateName(getCurrentState()) );

                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                            "Cancel Suspection:\t" + "state:" + getStateName(getCurrentState()) );

                    return getConfirmedActivityType();

                }
            }
        }
        //if in the confirmed state, we suspect whether users exit the activity
        else if (getCurrentState()==STATE_CONFIRMED) {
            /** if the detected activity is vehicle, bike or on foot, then we suspect the activity from now**/

            //if the latest activity is not the currently confirmed activity nor tilting nor unkown
            if (probableActivities.get(0).getType() != getConfirmedActivityType() &&
                    probableActivities.get(0).getType() != DetectedActivity.TILTING &&
                    probableActivities.get(0).getType() != DetectedActivity.UNKNOWN) {

                //set current state to suspect stop
                setCurrentState(STATE_SUSPECTING_STOP);
                //set suspected Activity type to the confirmed activity type
                setSuspectedStopActivityType(getConfirmedActivityType());
                //set suspect time
                setSuspectTime(detectionTime);

           //     Log.d (LOG_TAG, " examineTransportation [detected stop possible activity] " + getActivityNameFromType(getSuspectedStopActivityType()) + " entering state " + getStateName(getCurrentState())  );


                LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                        LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                        "Suspect Stop Transportation:\t" +  getActivityNameFromType(getSuspectedStopActivityType())  + "\t" + "state:" + getStateName(getCurrentState()) );
            }
        }
        else if (getCurrentState()==STATE_SUSPECTING_STOP) {

            boolean isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(detectionTime, getSuspectTime(), getWindowLengh(getSuspectedStopActivityType(), getCurrentState()) );

            if (isTimeToConfirm) {

                long startTime =detectionTime - getWindowLengh(getSuspectedStartActivityType(), getCurrentState());
                long endTime = detectionTime;
                boolean isExitingTransportationMode = confirmStopPossibleTransportation(getSuspectedStopActivityType(), getWindowData(startTime, endTime));

                if (isExitingTransportationMode) {

                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                            "Stop Transportation:\t" +  getActivityNameFromType(getSuspectedStopActivityType())  + "\t" + "state:" + getStateName(getCurrentState()) );

                    //back to static
                    setCurrentState(STATE_STATIC);

                    setConfirmedActivityType(NO_ACTIVITY_TYPE);

                    setSuspectedStopActivityType(NO_ACTIVITY_TYPE);

                    //set the suspect time so that other class can access it.(startTime is when we think the transportation starts)
                    setSuspectTime(startTime);

//                    Log.d (LOG_TAG, " examineTransportation [stop activity], entering state" + getStateName(getCurrentState()) );


                }

                //not exiting the confirmed activity
                else {
                    //back to static, cancel the suspection
                    setCurrentState(STATE_CONFIRMED);

                    setSuspectedStartActivityType(NO_ACTIVITY_TYPE);

         //           Log.d (LOG_TAG, " examineTransportation [still maintain confirmed]" + getActivityNameFromType(getConfirmedActivityType()) + " still in the state " + getStateName(getCurrentState()));

                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                            "Cancel Suspection:\t" +  "state:" + getStateName(getCurrentState()) );
                }

                setSuspectTime(0);
            }



            //or directly enter suspecting activity: if the current record is other type of transportation mode
            if (probableActivities.get(0).getType() != getSuspectedStopActivityType() &&
                    probableActivities.get(0).getType()!=DetectedActivity.TILTING &&
                    probableActivities.get(0).getType()!=DetectedActivity.STILL &&
                    probableActivities.get(0).getType()!=DetectedActivity.UNKNOWN ) {


               // Log.d (LOG_TAG, " examineTransportation by the way, check if we can suspect start activity " + getActivityNameFromType(record.getProbableActivities().get(0).getType()));
                isTimeToConfirm = checkTimeElapseOfLatestActivityFromSuspectPoint(
                        detectionTime,
                        getSuspectTime(),
                        getWindowLengh(probableActivities.get(0).getType(),
                                STATE_SUSPECTING_START) );

                if (isTimeToConfirm) {

     //               Log.d (LOG_TAG, " examineTransportation yes it's good time to confirm whether we can change the suspection for "
       //                     + getActivityNameFromType(probableActivities.get(0).getType()));

                    long startTime = detectionTime - getWindowLengh(probableActivities.get(0).getType(), STATE_SUSPECTING_START) ;
                    long endTime = detectionTime;
                    boolean isActuallyStartingAnotherActivity = changeSuspectingTransportation(
                            probableActivities.get(0).getType(),
                            getWindowData(startTime, endTime));

                    if (isActuallyStartingAnotherActivity) {

//                        Log.d (LOG_TAG, " examineTransportation [interrupt suspecting stop activity] " + getActivityNameFromType(getSuspectedStopActivityType()));



                        //back to static
                        setCurrentState(STATE_SUSPECTING_START);

                        //
                       // setConfirmedActivityType(NO_ACTIVITY_TYPE);

                        setSuspectedStopActivityType(NO_ACTIVITY_TYPE);

                        setSuspectedStartActivityType(probableActivities.get(0).getType());

           //             Log.d (LOG_TAG, " examineTransportation [detected start possible activity] " + getActivityNameFromType(getSuspectedStartActivityType()) + " entering state " + getStateName(getCurrentState()) );

                        //start suspecting new activity
                        setSuspectTime(detectionTime);

                        LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                                LogManager.LOG_TAG_PROBE_TRANSPORTATION,
                                "Suspect Start Transportation:\t" + getActivityNameFromType(getSuspectedStartActivityType()) + "\t" + "state:" + getStateName(getCurrentState()) );

                    }



                }

            }




        }

        return getConfirmedActivityType();


    }

    public static long getWindowLengh (int activityType, int state) {

        if (state==STATE_SUSPECTING_START) {

            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    return WINDOW_LENGTH_START_ACTIVITY_IN_VEHICLE;
                case DetectedActivity.ON_FOOT:
                    return WINDOW_LENGTH_START_ACTIVITY_ON_FOOT;
                case DetectedActivity.ON_BICYCLE:
                    return WINDOW_LENGTH_START_ACTIVITY_ON_BICYCLE;
                default:
                    return WINDOW_LENGTH_START_ACTIVITY_DEFAULT;

            }
        }
        else if (state==STATE_SUSPECTING_STOP) {

            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    return WINDOW_LENGTH_STOP_ACTIVITY_IN_VEHICLE;
                case DetectedActivity.ON_FOOT:
                    return WINDOW_LENGTH_STOP_ACTIVITY_ON_FOOT;
                case DetectedActivity.ON_BICYCLE:
                    return WINDOW_LENGTH_STOP_ACTIVITY_ON_BICYCLE;
                default:
                    return WINDOW_LENGTH_STOP_ACTIVITY_DEFAULT;

            }

        }else {
            return WINDOW_LENGTH_STOP_ACTIVITY_DEFAULT;
        }

    }


    public static boolean checkTimeElapseOfLatestActivityFromSuspectPoint( long lastestActivityTime, long suspectTime, long windowLenth) {

        if (lastestActivityTime - suspectTime > windowLenth)
            //wait for long enough
            return true;
        else
            //still need to wait
            return false;
    }

    /**
     * We get previous activity records from ActivityRecognitionManager
     * @param startTime
     * @param endTime
     * @return
     */
    private static ArrayList<ActivityRecord> getWindowData(long startTime, long endTime) {

        ArrayList<ActivityRecord> windowData = new ArrayList<ActivityRecord>();

        //TODO: get activity records from the database
        //windowData = DataHandler.getActivityRecordsBetweenTimes(startTime, endTime);

        ///for testing: get data from the testData

        ArrayList<Record> recordPool = ActivityRecognitionManager.getLocalRecordPool();

  //      Log.d(LOG_TAG, " examineTransportation you find " + recordPool.size() + " records in the activity recognition pool");

        for (int i=0; i<recordPool.size(); i++) {

            ActivityRecord record = (ActivityRecord) recordPool.get(i);

     //       Log.d(LOG_TAG, " record.getTimestamp() " + record.getTimestamp() +
       //             " windwo startTime " + startTime + " windwo endTime " + endTime);


            if (record.getTimestamp() >= startTime && record.getTimestamp() <= endTime)
                windowData.add(record);
        }


        return windowData;
    }

    private static boolean confirmStopPossibleTransportation(int activityType, ArrayList<ActivityRecord> windowData) {

        float threshold = getConfirmStopThreshold(activityType);

        /** check if in the window data the number of the possible activity exceeds the threshold**/

        //get number of targeted data
        int count = 0;
        int inRecentCount = 0;
        for (int i=0; i<windowData.size(); i++) {

            List<DetectedActivity> detectedActivities = windowData.get(i).getProbableActivities();

            //in the recent 6 there are more than 3
            if (i >= windowData.size()-5) {
                if (detectedActivities.get(0).getType()==activityType ) {
                    inRecentCount +=1;
                }
            }

            for (int activityIndex = 0; activityIndex<detectedActivities.size(); activityIndex++) {

                //if probable activities contain the target activity, we count! (not simply see the most probable one)
                if (detectedActivities.get(activityIndex).getType()==activityType ) {
                    count +=1;
                    break;
                }
            }
        }

//        Log.d(LOG_TAG, "[confirmStoptPossibleTransportation] examineTransportation there are only " + count  +  " " + getActivityNameFromType(activityType) + " out of " + windowData.size() + " data ");

        float percentage = (float)count/windowData.size();

        if (windowData.size()!=0) {
            //if the percentage > threshold
            Log.d(LOG_TAG, "[confirmStoptPossibleTransportation] examineTransportation the percentage is  " + percentage + " the recent count is " +inRecentCount);

            if ( threshold >= percentage && inRecentCount <= 2)
                return true;
            else
                return false;

        }
        else
            //if there's no data in the windowdata, we should not confirm the possible activity
            return false;




    }

    private static boolean changeSuspectingTransportation(int activityType, ArrayList<ActivityRecord> windowData) {

        float threshold = getConfirmStartThreshold(activityType);

        /** check if in the window data the number of the possible activity exceeds the threshold**/

        int inRecentCount = 0;

        for (int i=windowData.size()-1; i>=0; i--) {

            List<DetectedActivity> detectedActivities = windowData.get(i).getProbableActivities();

            //in the recent 6 there are more than 3
            if (i >= windowData.size()-3) {
                if (detectedActivities.get(0).getType()==activityType ) {
                    inRecentCount +=1;
                }
            }


        }

        if (windowData.size()!=0) {

            //if the percentage > threshold
            Log.d(LOG_TAG, "[changeSuspectingTransportation] examineTransportation changing transportation recentCount " +inRecentCount + " within " + windowData.size()  + "  data");


            if ( inRecentCount >= 2)
                return true;
            else
                return false;

        }
        else
            //if there's no data in the windowdata, we should not confirm the possible activity
            return false;

    }


    private static boolean confirmStartPossibleTransportation(int activityType, ArrayList<ActivityRecord> windowData) {

        float threshold = getConfirmStartThreshold(activityType);

        Log.d(LOG_TAG, " examineTransportation the threshold is " + threshold + " the windowDAta size is " + windowData.size());

        /** check if in the window data the number of the possible activity exceeds the threshold**/

        //get number of targeted data
        int count = 0;
        int inRecentCount = 0;

        for (int i=0; i<windowData.size(); i++) {

            List<DetectedActivity> detectedActivities = windowData.get(i).getProbableActivities();

            //in the recent 6 there are more than 3
            if (i >= windowData.size()-5) {

                Log.d(LOG_TAG, " examineTransportation start to see if there're more than 3");


                if (detectedActivities.get(0).getType()==activityType ) {
                    inRecentCount +=1;
                    Log.d(LOG_TAG, " examineTransportation got " + getActivityNameFromType(detectedActivities.get(0).getType())
                            + " equal to  " + getActivityNameFromType(activityType));

                }
            }

            if (detectedActivities.get(0).getType()==activityType ) {
                count +=1;
            }


        }

        Log.d(LOG_TAG, "[confirmStartPossibleTransportation] examineTransportation there are " + count  +  " " +
                getActivityNameFromType(activityType) + " out of " + windowData.size() + " data ");

        if (windowData.size()!=0) {

            float percentage = (float)count/windowData.size();
            //if the percentage > threshold
           Log.d(LOG_TAG, "[confirmStartPossibleTransportation] examineTransportation the percentage is  " + percentage + " recentCount " +inRecentCount);

           if ( threshold <= percentage || inRecentCount >= 2)
               return true;
            else
               return false;

        }
        else
        //if there's no data in the windowdata, we should not confirm the possible activity
            return false;




    }

    private static float getConfirmStopThreshold(int activityType) {

        //TODO: different activity has different threshold

        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return CONFIRM_STOP_ACTIVITY_THRESHOLD_IN_VEHICLE;
            case DetectedActivity.ON_FOOT:
                return CONFIRM_STOP_ACTIVITY_THRESHOLD_ON_FOOT;
            case DetectedActivity.ON_BICYCLE:
                return CONFIRM_STOP_ACTIVITY_THRESHOLD_ON_BICYCLE;
            default:
                return (float) 0.5;

        }
    }

    private static float getConfirmStartThreshold(int activityType) {

        //TODO: different activity has different threshold

        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return CONFIRM_START_ACTIVITY_THRESHOLD_IN_VEHICLE;
            case DetectedActivity.ON_FOOT:
                return CONFIRM_START_ACTIVITY_THRESHOLD_ON_FOOT;
            case DetectedActivity.ON_BICYCLE:
                return CONFIRM_START_ACTIVITY_THRESHOLD_ON_BICYCLE;
            default:
                return (float) 0.5;
        }
    }


    private void examineCondition () {

        //  Log.d(LOG_TAG, "[examineEventConditions] got " + ConditionManager.CONDITION_TYPE_PROBE_TRANSPORTATION );

        //** get ActivityRecord and examine the current transportation mode **/

        //we onlt detect transportation mdoe when users are not indoors (now we can only use Wifi..)

       // ActivityRecord record = DataHandler.getLastSavedActivityRecord();

        /*
        Log.d(LOG_TAG, "[examineEventConditions] new activit label " + TransportationModeManager.getActivityNameFromType(record.getProbableActivities().get(0).getType())
                    +":"+ record.getProbableActivities().get(0).getConfidence()  + " at " + record.getTimestamp());


        Log.d(LOG_TAG, "[examineEventConditions][testmobility], current transportation is " +
                            TransportationModeManager.getActivityNameFromType(transportationMode) + " the target one is " + condition.getStringTargetValue() +
                            " and the state is " + TransportationModeManager.getStateName(TransportationModeManager.getCurrentState())
            );
*/

    }

    public static void setCurrentState(int state) {
        mCurrentState = state;
    }

    public static int getCurrentState() {
        return mCurrentState;
    }

    public static long getSuspectTime() {
        return mSuspectTime;
    }

    public static void setSuspectTime(long suspectTime) {
        TransportationModeManager.mSuspectTime = suspectTime;
    }

    public static int getSuspectedStartActivityType() {
        return mSuspectedStartActivityType;
    }

    public static void setSuspectedStartActivityType(int suspectedStartActivityType) {
        TransportationModeManager.mSuspectedStartActivityType = suspectedStartActivityType;
    }

    public static int getSuspectedStopActivityType() {
        return mSuspectedStopActivityType;
    }

    public static void setSuspectedStopActivityType(int suspectedStopActivityType) {
        TransportationModeManager.mSuspectedStopActivityType = suspectedStopActivityType;
    }

    public static int getConfirmedActivityType() {
        return mConfirmedActivityType;
    }

    public static void setConfirmedActivityType(int confirmedActivityType) {
        TransportationModeManager.mConfirmedActivityType = confirmedActivityType;
    }

    public static String getConfirmedActvitiyString() {
        return getActivityNameFromType(mConfirmedActivityType);
    }

    public static ArrayList<ActivityRecord> getActivityRecords() {

        if (mActivityRecords==null){
            mActivityRecords = new ArrayList<ActivityRecord>();
        }
        return mActivityRecords;

    }

    public static String getCurrentStateString() {
        return getStateName(getCurrentState());
    }

    public static void addActivityRecord(ActivityRecord record) {
        getActivityRecords().add(record);
    }


    public static String getStateName(int state) {

        switch(state) {
            
            case STATE_CONFIRMED:
                return "Confirmed";
            case STATE_SUSPECTING_STOP:
                return "Suspect Stop";
            case STATE_SUSPECTING_START:
                return "Suspect Start";
            case STATE_STATIC:
                return "Static";
            default:
                return "NA";
        }
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
    public void stateChanged() {

    }

    @Override
    public void saveRecordsInLocalRecordPool() {

    }


    public static int getContextSourceTypeFromName(String sourceName) {

        switch (sourceName){

            case CONTEXT_SOURCE_TRANSPORTATION_STRING:
                return CONTEXT_SOURCE_TRANSPORTATION;
            case CONTEXT_SOURCE_DETECTION_STATE_STRING:
                return CONTEXT_SOURCE_DETECTION_STATE;
            default:
                return -1;
        }
    }

    public static String getContextSourceNameFromType(int sourceType) {

        switch (sourceType){

            case CONTEXT_SOURCE_TRANSPORTATION:
                return CONTEXT_SOURCE_TRANSPORTATION_STRING;
            case CONTEXT_SOURCE_DETECTION_STATE:
                return CONTEXT_SOURCE_DETECTION_STATE_STRING;
            default:
                return "NA";

        }
    }


    @Override
    public void updateStateValues() {

    }

}
