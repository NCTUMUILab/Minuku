package edu.umich.si.inteco.minuku.data;

import android.app.AlarmManager;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import edu.umich.si.inteco.minuku.GlobalNames;
import edu.umich.si.inteco.minuku.contextmanager.ContextExtractor;
import edu.umich.si.inteco.minuku.contextmanager.ContextManager;
import edu.umich.si.inteco.minuku.contextmanager.TransportationModeDetector;
import edu.umich.si.inteco.minuku.model.Condition;
import edu.umich.si.inteco.minuku.model.TimeConstraint;
import edu.umich.si.inteco.minuku.model.record.ActivityRecord;
import edu.umich.si.inteco.minuku.model.record.AppActivityRecord;
import edu.umich.si.inteco.minuku.model.record.LocationRecord;
import edu.umich.si.inteco.minuku.model.record.Record;
import edu.umich.si.inteco.minuku.model.record.SensorRecord;
import edu.umich.si.inteco.minuku.services.CaptureProbeService;
import edu.umich.si.inteco.minuku.util.ConditionManager;
import edu.umich.si.inteco.minuku.util.DatabaseNameManager;
import edu.umich.si.inteco.minuku.util.FileHelper;
import edu.umich.si.inteco.minuku.util.RecordingAndAnnotateManager;

public class DataHandler {

	private static final String LOG_TAG = "DataHandler";
	
	private static Context mContext;
	
	private static CaptureProbeService mContextManager;
	
	//handle alarmManager
	private static AlarmManager mAlarmManager;
	
	
	//A flag indicating whether the phone is currently connected to Wifi
	private boolean isWiFIConnected;
	
	public DataHandler (Context c){
		
		Log.d(LOG_TAG, " entering Data Handler");
		
		mContext= c;
		
		mContextManager = (CaptureProbeService) c;

		mAlarmManager  = (AlarmManager)mContext.getSystemService( mContext.ALARM_SERVICE );

	}
 

	
	 
