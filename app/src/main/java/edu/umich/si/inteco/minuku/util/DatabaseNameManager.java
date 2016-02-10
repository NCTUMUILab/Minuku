package edu.umich.si.inteco.minuku.util;

public class DatabaseNameManager {

	
	/**
	 * For database:
	 * 
	 * List of Table Names
	 * 
	 * **/

	public static final String STUDY_TABLE_NAME = "Study_Table";
	
	public static final String TASK_TABLE_NAME = "Task_Table";
    
	public static final String SESSION_TABLE_NAME = "Session_Table";
	
	public static final String CONFIGURATION_TABLE_NAME = "Configuration_Table";
	
	public static final String QUESTIONNAIRE_TABLE_NAME = "Questionnaire_Table";
	
	public static final String USER_RESPONSE_TABLE_NAME = "UserResponse_Table";



    /**Record Table**/
	public static final String RECORD_TABLE_NAME_LOCATION = "Record_Table_Location";
	public static final String RECORD_TABLE_NAME_ACTIVITY_RECOGNITION = "Record_Table_ActivityRecognition";
    public static final String RECORD_TABLE_NAME_TRANSPORTATION = "Record_Table_Transportation";
    public static final String RECORD_TABLE_NAME_APPLICATION_ACTIVITY = "Record_Table_AppActivity";
	public static final String RECORD_TABLE_NAME_SENSOR_ACCELEROMETER = "Record_Table_Accelerometer";
	public static final String RECORD_TABLE_NAME_SENSOR_GRAVITY  = "Record_Table_Sensor_Gravity";
	public static final String RECORD_TABLE_NAME_SENSOR_GYRSCOPE = "Record_Table_Sensor_Gyroscope";
	public static final String RECORD_TABLE_NAME_SENSOR_ACCELERATION = "Record_Table_Sensor_Linear_Acceleration";
	public static final String RECORD_TABLE_NAME_SENSOR_ROTATION_VECTOR = "Record_Table_Sensor_Rotation_Vector";
	public static final String RECORD_TABLE_NAME_SENSOR_MAGNETIC_FIELD = "Record_Table_Sensor_Magnetic_Field";
	public static final String RECORD_TABLE_NAME_SENSOR_ORIENTATION = "Record_Table_Sensor_Orientation";
	public static final String RECORD_TABLE_NAME_SENSOR_PROXIMITY = "Record_Table_Sensor_Proximity";
	public static final String RECORD_TABLE_NAME_SENSOR_AMBIENT_TEMPERATURE = "Record_Table_Sensor_Ambient_Temperature";
	public static final String RECORD_TABLE_NAME_SENSOR_LIGHT = "Record_Table_Sensor_Light";
	public static final String RECORD_TABLE_NAME_SENSOR_PRESSURE = "Record_Table_Sensor_Pressure";
	public static final String RECORD_TABLE_NAME_SENSOR_HUMIDITY = "Record_Table_Sensor_Humidity";
	public static final String COL_ID = "_id";
	
	
	 /**
     * Columns of STUDY Table
     *
     * 
     * **/
	 //task id
    public static final String COL_STUDY_ID = "study_id";
	
    public static final String COL_STUDY_NAME= "study_name";
	
    
    /**
     * Columns of TASK Table
     * 
     * A study database has one or multiple tasks.  Therefore, the database need to maintain a table to store a list of tasks that are currently undertaken.
     * 
     * A task table has the following columns: Task ID, Participant ID
     * 
     * **/

    //task id
    public static final String COL_TASK_ID = "task_id";
    //task name
    public static final String COL_TASK_NAME = "task_name";
    
    //task description
    public static final String COL_TASK_DESCRIPTION = "task_description";   
    
    //task created time
    public static final String COL_TASK_CREATED_TIME = "task_created_time"; 
    
    //task start time
    public static final String COL_START_TIME = "start_time"; 
    
    //task end time
    public static final String COL_END_TIME = "end_time";
    
    //participant id
    public static final String COL_PARTICIPANT_ID = "participant_id";
	

    public static final int COL_INDEX_TASK_ID = 0;  
    
    public static final int COL_INDEX_TASK_STUDY_ID = 1;  
    
    public static final int COL_INDEX_TASK_NAME = 2;   
    
    public static final int COL_INDEX_TASK_DESCRIPTION = 3;
    
    public static final int COL_INDEX_TASK_CREATED = 4;
    
    public static final int COL_INDEX_TASK_START_TIME = 5;
    
    public static final int COL_INDEX_TASK_END_TIME = 6;
    
    /**
     * Columns of Configuration Table
     * 
     * **/
    
    
    //configuration version
    public static final String COL_CONFIGURATION_VERSION = "version";
    
    //configuration content
    public static final String COL_CONFIGURATION_CONTENT = "content";
    
    //configuration name
    public static final String COL_CONFIGURATION_NAME = "name"; 
    
    //configuration name
    public static final String COL_CONFIGURATION_SOURCE = "source"; 
    
