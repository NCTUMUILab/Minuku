package edu.umich.si.inteco.minuku.context.ContextStateManagers;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.data.RemoteDBHelper;
import edu.umich.si.inteco.minuku.model.Record.Record;
import edu.umich.si.inteco.minuku.model.StateMappingRule;
import edu.umich.si.inteco.minuku.context.TelephonyStateListenner;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;

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


    public static int mainThreadUpdateFrequencyInMinutes = 1;
    public static long mainThreadUpdateFrequencyInMilliseconds = mainThreadUpdateFrequencyInMinutes *Constants.MILLISECONDS_PER_MINUTE;

    /** Applicaiton Usage Access **/
    //how often we get the update
    public static int mApplicaitonUsageUpdateFrequencyInMinutes = mainThreadUpdateFrequencyInMinutes;
    public static long mApplicaitonUsageUpdateFrequencyInMilliseconds = mApplicaitonUsageUpdateFrequencyInMinutes *Constants.MILLISECONDS_PER_MINUTE;

    //how far we look back
    public static int mApplicaitonUsageSinceLastDurationInMinutes = 5;
    public static long mApplicaitonUsageSinceLastDurationInMilliseconds = mApplicaitonUsageSinceLastDurationInMinutes *Constants.MILLISECONDS_PER_MINUTE;

    //screen on and off
    private static final String STRING_SCREEN_OFF = "Screen_off";
    private static final String STRING_SCREEN_ON = "Screen_on";
    private static final int SCREEN_OFF  = 0;
    private static final int SCREEN_ON  = 1;


    //network connectivity
    public static String NETWORK_TYPE_WIFI = "Wifi";
    public static String NETWORK_TYPE_MOBILE = "Mobile";
    private static boolean mIsNetworkAvailable = false;
    private static boolean mIsConnected = false;
    private static String mNetworkType = "NA";
    private static boolean mIsWifiAvailable = false;
    private static boolean mIsMobileAvailable = false;
    private static boolean mIsWifiConnected = false;
    private static boolean mIsMobileConnected = false;

    //power
    private static int mBatteryLevel = -1;
    private static float mBatteryPercentage = -1;
    private static String mBatteryChargingState = "NA";
    private static boolean mIsCharging = false;

    public static final String NO_CHARGING = "Not Charging";
    public static final String USB_CHARGING = "USB Charging";
    public static final String AC_CHARGING = "AC Charging";
    
    //Telephony
    private static int mLTESignalStrength;
    private static int mGsmSignalStrength;
    private static int mCdmaSignalStrenth;
    private static int mCdmaSignalStrenthLevel; // 1, 2, 3, 4
    private static int mGeneralSignalStrength;
    private static boolean mIsGSM = false;

    //audio and ringer
    public static final String RINGER_MODE_NORMAL = "Normal";
    public static final String RINGER_MODE_VIBRATE = "Silent";
    public static final String RINGER_MODE_SILENT = "Vibrate";

    public static final String MODE_CURRENT = "Current";
    public static final String MODE_INVALID = "Invalid";
    public static final String MODE_IN_CALL = "InCall";
    public static final String MODE_IN_COMMUNICATION = "InCommunicaiton";
    public static final String MODE_NORMAL = "Normal";
    public static final String MODE_RINGTONE = "Ringtone";

    //after api 23
    public static AudioDeviceInfo[] mAllAudioDevices;



    private static String mRingerMode = "NA";
    private static String mAudioMode = "NA";
    private static int mStreamVolumeMusic = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_INTEGER;
    private static int mStreamVolumeNotification = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_INTEGER;
    private static int mStreamVolumeRing = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_INTEGER;
    private static int mStreamVolumeVoicecall = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_INTEGER;
    private static int mStreamVolumeSystem = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_INTEGER;
    private static int mStreamVolumeDTMF = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_INTEGER;



    /**latest running app **/
    private static String mLastestForegroundActivity="defaultActivity";
    private static String mLastestForegroundPackage="defaultPackagename";

    private Context mContext;
    private static ActivityManager mActivityManager;
    private static PowerManager mPowerManager;
    private static  ConnectivityManager mConnectivityManager;
    private static TelephonyStateListenner mTelephonyStateListener;
    private static TelephonyManager mTelephonyManager;
    private static AudioManager mAudioManager;
    private static Handler mMainThread;

    public PhoneStatusManager(Context context){
        super();

        mContext = context;
        mActivityManager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);

        /**power**/
        mPowerManager = (PowerManager) mContext.getSystemService(mContext.POWER_SERVICE);

        /**connectivity**/
        mConnectivityManager = (ConnectivityManager)mContext.getSystemService(context.CONNECTIVITY_SERVICE);

        /**telephony**/
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(context.TELEPHONY_SERVICE);
        mTelephonyStateListener = new TelephonyStateListenner(mContext);

        //register the phonestatelistenner
        mTelephonyManager.listen(mTelephonyStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        //audio manager
        mAudioManager = (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);


        Log.d(LOG_TAG, "[testing app]: PhoneStatusManager");

        setUpContextSourceList();


        //TODO: for testing, we request udpates when the app starts. We put it in the updateContextSource later after we start to test request
        requestUpdates();


    }

    /**
     * Allow ContextManager to requestd and remove updates
     */
    @Override
    public void requestUpdates() {

        requestRequestedContextSources();

    }

    @Override
    public void removeUpdates() {}

    /** each ContextStateManager should override this static method
     * it adds a list of ContextSource that it will manage **/
    @Override
    protected void setUpContextSourceList() {

        Log.d(LOG_TAG, "setUpContextSourceList");

//        mContextSourceList.add(
//                new ContextSource(
//                        PHONE_SENSOR_ACCELEROMETER,
//                        Sensor.TYPE_ACCELEROMETER,
//                        //if it mIs not null, return true, else, return false
//                        (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null),
//                        SensorManager.SENSOR_DELAY_NORMAL));


    }


    private void requestRequestedContextSources() {

        //if get updates of network

        //if get updates of telephoney

        /** if we will update apps. first check if we have the permission**/
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            //we first check the user has granted the permission of usage access. We need it for Android 5.0 and above
            boolean usageAccessPermissionGranted = checkApplicationUsageAccess();

            if (!usageAccessPermissionGranted) {

                Log.d(LOG_TAG, "[testing app] user has not granted permission, need to bring them to the setting");
                //ask user to grant permission to app.
                //TODO: we only do this when the app information mIs requested
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);

            }
            else {

                Log.d(LOG_TAG, "[testing app] user has granted permission! no need to bring them to the setting");
            }
        }

        //run the monitoring thread to get the status of the phone.
        runPhoneStatusMainThread();
    }


    public static String getAndroiVersion() {
        return Build.VERSION.RELEASE;
    }

    public static int getAndroidAPILevel(){
        return Build.VERSION.SDK_INT;
    }


    public void runPhoneStatusMainThread (){

        Log.d(LOG_TAG, "[testing app]runMonitoringAppThread : running at " + ContextManager.getCurrentTimeString()) ;

        mMainThread = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                //get app update
                getMostRecentRunningApp();

                //get network update
                getNetworkConnectivityUpdate();

                //get ringer update
                getAudioRingerUpdate();


                mMainThread.postDelayed(this, mainThreadUpdateFrequencyInMilliseconds);

            }
        };

        /**start repeatedly store the extracted contextual information into Record objects**/
        mMainThread.post(runnable);

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
    private boolean checkApplicationUsageAccess() {

        boolean granted = false;

        //check whether the user has granted permission to Usage Access....If not, we direct them to the Usage Setting
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            try {
                PackageManager packageManager = mContext.getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(mContext.getPackageName(), 0);
                AppOpsManager appOpsManager = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);

                int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(), mContext.getPackageName());

                granted = mode == AppOpsManager.MODE_ALLOWED;
                Log.d(LOG_TAG, "[testing app]checkApplicationUsageAccess mode mIs : " + mode + " granted: " + granted);

            } catch (PackageManager.NameNotFoundException e) {
                Log.d(LOG_TAG, "[testing app]checkApplicationUsageAccess somthing mIs wrong");
            }
        }
        return granted;

    }

    protected void getMostRecentRunningApp() {

        Log.d(LOG_TAG, "[testing app]: getMostRecentRunningApp");
        String currentApp = "NA";

        /**
         * we have to check whether the phone mIs above API 21 or not.
         */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            //UsageStatsManager mIs available after Lollipop
            UsageStatsManager usm = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);

            List<UsageStats> appList = null;

            Log.d(LOG_TAG, "getMostRecentRunningApp [testing app] API 21 query usage between:  " +
                    ScheduleAndSampleManager.getTimeString(
                            ContextManager.getCurrentTimeInMillis()- mApplicaitonUsageSinceLastDurationInMilliseconds)
                            + " and " + ContextManager.getCurrentTimeString());


            //get the application usage statistics
            appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    //start time
                    ContextManager.getCurrentTimeInMillis()- mApplicaitonUsageSinceLastDurationInMilliseconds,
                    //end time: until now
                    ContextManager.getCurrentTimeInMillis());


            //if there's an app list
            if (appList != null && appList.size() > 0) {

                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    Log.d(LOG_TAG, "getMostRecentRunningApp [testing app] last time usage:  " + ScheduleAndSampleManager.getTimeString(usageStats.getLastTimeUsed()) +
                            " usage stats " + usageStats.getPackageName() + " total time in foreground " + usageStats.getTotalTimeInForeground()/60000
                    + " between " + ScheduleAndSampleManager.getTimeString(usageStats.getFirstTimeStamp()) + " and " + ScheduleAndSampleManager.getTimeString(usageStats.getLastTimeStamp()));
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

            //if the screen mIs on, get the information of the currently running app
            if (mPowerManager.isScreenOn()){
                curRunningForegrndActivity = taskInfo.get(0).topActivity.getClassName();
                curRunningForegrndPackNamge = taskInfo.get(0).topActivity.getPackageName();
            }

            //if the screen mIs off, says the screen mIs off.
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

        Log.d(LOG_TAG, "[setCurrentForegroundActivityAndPackage] the current running package mIs " + mLastestForegroundActivity + " and the activity mIs " + mLastestForegroundPackage);
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

            //the source type mIs app
            if(sourceType==SOURCE_TYPE_APP){

            }

        }
        //

    }


    /***
     *
     * ringer
     *
     * **/
    private void getAudioRingerUpdate() {

        if (mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_NORMAL)
            mRingerMode = RINGER_MODE_NORMAL;
        else if (mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_VIBRATE)
            mRingerMode = RINGER_MODE_VIBRATE;
        else if (mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_SILENT)
            mRingerMode = RINGER_MODE_SILENT;


        int mode = mAudioManager.getMode();
        Log.d(LOG_TAG, "[getAudioRingerUpdate] ringer mode: " + mRingerMode + " mode: " + mode);

        mStreamVolumeMusic= mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mStreamVolumeNotification= mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        mStreamVolumeRing= mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        mStreamVolumeVoicecall = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        mStreamVolumeSystem= mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);

        mAudioMode = getAudioMode(mAudioManager.getMode());

        //android 6
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

           mAllAudioDevices = mAudioManager.getDevices(AudioManager.GET_DEVICES_ALL);
        }


        mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);

        Log.d(LOG_TAG, "[getAudioRingerUpdate] volume:  music" + mStreamVolumeMusic
                        + " volume: notification: " +  mStreamVolumeNotification
                        + " volume: ring " +  mStreamVolumeRing
                        + " volume: voicecall: " +  mStreamVolumeVoicecall
                        + " volume: voicesystem: " +  mStreamVolumeSystem
                        + " mode:  " +  mAudioMode
        );

    }

    public String getAudioMode(int mode) {

        if (mode==AudioManager.MODE_CURRENT)
            return MODE_CURRENT;
        else if (mode==AudioManager.MODE_IN_CALL)
            return MODE_IN_CALL;
        else if (mode==AudioManager.MODE_IN_COMMUNICATION)
            return MODE_IN_COMMUNICATION;
        else if (mode==AudioManager.MODE_INVALID)
            return MODE_INVALID;

        else if (mode==AudioManager.MODE_NORMAL)
            return MODE_NORMAL;

        else if (mode==AudioManager.MODE_RINGTONE)
            return MODE_RINGTONE;
        else
            return ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    }


    /**
     *
     * Update Network Connectivity
     *
     */
    private void getNetworkConnectivityUpdate() {


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Network[] networks = mConnectivityManager.getAllNetworks();

            NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();

            for (Network network : networks) {
                activeNetwork = mConnectivityManager.getNetworkInfo(network);
                if (activeNetwork.getState().equals(NetworkInfo.State.CONNECTED)) {

                    //TODO: get network connectiviy for Android 6 or above


                }
            }
        }

        else{

            if (mConnectivityManager!=null) {

                NetworkInfo[] info = mConnectivityManager.getAllNetworkInfo();
                NetworkInfo activeNetworkWifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo activeNetworkMobile = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                boolean isWiFi = activeNetworkWifi.getType() == ConnectivityManager.TYPE_WIFI;
                boolean isMobile = activeNetworkMobile.getType() == ConnectivityManager.TYPE_MOBILE;


                if(activeNetworkWifi !=null) {

                    mIsWifiConnected = activeNetworkWifi != null &&
                            activeNetworkWifi.isConnected();
                    mIsMobileConnected = activeNetworkWifi != null &&
                            activeNetworkMobile.isConnected();

                    mIsConnected = mIsWifiConnected | mIsMobileConnected;

                    mIsWifiAvailable = activeNetworkWifi.isAvailable();
                    mIsMobileAvailable = activeNetworkMobile.isAvailable();

                    mIsNetworkAvailable = mIsWifiAvailable | mIsMobileAvailable;


                    if (isWiFi) {

//                        Log.d(LOG_TAG, "[TestConnectivity] connect to wifi");

                        //if we only submit the data over wifh. this should be configurable
                        if (RemoteDBHelper.getSubmitDataOnlyOverWifi())
                            RemoteDBHelper.syncWithRemoteDatabase();

                        mNetworkType = NETWORK_TYPE_WIFI;
                    }

                    else if (isMobile) {
                        mNetworkType = NETWORK_TYPE_MOBILE;
                    }


                    //assign value
//
//                    Log.d(LOG_TAG, "[TestConnectivity] connectivity change available? WIFI: available " + mIsWifiAvailable  +
//                            "  mIsConnected: " + mIsWifiConnected + " Mobile: available: " + mIsMobileAvailable + " mIs connected: " + mIsMobileConnected
//                            +" network type: " + mNetworkType + ",  mIs connected: " + mIsConnected + " mIs network available " + mIsNetworkAvailable);
                }
            }

        }

        
    }


    /****
     * Getter and Setter
     */
    public static int getBatteryLevel() {
        return mBatteryLevel;
    }

    public static void setBatteryLevel(int batteryLevel) {
        PhoneStatusManager.mBatteryLevel = batteryLevel;
    }

    public static float getBatteryPercentage() {
        return mBatteryPercentage;
    }

    public static void setBatteryPercentage(float batteryPercentage) {
        PhoneStatusManager.mBatteryPercentage = batteryPercentage;
    }

    public static String getBatteryChargingState() {
        return mBatteryChargingState;
    }

    public static void setBatteryChargingState(String batteryChargingState) {
        PhoneStatusManager.mBatteryChargingState = batteryChargingState;
    }

    public static boolean isCharging() {
        return mIsCharging;
    }

    public static void setIsCharging(boolean isCharging) {
        PhoneStatusManager.mIsCharging = isCharging;
    }
    
    
    
    /***Telephony***/
    public static int getGsmSignalStrength() {
        return mGsmSignalStrength;
    }

    public static void setGsmSignalStrength(int gsmSignalStrength) {
        mGsmSignalStrength = gsmSignalStrength;
    }

    public static int getCdmaSignalStrenth() {
        return mCdmaSignalStrenth;
    }

    public static int getLTESignalStrength() {
        return mLTESignalStrength;
    }

    public static void setLTESignalStrength(int lTESignalStrength) {
        PhoneStatusManager.mLTESignalStrength = lTESignalStrength;
    }

    public static void setCdmaSignalStrenth(int cdmaSignalStrenth) {
        mCdmaSignalStrenth = cdmaSignalStrenth;
    }

    public static int getCdmaSignalStrenthLevel() {
        return mCdmaSignalStrenthLevel;
    }

    public static void setCdmaSignalStrenthLevel(int cdmaSignalStrenthLevel) {
        mCdmaSignalStrenthLevel = cdmaSignalStrenthLevel;
    }

    public static int getGeneralSignalStrength() {
        return mGeneralSignalStrength;
    }

    public static void setGeneralSignalStrength(int generalSignalStrength) {
        mGeneralSignalStrength = generalSignalStrength;
    }

    public static boolean isGSM() {
        return mIsGSM;
    }

    public static void setGSM(boolean isGSM) {
        mIsGSM = isGSM;
    }
    

}