	/**
	 * write data to the local sqllite database
	 * @param recordpool
	 */
	public static  void SaveRecordsToLocalDatabase(ArrayList<Record> recordpool, int session_id ){
	
		//Log.d(LOG_TAG, "[SaveRecordsToLocalDatabase]  there are " + recordpool.size() + " records in the pool, saving records to session " + session_id);

          /** Because a record may need to be saved by different sessions, we will mark the record which session it has been saved for
         *  Before we save a record, we need to check whether the record has been saved by the target session. Because the inspection starts from
         *  the end of the record pool, we will iterate the whole record pool. Record generated record are more likely not to have been saved yet
         *  We navigate until the record that has been saved by the session. And we consider it the start at which we should start saving**/


        int indexOfStartForSavingRecord=0;
        int indexOfLastSavedByCurSession=0;
        int indexOfEndForSavingRecord=recordpool.size()-1;

        // we first inspect which record has been saved by the current session. We start from the end of the record pool
        //  Log.d(LOG_TAG, "[SaveRecordsToLocalDatabase] inspecting record at " + i + ". Its has been saved by " +  recordpool.get(i).getSavedSessionIds());
        /** 1. check whether the record has been saved by the session **/
        for (int i=recordpool.size()-1; i>=0; i--)
            if (recordpool.get(i).getSavedSessionIds().contains(session_id)) {
                //once we found the last record been saved by the session, we mark it as the starting point to save records.
                indexOfLastSavedByCurSession = i;
                break;
            }

        //if indexOfLastSavedByCurSession =0, none of the records has been saved, so the start index should be 0
        if (indexOfLastSavedByCurSession == 0)
            indexOfStartForSavingRecord = 0;
        //otherwise, we start from the next of the lastsaved..
        else
            indexOfStartForSavingRecord = indexOfLastSavedByCurSession+1;

       // Log.d(LOG_TAG, "[SaveRecordsToLocalDatabase] Finishing inspectation. The starting point to save the record is " + indexOfStartForSavingRecord + " and the end is " + indexOfEndForSavingRecord);

        //write each record to record tables based on their type and source; each source is written to a separate file
		for (int i=indexOfStartForSavingRecord; i<= indexOfEndForSavingRecord; i++){

            try{

                if (recordpool.get(i).getType()==ContextManager.CONTEXT_RECORD_TYPE_SENSOR){

                    SensorRecord sr = (SensorRecord) recordpool.get(i);

                    //get table names according to the sensor source.
                    LocalDBHelper.insertRecordTable(sr, getTableNameBySensorSourceNumber(sr.getSensorSource()), session_id );

                }

                else if (recordpool.get(i).getType()==ContextManager.CONTEXT_RECORD_TYPE_LOCATION  ){

                    LocationRecord lr = (LocationRecord) recordpool.get(i);

                    //insert record into the Location Record Table
                    LocalDBHelper.insertRecordTable(lr, DatabaseNameManager.RECORD_TABLE_NAME_LOCATION, session_id );

                }

                else if (recordpool.get(i).getType()==ContextManager.CONTEXT_RECORD_TYPE_ACTIVITY  ){

                    ActivityRecord ar = (ActivityRecord) recordpool.get(i);

                    //insert record into the Activity Record Table
                    LocalDBHelper.insertRecordTable(ar, DatabaseNameManager.RECORD_TABLE_NAME_ACTIVITY, session_id );

                }else if (recordpool.get(i).getType()==ContextManager.CONTEXT_RECORD_TYPE_APPLICATION_ACTIVITY){

                    AppActivityRecord ar = (AppActivityRecord) recordpool.get(i);

                    //insert record into the Activity Record Table
                    LocalDBHelper.insertRecordTable(ar, DatabaseNameManager.RECORD_TABLE_NAME_APPLICATION_ACTIVITY, session_id );

                }



                if (!recordpool.get(i).getSavedSessionIds().contains(session_id))
                    recordpool.get(i).getSavedSessionIds().add(session_id);

                // Log.d(LOG_TAG, "[SaveRecordsToLocalDatabase] finishing saving record at " + i + ", mark it has been saved by " + session_id + " now it has been saved by " +  recordpool.get(i).getSavedSessionIds());


            }catch (IndexOutOfBoundsException e ){

                e.printStackTrace();
            }


		}


        /** Clean the recordpool: check all the records and exmaine whether the record has been saved by all currently running sessions.
         * If yes, we dont need that record anymore. **/

        for (int i=0; i<recordpool.size(); i++){

            //We first set the flag true. If we found any session that is not contained in getSavedSessionIds(), we set the flag false, i.e.
            //the record has not been saved in all sessions

            boolean savedByAllSessions = true;

            for (int j=0; j< RecordingAndAnnotateManager.getCurRecordingSessions().size(); j++){

                //get the session id
                int runningSessionId = (int) RecordingAndAnnotateManager.getCurRecordingSessions().get(j).getId();

                //check if the session is currently paused, if yes, we don't consider it (because records should not be saved by a paused session)
                if (RecordingAndAnnotateManager.isSessionPaused(runningSessionId)){
                    //Log.d(LOG_TAG, "[SaveRecordsToLocalDatabase] the session " + runningSessionId + " is paused, we do not consider it" );
                    continue;
                }


                //if we found any session that has not been stored in SavedSessionIds(), the flag is set false
                if (!recordpool.get(i).getSavedSessionIds().contains(runningSessionId)) {
                    savedByAllSessions = false;
                    break;
                }
                //at the end of the loop means all sessions have saved the current record.  the flag stays true
            }

            //if the record has been saved by all running sessions, remove the record
            if (savedByAllSessions){
                Log.d(LOG_TAG, "[SaveRecordsToLocalDatabase] record at " + i + " has been saved by all sessions " + recordpool.get(i).getSavedSessionIds() +  " we will remove the record ");
                recordpool.remove(i);
                //i substracts one becasue the index will shift left if we remove one element
                i-=1;
            }
        }


    }
	
	
	/**
	 * write the record in the RecordPool to the local drive
	 * @param recordpool
	 */
	
