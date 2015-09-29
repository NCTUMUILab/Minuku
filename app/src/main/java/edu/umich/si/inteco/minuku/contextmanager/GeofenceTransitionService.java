package edu.umich.si.inteco.minuku.contextmanager;

import android.R;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

import edu.umich.si.inteco.minuku.GlobalNames;
import edu.umich.si.inteco.minuku.model.Log.ProbeLog;
import edu.umich.si.inteco.minuku.util.GooglePlayServiceUtil;
import edu.umich.si.inteco.minuku.util.LogManager;

public class GeofenceTransitionService extends IntentService {

	/** Tag for logging. */
    private static final String LOG_TAG = "GeofenceTransitionService";
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

        // First check for errors
        if (LocationClient.hasError(intent)) {

            // Get the error code
            int errorCode = LocationClient.getErrorCode(intent);

            // Get the error message
            String errorMessage = GooglePlayServiceUtil.getErrorString(this, errorCode);

            Log.e(LOG_TAG, "[onHandleIntent] has error receiving Geofencing result");


        // If there's no error, get the transition type and create a notification
        } else {

            // Get the type of transition (entry or exit)
            int transition = LocationClient.getGeofenceTransition(intent);

            
            
            
            // if the 
            if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) 
            		|| (transition == Geofence.GEOFENCE_TRANSITION_EXIT)
            		|| (transition == Geofence.GEOFENCE_TRANSITION_DWELL) ) {

            	Log.d(LOG_TAG, "[Detected Geofence Trigger] Successfully receive Geofence transition " + getTransitionNameFromCode(transition));
                
                List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
                String[] geofenceResults = new String[geofences.size()];

                for (int index = 0; index < geofences.size() ; index++) {
                    geofenceResults[index] = geofences.get(index).toString();
                    Log.d(LOG_TAG, "[Detected Geofence Trigger] the triggering geofences are "  + geofenceResults[index]);
                    
                    //save the transition state, later, we will need to save to the contextExtractor
                    mLatestGeofenceTransition= 
                    		getTransitionNameFromCode(transition) + ":" + 
                    		GooglePlayServiceUtil.getIdFromGeofenceResult(geofences.get(index).toString()) + ":" + 
                    		GooglePlayServiceUtil.getLatLngRadiusFromGeofenceResult(geofences.get(index).toString());
                    
                    if (GlobalNames.isTestingActivity) {
                    	//sendNotification();
                    	
                    	//logging the activity information..
                    	
                    	ProbeLog geofenceLog = new ProbeLog(
                    			LogManager.LOG_TAG_GEO_FENCE,
                    			LogManager.LOG_TAG_GEO_FENCE,
                    			ContextExtractor.getCurrentTimeInMillis(),
                    			ContextExtractor.getCurrentTimeString(), 
                    			mLatestGeofenceTransition
                    			);
                    	
                    	LogManager.writeLogToFile(geofenceLog);
                    	
                    	
                    	//Toast.makeText(this , "the detected activity is "  + mMostProbableActivity + ": " +  mProbableActivities , Toast.LENGTH_LONG).show();
                    }
                }
                
                
            // An invalid transition was reported
            } else {
                // Always log as an error
            	Log.e(LOG_TAG, "[Detected Geofence Trigger] the triggering " + transition + " is invalid" );
                
            }
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
    

    
	private void sendNotification() {

        // Create a notification builder that's compatible with platforms >= version 4
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext());

        
        // Set the title, text, and icon
        builder.setContentTitle("geofence")
               .setContentText( mLatestGeofenceTransition)
               .setSmallIcon(R.drawable.ic_notification_overlay)
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
    
}
