package edu.umich.si.inteco.minuku.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Condition {

	/** Tag for logging. */
    private static final String LOG_TAG = "Condition";

	private int _id;
	protected String mStateName;
	protected String mStateTargetValue;
    private ArrayList<TimeConstraint> mTimeConstraintList;

    private float[] mTargetValues;
    private String[] mTargetStringValues;
    
    public Condition (){
    	
    }

	/**
	 *
	 * @param stateName
	 * @param stateValue
	 */
	public Condition(String stateName, String stateValue) {
		mStateName = stateName;
		mStateTargetValue = stateValue;
	}

	/****
	 * functions of adding timeconstraint
	 * @param timeConstraint
	 */
	public void addTimeConstraint(TimeConstraint timeConstraint){
		
		if (mTimeConstraintList==null){
			mTimeConstraintList = new ArrayList<TimeConstraint>();
		}
		
		mTimeConstraintList.add(timeConstraint);
		
	}

	public String getStateName() {
		return mStateName;
	}

	public String getStateTargetValue() {
		return mStateTargetValue;
	}

	/**
	 * 
	 * @param constraintType
	 * @param relationship
	 */
	public void addTimeConstraint(String constraintType, String relationship ){
		
		if (mTimeConstraintList==null){
			mTimeConstraintList = new ArrayList<TimeConstraint>();
		}
		
		TimeConstraint tc = new TimeConstraint(constraintType,relationship );
		mTimeConstraintList.add(tc);
		
	}

	/**
	 *
	 * @param constraintType
	 * @param interval
	 * @param relationship
	 */
	public void addTimeConstraint(String constraintType, float interval, String relationship ){
		
		if (mTimeConstraintList==null){
			mTimeConstraintList = new ArrayList<TimeConstraint>();
		}
		
		TimeConstraint tc = new TimeConstraint(constraintType,interval, relationship );
		mTimeConstraintList.add(tc);
		
	}

	/**
	 * 
	 * @param constraintType
	 * @param timeOfDay
	 * @param relationship
	 */
	public void addTimeConstraint(String constraintType, int timeOfDay, String relationship ){
	
		if (mTimeConstraintList==null){
			mTimeConstraintList = new ArrayList<TimeConstraint>();
		}
	
		TimeConstraint tc = new TimeConstraint(constraintType,timeOfDay, relationship );
		mTimeConstraintList.add(tc);
	
	}

	/**
	 * 
	 * @param constraintType
	 * @param timestamp
	 */
	public void addTimeConstraint(String constraintType, long timestamp, String relationship ){
	
		if (mTimeConstraintList==null){
			mTimeConstraintList = new ArrayList<TimeConstraint>();
		}
		
		TimeConstraint tc = new TimeConstraint(constraintType,timestamp, relationship);
		mTimeConstraintList.add(tc);
		
	}
	
	/**
	 * 
	 * @param constraintType
	 * @param startTime
	 * @param endTime
	 * @param relationship
	 */
	public void addTimeConstraint(String constraintType, long startTime, long endTime, String relationship ){
		
		if (mTimeConstraintList==null){
			mTimeConstraintList = new ArrayList<TimeConstraint>();
		}
		
		TimeConstraint tc = new TimeConstraint(constraintType,startTime, endTime, relationship );
		mTimeConstraintList.add(tc);	
	}


	/**
	 * 
	 * @param timeConstraints
	 */
	public void addTimeConstraint(ArrayList<TimeConstraint> timeConstraints){
		
		if (mTimeConstraintList==null){
			mTimeConstraintList = timeConstraints;
		}
		
		mTimeConstraintList.addAll(timeConstraints);
		
	}
	
	public ArrayList<TimeConstraint> getTimeConstraints(){
		
		return mTimeConstraintList;
	}
	
}
