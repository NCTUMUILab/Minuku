package edu.umich.si.inteco.minuku.model;

import org.json.JSONObject;

import java.util.ArrayList;

public class Condition {

	/** Tag for logging. */
    private static final String LOG_TAG = "Condition";

	private int _id;
	//Name is provided by ContextStateManager
	protected String mStateName;
	//determined in the configuration file
	protected String mSource;
	//determined in the configuration file
	protected String mStateValue;
	protected JSONObject mCriterion;
    protected ArrayList<StateValueCriterion> mValueCriteria;
	protected ArrayList<TimeCriterion> mTimeCriteria;

    
    public Condition (){
    	
    }

	public Condition(String source, String value, ArrayList<StateValueCriterion> valueCriteria) {
		mSource = source;
		mStateValue  =value;
		mValueCriteria = valueCriteria;
	}

	public String getStateName() {
		return mStateName;
	}

	public JSONObject getCriterion() {
		return mCriterion;
	}

	public String getSource() {
		return mSource;
	}

	public String getStateValue() {
		return mStateValue;
	}
	
	public ArrayList<TimeCriterion> getTimeCriteria(){
		return mTimeCriteria;
	}

	public ArrayList<StateValueCriterion> getStateValueCriteria(){
		return mValueCriteria;
	}

	public void setTimeCriteria(ArrayList<TimeCriterion> timeCriteria) {
		mTimeCriteria = timeCriteria;
	}
}
