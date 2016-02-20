package edu.umich.si.inteco.minuku.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.model.ProbeObjectControl.ActionControl;
import edu.umich.si.inteco.minuku.model.Schedule;

public class ScheduleAndSampleManager {

	private static final String LOG_TAG = "ScheduleManager";
	
	private static AlarmManager mAlarmManager;

    public static int REQUEST_CODE_REFRESH_UPDATE = 8;
    public static int REQUEST_CODE_REFRESH_STOP = 4;
    public static int REQUEST_CODE_REFRESH_START = 2;
    public static int REQUEST_CODE_DAILY_ACTION = 2;

	private static Context mContext;
	
	public static int bedStartTime = 0;
	public static int bedEndTime = 5;
    public static int bedMiddleTime = 2;
    public static long time_base = 0;

    public static int pauseProbeService = bedMiddleTime;    //3 AM
    public static long resumeProbeService = bedEndTime;

	
	//this stores all of the request codes of been scheduled alarm
	private static ArrayList<Integer> mAlarmRequestCodes;
	
	public static final String ALARM_REQUEST_CODE = "alarm_request_code";

    public static final String ALARM_TYPE_ACTION = "action_alarm";
    public static final String ALARM_TYPE_REFRESH = "refresh_alarm";
    public static final String ALARM_TYPE_BATTERY = "battery_alarm";
	
	public static final String SCHEDULE_TYPE_EVENT_CONTINGENT = "event_contingent";
	public static final String SCHEDULE_TYPE_INTERVAL_CONTINGENT = "interval_contingent";
	
	public static final String SCHEDULE_SAMPLE_METHOD_SIMPLE_ONE_TIME = "simple_one_time";
	public static final String SCHEDULE_SAMPLE_METHOD_RANDOM = "random";
	public static final String SCHEDULE_SAMPLE_METHOD_RANDOM_WITH_MINIMUM_INTERVAL = "random_with_minimum_interval";
	public static final String SCHEDULE_SAMPLE_METHOD_FIXED_TIME_OF_DAY = "fixed_time_of_day";
	public static final String SCHEDULE_SAMPLE_METHOD_FIXED_INTERVAL = "fixed_interval";

    public static final String UPDATE_SCHEDULE_ALARM = "edu.umich.si.inteco.minuku.updateScheduleAlarm";
    public static final String STOP_SERVICE_ALARM = "edu.umich.si.inteco.minuku.stopServiceAlarm";
    public static final String START_SERVICE_ALARM = "edu.umich.si.inteco.minuku.startServiceAlarm";
	
	
	public static final String SCHEDULE_PROPERTIES_TYPE = "Type";
	public static final String SCHEDULE_PROPERTIES_SAMPLE_METHOD = "Sample_method";
	public static final String SCHEDULE_PROPERTIES_SAMPLE_DELAY = "Sample_delay";
	public static final String SCHEDULE_PROPERTIES_SAMPLE_INTERVAL = "Sample_interval";
	public static final String SCHEDULE_PROPERTIES_SAMPLE_DURATION = "Sample_duration";
	public static final String SCHEDULE_PROPERTIES_SAMPLE_COUNT = "Sample_count";
	public static final String SCHEDULE_PROPERTIES_SAMPLE_END_AT = "Sample_endAt";
	public static final String SCHEDULE_PROPERTIES_SAMPLE_START_AT = "Sample_startAt";
	public static final String SCHEDULE_PROPERTIES_FIXED_TIME_OF_DAY = "Time_of_day";
	public static final String SCHEDULE_PROPERTIES_SAMPLE_MINIMUM_INTERVAL = "Sample_min_interval";
	
	public ScheduleAndSampleManager(Context context){
		
		mContext = context;
		mAlarmManager = (AlarmManager)mContext.getSystemService( mContext.ALARM_SERVICE );
		mAlarmRequestCodes = new ArrayList<Integer>();
		
		//register action alarm receiver to receive action alarms.
		registerAlarmReceivers();

        //time base is for generated request code
		time_base = getCurrentTimeInMillis();
		
	}
	
	
	//register all alarm receivers. 
	public void registerAlarmReceivers(){
		
		//register alarm the receiver for action alarm
		registerActionAlarmReceiver();

        generateRefreshServiceScheduleAlarms();
	}

    /**
     * this function is for scheduling alarms for updating the schedule of action controls everyday
     * The schedule of the updateScheduleAlarm is at the end of bed time
     */
    public void registerUpdateScheduleAlarm() {

        Log.d(LOG_TAG, "[test reschedule][egisterUpdateScheduleAlarm] the alarm receiver  with request code ");

    }

