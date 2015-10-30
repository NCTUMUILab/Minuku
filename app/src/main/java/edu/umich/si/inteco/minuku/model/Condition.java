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
	protected String mSourceString;
	//determined in the configuration file
	protected String mStateValue;
	protected JSONObject mCriterion;
    protected ArrayList<StateValueCriterion> mValueCriteria;
	protected ArrayList<TimeCriterion> mTimeCriteria;
	protected int mSourceType;

    
    public Condition (){
    	
    }

	public Condition(String source, String value, ArrayList<StateValueCriterion> valueCriteria) {
		mSourceString = source;
		mStateValue  =value;
		mValueCriteria = valueCriteria;
	}

	public int getSourceType() {
		return mSourceType;
	}

	public void setSourceType(int sourceType) {
		this.mSourceType =sourceType;
	}

	public String getStateName() {
		return mStateName;
	}

	public void setStateName (String name) {
		mStateName = name;
	}
	public JSONObject getCriterion() {
		return mCriterion;
	}

	public String getSource() {
		return mSourceString;
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
