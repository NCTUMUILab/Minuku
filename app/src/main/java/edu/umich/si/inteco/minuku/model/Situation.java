package edu.umich.si.inteco.minuku.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.model.actions.Action;
import edu.umich.si.inteco.minuku.util.ConditionManager;
import edu.umich.si.inteco.minuku.util.TriggerManager;

public class Situation extends ProbeObject{
	
    private static final String LOG_TAG = "Situation";
	
    private String mName;
    private String mDescription="NA";
    private ArrayList<Condition> mConditionList;

    public Situation(String name){
    	super();
    	mName = name;
    	mConditionList = new ArrayList<Condition>();
        _class = TriggerManager.PROBE_OBJECT_CLASS_EVENT;

    }
    
    public Situation(int id, String name, int study_id){
    	super();
    	_id = id;
    	mName = name;
    	_studyId = study_id;
    	mConditionList = new ArrayList<Condition>();
    	 _class = TriggerManager.PROBE_OBJECT_CLASS_EVENT;
    }
    
    
    public Situation(int id, String name, String description){
    	super();
    	_id = id;
    	mName = name;
    	mDescription = description;
    	mConditionList = new ArrayList<Condition>();
    	 _class = TriggerManager.PROBE_OBJECT_CLASS_EVENT;
    }    

    public Situation(String name, Task task){
    	super();
    	mName = name;
    	mConditionList = new ArrayList<Condition>();
    	 _class = TriggerManager.PROBE_OBJECT_CLASS_EVENT;
    }

	/**
	 * this function checkes whether a circumstance to be detected involves a specific state.
	 * If any of the conditions uses the value of the state, the function returns true.
	 * @param state
	 * @return
	 */
	public boolean isUsingState(State state){

		Log.d(LOG_TAG, "[isUsingState] check whether the circumstance monitors the state: ");
		for (int i=0; i<mConditionList.size(); i++){

			Condition condition = mConditionList.get(i);
			Log.d(LOG_TAG, "[isUsingState] condition: " + condition.getStateName() + " state: " + state.getName());
			//find a condition that monitors the state
			if (condition.getStateName().equals(state.getName())){
				return true;
			}
		}

		return false;
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