	public static void WriteRecordsToFile(ArrayList<Record> recordpool){		
		
		//write each record to files based on their source; each source is written to a separate file
	
		for (int i=0; i< recordpool.size();i++){
			
			//Log.d(LOG_TAG, " there are " + recordpool.size() + " records in the pool");
			
			//SensorRecord		
			if (recordpool.get(i).getType()==ContextManager.CONTEXT_RECORD_TYPE_SENSOR){
				
				SensorRecord sr = (SensorRecord) recordpool.get(i);
				
				String filename = getFileNameBySensorSourceNumber(sr.getSensorSource());
				
				
				//get the content of the record				
				String s = sr.toString()+"\n";
				FileHelper.writeStringToFile(GlobalNames.PACKAGE_DIRECTORY_PATH + "Record/", filename + "-" +getLogFileTimeString() + ".txt", s );
				
			}else if (recordpool.get(i).getType()==ContextManager.CONTEXT_RECORD_TYPE_LOCATION  ){
				
				LocationRecord lr = (LocationRecord) recordpool.get(i);
				
				String filename = "LOCATION";
				//Log.d(LOG_TAG, " WriteToFile , the record type is " + recordpool.get(i).getType() +  " the source is " + filename);
				
				String s = lr.toString()+"\n";
				FileHelper.writeStringToFile(GlobalNames.PACKAGE_DIRECTORY_PATH + "Record/", filename + "-" +getLogFileTimeString() + ".txt", s );
				
			}else if (recordpool.get(i).getType()==ContextManager.CONTEXT_RECORD_TYPE_ACTIVITY  ){
				
				ActivityRecord ar = (ActivityRecord) recordpool.get(i);				
				
				String filename = "ACTIVITY_RECOGNITION";
				
				
				String s = ar.toString()+"\n";
				FileHelper.writeStringToFile(GlobalNames.PACKAGE_DIRECTORY_PATH + "Record/", filename + "-" +getLogFileTimeString() + ".txt", s );
				
			}
			
		}		
		//		
	}


    public static ArrayList<String> getDataBySession(int sessionId, int recordType, long startTime, long endTime) {


        //for each record type get data

        ArrayList<String> resultList = new ArrayList<String>();

        //first know which table and column to query..
        ArrayList<String> tableAndColumns = getTableAndColumnByRecordType(recordType);

        String tableName="", columnName1="", columnName2="", columnName3="" ;

        //get table and column names
        if (tableAndColumns!=null && tableAndColumns.size()>0){

            tableName = tableAndColumns.get(0);

            //get result without filter any columns
            if (tableAndColumns.size()==1){

                //execute the query
                resultList = LocalDBHelper.queryRecordsInSession(tableName, sessionId, startTime, endTime);
         //       Log.d(LOG_TAG, "[getDataBySession] got " + resultList.size() + " of results from queryRecordsInSession");


            }


        }



        return resultList;
    }



    public static ArrayList<String> getDataBySession(int sessionId, int recordType) {


        //for each record type get data

        ArrayList<String> resultList = new ArrayList<String>();

        //first know which table and column to query..
        ArrayList<String> tableAndColumns = getTableAndColumnByRecordType(recordType);

        String tableName="", columnName1="", columnName2="", columnName3="" ;

        //get table and column names
        if (tableAndColumns!=null && tableAndColumns.size()>0){

            tableName = tableAndColumns.get(0);

            //get result without filter any columns
            if (tableAndColumns.size()==1){

                //execute the query
                resultList = LocalDBHelper.queryRecordsInSession(tableName, sessionId);
                Log.d(LOG_TAG, "[getDataBySession] got " + resultList.size() + " of results from queryRecordsInSession");


            }


        }



        return resultList;
    }


	public static ArrayList<String> getDataBySession(int sessionId, String annotationVizType) {

        ArrayList<String> resultList = new ArrayList<String>();

        //first know which table and column to query..
        ArrayList<String> tableAndColumns = getTableAndColumnByVizType(annotationVizType);

        String tableName="";

        //get table and column names
        if (tableAndColumns!=null && tableAndColumns.size()>0){

            tableName = tableAndColumns.get(0);

            //get result without filter any columns
            if (tableAndColumns.size()==1){

                //execute the query
                resultList = LocalDBHelper.queryRecordsInSession(tableName, sessionId);
                Log.d(LOG_TAG, "[getDataBySession] got " + resultList.size() + " of results from queryRecordsInSession");



            }

        }

            return resultList;
    }