    public static void unregisterAlarmReceivers() {

        //unregister action alarm receiver
        unregisterActionAlarmReceiver();

        //unregister update schedule alarm receiver
        //unregisterUpdateScheduleAlarmReceiver();
    }

/*
    public static void registerUpdateScheduleAlarmReceiver(){

        Log.d(LOG_TAG, "[test reschedule][registerUpdateScheduleAlarmReceiver] " );


        //register action alarm
        IntentFilter alarm_filter = new IntentFilter();
        alarm_filter.addAction(Constants.UPDATE_SCHEDULE_ALARM);
        alarm_filter.addAction(Constants.STOP_SERVICE_ALARM);
        alarm_filter.addAction(Constants.START_SERVICE_ALARM);

        mContext.registerReceiver(UpdateScheduleAlarmReceiver, alarm_filter);

    }
*/
	
	public static void registerActionAlarmReceiver(){
		//register action alarm
		IntentFilter alarm_filter = new IntentFilter(Constants.ACTION_ALARM);
		mContext.registerReceiver(ActionAlarmReceiver, alarm_filter);
		
	}

	public static void unregisterActionAlarmReceiver(){
		mContext.unregisterReceiver(ActionAlarmReceiver);
	}

    /*
    public static void unregisterUpdateScheduleAlarmReceiver(){
         mContext.unregisterReceiver(UpdateScheduleAlarmReceiver);
    }
*/

    public static long getSamplingAtTime(Schedule schedule) {

        //get information of how
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        long time = -1;

        //first get hour and minute of the specified time
        int t_hour  = getHourOfTimeOfDay(schedule.getFixedTimeOfDay());
        int t_minute = getMinuteOfTimeOfDay(schedule.getFixedTimeOfDay());

        //create the specified time in milliseconds today
        Calendar designatedTime = Calendar.getInstance();
        designatedTime.set(year, month-1, day, t_hour, t_minute);

        Log.d(LOG_TAG, "[test fixed time][getSamplingTime] the designatedTime is : " + getTimeString(designatedTime.getTimeInMillis()) );

        ////compare if the designated time has passed (i.e. the curren time  > designated time ), return -1.
        if(cal.after(designatedTime)){
            // day + 1
            designatedTime.roll(Calendar.DAY_OF_MONTH, 1);
				Log.d(LOG_TAG, "[test fixed time][getSamplingTime] the timeOfDay " + schedule.getFixedTimeOfDay() + " has passed, now is already " + hour+ " so we don't scheudle it" );
        }
        //the time has not been passed
        else {
            Log.d(LOG_TAG, "[test fixed time][getSamplingTime] the timeOfDay " + schedule.getFixedTimeOfDay() + " has not passed, we will scheudle the action at " + getTimeString(designatedTime.getTimeInMillis() ) );
            time = designatedTime.getTimeInMillis();
        }


        return time;
    }
	
	private static long getSamplingStartTime(Schedule schedule){

		TimeZone tz = TimeZone.getDefault();
		Calendar cal = Calendar.getInstance(tz);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);

		long startTime = 0;

		// if there's a start time constraint, we should generate sample time after that .
		if (schedule.getSampleStartAtTimeOfDay() != null) {

			//geneerate the time of day of the same day.
			int t_hour = getHourOfTimeOfDay(schedule.getSampleStartAtTimeOfDay());
			int t_minute = getMinuteOfTimeOfDay(schedule.getSampleStartAtTimeOfDay());

			Log.d(LOG_TAG, "testRandom [getSamplingStartTime] the timeOfDay is : " + t_hour + " : " + t_minute);

			Calendar designatedStartTime = Calendar.getInstance();
			designatedStartTime.set(year, month - 1, day, t_hour, t_minute);

			Log.d(LOG_TAG, "testRandom [getSamplingStartTime] the designatedStartTime is : " + getTimeString(designatedStartTime.getTimeInMillis()));

			//if the timeOfDay has passed (i.e. the curren time  > designated time ), we used "now", which is "cal" as a start time.
			if (cal.after(designatedStartTime)) {

				// day + 1
				startTime = cal.getTimeInMillis();

				Log.d(LOG_TAG, "testRandom [getSamplingStartTime] the timeOfDay " + schedule.getSampleStartAtTimeOfDay() + " has passed, now is already " + hour
						+ " so the rolling it, the new designated start time is now, which is: " + getTimeString(startTime));

			}
			//otherwise, we should sample starting from the star time.
			else {
				startTime = designatedStartTime.getTimeInMillis();
			}


			Log.d(LOG_TAG, "testRandom [getSamplingStartTime] the final start time should be " + getTimeString(startTime));

		}
		else {
			startTime = cal.getTimeInMillis();
		}

