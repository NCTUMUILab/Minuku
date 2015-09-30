package edu.umich.si.inteco.minuku.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.umich.si.inteco.minuku.contextmanager.TransportationModeDetector;

/**
 * Created by Armuro on 7/22/14.
 */
public class MobilityManager {

    public static int MOBILITY_REQUEST_CODE = 1;

    /** Tag for logging. */
    private static final String LOG_TAG = "MobilityManager";

    public static final int DELAY_LOCATION_UPDATE_WHEN_STATIC = 1;//the default delayed interval is 60 seconds
    public static final int REMOVE_LOCATION_UPDATE_WHEN_STATIC = 2;
    public static final int REMAIN_LOCATION_UPDATE_WHEN_STATIC = 0;

    public static final String ALARM_MOBILITY = "Mobility Change";

    //this parameter allows researchers to determine whether they want to pause location request or
    //slow down location request
    //
    public static int ADJUST_LOCATION_UPDATE_INTERVAL_WHEN_STATIC = 1;

    public static final String MOBILITY = "mobility";
    public static final String MOBILE_OUTDOORS = "mobile_outdoors";
    public static final String MOBILE_INDOORS = "mobile_indoors";
    public static final String STATIC_INDOORS = "static_indoors";
    public static final String STATIC_OUTDOORS = "static_outdoors";
    public static final String STATIC = "static";
    public static final String MOBILE = "mobile";

    public static final String UNKNOWN = "unkown";

    public static final String MOBILITY_CHANGE = "edu.umich.si.inteco.captureprobe.mobility";

    private static Context mContext;
    private static String mobility = "NA";
    private static String preMobility = "NA";

    public MobilityManager(Context context) {
        this.mContext = context;
    }

    //use transportation mode and location to determine the current mobility
    public static void updateMobility() {

        int transportation = TransportationModeDetector.getConfirmedActivityType();

        /*
        Log.d(LOG_TAG, "[updateMobility] [testmobility]the current transportation is " + TransportationModeDetector.getActivityNameFromType(transportation) +
                " and the current stat is " + TransportationModeDetector.getStateName(TransportationModeDetector.getCurrentState()) );
*/

        if (transportation == TransportationModeDetector.NO_ACTIVITY_TYPE &&
                TransportationModeDetector.getCurrentState()==TransportationModeDetector.STATE_STATIC) {

            ///the mobility is static....we will slow down the location request rate
            mobility = STATIC;
            //LocationManager.setLocationUpdateInterval(LocationManager.LOCATION_UPDATE_SLOW_INTERVAL_IN_SECONDS);

        }

        else {
            //TODO: make the rule more sophsticated
            mobility = MOBILE;
            ///the phone is or is probably moving....we will recover the location request rate
            //LocationManager.setLocationUpdateInterval(LocationManager.LOCATION_UPDATE_FAST_INTERVAL_IN_SECONDS);

        }

        if (!preMobility.equals(mobility)) {

            Log.d(LOG_TAG, "[updateMobility] mobility is changed" );

            //fire mobility change event
            Intent intent = new Intent();
            //add request code
            intent.putExtra(MOBILITY, mobility);
            intent.setAction(MOBILITY_CHANGE);
            mContext.sendBroadcast(intent);

        }

        preMobility = mobility;


    }

    public static String getMobility() {
        return mobility;
    }

    public static void setMobility(String mobility) {
        MobilityManager.mobility = mobility;
    }


}
