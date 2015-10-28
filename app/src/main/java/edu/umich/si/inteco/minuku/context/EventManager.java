package edu.umich.si.inteco.minuku.context;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.data.LocalDBHelper;
import edu.umich.si.inteco.minuku.model.Circumstance;
import edu.umich.si.inteco.minuku.model.SimpleGeofence;
import edu.umich.si.inteco.minuku.model.Criterion;
import edu.umich.si.inteco.minuku.util.ConditionManager;
import edu.umich.si.inteco.minuku.util.DatabaseNameManager;

public class EventManager {

	private static final String LOG_TAG = "EventManager";
	
	private static Context mContext;
	
	private static ArrayList<Circumstance> mCircumstanceList;
	
	private static ArrayList<SimpleGeofence> mMonitoredGeofences;

	private static LocalDBHelper mLocalDBHelper;
	
	public EventManager(Context context){
		
		mContext = context;
		mCircumstanceList = new ArrayList<Circumstance>();
		mLocalDBHelper = new LocalDBHelper(mContext, Constants.TEST_DATABASE_NAME);
	}


	private static boolean eventPassTimeConstraint(ArrayList<String> results, ArrayList<Criterion> timeconstraints){
		
		boolean pass = true; 
    	
    	//duration: check if the latest timestamp  - earliest timestamp 
    	String[] lastResult = results.get(results.size()-1).split(Constants.DELIMITER);
    	String[] firstResult = results.get(0).split(Constants.DELIMITER);
    	
    	long earliestTime = Long.parseLong( firstResult[DatabaseNameManager.COL_INDEX_RECORD_TIMESTAMP_LONG] );
    	long latestTime = Long.parseLong( lastResult[DatabaseNameManager.COL_INDEX_RECORD_TIMESTAMP_LONG] );
    	int duration = (int) (latestTime - earliestTime)/ Constants.MILLISECONDS_PER_SECOND ;
    	/*
    	Log.d (LOG_TAG, "[ eventPassTimeConstraint] the earliest time is " + getTimeString (earliestTime) 
    			+ " the latest time is " + getTimeString (latestTime) + " the duration is " + duration + " seconds " );
    	*/
    	for (int i = 0; i<timeconstraints.size(); i++){

    		Criterion tc = timeconstraints.get(i);
    		
    		
    		float recency_criteria = -1;
        	long timestamp_criteria = -1;
        	float duration_criteria = -1;
        	
    		if (tc.getType().equals(ConditionManager.CONDITION_TIME_CONSTRAINT_RECENCY) ){
    		/*
    			Log.d (LOG_TAG, "[ eventPassTimeConstraint] get constraint " + tc.getType() + " relationship " + tc.getRelationship()
    					+ tc.getInterval());
        		*/
    			recency_criteria = tc.getInterval();
    		}
    		else if (tc.getType().equals(ConditionManager.CONDITION_TIME_CONSTRAINT_EXACTTIME)){
    			/*
    			Log.d (LOG_TAG, "[ eventPassTimeConstraint] get constraint " + tc.getType() + " relationship " + tc.getRelationship()
    					+ tc.getExactTime());
    					*/
    			timestamp_criteria = tc.getExactTime();
    		}
    		else if (tc.getType().equals(ConditionManager.CONDITION_TIME_CONSTRAINT_DURATION)){
    			/*
    			Log.d (LOG_TAG, "[ eventPassTimeConstraint] get constraint " + tc.getType() + " relationship " + tc.getRelationship()
    					+ tc.getInterval());
    					*/
    			duration_criteria = tc.getInterval();
    		}
    		
    		if (duration_criteria!=-1){
//    			Log.d (LOG_TAG, "[ eventPassTimeConstraint] duration criteria is " + duration_criteria + " duration is "  + duration + ", before examine duration, pass is " + pass);
    			pass= pass & ConditionManager.isSatisfyingCriteria(duration, tc.getRelationship(),duration_criteria);
  //  			Log.d (LOG_TAG, "[ eventPassTimeConstraint] after examine duration, pass is " + pass);
    		}
    		

    	}
		
		return pass;
		
	}

	
	public static void setEventList(ArrayList<Circumstance> circumstanceList){
		mCircumstanceList = circumstanceList;
	}
	
	public static ArrayList<Circumstance> getEventList(){
		return mCircumstanceList;
	}
	
	public static void addMonitoredGeofence(SimpleGeofence simpleGeofence){
		
		if (mMonitoredGeofences==null){
			mMonitoredGeofences= new ArrayList<SimpleGeofence>();
		}
		mMonitoredGeofences.add(simpleGeofence);
	}
	

	public static void setMonitoredGeofences(ArrayList<SimpleGeofence> geofences){
		mMonitoredGeofences = geofences;
	}
	
	public static ArrayList<SimpleGeofence> getMonitoredGeofences(){		
		return mMonitoredGeofences;
	}
	
	public static void removeSimpleGeofence(SimpleGeofence simpleGeofence){
		
		if (mMonitoredGeofences!=null){
			mMonitoredGeofences.remove(simpleGeofence);
		}
		
	}
	
	
	public static void addEvent(Circumstance circumstance){
		
		if (mCircumstanceList ==null){
			mCircumstanceList = new ArrayList<Circumstance>();
		}
		mCircumstanceList.add(circumstance);
	}
	
	public static void removeEvent(Circumstance circumstance){
		
		if (mCircumstanceList !=null){
			mCircumstanceList.remove(circumstance);
		}
		
	}
	
	public static void removeEvent(int index){
		
		if (mCircumstanceList !=null){
			mCircumstanceList.remove(index);
		}
		
	}
	
	public static Circumstance getEventById(int id){
		
		//connect the event object with the action object..
		
		for (int j= 0; j< mCircumstanceList.size(); j++){
			
			Circumstance evt;
			if (mCircumstanceList.get(j).getId()==id){
				evt = mCircumstanceList.get(j);
				
				return evt;
			}
			
		}
		
		return null;
	}
	
	
	/**convert long to timestring**/
	
	public static String getTimeString(long time){		

		SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
		String currentTimeString = sdf_now.format(time);
		
		return currentTimeString;
	}
	
	
	
}
