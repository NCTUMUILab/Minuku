package edu.umich.si.inteco.minuku.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import edu.umich.si.inteco.tansuo.app.GlobalNames;

/**
 * Created by Armuro on 7/16/14.
 */
public class PreferenceHelper {

    //preferndce name
    private static final String LOG_TAG = "PreferenceHelper";
    public static final String SHARED_PREFERENCE_NAME = "probe sharedpreference";
    public static final String SHARED_PREFERENCE_PROPERTY_DEVICE_ID = "device_id";
    public static final String SHARED_PREFERENCE_PROPERTY_REQUEST_CODE = "request_code";
    public static final String SHARED_PREFERENCE_PROPERTY_LAST_SEVER_SYNC_TIME = "last_server_sync_time";


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
