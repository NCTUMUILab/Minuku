package edu.umich.si.inteco.minuku.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.ContextStateManager;
import edu.umich.si.inteco.minuku.model.AnnotationSet;
import edu.umich.si.inteco.minuku.model.Configuration;
import edu.umich.si.inteco.minuku.model.Criteria.Criterion;
import edu.umich.si.inteco.minuku.model.Questionnaire.Questionnaire;
import edu.umich.si.inteco.minuku.model.Session;
import edu.umich.si.inteco.minuku.model.Task;
import edu.umich.si.inteco.minuku.model.UserResponse;
import edu.umich.si.inteco.minuku.model.Record.Record;
import edu.umich.si.inteco.minuku.util.DatabaseNameManager;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;

public class LocalDBHelper extends SQLiteOpenHelper{

	/** Tag for logging. */
    private static final String LOG_TAG = "LocalDBHelper";

    /**parameters for creating a database**/

    private static int DATABASE_VERSION = 1;


    /** constants **/

    private static String SQL_CMD_CREATE_TABLE = "CREATE TABLE";

    private static int SQL_TYPE_CREATE_TABLE = 1;

    private static int SQL_TYPE_INSERT_TABLE = 2;


    /***Member**/

    private Context mContext;


	public LocalDBHelper(Context context, String dbName) {

		super(context, dbName, null, DATABASE_VERSION);

		mContext = context;

        initiateDatabaseManager();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		//when the DBHelper is initiated, create the necessary tables
		if (db!=null){

			Log.d(LOG_TAG, "test creat tables before LBLocal oncreate");

			////Log.d(LOG_TAG, " the DB " + db.toString() + " has existed");

			createStudyTable(db, DatabaseNameManager.STUDY_TABLE_NAME);

			//2. Whenever a study is created, create a TASK Table
			createTaskTable(db, DatabaseNameManager.TASK_TABLE_NAME);

			//3. ...and a SESSION table, so that we can insert tasks and session into the table
			createSessionTable(db, DatabaseNameManager.SESSION_TABLE_NAME);

			//4 a study will have a configuration, presumably.
			createConfigurationTable(db, DatabaseNameManager.CONFIGURATION_TABLE_NAME);

			//5 create user response table
			createUserResponseTable(db, DatabaseNameManager.USER_RESPONSE_TABLE_NAME);

			createQuestionnaireTable(db, DatabaseNameManager.QUESTIONNAIRE_TABLE_NAME);

			//4. create necessary record tables
			createRecordTables(db);

            initiateDatabaseManager();

		}



	}





	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		/*
		if (db!=null){
			db.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + SESSION_TABLE_NAME);
			onCreate(db);
		}*/
	}


    public void initiateDatabaseManager() {
        DatabaseManager.initializeInstance(this);
    }


	public void createStudyTable(SQLiteDatabase db, String table_name){

    	String cmd = SQL_CMD_CREATE_TABLE + " " +
    				table_name + " ( "+
    				DatabaseNameManager.COL_ID + " " + " INTEGER PRIMARY KEY, " +
    				DatabaseNameManager.COL_STUDY_NAME + " TEXT NOT NULL " +
    				");" ;

    	db.execSQL(cmd);

    }


    public void createTaskTable(SQLiteDatabase db, String table_name){

    	String cmd = SQL_CMD_CREATE_TABLE + " " +
    				table_name + " ( "+
    				DatabaseNameManager.COL_ID + " " + " INTEGER PRIMARY KEY, " +
    				DatabaseNameManager.COL_STUDY_ID + " INTEGER NOT NULL, " +
    				DatabaseNameManager.COL_TASK_NAME + " TEXT NOT NULL, " +
    				DatabaseNameManager.COL_TASK_DESCRIPTION + " TEXT NOT NULL, " +
    				DatabaseNameManager.COL_TASK_CREATED_TIME + " INTEGER NOT NULL, " +
    				DatabaseNameManager.COL_START_TIME + " INTEGER NOT NULL, " +
    				DatabaseNameManager.COL_END_TIME + " INTEGER NOT NULL " +
    				");" ;

    	db.execSQL(cmd);
    }


    /**
     *
     * @param db
     * @param table_name
     */
    public void createSessionTable(SQLiteDatabase db, String table_name){

    	String cmd = SQL_CMD_CREATE_TABLE + " " +
    				table_name + " ( "+
    				DatabaseNameManager.COL_ID + " " + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    				DatabaseNameManager.COL_TASK_ID + " INTEGER NOT NULL, " +
    				DatabaseNameManager.COL_TIMESTAMP_STRING + " TEXT NOT NULL, " +
    				DatabaseNameManager.COL_SESSION_START_TIME + " INTEGER NOT NULL, " +
    				DatabaseNameManager.COL_SESSION_END_TIME + " INTEGER, " +
                    DatabaseNameManager.COL_SESSION_ANNOTATION_SET + " TEXT, " +
                    DatabaseNameManager.COL_SESSION_BATTERY_LIFE + " NUMERIC, " +
                    DatabaseNameManager.COL_SESSION_MODIFIED_FLAG + " INTEGER, " +
                    DatabaseNameManager.COL_SESSION_CONTEXTSOURCES + " TEXT NOT NULL " +
    				");" ;

    	db.execSQL(cmd);
    }

    public void createConfigurationTable(SQLiteDatabase db, String table_name){

    	String cmd = SQL_CMD_CREATE_TABLE + " " +
				table_name + " ( "+
				DatabaseNameManager.COL_ID + " " + " INTEGER PRIMARY KEY, " +
				DatabaseNameManager.COL_STUDY_ID + " INTEGER NOT NULL, " +
				DatabaseNameManager.COL_CONFIGURATION_NAME+ " TEXT, " +
				DatabaseNameManager.COL_CONFIGURATION_VERSION+ " INTEGER, " +
				DatabaseNameManager.COL_CONFIGURATION_CONTENT+ " TEXT " +
				");" ;

		db.execSQL(cmd);


    }

    public void createUserResponseTable(SQLiteDatabase db, String table_name){

    	String cmd = SQL_CMD_CREATE_TABLE + " " +
				table_name + " ( "+
				DatabaseNameManager.COL_ID + " " + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				DatabaseNameManager.COL_STUDY_ID + " INTEGER NOT NULL, " +
				DatabaseNameManager.COL_QUESTIONNAIRE_ID+ " INTEGER NOT NULL, " +
				DatabaseNameManager.COL_START_TIME+ " INTEGER, " +
				DatabaseNameManager.COL_END_TIME+ " INTEGER, " +
				DatabaseNameManager.COL_CONTENT+ " TEXT " +
				");" ;

		db.execSQL(cmd);


    }


    public void createQuestionnaireTable(SQLiteDatabase db, String table_name){

    	String cmd = SQL_CMD_CREATE_TABLE + " " +
				table_name + " ( "+
				DatabaseNameManager.COL_ID + " " + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				DatabaseNameManager.COL_STUDY_ID + " INTEGER NOT NULL, " +
				DatabaseNameManager.COL_QUESTIONNAIRE_TEMPLATE_ID + " INTEGER NOT NULL, " +
				DatabaseNameManager.COL_QUESTIONNAIRE_GENERATED_TIME+ " INTEGER NOT NULL, " +
				DatabaseNameManager.COL_QUESTIONNAIRE_ATTENDED_TIME+ " INTEGER, " +
				DatabaseNameManager.COL_QUESTIONNAIRE_SUBMITTED_TIME+ " INTEGER, " +
				DatabaseNameManager.COL_QUESTIONNAIRE_IS_SUBMITTED+ " INTEGER, " +
				DatabaseNameManager.COL_QUESTIONNAIRE_RESPONSE + " TEXT " +
				");" ;

		db.execSQL(cmd);

    }


    /**
     *
     * @param db
     * @param table_name
     */
    public void createRecordTable(SQLiteDatabase db, String table_name){

    	Log.d(LOG_TAG, " test creat tables enter createSessionTable()");

    	String cmd = SQL_CMD_CREATE_TABLE + " " +
    				table_name + " ( "+
    				DatabaseNameManager.COL_ID + " " + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    				DatabaseNameManager.COL_SESSION_ID + " INTEGER NOT NULL, " +
					DatabaseNameManager.COL_DATA + " INTEGER NOT NULL, " +
    				DatabaseNameManager.COL_TIMESTAMP_STRING+ " TEXT NOT NULL, " +
    				DatabaseNameManager.COL_TIMESTAMP_LONG+ " INTEGER NOT NULL " +
    				");" ;

    	db.execSQL(cmd);

    }

    /***
     * This function create all record tables
     * @param db
     */
    private void createRecordTables (SQLiteDatabase db){

		Log.d(LOG_TAG, " test creat tables ContextManager.getContextStateManagerList() " + ContextManager.getContextStateManagerList());

		Log.d(LOG_TAG, "test creat tables  ContextManager.getContextStateManagerList() " + ContextManager.getContextStateManagerList().size());


//		for (int i=0; i<  LocationManager.getAllDatabaseTableNames().size(); i++){
//			String name =  LocationManager.getAllDatabaseTableNames().get(i);
//			createRecordTable(db,name);
//			Log.d(LOG_TAG, "[test creat tables] create tables :" + name);
//		}
//
//		for (int i=0; i<  ActivityRecognitionManager.getAllDatabaseTableNames().size(); i++){
//			String name =  ActivityRecognitionManager.getAllDatabaseTableNames().get(i);
//			createRecordTable(db,name);
//			Log.d(LOG_TAG, "[test creat tables] create tables :" + name);
//		}
//
//		for (int i=0; i<  PhoneStatusManager.getAllDatabaseTableNames().size(); i++){
//			String name =  PhoneStatusManager.getAllDatabaseTableNames().get(i);
//			createRecordTable(db,name);
//			Log.d(LOG_TAG, "[test creat tables] create tables :" + name);
//		}
//
//		for (int i=0; i<  PhoneSensorManager.getAllDatabaseTableNames().size(); i++){
//			String name =  PhoneSensorManager.getAllDatabaseTableNames().get(i);
//			createRecordTable(db,name);
//			Log.d(LOG_TAG, "[test creat tables] create tables :" + name);
//		}
//
//		for (int i=0; i<  TransportationModeManager.getAllDatabaseTableNames().size(); i++){
//			String name =  TransportationModeManager.getAllDatabaseTableNames().get(i);
//			createRecordTable(db,name);
//			Log.d(LOG_TAG, "[test creat tables] create tables :" + name);
//		}


		/**loop through ContextStateManagers to create record tables (record tables are defined in each ContextStateManager**/
		for (int i=0; i< ContextManager.getContextStateManagerList().size(); i++){

			ContextStateManager csm = ContextManager.getContextStateManagerList().get(i);
			for (int j=0; j<csm.getAllDatabaseTableNames().size(); j++) {
				createRecordTable(db, csm.getAllDatabaseTableNames().get(j));
			}
		}
    }


    /**
     *
     * Insert data into tables. Since every table has a different scheme, we separate them as different functions
     *
     * **/


    public long insertStudyTable(int id, String table_name){



		//get row number after the insertion
		//Log.d(LOG_TAG, " Inserting study " + id);

		long rowId=0;
		try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(DatabaseNameManager.COL_ID, id);
			rowId = db.insert(table_name, null, values);

		}catch(Exception e){
			e.printStackTrace();
			rowId = -1;
		}

        DatabaseManager.getInstance().closeDatabase();

		//Log.d(LOG_TAG, " Inserting successfully! The " + table_name + " table now has " + rowId + " rows.");

		return rowId;
    }

    /**
     * insert a task into a TaskTable
     * @param task
     * @param table_name
     */
    public long insertTaskTable(Task task, String table_name){


    	//TODO: the user should be able to specify the database because each study may have a different database.


		long rowId=0;
		try{
            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            //  Log.d(LOG_TAG, "[test instantiate db]  after instanstiate the database" );
            ContentValues values = new ContentValues();


            values.put(DatabaseNameManager.COL_ID, task.getId());
            values.put(DatabaseNameManager.COL_STUDY_ID, task.getStudyId());
            values.put(DatabaseNameManager.COL_TASK_NAME, task.getName());
            values.put(DatabaseNameManager.COL_TASK_DESCRIPTION, task.getDescription());
            values.put(DatabaseNameManager.COL_TASK_CREATED_TIME, getCurrentTimeInMilli());
            values.put(DatabaseNameManager.COL_START_TIME, task.getStartTime());
            values.put(DatabaseNameManager.COL_END_TIME, task.getEndTime());

            //Log.d(LOG_TAG, "[testInsert][insertTaskTable] db " + db);
            //get row number after the insertion
            //Log.d(LOG_TAG, " trying to insert task " +task.getId() + " : " + task.getName() + " to the task table " + table_name);

            rowId = db.insert(table_name, null, values);

		}catch(Exception e){
			e.printStackTrace();
			rowId = -1;
		}

        DatabaseManager.getInstance().closeDatabase();

		//Log.d(LOG_TAG, " Inserting successfully! The " + table_name + " table now has " + queryTaskCount() + " rows.");

		return rowId;
    }


    /**
     *
     * @param config
     * @param table_name
     * @return
     */
    public long insertConfigurationTable(Configuration config, String table_name){


       // Log.d(LOG_TAG, "[testInsert][insertTaskTable] db " + db);
       // Log.d(LOG_TAG, "[insertConfigurationTable] db " + db);
		long rowId=0;
		try{
            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(DatabaseNameManager.COL_ID, config.getId());
            values.put(DatabaseNameManager.COL_STUDY_ID, config.getStudyId());
            values.put(DatabaseNameManager.COL_CONFIGURATION_NAME, config.getName());
            values.put(DatabaseNameManager.COL_CONFIGURATION_VERSION, config.getVersion());
            values.put(DatabaseNameManager.COL_CONFIGURATION_CONTENT, config.getContent().toString());
			rowId = db.insert(table_name, null, values);

		}catch(Exception e){
			e.printStackTrace();
			rowId = -1;
		}

        DatabaseManager.getInstance().closeDatabase();
		//Log.d(LOG_TAG, " Inserting configuration successfully! The " + table_name + " table now has " + rowId + " rows.");

		return rowId;

    }


    /**
     * insert a response into a ResponseTable
     * @param response
     * @param table_name
     */
    public long insertUserResponseTable(UserResponse response, String table_name){

    	//TODO: the user should be able to specify the database because each study may have a different database.

		//get row number after the insertion
		Log.d(LOG_TAG, " Inserting User Response " + response.getId() + " response: " + response.getContent()  + " to the task table " + table_name);

		long rowId=0;
		try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(DatabaseNameManager.COL_STUDY_ID, response.getStudyId());
            values.put(DatabaseNameManager.COL_QUESTIONNAIRE_ID, response.getStudyId());
            values.put(DatabaseNameManager.COL_START_TIME, response.getStartTime());
            values.put(DatabaseNameManager.COL_END_TIME, response.getEndTime());
            values.put(DatabaseNameManager.COL_CONTENT, response.getContent().toString());

            rowId = db.insert(table_name, null, values);
            DatabaseManager.getInstance().closeDatabase();
		}catch(Exception e){
			e.printStackTrace();
			rowId = -1;
		}


		//Log.d(LOG_TAG, " Inserting successfully! The " + table_name + " table now has " + rowId + " rows.");

		return rowId;
    }


	public static ArrayList<String> queryQuestionnaire(int questionnaireId){

//		Log.d(LOG_TAG, "[querySession] getsession " + sessionId);

		ArrayList<String> rows = new ArrayList<String>();

		try{

			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

			String sql = "SELECT *"  +" FROM " + DatabaseNameManager.QUESTIONNAIRE_TABLE_NAME +
					//condition with session id
					" where " + DatabaseNameManager.COL_ID + " = " + questionnaireId + "";

//            Log.d(LOG_TAG, "[querySession] the query statement is " +sql);

			Cursor cursor = db.rawQuery(sql, null);
			int columnCount = cursor.getColumnCount();
			while(cursor.moveToNext()){
				String curRow = "";
				for (int i=0; i<columnCount; i++){
					curRow += cursor.getString(i)+ Constants.DELIMITER;
				}
				rows.add(curRow);
			}
			cursor.close();

			DatabaseManager.getInstance().closeDatabase();


		}catch (Exception e){

		}

		Log.d(LOG_TAG, "[querySession] the session is " +rows);

		return rows;


	}

	public static long updateQuestionnaireAttendenceTable(Questionnaire questionnaire, String table_name){

		//TODO: the user should be able to specify the database because each study may have a different database.

		Log.d(LOG_TAG, "test qu going to update questionnaire attendence time " + ScheduleAndSampleManager.getTimeString(questionnaire.getAttendedTime()) + " to questionnaire "
		 + questionnaire.getId() + " : " + questionnaire.getDescription() );

		String where = DatabaseNameManager.COL_ID + " = " +  questionnaire.getId();

		long rowId=0;
		try{
			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
			ContentValues values = new ContentValues();

			values.put(DatabaseNameManager.COL_QUESTIONNAIRE_ATTENDED_TIME, questionnaire.getAttendedTime() );
			Log.d(LOG_TAG, "test qu updating questionnaire attendence time " +  ScheduleAndSampleManager.getTimeString(questionnaire.getAttendedTime()) );

			db.update(table_name, values, where, null);
			DatabaseManager.getInstance().closeDatabase();
		}catch(Exception e){
			e.printStackTrace();
			rowId = -1;
		}


		//Log.d(LOG_TAG, " Inserting successfully! The " + table_name + " table now has " + rowId + " rows.");

		return rowId;
	}


	public static long updateQuestionnaireResponseTable(Questionnaire questionnaire, String table_name){

		//TODO: the user should be able to specify the database because each study may have a different database.

		String where = DatabaseNameManager.COL_ID + " = " +  questionnaire.getId();

		long rowId=0;
		JSONArray responses = questionnaire.getResponses();
//
//		Log.d(LOG_TAG, "test qu going to update questionnaire " +questionnaire.getId() + " have " + questionnaire.getQuestions().size() + " questions" );
//
//		Log.d(LOG_TAG, "test qu getting responses: " + responses);

		try{

			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
			ContentValues values = new ContentValues();

			values.put(DatabaseNameManager.COL_QUESTIONNAIRE_SUBMITTED_TIME, questionnaire.getSubmittedTime());
			values.put(DatabaseNameManager.COL_QUESTIONNAIRE_IS_SUBMITTED, questionnaire.isSubmitted());
			values.put(DatabaseNameManager.COL_QUESTIONNAIRE_ATTENDED_TIME, questionnaire.getAttendedTime());
			values.put(DatabaseNameManager.COL_QUESTIONNAIRE_RESPONSE, responses.toString());

			Log.d(LOG_TAG, "test qu going to update questionnaire " +
					" submitted time " + ScheduleAndSampleManager.getTimeString(questionnaire.getSubmittedTime()) + " is submitted " + questionnaire.isSubmitted() +
					" generated time " + ScheduleAndSampleManager.getTimeString(questionnaire.getGeneratedTime()) +
					" attendence time " +ScheduleAndSampleManager.getTimeString(questionnaire.getAttendedTime()) +
					" to questionnaire  " + questionnaire.getId() + " : " + questionnaire.getDescription()  + " with response "
					+ responses.toString());

			db.update(table_name, values, where, null);
			DatabaseManager.getInstance().closeDatabase();
		}catch(Exception e){
			e.printStackTrace();
			rowId = -1;
		}


		//Log.d(LOG_TAG, " Inserting successfully! The " + table_name + " table now has " + rowId + " rows.");

		return rowId;
	}

    public long insertQuestionnaireTable(Questionnaire questionnaire, String table_name){

    	//TODO: the user should be able to specify the database because each study may have a different database.

		long rowId=0;
		try{
            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(DatabaseNameManager.COL_QUESTIONNAIRE_GENERATED_TIME, questionnaire.getGeneratedTime());
            values.put(DatabaseNameManager.COL_QUESTIONNAIRE_TEMPLATE_ID, questionnaire.getTemplateId());
			values.put(DatabaseNameManager.COL_STUDY_ID, questionnaire.getStudyId());

			rowId = db.insert(table_name, null, values);
            DatabaseManager.getInstance().closeDatabase();
		}catch(Exception e){
			e.printStackTrace();
			rowId = -1;
		}


		//Log.d(LOG_TAG, " Inserting successfully! The " + table_name + " table now has " + rowId + " rows.");

		return rowId;
    }


    /**
     * Update the endtime of a session. Usually this information is obtained when a recording is finished.
     * @param sessionId
     * @param isUpdating
     */
    public static void updateSessionModifiedFlag(int sessionId, boolean isUpdating ){

        String where = DatabaseNameManager.COL_ID + " = " +  sessionId;

        try{
            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            if (isUpdating){
                values.put(DatabaseNameManager.COL_SESSION_MODIFIED_FLAG, 1);
            }
            else
                values.put(DatabaseNameManager.COL_SESSION_MODIFIED_FLAG, 0);
            db.update(DatabaseNameManager.SESSION_TABLE_NAME, values, where, null);
            //	Log.d(LOG_TAG, " Updating end time " + endTime  + " successfully! ");
            DatabaseManager.getInstance().closeDatabase();

        }catch(Exception e){
            e.printStackTrace();
        }


        ArrayList<String> result = querySessions();
        //Log.d(LOG_TAG, "the session update result is " + result);

    }

    /**
     * Update the endtime of a session. Usually this information is obtained when a recording is finished.
     * @param sessionId
     * @param table_name
     * @param endTime
     */
    public void updateSessionEndTime(int sessionId, String table_name, long endTime ){



		String where = DatabaseNameManager.COL_ID + " = " +  sessionId;

		try{
            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(DatabaseNameManager.COL_SESSION_END_TIME, endTime);
            db.update(DatabaseNameManager.SESSION_TABLE_NAME, values, where, null);
		//	Log.d(LOG_TAG, " Updating end time " + endTime  + " successfully! ");
            DatabaseManager.getInstance().closeDatabase();

		}catch(Exception e){
			e.printStackTrace();
		}


		ArrayList<String> result = querySessions();
		//Log.d(LOG_TAG, "the session update result is " + result);

    }

    /**
     * Add annotation to a session
     * @param sessionId
     * @param table_name
     * @param annotationSet
     */
    public void updateSessionAnnotation(int sessionId, String table_name, AnnotationSet annotationSet, boolean isUpdating){

        Log.d(LOG_TAG, "[updateSessionAnnotation][test modified session] submit annotation set " + annotationSet.toJSONObject().toString()
        + " this is update operation " + isUpdating);

        String where = DatabaseNameManager.COL_ID + " = " +  sessionId;

      //  Log.d(LOG_TAG, "[querySession] the query statement is " +where);

        try{
            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(DatabaseNameManager.COL_SESSION_ANNOTATION_SET, annotationSet.toJSONObject().toString());

            //if this is an updating operation, we need to mark the session until the session has been uploaded to the server
            if (isUpdating) {
                Log.d(LOG_TAG, "[updateSessionAnnotation][test modified session] putting modified flat "  + 1);
                values.put(DatabaseNameManager.COL_SESSION_MODIFIED_FLAG, 1);
            }
            else {
                values.put(DatabaseNameManager.COL_SESSION_MODIFIED_FLAG, 0);
            }

            db.update(DatabaseNameManager.SESSION_TABLE_NAME, values, where, null);
         //   Log.d(LOG_TAG, " Updating annotation successfully!");
            DatabaseManager.getInstance().closeDatabase();

        }catch(Exception e){
            e.printStackTrace();
        }

        ArrayList<String> result = querySessions();
       // Log.d(LOG_TAG, "the session update result is " + result);

    }

	/**
	 * update session
	 * @param session
	 * @param table_name
	 * @return
	 */
	public void updateSessionTable(Session session, String table_name){

		String where = DatabaseNameManager.COL_ID + " = " +  session.getId();

		try{
			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
			ContentValues values = new ContentValues();

			String contextsourceStr = "";
			//create a string for contextsource
			for (int i=0; i<session.getContextSourceNames().size(); i++){
				contextsourceStr += session.getContextSourceNames().get(i);
				if (i<session.getContextSourceNames().size()-1)
					contextsourceStr += Constants.CONTEXT_SOURCE_DELIMITER;
			}

			values.put(DatabaseNameManager.COL_SESSION_CONTEXTSOURCES, contextsourceStr);

			db.update(DatabaseNameManager.SESSION_TABLE_NAME, values, where, null);
			Log.d(LOG_TAG, "  testBackgroundLogging] Updating background logging task " + contextsourceStr);
			DatabaseManager.getInstance().closeDatabase();

		}catch(Exception e){
			e.printStackTrace();
		}

	}


    /**
     * insert a session into a TaskTable
     * @param session
     * @param table_name
     */
    public long insertSessionTable(Session session, String table_name){

    	//TODO: the user should be able to specify the database because each study may have a different database.

        Log.d(LOG_TAG, "put session " + session.getId() + " source " +  session.getContextSourceNames() + " to table " + table_name);

		long rowId=0;

		try{
            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();

            values.put(DatabaseNameManager.COL_TASK_ID, session.getTaskId());
            values.put(DatabaseNameManager.COL_TIMESTAMP_STRING, getTimeString(session.getStartTime()));
            values.put(DatabaseNameManager.COL_SESSION_START_TIME, session.getStartTime());
            values.put(DatabaseNameManager.COL_SESSION_BATTERY_LIFE, session.getBatteryLife());

            String contextsourceStr = "";
            //create a string for contextsource
            for (int i=0; i<session.getContextSourceNames().size(); i++){
                contextsourceStr += session.getContextSourceNames().get(i);
                if (i<session.getContextSourceNames().size()-1)
                    contextsourceStr += Constants.CONTEXT_SOURCE_DELIMITER;
            }

            values.put(DatabaseNameManager.COL_SESSION_CONTEXTSOURCES, contextsourceStr);

            //get row number after the insertion
            Log.d(LOG_TAG, "[testing sav and load session] Inserting session task id: " + session.getTaskId() + " id: " + session.getId() + ": Session-" + session.getStartTime() +
                      " with contextsource " + contextsourceStr +  " to the session table " + table_name);

            rowId = db.insert(table_name, null, values);


		}catch(Exception e){
			e.printStackTrace();
			rowId = -1;
		}

        DatabaseManager.getInstance().closeDatabase();

		return rowId;
    }


	/**
	 * insert a Record into the local SQLLite DB
	 * @param record
	 * @param table_name
	 * @param session_id
	 * @return
	 */
    public static long insertRecordTable(Record record, String table_name, int session_id){

		long rowId=0;
		try{

			//get DB instance
            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

			//create a ContentValues object to store record values
            ContentValues values = new ContentValues();

			//put record values into ContentValues object
            values.put(DatabaseNameManager.COL_SESSION_ID, session_id);
            values.put(DatabaseNameManager.COL_TIMESTAMP_STRING, getTimeString(record.getTimestamp()));
            values.put(DatabaseNameManager.COL_TIMESTAMP_LONG, record.getTimestamp());
			//data is JSON String
			values.put(DatabaseNameManager.COL_DATA, record.getData().toString());

//            Log.d(LOG_TAG, "[insertRecordTable][SaveRecordsToLocalDatabase]  Going to inserting record "  + record.getSource() + ": at " + record.getTimestamp() + " : " + record.getTimeString() + " in session " + session_id +
//            " to the record table " + table_name + " content" + record.getData().toString());

			rowId = db.insert(table_name, null, values);

//			Log.d(LOG_TAG, "[insertRecordTable][SaveRecordsToLocalDatabase]  Inserting successfully! The " + table_name + " table now has " + rowId + " rows.");

		}catch(Exception e){
			e.printStackTrace();
			rowId = -1;
		}

        DatabaseManager.getInstance().closeDatabase();

		return rowId;
    }




    public ArrayList<String> queryUnUploadedData(long lastSynTimestamp){

    	ArrayList<String> s = new ArrayList<String>();

    	return s;
    }



    /*****
     *
     * Remove data
     */


    public static void removeQuestionnaires() {
        try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            db.execSQL("delete from "+ DatabaseNameManager.QUESTIONNAIRE_TABLE_NAME);
            DatabaseManager.getInstance().closeDatabase();

        }catch (Exception e){

        }
    }

    public static void removeTasks() {
        try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            db.execSQL("delete from "+ DatabaseNameManager.TASK_TABLE_NAME);
            DatabaseManager.getInstance().closeDatabase();

        }catch (Exception e){

        }
    }


    public static void removeConfigurations() {
        try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            db.execSQL("delete from "+ DatabaseNameManager.CONFIGURATION_TABLE_NAME);
            DatabaseManager.getInstance().closeDatabase();

        }catch (Exception e){

        }
    }


    /*****
     *
     * Query Tables
     *
     *
     */




    //query configuration table
    public static ArrayList<String> queryConfigurations(){

    	ArrayList<String> rows = new ArrayList<String>();

    	try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
    		String sql = "SELECT *"  +" FROM " + DatabaseNameManager.CONFIGURATION_TABLE_NAME ;

    		Cursor cursor = db.rawQuery(sql, null);
    		int columnCount = cursor.getColumnCount();
			while(cursor.moveToNext()){
				String curRow = "";
				for (int i=0; i<columnCount; i++){
					curRow += cursor.getString(i)+ Constants.DELIMITER;
				}
				rows.add(curRow);
			}
			cursor.close();
            DatabaseManager.getInstance().closeDatabase();

    	}catch (Exception e){

    	}

    	return rows;

    }


    //query task table
    public static ArrayList<String> queryTasks (){

    	ArrayList<String> rows = new ArrayList<String>();

    	try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
    		String sql = "SELECT *"  +" FROM " + DatabaseNameManager.TASK_TABLE_NAME ;

    		Cursor cursor = db.rawQuery(sql, null);
    		int columnCount = cursor.getColumnCount();
			while(cursor.moveToNext()){
				String curRow = "";
				for (int i=0; i<columnCount; i++){
					curRow += cursor.getString(i)+ Constants.DELIMITER;
				}
				rows.add(curRow);
			}
			cursor.close();

            DatabaseManager.getInstance().closeDatabase();

    	}catch (Exception e){

    	}


    	return rows;

    }


	public static  ArrayList<String> queryQuestionnairesAfterId (int id) {


		ArrayList<String> rows = new ArrayList<String>();

		try{

			SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
			String sql = "SELECT *"  +" FROM " + DatabaseNameManager.QUESTIONNAIRE_TABLE_NAME +
					" where " + DatabaseNameManager.COL_ID + " > " + id ;
//					+
//					" order by " + DatabaseNameManager.COL_ID;

			 Log.d(LOG_TAG, "[test qu queryquestionnaire after id] the query statement is " +sql);

			Cursor cursor = db.rawQuery(sql, null);
			int columnCount = cursor.getColumnCount();
			while(cursor.moveToNext()){
				String curRow = "";
				for (int i=0; i<columnCount; i++){
					curRow += cursor.getString(i)+ Constants.DELIMITER;
				}
				rows.add(curRow);
			}
			cursor.close();

			DatabaseManager.getInstance().closeDatabase();

		}catch (Exception e){

		}


		return rows;
	}



    public static  ArrayList<String> querySessionsBetweenTimes(long startTime, long endTime) {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + DatabaseNameManager.SESSION_TABLE_NAME +
                    " where " + DatabaseNameManager.COL_SESSION_START_TIME + " > " + startTime + " and " +
                                DatabaseNameManager.COL_SESSION_START_TIME + " < " + endTime +
                                " order by " + DatabaseNameManager.COL_SESSION_START_TIME;

           // Log.d(LOG_TAG, "[querySessionsBetweenTimes] the query statement is " +sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DatabaseManager.getInstance().closeDatabase();

        }catch (Exception e){

        }


        return rows;

    }


    //query task table
    public static ArrayList<String> queryModifiedSessions (){

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + DatabaseNameManager.SESSION_TABLE_NAME + " where " +
                    DatabaseNameManager.COL_SESSION_MODIFIED_FLAG + " = 1";

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DatabaseManager.getInstance().closeDatabase();

        }catch (Exception e){

        }


        return rows;

    }




    //query task table
    public static ArrayList<String> querySessions (){

    	ArrayList<String> rows = new ArrayList<String>();

    	try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
    		String sql = "SELECT *"  +" FROM " + DatabaseNameManager.SESSION_TABLE_NAME ;

    		Cursor cursor = db.rawQuery(sql, null);
    		int columnCount = cursor.getColumnCount();
			while(cursor.moveToNext()){
				String curRow = "";
				for (int i=0; i<columnCount; i++){
					curRow += cursor.getString(i)+ Constants.DELIMITER;
				}
				rows.add(curRow);
			}
			cursor.close();

            DatabaseManager.getInstance().closeDatabase();

    	}catch (Exception e){

    	}


    	return rows;

    }

    public static ArrayList<String> querySession(int sessionId){

//		Log.d(LOG_TAG, "[querySession] getsession " + sessionId);

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

            String sql = "SELECT *"  +" FROM " + DatabaseNameManager.SESSION_TABLE_NAME +
                    //condition with session id
                    " where " + DatabaseNameManager.COL_ID + " = " + sessionId + "";

//            Log.d(LOG_TAG, "[querySession] the query statement is " +sql);

            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                rows.add(curRow);
            }
            cursor.close();

            DatabaseManager.getInstance().closeDatabase();


        }catch (Exception e){

        }

		Log.d(LOG_TAG, "[querySession] the session is " +rows);

        return rows;


    }

    //get the number of existing session
    public static long queryTaskCount (){

    	long count = 0;

    	try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
    		String sql = "SELECT * "  +" FROM " + DatabaseNameManager.TASK_TABLE_NAME ;
    		Cursor cursor = db.rawQuery(sql, null);
    		count = cursor.getCount();

			cursor.close();

            DatabaseManager.getInstance().closeDatabase();


    	}catch (Exception e){

    	}

    	return count;

    }

    //get the number of existing session
    public static long querySessionCount (){


    	long count = 0;

    	try{
            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
    		String sql = "SELECT * "  +" FROM " + DatabaseNameManager.SESSION_TABLE_NAME ;
    		Cursor cursor = db.rawQuery(sql, null);
    		count = cursor.getCount();

			cursor.close();

            DatabaseManager.getInstance().closeDatabase();

    	}catch (Exception e){

    	}
        return count;

    }


    /*
    private static String generateTimeConstraintSQL(ArrayList<Criterion> timeconstraints){

    	String sql="";
    	float recency = -1;
    	long timestamp = -1;

    	if (timeconstraints.size()==0){
    		return null;
    	}

    	for (int i = 0; i<timeconstraints.size(); i++){

    		Criterion tc = timeconstraints.get(i);

    		if (tc.getType().equals(ConditionManager.CONDITION_TIME_CONSTRAINT_RECENCY) ){
    			recency = tc.getInterval();
    		}
    		else if (tc.getType().equals(ConditionManager.CONDITION_TIME_CONSTRAINT_EXACTTIME)){
    			timestamp = tc.getExactTime();
    		}

    	}


		//add time into SQL
		if (recency!=-1){

			long targetTime =  getCurrentTimeInMilli() - (long)(recency* Constants.MILLISECONDS_PER_SECOND) ;

			sql += DatabaseNameManager.COL_TIMESTAMP_LONG + " > "  + targetTime;

		}
		else if (timestamp!=-1){

			//TODO: exact time

		}


    	return sql;

    }
*/


    public static ArrayList<String> queryLastRecord(String table_name, int sessionId) {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + table_name  +
                    " where " + DatabaseNameManager.COL_SESSION_ID + " = " + sessionId +
                    " order by " + DatabaseNameManager.COL_ID + " DESC LIMIT 1";
                    ;


            Log.d(LOG_TAG, "[queryLastRecord] the query statement is " +sql);

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                Log.d(LOG_TAG, "[queryLastRecord] get result row " +curRow);

                rows.add(curRow);
            }
            cursor.close();


            DatabaseManager.getInstance().closeDatabase();

        }catch (Exception e){

        }


        return rows;
    }

    public static ArrayList<String> queryRecordsInSession(String table_name, int sessionId, long startTime, long endTime) {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + table_name  +
                    " where " + DatabaseNameManager.COL_SESSION_ID + " = " + sessionId + " and " +
                    DatabaseNameManager.COL_TIMESTAMP_LONG + " > " + startTime + " and " +
                    DatabaseNameManager.COL_TIMESTAMP_LONG + " < " + endTime  +
                    " order by " + DatabaseNameManager.COL_TIMESTAMP_LONG;

//            Log.d(LOG_TAG, "[queryRecordsInSession][testgetdata] the query statement is " +sql);

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
//                    Log.d(LOG_TAG, "[queryRecordsInSession][testgetdata] column " + i + " content: " + cursor.getString(i));
                    curRow += cursor.getString(i)+ Constants.DELIMITER;

                }
