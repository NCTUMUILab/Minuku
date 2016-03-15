package edu.umich.si.inteco.minuku.context.ContextStateManagers;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.ContextSource;
import edu.umich.si.inteco.minuku.model.Record.Record;
import edu.umich.si.inteco.minuku.model.StateMappingRule;
import edu.umich.si.inteco.minuku.context.TelephonyStateListenner;
import edu.umich.si.inteco.minuku.receivers.BatteryStatusReceiver;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;

/**
 * Created by Armuro on 10/4/15.
 */
public class PhoneStatusManager extends ContextStateManager {

    //network
    //NetworkStats & NetworkStatsManager : api 23
    
    /** Tag for logging. */
    private static final String LOG_TAG = "PhoneStatusManager";

    /**Table Names**/
    public static final String RECORD_TABLE_NAME_APPUSAGE = "Record_Table_AppUsage";
    public static final String RECORD_TABLE_NAME_TELEPHONY = "Record_Table_Telephony";
    public static final String RECORD_TABLE_NAME_CONNECTIVITY = "Record_Table_Connectivity";
    public static final String RECORD_TABLE_NAME_BATTERY = "Record_Table_Battery";
    public static final String RECORD_TABLE_NAME_RINGER = "Record_Table_Ringer";



    /** context measure **/
    public static final String CONTEXT_SOURCE_MEASURE_BATTERY_CHARGING_STATE = "ChargingState";
    public static final String CONTEXT_SOURCE_MEASURE_BATTERY_LEVEL = "Level";
    public static final String CONTEXT_SOURCE_MEASURE_BATTERY_PERCENTAGE = "Percentage";
    public static final String CONTEXT_SOURCE_MEASURE_BATTERY_IS_CHARGING = "IsCharging";

    public static final String CONTEXT_SOURCE_MEASURE_CONNECTIVITY_NETWORK_TYPE = "Network_Type";
    public static final String CONTEXT_SOURCE_MEASURE_CONNECTIVITY_NETWORK_AVAILABLE = "NetworkAvailable";
    public static final String CONTEXT_SOURCE_MEASURE_CONNECTIVITY_NETWORK_CONNECTED = "NetworkConnected";
    public static final String CONTEXT_SOURCE_MEASURE_CONNECTIVITY_WIFI_AVAILABLE = "WifiAvailable";
    public static final String CONTEXT_SOURCE_MEASURE_CONNECTIVITY_WIFI_CONNECTED = "WifiConnected";
    public static final String CONTEXT_SOURCE_MEASURE_CONNECTIVITY_MOBILE_AVAILABLE = "MobileAvailable";
    public static final String CONTEXT_SOURCE_MEASURE_CONNECTIVITY_MOBILE_CONNECTED = "MobileConnected";

    public static final String CONTEXT_SOURCE_MEASURE_TELEPHONY_NETWORK_OPERATOR_NAME = "OperatorName";
    public static final String CONTEXT_SOURCE_MEASURE_TELEPHONY_CALL_STATE = "CallState";
    public static final String CONTEXT_SOURCE_MEASURE_TELEPHONY_PHONE_SIGNAL_TYPE = "SignalType";
    public static final String CONTEXT_SOURCE_MEASURE_TELEPHONY_GENERAL_SIGNAL_STRENGTH = "SignalStrength";

    public static final String CONTEXT_SOURCE_MEASURE_RINGER_MODE = "RingerMode";
    public static final String CONTEXT_SOURCE_MEASURE_AUDIO_MODE = "AudioMode";
    public static final String CONTEXT_SOURCE_MEASURE_STREAM_VOLUME_NOTIFICATION = "VolumeNofificaiton";
    public static final String CONTEXT_SOURCE_MEASURE_STREAM_VOLUME_RING = "VolumeRing";
    public static final String CONTEXT_SOURCE_MEASURE_STREAM_VOLUME_VOICECALL = "VolumeVoicecall";
    public static final String CONTEXT_SOURCE_MEASURE_STREAM_VOLUME_SYSTEM = "VolumeSystem";
    public static final String CONTEXT_SOURCE_MEASURE_STREAM_VOLUME_MUSIC = "VolumeMusic";

    public static final String CONTEXT_SOURCE_MEASURE_APPUSAGE_SCREEN_STATUS = "ScreenStatus";
    public static final String CONTEXT_SOURCE_MEASURE_APPUSAGE_LATEST_USED_APP = "LatestUsedApp";
    public static final String CONTEXT_SOURCE_MEASURE_APPUSAGE_USED_APPS_STATS_IN_RECENT_HOUR = "RecentApps";

    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_BATTERY_LEVEL = "Level";
    public static final String RECORD_DATA_PROPERTY_BATTERY_PERCENTAGE = "Percentage";
    public static final String RECORD_DATA_PROPERTY_BATTERY_CHARGING_STATE = "Charging_State";
    public static final String RECORD_DATA_PROPERTY_BATTERY_ISCHARGING = "Is_Charging";

    public static final String RECORD_DATA_PROPERTY_CONNECTIVITY_NETWORK_TYPE = "Network_Type";
    public static final String RECORD_DATA_PROPERTY_CONNECTIVITY_NETWORK_AVAILABLE = "Network_Available";
    public static final String RECORD_DATA_PROPERTY_CONNECTIVITY_NETWORK_CONNECTED = "Network_Connected";
    public static final String RECORD_DATA_PROPERTY_CONNECTIVITY_WIFI_AVAILABLE = "Wifi_Available";
    public static final String RECORD_DATA_PROPERTY_CONNECTIVITY_WIFI_CONNECTED = "Wifi_Connected";
    public static final String RECORD_DATA_PROPERTY_CONNECTIVITY_MOBILE_AVAILABLE = "Mobile_Available";
    public static final String RECORD_DATA_PROPERTY_CONNECTIVITY_MOBILE_CONNECTED = "Mobile_Connected";

