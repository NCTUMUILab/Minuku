package edu.umich.si.inteco.minuku.model;

import edu.umich.si.inteco.minuku.util.ConditionManager;

public class TimeConstraint {

	
	private String _type;
	
	//require something to take place in the last certain amount of time 
	private float _interval = -1; 
	
	//require something to take place before/after/at certain time 
	private long _startTime  = -1;
	
	private long _endTime = -1;
	
	private long _timestamp = -1;
	
	private int _timeOfDay = -1;
	
	private String _relationship=null;
		

	public TimeConstraint(String constraintType, String relationship ){
		
		_type = constraintType;
		_relationship = relationship;		
	}
	
	public TimeConstraint(String constraintType, float interval, String relationship ){
		
		_type = constraintType;
		
		if (constraintType.equals(ConditionManager.CONDITION_TIME_CONSTRAINT_DURATION))
			_interval = interval;
		if (constraintType.equals(ConditionManager.CONDITION_TIME_CONSTRAINT_RECENCY))
			_interval = interval;
		
		_relationship = relationship;		
	}
	
	public TimeConstraint(String constraintType, int timeOfDay, String relationship ){
		
		_type = constraintType;
		_timeOfDay = timeOfDay;
		_relationship = relationship;
	}		
	
	public TimeConstraint(String constraintType, long timestamp, String relationship){
		
		_type = constraintType;
		_timestamp = timestamp;
		_relationship = relationship;
	}

	
	public TimeConstraint (String constraintType, long startTime, long endTime, String relationship){		
		
		_type = constraintType;
		_startTime = startTime;
		_endTime = endTime;	
		_relationship = relationship;
	}

	
	public void setRelationship(String relationship){
		_relationship = relationship;		
	}
	
	public String getRelationship(){
		return _relationship;
	}
	
	public void setTimeOfDay(int timeOfDay){
		_timeOfDay = timeOfDay;
	}
	
	public int getTimeDay(){
		return _timeOfDay; 
	}

	public void setStartAndEndTime(long startTime, long endTime){
		_startTime= startTime;
		_endTime = endTime;
	}	
	
	public void setStartTime(int startTime){
		_startTime = startTime;
	}
	
	public long getStartTime(){
		return _startTime; 
	}
	
	public void setEndTime(int endTime){
		_endTime = endTime;
	}
	
	public long getEndTime(){
		return _endTime; 
	}	

	public void setInterval(float interval){
		_interval = interval;
	}
	
	public float getInterval(){
		return _interval; 
	}
	

	public void setExactTime(long timestamp){
		_timestamp = timestamp; 
	}
	
	public long getExactTime(){
		return _timestamp; 
	}
	
	public String getType(){
		return _type;
	} 
	
	public void setType(String t){
		_type = t;
	}
	
}
