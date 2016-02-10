package edu.umich.si.inteco.minuku.receivers;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.data.DataHandler;
import edu.umich.si.inteco.minuku.data.RemoteDBHelper;
import edu.umich.si.inteco.minuku.model.Schedule;
import edu.umich.si.inteco.minuku.util.RecordingAndAnnotateManager;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;

/**
 * Created by Armuro on 7/11/14.
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver{

    /** Tag for logging. */
    private static final String LOG_TAG = "ConnectivityChange";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(LOG_TAG, "[ConnectivityChangeReceiver] connectivity change");

        ConnectivityManager conMngr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = conMngr.getActiveNetworkInfo();

        if(activeNetwork !=null && activeNetwork.getType()==ConnectivityManager.TYPE_WIFI) {

            Log.d(LOG_TAG, "[ConnectivityChangeReceiver] connect to wifi");

            RemoteDBHelper.syncWithRemoteDatabase();

            //test getting background recording document from time 0
            ArrayList<JSONObject> documents = RecordingAndAnnotateManager.getBackgroundRecordingDocuments(0);
        }

        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.) {
            activeNetwork = conMngr.getActiveNetworkInfo();



        }

        else{
            activeNetwork = conMngr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (activeNetwork !=null && activeNetwork.isConnected()){
                Log.d(LOG_TAG, "[ConnectivityChangeReceiver] connected to wifi");

                //synchronize with the database

            }
        }
        */




    }


}
