package edu.umich.si.inteco.minuku.contextmanager;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.umich.si.inteco.minuku.model.record.PhoneActivityRecord;
import edu.umich.si.inteco.minuku.services.MinukuMainService;

public class PhoneActivityManager extends ContextSourceManager {

	/** Tag for logging. */
    private static final String LOG_TAG = "AppsManager";

	//constant for screen on and off
	private static final String STRING_SCREEN_OFF = "Screen_off";
	private static final String STRING_SCREEN_ON = "Screen_on";
	private static final int SCREEN_OFF  = 0;
	private static final int SCREEN_ON  = 1;

	/**App **/
	private static String mCurForegroundActivity="defaultActivity";
	private static String mCurForegroundPackage="defaultPackagename";

	private Context mContext;
	private static ActivityManager mActivityManager;
	private static PowerManager mPowerManager;
	private static Handler mMainThread;
	
	public PhoneActivityManager(Context context){

		mContext = context;
		mActivityManager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
		mPowerManager = (PowerManager) mContext.getSystemService(mContext.POWER_SERVICE);
	}

	public static String getAndroiVersion() {
		return Build.VERSION.RELEASE;
	}

	public static int getAndroidAPILevel(){
		return Build.VERSION.SDK_INT;
	}


	/**
	 * check the current foreground activity
	 *
	 * IMPORTANT NOTE:
	 * Since Android API 5.0 APIS (sdk 21), Android changes the way we can get app information
	 * Since API 21 we're not able to use getRunningTasks to get the top acitivty.
	 * Instead, we need to use XXX to get recent statistics of app use.
	 *
	 * So below we'll check the sdk level of the phone to find out how we can get app information
	 *
	 *
	 *
	 */


	public void runMonitoringAppThread (){

		mMainThread = new Handler();

		Runnable runnable = new Runnable() {
		    @Override 
		    public void run() {

				getMostRecentRunningApp();

		    	mMainThread.postDelayed(this, MinukuMainService.DEFAULT_APP_MONITOR_RATE_INTERVAL);
		    	
		    }
		  };	
		  
		  /**start repeatedly store the extracted contextual information into Record objects**/
		  mMainThread.post(runnable); 

	}


	protected void getMostRecentRunningApp() {

		String currentApp = "NA";

		/**
		 * we have to check whether the phone is after API 21 or not.
		 */
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			UsageStatsManager usm = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);

			List<UsageStats> appList = null;

			appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
					ContextManager.getCurrentTimeInMillis() - 1000*1000, ContextManager.getCurrentTimeInMillis());

			//if there's an app list
			if (appList != null && appList.size() > 0) {
				SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
				for (UsageStats usageStats : appList) {
					mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
				}
				if (mySortedMap != null && !mySortedMap.isEmpty()) {
					currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
				}
			}
		}


		else {
			getForegroundActivityBeforeAPI21();
		}

	}

	/***
	 * this function will be
	 */
	protected static void getForegroundActivityBeforeAPI21(){

		String curRunningForegrndActivity="";
		String curRunningForegrndPackNamge="";
		/** get the info from the currently foreground running activity **/
		List<ActivityManager.RunningTaskInfo> taskInfo=null;
		
		//get the latest (or currently running) foreground activity and package name
		if ( mActivityManager!=null){

			taskInfo = mActivityManager.getRunningTasks(1);
			
			//if the screen is on, get the information of the currently running app
			if (mPowerManager.isScreenOn()){
				curRunningForegrndActivity = taskInfo.get(0).topActivity.getClassName();
				curRunningForegrndPackNamge = taskInfo.get(0).topActivity.getPackageName();
			}
			
			//if the screen is off, says the screen is off. 
			else {
				curRunningForegrndActivity = STRING_SCREEN_OFF;
				curRunningForegrndPackNamge = STRING_SCREEN_OFF;
			}


			//store the running activity and its package name in the Context Extractor
			if(taskInfo!=null){
				setCurrentForegroundActivityAndPackage(curRunningForegrndActivity, curRunningForegrndPackNamge);
			}
			
		}

	}


	public static void setCurrentForegroundActivityAndPackage(String curForegroundActivity, String curForegroundPackage) {

		mCurForegroundActivity=curForegroundActivity;
		mCurForegroundPackage=curForegroundPackage;

		//save into record
		PhoneActivityRecord record = new PhoneActivityRecord(mCurForegroundPackage,  mCurForegroundActivity);
		record.setTimestamp(ContextManager.getCurrentTimeInMillis());
		ContextManager.addRecordToPool(record);

		Log.d(LOG_TAG, "[setCurrentForegroundActivityAndPackage] the current running package is " + mCurForegroundActivity + " and the activity is " + mCurForegroundPackage);
	}



	@Override
	public void examineConditions() {

	}

	@Override
	public void stateChanged() {

	}

	@Override
	public void saveRecordsInLocalRecordPool() {

	}

}
