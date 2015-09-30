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

        if (mobilityIntent.getStringExtra(MobilityManager.MOBILITY).equals(MobilityManager.STATIC)) {

            //slow down locationUpdate rate
//            ContextExtractor.getLocationRequester().setLocationUpdateInterval(LocationManager.LOCATION_UPDATE_SLOW_INTERVAL_IN_SECONDS);

            //turn off location if is static
            ContextExtractor.getLocationRequester().removeUpdate();

            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ALARM_RECEIVED,
                    "Alarm Received:\t" + MobilityManager.ALARM_MOBILITY + "\t" + MobilityManager.STATIC + "\t" + "new location interval" + LocationManager.LOCATION_UPDATE_SLOW_INTERVAL_IN_SECONDS);

        }

        else if (mobilityIntent.getStringExtra(MobilityManager.MOBILITY).equals(MobilityManager.MOBILE)) {

            //slow down locationUpdate rate
  //          ContextExtractor.getLocationRequester().setLocationUpdateInterval(LocationManager.LOCATION_UPDATE_FAST_INTERVAL_IN_SECONDS);

            ContextExtractor.getLocationRequester().requestUpdates();
            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ALARM_RECEIVED,
                    "Alarm Received:\t" + MobilityManager.ALARM_MOBILITY + "\t" + MobilityManager.MOBILE + "\t" + "new location interval" + LocationManager.LOCATION_UPDATE_FAST_INTERVAL_IN_SECONDS);


        }


    }
}
