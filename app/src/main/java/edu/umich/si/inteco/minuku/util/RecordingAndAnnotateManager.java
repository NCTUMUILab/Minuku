package edu.umich.si.inteco.minuku.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.MainActivity;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.data.DataHandler;
import edu.umich.si.inteco.minuku.data.LocalDBHelper;
import edu.umich.si.inteco.minuku.data.RemoteDBHelper;
import edu.umich.si.inteco.minuku.model.Annotation;
import edu.umich.si.inteco.minuku.model.AnnotationSet;
import edu.umich.si.inteco.minuku.model.Session;
import edu.umich.si.inteco.minuku.model.SessionDocument;
import edu.umich.si.inteco.minuku.model.Task;

/**
 * Created by Armuro on 6/17/14.
 */
public class RecordingAndAnnotateManager {


    /**Constant for annotation propoerties**/
    public static final String ANNOTATION_PROPERTIES_ANNOTATION = "Annotation";
    public static final String ANNOTATION_PROPERTIES_ID = "Id";
    public static final String ANNOTATION_PROPERTIES_NAME= "Name";
    public static final String ANNOTATION_PROPERTIES_START_TIME = "Start_time";
    public static final String ANNOTATION_PROPERTIES_END_TIME = "End_time";
    public static final String ANNOTATION_PROPERTIES_IS_ENTIRE_SESSION = "Entire_session";
    public static final String ANNOTATION_PROPERTIES_CONTENT = "Content";
    public static final String ANNOTATION_PROPERTIES_TAG = "Tag";

    public static final int BACKGOUND_RECORDING_SESSION_ID = 1;

    //visualization type
    public static final String ANNOTATION_VISUALIZATION_NONE = "none";
    public static final String ANNOTATION_VISUALIZATION_TYPE_LOCATION = "location";

    public static final String ANNOTATION_REVIEW_RECORDING_FLAG = "review_recording";

    //Annotateion mode (for annotate action)
    public static final String ANNOTATE_MODE_MANUAL = "manual";
    public static final String ANNOTATE_MODE_AUTO = "auto";

    //the recording type of the annotation
    public static final String ANNOTATE_RECORDING_NEW = "new";
    public static final String ANNOTATE_RECORDING_BACKGROUND = "background";

    //review recording
    public static final String ANNOTATE_REVIEW_RECORDING_NONE= "none";
    //review all previous recordings
    public static final String ANNOTATE_REVIEW_RECORDING_ALL = "all";
    //review recent recordings (within 24 hours)
    public static final String ANNOTATE_REVIEW_RECORDING_RECENT = "recent";
    //review the latest recorded recording
    public static final String ANNOTATE_REVIEW_RECORDING_LATEST = "latest";

    /** Tag for logging. */
    private static final String LOG_TAG = "RcrdAnntMgr";

    private static Context mContext;
    private static LocalDBHelper mLocalDBHelper;
    private static ArrayList<Session> mCurRecordingSessions;

    public RecordingAndAnnotateManager(Context context) {
        this.mContext = context;
        mLocalDBHelper = new LocalDBHelper(mContext, Constants.TEST_DATABASE_NAME);
        mCurRecordingSessions = new ArrayList<Session>();

        //add the background recording to the curRecordingSession

        Log.d(LOG_TAG, "[testBackgroundLogging] ContextManager.getBackgroundLoggingSetting().isEnabled()  "
                + ContextManager.getBackgroundLoggingSetting().isEnabled());
        if (ContextManager.getBackgroundLoggingSetting().isEnabled()){
            setupBackgroundRecordingEnvironment();
        }
        else {

            //TODO: we need to remove existing backgroundlogging task from the database
        }

    }

    /**
     *
     * @param annotationSet
     * @param sessionId
     */
    public static void addAnnotationToSession(AnnotationSet annotationSet, int sessionId) {

        //get session by id and then set annotation set
        Log.d(LOG_TAG, "[addAnnotationToSession] going to get session  " + sessionId);

        Session session = getSession(sessionId);

        //check if the session originally have annotation, if yes, this is a update operation

        boolean isUpdating = false;

        //if start time of the trip is before the lastsessionudpate time, this is a new modified session
        if (session.getStartTime() <= RemoteDBHelper.getLastServerSyncTime()) {

            isUpdating = true;
        }
        else {
            isUpdating = false;
        }

        Log.d(LOG_TAG, "[addAnnotationToSession][test modified session] the modifed session time is  " + ScheduleAndSampleManager.getTimeString(session.getStartTime()) +

                 " and the lastupdatesession time is " + ScheduleAndSampleManager.getTimeString(RemoteDBHelper.getLastServerSyncTime()) + " so the update flag is " + isUpdating);



        if (session!=null){
            session.setAnnotationSet(annotationSet);
        }

        //also update the database
        updateAnnotationInDatabase(annotationSet, sessionId, isUpdating);
    }


    public static void setNoficiationIdToSession(int notificationId, int sessionId){

        Session session = getCurRecordingSession(sessionId);

        if (session!=null)
           session.setOngoingNotificationId(notificationId);

    }

    /**
     *
     * @param session
     */
    public static void saveSessionToDataBase(Session session) {
        long rowId = mLocalDBHelper.insertSessionTable(session, DatabaseNameManager.SESSION_TABLE_NAME);
        Log.d(LOG_TAG, "[testing sav and load session][saveSessionToDataBase] saving session " + session.getId() + " to the database");

    }