    public static final String RECORD_DATA_PROPERTY_TELEPHONY_NETWORK_OPERATOR_NAME = "Operator_Name";
    public static final String RECORD_DATA_PROPERTY_TELEPHONY_CALL_STATE = "Call_State";
    public static final String RECORD_DATA_PROPERTY_TELEPHONY_PHONE_SIGNAL_TYPE = "Signal_Type";
    public static final String RECORD_DATA_PROPERTY_TELEPHONY_LTE_SIGNAL_STRENGTH = "LTE_Strength";
    public static final String RECORD_DATA_PROPERTY_TELEPHONY_GSM_SIGNAL_STRENGTH = "GSM_Strength";
    public static final String RECORD_DATA_PROPERTY_TELEPHONY_CDMA_SIGNAL_STRENGTH = "CDMA_Strength";
    public static final String RECORD_DATA_PROPERTY_TELEPHONY_CDMA_SIGNAL_STRENTH_LEVEL = "CDMA_Level";
    public static final String RECORD_DATA_PROPERTY_TELEPHONY_GENERAL_SIGNAL_STRENGTH = "Signal_Strength";

    public static final String RECORD_DATA_PROPERTY_RINGER_MODE = "Ringer_Mode";
    public static final String RECORD_DATA_PROPERTY_AUDIO_MODE = "Audio_Mode";
    public static final String RECORD_DATA_PROPERTY_STREAM_VOLUME_NOTIFICATION = "Volume_Nofificaiton";
    public static final String RECORD_DATA_PROPERTY_STREAM_VOLUME_RING = "Volume_Ring";
    public static final String RECORD_DATA_PROPERTY_STREAM_VOLUME_VOICECALL = "Volume_Voicecall";
    public static final String RECORD_DATA_PROPERTY_STREAM_VOLUME_SYSTEM = "Volume_System";
    public static final String RECORD_DATA_PROPERTY_STREAM_VOLUME_MUSIC = "Volume_Music";

    public static final String RECORD_DATA_PROPERTY_APPUSAGE_SCREEN_STATUS = "Screen_Status";
    public static final String RECORD_DATA_PROPERTY_APPUSAGE_LATEST_USED_APP = "Latest_Used_App";
    public static final String RECORD_DATA_PROPERTY_APPUSAGE_LATEST_USED_APP_TIME = "Latest_Used_App_Time";
    public static final String RECORD_DATA_PROPERTY_APPUSAGE_LATEST_FOREGROUND_ACTIVITY = "Latest_Foreground_Activity";
    public static final String RECORD_DATA_PROPERTY_APPUSAGE_USED_APPS_STATS_IN_RECENT_HOUR = "Recent_Apps";
    public static final String RECORD_DATA_PROPERTY_APPUSAGE_APP_USE_DURATION_IN_LAST_CERTAIN_TIME = "AppUseDurationInLastCertainTime";


    /**ContextSourceType**/
    public static final int CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE = 0;
    public static final int CONTEXT_SOURCE_PHONE_STATUS_RINGER = 1;
    public static final int CONTEXT_SOURCE_PHONE_STATUS_BATTERY = 2;
    public static final int CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY = 3;
    public static final int CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY = 4;

    public static final String STRING_CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE = "PhoneStatus-AppUsage";
    public static final String STRING_CONTEXT_SOURCE_PHONE_STATUS_RINGER = "PhoneStatus-Ringer";
    public static final String STRING_CONTEXT_SOURCE_PHONE_STATUS_BATTERY = "PhoneStatus-Battery";
    public static final String STRING_CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY = "PhoneStatus-Telephony";
    public static final String STRING_CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY = "PhoneStatus-Connectivity";

    public static final String SOURCE_TYPE_APP_STRING ="app" ;

    public static final int SOURCE_TYPE_APP =0 ;


    public static int mainThreadUpdateFrequencyInSeconds = 5;
    public static long mainThreadUpdateFrequencyInMilliseconds = mainThreadUpdateFrequencyInSeconds *Constants.MILLISECONDS_PER_SECOND;

    /** Applicaiton Usage Access **/
    //how often we get the update
    public static int mApplicaitonUsageUpdateFrequencyInSeconds = mainThreadUpdateFrequencyInSeconds;
    public static long mApplicaitonUsageUpdateFrequencyInMilliseconds = mApplicaitonUsageUpdateFrequencyInSeconds *Constants.MILLISECONDS_PER_SECOND;

    //how far we look back
    public static int mApplicaitonUsageSinceLastDurationInSeconds = mApplicaitonUsageUpdateFrequencyInSeconds;
    public static long mApplicaitonUsageSinceLastDurationInMilliseconds = mApplicaitonUsageSinceLastDurationInSeconds *Constants.MILLISECONDS_PER_SECOND;

    //screen on and off
    private static final String STRING_SCREEN_OFF = "Screen_off";
    private static final String STRING_SCREEN_ON = "Screen_on";
    private static final String STRING_INTERACTIVE = "Interactive";
    private static final String STRING_NOT_INTERACTIVE = "Not_Interactive";

    private static String mScreenStatus = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;


    //network connectivity
    public static String NETWORK_TYPE_WIFI = "Wifi";
    public static String NETWORK_TYPE_MOBILE = "Mobile";
    private static boolean mIsNetworkAvailable = false;
    private static boolean mIsConnected = false;
    private static boolean mIsWifiAvailable = false;
    private static boolean mIsMobileAvailable = false;
    private static boolean mIsWifiConnected = false;
    private static boolean mIsMobileConnected = false;

    //power
    private static int mBatteryLevel = -1;
    private static float mBatteryPercentage = -1;
    private static String mBatteryChargingState = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    private static boolean mIsCharging = false;

    public static final String NO_CHARGING = "Not Charging";
    public static final String USB_CHARGING = "USB Charging";
    public static final String AC_CHARGING = "AC Charging";
    
    //Telephony
    public static final String PHONE_SIGNAL_TYPE_NONE="None";
    public static final String PHONE_SIGNAL_TYPE_GSM="GSM";
    public static final String PHONE_SIGNAL_TYPE_CDMA="CDMA";
    public static final String PHONE_SIGNAL_TYPE_SIP="SIP";

    public static final String CALL_STATE_IDLE="Idle";
    public static final String CALL_STATE_OFFHOOK="Offhook";
    public static final String CALL_STATE_RINGING="Rining";

    public static String mNetworkOperatorName = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    public static String mNetworkType = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    public static String mCallState = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    private static String mPhoneSignalType= ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;

    private static int mLTESignalStrength;
    private static int mGsmSignalStrength;
    private static int mCdmaSignalStrenth;
    private static int mCdmaSignalStrenthLevel; // 1, 2, 3, 4
    private static int mGeneralSignalStrength;
    private static boolean mIsGSM = false;



