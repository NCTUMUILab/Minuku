package edu.umich.si.inteco.minuku.context;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.data.DataHandler;
import edu.umich.si.inteco.minuku.data.LocalDBHelper;
import edu.umich.si.inteco.minuku.model.Condition;
import edu.umich.si.inteco.minuku.model.Event;
import edu.umich.si.inteco.minuku.model.ProbeObject;
import edu.umich.si.inteco.minuku.model.ProbeObjectControl.ActionControl;
import edu.umich.si.inteco.minuku.model.SimpleGeofence;
import edu.umich.si.inteco.minuku.model.TimeConstraint;
import edu.umich.si.inteco.minuku.model.TriggerLink;
import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.ConditionManager;
import edu.umich.si.inteco.minuku.util.DatabaseNameManager;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;
import edu.umich.si.inteco.minuku.util.TriggerManager;

public class EventManager {

	private static final String LOG_TAG = "EventManager";
	
	private static Context mContext;
	
	private static ArrayList<Event> mEventList;
	
	private static ArrayList<SimpleGeofence> mMonitoredGeofences;

	private static LocalDBHelper mLocalDBHelper;
	
	public EventManager(Context context){
		
		mContext = context;
		mEventList = new ArrayList<Event>();
		mLocalDBHelper = new LocalDBHelper(mContext, Constants.TEST_DATABASE_NAME);
	}


	private static boolean eventPassTimeConstraint(ArrayList<String> results, ArrayList<TimeConstraint> timeconstraints){
		
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

    		TimeConstraint tc = timeconstraints.get(i);
    		
    		
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

	
	public static void setEventList(ArrayList<Event> eventList){
		mEventList = eventList;
	}
	
	public static ArrayList<Event> getEventList(){		
		return mEventList;
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
	
	
	public static void addEvent(Event event){
		
		if (mEventList==null){
			mEventList = new ArrayList<Event>();
		}
		mEventList.add(event);
	}
	
	public static void removeEvent(Event event){
		
		if (mEventList!=null){
			mEventList.remove(event);
		}
		
	}
	
	public static void removeEvent(int index){
		
		if (mEventList!=null){
			mEventList.remove(index);
		}
		
	}
	
	public static Event getEventById(int id){
		
		//connect the event object with the action object..
		
		for (int j= 0; j< mEventList.size(); j++){
			
			Event evt;
			if (mEventList.get(j).getId()==id){
				evt = mEventList.get(j); 
				
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
