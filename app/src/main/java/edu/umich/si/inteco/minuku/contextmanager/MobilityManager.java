package edu.umich.si.inteco.minuku.contextmanager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import edu.umich.si.inteco.minuku.util.LogManager;

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

    private static ContextManager mContextManager;

    //this parameter allows researchers to determine whether they want to pause location request or
    //slow down location request
    //
    public static int ADJUST_LOCATION_UPDATE_INTERVAL_WHEN_STATIC = 1;
    public static final String STATIC = "static";
    public static final String MOBILE = "mobile";
    public static final String UNKNOWN = "unkown";

    private static Context mContext;
    private static String mobility = "NA";
    private static String preMobility = "NA";

    public MobilityManager(Context context, ContextManager contextManager) {
        this.mContext = context;
        this.mContextManager = contextManager;
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

            //remain the original pace
            if (MobilityManager.ADJUST_LOCATION_UPDATE_INTERVAL_WHEN_STATIC
                    == MobilityManager.REMAIN_LOCATION_UPDATE_WHEN_STATIC) {
                //we do nothing. the update frequency should remain the same
            }

            //slow down location request if the mobility of the phone is found to be "Static"
            else if (MobilityManager.ADJUST_LOCATION_UPDATE_INTERVAL_WHEN_STATIC
                    == MobilityManager.DELAY_LOCATION_UPDATE_WHEN_STATIC) {
                mContextManager.getLocationManager().setLocationUpdateInterval(LocationManager.SLOW_UPDATE_INTERVAL_IN_MILLISECONDS);
            }

            //remove location request if the mobility of the phone is found to be "Static"
            else if (MobilityManager.ADJUST_LOCATION_UPDATE_INTERVAL_WHEN_STATIC
                    == MobilityManager.REMOVE_LOCATION_UPDATE_WHEN_STATIC) {
                mContextManager.getLocationManager().removeLocationUpdate();
            }

            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ALARM_RECEIVED,
                    "Alarm Received:\t" + MobilityManager.ALARM_MOBILITY +
                            "\t" + MobilityManager.STATIC + "\t" + "location update interval" +
                            LocationManager.getLocationUpdateIntervalInMillis());
        }

        /**the user is found to be mobile (moving), make location update frequency back to normal**/
        else {
            mobility = MOBILE;

            mContextManager.getLocationManager().setLocationUpdateInterval(LocationManager.UPDATE_INTERVAL_IN_MILLISECONDS);

            mContextManager.getLocationManager().requestLocationUpdate();

            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ALARM_RECEIVED,
                    "Alarm Received:\t" + MobilityManager.ALARM_MOBILITY + "\t" +
                            MobilityManager.MOBILE + "\t" + "location interval" +
                            LocationManager.getLocationUpdateIntervalInMillis());
        }

        /*
        if (!preMobility.equals(mobility)) {

            Log.d(LOG_TAG, "[updateMobility] mobility is changed" );

            //fire mobility change event
            Intent intent = new Intent();
            //add request code
            intent.putExtra(MOBILITY, mobility);
            intent.setAction(MOBILITY_CHANGE);
            mContext.sendBroadcast(intent);

        }*/

        preMobility = mobility;


    }

    public static String getMobility() {
        return mobility;
    }

    public static void setMobility(String mobility) {
        MobilityManager.mobility = mobility;
    }


}
