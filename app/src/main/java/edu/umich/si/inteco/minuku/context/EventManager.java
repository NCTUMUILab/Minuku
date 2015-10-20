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
	
	public void updateEvents(){
		
		
		
	}

	
	/**
	 * This function examines the conditions of each existing event. It interprets the conditions in each event, and call DataHandler to find the right table to query 
	 */

	/*
	public static void examineEventConditions(ArrayList<Integer> monitored_event_ids){

		//check the list of monitored event
		for (int event_index = 0; event_index < monitored_event_ids.size(); event_index ++){
			
			//get the id of each monitored event
			int monitored_event_id = monitored_event_ids.get(event_index);
			Log.d(LOG_TAG,"[examineEventConditions] examine conditions of event " + monitored_event_id);
			
			
			//get the event instance with the event id			
			for (int i=0; i<mEventList.size(); i++){
						
				Event event = mEventList.get(i);	
			//	Log.d(LOG_TAG,"[examineEventConditions] examining event " + event.getId());
				
				
				//get the monitored event, then we get the condition of the event.
				if (event.getId()==monitored_event_id) {
					
					//Log.d(LOG_TAG,"[examineEventConditions] the " + i + " event is  " + event.getName());
					
					//get conditions from the event
					ArrayList<Condition> conditionSet = event.getConditionList();
					//Log.d(LOG_TAG, "[examineEventConditions] currently there are " + conditionSet.size() + " conditions in the event " + event.getName());
					

					for (int j=0; j<conditionSet.size(); j++){
						
						Condition condition = conditionSet.get(j);


                        //check if the condition is special type...such as TransportaionModeDetection
                        if (condition.getStateName().equals(ConditionManager.CONDITION_TYPE_PROBE_TRANSPORTATION)) {



                        }

                        //normal type of condition, query database to examien conditions
                        else {


                            //get result from the DataHanlder
                            ArrayList<String> res = DataHandler.getDataByCondition(condition);
                            Log.d(LOG_TAG, "[examineEventConditions] got " + res.size() + " results,for event " + event.getId() + " : " +  event.getName() +  " need to examine timeconstraint ");

                            //if got the result, i.e. having detected  the event, finally need to check its timeconstraint (duration)
                            if (res.size() >0){

                                //get timeconstraint...
                                ArrayList<TimeConstraint> timeconstraints = condition.getTimeConstraints();

                                boolean pass = eventPassTimeConstraint(res, timeconstraints);

                                //if the event is detected
                                if (pass){

                                    //log when an event is detected
                                    LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                                            LogManager.LOG_TAG_EVENT_DETECTED,
                                            "Event detected:\t" + event.getId() + "\t" + event.getName());

                                    //check the triggerlinks of the current event to see if it would trigger any ActionControl.
                                    Log.d(LOG_TAG, "[examineEventConditions] The event " + event.getId() + "  condition is satisfied, check its triggerLinks! "+ " the event has " + event.getTriggerLinks().size() + " triggerlinks ");


                                    //execute the triggeredObject
                                    for (int k=0; k<event.getTriggerLinks().size(); k++){

                                        TriggerLink tl = event.getTriggerLinks().get(k);

                                        Log.d(LOG_TAG, "[examineEventConditions] the triggerdlinks's trigger is " + tl.getTriggerClass() + " " +
                                                tl.getTrigger().getId() + " and it triggers object " + tl.getTriggeredProbeObject().getProbeObjectClass() + " " +  tl.getTriggeredProbeObject().getId() + " , of which the class is "
                                                + tl.getTriggeredProbeObject().getProbeObjectClass());

                                        ProbeObject triggeredObject = tl.getTriggeredProbeObject();

                                        if (triggeredObject.getProbeObjectClass().equals(TriggerManager.PROBE_OBJECT_CLASS_ACTION_CONTROL)){
                                            ActionControl ac = (ActionControl) triggeredObject;
                                            //		Log.d(LOG_TAG, "[examineEventConditions] found the triggered probe object is action control" + ac.getId() + " of which the action is " + ac.getAction().getName()  );

                                            //schedule the action control
                                            ScheduleAndSampleManager.registerActionControl(ac);

                                            //log triggering actioncontrol
                                            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                                                    LogManager.LOG_TAG_ACTION_TRIGGER,
                                                    "Triggering ActionControl:\t" + ActionManager.getActionControlTypeName(ac.getType()) + "\t" + ac.getAction().getName());
                                        }

                                        else if (triggeredObject.getClass().equals(TriggerManager.PROBE_OBJECT_CLASS_EVENT)){


                                        }

                                        else if (triggeredObject.getClass().equals(TriggerManager.PROBE_OBJECT_CLASS_ACTION)){


                                        }


                                    }



                                }	//if (pass)
                            }



                        }

						
					}//conditionSet
					
				}//if (event.getId()==monitored_event_id) 
			}
			
		}//event_index
		
	}
	*/

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
