package edu.umich.si.inteco.minuku.receivers;

import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(LOG_TAG, "[ConnectivityChangeReceiver] connectivity change");

        ConnectivityManager conMngr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Network[] networks = conMngr.getAllNetworks();

            NetworkInfo activeNetwork = conMngr.getActiveNetworkInfo();

            for (Network network : networks) {
                activeNetwork = conMngr.getNetworkInfo(network);
                if (activeNetwork.getState().equals(NetworkInfo.State.CONNECTED)) {
//                    Log.d(LOG_TAG, "[ConnectivityChangeReceiver] connected");
                }
            }
        }

        else{

            if (conMngr!=null) {

                NetworkInfo[] info = conMngr.getAllNetworkInfo();
                NetworkInfo activeNetworkWifi = conMngr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo activeNetworkMobile = conMngr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                boolean isWiFi = activeNetworkWifi.getType() == ConnectivityManager.TYPE_WIFI;
                boolean isMobile = activeNetworkWifi.getType() == ConnectivityManager.TYPE_MOBILE;


                if (info != null) {

                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
//                            Log.d(LOG_TAG, "[ConnectivityChangeReceiver"+
//                                    " NETWORKNAME: " + anInfo.getTypeName());

                        }
                    }
                }

                if(activeNetworkWifi !=null) {

                    boolean isConnectedtoWifi = activeNetworkWifi != null &&
                            activeNetworkWifi.isConnected();
                    boolean isConnectedtoMobile = activeNetworkWifi != null &&
                            activeNetworkMobile.isConnected();



                    boolean isWifiAvailable = activeNetworkWifi.isAvailable();
                    boolean isMobileAvailable = activeNetworkMobile.isAvailable();

                    if (isWiFi) {

//                        Log.d(LOG_TAG, "[ConnectivityChangeReceiver] connect to wifi");

                        //if we only submit the data over wifh. this should be configurable
                        if (RemoteDBHelper.getSubmitDataOnlyOverWifi())
                            RemoteDBHelper.syncWithRemoteDatabase();

                    }

                    else if (isMobile) {

//                        Log.d(LOG_TAG, "[ConnectivityChangeReceiver] connect to mobile");
                    }


//                    Log.d(LOG_TAG, "[ConnectivityChangeReceiver] connectivity change available? WIFI: available " + isWifiAvailable  +
//                            "  isConnected: " + isConnectedtoWifi + " Mobile: available: " + isMobileAvailable + " is connected: " + isConnectedtoMobile);

                }
            }

        }

    }


}