//                Log.d(LOG_TAG, "[queryRecordsInSession][testgetdata] get result row " +curRow);

                rows.add(curRow);
            }
            cursor.close();

            DatabaseManager.getInstance().closeDatabase();


        }catch (Exception e){

        }


        return rows;


    }

    public static ArrayList<String> queryRecordsInSession(String table_name, int sessionId) {

        ArrayList<String> rows = new ArrayList<String>();

        try{

            SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
            String sql = "SELECT *"  +" FROM " + table_name  +
                    " where " + DatabaseNameManager.COL_SESSION_ID + " = " + sessionId +
                    " order by " + DatabaseNameManager.COL_TIMESTAMP_LONG;


            Log.d(LOG_TAG, "[queryRecordsInSession] the query statement is " +sql);

            //execute the query
            Cursor cursor = db.rawQuery(sql, null);
            int columnCount = cursor.getColumnCount();
            while(cursor.moveToNext()){
                String curRow = "";
                for (int i=0; i<columnCount; i++){
                    curRow += cursor.getString(i)+ Constants.DELIMITER;
                }
                Log.d(LOG_TAG, "[queryRecordsInSession] get result row " +curRow);

                rows.add(curRow);
            }
            cursor.close();

            DatabaseManager.getInstance().closeDatabase();

        }catch (Exception e){

        }


        return rows;


    }


    /**query result from multiple columns and values**/
    public static ArrayList<String> queryFromMultipleColumns(String table_name, int sessionid, ArrayList<String> columns, ArrayList<String> relations, ArrayList<String> values, ArrayList<Criterion> timeconstraints){


    	ArrayList<String> rows = new ArrayList<String>();

    	return rows;
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
	private static long getCurrentTimeInMilli(){
		//get timzone
		TimeZone tz = TimeZone.getDefault();
		Calendar cal = Calendar.getInstance(tz);
		long t = cal.getTimeInMillis();

		return t;
	}

	/**Generate a formated time string (in the format of "yyyy-MM-dd HH:mm:ss" **/
	private static String getTimeString(long time) {

		SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
		String timeString = sdf_now.format(time);

		return timeString;
	}



}