    /**
     *
     * @param annotationSet
     * @param sessionId
     */
    public static void updateAnnotationInDatabase(AnnotationSet annotationSet, int sessionId, boolean isUpdating){
        mLocalDBHelper.updateSessionAnnotation(sessionId, DatabaseNameManager.SESSION_TABLE_NAME, annotationSet, isUpdating);
    }

    /**
     *
     * @param sessionId
     * @return
     */
    public static Session getCurRecordingSession(int sessionId) {

     //   Log.d(LOG_TAG, " [getCurRecordingSession] tyring to search session by id" + sessionId);


        for (int i=0; i<mCurRecordingSessions.size(); i++){
     //       Log.d(LOG_TAG, " [getCurRecordingSession] looping to " + i + "th session of which the id is " + mCurRecordingSessions.get(i).getId());

            if (mCurRecordingSessions.get(i).getId()==sessionId){
                return mCurRecordingSessions.get(i);
            }
        }
        return null;
    }

    /**
     *
     * @param sessionId
     * @return
     */
    public static int removeRecordingSession(int sessionId) {

        for (int i=0; i<mCurRecordingSessions.size(); i++){
            if (mCurRecordingSessions.get(i).getId()==sessionId){
                mCurRecordingSessions.remove(i);
                return i;
            }
        }
        return -1;
    }

    public static boolean isSessionPaused(int sessionId) {

        Session session = getCurRecordingSession(sessionId);
        //Log.d(LOG_TAG, " [isSessionPaused] session " + sessionId + " pause is " + session.isPaused());
        return session.isPaused();
    }


    public static void startListRecordingActivity(String reviewMode) {
//        Log.d(LOG_TAG, " [test listrecording review mode] [startListRecordingActivity] going to start list recording activity from annoatate activity, the review mode is " + reviewMode);

        if (reviewMode.equals(RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_NONE)) {
            return ;
        }

        else {
            Bundle bundle = new Bundle();

            //indicate which session
            bundle.putString(ConfigurationManager.ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING, reviewMode);
            bundle.putString("launchTab", Constants.MAIN_ACTIVITY_TAB_RECORDINGS);
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mContext.startActivity(intent);
        }

    }

    public static void addCurRecordingSession(Session session) {

        ///add battery status information to the session
        //TODO: maybe in the future we should add the battery information at somewhere else
        session.setBatteryLife(BatteryHelper.getBatteryPercentage());

//        Log.d(LOG_TAG, "[test currunningSession ] addCurRecordingSession added session " + session.getId());

        //add to the list
        mCurRecordingSessions.add(session);

        //if the session is not background recording, we add it ti the database (there should only be one background recording in the db
        //which has been added when the app first launches
        if (session.getId()!= Constants.BACKGOUND_RECORDING_SESSION_ID)
            saveSessionToDataBase(session);
    }

    public static ArrayList<Session> getCurRecordingSessions() {
        return mCurRecordingSessions;
    }

    public static void setCurRecordingSessions(ArrayList<Session> curRecordingSessions) {
        RecordingAndAnnotateManager.mCurRecordingSessions = curRecordingSessions;
    }