    public static final String COL_QUESTIONNAIRE_ID  = "questionnaire_id";
    
    public static final String COL_CONTENT = "content";
    
    public static final int COL_INDEX_CONFIGURATION_ID = 0;
    
    public static final int COL_INDEX_CONFIGURATION_STUDY_ID = 1;
    
    public static final int COL_INDEX_CONFIGURATION_NAME = 2;    
    
    public static final int COL_INDEX_CONFIGURATION_VERSION = 3;  
    
    public static final int COL_INDEX_CONFIGURATION_CONTENT= 4;

    
    /**
     * Columns of Questionnaire Table
     * 
     * **/   
    
    public static final String COL_QUESTIONNAIRE_ATTENDED_TIME = "attended_time";
    public static final String COL_QUESTIONNAIRE_GENERATED_TIME = "generated_time";
    public static final String COL_QUESTIONNAIRE_SUBMITTED_TIME = "submitted_time";
    public static final String COL_QUESTIONNAIRE_RESPONSE = "response";
    
	
    /**
    * Columns of SESSION Table
    * 
    * A TASK has a number of sessions. A session is a bounded continuous recording (e.g. 10 minutes of recording of GPS, proximity, and self-report). Therefore 
    * each session has a number of Records associated with it (e.g. 200 Location records).  
    * 
    * A task table has the following columns: Task ID, Participant ID
    * 
    * **/
   //session id

   public static final String COL_SESSION_ID = "session_id";
   
   //session start time
   public static final String COL_SESSION_START_TIME = "session_start_time"; 
   
   //session end time
   public static final String COL_SESSION_END_TIME = "session_end_time";

   //session annotaiton set
   public static final String COL_SESSION_ANNOTATION_SET = "session_annotation_set";

    //session battery percentage
    public static final String COL_SESSION_BATTERY_LIFE = "session_battery_life";

    //session has been changed flag
    public static final String COL_SESSION_MODIFIED_FLAG = "session_modified_flag";

    //session has been changed flag
    public static final String COL_SESSION_CONTEXTSOURCES = "session_contextsources";

    public static final int COL_INDEX_SESSION_ID = 0;

    public static final int COL_INDEX_SESSION_TASK_ID = 1;

    public static final int COL_INDEX_SESSION_TIMESTAMP_STRING = 2;

    public static final int COL_INDEX_SESSION_START_TIME = 3;

    public static final int COL_INDEX_SESSION_END_TIME= 4;

    public static final int COL_INDEX_SESSION_ANNOTATION_SET= 5;

    public static final int COL_INDEX_SESSION_BATTERY_LIFE = 6;

    public static final int COL_INDEX_SESSION_MODIFIED_FLAG = 7;

    public static final int COL_INDEX_SESSION_CONTEXTSOURCES = 8;
   
    /**
     * Columns of RECORD Table. 
     * 
     * There are different types of Record tables, each of which may have a same or a different format. For example, the schema of LocationTable is 
     * different from of the accelerometer table. However, the schema of the accelerometer table is identical to that of the gyroscope table. Therefore, 
     * when creating a record table (usually is when the app is firstly installed..), a record type should be specified. 
     * 
     * A task table has the following columns: Task ID, Participant ID
     * 
     * **/   
	
    //timestamp
	public static final String COL_TIMESTAMP_STRING = "timestamp_string";
	
	public static final String COL_TIMESTAMP_LONG = "timestamp_long";

    public static final String COL_DATA = "data";

    public static final int COL_INDEX_RECORD_ID = 0;

    public static final int COL_INDEX_RECORD_SESSION_ID = 1;

    public static final int COL_INDEX_RECORD_DATA = 2;

	public static final int COL_INDEX_RECORD_TIMESTAMP_STRING = 3;
	
	public static final int COL_INDEX_RECORD_TIMESTAMP_LONG = 4;



    //JSON properties name for remote DB
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_OTHERS= "others";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_STUDY_CONDITION= "study_condition";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_NAME = "name";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_TIMESTAMP_HOUR = "timestamp_hour";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_START_TIME = "start_time";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_END_TIME = "end_time";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_BATTERY_LIFE = "battery_life";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_RECORDS = "records";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_TASK = "task";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_ANNOTATIONSET = "annotation_set";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_ID = "_id";
    public static final String MONGO_DB_DOCUMENT_STUDY_CONDITION = "condition";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_DEVICE_ID = "device_id";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_USER_ID = "user_id";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_SESSION_ID = "session_id";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_HAS_DOCUMENT = "has_document";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_LAST_SYNC_HOUR_TIME = "last_sync_hour_time";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_DATA_TYPE = "data_type";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_LAST_SYNC_SESSION_ID = "last_sync_session_id";


    public static final String MONGO_DB_DOCUMENT_PROPERTIES_EMAIL_SUBJECT = "email_subject";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_EMAIL_CONTENT = "email_content";
    public static final String MONGO_DB_DOCUMENT_PROPERTIES_TRIPS = "trips";

}
