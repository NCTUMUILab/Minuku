package edu.umich.si.inteco.minuku.context.ContextStateManagers;

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

import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.Record.Record;
import edu.umich.si.inteco.minuku.model.StateMappingRule;
import edu.umich.si.inteco.minuku.services.MinukuMainService;

/**
 * Created by Armuro on 10/4/15.
 */
public class PhoneStatusManager extends ContextStateManager {

    //network
    //NetworkStats & NetworkStatsManager : api 23
    
    /** Tag for logging. */
    private static final String LOG_TAG = "PhoneStatusManager";


    public static final String SOURCE_TYPE_APP_STRING ="app" ;

    public static final int SOURCE_TYPE_APP =0 ;


    //constant for screen on and off
    private static final String STRING_SCREEN_OFF = "Screen_off";
    private static final String STRING_SCREEN_ON = "Screen_on";
    private static final int SCREEN_OFF  = 0;
    private static final int SCREEN_ON  = 1;

    /**latest running app **/
    private static String mLastestForegroundActivity="defaultActivity";
    private static String mLastestForegroundPackage="defaultPackagename";

    private Context mContext;
    private static ActivityManager mActivityManager;
    private static PowerManager mPowerManager;
    private static Handler mMainThread;

    public PhoneStatusManager(Context context){
        super();
        mContext = context;
        mActivityManager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
        mPowerManager = (PowerManager) mContext.getSystemService(mContext.POWER_SERVICE);

        setUpContextSourceList();

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
     */


    public void runMonitoringAppThread (){

        mMainThread = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

//                getMostRecentRunningApp();

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

        mLastestForegroundActivity=curForegroundActivity;
        mLastestForegroundPackage=curForegroundPackage;

        //save into record
        Record record = new Record();
        record.setTimestamp(ContextManager.getCurrentTimeInMillis());
        ContextManager.addRecordToPublicRecordPool(record);

        Log.d(LOG_TAG, "[setCurrentForegroundActivityAndPackage] the current running package is " + mLastestForegroundActivity + " and the activity is " + mLastestForegroundPackage);
    }


    //TODO: complete the source type table
    public static int getContextSourceTypeFromName(String sourceName) {

        switch (sourceName){

            case SOURCE_TYPE_APP_STRING:
                return SOURCE_TYPE_APP;
            default:
                return -1;
        }
    }

    public static String getContextSourceNameFromType(int sourceType) {

        switch (sourceType){

            case SOURCE_TYPE_APP:
                return SOURCE_TYPE_APP_STRING;
            default:
                return "NA";

        }
    }



    public void updateStateValues() {

        /** get the relevant rule to the source. **/
        for (int i=0; i <mStateMappingRules.size(); i++){
            StateMappingRule rule = mStateMappingRules.get(i);

            //get type from the rule
            int sourceType = getContextSourceTypeFromName(rule.getName() );

            //the source type is app
            if(sourceType==SOURCE_TYPE_APP){

            }

        }
        //

    }

}
