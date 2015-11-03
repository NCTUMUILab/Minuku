package edu.umich.si.inteco.minuku.model.ProbeObjectControl;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umich.si.inteco.minuku.model.ProbeObject;
import edu.umich.si.inteco.minuku.model.Schedule;
import edu.umich.si.inteco.minuku.model.TriggerLink;
import edu.umich.si.inteco.minuku.model.actions.Action;
import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.ConfigurationManager;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;
import edu.umich.si.inteco.minuku.util.TriggerManager;

public class ActionControl extends ProbeObject {

	
	
	private static final String LOG_TAG = "ActionControl";
	private int _type;
	private String _launch; 
	private Action _action;

	//Schedule
	private boolean _hasSchedule = false; 
	private boolean _triggered = false;
	private Schedule mSchedule;	
	
	
	public ActionControl (int id, JSONObject jsonObject, int type, Action action) {
		
		super();
		Log.d(LOG_TAG, "[ActionControl Constructor] " + jsonObject + " action " + action.getId() + " continuity " + action.isContinuous());
		_id = id;
		_action = action;
		_type = type;
		_class = TriggerManager.PROBE_OBJECT_CLASS_ACTION_CONTROL;
		//parse the JSONObject to load the content of the ActionControl 	
		this.setup(jsonObject);
	}
	
