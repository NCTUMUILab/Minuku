package edu.umich.si.inteco.minuku.context;

import edu.umich.si.inteco.minuku.R;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofenceStatusCodes;

import java.util.ArrayList;
import java.util.List;

import edu.umich.si.inteco.minuku.util.GooglePlayServiceUtil;

public class GeofenceTransitionService extends IntentService {

	/** Tag for logging. */
    private static final String LOG_TAG = "GFTransService";
    private static String mLatestGeofenceTransition="Id:Transition:Radius";
    
    
	public GeofenceTransitionService() {
        super("GeofenceTransitionService");
    }
	
	public GeofenceTransitionService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {

        /*
        receive events from intent
         */
       GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        //check if the event contains error
        if (geofencingEvent.hasError()) {
            String errorMessage = getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(LOG_TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details for all triggered geofences as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);
            Log.i(LOG_TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(LOG_TAG, getString(R.string.geofence_transition_invalid_type,
                    geofenceTransition));
        }


	}
	
	/**
     * Map detected activity types to strings
     */
    public static String getTransitionNameFromCode(int transition) {
        switch(transition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "entered";
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "dwell";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "exited";           
            default:
                return "unknown";
        }
    }

    /**
     *
     * @param context
     * @param geofenceTransition
     * @param triggeringGeofences
     * @return
     */
    private String getGeofenceTransitionDetails ( Context context, int geofenceTransition, List<Geofence> triggeringGeofences){

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();

        //
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }


    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }


    private void sendNotification(String notificationDetails) {

        // Create a notification builder that's compatible with platforms >= version 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        
        // Set the title, text, and icon
        builder.setContentTitle(notificationDetails)
               .setContentText(getString(R.string.geofence_transition_notification_text))
               .setSmallIcon(R.drawable.ic_notification)
               // Get the Intent that starts the Location settings panel
               .setContentIntent(getContentIntent());

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(1, builder.build());

    }
   
    private PendingIntent getContentIntent() {

        // Set the Intent action to open Location Settings
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        // Create a PendingIntent to start an Activity
        return PendingIntent.getService(getApplicationContext(), GooglePlayServiceUtil.GEOFENCE_TRANSITION_PENDING_INTENT_REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }


    public  String getErrorString(Context context, int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return getString(R.string.geofence_not_available);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return getString(R.string.geofence_too_many_geofences);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return getString(R.string.geofence_too_many_pending_intents);
            default:
                return getString(R.string.unknown_geofence_error);
        }
    }

}