    //TODO: TrafficStats


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
    private static String mLastestForegroundActivity= ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    private static String mLastestForegroundPackage= ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    private static String mLastestForegroundPackageTime= ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    private static String mRecentUsedAppsInLastHour= ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;

    private Context mContext;
    private static ActivityManager mActivityManager;

    private static PowerManager mPowerManager;
    private static BatteryStatusReceiver mBatteryStatusReceiver;

    private static  ConnectivityManager mConnectivityManager;
    private static TelephonyStateListenner mTelephonyStateListener;
    private static TelephonyManager mTelephonyManager;
    private static AudioManager mAudioManager;
    private static Handler mMainThread;

    private static HashMap<String, String> mAppPackageNameHmap;


    public PhoneStatusManager(Context context){
        super();

        mContext = context;

        //load app XML
        mAppPackageNameHmap = new HashMap<String, String>();
        loadAppAndPackage();

        mActivityManager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);

        /**power**/
        mPowerManager = (PowerManager) mContext.getSystemService(mContext.POWER_SERVICE);
        mBatteryStatusReceiver = new BatteryStatusReceiver();


        /**connectivity**/
        mConnectivityManager = (ConnectivityManager)mContext.getSystemService(context.CONNECTIVITY_SERVICE);

        /**telephony**/
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(context.TELEPHONY_SERVICE);
        mTelephonyStateListener = new TelephonyStateListenner(mContext);


        //audio manager
        mAudioManager = (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);


        Log.d(LOG_TAG, "[testing app]: PhoneStatusManager");

        setUpContextSourceList();