	private void setup(JSONObject jsonObject) {
		
		
		try {
			_launch = jsonObject.getString(ConfigurationManager.ACTION_PROPERTIES_LAUNCH);
			
			//if the control is triggered by an ProbeObject
			if (jsonObject.has(ConfigurationManager.ACTION_PROPERTIES_TRIGGER)){
				
				JSONObject triggerJSON = jsonObject.getJSONObject(ConfigurationManager.ACTION_PROPERTIES_TRIGGER);
				
				//1. get properties of the trigger
				String trigger_class = triggerJSON.getString(ConfigurationManager.ACTION_TRIGGER_CLASS_PROPERTIES);
				int trigger_id = triggerJSON.getInt(ConfigurationManager.ACTION_PROPERTIES_ID);
				float sampling_rate = (float) triggerJSON.getDouble(ConfigurationManager.ACTION_TRIGGER_PROPERTIES_SAMPLING_RATE);
				
				//needs to know this action control is used in which study
				int study_id = this.getAction().getStudyId();
				
				//add triggerlink to the list. The main service will connect triggers and triggered objects later.
				//now we just need to remember the relationship between triggers and the triggered objects 
				this._triggered = true;				
				TriggerLink tl = new TriggerLink (trigger_class, trigger_id, this, sampling_rate);
				tl.setStudyId(study_id);
				
				//add triggerlinks to the Probe service so that it can make the connections when the the app is running. 
                TriggerManager.addTriggerLink(tl);
				
				
				
				Log.d(LOG_TAG, "[ ActionControl] the control " + this._id + "  has a trigger " + trigger_class 
						+ " id " + trigger_id + " sampling rate " + sampling_rate + " in the study " + study_id);
						
			}
			
			//if the ActionControl has the schedule componen 
			if (jsonObject.has(ConfigurationManager.ACTION_PROPERTIES_SCHEDULE)){
				
				JSONObject scheduleJSON = jsonObject.getJSONObject(ConfigurationManager.ACTION_PROPERTIES_SCHEDULE);
				
				//each actionControl has one schedule and one trigger (to simplify the relationships between trigger and schedule...)
				this.mSchedule = loadScheduleFromJSON(scheduleJSON);

			}
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	
	public String getLaunchMethod(){
		return _launch;
	} 
	
	public void setLaunchMethod(String launch){
		_launch = launch;
	}
	
	public Action getAction(){
		return _action;
	}
	
	public void setAction(Action action) {
		_action = action;
	}

	public int getType(){
		return _type;
	} 
	
	public void setType(int type){
		_type = type;
	}
	
	
    /** For contiunous action**/
    public boolean isTriggered () {
    	   	
    	return _triggered;
    }
    
    public void setTriggered (boolean triggered) {  	
    	this._triggered = triggered;     	
    }
    
	
	public boolean hasSchedule(){
		return _hasSchedule;
	}
	
	public void hasSchedule(boolean flag){
		_hasSchedule = flag;
	}
	
	public Schedule getSchedule (){
			return mSchedule;
		}
		
	public void setSchedule(Schedule schedule){
		mSchedule = schedule;
	}
	
	/**
	 * Load schedule to save in a ActionControl
	 * @param scheduleJSON
	 */
	public Schedule loadScheduleFromJSON (JSONObject scheduleJSON){
		
		Schedule schedule = null; 
		Log.d(LOG_TAG, " [loadScheduleFromJSON ] loading scheduleJSON " + scheduleJSON );
		
		
		
		try {
						
			String sample_method = scheduleJSON .getString(ScheduleAndSampleManager.SCHEDULE_PROPERTIES_SAMPLE_METHOD);
			
			//instantiate the schedule object
			schedule = new Schedule(sample_method);
				
			//get delay of the schedule, the default value is 0
			int delay = 0;
			if (scheduleJSON.has(ScheduleAndSampleManager.SCHEDULE_PROPERTIES_SAMPLE_DELAY)){
				
				delay = scheduleJSON.getInt(ScheduleAndSampleManager.SCHEDULE_PROPERTIES_SAMPLE_DELAY);
			}
			
			Log.d(LOG_TAG, " [loadScheduleFromJSON ] examine schedules " + " is triggered " + "delay: " + delay);
			schedule.setSampleDelay(delay);				

			
			//if the action is scheduled at a fixed time of day, there's no need to store the sampling count and the delay, and the sampling startTime and end Time
			// we only need to know when the action should happen			
			if (sample_method.equals(ScheduleAndSampleManager.SCHEDULE_SAMPLE_METHOD_FIXED_TIME_OF_DAY) ){
				
				String time_of_day = scheduleJSON.getString(ScheduleAndSampleManager.SCHEDULE_PROPERTIES_FIXED_TIME_OF_DAY);
				schedule.setFixedTimeOfDay(time_of_day);
				
				Log.d(LOG_TAG, "[loadScheduleFromJSON] the aciton is at fixed time of day: examine schedules" + 
						 " sample method " + schedule.getSampleMethod()  + " fixed time of day " + schedule.getFixedTimeOfDay()  );
				
			}
			
			//the schedule is not at a specific time. Need to obtain related fields to calculate sampling startTime, endTime, count, etc. 
			else {
				
				if (scheduleJSON.has(ScheduleAndSampleManager.SCHEDULE_PROPERTIES_SAMPLE_COUNT)){
					
					int count  = scheduleJSON.getInt(ScheduleAndSampleManager.SCHEDULE_PROPERTIES_SAMPLE_COUNT);				
					schedule.setSampleCount(count);

					
				}
				
				
				
				//2. check sampling method: fixed interval or randomly
				
				//fixed interval
				if (sample_method.equals(ScheduleAndSampleManager.SCHEDULE_SAMPLE_METHOD_SIMPLE_ONE_TIME)){
					Log.d(LOG_TAG, " simple one time, no need to calculate sample times "); 
				}
				
				else if (sample_method.equals(ScheduleAndSampleManager.SCHEDULE_SAMPLE_METHOD_FIXED_INTERVAL) ){
								
					schedule.setInverval(scheduleJSON.getInt(ScheduleAndSampleManager.SCHEDULE_PROPERTIES_SAMPLE_INTERVAL));
					Log.d(LOG_TAG, " need to calcualte schedule end time: fixed interval " + schedule.getInterval() ); 
				}
				//randomly choose a time
				else if (  sample_method.equals(ScheduleAndSampleManager.SCHEDULE_SAMPLE_METHOD_RANDOM   ) ){
									
					Log.d(LOG_TAG, " need to calcualte schedule end time: random" ); 
				}
				//randomly choose a time but with a minimum interval 
				else if (  sample_method.equals(ScheduleAndSampleManager.SCHEDULE_SAMPLE_METHOD_RANDOM_WITH_MINIMUM_INTERVAL   ) ){
					
					schedule.setMinInverval(scheduleJSON.getInt(ScheduleAndSampleManager.SCHEDULE_PROPERTIES_SAMPLE_MINIMUM_INTERVAL));
					Log.d(LOG_TAG, " need to calcualte schedule end time: random with min interval" + schedule.getMinInterval() ); 
				}

				
				
				//2. check if the end time is obtained through duration or endTimeAt
				
				//if use the duration
				if (scheduleJSON.has(ScheduleAndSampleManager.SCHEDULE_PROPERTIES_SAMPLE_DURATION)){
				
					int duration = scheduleJSON.getInt(ScheduleAndSampleManager.SCHEDULE_PROPERTIES_SAMPLE_DURATION);
					schedule.setSampleDuration(duration);
					
					
					Log.d(LOG_TAG, "examine schedules" + " , for action " + schedule.getActionId()
							 + " sample method " + schedule.getSampleMethod()  + " delay: "  + schedule.getSampleDelay()  + " count: "
							 + schedule.getSampleCount() + "  duratoin: " + schedule.getSampleDuration());
					
				}
				//if use the end time
				else if (scheduleJSON.has(ScheduleAndSampleManager.SCHEDULE_PROPERTIES_SAMPLE_END_AT)){
					
					String endAt = scheduleJSON.getString(ScheduleAndSampleManager.SCHEDULE_PROPERTIES_SAMPLE_END_AT);
					schedule.setSampleEndAtTimeOfDay(endAt);		
					
					Log.d(LOG_TAG, "examine schedules"  + " , for action " + schedule.getActionId()
							 + " sample method " + schedule.getSampleMethod()  + " delay: "  + schedule.getSampleDelay()  + " count: "
							 + schedule.getSampleCount() + "  endAt " + schedule.getSampleEndAtTimeOfDay());
					
				}			
			}
	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return schedule;
	}
	
}
