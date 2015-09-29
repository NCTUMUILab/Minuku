package edu.umich.si.inteco.minuku.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.model.actions.Action;
import edu.umich.si.inteco.minuku.util.ConditionManager;
import edu.umich.si.inteco.minuku.util.TriggerManager;

public class Event extends ProbeObject{

	/** Tag for logging. */
    private static final String LOG_TAG = "Event";
    
    /**property**/
    private String _name;
    private String _description="NA";
    private Task _task;
    private JSONArray _conditionJSON = null;
	private Schedule mSchedule;
    
    /**member**/
    private ArrayList<Condition> mConditionSet;
    private ArrayList<Action> mTriggeredActionList;
    
    
    
    /**
     * Constructor
     */
    
    public Event(String name){
    	super();
    	_name = name;
    	mConditionSet = new ArrayList<Condition>();
        _class = TriggerManager.PROBE_OBJECT_CLASS_EVENT;

    }
    
    public Event(int id, String name, int study_id){
    	super();
    	_id = id;
    	_name = name;
    	_studyId = study_id;
    	mConditionSet = new ArrayList<Condition>();
    	 _class = TriggerManager.PROBE_OBJECT_CLASS_EVENT;
    }
    
    
    public Event(int id, String name, String description){
    	super();
    	_id = id;
    	_name = name;
    	_description = description;
    	mConditionSet = new ArrayList<Condition>();
    	 _class = TriggerManager.PROBE_OBJECT_CLASS_EVENT;
    }    

    public Event(String name, Task task){
    	super();
    	_name = name;
    	_task = task;
    	mConditionSet = new ArrayList<Condition>();
    	_task.getEventList().add(this);
    	 _class = TriggerManager.PROBE_OBJECT_CLASS_EVENT;
    }
    

    public void setTask(Task task){
    	_task = task;
    }
    
    public Task getTask (){
    	return _task;
    }
    
    public void setName(String name){
    	_name = name;
    }
    
    public String getName(){
    	return _name;
    }
    
    
    public void setConditionJSON(JSONArray conditionJSON){
    	_conditionJSON = conditionJSON;
    }
    
    public JSONArray getConditionJSON(){
    	return _conditionJSON;
    }
    
    public void setConditionSet(ArrayList<Condition> conditionSet ){
    	
    	mConditionSet = conditionSet;
    }
    
    public ArrayList<Condition> getConditionSet(){
    	
    	return mConditionSet;
    }
    
    public void addCondition(Condition condition){
    	
    	mConditionSet.add(condition);
    }
    
    public void addCondition(String type, String relationship, String targetValue){
    	
    	Condition condition = new Condition(type, relationship, targetValue);
    	mConditionSet.add(condition);
    	
    }
    
    public void addCondition(String type, String relationship, float targetValue){
    	
    	Condition condition = new Condition(type, relationship, targetValue);
    	mConditionSet.add(condition);
    	
    }
    
    public void addCondition(String type, double latitude, double longitude, String relationship, float targetValue){
    	
    	Condition condition = new Condition(type, latitude, longitude, relationship, targetValue);
    	mConditionSet.add(condition);
    	
    }
    
    public void addCondition(String type, double latitude, double longitude, String relationship, float upper, float lower){
    	
    	Condition condition = new Condition(type, latitude, longitude, relationship, upper, lower);
    	mConditionSet.add(condition);
    	
    	
    	
    }
    
    /**
     * create JSON object according to the condition set arraylist
     * @return json object
     */
    
    

	
	
    //there is a bug in this function
    public String getConditionsInJSONString (){
    	
    	String jsonString = null; 
    	JSONObject jObjectConditionSet= new JSONObject();
    	
    	//convert condition into JSON object 
    	
    	try {
    		
    		for (int i=0; i <mConditionSet.size(); i++){
        		
        		Condition condition = mConditionSet.get(i);
        		
        		JSONObject jObjectCondition = new JSONObject();
        		
        		//add type & relationship
    			jObjectCondition.put("type", condition.getType());
        		jObjectCondition.put("relationship", condition.getRelationship());
        		
        		/** Location **/
        		if (condition.getType()==ConditionManager.CONDITION_TYPE_DISTANCE_TO){       			
        			jObjectCondition.put("lat", condition.getLatLng().latitude);
        			jObjectCondition.put("lng", condition.getLatLng().longitude);
        			jObjectCondition.put("floatTargetValue", condition.getFloatTargetValue());
        			Log.d(LOG_TAG, "the generated jason object is " + jObjectCondition.toString(4));

        		}
        		/** Activity **/
        		if (condition.getType()==ConditionManager.CONDITION_TYPE_ACTIVITY_TYPE){	      			
        			jObjectCondition.put("stringTargetValue", condition.getStringTargetValue());
        			Log.d(LOG_TAG, "the generated jason object is " + jObjectCondition.toString(4));
        		}
        		
        		if (condition.getType()==ConditionManager.CONDITION_TYPE_ACTIVITY_CONFIDENCE){
        			
        			
        		}
        		Log.d(LOG_TAG, "adding json to the condition");
        		
        		
        		/**check if there's constraint to add to the condition**/      		
        		JSONObject jObjectConstraint = new JSONObject();       		
    	
    			for (int j=0; j<condition.getTimeConstraints().size(); j++){
        			
    				Log.d(LOG_TAG, "adding constraint...");
        			TimeConstraint constraint = condition.getTimeConstraints().get(j);
        			jObjectConstraint.put("type", constraint.getType());
        			jObjectConstraint.put("relationship", constraint.getRelationship());
        			
        			if (constraint.getType().equals(ConditionManager.CONDITION_TIME_CONSTRAINT_RECENCY) || 
        					constraint.getType().equals(ConditionManager.CONDITION_TIME_CONSTRAINT_DURATION) ){
        			
        				jObjectConstraint.put("targetValue", constraint.getInterval());
        				
        				
        			}if (constraint.getType().equals(ConditionManager.CONDITION_TIME_CONSTRAINT_EXACTTIME)){
        			
        				jObjectConstraint.put("targetValue", constraint.getExactTime());
        				
        				
        			}if (constraint.getType().equals(ConditionManager.CONDITION_TIME_CONSTRAINT_TIMEOFDAY)){
        			
        				jObjectConstraint.put("targetValue", constraint.getTimeDay());
        			
        			}
        			
        			
        		}
    			
    			Log.d(LOG_TAG, "going to adding json constraint to the condition");
    			
    			//add constraint to the JSONCondition
    			jObjectCondition.put("Constraint", jObjectConstraint);

    			jObjectConditionSet.put("Condition", jObjectConditionSet);
    			
    		}//1st for loop
    		
    		Log.d(LOG_TAG, "going to print the jason object");
    		Log.d(LOG_TAG, "the generated jason object is " + jObjectConditionSet.toString(4));

    		
    	}catch (Exception e){
    		
    		
    		
    	}
    	
    	
    	return jsonString;
    }
    
    
    public void getCondition(int index){
        
    	mConditionSet.get(index);
    }

    public void removeCondition(int index){
    
    	mConditionSet.remove(index);
    }
    
    public void removeCondition(Condition condition){
        
    	mConditionSet.remove(condition);
    }
    
	public String getDescription(){
		return _description;
	} 
	
	public void setDescription(String d){
		_description = d;
	} 

	public Schedule getSchedule (){
		return mSchedule;
	}
	
	public void setSchedule(Schedule schedule){
		mSchedule = schedule;
	}
	
}
