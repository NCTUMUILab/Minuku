package edu.umich.si.inteco.minuku.model.Criteria;

import java.util.ArrayList;
import java.util.Objects;

import edu.umich.si.inteco.minuku.context.ContextStateManagers.ContextStateManager;
import edu.umich.si.inteco.minuku.util.ConditionManager;

public class Criterion {

	protected String mMeasure;
	protected String mRelationship;
	protected Object mTargetValue;
	protected ArrayList<String> mParameters;

	public Criterion(){
	}
	
	public Criterion(String measure, String relationship, Object targetValue){
		mMeasure = measure;
		mRelationship =relationship;
		mTargetValue = targetValue;
	}
	
	public void setRelationship(String relationship){
		mRelationship = relationship;		
	}
	
	public String getRelationship(){
		return mRelationship;
	}

	public void setTargetValue(Object object){
		mTargetValue = object;
	}

	public Object getTargetValue() {
		return mTargetValue;
	}

	public String getMeasure(){
		return mMeasure;
	} 
	
	public void setMeasure(String m){
		mMeasure = m;
	}

	public ArrayList<String> getParameters() {
		return mParameters;
	}

	public void setParameters(ArrayList<String> parameters) {
		this.mParameters = mParameters;
	}

	public void addParameter(String param){
		if (mParameters==null){
			mParameters = new ArrayList<String>();
		}

		mParameters.add(param);
	}


	@Override
	public String toString() {

		return "Criterion{" +
				mMeasure + "-" +
				mRelationship + "-" +
				mTargetValue+
				'}';
	}

}
