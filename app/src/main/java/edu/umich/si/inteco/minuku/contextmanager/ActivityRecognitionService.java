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

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import edu.umich.si.inteco.tansuo.app.GlobalNames;
import edu.umich.si.inteco.tansuo.app.util.GooglePlayServiceUtil;
import edu.umich.si.inteco.tansuo.app.util.LogManager;

public class ActivityRecognitionService extends IntentService {

	/** Tag for logging. */
    private static final String LOG_TAG = "ActivityRecognitionService";
    
	private static DetectedActivity mMostProbableActivity;
	
	private static List<DetectedActivity> mProbableActivities;
	
	// Store the app's shared preferences repository
	private SharedPreferences mPrefs;
	
	public ActivityRecognitionService() {

		super("ActivityRecognitionService");
		mMostProbableActivity = null;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

		Log.d(LOG_TAG, "onHandleIntent" );
		
		// If the activity recognition intent contains an activity update, get the update
		if (ActivityRecognitionResult.hasResult(intent)) {

			try{
				
				ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);


                for (int i=0; i<result.getProbableActivities().size();i++){
                	Log.d(LOG_TAG, "ActivityRecognitionResult: " +  result.getProbableActivities().get(i).toString());
                }
                
                mProbableActivities = result.getProbableActivities();        
                
                //store the activity and confidence into memory
                mMostProbableActivity = result.getMostProbableActivity();

                long detectionTime = result.getTime();
                //save the updated activity to ContextExtractor

       //         ContextExtractor.setProbableActivities(mProbableActivities, detectionTime);
       //         ContextExtractor.setMostProbableActivity(mMostProbableActivity, detectionTime);
                
                
                if (GlobalNames.isTestingActivity) {
                	//sendNotification();
                	
                	//logging the activity information..
                    String message = "";

                    for (int i=0; i<mProbableActivities.size(); i++){
                        message += getActivityNameFromType(mProbableActivities.get(i).getType()) + ":" + mProbableActivities.get(i).getConfidence();
                        if (i<mProbableActivities.size()-1){
                            message+= ";;";
                        }
                    }

                    LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                            message);

                    //sendNotification();

                	//Toast.makeText(this , "the detected activity is "  + mMostProbableActivity + ": " +  mProbableActivities , Toast.LENGTH_LONG).show();
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
            message += getActivityNameFromType(mProbableActivities.get(i).getType()) + ":" + mProbableActivities.get(i).getConfidence();
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
    

    
    
	public static DetectedActivity getCurActivity(){
		return mMostProbableActivity;
	}
    
	public static List<DetectedActivity> getProbableActivities(){
		return mProbableActivities;
	}

	
	/**
     * Map detected activity types to strings
     */
    public static String getActivityNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }


    public static int getActivityTypeFromName(String activityName) {

        if (activityName.equals("in_vehicle")) {
            return DetectedActivity.IN_VEHICLE;
        }else if(activityName.equals("on_bicycle")) {
            return DetectedActivity.ON_BICYCLE;
        }else if(activityName.equals("on_foot")) {
            return DetectedActivity.ON_FOOT;
        }else if(activityName.equals("still")) {
            return DetectedActivity.STILL;
        }else if(activityName.equals("unknown")) {
            return DetectedActivity.UNKNOWN ;
        }else if(activityName.equals("tilting")) {
            return DetectedActivity.TILTING;
        }else
            return DetectedActivity.UNKNOWN;
    }
	
}
