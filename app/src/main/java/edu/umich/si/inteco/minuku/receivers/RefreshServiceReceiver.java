package edu.umich.si.inteco.minuku.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import edu.umich.si.inteco.minuku.GlobalNames;
import edu.umich.si.inteco.minuku.services.CaptureProbeService;
import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;

/**
 * Created by Armuro on 7/16/14.
 */
public class RefreshServiceReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "ScheduleManager";

    public void onReceive(Context context, Intent intent)
    {
        //write logs


        if (intent.getAction().equals(ScheduleAndSampleManager.UPDATE_SCHEDULE_ALARM)){
            Log.d(LOG_TAG, "In UpdateScheduleAlarmReceiver");

            Log.d(LOG_TAG, "[test reschedule][onReceive] the alarm receiver  with request code " + intent.getIntExtra(ScheduleAndSampleManager.ALARM_REQUEST_CODE, 0)
                    + "going to update actioncontrol's schedule");

            ScheduleAndSampleManager.updateScheduledActionControls();

            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                LogManager.LOG_TAG_ALARM_RECEIVED,
                "Alarm Received:\t" + ScheduleAndSampleManager.ALARM_TYPE_REFRESH + "\t" + "Update Schedule" + "\t" + intent.getIntExtra(ScheduleAndSampleManager.ALARM_REQUEST_CODE, 0));

            //this is old..the first method we came up with. During midnight we rescheule anything that needs to be a new schedule
            //but now we will stop service during midnight and restart in the morning

            /**
             *  TODO: we should let researchers to define whether they want to stop service during midnight,
             *  because we may be interested in some events happening midnight (drunk, sending late messages)
             *  For the labeling study we can just try
             */

        }else if (intent.getAction().equals(ScheduleAndSampleManager.START_SERVICE_ALARM)) {

            Log.d(LOG_TAG, "[UpdateScheduleAlarmReceiver ] we will start the service ");

            Log.d(LOG_TAG, "[test reschedule][onReceive] the alarm receiver  with request code " + intent.getIntExtra(ScheduleAndSampleManager.ALARM_REQUEST_CODE, 0)
                    + "going to start service");

            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ALARM_RECEIVED,
                    "Alarm Received:\t" + ScheduleAndSampleManager.ALARM_TYPE_REFRESH + "\t" + "Start Service" + "\t" + intent.getIntExtra(ScheduleAndSampleManager.ALARM_REQUEST_CODE, 0));

      //otherwise we start the service.
            Intent sintent = new Intent(context, CaptureProbeService.class);
            //		            sintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      //      context.startService(sintent);

        }else if (intent.getAction().equals(ScheduleAndSampleManager.STOP_SERVICE_ALARM)){

            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ALARM_RECEIVED,
                    "Alarm Received:\t" + ScheduleAndSampleManager.ALARM_TYPE_REFRESH + "\t" + "Stop Service" + "\t" + intent.getIntExtra(ScheduleAndSampleManager.ALARM_REQUEST_CODE, 0));

            Log.d(LOG_TAG, "[UpdateScheduleAlarmReceiver ] we will stop the service ");

            Intent stopintent = new Intent(context, CaptureProbeService.class);
     //       context.stopService(stopintent);

        }


    }
}
