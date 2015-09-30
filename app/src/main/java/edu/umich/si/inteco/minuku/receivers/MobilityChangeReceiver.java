package edu.umich.si.inteco.minuku.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import edu.umich.si.inteco.minuku.contextmanager.ContextExtractor;
import edu.umich.si.inteco.minuku.contextmanager.LocationManager;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.MobilityManager;

/**
 * Created by Armuro on 7/22/14.
 */
public class MobilityChangeReceiver extends BroadcastReceiver{

    /** Tag for logging. */
    private static final String LOG_TAG = "MobilityChangeReceiver";

    @Override
    public void onReceive(Context context, Intent mobilityIntent){

        Log.d(LOG_TAG, "[onReceive][testmobility] the mobility change to " + mobilityIntent.getStringExtra(MobilityManager.MOBILITY) );

        /**the user is found to be static **/
        if (mobilityIntent.getStringExtra(MobilityManager.MOBILITY).equals(MobilityManager.STATIC)) {

            /*
                determine whether we should remain, delay or cancel location udpate when the user is found static
            */

            //remain the original pace
            if (MobilityManager.ADJUST_LOCATION_UPDATE_INTERVAL_WHEN_STATIC
                == MobilityManager.REMAIN_LOCATION_UPDATE_WHEN_STATIC) {
                //we do nothing. the update frequency should remain the same
            }

            //slow down location request if the mobility of the phone is found to be "Static"
            else if (MobilityManager.ADJUST_LOCATION_UPDATE_INTERVAL_WHEN_STATIC
                    == MobilityManager.DELAY_LOCATION_UPDATE_WHEN_STATIC) {
                ContextExtractor.getLocationManager().setLocationUpdateInterval(LocationManager.SLOW_UPDATE_INTERVAL_IN_MILLISECONDS);
            }

            //remove location request if the mobility of the phone is found to be "Static"
            else if (MobilityManager.ADJUST_LOCATION_UPDATE_INTERVAL_WHEN_STATIC
                    == MobilityManager.REMOVE_LOCATION_UPDATE_WHEN_STATIC) {
                ContextExtractor.getLocationManager().removeLocationUpdate();
            }

            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ALARM_RECEIVED,
                    "Alarm Received:\t" + MobilityManager.ALARM_MOBILITY +
                            "\t" + MobilityManager.STATIC + "\t" + "location update interval" +
                            LocationManager.getLocationUpdateIntervalInMillis());

        }
        /**the user is found to be mobile (moving)**/
        else if (mobilityIntent.getStringExtra(MobilityManager.MOBILITY).equals(MobilityManager.MOBILE)) {

            //make location update frequency back to normal
            ContextExtractor.getLocationManager().setLocationUpdateInterval(LocationManager.UPDATE_INTERVAL_IN_MILLISECONDS);

            ContextExtractor.getLocationManager().requestLocationUpdate();

            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ALARM_RECEIVED,
                    "Alarm Received:\t" + MobilityManager.ALARM_MOBILITY + "\t" +
                            MobilityManager.MOBILE + "\t" + "location interval" +
                            LocationManager.getLocationUpdateIntervalInMillis());

        }
    }
}
