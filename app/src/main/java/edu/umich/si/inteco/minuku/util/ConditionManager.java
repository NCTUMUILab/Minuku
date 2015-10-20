package edu.umich.si.inteco.minuku.util;

import android.provider.CalendarContract.Events;
import android.util.Log;

import java.util.ArrayList;

public class ConditionManager {

	public ConditionManager(){
		
	}
	
	private static final String LOG_TAG = "ConditionManager";
	/**constant condition type**/	
	
	//properties 
	public static final String CONDITION_PROPERTIES_STATE = "State";
	public static final String CONDITION_PROPERTIES_SOURCE = "Source";
	public static final String CONDITION_PROPERTIES_RELATIONSHIP = "Relationship";
	public static final String CONDITION_PROPERTIES_TARGETVALUE ="TargetValue";
	public static final String CONDITION_PROPERTIES_CRITERION ="Criterion";
	public static final String CONDITION_PROPERTIES_LATITUDE = "Lat";
	public static final String CONDITION_PROPERTIES_LONGITUDE = "Lng";
	
	
	//activity
	public static final String CONDITION_TYPE_ACTIVITY_TYPE = "activity_type";
	public static final String CONDITION_TYPE_ACTIVITY_CONFIDENCE = "activity_confidence";
    //we have a seperate way to detect this..
    public static final String CONDITION_TYPE_PROBE_TRANSPORTATION = "transportation_detection";

	//location
	public static final String CONDITION_TYPE_DISTANCE_TO = "distance_to";

	
	/**constant for relations**/	
	public static final String CONDITION_RELATIONSHIP_STRING_EQUAL_TO = "string_equal_to";
	public static final String CONDITION_RELATIONSHIP_STRING_NOT_EQUAL_TO = "string_not_equal_to";
    public static final String CONDITION_RELATIONSHIP_IS = "is";
	public static final String CONDITION_RELATIONSHIP_EQUAL_TO = "=";
	public static final String CONDITION_RELATIONSHIP_NOT_EQUAL_TO = "!=";
	public static final String CONDITION_RELATIONSHIP_LARGER_THAN = ">";
	public static final String CONDITION_RELATIONSHIP_LARGER_THAN_AND_EQUAL_TO = ">=";	
	public static final String CONDITION_RELATIONSHIP_SMALLER_THAN = "<";
	public static final String CONDITION_RELATIONSHIP_SMALLER_THAN_AND_EQUAL_TO = "<=";
	public static final String CONDITION_RELATIONSHIP_BETWEEN = "between";

	
	/***Time Constraint Type**/
	public static final String CONDITION_TIME_CONSTRAINT_RECENCY = "recency";
	public static final String CONDITION_TIME_CONSTRAINT_DURATION = "duration";
	public static final String CONDITION_TIME_CONSTRAINT_TIMEOFDAY = "time_of_day";
	public static final String CONDITION_TIME_CONSTRAINT_EXACTTIME = "exact_time";
	public static final String CONDITION_TIME_CONSTRAINT_DAYOFWEEK = "day_of_week";
	public static final String CONDITION_TIME_CONSTRAINT_DAYOFMONTH = "day_of_month";


	
	/**
	 * The function read a string and generate a list of events containing conditions. 
	 * This function should be called by EventManager, and the output will be added to EventManager's eventList
	 * 
	 * @param str
	 * @return
	 */
	public ArrayList<Events> generateConditionsFromString (String str){
		
		ArrayList<Events> events = new ArrayList<Events>();
		
		return events;
	}
	
	
	/***examine conditions**/
	public static boolean isSatisfyingCriteria(String value, String relationship, String targetValue ){
		
		boolean pass=false;
		
		Log.d(LOG_TAG, "[isSatisfyingCriteria] comparing value " + value +" and targetvalue " + targetValue + " rel: " + relationship  + " pass: " + pass) ;
		
		
		if (relationship.equals(CONDITION_RELATIONSHIP_STRING_EQUAL_TO)){
			
			if (value.equals(targetValue))
				pass = true;
			
		}else if (relationship.equals(CONDITION_RELATIONSHIP_STRING_NOT_EQUAL_TO)){
			
			if (!value.equals(targetValue))
				pass = true;
			
		}
		return pass;
		
	}
	
	
	/***examine conditions**/
	public static boolean isSatisfyingCriteria(float value, String relationship, float targetValue ){
		
		
		boolean pass=false;
		
		if (relationship.equals(CONDITION_RELATIONSHIP_EQUAL_TO)){
			
			if (value==targetValue)
				pass = true;
			
		}else if (relationship.equals(CONDITION_RELATIONSHIP_NOT_EQUAL_TO)){
			
			if (value!=targetValue)
				pass = true;
			
		}else if (relationship.equals(CONDITION_RELATIONSHIP_LARGER_THAN)){
			
			if (value>targetValue)
				pass = true;
			
		}else if (relationship.equals(CONDITION_RELATIONSHIP_LARGER_THAN_AND_EQUAL_TO)){
			
			if (value>=targetValue)
				pass = true;
			
		}else if (relationship.equals(CONDITION_RELATIONSHIP_SMALLER_THAN)){
			
			if (value<targetValue)
				pass = true;
			
		}else if (relationship.equals(CONDITION_RELATIONSHIP_SMALLER_THAN_AND_EQUAL_TO)){
			
			if (value<=targetValue)
				pass = true;
			
		}
		
		Log.d(LOG_TAG, "isSatisfyingCriteria] comparing value " + value +" and targetvalue " + targetValue + " rel: " + relationship  + " pass: " + pass) ;
		
		
		return pass;
	}
	
	
}
