package edu.umich.si.inteco.minuku.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import edu.umich.si.inteco.minuku.context.ContextStateManagers.PhoneSensorManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.PhoneStatusManager;
import edu.umich.si.inteco.minuku.services.MinukuMainService;
import edu.umich.si.inteco.minuku.util.BatteryHelper;

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

        PhoneStatusManager.setBatteryLevel(level);
        PhoneStatusManager.setBatteryPercentage(battPercentage);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        PhoneStatusManager.setIsCharging(isCharging);

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;


        if (!isCharging){
            PhoneStatusManager.setBatteryChargingState(BatteryHelper.NO_CHARGING);
        }
        else if (chargePlug==BatteryManager.BATTERY_PLUGGED_USB){
            PhoneStatusManager.setBatteryChargingState(BatteryHelper.USB_CHARGING);
        }else if (chargePlug==BatteryManager.BATTERY_PLUGGED_AC){
            PhoneStatusManager.setBatteryChargingState(BatteryHelper.AC_CHARGING);
        }

        /*** we control whether to start or stop the Probe service based on the Battery status **/
        //TODO: Kill Minuku service if the battery is low

        Log.d(LOG_TAG, "[BatteryStatusReceiver] now the phone is " + PhoneStatusManager.getBatteryChargingState() + " the battery percentage is " + BatteryHelper.getBatteryPercentage());

        //when the phone is charging, we should just have the service running, regardless of the battery status
        if (isCharging) {
            //keep the service running and start the service it if it is not running
            if (!MinukuMainService.isServiceRunning()){
                Log.d(LOG_TAG, "[BatteryStatusReceiver] the battery is charging, we can restart the service");
/*
                Intent sintent = new Intent(context, MinukuMainService.class);
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
                if (MinukuMainService.isServiceRunning()){
                    Log.d(LOG_TAG, "[BatteryStatusReceiver] the battery is not charging and the battery life is low, we now stop the service");
/*
                    Intent stopintent = new Intent(context, MinukuMainService.class);
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
