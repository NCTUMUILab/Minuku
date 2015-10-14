package edu.umich.si.inteco.minuku.contextmanager;

import android.R;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.util.GooglePlayServiceUtil;
import edu.umich.si.inteco.minuku.util.LogManager;

public class ActivityRecognitionService extends IntentService {

	/** Tag for logging. */
    private static final String LOG_TAG = "ActRecgService";

	private DetectedActivity mMostProbableActivity;

	private List<DetectedActivity> mProbableActivities;
	
	// Store the app's shared preferences repository
	private SharedPreferences mPrefs;
	
	public ActivityRecognitionService() {

		super("ActivityRecognitionService");
		mMostProbableActivity = null;
	}

    /*
    receive detected activities from the Google Play Service
     */
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
		// If the activity recognition intent contains an activity update, get the update
		if (ActivityRecognitionResult.hasResult(intent)) {

			try{
                //get the result from Google Play Service
				ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

                //store the returned list of probable acitvities (with confidence level)
                mProbableActivities = result.getProbableActivities();

                //store the returned most probable activity (with confidence level)
                mMostProbableActivity = result.getMostProbableActivity();
                Log.d(LOG_TAG, "[test ActivityRecognition] " +   mMostProbableActivity.toString());

                //Toast.makeText(this, "the detected activity is " + mMostProbableActivity + ": " + mProbableActivities, Toast.LENGTH_SHORT).show();

                /** save activity labels and detection time to ActivityRecognition Manager **/

                //we don't save this when we playback activity traces from file
                //instead, we let ContextManager to feed activity

                //after we get activity information from Google's Play service, we update the activity information
                //in ActitivityRecognition Manager
                ActivityRecognitionManager.setActivities(mProbableActivities, mMostProbableActivity);

                if (Constants.isTestingActivity) {
                	sendNotification();
                	
                	//logging the activity information..
                    String message = "";

                    for (int i=0; i<mProbableActivities.size(); i++){
                        message += ActivityRecognitionManager.getActivityNameFromType(mProbableActivities.get(i).getType()) + ":" + mProbableActivities.get(i).getConfidence();
                        if (i<mProbableActivities.size()-1){
                            message+= ";;";
                        }
                    }

                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            message);


                }
                	
                
			}catch (Exception e){
        		Log.e(LOG_TAG, "Got error when requesting activity ");
        	}
		}
	}

	private void sendNotification() {

        // Create a notification builder that's compatible with platforms >= version 4
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext());

        getContentIntent().cancel();

        String message = "";

        for (int i=0; i<mProbableActivities.size(); i++){
            message += ActivityRecognitionManager.getActivityNameFromType(mProbableActivities.get(i).getType()) + ":" + mProbableActivities.get(i).getConfidence();
            if (i<mProbableActivities.size()-1){
                message+= ";;";
            }
        }

        
        // Set the title, text, and icon
        builder.setContentTitle("activity recognition")
               .setContentText( message)
               .setSmallIcon(R.drawable.ic_notification_overlay)
               // Get the Intent that starts the Location settings panel
               .setContentIntent(getContentIntent());

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(9999, builder.build());
    }
	
	
	/**
     * Get a content Intent for the notification
     */
    private PendingIntent getContentIntent() {

        // Set the Intent action to open Location Settings
        Intent activityIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        // Create a PendingIntent to start an Activity
        return PendingIntent.getService(getApplicationContext(), GooglePlayServiceUtil.ACTIVITY_RECOGNITION_PENDING_INTENT_REQUEST_CODE, activityIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }
	
}
