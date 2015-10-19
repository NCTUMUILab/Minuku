package edu.umich.si.inteco.minuku.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.model.actions.Action;
import edu.umich.si.inteco.minuku.util.ConditionManager;
import edu.umich.si.inteco.minuku.util.TriggerManager;

public class Event extends ProbeObject{
	
    private static final String LOG_TAG = "Event";
	
    private String mName;
    private String mDescription="NA";
    private ArrayList<Condition> mConditionList;

    public Event(String name){
    	super();
    	mName = name;
    	mConditionList = new ArrayList<Condition>();
        _class = TriggerManager.PROBE_OBJECT_CLASS_EVENT;

    }
    
    public Event(int id, String name, int study_id){
    	super();
    	_id = id;
    	mName = name;
    	_studyId = study_id;
    	mConditionList = new ArrayList<Condition>();
    	 _class = TriggerManager.PROBE_OBJECT_CLASS_EVENT;
    }
    
    
    public Event(int id, String name, String description){
    	super();
    	_id = id;
    	mName = name;
    	mDescription = description;
    	mConditionList = new ArrayList<Condition>();
    	 _class = TriggerManager.PROBE_OBJECT_CLASS_EVENT;
    }    

    public Event(String name, Task task){
    	super();
    	mName = name;
    	mConditionList = new ArrayList<Condition>();
    	 _class = TriggerManager.PROBE_OBJECT_CLASS_EVENT;
    }

    public void setName(String name){
    	mName = name;
    }
    
    public String getName(){
    	return mName;
    }

    public void setConditionList(ArrayList<Condition> ConditionList ){
    	mConditionList = ConditionList;
    }
    
    public ArrayList<Condition> getConditionList(){
    	return mConditionList;
    }
    
    public void addCondition(Condition condition){
    	mConditionList.add(condition);
    }
    
    public void addCondition(String stateName, String stateValue){
    	Condition condition = new Condition(stateName, stateValue);
    	mConditionList.add(condition);
    }

    public void getCondition(int index){
    	mConditionList.get(index);
    }

    public void removeCondition(int index){
    	mConditionList.remove(index);
    }
    
    public void removeCondition(Condition condition){
    	mConditionList.remove(condition);
    }
    
	public String getDescription(){
		return mDescription;
	} 
	
	public void setDescription(String d){
		mDescription = d;
	} 

	
}
