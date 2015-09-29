package edu.umich.si.inteco.minuku.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

import edu.umich.si.inteco.minuku.services.CaptureProbeService;
import edu.umich.si.inteco.minuku.util.BatteryHelper;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;

/**
 * Created by Armuro on 7/21/14.
 */
public class BatteryStatusReceiver extends BroadcastReceiver{

    private static final String LOG_TAG = "BatteryStatusReceiver ";


    @Override
    public void onReceive(Context context, Intent batteryStatus){

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float battPercentage = level/(float)scale;

        BatteryHelper.setBatteryLevel(level);
        BatteryHelper.setBatteryPercentage(battPercentage);


        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        BatteryHelper.setCharging(isCharging);

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;


        if (!isCharging){
            BatteryHelper.setBatteryChargingState(BatteryHelper.NO_CHARGING);
        }
        else if (chargePlug==BatteryManager.BATTERY_PLUGGED_USB){
            BatteryHelper.setBatteryChargingState(BatteryHelper.USB_CHARGING);
        }else if (chargePlug==BatteryManager.BATTERY_PLUGGED_AC){
            BatteryHelper.setBatteryChargingState(BatteryHelper.AC_CHARGING);
        }

        /*** we control whether to start or stop the Probe service based on the Battery status **/

        Log.d(LOG_TAG, "[BatteryStatusReceiver] now the phone is " + BatteryHelper.getBatteryChargingState() + " the battery percentage is " + BatteryHelper.getBatteryPercentage());

        //when the phone is charging, we should just have the service running, regardless of the battery status
        if (isCharging) {
            //keep the service running and start the service it if it is not running
            if (!CaptureProbeService.isServiceRunning()){
                Log.d(LOG_TAG, "[BatteryStatusReceiver] the battery is charging, we can restart the service");
/*
                Intent sintent = new Intent(context, CaptureProbeService.class);
                context.startService(sintent);

                Toast.makeText(context, " battery is charging: " + BatteryHelper.getBatteryChargingState() + ", start Probe service", Toast.LENGTH_LONG).show();

                LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                        LogManager.LOG_TAG_ALARM_RECEIVED,
                        "Alarm Received:\t" + ScheduleAndSampleManager.ALARM_TYPE_BATTERY + "\t" + "Start Service");
*/
            }

        }
        //we check the battery status
        else {

            if (battPercentage < BatteryHelper.BATTERY_LIFE_PERCENTAGE_THRESHOLD){

                //if the batterly percentage is lower than the threshould and the phone is not charging, we should stop the service
                if (CaptureProbeService.isServiceRunning()){
                    Log.d(LOG_TAG, "[BatteryStatusReceiver] the battery is not charging and the battery life is low, we now stop the service");
/*
                    Intent stopintent = new Intent(context, CaptureProbeService.class);
                    context.stopService(stopintent);

                    Toast.makeText(context, " battery level "  + battPercentage + " is too low, stop Probe service", Toast.LENGTH_LONG).show();

                    LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                            LogManager.LOG_TAG_ALARM_RECEIVED,
                            "Alarm Received:\t" + ScheduleAndSampleManager.ALARM_TYPE_BATTERY + "\t" + "Stop Service");
                            */
                }

            }

            else {

                //Toast.makeText(context, " battery level "  + battPercentage + " is still high, don't stop Probe service", Toast.LENGTH_LONG).show();

            }

        }



    }

}