    public static Session getSession (int sessionId) {

        ArrayList<String> res =  mLocalDBHelper.querySession(sessionId);
        Session session = null;

        for (int i=0; i<res.size() ; i++) {

            String sessionStr = res.get(i);

            //split each row into columns
            String[] separated = sessionStr.split(Constants.DELIMITER);

            /** get properties of the session **/
            int id = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_SESSION_ID]);
            int taskId = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_SESSION_TASK_ID]);
            long startTime = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_SESSION_START_TIME]);
            float batteryLife = Float.parseFloat(separated[DatabaseNameManager.COL_INDEX_SESSION_BATTERY_LIFE]);

            //the original contextsourceString is connected by Constants.CONTEXT_SOURCE_DELIMITER. we need to make it an array so that we can save it to
            //a session later
            String contextsourceString = separated[DatabaseNameManager.COL_INDEX_SESSION_CONTEXTSOURCES];
            String[] contextsourceArray = contextsourceString.split(Constants.CONTEXT_SOURCE_DELIMITER);

            long endTime = 0;
            //the session could be still ongoing..so we need to check where's endTime
            if (!separated[DatabaseNameManager.COL_INDEX_SESSION_END_TIME].equals("null")){
                endTime = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_SESSION_END_TIME]);
            }else {

                //there 's no end time of the session, we take the time of the last record
                String lastRecord = DataHandler.getLastSavedRecordInSession(sessionId);
//                Log.d(LOG_TAG, "[test get session time]the last record is " + lastRecord);
                if (lastRecord!=null){
                    long time = Long.parseLong(lastRecord.split(Constants.DELIMITER)[DatabaseNameManager.COL_INDEX_RECORD_TIMESTAMP_LONG] );
                    Log.d(LOG_TAG, "[test get session time] the last record time is  " + ScheduleAndSampleManager.getTimeString(time));
                    endTime = time;
                }

            }

            /** get annotaitons associated with the session **/
            JSONObject annotationSetJSON = null;
            JSONArray annotateionSetJSONArray = null;
            try {
                annotationSetJSON = new JSONObject(separated[DatabaseNameManager.COL_INDEX_SESSION_ANNOTATION_SET]);
                annotateionSetJSONArray = annotationSetJSON.getJSONArray(ANNOTATION_PROPERTIES_ANNOTATION);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            /** create sessions from the properies obtained **/
            session = new Session(id, startTime, taskId );
            session.setEndTime(endTime);
            session.setBatteryLife(batteryLife);
            session.setContextSourceTypes(contextsourceArray);

//            Log.d(LOG_TAG, " [testing load session][getSession] id " + id + " startTime " + startTime + " end time " + endTime + " annotateionSetJSONArray " + annotateionSetJSONArray);
            Log.d(LOG_TAG, " [testing load session][getSession] id " + id + " startTime " + startTime + " end time " + endTime + " contextsource " + session.getContextSourceNames());


            //set annotationset if there is one
            if (annotateionSetJSONArray!=null){
                AnnotationSet annotationSet =  toAnnorationSet(annotateionSetJSONArray);
                session.setAnnotationSet(annotationSet);
            }


        }
        return session;
    }


    public static ArrayList<String> getRecordsInBackgroundRecording(String tableName){
        //test query the background recording session
        ArrayList<String> res = LocalDBHelper.queryRecordsInSession(tableName,BACKGOUND_RECORDING_SESSION_ID);
        return res;
    }

    //get sessions recorded today
    public  static ArrayList<Session> getRecentSessions() {

        ArrayList<Session> sessions = new ArrayList<Session>();

        //from bed time to now
        //end time is now
        long queryEndTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
        //start time = 24 hours agochange
        long queryStartTime = ScheduleAndSampleManager.getCurrentTimeInMillis() - Constants.MILLISECONDS_PER_DAY;

        Log.d(LOG_TAG, " [getRecentSessions] going to query session between " + ScheduleAndSampleManager.getTimeString(queryStartTime) + " and " + ScheduleAndSampleManager.getTimeString(queryEndTime) );

        ArrayList<String> res =  mLocalDBHelper.querySessionsBetweenTimes(queryStartTime, queryEndTime);

        //we start from 1 instead of 0 because the 1st session is the background recording. We will skip it.
        for (int i=0; i<res.size() ; i++) {

            String sessionStr = res.get(i);
            String[] separated = sessionStr.split(Constants.DELIMITER);

            int id = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_SESSION_ID]);

            if (id==RecordingAndAnnotateManager.BACKGOUND_RECORDING_SESSION_ID)
                continue;


            int taskId = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_SESSION_TASK_ID]);
            long startTime = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_SESSION_START_TIME]);
            float batteryLife = Float.parseFloat(separated[DatabaseNameManager.COL_INDEX_SESSION_BATTERY_LIFE]);

            long endTime = 0;
            //the session could be still ongoing..so we need to check where's endTime
            if (!separated[DatabaseNameManager.COL_INDEX_SESSION_END_TIME].equals("null")){
                endTime = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_SESSION_END_TIME]);
            }else {

                //there 's no end time of the session, we take the time of the last record
                String lastRecord = DataHandler.getLastSavedRecordInSession(id);
                Log.d(LOG_TAG, "[test get session time]the last record is " + lastRecord);
                if (lastRecord!=null){
                    long time = Long.parseLong(lastRecord.split(Constants.DELIMITER)[DatabaseNameManager.COL_INDEX_RECORD_TIMESTAMP_LONG] );
                    Log.d(LOG_TAG, "[test get session time] the last record time is  " + ScheduleAndSampleManager.getTimeString(time));
                    endTime = time;
                }

            }


            JSONObject annotationSetJSON ;
            JSONArray annotateionSetJSONArray = null;
            try {
                annotationSetJSON = new JSONObject(separated[DatabaseNameManager.COL_INDEX_SESSION_ANNOTATION_SET]);
                annotateionSetJSONArray = annotationSetJSON.getJSONArray(ANNOTATION_PROPERTIES_ANNOTATION);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d(LOG_TAG, " [getRecentSessions] id " + id + " startTime" + startTime + " end time " + endTime);

            Session session = new Session(id, startTime, taskId );
            session.setEndTime(endTime);
            session.setBatteryLife(batteryLife);

            //get annotationset
            if (annotateionSetJSONArray!=null){
                AnnotationSet annotationSet =  toAnnorationSet(annotateionSetJSONArray);
                session.setAnnotationSet(annotationSet);
            }

            sessions.add(session);
        }

        Log.d(LOG_TAG, " [getRecentSessions] tje resuslt is " + res );

        return sessions;

    }


    public static ArrayList<Session> getModifiedSessions() {

        ArrayList<Session> sessions = new ArrayList<Session>();

        //get all sessions from the local database

        ArrayList<String> res =  mLocalDBHelper.queryModifiedSessions();


        Log.d(LOG_TAG, " [getModifiedSessions][test modified session] the resuslt is " + res );

        //we start from 1 instead of 0 because the 1st session is the background recording. We will skip it.
        for (int i=0; i<res.size() ; i++) {

            String sessionStr = res.get(i);
            String[] separated = sessionStr.split(Constants.DELIMITER);

            int id = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_SESSION_ID]);

            if (id==RecordingAndAnnotateManager.BACKGOUND_RECORDING_SESSION_ID)
                continue;

            int taskId = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_SESSION_TASK_ID]);
            long startTime = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_SESSION_START_TIME]);
            float batteryLife = Float.parseFloat(separated[DatabaseNameManager.COL_INDEX_SESSION_BATTERY_LIFE]);

            long endTime = 0;
            //the session could be still ongoing..so we need to check where's endTime
            if (!separated[DatabaseNameManager.COL_INDEX_SESSION_END_TIME].equals("null")){
                endTime = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_SESSION_END_TIME]);
            }else {

                //there 's no end time of the session, we take the time of the last record
                String lastRecord = DataHandler.getLastSavedRecordInSession(id);
                Log.d(LOG_TAG, "[test get session time]the last record is " + lastRecord);
                if (lastRecord!=null){
                    long time = Long.parseLong(lastRecord.split(Constants.DELIMITER)[DatabaseNameManager.COL_INDEX_RECORD_TIMESTAMP_LONG] );
                    Log.d(LOG_TAG, "[test get session time] the last record time is  " + ScheduleAndSampleManager.getTimeString(time));
                    endTime = time;
                }

            }


            JSONObject annotationSetJSON ;
            JSONArray annotateionSetJSONArray = null;
            try {
                annotationSetJSON = new JSONObject(separated[DatabaseNameManager.COL_INDEX_SESSION_ANNOTATION_SET]);
                annotateionSetJSONArray = annotationSetJSON.getJSONArray(ANNOTATION_PROPERTIES_ANNOTATION);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d(LOG_TAG, " [getModifiedSessions] id " + id + " startTime" + startTime + " end time " + endTime);

            Session session = new Session(id, startTime, taskId );
            session.setEndTime(endTime);
            session.setBatteryLife(batteryLife);

            //get annotationset
            if (annotateionSetJSONArray!=null){
                AnnotationSet annotationSet =  toAnnorationSet(annotateionSetJSONArray);
                session.setAnnotationSet(annotationSet);
            }

            sessions.add(session);
        }

        // Log.d(LOG_TAG, " [getModifiedSessions] in the end there are " + sessions.size() + " sessions " );


        return sessions;
    }

    public static ArrayList<Session> getAllSessions() {

        ArrayList<Session> sessions = new ArrayList<Session>();

        //get all sessions from the local database

        ArrayList<String> res =  mLocalDBHelper.querySessions();


        Log.d(LOG_TAG, " [getAllSessions] tje resuslt is " + res );

        //we start from 1 instead of 0 because the 1st session is the background recording. We will skip it.
        for (int i=0; i<res.size() ; i++) {

            String sessionStr = res.get(i);
            String[] separated = sessionStr.split(Constants.DELIMITER);

            int id = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_SESSION_ID]);

            if (id==RecordingAndAnnotateManager.BACKGOUND_RECORDING_SESSION_ID)
                continue;

            int taskId = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_SESSION_TASK_ID]);
            long startTime = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_SESSION_START_TIME]);
            float batteryLife = Float.parseFloat(separated[DatabaseNameManager.COL_INDEX_SESSION_BATTERY_LIFE]);

            long endTime = 0;
            //the session could be still ongoing..so we need to check where's endTime
            if (!separated[DatabaseNameManager.COL_INDEX_SESSION_END_TIME].equals("null")){
                endTime = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_SESSION_END_TIME]);
            }else {

                //there 's no end time of the session, we take the time of the last record
                String lastRecord = DataHandler.getLastSavedRecordInSession(id);
                Log.d(LOG_TAG, "[test get session time]the last record is " + lastRecord);
                if (lastRecord!=null){
                    long time = Long.parseLong(lastRecord.split(Constants.DELIMITER)[DatabaseNameManager.COL_INDEX_RECORD_TIMESTAMP_LONG] );
                    Log.d(LOG_TAG, "[test get session time] the last record time is  " + ScheduleAndSampleManager.getTimeString(time));
                    endTime = time;
                }

            }


            JSONObject annotationSetJSON ;
            JSONArray annotateionSetJSONArray = null;
            try {
                annotationSetJSON = new JSONObject(separated[DatabaseNameManager.COL_INDEX_SESSION_ANNOTATION_SET]);
                annotateionSetJSONArray = annotationSetJSON.getJSONArray(ANNOTATION_PROPERTIES_ANNOTATION);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d(LOG_TAG, " [getAllSessions] id " + id + " startTime" + startTime + " end time " + endTime);

            Session session = new Session(id, startTime, taskId );
            session.setEndTime(endTime);
            session.setBatteryLife(batteryLife);

            //get annotationset
            if (annotateionSetJSONArray!=null){
                AnnotationSet annotationSet =  toAnnorationSet(annotateionSetJSONArray);
                session.setAnnotationSet(annotationSet);
            }

            sessions.add(session);
        }

       // Log.d(LOG_TAG, " [getAllSessions] in the end there are " + sessions.size() + " sessions " );


        return sessions;
    }

    public static AnnotationSet toAnnorationSet(JSONArray annotationJSONArray) {

        AnnotationSet annotationSet = new AnnotationSet();
        ArrayList<Annotation> annotations = new ArrayList<Annotation>();

        for (int i=0 ; i<annotationJSONArray.length(); i++){

            JSONObject annotationJSON = null;
            try {
                Annotation annotation = new Annotation();
                annotationJSON = annotationJSONArray.getJSONObject(i);

                String content = annotationJSON.getString(ANNOTATION_PROPERTIES_CONTENT);
                annotation.setContent(content);

                JSONArray tagsJSONArray = annotationJSON.getJSONArray(ANNOTATION_PROPERTIES_TAG);

                for (int j=0; j<tagsJSONArray.length(); j++){

                    String tag = tagsJSONArray.getString(j);
                    annotation.addTag(tag);
                    Log.d(LOG_TAG, "[toAnnorationSet] the content is " + content +  " tag " + tag);
                }

                annotations.add(annotation);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        annotationSet.setAnnotations(annotations);

        Log.d(LOG_TAG, "[toAnnorationSet] the annotationSet has  " + annotationSet.getAnnotations().size() + " annotations ");
        return annotationSet;

    }




    public static AnnotationSet getAnnotationFromDatabase(int sessionId){

        AnnotationSet annotationSet = null;

        //get result from the database
        ArrayList<String> sessionResults = new ArrayList<String>();

       // sessionResults = mLocalDBHelper.querySessions(sessionId);


        return annotationSet;
    }

    /**
     * This function is just for creating environment for background recording
     * **/

    public void setupBackgroundRecordingEnvironment() {

        Log.d(LOG_TAG, "[setupBackgroundRecordingEnvironment] setting up background recording");

        /**1 Create a task and a session for background recording**/
        Session session = new Session(ContextManager.getCurrentTimeInMillis());
        session.setId(BACKGOUND_RECORDING_SESSION_ID);

        /**2 configure backroundrecording based on the setting**/

        //we first know which loggin task is associated with the background recording, then we get the contextsource for each logging task.
        for (int i=0; i< ContextManager.getBackgroundLoggingSetting().getLoggingTasks().size(); i++){
            //get logging task id
            int id = ContextManager.getBackgroundLoggingSetting().getLoggingTasks().get(i);
            //get context source name from each logging task
            String contextsource= ContextManager.getLoggingTask(id).getSource();
            //add the contexSource to session
            session.addContextSourceType(contextsource);

//            Log.d(LOG_TAG, "[setupBackgroundRecordingEnvironment] the backgroundrecording contain logging task " +
//                            ContextManager.getLoggingTask(id) + " with the source: " + ContextManager.getLoggingTask(id).getSource()
//                    + " and add to session ");

        }




        //if backgroundlogging is active, we should add it into the current logging session.
        addCurRecordingSession(session);

        Task task = new Task(Constants.BACKGOUND_RECORDING_TASK_ID,
                Constants.BACKGOUND_RECORDING_TASK_NAME,
                Constants.BACKGOUND_RECORDING_TASK_DESCRIPTION,
                Constants.BACKGOUND_RECORDING_NO_STUDY_ID);

        session.setTaskId(task.getId());


        /**if the database is empty, we need to store the background recording session into the database
         * else, we need to check whether the Task table has stored Background recording.
         * If there's one, we don't need to do anything.
         * TODO: we need to save the backgroudrecording setting in Preference
         */

        //check database for tasks
        ArrayList<String> res = mLocalDBHelper.queryTasks();

        //if the task and the session for background recording has been setup, then return
        if (res.size()>0) {
            //remove the configurations..

            //check tasks in the DB to examine whether a backgroundlogging task has been saved (we jsut need one background recording!)
            for (int i=0; i<res.size();i++){

                //a backgroundlogging task has been saved in the database, do not need to insert another one. Return!
                if (res.get(i).contains("Background_Recording")){
                    return;
                }
            }
            //if not return yet, there's no existing background logging task in the DB, so we insert the task
            mLocalDBHelper.insertTaskTable(task, DatabaseNameManager.TASK_TABLE_NAME);
        }

        //if the database is empty, we need to store the background recording session into the database
        if (mLocalDBHelper.querySessionCount()==0){
            //insert session for background recording into the database
            saveSessionToDataBase(session);
        }

    }



    public static ArrayList<String> generateHourKeysForSessionDocument(long startTime, long endTime) {


        //get the hour to see how many sections we have in records
        SimpleDateFormat sdf_hour = new SimpleDateFormat(Constants.DATE_FORMAT_HOUR);

//        2014-05-25 14:00:00 -0700

        ArrayList<String> keys = new ArrayList<String>();

        int startHour = Integer.parseInt(ScheduleAndSampleManager.getTimeString(startTime, sdf_hour));
        int endHour = Integer.parseInt(ScheduleAndSampleManager.getTimeString(endTime,sdf_hour));
        Log.d (LOG_TAG, "[generateHourKeysForSessionDocument] startTime " + startTime + " the hour is " + startHour + " endTime " + endTime +  " the hour is " + endHour);

        //create hour key
        TimeZone tz = TimeZone.getDefault();
        Calendar startCal = Calendar.getInstance(tz);
        startCal.setTimeInMillis(startTime);

        //get the date of now: the first month is Jan:0
        int year = startCal.get(Calendar.YEAR);
        int month = startCal.get(Calendar.MONTH) + 1;
        int day = startCal.get(Calendar.DAY_OF_MONTH);
        int hour = startCal.get(Calendar.HOUR_OF_DAY);

        //no minute. only hour
        startCal.set(year, month-1,day, hour, 0,0);

        //create the first key using startTime (only the hour)
        String firstKey = ScheduleAndSampleManager.getTimeString(startCal.getTimeInMillis());
        keys.add(firstKey);
        Log.d (LOG_TAG, "[generateHourKeysForSessionDocument] the firstkey is" + firstKey);


        Calendar cal = Calendar.getInstance(tz);

        //create new hour keys starting from the next hour.
        for (int i=startHour+1; i<=endHour; i++){

            //no minute. only hour
            cal.set(year, month-1, day, i, 0,0);
            String newKey = ScheduleAndSampleManager.getTimeString(cal.getTimeInMillis());
            keys.add(newKey);
            Log.d (LOG_TAG, "[generateHourKeysForSessionDocument] the new key is " + newKey );
        }



        return keys;
    }


    /**
     * "records":
     [
     {
     "timestamp_hour": "2014-05-25 14:00:00 -0700",
     "location":
     {
     "45": {
     "1": {"lat":42.121, "lng":118.12}, "4": {"lat":42.121, "lng":118.12}

     },
     "46": {

     "2": {"lat":42.121, "lng":118.12}, "5": {"lat":42.121, "lng":118.12}
     }
     },
     "activity":{
     "45": {
     "1": {"activity":"in_vehicle", "confidence":44}, "4": {"activity":"in_vehicle", "confidence":54}

     },
     "46": {

     "23": {"activity":"on_foot", "confidence":64}, "54": {"activity":"still", "confidence":54}
     }
     }
     }

     ]

     /*/

    /**based on the last sync hour, we determine what sessions to post **/
    public static  ArrayList<JSONObject> getSessionecordingDocuments(long lastSyncHourTime) {

        ArrayList<JSONObject> documents = new ArrayList<JSONObject>();

        //find all sessions of which the startTime is later than the lastSynchourtime

        ArrayList<Session> sessions = getAllSessions();

        for (int i=0; i<sessions.size(); i++) {

            if (sessions.get(i).getStartTime() > lastSyncHourTime) {

                Log.d(LOG_TAG, "[getSessionecordingDocuments] the session " + sessions.get(i).getId() + " should be posted");

                JSONObject document = getSessionDocument((int)sessions.get(i).getId());

                documents.add(document);

            }
         }

        return documents;

    }

    /**based on the last sync hour, we determine what sessions to post **/
    public static  ArrayList<JSONObject> getModifiedSessionDocuments() {

        ArrayList<JSONObject> documents = new ArrayList<JSONObject>();

        //find all sessions of which the startTime is later than the lastSynchourtime

        ArrayList<Session> sessions = getModifiedSessions();

        for (int i=0; i<sessions.size(); i++) {
            Log.d(LOG_TAG, "[getModifiedSessionDocuments][test modified session] the session " + sessions.get(i).getId() + " should be posted");

            JSONObject document = getSessionDocument((int)sessions.get(i).getId());
            documents.add(document);
        }

        return documents;

    }


    public static ArrayList<JSONObject> getBackgroundRecordingDocuments(long lastSyncHourTime) {

        ArrayList<JSONObject> documents = new ArrayList<JSONObject>();

        //time range is from the last SyncHour to the most recent complete hour
        long now = ContextManager.getCurrentTimeInMillis();

        Log.d (LOG_TAG, "[getBackgroundRecordingDocuments][testgetdata] lastSyncHourTime: " + lastSyncHourTime);
        Session session = getSession(BACKGOUND_RECORDING_SESSION_ID);
        Log.d (LOG_TAG, "[getBackgroundRecordingDocuments][testgetdata] session: start time " + session.getStartTime() + " lastsync : " +   lastSyncHourTime);

        long startTime =0;
        long endTime  =0;
        //there's no backgrounding documents existing in the server
        if (lastSyncHourTime==0) {

            startTime = session.getStartTime();
            //produce hour time

            TimeZone tz = TimeZone.getDefault();
            Calendar startCal = Calendar.getInstance(tz);
            startCal.setTimeInMillis(startTime);

            //get the date of now: the first month is Jan:0
            int year = startCal.get(Calendar.YEAR);
            int month = startCal.get(Calendar.MONTH) + 1;
            int day = startCal.get(Calendar.DAY_OF_MONTH);
            int hour = startCal.get(Calendar.HOUR_OF_DAY);


            startCal.set(year, month-1,day, hour, 0,0);

            startTime = startCal.getTimeInMillis();
            endTime = startTime + Constants.MILLISECONDS_PER_HOUR;
//            Log.d (LOG_TAG, "[getBackgroundRecordingDocuments][testing load session] no backgorund recording yet, startTime " + ScheduleAndSampleManager.getTimeString(startTime) + " - " + ScheduleAndSampleManager.getTimeString(endTime) );

        }
        //there are specific lastSynchourTime
        else {
            //if the last sync hour in the database is 9, we start from 10, because 9 indicates that 9:00-10:00 has been stored. So we start from 10-11
            startTime = lastSyncHourTime+ Constants.MILLISECONDS_PER_HOUR;
            endTime = startTime + Constants.MILLISECONDS_PER_HOUR;
//            Log.d (LOG_TAG, "[getBackgroundRecordingDocuments][testing load session] get lastSynchour, startTime " + ScheduleAndSampleManager.getTimeString(startTime) + " - " + ScheduleAndSampleManager.getTimeString(endTime) );
        }


        //for that many hours, we generate each hour to generate Background recoridng document
        //getLogDocument(long startHourTime, long endHourTime)

        while (endTime <now) {

            JSONObject document= getBackgroundRecordingDocument(startTime, endTime);
            //Log.d(LOG_TAG, "[getBackgroundRecordingDocuments] get document" + document.toString());
            documents.add(document);
            startTime = endTime;
            endTime += Constants.MILLISECONDS_PER_HOUR;
        }

        Log.d (LOG_TAG, "[getBackgroundRecordingDocuments][testing load session] the documents are:" + documents);
        return documents;

    }


    //TODO: we should get background document based on the logging task.
     public static JSONObject getBackgroundRecordingDocument(long startTime, long endTime) {

         Log.d (LOG_TAG, "[getBackgroundRecordingDocument] going to get background recording from " + ScheduleAndSampleManager.getTimeString(startTime) + " to " + ScheduleAndSampleManager.getTimeString(endTime));

         //we will generate Background recording jSON basedon the 1st hour and the lasthour
         JSONObject document  = new JSONObject();

         //we generate a background recording document for each hour
         JSONObject hourJSON = new JSONObject();

         try {
             SimpleDateFormat sdf_id = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_ID);
             document.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_ID, Constants.DEVICE_ID +"-"+ScheduleAndSampleManager.getTimeString(startTime, sdf_id) );
             document.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_STUDY_CONDITION, Constants.CURRENT_STUDY_CONDITION );
             document.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_DEVICE_ID, Constants.DEVICE_ID);
             document.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_TIMESTAMP_HOUR, ScheduleAndSampleManager.getTimeString(startTime));
         } catch (JSONException e) {
             e.printStackTrace();
         }

         //create a session object from the database.
         Session session = getSession(RecordingAndAnnotateManager.BACKGOUND_RECORDING_SESSION_ID);

         for (int i=0; i<session.getContextSourceNames().size(); i++) {

             //TODO: it seems that we're not getting any data from the database, thought we can already format the session document. need to check
             // DataHandler.getDataBySession

             Log.d (LOG_TAG, "[getBackgroundRecordingDocument][testgetdata] going to get data from session " + session.getId());

             //get data from the database
             ArrayList<String> res = DataHandler.getDataBySession(RecordingAndAnnotateManager.BACKGOUND_RECORDING_SESSION_ID,
                     session.getContextSourceNames().get(i), startTime, endTime);

             //so far we're not sure what "minute" key will be used later. So we just create sixty JSONObject. Later we will only add the JSONObjects that are not null
             //to the final JSONObject
             JSONObject[] minuteJSONArray = new JSONObject[60];
             /** make it a jSON object **/

             //this JSON will add non-null minuteJSONObject to it
             JSONObject recordTypeJSON = new JSONObject();

             //result for a recordType
             for (int j = 0; j < res.size(); j++) {

                 //each record
                 String recordStr = res.get(j);
                 String[] separated = recordStr.split(Constants.DELIMITER);
                 //Log.d (LOG_TAG, "[getBackgroundRecordingDocument] recordStr" + recordStr );
                 /** based on the time of record assign to the right key **/

                 long timestamp = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_RECORD_TIMESTAMP_LONG]);

                 SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_HOUR_MIN_SECOND);
                 String timeStr = ScheduleAndSampleManager.getTimeString(timestamp, sdf);
                 String[] timeparts = timeStr.split(":");

                 //get minute and second
                 String hour = timeparts[0];
                 String min = timeparts[1];
                 String second = timeparts[2];


                 //when we know which minute this record should be located in, we check if the corresponding JSONObject in minuteJSONArray has been initialized.
                 //if not, we initialize it.
                 if (minuteJSONArray[(Integer.parseInt(min))] == null) {
                     minuteJSONArray[(Integer.parseInt(min))] = new JSONObject();
                 }

                 //create "second" key
                 JSONObject secondRecordJSON = createSecondRecordJSONByRecordType(separated, session.getContextSourceNames().get(i));

                 //add the secondJSON to the minuteJSON
                 try {
                     minuteJSONArray[(Integer.parseInt(min))].put(second, secondRecordJSON);
                   //  Log.d (LOG_TAG, "[getBackgroundRecordingDocument] hour "+ hour  + " min " + min + " second " + second + " the secondJSON " + secondRecordJSON);

                 } catch (JSONException e) {
                     e.printStackTrace();
                 }

             }

             //add all minuteJSON to the recordTypeJSON
             for(int minuteJSONIndex = 0; minuteJSONIndex < minuteJSONArray.length; minuteJSONIndex++) {

                 if (minuteJSONArray[minuteJSONIndex]!=null ) {

                     try {
                         recordTypeJSON.put( (minuteJSONIndex)+"" , minuteJSONArray[minuteJSONIndex]);
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                 }
             }

             //HourJSOn add recordTypeJSON
             try {
                 hourJSON.put(session.getContextSourceNames().get(i), recordTypeJSON );
                // Log.d (LOG_TAG, "getBackgroundRecordingDocument the hourJSON is " + hourJSON);


                 document.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_RECORDS, hourJSON);


             } catch (JSONException e) {
                 e.printStackTrace();
             }

         }

         Log.d (LOG_TAG, "getBackgroundRecordingDocument document " + document);
         return document;
     }

    @SuppressLint("LongLogTag")
    public static JSONObject getSessionDocument(int sessionId) {

        SessionDocument sessionDocument = new SessionDocument(sessionId);

        //get meta data of the session
        Session session = getSession(sessionId);

        JSONObject sessionJSON = new JSONObject();
        JSONArray allRecordJSON = new JSONArray();

        long sessionStartTime = session.getStartTime();
        long sessionEndTime = session.getEndTime();

        //we get a list of keys for hour sections

        ArrayList<String> hourKeys = generateHourKeysForSessionDocument(sessionStartTime, sessionEndTime);

        //Log.d (LOG_TAG, "[getSessionDocument] the hour keys are " + hourKeys );

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        long startTime = 0, endTime = 0;

       //the highest level structure is hour
       for (int indexOfhourKeys = 0; indexOfhourKeys<hourKeys.size(); indexOfhourKeys++){

           JSONObject hourJSON = new JSONObject();

           try {
               //put the hour into timestamp_hour
               hourJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_TIMESTAMP_HOUR, hourKeys.get(indexOfhourKeys));
               //generate start and end time to get records in the session
               Date startTimeDate = sdf_now.parse(hourKeys.get(indexOfhourKeys));
               startTime = startTimeDate.getTime();
               endTime = startTime + Constants.MILLISECONDS_PER_HOUR;
           } catch (JSONException e) {
               e.printStackTrace();
           } catch (ParseException e) {
               e.printStackTrace();
           }


           //for each hour we get session data in that hour
           for (int i=0; i<session.getContextSourceNames().size(); i++) {

               Log.d (LOG_TAG, "[getSessionDocument] the" + i + " sourcename is ======================" + session.getContextSourceNames().get(i) + "==============================");
              Log.d (LOG_TAG, "[getSessionDocument] get session " + sessionId + "'s records between" +  ScheduleAndSampleManager.getTimeString(startTime) + " - " +   ScheduleAndSampleManager.getTimeString(endTime)
               + " the battery life is " + session.getBatteryLife());

               //get result of the record type in the session given a startTime and an endTime of each hour
               ArrayList<String> res = DataHandler.getDataBySession(sessionId,session.getContextSourceNames().get(i),startTime, endTime );

               //so far we're not sure what "minute" key will be used later. So we just create sixty JSONObject. Later we will only add the JSONObjects that are not null
               //to the final JSONObject
               JSONObject[] minuteJSONArray= new JSONObject[60];
               /** make it a jSON object **/

                //this JSON will add non-null minuteJSONObject to it
               JSONObject recordTypeJSON = new JSONObject();

               //result for a recordType
               for (int j=0; j<res.size(); j++){

                   //each record
                   String recordStr = res.get(j);
                   String[] separated = recordStr.split(Constants.DELIMITER);
                  // Log.d (LOG_TAG, "[getSessionDocument] recordStr" + recordStr );
                   /** based on the time of record assign to the right key **/

                   long timestamp = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_RECORD_TIMESTAMP_LONG]);

                   SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_HOUR_MIN_SECOND);
                   String timeStr = ScheduleAndSampleManager.getTimeString(timestamp,sdf);
                   String[] timeparts = timeStr.split(":");

                   //get minute and second
                   String hour = timeparts[0];
                   String min = timeparts[1];
                   String second = timeparts[2];



                   //when we know which minute this record should be located in, we check if the corresponding JSONObject in minuteJSONArray has been initialized.
                   //if not, we initialize it.
                   if ( minuteJSONArray[(Integer.parseInt(min)) ]==null){
                       minuteJSONArray[(Integer.parseInt(min))]= new JSONObject();
                   }

                   //create "second" key
                   JSONObject secondRecordJSON = createSecondRecordJSONByRecordType(separated, session.getContextSourceNames().get(i));

                   //add the secondJSON to the minuteJSON
                   try {
                       minuteJSONArray[(Integer.parseInt(min))].put(second, secondRecordJSON);
                    //   Log.d (LOG_TAG, "[getSessionDocument] hour "+ hour  + " min " + min + " second " + second + " the secondJSON " + secondRecordJSON);

                   } catch (JSONException e) {
                       e.printStackTrace();
                   }



               }

               //add all minuteJSON to the recordTypeJSON
               for(int minuteJSONIndex = 0; minuteJSONIndex < minuteJSONArray.length; minuteJSONIndex++) {

                    if (minuteJSONArray[minuteJSONIndex]!=null ) {

                        try {
                            recordTypeJSON.put( (minuteJSONIndex)+"" , minuteJSONArray[minuteJSONIndex]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
               }

               Log.d (LOG_TAG, "[getSessionDocument] RecordTypeJSON " + recordTypeJSON);


               //HourJSOn add recordTypeJSON
               try {
                   hourJSON.put(session.getContextSourceNames().get(i), recordTypeJSON );

               } catch (JSONException e) {
                   e.printStackTrace();
               }


               //Log.d (LOG_TAG, "[getSessionDocument] AllRecordTypeJSON " + allRecordJSON);
           }//end of recordtype

           startTime = sessionStartTime;
           endTime = startTime + Constants.MILLISECONDS_PER_HOUR;

           //add the records in that hour to the sessionJSOn
           allRecordJSON.put(hourJSON);

       }

        //complete the rest proprerties of the session
        try {
            SimpleDateFormat sdf_id = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_ID);
            sessionJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_ID, Constants.DEVICE_ID +"-"+ScheduleAndSampleManager.getTimeString(startTime, sdf_id) );
            sessionJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_STUDY_CONDITION, Constants.CURRENT_STUDY_CONDITION );
            sessionJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_START_TIME, ScheduleAndSampleManager.getTimeString(sessionStartTime));
            sessionJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_BATTERY_LIFE, session.getBatteryLife());
            sessionJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_END_TIME, ScheduleAndSampleManager.getTimeString(sessionEndTime));
            sessionJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_RECORDS, allRecordJSON);
            sessionJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_ANNOTATIONSET, session.getAnnotationsSet().toJSONObject());
            sessionJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_SESSION_ID, getSessionId(sessionId) );
            sessionJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_DEVICE_ID, Constants.DEVICE_ID);

            JSONObject taskJSON = new JSONObject();

            Task task = TaskManager.getTask(session.getTaskId());
            if (task!=null) {
                taskJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_ID, task.getId());
                taskJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_NAME, task.getName());
            }

            sessionJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_TASK, taskJSON);



//            sessionJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_TASK, )

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d (LOG_TAG, "[getSessionDocument] sessionJSON " + sessionJSON);



        return sessionJSON;
    }


    private static int getSessionId(int sessionId) {

        return sessionId;

    }

    private static JSONObject createSecondRecordJSONByRecordType(String[] separated, String sourceName) {

        JSONObject recordJSON = new JSONObject();

        //TODO: create dataRecord based on the data from database.


        return recordJSON;


    }


}


