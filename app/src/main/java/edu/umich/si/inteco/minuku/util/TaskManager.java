package edu.umich.si.inteco.minuku.util;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.GlobalNames;
import edu.umich.si.inteco.minuku.data.LocalDBHelper;
import edu.umich.si.inteco.minuku.model.Task;

public class TaskManager {

	/** Tag for logging. */
    private static final String LOG_TAG = "LocalDBHelper";
    
	private static ArrayList<Task> mTaskList;
	private static LocalDBHelper mLocalDBHelper;
	private Context mContext;
	
	public TaskManager(Context context){
		mContext = context;
		mTaskList = new ArrayList<Task>();
		mLocalDBHelper = new LocalDBHelper(mContext, GlobalNames.TEST_DATABASE_NAME);
		loadTask();
	}
	
	/**
	 * The task manager should read tasks from the database. If there's no task in the databse, the taskManager should create one.
	 */
	public void loadTask(){
		
		//check tasks from the database
        ArrayList<String> res = new ArrayList<String>();
        res = mLocalDBHelper.queryTasks();
        Log.d(LOG_TAG, "there are " + res.size() + " tasks in the database");

		//if only the background recording task is in the database, ;oa
				
		if (res.size()<=1){
			loadTestingTasksFromAsset();
		}
			
		//there are existing tasks in the database
		else {
			
			//if there are tasks in the database, load them into the memory
			for (int i=0; i<res.size() ; i++){
				String t = res.get(i);
				Log.d(LOG_TAG, " the " + i + " task is:  " + t );

				//get properties of each task.
				String [] separated = t.split(";;;");
			    
				int id  = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_TASK_ID]);
				int study_id =  Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_TASK_STUDY_ID]);
				String name = separated[DatabaseNameManager.COL_INDEX_TASK_NAME];
				String description = separated[DatabaseNameManager.COL_INDEX_TASK_DESCRIPTION];	
				long startTime = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_TASK_START_TIME]);
				long endTime = Long.parseLong(separated[DatabaseNameManager.COL_INDEX_TASK_END_TIME]);
				
				Log.d(LOG_TAG, "get task from the database: id: " + id + " , study id: " + study_id + " name " + name + " , " + description + " , at " + 
			    " , start  " + startTime + " , end: " + endTime);
				
				
				//add the task into the tasklist object
				Task task = new Task(id, name, startTime, endTime, description, study_id);
				
				mTaskList.add(task);			
			}
			
		}

	}
	
	public void loadTestingTasksFromAsset(){
		
		long rowId=0;
		
		if (mTaskList==null){
			mTaskList = new ArrayList<Task>();	
		}
		
		
		/** load tasks from the studies.txt (in JSON), after we connect to the databse, we will get the study file **/
		
		//read the study json file from assets
		String study_str= new FileHelper(mContext).loadFileFromAsset(ConfigurationManager.CONFIGURATION_FILE_NAME);
	
		//create Json object 
		try {
			
			JSONArray studyJSONArray = new JSONArray(study_str) ;
			
			for (int i=0; i< studyJSONArray.length(); i++){
				
				JSONObject studyJSON = studyJSONArray.getJSONObject(i);				
				int study_id = studyJSON.getInt(ConfigurationManager.CONFIGURATION_PROPERTIES_ID);
						
				//get task JSON
				JSONArray taskJSONArray = studyJSON.getJSONArray("Task");
				
				for (int j = 0; j < taskJSONArray.length(); j++){
					
					JSONObject taskJSON = taskJSONArray.getJSONObject(j);		
					
					//extract values
					int id = taskJSON.getInt(ConfigurationManager.TASK_PROPERTIES_ID);				
					String name = taskJSON.getString(ConfigurationManager.TASK_PROPERTIES_NAME); 
					String description = taskJSON.getString(ConfigurationManager.TASK_PROPERTIES_DESCRIPTION); 
					long createdTime = taskJSON.getLong(ConfigurationManager.TASK_PROPERTIES_CREATED_TIME);
					long startTime=  taskJSON.getLong(ConfigurationManager.TASK_PROPERTIES_START_TIME);
					long endTime = taskJSON.getLong(ConfigurationManager.TASK_PROPERTIES_END_TIME);
					
					
					Log.d(LOG_TAG, "get task from the file: id: " + id + " , " + name + " , " + description + " , " + createdTime + " , " + startTime + " , " + endTime);
					
					
					//create Task object and inset the task into the list
					Task task = new Task(id, name, startTime, endTime, description, study_id);
					
					mTaskList.add(task);
					
					//inserting tasks into the task table
					rowId=mLocalDBHelper.insertTaskTable(task, DatabaseNameManager.TASK_TABLE_NAME);
					
					
					
				}//end of reading task from JSON
				
			}
			
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		
	}

	public static ArrayList<Task> getTaskList(){
		return mTaskList;
	}


    public static Task getTask(int id) {

        for (int i=0; i<mTaskList.size(); i++){

            if (id==mTaskList.get(i).getId()){
                return mTaskList.get(i);
            }
        }
        return null;
    }

	public static Task getTaskByName(String taskName) {
		
		Task task;
		
		for (int i=0; i<mTaskList.size(); i++){
			
			task = mTaskList.get(i);		
			String name = task.getName();
			Log.d(LOG_TAG, "exmaing task name " + name);
			
			
			if (task.getName().equals(taskName))
				return task;
		}
		
		return null;
	}
	
	
}
