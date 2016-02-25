package edu.umich.si.inteco.minuku.model.Criteria;

/**
 * Created by Armuro on 10/28/15.
 */
public class TimeCriterion extends Criterion {


    //require something to take place in the last certain amount of time
    private float _interval = -1;

    //require something to take place before/after/at certain time
    private long _startTime  = -1;

    private long _endTime = -1;

    private long _timestamp = -1;

    private int _timeOfDay = -1;

/*
    public TimeCriterion(String constraintType, float interval, String relationship){
        super();

        if (constraintType.equals(ConditionManager.CONDITION_TIME_CONSTRAINT_DURATION))
            _interval = interval;
        if (constraintType.equals(ConditionManager.CONDITION_TIME_CONSTRAINT_RECENCY))
            _interval = interval;

        _relationship = relationship;
    }
*/

    public TimeCriterion(String measure, String relationship, float targetValue){
        super(measure, relationship, targetValue);
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

}
