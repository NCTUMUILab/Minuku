package edu.umich.si.inteco.minuku.model;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.model.actions.Action;

public class Schedule {

	private static final String LOG_TAG = "Schedule";
	
	private int _id;
	private String _type;
	private String _sampleMethod=null;
	private int _actionId;
	private Action _action;
	
	
	/****Sample Related***/
	private int mSampleCount=-1;
	private int mSampleDelay=-1;
	private int mSampleDuration=-1;	
	private String mSampleEndAt=null;
	private int mMinInterval = -1;
	private int mInterval = -1;
	
	private String mTimeOfDay=null;
	
	
	private long mSampleEndAtTimeOfDay=-1;
	
	
	
	/** the two properties are calculated **/
	private ArrayList<Long> mSampleStartTimeList;
	private ArrayList<Long> mSampleEndTimeList;
	
	
	public Schedule (String type, String sample_method){

		_type = type;
		_sampleMethod = sample_method;
		
	}
	
	public Schedule (String sample_method){

		_sampleMethod = sample_method;
		
	}

	public Schedule (int action_id, String type, String sample_method){
		
		_actionId = action_id;
		_type = type;
		_sampleMethod = sample_method;
		
	}
	
	
	public Schedule (Action action, String type, String sample_method){
		
		_action = action;
		_type = type;
		_sampleMethod = sample_method;
		
	}
	
	public void setActionId(int id){
		_actionId = id;
	}

	public long getActionId(){
		return _actionId;
	}
	
	
	public void setId(int id){
		_id = id;
	}

	public long getId(){
		return _id;
	}
	
	/*
	public String getType(){
		return _type;
	} 
	
	public void setType(String t){
		_type = t;
	}
	*/
	
	public void setAction(Action action){
		
		_action = action;
	}
	
	public Action getAction(){
		
		return _action;
	}
	
	
	public String getSampleMethod(){
		return _sampleMethod;
	} 
	
	public void setSampleMethod(String sample_method){
		_sampleMethod = sample_method;
	}
	
	
	public void setSampleCount(int count){
		
		 mSampleCount = count;
	}
	
	public int getSampleCount(){
		
		return  mSampleCount;
	}
	
	
	public void setSampleDuration(int duration){
		
		 mSampleDuration = duration;
	}
	
	public int getSampleDuration(){
		
		return  mSampleDuration;
	}
	
	public String getSampleEndAtTimeOfDay(){
		return mSampleEndAt;
	} 
	
	public void setSampleEndAtTimeOfDay(String time_of_day){
		mSampleEndAt = time_of_day;
	}

	
	
	public String getFixedTimeOfDay(){
		return mTimeOfDay;
	} 
	
	public void setFixedTimeOfDay(String time_of_day){
		mTimeOfDay = time_of_day;
	}
	
	
	
	
	
	public void setSampleDelay(int delay){
		
		 mSampleDelay = delay;
	}
	
	public int getSampleDelay(){
		
		return  mSampleDelay;
	}
	
	public ArrayList<Long> getSampleStartTimeList(){
		
		if (mSampleStartTimeList==null){
			mSampleStartTimeList = new ArrayList<Long>();
		}
		
		return mSampleStartTimeList;
		
	}
	
	public ArrayList<Long> getSampleEndTimeList(){
		
		if (mSampleEndTimeList==null){
			mSampleEndTimeList = new ArrayList<Long>();
		}
		
		return mSampleEndTimeList;
		
	}
	
	
	public void addStartTime(long startTime){
		
		if (mSampleStartTimeList==null){
			mSampleStartTimeList = new ArrayList<Long>();
		}
		
		mSampleStartTimeList.add(startTime);
	}
	
	public void addEndTime(long endTime){
		
		if (mSampleEndTimeList==null){
			mSampleEndTimeList = new ArrayList<Long>();
		}
		
		mSampleStartTimeList.add(endTime);
	}
	
	public void setSampleStartTime(ArrayList<Long> startTimes){
		
		mSampleStartTimeList = startTimes;
	}
	
	public void setSampleEndTime(ArrayList<Long> endTimes){
		
		mSampleEndTimeList = endTimes;
	}
	
	public void setMinInverval (int interval){
		
		mMinInterval = interval;
	}

	public int getMinInterval (){
		return mMinInterval;
	}
	
	public void setInverval (int interval){
		
		mInterval = interval;
	}

	public int getInterval (){
		return mInterval;
	}
	
}