        runPhoneStatusMainThread();


    }

    /** each ContextStateManager should override this static method
     * it adds a list of ContextSource that it will manage **/
    @Override
    protected void setUpContextSourceList() {

        Log.d(LOG_TAG, "setUpContextSourceList");

        //app usage
        mContextSourceList.add(
                new ContextSource(
                        STRING_CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE,
                        CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE,
                        true,
                        mainThreadUpdateFrequencyInMilliseconds));

        //ringer
        mContextSourceList.add(
                new ContextSource(
                        STRING_CONTEXT_SOURCE_PHONE_STATUS_RINGER,
                        CONTEXT_SOURCE_PHONE_STATUS_RINGER,
                        true,
                        mainThreadUpdateFrequencyInMilliseconds));

        //battery. the update is pushed by the batterystatusreceiver. so there's no frequency update
        mContextSourceList.add(
                new ContextSource(
                        STRING_CONTEXT_SOURCE_PHONE_STATUS_BATTERY,
                        CONTEXT_SOURCE_PHONE_STATUS_BATTERY,
                        true));

        //telephony.  the update is pushed by the phone telephnony listener. so there's no frequency update
        mContextSourceList.add(
                new ContextSource(
                        STRING_CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY,
                        CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY,
                        true));

        //connectivity
        mContextSourceList.add(
                new ContextSource(
                        STRING_CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY,
                        CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY,
                        true,
                        mainThreadUpdateFrequencyInMilliseconds));

    }


    /**
     * Allow ContextManager to requestd and remove updates
     */
    @Override
    public void requestUpdates() {}

    @Override
    public void removeUpdates() {}


    /** this function allows ConfigurationManager to adjust the configuration of each ContextSource,
     * e.g sampling rate. */
    @Override
    public void updateContextSourceList(String source, long samplingRate){

        //since all of the sources use the same main thread, for now, we just change the delay of the main thread
        mainThreadUpdateFrequencyInMilliseconds = samplingRate;

    }

    /**
     * ContextStateMAnager needs to override this fundtion to implement writing a Record and save it to the LocalDataPool
     */
    public void saveRecordToLocalRecordPool(String sourceName) {
//
//        Log.d(LOG_TAG, "[test save records] saving record of " + sourceName);

        /** store values into a Record so that we can store them in the local database **/
        Record record = new Record();
        record.setTimestamp(ContextManager.getCurrentTimeInMillis());
        record.setSource(sourceName);

        /** create data in a JSON Object. Each CotnextSource will have different formats.
         * So we need each ContextSourceMAnager to implement this part**/
        JSONObject data = new JSONObject();
        JSONArray array = new JSONArray();

        if (sourceName.equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE)) {

            try {

                data.put(RECORD_DATA_PROPERTY_APPUSAGE_SCREEN_STATUS, mScreenStatus);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

                    data.put(RECORD_DATA_PROPERTY_APPUSAGE_LATEST_USED_APP, mLastestForegroundPackage);
                    data.put(RECORD_DATA_PROPERTY_APPUSAGE_LATEST_USED_APP_TIME, mLastestForegroundPackageTime);
                    data.put(RECORD_DATA_PROPERTY_APPUSAGE_USED_APPS_STATS_IN_RECENT_HOUR, mRecentUsedAppsInLastHour);

                }
                else {
                    data.put(RECORD_DATA_PROPERTY_APPUSAGE_LATEST_USED_APP, mLastestForegroundPackage);
                    data.put(RECORD_DATA_PROPERTY_APPUSAGE_LATEST_FOREGROUND_ACTIVITY, mLastestForegroundActivity);
                }


            }catch (JSONException e) {
                e.printStackTrace();
            }



        }

        else if (sourceName.equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_RINGER)) {

            try {
                data.put(RECORD_DATA_PROPERTY_RINGER_MODE, mRingerMode);
                data.put(RECORD_DATA_PROPERTY_AUDIO_MODE, mAudioMode);
                data.put(RECORD_DATA_PROPERTY_STREAM_VOLUME_MUSIC, mStreamVolumeMusic);
                data.put(RECORD_DATA_PROPERTY_STREAM_VOLUME_NOTIFICATION, mStreamVolumeNotification);
                data.put(RECORD_DATA_PROPERTY_STREAM_VOLUME_RING, mStreamVolumeRing);
                data.put(RECORD_DATA_PROPERTY_STREAM_VOLUME_VOICECALL, mStreamVolumeVoicecall);
                data.put(RECORD_DATA_PROPERTY_STREAM_VOLUME_SYSTEM, mStreamVolumeSystem);

            }catch (JSONException e) {
                e.printStackTrace();
            }

        }

        else if (sourceName.equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY)) {

            try {

                data.put(RECORD_DATA_PROPERTY_TELEPHONY_NETWORK_OPERATOR_NAME, mNetworkOperatorName);
                data.put(RECORD_DATA_PROPERTY_TELEPHONY_CALL_STATE, mCallState);
                data.put(RECORD_DATA_PROPERTY_TELEPHONY_PHONE_SIGNAL_TYPE, mPhoneSignalType);

                if (isGSM()) {
                    data.put(RECORD_DATA_PROPERTY_TELEPHONY_GENERAL_SIGNAL_STRENGTH, getGeneralSignalStrength() );
                    data.put(RECORD_DATA_PROPERTY_TELEPHONY_GSM_SIGNAL_STRENGTH, getGsmSignalStrength());
                    data.put(RECORD_DATA_PROPERTY_TELEPHONY_LTE_SIGNAL_STRENGTH, getLTESignalStrength());

                }
                else {
                    data.put(RECORD_DATA_PROPERTY_TELEPHONY_CDMA_SIGNAL_STRENGTH, getCdmaSignalStrenth());
                    data.put(RECORD_DATA_PROPERTY_TELEPHONY_CDMA_SIGNAL_STRENTH_LEVEL, getCdmaSignalStrenthLevel());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        else if (sourceName.equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY)){

            try {
                data.put(RECORD_DATA_PROPERTY_CONNECTIVITY_NETWORK_TYPE, mNetworkType);
                data.put(RECORD_DATA_PROPERTY_CONNECTIVITY_NETWORK_AVAILABLE, mIsNetworkAvailable);
                data.put(RECORD_DATA_PROPERTY_CONNECTIVITY_NETWORK_CONNECTED, mIsConnected);
                data.put(RECORD_DATA_PROPERTY_CONNECTIVITY_WIFI_AVAILABLE, mIsWifiAvailable);
                data.put(RECORD_DATA_PROPERTY_CONNECTIVITY_WIFI_CONNECTED, mIsWifiConnected);
                data.put(RECORD_DATA_PROPERTY_CONNECTIVITY_MOBILE_AVAILABLE, mIsMobileAvailable);
                data.put(RECORD_DATA_PROPERTY_CONNECTIVITY_MOBILE_CONNECTED, mIsMobileConnected);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        else if (sourceName.equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_BATTERY)) {

            try {

                data.put(RECORD_DATA_PROPERTY_BATTERY_LEVEL, mBatteryLevel);
                data.put(RECORD_DATA_PROPERTY_BATTERY_PERCENTAGE, mBatteryPercentage);
                data.put(RECORD_DATA_PROPERTY_BATTERY_CHARGING_STATE, mBatteryChargingState);
                data.put(RECORD_DATA_PROPERTY_BATTERY_ISCHARGING, mIsCharging);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



        /*** Set data to Record **/
        record.setData(data);

//        Log.d(LOG_TAG, "[test save records] the data of " + sourceName + " is " + record.getData());


        /** Save Record**/
        mLocalRecordPool.add(record);

    }

    @Override
    protected void updateContextSourceListRequestStatus() {

        boolean isRequested = false;

        for (int i=0; i<mContextSourceList.size(); i++){

            ContextSource source = mContextSourceList.get(i);

            mContextSourceList.get(i).setIsRequested(updateContextSourceRequestStatus(source));
            Log.d(LOG_TAG, "[[test source being requested]] check saving data the contextsource "
                    + source.getName() + " requested: " + source.isRequested());

            isRequested = mContextSourceList.get(i).isRequested();

            //If neither AllProbableActivities nor MostProbableActivity are requested, we should stop requesting activity information
            if (isRequested){

                if (!mCurrentlyRequestedContextSourceList.contains(source)){
                    Log.d(LOG_TAG, "[[test source being requested]], start requesting informatoin");
                    requestUpdate(source);
                }

            }
            else {
                //TODO: check if the contextsource is currently getting update, if yes, remove update

                if (mCurrentlyRequestedContextSourceList.contains(source)){
                    Log.d(LOG_TAG, "[[test source being requested]], stop requesting informatoin because it is not needed anymore");
                    removeUpdate(source);
                }

            }
        }
    }


    /**
     * request update based on the source
     * @param source
     */
    private void requestUpdate(ContextSource source) {

        Log.d(LOG_TAG, "[[test source being requested]], requestUpdate " + source.getName());

        mCurrentlyRequestedContextSourceList.add(source);

        if (source.getName().equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE)) {
            //we need to check whether we have permission to get app usage.
            requestAppUsageUpdate();
        }
        else if (source.getName().equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_BATTERY)) {
            requestBatteryUpdate();
            //todo: get battery update
        }
        else if (source.getName().equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY)) {
            //we don't do anything because connectivity sources are pull instead of push
        }
        else if (source.getName().equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_RINGER)) {
            //we don't do anything because connectivity sources are pull instead of push
        }
        else if (source.getName().equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY)) {
            requestTelephonyUpdate();
        }

    }


    /**
     * remove update based on the source
     * @param source
     */
    private void removeUpdate(ContextSource source) {

        mCurrentlyRequestedContextSourceList.remove(source);

        if (source.getName().equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE)) {
            //we don't do anything because connectivity sources are pull instead of push
        }
        else if (source.getName().equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_BATTERY)) {
            removeBatteryUpdate();

        }
        else if (source.getName().equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY)) {
            //we don't do anything because connectivity sources are pull instead of push
        }
        else if (source.getName().equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_RINGER)) {
            //we don't do anything because connectivity sources are pull instead of push
        }
        else if (source.getName().equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY)) {
            removeTelephonyUpdate();
        }

    }

    /**
     * Register Intent.ACTION_BATTERY_CHANGED Receiver
     */
    private void requestBatteryUpdate() {
        IntentFilter batteryStatus_filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mBatteryStatusReceiver, batteryStatus_filter);
    }

    /**
     * Unregister Intent.ACTION_BATTERY_CHANGED Receiver
     */
    private void removeBatteryUpdate() {
        mContext.unregisterReceiver(mBatteryStatusReceiver);
    }


    //register the phonestatelistenner
    private void requestTelephonyUpdate() {

        mTelephonyManager.listen(mTelephonyStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        mPhoneSignalType = getPhoneTypeName(mTelephonyManager.getPhoneType());
        mNetworkOperatorName = mTelephonyManager.getNetworkOperatorName();

//        Log.d(LOG_TAG, "[test source being requested] getDeviceId " + mTelephonyManager.getDeviceId() + " NetworkOperator " + mTelephonyManager.getNetworkOperator()
//                + " NetworkOperatorName " + mTelephonyManager.getNetworkOperatorName() + " CallState " + mTelephonyManager.getCallState() + " getCellLocation " + mTelephonyManager.getCellLocation()
//                + " DataActivity " + mTelephonyManager.getDataActivity() + "  NetworkType " + mTelephonyManager.getNetworkType() + " PhoneType" + mTelephonyManager.getPhoneType());
    }

    //unregister the phonestatelistenner
    private void removeTelephonyUpdate() {
        Log.d(LOG_TAG, "[test source being requested] getDeviceId");
        mTelephonyManager.listen(mTelephonyStateListener, PhoneStateListener.LISTEN_NONE);
    }


    private void requestAppUsageUpdate() {

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
        }

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


                Log.d(LOG_TAG, "[test source being requested] app: " + isContextSourceInCurrentRequestedList(STRING_CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE)
                 +  " connectivity " + isContextSourceInCurrentRequestedList(STRING_CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY)
                +  " ringer " + isContextSourceInCurrentRequestedList(STRING_CONTEXT_SOURCE_PHONE_STATUS_RINGER ));

                //get app usage update
                if(isContextSourceInCurrentRequestedList(STRING_CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE)){

                    //get screen data
                    getScreen();

                    //get app data
                    getAppUsageUpdate();

                    updateStateValues(CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE);

                    //save
                    saveRecordToLocalRecordPool(STRING_CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE);
                }

                //get network update
                if(isContextSourceInCurrentRequestedList(STRING_CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY)){
                    getNetworkConnectivityUpdate();

                    updateStateValues(CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY);
                    //save
                    saveRecordToLocalRecordPool(STRING_CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY);
                }

                //get ringer update
                if(isContextSourceInCurrentRequestedList(STRING_CONTEXT_SOURCE_PHONE_STATUS_RINGER)) {
                    getAudioRingerUpdate();

                    updateStateValues(CONTEXT_SOURCE_PHONE_STATUS_RINGER);
                    //save
                    saveRecordToLocalRecordPool(STRING_CONTEXT_SOURCE_PHONE_STATUS_RINGER);
                }


                if (isContextSourceInCurrentRequestedList(STRING_CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY)) {

                    updateStateValues(CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY);

                    //we don't save telephony records every time we get a change because if updates too frequently
                    saveRecordToLocalRecordPool(STRING_CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY);

                }


                if (isContextSourceInCurrentRequestedList(STRING_CONTEXT_SOURCE_PHONE_STATUS_BATTERY)) {

                    updateStateValues(CONTEXT_SOURCE_PHONE_STATUS_BATTERY);

                    //we don't save battery records every time we get a change because if updates too frequently
                    saveRecordToLocalRecordPool(STRING_CONTEXT_SOURCE_PHONE_STATUS_BATTERY);

                }



                mMainThread.postDelayed(this, mainThreadUpdateFrequencyInMilliseconds);

            }
        };

        /**start repeatedly store the extracted contextual information into Record objects**/
        mMainThread.post(runnable);

    }


    public String getAppNameFromPackageName(String packageName){




        return null;
    }


    /**
     * these functions should be implemented in a ContextStateManager. It examines StateMappingRule with the
     * data and returns a boolean pass.
     * @param sourceType
     * @param measure
     * @param relationship
     * @param targetValue
     * @return
     */
    @Override
    protected boolean examineStateRule(int sourceType, String measure, String relationship, String targetValue, ArrayList<String> params ){

        boolean pass = false;

        Log.d(LOG_TAG, "test smr app examine statemappingrule, get measure "
                + getContextSourceNameFromType(sourceType) +
                " now examine target value : " + targetValue );

        //1 first we need to get the right source based on the sourcetype.
        //so that we know where the get the source value.
        String sourceValue=null;

        if (sourceType== CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE){

            Log.d(LOG_TAG, "test smr app examine statemappingrule enter CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE " );

            if (measure.equals(CONTEXT_SOURCE_MEASURE_APPUSAGE_LATEST_USED_APP)){

                Log.d(LOG_TAG, "test smr app examine statemappingrule measure: CONTEXT_SOURCE_MEASURE_APPUSAGE_LATEST_USED_APP  " );


                /** for app, we need to convert package name to app name**/
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {


                }
                else {

                    sourceValue = mAppPackageNameHmap.get(mLastestForegroundPackage);

                }


//                Log.d(LOG_TAG, "test smr the app names are package: " + mLastestForegroundPackage + "app name " + sourceValue);
            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_APPUSAGE_SCREEN_STATUS)){
                sourceValue = mScreenStatus;
            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_APPUSAGE_USED_APPS_STATS_IN_RECENT_HOUR)){

            }
        }

        else if (sourceType== CONTEXT_SOURCE_PHONE_STATUS_BATTERY){

            Log.d(LOG_TAG, "test smr app examine statemappingrule enter CONTEXT_SOURCE_PHONE_STATUS_BATTERY " );


            /**1. get source value according to the measure type**/
            //if the measure is "latest value", get the latest saved data**/
            if (measure.equals(CONTEXT_SOURCE_MEASURE_BATTERY_CHARGING_STATE)){
                Log.d(LOG_TAG, "test smr app examine statemappingrule enter CONTEXT_SOURCE_MEASURE_BATTERY_CHARGING_STATE " );

                sourceValue = getBatteryChargingState();
            }


        }
        else if (sourceType== CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY){

            if (measure.equals(CONTEXT_SOURCE_MEASURE_CONNECTIVITY_NETWORK_TYPE)){
                sourceValue = mNetworkType;
            }
        }

        else if (sourceType== CONTEXT_SOURCE_PHONE_STATUS_RINGER){

            if (measure.equals(CONTEXT_SOURCE_MEASURE_RINGER_MODE)){
                sourceValue = mRingerMode;
            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_AUDIO_MODE)) {
                sourceValue = mAudioMode;
            }

        }


        else if (sourceType== CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY){

            if (measure.equals(CONTEXT_SOURCE_MEASURE_TELEPHONY_CALL_STATE)){
                sourceValue = getCallState();
            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_TELEPHONY_NETWORK_OPERATOR_NAME)){
                sourceValue = mNetworkOperatorName;
            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_TELEPHONY_PHONE_SIGNAL_TYPE)){
                sourceValue = mPhoneSignalType;
            }


        }

        /** 2. examine the criterion after we get the source value**/
        if (sourceValue != null) {

            pass = satisfyCriterion(sourceValue, relationship, targetValue);
                Log.d(LOG_TAG, "test smr app examine statemappingrule, get measure "
                        + getContextSourceNameFromType(sourceType) + " and get value : " +  sourceValue +
                        " now examine target value : " + targetValue + " so the pass is : " + pass);

        }


        return  pass;

    }


    @Override
    protected boolean examineStateRule(int sourceType, String measure, String relationship, int targetValue,  ArrayList<String> params ){

        boolean pass = false;
        int sourceValue=-1;

        if (sourceType== CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE){


            if (measure.equals(RECORD_DATA_PROPERTY_APPUSAGE_APP_USE_DURATION_IN_LAST_CERTAIN_TIME)){

                //we expect to get two parameters here: last certain time, and the target app

            }

        }

        else if (sourceType== CONTEXT_SOURCE_PHONE_STATUS_BATTERY){

            if (measure.equals(CONTEXT_SOURCE_MEASURE_BATTERY_LEVEL)){

                sourceValue = getBatteryLevel();
            }
            else if (measure.equals(CONTEXT_SOURCE_MEASURE_BATTERY_PERCENTAGE)){
                sourceValue = (int)getBatteryPercentage();
            }

        }

        else if (sourceType== CONTEXT_SOURCE_PHONE_STATUS_RINGER){

            if (measure.equals(CONTEXT_SOURCE_MEASURE_STREAM_VOLUME_RING)){
                sourceValue = mStreamVolumeRing;
            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_STREAM_VOLUME_MUSIC)) {
                sourceValue = mStreamVolumeMusic;

            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_STREAM_VOLUME_NOTIFICATION)) {
                sourceValue = mStreamVolumeNotification;

            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_STREAM_VOLUME_SYSTEM)) {
                sourceValue = mStreamVolumeSystem;
            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_STREAM_VOLUME_VOICECALL)) {
                sourceValue = mStreamVolumeVoicecall;
            }

        }


        else if (sourceType== CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY){

            if (measure.equals(CONTEXT_SOURCE_MEASURE_TELEPHONY_GENERAL_SIGNAL_STRENGTH)){
                sourceValue = getGeneralSignalStrength();
            }

        }

        /** examine the criterion after we get the source value**/
        if (sourceValue != -1) {

            pass = satisfyCriterion(sourceValue, relationship, targetValue);
//                Log.d(LOG_TAG, "test smr examine statemappingrule, get measure "
//                        + getContextSourceNameFromType(sourceType) + " and get value : " +  sourceValue +
//                        "now examine target value : " + targetValue + " so the pass is : " + pass);

        }

        return  pass;
    }


    @Override
    protected boolean examineStateRule(int sourceType, String measure, String relationship, boolean targetValue, ArrayList<String> params ) {

        boolean pass = false;

        boolean sourceValue;


        if (sourceType== CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY){

            if (measure.equals(CONTEXT_SOURCE_MEASURE_CONNECTIVITY_MOBILE_AVAILABLE)){
                sourceValue  = mIsMobileAvailable;
            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_CONNECTIVITY_MOBILE_CONNECTED)){
                sourceValue  = mIsMobileConnected;
            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_CONNECTIVITY_WIFI_AVAILABLE)){
                sourceValue  = mIsWifiAvailable;
            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_CONNECTIVITY_WIFI_CONNECTED)){
                sourceValue  = mIsMobileConnected;
            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_CONNECTIVITY_NETWORK_AVAILABLE)){
                sourceValue  = mIsNetworkAvailable;
            }

            else if (measure.equals(CONTEXT_SOURCE_MEASURE_CONNECTIVITY_NETWORK_CONNECTED)){
                sourceValue  = mIsNetworkAvailable;
            }


        }

        else if (sourceType== CONTEXT_SOURCE_PHONE_STATUS_BATTERY){

            Log.d(LOG_TAG, "test smr app examine statemappingrule enter CONTEXT_SOURCE_PHONE_STATUS_BATTERY " );


            /**1. get source value according to the measure type**/
            //if the measure is "latest value", get the latest saved data**/
            if (measure.equals(CONTEXT_SOURCE_MEASURE_BATTERY_IS_CHARGING)){
                Log.d(LOG_TAG, "test smr app examine statemappingrule enter CONTEXT_SOURCE_MEASURE_BATTERY_IS_CHARGING ischarging " + mIsCharging );

                sourceValue = mIsCharging;
            }


        }


        return  pass;
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
                Log.d(LOG_TAG, "[test source being requested]checkApplicationUsageAccess mode mIs : " + mode + " granted: " + granted);

            } catch (PackageManager.NameNotFoundException e) {
                Log.d(LOG_TAG, "[testing app]checkApplicationUsageAccess somthing mIs wrong");
            }
        }
        return granted;

    }


    protected  void getScreen() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {

            //use isInteractive after api 20
            if (mPowerManager.isInteractive())
                mScreenStatus = STRING_INTERACTIVE;
            else
                mScreenStatus = STRING_SCREEN_OFF;

        }
        //before API20, we use screen on or off
        else {
            if(mPowerManager.isScreenOn())
                mScreenStatus = STRING_SCREEN_ON;
            else
                mScreenStatus = STRING_SCREEN_OFF;

        }

        Log.d(LOG_TAG, "test source being requested [testing app] SCREEN:  " + mScreenStatus);
    }


    protected void getAppUsageUpdate() {

        Log.d(LOG_TAG, "test source being requested [testing app]: getAppUsageUpdate");
        String currentApp = "NA";

        /**
         * we have to check whether the phone mIs above API 21 or not.
         */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            //UsageStatsManager mIs available after Lollipop
            UsageStatsManager usm = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);

            List<UsageStats> appList = null;

            Log.d(LOG_TAG, "test source being requested [testing app] API 21 query usage between:  " +
                    ScheduleAndSampleManager.getTimeString(
                            ContextManager.getCurrentTimeInMillis()- mApplicaitonUsageSinceLastDurationInMilliseconds)
                            + " and " + ContextManager.getCurrentTimeString());


            //get the application usage statistics
            appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    //start time
                    ContextManager.getCurrentTimeInMillis()- mApplicaitonUsageSinceLastDurationInMilliseconds,
                    //end time: until now
                    ContextManager.getCurrentTimeInMillis());

            mRecentUsedAppsInLastHour = "";


            //if there's an app list
            if (appList != null && appList.size() > 0) {

                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    Log.d(LOG_TAG, "test app:  " + ScheduleAndSampleManager.getTimeString(usageStats.getLastTimeUsed()) +
                            " usage stats " + usageStats.getPackageName() + " total time in foreground " + usageStats.getTotalTimeInForeground()/60000
                    + " between " + ScheduleAndSampleManager.getTimeString(usageStats.getFirstTimeStamp()) + " and " + ScheduleAndSampleManager.getTimeString(usageStats.getLastTimeStamp()));
                }



                if (mySortedMap != null && !mySortedMap.isEmpty()) {

                    mLastestForegroundPackage = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    mLastestForegroundPackageTime = ScheduleAndSampleManager.getTimeString(mySortedMap.get(mySortedMap.lastKey()).getLastTimeUsed());

                    Log.d(LOG_TAG, "test app "  +  mLastestForegroundPackage + " time " +
                            mLastestForegroundPackageTime);
                }


                //create a string for mRecentUsedAppsInLastHour
                for(Map.Entry<Long, UsageStats> entry : mySortedMap.entrySet()) {
                    long key = entry.getKey();
                    UsageStats stats = entry.getValue();

                    mRecentUsedAppsInLastHour += stats.getPackageName() + ":" + ScheduleAndSampleManager.getTimeString(key);
                    if (key!=mySortedMap.lastKey())
                        mRecentUsedAppsInLastHour += Constants.DELIMITER_IN_COLUMN;

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

            curRunningForegrndActivity = taskInfo.get(0).topActivity.getClassName();
            curRunningForegrndPackNamge = taskInfo.get(0).topActivity.getPackageName();

            Log.d(LOG_TAG, "test app os version " +android.os.Build.VERSION.SDK_INT + " under 21 "
                     + curRunningForegrndActivity + " " + curRunningForegrndPackNamge );

            //store the running activity and its package name in the Context Extractor
            if(taskInfo!=null){
                setCurrentForegroundActivityAndPackage(curRunningForegrndActivity, curRunningForegrndPackNamge);
            }

        }

    }


    public static void setCurrentForegroundActivityAndPackage(String curForegroundActivity, String curForegroundPackage) {

        mLastestForegroundActivity=curForegroundActivity;
        mLastestForegroundPackage=curForegroundPackage;

//        Log.d(LOG_TAG, "[setCurrentForegroundActivityAndPackage] the current running package mIs " + mLastestForegroundActivity + " and the activity mIs " + mLastestForegroundPackage);
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
//        Log.d(LOG_TAG, "[getAudioRingerUpdate] ringer mode: " + mRingerMode + " mode: " + mode);

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

//        Log.d(LOG_TAG, "[test source being requested] volume:  music" + mStreamVolumeMusic
//                        + " volume: notification: " +  mStreamVolumeNotification
//                        + " volume: ring " +  mStreamVolumeRing
//                        + " volume: voicecall: " +  mStreamVolumeVoicecall
//                        + " volume: voicesystem: " +  mStreamVolumeSystem
//                        + " mode:  " +  mAudioMode
//        );

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

            NetworkInfo activeNetwork;

            for (Network network : networks) {
                activeNetwork = mConnectivityManager.getNetworkInfo(network);

                if (activeNetwork.getType()==ConnectivityManager.TYPE_WIFI){
                    mIsWifiAvailable = activeNetwork.isAvailable();
                    mIsWifiConnected = activeNetwork.isConnected();
                }

                else if (activeNetwork.getType()==ConnectivityManager.TYPE_MOBILE){
                    mIsMobileAvailable = activeNetwork.isAvailable();
                    mIsMobileConnected = activeNetwork.isConnected();
                }

            }

            if (mIsWifiConnected) {
                mNetworkType = NETWORK_TYPE_WIFI;
            }
            else if (mIsMobileConnected) {
                mNetworkType = NETWORK_TYPE_MOBILE;
            }

            mIsNetworkAvailable = mIsWifiAvailable | mIsMobileAvailable;
            mIsConnected = mIsWifiConnected | mIsMobileConnected;


            Log.d(LOG_TAG, "[test save records] connectivity change available? WIFI: available " + mIsWifiAvailable  +
                    "  mIsConnected: " + mIsWifiConnected + " Mobile: available: " + mIsMobileAvailable + " mIs connected: " + mIsMobileConnected
                    +" network type: " + mNetworkType + ",  mIs connected: " + mIsConnected + " mIs network available " + mIsNetworkAvailable);


        }

        else{

            Log.d(LOG_TAG, "[test save records] api under lollipop " );


            if (mConnectivityManager!=null) {

                NetworkInfo activeNetworkWifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo activeNetworkMobile = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                boolean isWiFi = activeNetworkWifi.getType() == ConnectivityManager.TYPE_WIFI;
                boolean isMobile = activeNetworkMobile.getType() == ConnectivityManager.TYPE_MOBILE;

                Log.d(LOG_TAG, "[test save records] connectivity change available? " + isWiFi);


                if(activeNetworkWifi !=null) {

                    mIsWifiConnected = activeNetworkWifi != null &&
                            activeNetworkWifi.isConnected();
                    mIsMobileConnected = activeNetworkWifi != null &&
                            activeNetworkMobile.isConnected();

                    mIsConnected = mIsWifiConnected | mIsMobileConnected;

                    mIsWifiAvailable = activeNetworkWifi.isAvailable();
                    mIsMobileAvailable = activeNetworkMobile.isAvailable();

                    mIsNetworkAvailable = mIsWifiAvailable | mIsMobileAvailable;


                    if (mIsWifiConnected) {
                        mNetworkType = NETWORK_TYPE_WIFI;
                    }

                    else if (mIsMobileConnected) {
                        mNetworkType = NETWORK_TYPE_MOBILE;
                    }


                    //assign value
//
                    Log.d(LOG_TAG, "[test save records] connectivity change available? WIFI: available " + mIsWifiAvailable  +
                            "  mIsConnected: " + mIsWifiConnected + " Mobile: available: " + mIsMobileAvailable + " mIs connected: " + mIsMobileConnected
                            +" network type: " + mNetworkType + ",  mIs connected: " + mIsConnected + " mIs network available " + mIsNetworkAvailable);

                }
            }

        }

        
    }


    /**
     *
     */
    private void loadAppAndPackage() {

        if (mAppPackageNameHmap==null){
            mAppPackageNameHmap = new HashMap<String, String>();
        }

        Resources res = mContext.getResources();

        String[] appNames = res.getStringArray(R.array.app);

        for (int i=0; i<appNames.length; i++){

            String app_package = appNames[i];

            String [] strs = app_package.split(":");

            String appName = strs[0];
            String packageName = strs[1];
            Log.d(LOG_TAG, "the app names are puting key: " + packageName + " value: " + appName);
            mAppPackageNameHmap.put(packageName, appName);

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
    
    public static void setCallState (int callState) {

        if (callState==TelephonyManager.CALL_STATE_IDLE)
            mCallState = CALL_STATE_IDLE;
        else if (callState==TelephonyManager.CALL_STATE_OFFHOOK)
            mCallState = CALL_STATE_OFFHOOK;
        else if (callState==TelephonyManager.CALL_STATE_RINGING)
            mCallState = CALL_STATE_RINGING;

    }

    public static String getCallState () {
        return mCallState;
    }

    public static void setNetworkOperatorName(String name) {
        mNetworkOperatorName = name;
    }


    public static String getNetworkOperatorName() {
        return mNetworkOperatorName;
    }

    public static void setPhoneType(int phoneType) {

        if (phoneType==TelephonyManager.PHONE_TYPE_CDMA)
            mPhoneSignalType = PHONE_SIGNAL_TYPE_CDMA;
        else if (phoneType==TelephonyManager.PHONE_TYPE_GSM)
            mPhoneSignalType = PHONE_SIGNAL_TYPE_GSM;
        else if (phoneType==TelephonyManager.PHONE_TYPE_NONE)
            mPhoneSignalType = PHONE_SIGNAL_TYPE_NONE;
        else if (phoneType==TelephonyManager.PHONE_TYPE_SIP)
            mPhoneSignalType = PHONE_SIGNAL_TYPE_SIP;
    }


    public static String getPhoneTypeName() {
        return mPhoneSignalType;
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


    public static String getScreenStatus() {
        return mScreenStatus;
    }


    public static String getLatestForegroundActivity() {
        return mLastestForegroundActivity;
    }

    public static String getLatestForegroundPackage() {
        return mLastestForegroundPackage;
    }

    public static String getLatestForegroundPackageTime() {
        return mLastestForegroundPackageTime;
    }

    public static String getRecentUsedAppsInLastHour() {
        return mRecentUsedAppsInLastHour;
    }


    public static boolean isGSM() {
        return mIsGSM;
    }

    public static void setGSM(boolean isGSM) {
        mIsGSM = isGSM;
    }

    /**Database table name should be defined by each ContextStateManager. So each CSM should overwrite this**/
    public static String getDatabaseTableNameBySourceName (String sourceName) {

        if (sourceName.equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_BATTERY))
            return RECORD_TABLE_NAME_BATTERY;
        else if (sourceName.equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE))
            return RECORD_TABLE_NAME_APPUSAGE;
        else if (sourceName.equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY))
            return RECORD_TABLE_NAME_CONNECTIVITY;
        else if (sourceName.equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY))
            return RECORD_TABLE_NAME_TELEPHONY;
        else if (sourceName.equals(STRING_CONTEXT_SOURCE_PHONE_STATUS_RINGER))
            return RECORD_TABLE_NAME_RINGER;
        else
            return null;

    }

    /**
     * this function should return a list of database table names for its contextsource. Must implement it
     * in order to create tables
     * @return
     */
    @Override
    public ArrayList<String> getAllDatabaseTableNames () {
        ArrayList<String> tablenames = new ArrayList<String>();

        tablenames.add(RECORD_TABLE_NAME_BATTERY);
        tablenames.add(RECORD_TABLE_NAME_APPUSAGE);
        tablenames.add(RECORD_TABLE_NAME_CONNECTIVITY);
        tablenames.add(RECORD_TABLE_NAME_RINGER);
        tablenames.add(RECORD_TABLE_NAME_TELEPHONY);

        return tablenames;
    }


    public static String getPhoneTypeName(int phoneType) {

        if (phoneType==TelephonyManager.PHONE_TYPE_CDMA)
            return PHONE_SIGNAL_TYPE_CDMA;
        else if (phoneType==TelephonyManager.PHONE_TYPE_GSM)
            return PHONE_SIGNAL_TYPE_GSM;
        else if (phoneType==TelephonyManager.PHONE_TYPE_NONE)
            return PHONE_SIGNAL_TYPE_NONE;
        else if (phoneType==TelephonyManager.PHONE_TYPE_SIP)
            return PHONE_SIGNAL_TYPE_SIP;
        else
            return ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    }



    public static int getContextSourceTypeFromName(String sourceName) {

        switch (sourceName){

            case STRING_CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE:
                return CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE;
            case STRING_CONTEXT_SOURCE_PHONE_STATUS_BATTERY:
                return CONTEXT_SOURCE_PHONE_STATUS_BATTERY;
            case STRING_CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY:
                return CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY;
            case STRING_CONTEXT_SOURCE_PHONE_STATUS_RINGER:
                return CONTEXT_SOURCE_PHONE_STATUS_RINGER;
            case STRING_CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY:
                return CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY;
            
            //the default is most probable activities
            default:
                return -1;
        }
    }

    public static String getContextSourceNameFromType(int sourceType) {

        switch (sourceType){

            case CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE:
                return STRING_CONTEXT_SOURCE_PHONE_STATUS_APPUSAGE;
            case CONTEXT_SOURCE_PHONE_STATUS_BATTERY:
                return STRING_CONTEXT_SOURCE_PHONE_STATUS_BATTERY;
            case CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY:
                return STRING_CONTEXT_SOURCE_PHONE_STATUS_CONNECTIVITY;
            case CONTEXT_SOURCE_PHONE_STATUS_RINGER:
                return STRING_CONTEXT_SOURCE_PHONE_STATUS_RINGER;
            case CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY:
                return STRING_CONTEXT_SOURCE_PHONE_STATUS_TELEPHONY;
            default:
                return null;

        }
    }

}