    public static ActivityRecord parseDBResultToActivityRecord(String recordStr) {

        ActivityRecord activityRecord = new ActivityRecord();
        List<DetectedActivity> probableActivities= new ArrayList<DetectedActivity>();

        String[] separated = recordStr.split(GlobalNames.DELIMITER);

        long timestamp = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_RECORD_TIMESTAMP_LONG]);

        //activity 1
        String activityType1 = separated[DatabaseNameManager.COL_INDEX_RECORD_ACTIVITY_LABEL_1];
        int activityConf1 = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_RECORD_ACTIVITY_CONFIDENCE_1]);
        DetectedActivity detectedActivity1 = new DetectedActivity(TransportationModeDetector.getActivityTypeFromName(activityType1), activityConf1);

        probableActivities.add(detectedActivity1);

        String activityType2 = separated[DatabaseNameManager.COL_INDEX_RECORD_ACTIVITY_LABEL_2];
        if (activityType2!=null && !activityType2.equals("null") ) {
            int activityConf2 = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_RECORD_ACTIVITY_CONFIDENCE_2]);
            DetectedActivity detectedActivity2 = new DetectedActivity(TransportationModeDetector.getActivityTypeFromName(activityType2), activityConf2);
            probableActivities.add(detectedActivity2);
        }

        String activityType3 = separated[DatabaseNameManager.COL_INDEX_RECORD_ACTIVITY_LABEL_3];
        if (activityType3!=null && !activityType3.equals("null")) {
            int activityConf3 = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_RECORD_ACTIVITY_CONFIDENCE_3]);
            DetectedActivity detectedActivity3 = new DetectedActivity(TransportationModeDetector.getActivityTypeFromName(activityType3), activityConf3);
            probableActivities.add(detectedActivity3);
        }

        activityRecord.setProbableActivities(probableActivities);
        activityRecord.setTimestamp(timestamp);


        return activityRecord;

    }

    public static ArrayList<ActivityRecord> getActivityRecordsBetweenTimes(long starTime, long endTime) {

        ArrayList<ActivityRecord> records = new ArrayList<ActivityRecord>();

        ArrayList<String> res = new ArrayList<String>();

        res = LocalDBHelper.queryRecordsInSession(
                DatabaseNameManager.RECORD_TABLE_NAME_ACTIVITY,
                RecordingAndAnnotateManager.BACKGOUND_RECORDING_SESSION_ID,
                starTime,
                endTime
                );

        Log.d(LOG_TAG, "[getActivityRecordsBetweenTimes] the last record is " + res);


        for (int j=0; j<res.size(); j++) {
            //each record
            String recordStr = res.get(j);

            ActivityRecord activityRecord =parseDBResultToActivityRecord(recordStr);

            if (activityRecord!=null) {
                records.add(activityRecord);
            }
        }
        return records;

    }

    public static String getLastSavedRecordInSession(int sessionId) {


        ArrayList<String> res = new ArrayList<String>();

        res = LocalDBHelper.queryLastRecord(
                DatabaseNameManager.RECORD_TABLE_NAME_LOCATION,
                sessionId);

        Log.d(LOG_TAG, "[getLastSavedActivityRecord] the last record is " + res);

        if (res.size()>0)
            return res.get(0);
        else
            return null;
    }


    public static ActivityRecord getLastSavedActivityRecord() {

        ActivityRecord activityRecord = null;

        ArrayList<String> res = new ArrayList<String>();

        res = LocalDBHelper.queryLastRecord(
                DatabaseNameManager.RECORD_TABLE_NAME_ACTIVITY,
                RecordingAndAnnotateManager.BACKGOUND_RECORDING_SESSION_ID);

        Log.d(LOG_TAG, "[getLastSavedActivityRecord] the last record is " + res);


        for (int j=0; j<res.size(); j++) {
            //each record
            String recordStr = res.get(j);
            activityRecord =parseDBResultToActivityRecord(recordStr);
        }

        return activityRecord;

    }


	/**
	 * Based on the rules, decide which tables and columns the DB need to query 
	 */
	public static ArrayList<String> getDataByCondition(Condition condition) {
		
		ArrayList<String> resultList = new ArrayList<String>();

		//first know which table and column to query..		
		ArrayList<String> tableAndColumns = getTableAndColumnByConditionType(condition);
		
		String tableName="", columnName1="", columnName2="", columnName3="" ;
		
		//get timeconstraint...
		ArrayList<TimeConstraint> timeConstraintList = condition.getTimeConstraints();

		
		//get table and column names
		if (tableAndColumns!=null && tableAndColumns.size()>0){			
			//the first value is the tablename
			tableName = tableAndColumns.get(0);
			//Log.d(LOG_TAG, "[getDataByCondition] the condition is " +condition.getType() + " has " + tableAndColumns.size() + " columns ");
			
			
			//get result without filter any columns 
			if (tableAndColumns.size()==1){		
				
				//execute the query
				resultList = LocalDBHelper.queryWithoutColumn(
						tableName,
                        RecordingAndAnnotateManager.BACKGOUND_RECORDING_SESSION_ID,
						timeConstraintList);
				//Log.d(LOG_TAG, "[getDataByCondition] got " + resultList.size() + " of results from WithoutColumn");
				
		
			}
			
			//only one column
			if (tableAndColumns.size()==2){		
				
				columnName1 = tableAndColumns.get(1);
				
				//after getting table and column names, execute the query
				resultList = LocalDBHelper.queryFromSingleColumn(
						tableName,
                        RecordingAndAnnotateManager.BACKGOUND_RECORDING_SESSION_ID,
						columnName1, 
						condition.getRelationship(), 
						condition.getStringTargetValue(), 
						timeConstraintList);
				
				//Log.d(LOG_TAG, "[getDataByCondition] got " + resultList.size() + " of results from single column");
				
			}
			//two columns
			else if (tableAndColumns.size()==3){	
				
				columnName1 = tableAndColumns.get(1);
				columnName2 = tableAndColumns.get(2);
				
				//below are possible two-column conditions
				
				
			}
			
			//three columns
			else if (tableAndColumns.size()==4){	
				
				columnName1 = tableAndColumns.get(1);
				columnName2 = tableAndColumns.get(2);
				columnName3 = tableAndColumns.get(3);
			}
				
		}
		
		//Log.d(LOG_TAG,"[getDataByCondition] read to query.. the table name is " +tableName+ " columnName:  " + columnName1
			//	+ " relationship: " + condition.getRelationship() + " target value: " + condition.getStringTargetValue());


		//Log.d(LOG_TAG, "[getDataByCondition] the row number is " + resultList.size() );
		
		
		
		//further processing the result list, if the condition contains some special types that need further examination, e.g. distance 
		
		if (resultList.size() >0)
			resultList = filterDataResultByCondition(resultList, condition);

		return resultList;
		
	}
	
	
	private static ArrayList<String> filterDataResultByCondition (ArrayList<String> resultList, Condition condition ){
		
		//Log.d(LOG_TAG, "[filterDataResultByCondition]  the result to filter is  " + resultList );
		
		ArrayList<String> newResultList = new  ArrayList<String>();
		
		
		//if the query is about the distance, need to further process the distance information
		if (condition.getType().equals(ConditionManager.CONDITION_TYPE_DISTANCE_TO)){
			
			//Phease 1: first check the condition, and filter the resultlist
			for (int i = 0; i< resultList.size(); i++){
				
				String[] res = resultList.get(i).split(GlobalNames.DELIMITER);

				/**
				 * 4. lat
				 * 5. lng
				 * 6. accuracy
				 * 7. altitude
				 * 8. provider
				 * 9. bearing
				 * 10.speed
				 * 
				 */
				
				//get location parameters
				double lat = Double.parseDouble(res[DatabaseNameManager.COL_INDEX_RECORD_LOC_LATITUDE_] );
				double lng = Double.parseDouble(res[DatabaseNameManager.COL_INDEX_RECORD_LOC_LONGITUDE] );
				/*
				Log.d(LOG_TAG, "[filterDataResultByCondition] the result lat:  " + lat + " lng: " + lng
						 + " the target lat:  " + condition.getLatLng().latitude + " lng: " + condition.getLatLng().longitude);
*/
				//The computed distance is stored in results[0]. If results has length 2 or greater, the initial bearing is stored in results[1]. If results has length 3 or greater, the final bearing is stored in results[2].
				float distance[] = new float[1];
				Location.distanceBetween(lat, lng, condition.getLatLng().latitude, condition.getLatLng().longitude, distance);
				
	//			Log.d(LOG_TAG, "[filterDataResultByCondition]  the calculated distance is " + distance [0]);

				//examine whether the distance satisfay the criteria
				boolean pass = ConditionManager.isSatisfyingCriteria (distance [0], condition.getRelationship(), condition.getFloatTargetValue()); 
				
				//add passed result to the new result
				if (pass)									
					newResultList.add(resultList.get(i));
				
			}
			
		}
		else {
			
			newResultList = resultList;
			
		}
		
		
		//Log.d(LOG_TAG, "[filterDataResultByCondition] the newresult has " + newResultList.size() + " rows " );
			

		return newResultList ;
		
	}

    public static ArrayList<String> getTableAndColumnByRecordType(int recordType) {

        ArrayList<String> TableAndColumns = new ArrayList<String>();


        if (recordType==ContextManager.CONTEXT_RECORD_TYPE_LOCATION) {

            TableAndColumns.add(DatabaseNameManager.RECORD_TABLE_NAME_LOCATION);

        }
        else if (recordType==ContextManager.CONTEXT_RECORD_TYPE_ACTIVITY){

            TableAndColumns.add(DatabaseNameManager.RECORD_TABLE_NAME_ACTIVITY);

        }
        else if (recordType==ContextManager.CONTEXT_RECORD_TYPE_APPLICATION_ACTIVITY){

            TableAndColumns.add(DatabaseNameManager.RECORD_TABLE_NAME_APPLICATION_ACTIVITY);

        }

        return TableAndColumns;

    }


    public static ArrayList<String> getTableAndColumnByVizType(String annotationVizType) {

        ArrayList<String> TableAndColumns = new ArrayList<String>();


        if (annotationVizType==RecordingAndAnnotateManager.ANNOTATION_VISUALIZATION_TYPE_LOCATION) {

            TableAndColumns.add(DatabaseNameManager.RECORD_TABLE_NAME_LOCATION);


        }
        return TableAndColumns;

    }



	/**
	 * This function gets the condition and find the corresponding Table and Columns in the local database.
	 * @param condition
	 * @return
	 */
	public static ArrayList<String> getTableAndColumnByConditionType (Condition condition) {
		
		//the first value in the array is Table name, the rest is the column names. Note that there could be more than one column
		ArrayList<String> TableAndColumns = new ArrayList<String>();	
		
		//Log.d(LOG_TAG, "[getTableAndColumnByConditionType] the condition type is " + condition.getType());
		
		/** Activity **/
		if (condition.getType().equals(ConditionManager.CONDITION_TYPE_ACTIVITY_TYPE)){	
			
			TableAndColumns.add(DatabaseNameManager.RECORD_TABLE_NAME_ACTIVITY);
			TableAndColumns.add(DatabaseNameManager.COL_ACTIVITY_1);	//type of the most probable activity	
		}
		
		if (condition.getType().equals(ConditionManager.CONDITION_TYPE_ACTIVITY_CONFIDENCE)){
			
			TableAndColumns.add(DatabaseNameManager.RECORD_TABLE_NAME_ACTIVITY);
			TableAndColumns.add(DatabaseNameManager.COL_ACTIVITY_CONF_1);	//confidence of the most probable activity	
		}
		
		
		/** Location **/
		if (condition.getType().equals(ConditionManager.CONDITION_TYPE_DISTANCE_TO)){
			//Log.d(LOG_TAG, "[getTableAndColumnByConditionType] the condition is distance");
			TableAndColumns.add(DatabaseNameManager.RECORD_TABLE_NAME_LOCATION);
		}
		
		
		return TableAndColumns;
		
	}
	
	
	
	/***
	 * 
	 * 
	 * @return
	 */
	private static String getLogFileTimeString(){		
		//get timzone		 
		TimeZone tz = TimeZone.getDefault();		
		Calendar cal = Calendar.getInstance(tz);
		
		SimpleDateFormat sdf_now = new SimpleDateFormat(GlobalNames.DATE_FORMAT_NOW_HOUR);
		String s = sdf_now.format(cal.getTime());
		
		return s;
	}
	
	
	
	/**
	 * 
	 * @param number
	 * @return
	 */
	private static String getTableNameBySensorSourceNumber(int number){
		
		String tableName = "";
		
		if (number==ContextManager.SENSOR_SOURCE_PHONE_ACCELEROMETER){
			tableName = DatabaseNameManager.RECORD_TABLE_NAME_ACCELEROMETER;
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_GRAVITY){
			tableName = DatabaseNameManager.RECORD_TABLE_NAME_GRAVITY;
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_GYRSCOPE){
			tableName = DatabaseNameManager.RECORD_TABLE_NAME_GYRSCOPE;
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_LINEAR_ACCELERATION){
			tableName = DatabaseNameManager.RECORD_TABLE_NAME_ACCELERATION;
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_ROTATION_VECTOR){
			tableName = DatabaseNameManager.RECORD_TABLE_NAME_ROTATION_VECTOR;
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_MAGNETIC_FIELD){
			tableName = DatabaseNameManager.RECORD_TABLE_NAME_MAGNETIC_FIELD;
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_ORIENTATION){
			tableName = DatabaseNameManager.RECORD_TABLE_NAME_ORIENTATION;
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_PROXIMITY){
			tableName = DatabaseNameManager.RECORD_TABLE_NAME_PROXIMITY;
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_AMBIENT_TEMPERATURE){
			tableName = DatabaseNameManager.RECORD_TABLE_NAME_AMBIENT_TEMPERATURE;
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_LIGHT){
			tableName = DatabaseNameManager.RECORD_TABLE_NAME_LIGHT;
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_PRESSURE){
			tableName = DatabaseNameManager.RECORD_TABLE_NAME_PRESSURE;
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_RELATIVE_HUMIDITY){
			tableName = DatabaseNameManager.RECORD_TABLE_NAME_HUMIDITY;
		}
		
		
		return tableName; 
	}

	
	
	
	/***get the filename based on the sensor source**/
	private static String getFileNameBySensorSourceNumber(int number){
		
		String filename = "undefined sensor type";
		
		if (number==ContextManager.SENSOR_SOURCE_PHONE_ACCELEROMETER){
			filename = "PHONE_ACCELEROMETER";
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_GRAVITY){
			filename = "PHONE_GRAVITY";
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_GYRSCOPE){
			filename = "PHONE_GYRSCOPE";
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_LINEAR_ACCELERATION){
			filename = "LINEAR_ACCELERATION";
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_ROTATION_VECTOR){
			filename = "ROTATION_VECTOR";
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_MAGNETIC_FIELD){
			filename = "MAGNETIC_FIELD";
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_ORIENTATION){
			filename = "PHONE_ORIENTATION";
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_PROXIMITY){
			filename = "PHONE_PROXIMITY";
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_AMBIENT_TEMPERATURE){
			filename = "PHONE_AMBIENT_TEMPERATURE";
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_LIGHT){
			filename = "PHONE_LIGHT";
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_PRESSURE){
			filename = "PHONE_PRESSURE";
		}else if (number==ContextManager.SENSOR_SOURCE_PHONE_RELATIVE_HUMIDITY){
			filename = "RELATIVE_HUMIDITY";
		}
		
		return filename;
	}
	
	
	/***
	 * 
	 * 
	 * *Utility Functions**
	 * 
	 * 
	 * 
	 * ***/
	/**get the current time in milliseconds**/
	public static long getCurrentTimeInMilli(){		
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
		
		SimpleDateFormat sdf_now = new SimpleDateFormat(GlobalNames.DATE_FORMAT_NOW);
		String currentTimeString = sdf_now.format(cal.getTime());
		
		return currentTimeString;
	}

}
