package edu.umich.si.inteco.minuku.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Armuro on 7/16/14.
 */
public class PreferenceHelper {

    private static final String LOG_TAG = "PreferenceHelper";

    //preferndce name
    public static final String PACKAGE_NAME = "edu.umich.si.inteco.minuku";

    public static final String SHARED_PREFERENCE_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";
    public static final String DEVICE_ID = PACKAGE_NAME+ ".DEVICE_ID";
    public static final String SCHEDULE_REQUEST_CODE = PACKAGE_NAME+".REQUEST_CODE";
    public static final String DATABASE_LAST_SEVER_SYNC_TIME = PACKAGE_NAME+ ".LAST_SERVER_SYNC_TIME";

    /***
     * Shared preference for storing context related information
     */
    public static final String CONTEXT_GEOFENCE_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";


    private static Context mContext;

    public PreferenceHelper (Context context) {

        this.mContext = context;
    }

    public static void setPreferenceValue (String property, String value) {

        Log.d(LOG_TAG, "[setPreferenceValue] saving " + value + " to " + property);

        if (getPreference()!=null) {
            SharedPreferences.Editor editor = getPreference().edit();
            editor.putString(property,value);
            editor.commit();
        }

    }

    public static void setPreferenceValue (String property, boolean value) {

        Log.d(LOG_TAG, "[setPreferenceValue] saving " + value + " to " + property);

        if (getPreference()!=null) {
            SharedPreferences.Editor editor = getPreference().edit();
            editor.putBoolean(property,value);
            editor.commit();
        }

    }

    public static void setPreferenceValue (String property, long value) {

        Log.d(LOG_TAG, "[setPreferenceValue] saving " + value + " to " + property);

        if (getPreference()!=null) {
            SharedPreferences.Editor editor = getPreference().edit();
            editor.putLong(property,value);
            editor.commit();
        }

    }

    public static long getPreferenceLong (String property, long defaultValue) {

        Log.d(LOG_TAG, "[setPreferenceValue] getting values from " +  property);


        if (getPreference()!=null) {
            return  getPreference().getLong(property, defaultValue);
        }
        else
            return defaultValue;
    }

    public static String getPreferenceString (String property, String defaultValue) {

        Log.d(LOG_TAG, "[setPreferenceValue] getting values from " +  property);


        if (getPreference()!=null) {
            return  getPreference().getString(property, defaultValue);
        }
        else
            return defaultValue;
    }

    public static boolean getPreferenceBoolean (String property, boolean defaultValue) {

        if (getPreference()!=null) {
            return  getPreference().getBoolean(property,defaultValue);
        }
        else
            return defaultValue;
    }

    public static SharedPreferences getPreference() {

        if (mContext!=null) {
            return mContext.getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        }
        else {
            return null;
        }

    }

}
