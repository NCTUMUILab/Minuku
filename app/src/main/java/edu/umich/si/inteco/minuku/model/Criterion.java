package edu.umich.si.inteco.minuku.model;

import java.util.Objects;

import edu.umich.si.inteco.minuku.context.ContextStateManagers.ContextStateManager;
import edu.umich.si.inteco.minuku.util.ConditionManager;

public class Criterion {

	private int mMeasure;
	private int mRelationship;
	private Object mTargetValue;

	public Criterion(){
	}
	
	public Criterion(int measure, int relationship, Object targetValue){
		mMeasure = measure;
		mRelationship =relationship;
		mTargetValue = targetValue;
	}
	
	public void setRelationship(int relationship){
		mRelationship = relationship;		
	}
	
	public int getRelationship(){
		return mRelationship;
	}

	public void setValue(Object object){
		mTargetValue = object;
	}

	public Object getTargetValue() {
		return mTargetValue;
	}

	public int getMeasureType(){
		return mMeasure;
	} 
	
	public void setMeasureType(int m){
		mMeasure = m;
	}
	
}
