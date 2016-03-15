package edu.umich.si.inteco.minuku.model;

import org.json.JSONObject;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.model.Criteria.StateValueCriterion;
import edu.umich.si.inteco.minuku.model.Criteria.TimeCriterion;

public class Condition {

	/** Tag for logging. */
    private static final String LOG_TAG = "Condition";

	private int _id;
	//Name is provided by ContextStateManager

	// a condition means a State (specified through a StateName) is with the StateValue.
	protected String mStateName;
	protected String mSourceString;
	protected String mStateValue;
	protected JSONObject mCriterionJSON;
    protected ArrayList<StateValueCriterion> mValueCriteria;
	protected ArrayList<TimeCriterion> mTimeCriteria;
	protected int mSourceType;
	protected StateMappingRule mStateMappingRule;

    
    public Condition (){
    	
    }

	public Condition(String source, String value) {
		mSourceString = source;
		mStateValue  =value;
	}

	public Condition(String source, String value, ArrayList<StateValueCriterion> valueCriteria) {
		mSourceString = source;
		mStateValue  =value;
		mValueCriteria = valueCriteria;
	}

	public void setStateMappingRule (StateMappingRule rule){
		mStateMappingRule = rule;
	}

	public StateMappingRule getStateMappingRule () {
		return mStateMappingRule;
	}

	public int getSourceType() {
		return mSourceType;
	}

	public void setSourceType(int sourceType) {
		this.mSourceType =sourceType;
	}

	public void setSource(String source) {
		this.mSourceString = source;
	}

	public String getStateName() {
		return mStateName;
	}

	public void setStateName (String name) {
		mStateName = name;
	}

	public void setStateTargetValue(String value) {
		mStateValue = value;
	}

	public JSONObject getCriterionJSON() {
		return mCriterionJSON;
	}

	public String getSource() {
		return mSourceString;
	}

	public String getStateTargetValue() {
		return mStateValue;
	}
	
	public ArrayList<TimeCriterion> getTimeCriteria(){
		return mTimeCriteria;
	}

	public ArrayList<StateValueCriterion> getStateValueCriteria(){
		return mValueCriteria;
	}

	public void setValueCriteria(ArrayList<StateValueCriterion> valueCriteria) {
		mValueCriteria = valueCriteria;
	}

	public void setTimeCriteria(ArrayList<TimeCriterion> timeCriteria) {
		mTimeCriteria = timeCriteria;
	}
}