		//if there's no Start Time properties, we return "now"
		return (startTime + schedule.getSampleDelay()* Constants.MILLISECONDS_PER_SECOND);
		
	}

    //we need to save request codes in the preference instead of in memory
    public static void savePendingIntentRequestCodeToPreference(int requestCode) {


        //we first get the current requestcode json from the sharedpference then we add a new request code and then overwrite it

        Log.d(LOG_TAG, "[savePendingIntentRequestCodeToPreference] checking existing request code "
                + PreferenceHelper.getPreferenceString(PreferenceHelper.SCHEDULE_REQUEST_CODE, null));

        if (PreferenceHelper.getPreferenceString(PreferenceHelper.SCHEDULE_REQUEST_CODE, null)!=null) {
            try {
                Log.d(LOG_TAG, "[savePendingIntentRequestCodeToPreference] the existing saved request code json is "
                        + PreferenceHelper.getPreferenceString(PreferenceHelper.SCHEDULE_REQUEST_CODE, null));

                JSONArray existingRequestCodes =  new JSONArray(PreferenceHelper.getPreferenceString(PreferenceHelper.SCHEDULE_REQUEST_CODE, null));
                existingRequestCodes.put(requestCode);
                PreferenceHelper.setPreferenceValue(PreferenceHelper.SCHEDULE_REQUEST_CODE,  existingRequestCodes.toString());

                Log.d(LOG_TAG, "[savePendingIntentRequestCodeToPreference] the new request code json is " + existingRequestCodes );

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        //create a new preference
        {
            Log.d(LOG_TAG, "[savePendingIntentRequestCodeToPreference] there are no request code, create one " );

            JSONArray requestCodes = new JSONArray();
            requestCodes.put(requestCode);
            PreferenceHelper.setPreferenceValue(PreferenceHelper.SCHEDULE_REQUEST_CODE, requestCodes.toString());

            Log.d(LOG_TAG, "[savePendingIntentRequestCodeToPreference] the new request code json is " + requestCodes );

        }




    }

	private static long getSamplingEndTime(long startTime, Schedule schedule){
		
		TimeZone tz = TimeZone.getDefault();		
		Calendar cal = Calendar.getInstance(tz);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH)+1; 
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);

		//Log.d(LOG_TAG, "[getSamplingEndTime] now is  : " + month + "/" + day + " " + hour );
		long endTime = 0;
		
		//1. starTime  + duration = endTime
		if ( schedule.getSampleDuration()!=-1){	
			
			endTime = startTime + schedule.getSampleDuration()* Constants.MILLISECONDS_PER_SECOND;

			//TODO: avoid bed time?
			/*
			if (endTime > getNextBedStartTimeInMillis())
				endTime = getNextBedStartTimeInMillis();
				*/
			
			return endTime;

		}
		
		//2 TimeofDay 
		else if (schedule.getSampleEndAtTimeOfDay()!=null){
			
			//geneerate the time of day of the same day. 
			int t_hour = getHourOfTimeOfDay(schedule.getSampleEndAtTimeOfDay());
			int t_minute =  getMinuteOfTimeOfDay(schedule.getSampleEndAtTimeOfDay());	
			
			//Log.d(LOG_TAG, "[getSamplingEndTime] the timeOfDay is : " + t_hour  + " : " + t_minute );

			
			Calendar designatedEndTime = Calendar.getInstance();
			designatedEndTime.set(year, month-1, day, t_hour, t_minute);

			//Log.d(LOG_TAG, "[getSamplingEndTime] the designatedEndTime is : " + getTimeString(designatedEndTime.getTimeInMillis()) );
			//if the timeOfDay has passed (i.e. the curren time  > designated time ), return -1.
			if(cal.after(designatedEndTime)){	
				
				
				// day + 1
				designatedEndTime.roll(Calendar.DAY_OF_MONTH, 1); 
				/*
				Log.d(LOG_TAG, "[getSamplingEndTime] the timeOfDay " + schedule.getSampleEndAtTimeOfDay() + " has passed, now is already " + hour
						 + " so the rolling it, the new designated time is " + getTimeString(designatedEndTime.getTimeInMillis()) );
					*/	 
			}
			/*
			Log.d(LOG_TAG, "[getSamplingEndTime] comparing bed time " +  getTimeString( getNextBedStartTimeInMillis() ) 
					+ "and end time " + getTimeString( designatedEndTime.getTimeInMillis() ));
					*/
			//if the end time will pass the coming bed time, then the latest end time should the bed Time.	
			if (designatedEndTime.getTimeInMillis() > getNextBedStartTimeInMillis()){
	
				endTime = getNextBedStartTimeInMillis();
				//Log.d(LOG_TAG, "[getSamplingEndTime] the end time is later than the bed time" +  getTimeString(designatedEndTime.getTimeInMillis()) );
						 
			}
			else {
				endTime = designatedEndTime.getTimeInMillis();
				//Log.d(LOG_TAG, "[getSamplingEndTime] the end time is earlier than the bed time" +  getTimeString(designatedEndTime.getTimeInMillis()) );

			}
			
			//Log.d(LOG_TAG, "[getSamplingEndTime] the final end time should be " + getTimeString( endTime));
			
			return endTime;

		}
		else 
			return -1;
		
	}
	
	
	private static int getHourOfTimeOfDay (String TimeOfDay){
		
		return Integer.parseInt(TimeOfDay.split(":")[0] ) ;
	}
	
	private static int getMinuteOfTimeOfDay (String TimeOfDay){

		return Integer.parseInt(TimeOfDay.split(":")[1] );
	}
	
	
	
	
	

	

	
	
	/**
	 * 
	 * @param ac
	 */
	public static void executeSchedule (ActionControl ac){

		Log.d(LOG_TAG, "[executeSchedule] going to schedule " + ac.getId() +"'s schedule" );
		
		Schedule schedule = ac.getSchedule();
		ArrayList<Long> SampleTimes = generateSampleTimes(schedule);
		//according to the sample times, generate alarms
		generateAlarmsForSamples(SampleTimes, ac);
		
	}
	
	/**
	 * 
	 * @param schedule
	 * @return
	 */
	private static ArrayList<Long> generateSampleTimes(Schedule schedule) {
		
		ArrayList <Long> SampleTimes=null ;
		TimeZone tz = TimeZone.getDefault();		
		Calendar cal = Calendar.getInstance(tz);
		long now = cal.getTimeInMillis();

		/***
		 * We need to get the sample method, sample count, sample star time, and sample end time to know how to schedule
		 * 
		 */
		
		//get properties of the schedule;
		String sample_method = schedule.getSampleMethod();
		//if the schedule has sample count, get the sample count
		int sample_count = schedule.getSampleCount();	


        /** 1. if the method is at fixed time of a day **/
		if (sample_method.equals(SCHEDULE_SAMPLE_METHOD_FIXED_TIME_OF_DAY)) {

            String timeString = schedule.getFixedTimeOfDay();
            //need to schedule at the specified time

            long sampleTime = getSamplingAtTime(schedule);
            Log.d(LOG_TAG, "[test fixed time] [generateSampleTimes] fixed time of day, the schedule will be at " + getTimeString(sampleTime));
            if (sampleTime!=-1){
                SampleTimes = new ArrayList<Long>();
                SampleTimes.add(sampleTime);
            }
            return SampleTimes;

        }


        /** 2. if the sample method is simple one time, there's no need to generate a number of sample times**/
        else if (sample_method.equals(SCHEDULE_SAMPLE_METHOD_SIMPLE_ONE_TIME)){

            //because it's one time, we just add a delay to "now"
			long samplingStartTimeInMilli =  now + schedule.getSampleDelay()* Constants.MILLISECONDS_PER_SECOND;
			Log.d(LOG_TAG, "testRandom [generateSampleTimes] this is simple one time action,  the one time action is at " + getTimeString(samplingStartTimeInMilli) );
			
			// if the startTime is later than the next bedTime, should abort this schedule..
			if (samplingStartTimeInMilli > getNextBedStartTimeInMillis() ){			
				Log.d(LOG_TAG, "testRandom [generateSampleTimes]  the start time " + getTimeString(samplingStartTimeInMilli) + " is too late, the bed time is " +  getTimeString( getNextBedStartTimeInMillis()) );
				return SampleTimes;
			}
						
			//because there's only one time action, just add the startTime into the sampletimes. 
			SampleTimes = new ArrayList<Long>();
			SampleTimes.add(samplingStartTimeInMilli);
            Log.d(LOG_TAG, "testRandom [generateSampleTimes] SampleTimes is " + SampleTimes);

			return SampleTimes;
			
		}
		
		//if it needs multiple sample times (i.e. not simple one time), then we call sample starTime, endTime, and other parameters, and calculate the sample
		//times
		else {
			/** identify the sampling period **/
			long samplingStartTimeInMilli = getSamplingStartTime(schedule);
			long samplingEndTimeInMilli = getSamplingEndTime(now, schedule);	

			Log.d(LOG_TAG, "[testRandom] sampling from "  + getTimeString(samplingStartTimeInMilli) + "  - " + getTimeString(samplingEndTimeInMilli));

			// if the startTime is later than the next bedTime, should abort this schedule..
			if (samplingStartTimeInMilli > getNextBedStartTimeInMillis() ){			
				Log.d(LOG_TAG, "[testRandom] the start time " + getTimeString(samplingStartTimeInMilli) + " is too late, the bed time is " +  getTimeString( getNextBedStartTimeInMillis()) );
				return SampleTimes;
			}

			/** generate the sample times **/
			SampleTimes = calculateSampleTimes(sample_count, sample_method, samplingStartTimeInMilli, samplingEndTimeInMilli, schedule);
			Log.d(LOG_TAG, " [testRandom] sample times are: " + SampleTimes.toString());


			return SampleTimes;
			
		}

		
		
	}
	
	
	
	/**
	 * Generate a list of sample times according to the schedule's sampling method, sampling period, and sampling numbers.
	 * @return
	 */
	private static ArrayList<Long> calculateSampleTimes (int sample_number, String method, long startTime, long endTime, Schedule schedule){
		
		ArrayList<Long> times = new ArrayList<Long>();
		long now = getCurrentTimeInMillis();
		Random random = new Random(now);

		//if startTime has passed, now becomes the startTime. This is for cases where the delay might be zero. So the action should immediately
		//happen right after the event. But the processing time may delay the startTime.

		if (now > startTime) now=startTime;

		//1. pure random
		if (method.equals(ScheduleAndSampleManager.SCHEDULE_SAMPLE_METHOD_RANDOM)){
	
			//start to random
			int sample_period = (int) (endTime - startTime);			
			
			Log.d(LOG_TAG, "[testRandom] random between 0  and " + sample_period);

			//generate a number of random number
			for (int i=0; i<sample_number; i++){

				long time = random.nextInt(sample_period) + startTime;
				times.add(time);
			
				Log.d(LOG_TAG, "[testRandom] generate a new random number is " + time + " : " + getTimeString(time));
			}

		}
		
		//restrictd random: a subsequent time must be at a time when its interval from the previous time is larger than the minimum. e.g. 
		//if the first generated time is 13:47, and the mininum is 1 hour, then the next time must be later than 14:47. 
		//the purpose is to avoid too intensive actions (e.g. prompting questionnaires to a participant)
		
		
		
		else if  (method.equals(SCHEDULE_SAMPLE_METHOD_RANDOM_WITH_MINIMUM_INTERVAL)){
			
			int sample_period;
			long sub_startTime = startTime;
			long sub_endTime  = endTime;			
			long min_interval = schedule.getMinInterval() * Constants.MILLISECONDS_PER_SECOND;
					
			//1. first get the entire sampling period			
			
			//repeatedly find the subsampling period
			for (int i=0; i<sample_number; i++){				

				//2. divide the period by the number of sample
				sample_period = (int) (sub_endTime - sub_startTime);
				
				int sub_sample_period = sample_period/(sample_number-i);
				
				//3. random within the sub sample period
				long time =  random.nextInt(sub_sample_period) + sub_startTime;

				Log.d(LOG_TAG, "testRandom semi sampling: the " + i + " sampling period is from " + getTimeString(sub_startTime) + " to " + getTimeString(sub_endTime) +
				" divied by " + (sample_number-i) + " each period is " + (sub_sample_period/60/1000) + " minutes long, " + " the sampled time within the period is " +
						getTimeString(time) );

				//4. save the sampled time
				times.add(time);
				
				//5. the next startime is the previous sample time + min interval. We do this to ensure that the next sampled time is 
				//not too close to the previous sampled time. 
				sub_startTime = time +  min_interval;
				
//				Log.d(LOG_TAG, "testRandom semi sampling: the new start time is " + getTimeString(sub_startTime));
				
				//6. if the next start time is later than the overall end time, stop the sampling.
				if (sub_startTime >= sub_endTime)
					break;				
			}
			
			
			
		}
		
		else if (method.equals(SCHEDULE_SAMPLE_METHOD_FIXED_TIME_OF_DAY)){


          //  Log.d(LOG_TAG, "adding sampled time " + getTimeString(next_sample_time));


			
		}
		
		else if(method.equals(SCHEDULE_SAMPLE_METHOD_FIXED_INTERVAL)){
			
			int sample_interval = schedule.getInterval();
			int sample_period = (int) (endTime - startTime);

			//this is a clear constraint that we should only schedule this many times.
			int sample_count = schedule.getSampleCount();
			
			Log.d(LOG_TAG, "[testRandom] [calculateSampleTimes] the fixed sample interval is" + sample_interval + " and the period is " + sample_period);
			
			long next_sample_time = startTime+ sample_interval* Constants.MILLISECONDS_PER_SECOND;
			
			while (next_sample_time < endTime && sample_count>0){
				
				Log.d(LOG_TAG, "[testRandom] adding sampled time " + getTimeString(next_sample_time));
				times.add(next_sample_time);			
				next_sample_time+=sample_interval* Constants.MILLISECONDS_PER_SECOND;
				sample_count -=1;
			}
			
		}
		
		return times;
	}


    /**
     * generate the alarm for update schedules( or do stop/start the service)
     *
     */
    private static void generateRefreshServiceScheduleAlarms() {

        Log.d(LOG_TAG, "[test reschedule] SERVICE_SETTING_STOP_SERVICE_DURING_MIDNIGHT " + PreferenceHelper.getPreferenceBoolean(ConfigurationManager.SERVICE_SETTING_STOP_SERVICE_DURING_MIDNIGHT, false) );


        //if we need to schedule an alarm to stop the service
        if (PreferenceHelper.getPreferenceBoolean(ConfigurationManager.SERVICE_SETTING_STOP_SERVICE_DURING_MIDNIGHT, false)) {

            //intent for stoping service
            long stopServiceTime  = getNextTimeInMillis(bedMiddleTime);
            int request_code_stopServiceTime =NotificationHelper.generatePendingIntentRequestCode(stopServiceTime);
            savePendingIntentRequestCodeToPreference(request_code_stopServiceTime);
          //  mAlarmRequestCodes.add(request_code_stopServiceTime);
            Intent stopServiceIntent = new Intent(STOP_SERVICE_ALARM);
            //add request code
            stopServiceIntent.putExtra(ScheduleAndSampleManager.ALARM_REQUEST_CODE, REQUEST_CODE_REFRESH_STOP);
            PendingIntent stopServicePi = PendingIntent.getBroadcast(mContext, REQUEST_CODE_REFRESH_STOP, stopServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, stopServiceTime,  stopServicePi);
            Log.d(LOG_TAG, "[test reschedule] [generateRefreshServiceScheduleAlarms] the scheduled stop service is " + getTimeString(stopServiceTime) +" and the next is " + getTimeString(stopServiceTime + Constants.MILLISECONDS_PER_DAY) );


            //intent for starting service
            long startServiceTime  = getNextTimeInMillis(bedEndTime);
            int request_code_startServiceTime =NotificationHelper.generatePendingIntentRequestCode(startServiceTime);
            Intent startServiceIntent = new Intent(START_SERVICE_ALARM);
            //add request code
            startServiceIntent.putExtra(ScheduleAndSampleManager.ALARM_REQUEST_CODE, REQUEST_CODE_REFRESH_START);
            PendingIntent startServicePi = PendingIntent.getBroadcast(mContext, REQUEST_CODE_REFRESH_START, startServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, startServiceTime,  startServicePi);
            Log.d(LOG_TAG, "[test reschedule] [generateRefreshServiceScheduleAlarms] the scheduled start service is " + getTimeString(startServiceTime) +" and the next is " + getTimeString(startServiceTime+ Constants.MILLISECONDS_PER_DAY) );

        }

        else {

            //do not stop the service at midnight
            //get the next schedule update Time (or service start time) and schedule an alarm, the alarm should be scheduled during the bed time
            long updateScheduletime  = getNextTimeInMillis(bedEndTime);

            Intent intent = new Intent(UPDATE_SCHEDULE_ALARM);
            //add request code
            intent.putExtra(ScheduleAndSampleManager.ALARM_REQUEST_CODE, REQUEST_CODE_REFRESH_UPDATE);
            int request_code_updateTime =NotificationHelper.generatePendingIntentRequestCode(updateScheduletime);

            // Log.d(LOG_TAG, "[test reschedule] [generateRefreshServiceScheduleAlarms] request code: "+ intent.getIntExtra(ALARM_REQUEST_CODE, 0) + " at " + getTimeString(time) + " after this action alarm, now we have stored " + mAlarmRequestCodes.size() + " request codes");

            //the updateschedule is repeated on a daily basis
            PendingIntent pi = PendingIntent.getBroadcast(mContext, REQUEST_CODE_REFRESH_UPDATE,intent, PendingIntent.FLAG_CANCEL_CURRENT);
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, updateScheduletime, pi);
            Log.d(LOG_TAG, "[test reschedule] [generateRefreshServiceScheduleAlarms] the scheduled update time is " + getTimeString(updateScheduletime) +" and the next is " + getTimeString(updateScheduletime + Constants.MILLISECONDS_PER_DAY) );


        }

    }
	
	/**
	 * generate alarms for the schedules
	 * @param times
	 */
	private static void generateAlarmsForSamples (ArrayList<Long> times, ActionControl ac){

        //if there are times to schedule
		if (times!=null) {

            Log.d(LOG_TAG, " [generateAlarmsForSamples] there are  " + times.size() + " alarms that should be scheduled for action control "
                    + ac.getId());

            for (int i=0; i< times.size(); i++){

                long time = times.get(i);
                int request_code =NotificationHelper.generatePendingIntentRequestCode(time);

                //log the time of action control
                LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                        LogManager.LOG_TAG_ACTION_TRIGGER,
                        "Schedule ActionControl:\t" + ActionManager.getActionControlTypeName(ac.getType()) + "\t" + ac.getAction().getName() + "\t" + getTimeString(time));


                Log.d(LOG_TAG, "[generateAlarmsForSamples] we are going to " + ActionManager.getActionControlTypeName(ac.getType()) + " " + ac.getAction().getName() + " at " + getTimeString(time) +
                " with request code " + request_code);

                //create an alarm for a time
                Intent intent = new Intent(Constants.ACTION_ALARM);
                Bundle bundle = new Bundle();


                //send action control id to the alarm so that the application can retrieve the corresponding action and execute it.
               // Log.d(LOG_TAG, " [generateAlarmsForSamples] putting action control id " + ac.getId() + " to bundles with alarm at" + getTimeString(time));

                //add info to the intent, including triggered object id, and the request code.
                //id
                bundle.putInt(ConfigurationManager.ACTION_PROPERTIES_ID, ac.getId());
                //request code: use the time as its request code (so that it's kind of unique
                bundle.putInt(ALARM_REQUEST_CODE, request_code);

                //store the pendingIntent request code so that we can can cancel them in the future
               // mAlarmRequestCodes.add(request_code);
                savePendingIntentRequestCodeToPreference(request_code);

                //add extra
                intent.putExtras(bundle);
                /*
                Log.d(LOG_TAG, " [generateAlarmsForSamples] the stored action control id is " +intent.getIntExtra(ActionManager.ACTION_PROPERTIES_ID, 0) + " request code: "
                        + intent.getIntExtra(ALARM_REQUEST_CODE, 0) + " at " + getTimeString(time) + " after this action alarm, now we have stored " + mAlarmRequestCodes.size() + " request codes");
*/
                PendingIntent pi = PendingIntent.getBroadcast(mContext, request_code,intent, 0);

                mAlarmManager.set(AlarmManager.RTC_WAKEUP, time, pi);
            }

        }


	}
	
	public static int generatePendingIntentRequestCode(long time){
		
		int code = 0;
				 
		if (time-time_base > 1000000000){
			time_base = getCurrentTimeInMillis();
		}
		
		return (int) (time-time_base);
	}
	
	
	/**get the current time in milliseconds**/
	public static long getCurrentTimeInMillis(){		
		//get timzone		
		TimeZone tz = TimeZone.getDefault();		
		Calendar cal = Calendar.getInstance(tz);
		long t = cal.getTimeInMillis();		
		return t;
	}




    public static long getNextTimeInMillis(int targetHour){

        TimeZone tz  = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        long now = cal.getTimeInMillis();

        //get the date of now: the first month is Jan:0
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

       // Log.d(LOG_TAG, "[test reschedule ][getNextUpdateScheduleTimeInMillis] now is  : " + getTimeString(now) );

        //use the midnight to schedule updateScheduleAlarm
        int hourOfUpdateScheduleTime  = targetHour;
        int minute = 0;

        //set the update time tomorrow
        Calendar nextTime= Calendar.getInstance();

        //make the update time tomorrow
       // nextTime.roll(Calendar.DAY_OF_MONTH,1);
        day = nextTime.get(Calendar.DAY_OF_MONTH);

        if (targetHour==pauseProbeService){

       //     nextTime.setTimeInMillis(getCurrentTimeInMillis()+ 10*1000);
           nextTime.set(year, month-1, day, hourOfUpdateScheduleTime, 0,0);
           nextTime.roll(Calendar.DAY_OF_MONTH,1);
        }

        else if  (targetHour==resumeProbeService){
            //next day
         //   nextTime.setTimeInMillis(getCurrentTimeInMillis() + 60*1000);
            nextTime.set(year, month-1, day, hourOfUpdateScheduleTime, 0,0);
            nextTime.roll(Calendar.DAY_OF_MONTH,1);

        }


        Log.d(LOG_TAG, "[test reschedule][getNextUpdateScheduleTimeInMillis][test cancel alarm] the schedule updateTime  is  : " + getTimeString(nextTime.getTimeInMillis()) );


        return nextTime.getTimeInMillis();

    }

    public static  long getLastEndOfBedTimeInMillis() {

        long bedTime =0;

        TimeZone tz  = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        long now = cal.getTimeInMillis();

        //get the date of now: the first month is Jan:0
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        //get the daily report deliver time
        int HourOfBedEndTime  = ScheduleAndSampleManager.bedEndTime;
        int minute = 0;

        //set bedTime on the same day
        Calendar lastBedEndTime= Calendar.getInstance();
        lastBedEndTime.set(year, month-1, day, HourOfBedEndTime, minute);

        //check whether the bedTime should be on the next day..
        //has passed
        if (cal.before(lastBedEndTime)){

            //day + 1
            cal.roll(Calendar.DAY_OF_MONTH, -1);
            day = cal.get(Calendar.DAY_OF_MONTH);

        }

        //set the "day"
        lastBedEndTime.set(year, month-1, day, HourOfBedEndTime, 0,0);


        return lastBedEndTime.getTimeInMillis();
    }



	public static long getNextBedStartTimeInMillis() {

        long bedTime = 0;

        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        long now = cal.getTimeInMillis();

        //get the date of now: the first month is Jan:0
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        //get the daily report deliver time
        int hourOfBedTime = ScheduleAndSampleManager.bedStartTime;
        int minute = 0;
        int new_month= 100;

        //set bedTime on the same day
        Calendar nextBedTime = Calendar.getInstance();
        nextBedTime.set(year, month - 1, day, hourOfBedTime, minute);

        //check whether the bedTime should be on the next day..
        //has passed
        if (cal.after(nextBedTime)) {

            //day + 1
            cal.roll(Calendar.DAY_OF_MONTH, 1);
            day = cal.get(Calendar.DAY_OF_MONTH);
            new_month = nextBedTime.get(Calendar.MONTH)+1;
            Log.d(LOG_TAG, "[getNextBedStartTimeInMillis] the next bed time month " +  new_month);

        }

        //set the "day"
        nextBedTime.set(year, new_month, day, hourOfBedTime, 0, 0);

        Log.d(LOG_TAG, "[getNextBedStartTimeInMillis] the next bed time is " + getTimeString(nextBedTime.getTimeInMillis()));
        return nextBedTime.getTimeInMillis();
    }

	/**convert long to timestring**/

	public static String getTimeString(long time){

		SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
		String currentTimeString = sdf_now.format(time);

		return currentTimeString;
	}

    public static String getTimeString(long time,  SimpleDateFormat sdf){

        String currentTimeString = sdf.format(time);

        return currentTimeString;
    }
	
	
	public static AlarmManager getAlarmManager(){
		return mAlarmManager;
	}
	
	
	public static void cancelAlarmByRequestCode(int requestCode, Intent intent) {
		
		Log.d(LOG_TAG, " [cancelAlarmByRequestCode] now we are canceling the pendingIntent " +  requestCode);
		
		//create pendingIntent for each request code
		PendingIntent pi = PendingIntent.getBroadcast(mContext, requestCode,intent, 0);
		
		//cancel the pendingIntent
		mAlarmManager.cancel(pi);
		
	}

    public static void registerActionControl(ActionControl ac) {

        if (ac.getSchedule()!=null){
            //schedule the alarm for the actionControl
            Log.d(LOG_TAG, "[testRandom][registerActionControl] the control " + ac.getId()  + " for " + ac.getAction().getName()+ " has schedule" );

            ScheduleAndSampleManager.executeSchedule(ac);
        }

    }


    public static void updateScheduledActionControls() {

        Log.d(LOG_TAG, "[testRandom] [test reschedule][updateActionControlsSchedules] going to update all actioncontrol's schedule "  );

        for (int i=0; i<ActionManager.getActionControlList().size(); i++){

            ActionControl ac = ActionManager.getActionControlList().get(i);

            //only reschedule action controls that are scheduled..
            if (ac.getLaunchMethod().equals(ActionManager.ACTION_LAUNCH_STYLE_SCHEDULE)){

                registerActionControl(ac);
            }
        }

    }

    public static void cancelAllAlarms() {

        cancelAllActionAlarms();

    }
	
	
	public static void cancelAllActionAlarms() {
		
		//create action alarm intent
		Intent intent = new Intent(Constants.ACTION_ALARM);

        //get request code
        if (PreferenceHelper.getPreferenceString(PreferenceHelper.SCHEDULE_REQUEST_CODE, null)!=null) {
            try {
                JSONArray requestcodes = new JSONArray(PreferenceHelper.getPreferenceString(PreferenceHelper.SCHEDULE_REQUEST_CODE, null));

                Log.d(LOG_TAG, "[cancelAllActionAlarms] we are going to cancel " + requestcodes.length() + " pendingIntents");

                //iterate all stored request codes
                for (int i=0; i<requestcodes.length(); i++){

                    //get the request code
                    int requestCode = requestcodes.getInt(i);

                    //cancel the action alarm
                    cancelAlarmByRequestCode(requestCode, intent);
                }

                //after removing all pendingintents by request codes, should clear the request code in the preference
                PreferenceHelper.setPreferenceValue(PreferenceHelper.SCHEDULE_REQUEST_CODE, null);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }




		
	}





    /**
     * Alarm receiver for Action Alarm
     */
	static BroadcastReceiver ActionAlarmReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
        	if (intent.getAction().equals(Constants.ACTION_ALARM)){
    			Log.d(LOG_TAG, "In ActionAlarmReceiver ");
    			
    			//based on the action type, choose which action to do
    			int acId = intent.getIntExtra(ConfigurationManager.ACTION_PROPERTIES_ID, 0);
    			
    			/**retrieve action according to the action id**/
    			//int acId = bundle.getInt(ActionManager.ACTION_PROPERTIES_ID);

    			Log.d(LOG_TAG, "[ActionAlarmReceiver] the alarm receive receives action execution alarm, need to execute the action control " + acId
    					+ " with request code " + intent.getIntExtra(ALARM_REQUEST_CODE, 0));

                //write logs
                LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                        LogManager.LOG_TAG_ALARM_RECEIVED,
                        "Alarm Received:\t" + ALARM_TYPE_ACTION + "\t" + "ac-" + acId + "\t" + intent.getIntExtra(ALARM_REQUEST_CODE, 0));


    			//pass id to the ActionManager to execute the action control
    			ActionManager.executeActionControl(acId);
    		}	
     
        }
    };
	
}
