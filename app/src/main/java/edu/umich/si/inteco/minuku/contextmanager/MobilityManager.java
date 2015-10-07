package edu.umich.si.inteco.minuku.contextmanager;

import android.content.Context;
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
    public static int ADJUST_LOCATION_UPDATE_INTERVAL_WHEN_STATIC = DELAY_LOCATION_UPDATE_WHEN_STATIC ;
    public static final String STATIC = "static";
    public static final String MOBILE = "mobile";
    public static final String UNKNOWN = "unkown";

    private static Context mContext;
    private static String mobility = "NA";
    private static String preMobility = "NA";

    //even though mobility has not changed, if the phone stays in Static state for 5 cycles, we slow down the
    //location update frequency
    private static int sStaticCountDown = 5;

    public MobilityManager(Context context, ContextManager contextManager) {
        this.mContext = context;
        this.mContextManager = contextManager;
    }

    //use transportation mode and location to determine the current mobility
    public static void updateMobility() {

        //we get transportation mode from TransportationModeManager and then determine whether
        // we want to adjust the frequency of location updates.
        int transportation = TransportationModeManager.getConfirmedActivityType();

       Log.d(LOG_TAG, "[updateMobility] [testmobility]the current transportation is " + TransportationModeManager.getActivityNameFromType(transportation) +
                " and the current stat is " + TransportationModeManager.getStateName(TransportationModeManager.getCurrentState()) );

        if (transportation == TransportationModeManager.NO_ACTIVITY_TYPE &&
                TransportationModeManager.getCurrentState()== TransportationModeManager.STATE_STATIC) {

            ///the mobility is static....we will slow down the location request rate
            mobility = STATIC;

            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ALARM_RECEIVED,
                    "Alarm Received:\t" + MobilityManager.ALARM_MOBILITY +
                            "\t" + MobilityManager.STATIC + "\t" + "location update interval" +
                            LocationManager.getLocationUpdateIntervalInMillis());

            sStaticCountDown -=1;
        }

        /**the user is found to be mobile (moving), make location update frequency back to normal**/
        else {
            mobility = MOBILE;

            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ALARM_RECEIVED,
                    "Alarm Received:\t" + MobilityManager.ALARM_MOBILITY + "\t" +
                            MobilityManager.MOBILE + "\t" + "location interval" +
                            LocationManager.getLocationUpdateIntervalInMillis());
        }

        Log.d(LOG_TAG, "[updateMobility] sStaticCountDown: " + sStaticCountDown + " mobility: " + mobility);


        //we need to adjust the location update frequency when we see a different mobility
        if (!preMobility.equals("NA") && !preMobility.equals(mobility)) {

            if (mobility==STATIC){
                setStaticLocationUpdateFrequency();
            }
            else if (mobility==MOBILE) {

                //reset the Static countdown
                sStaticCountDown = 5;
                mContextManager.getLocationManager().setLocationUpdateInterval(LocationManager.UPDATE_INTERVAL_IN_MILLISECONDS);
            }
        }
        //even if the mobility remains the same, if being Static is long enough, we slow down location update
        else if (mobility==STATIC && sStaticCountDown==0){
            setStaticLocationUpdateFrequency();
        }


        preMobility = mobility;

    }

    private static void setStaticLocationUpdateFrequency() {

        //remain the original pace
        if (MobilityManager.ADJUST_LOCATION_UPDATE_INTERVAL_WHEN_STATIC
                == MobilityManager.REMAIN_LOCATION_UPDATE_WHEN_STATIC) {
            Log.d(LOG_TAG, "[updateMobility] we keep the original frequency");
        }

        //slow down location request if the mobility of the phone is found to be "Static"
        else if (MobilityManager.ADJUST_LOCATION_UPDATE_INTERVAL_WHEN_STATIC
                == MobilityManager.DELAY_LOCATION_UPDATE_WHEN_STATIC) {
            Log.d(LOG_TAG, "[updateMobility] we use a lower frequency");
            mContextManager.getLocationManager().setLocationUpdateInterval(LocationManager.SLOW_UPDATE_INTERVAL_IN_MILLISECONDS);

        }

        //remove location request if the mobility of the phone is found to be "Static"
        else if (MobilityManager.ADJUST_LOCATION_UPDATE_INTERVAL_WHEN_STATIC
                == MobilityManager.REMOVE_LOCATION_UPDATE_WHEN_STATIC) {
            Log.d(LOG_TAG, "[updateMobility] we remove location update");
            mContextManager.getLocationManager().removeLocationUpdate();

        }
        else {
            Log.d(LOG_TAG, "[updateMobility] I have no idea");
        }
    }

    public static String getMobility() {
        return mobility;
    }

    public static void setMobility(String mobility) {
        MobilityManager.mobility = mobility;
    }


}
