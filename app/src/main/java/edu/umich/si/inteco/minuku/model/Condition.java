package edu.umich.si.inteco.minuku.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Condition {

	/** Tag for logging. */
    private static final String LOG_TAG = "Condition";
    
    private int _id;  
    
    private ArrayList<TimeConstraint> mTimeConstraintList;
    
    private String _type; 
    
    private String _relationship;
    
    private LatLng _latlng;
    
    private String _stringTargetValue;
    
    private float _floatTargetValue;
    
    private float _upper;
    
    private float _lower;

    /***
     * List of Type
     * 
    	public static final String CONDITION_TIME_CONSTRAINT_RECENCY = "recency";
		public static final String CONDITION_TIME_CONSTRAINT_DURATION = "duration";
		public static final String CONDITION_TIME_CONSTRAINT_TIMEOFDAY = "time_of_day";
		public static final String CONDITION_TIME_CONSTRAINT_EXACTTIME = "exact_time";
		public static final String CONDITION_TIME_CONSTRAINT_DAYOFWEEK = "day_of_week";
		public static final String CONDITION_TIME_CONSTRAINT_DAYOFMONTH = "day_of_month";
	
     */
    
    private float[] mTargetValues;
    private String[] mTargetStringValues;
    
    public Condition (){
    	
    }
    
    //stringEqualTo
	public Condition(String type, String relationship, String targetValue){
		
		Log.d(LOG_TAG, "the target value is a String: " + targetValue);
		_type = type;
		_relationship = relationship;	
		_stringTargetValue = targetValue;
		
	}	
	
	//larger, equal, smaller
	public Condition(String type, String relationship, float targetValue){
		Log.d(LOG_TAG, "the target value is a float " + targetValue);
		_type = type;
		_relationship = relationship;		
		_floatTargetValue = targetValue;
	}	
	
	public Condition(String type, String relationship, float upper, float lower){
		
		_type = type;
		_relationship = relationship;
		_upper = upper;
		_lower = lower;		
	}

	
	//distance larger, smaller, or equal
	public Condition(String type, double latitude, double longitude, String relationship, float targetValue){
		
		_type = type;
		_relationship = relationship;
		_latlng = new LatLng(latitude, longitude);
		_floatTargetValue = targetValue;
		
		
	}	
	
	//distance between
	public Condition(String type, double latitude, double longitude, String relationship, float upper, float lower){
		
		_type = type;
		_relationship = relationship;
		_latlng = new LatLng(latitude, longitude);
		_upper = upper;
		_lower = lower;
	
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
	
	public String getType(){
		return _type;
	}
	
	public String getRelationship(){
		return _relationship;
	}
	
	public String getStringTargetValue(){
	
		return _stringTargetValue;
	}
	
	public float getFloatTargetValue(){
		
		return _floatTargetValue;
	}	
	
	public float getUpper(){
		return _upper;
	}
	
	public float getLower(){
		return _lower;
	}
	
	public LatLng getLatLng(){
		return _latlng;
	}

	public void setLatLng (double lat, double lng) {
		
		_latlng = new LatLng(lat, lng);
	}
	
}
