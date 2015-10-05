package edu.umich.si.inteco.minuku.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import edu.umich.si.inteco.minuku.services.MinukuMainService;
import edu.umich.si.inteco.minuku.util.LogManager;

public class BootCompleteReceiver extends BroadcastReceiver {

	private static final String LOG_TAG = "BootCompleteReceiver";
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
		{
			
		     Log.d(LOG_TAG, "Successfully receive reboot request");
		     //here we start the service             
		     Intent sintent = new Intent(context, MinukuMainService.class);
//		     sintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		     context.startService(sintent);

            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ALARM_RECEIVED,
                    "Alarm Received:\t" + "BootComplete" + "\t" + "restart Service");
		 }

	}

}
