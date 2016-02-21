package edu.umich.si.inteco.minuku.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.activities.AnnotateActivity;
import edu.umich.si.inteco.minuku.activities.ListRecordingActivity;
import edu.umich.si.inteco.minuku.MainActivity;
import edu.umich.si.inteco.minuku.activities.QuestionnaireActivity;
import edu.umich.si.inteco.minuku.activities.RequestPermissionActivity;

public class NotificationHelper {

	private static final String LOG_TAG = "NotificationHelper";

	/**Constant Notification Request Code**/
	public static final int LOCATION_NOTIFICATION_PENDING_INTENT_REQUEST_CODE = 5;
	public static final int ACTIVITY_RECOGNITION_PENDING_INTENT_REQUEST_CODE = 3;
	public static final int GEOFENCE_TRANSITION_PENDING_INTENT_REQUEST_CODE = 4;

    public static final String NOTIFICATION_TYPE_NORMAL = "normal";
    public static final String NOTIFICATION_TYPE_ONGOING = "ongoing";

    public static final String NOTIFICATION_LAUNCH_WHEN_START_ACTION = "when_start";
    public static final String NOTIFICATION_LAUNCH_WHEN_STOP_ACTION = "when_stop";
    public static final String NOTIFICATION_LAUNCH_WHEN_PAUSE_ACTION = "when_pause";
    public static final String NOTIFICATION_LAUNCH_WHEN_RESUME_ACTION = "when_resume";
    public static final String NOTIFICATION_LAUNCH_WHEN_CANCEL_ACTION = "when_cancel";

    /** built in notification title and messages **/

    //for recoridng
    public static final String NOTIFICATION_TITLE_RECORDING = "Recording data";
    public static final String NOTIFICATION_MESSAGE_RECORDING_TAP_TO_ANNOTATE = "Tap to add information";

    //for permission
    public static final String NOTIFICATION_TITLE_ASK_FOR_PERMISSION = "Asking your permission";
    public static final String NOTIFICATION_MESSAGE_ASK_FOR_PERMISSION = "We need your permission to enable the Minuku service";
    public static final String REQUEST_PERMISSION_NAME = "Permission";
    public static final String REQUEST_PERMISSION_CODE = "Permission_code";


    //notification id
    public static final int NOTIFICATION_ID_QUESTIONNAIRE = 0;
    public static final int NOTIFICATION_ID_ANNOTATE = 1;
    public static final int NOTIFICATION_ID_LIST_RECORDING = 3;
    public static final int NOTIFICATION_ID_ONGOING_RECORDING_ANNOTATE_IN_PROCESS = 2;
    public static final int NOTIFICATION_ID_REQUEST_PERMISSION = 1002;
    public static final int NOTIFICATION_ID_TEST = 1001;


	private static Context mContext; 
	
	public static long time_base = 0;
	
	private static NotificationManager mNotificationManager;

    private static Uri defaultNotiAlarmSound;
    private static long[] defaultNotiPattern = {100,100,200,200,100,100,200,200};
	
	public NotificationHelper (Context context){
		
		mContext = context;
		mNotificationManager= (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		time_base = getCurrentTimeInMillis();

        defaultNotiAlarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	}

    /**
     *
     * @param context
     */
	public static void SetContext (Context context){
		mContext = context;
	}


    public static void cancelNotification(int notificationId){

        Log.d(LOG_TAG, "going to cancel notification " + notificationId);
        mNotificationManager.cancel(notificationId);

    }


    //TODO: not showing notifciation
    public static void createPermissionRequestNotificaiton(String permission, int requestCode, String title, String message) {

        Log.d(LOG_TAG, "[test permission] enter createPermissionRequestNotificaiton 1");

        Uri alarmSound =  defaultNotiAlarmSound;
        long[] pattern = defaultNotiPattern;

        Bundle bundle = new Bundle();

        //indicate which permission
        bundle.putString(REQUEST_PERMISSION_NAME, permission);
        bundle.putInt(REQUEST_PERMISSION_CODE, requestCode);

        Intent intent = new Intent(mContext, RequestPermissionActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Log.d(LOG_TAG, "[test permission] enter createPermissionRequestNotificaiton 2");

        PendingIntent pi =
                PendingIntent.getActivity(
                        mContext.getApplicationContext(),
                        generatePendingIntentRequestCode(9),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //create notfication for annotateActivity
        Notification noti=null;

        //depending on the type of the noti...we set the noti id
        int noti_id = NOTIFICATION_ID_REQUEST_PERMISSION;

        Log.d(LOG_TAG, "[test permission] enter createPermissionRequestNotificaiton 3 title " + title + " message " + message);

        // Create a notification builder that's compatible with platforms >= version 4
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext);

        // Set the title, text, and icon
        builder.setContentTitle("mobility")
                .setContentText( message)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        // Get the Intent that starts the Location settings panel
                .setContentIntent(pi);

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(9997, builder.build());


//        noti = new Notification.Builder(mContext)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setSmallIcon(R.drawable.ic_notification)
//                .setVibrate(pattern)
//                .setAutoCancel(true)
//                .setLights(Color.BLUE, 500, 500)
//                .setSound(alarmSound)
//                .setContentIntent(pi).build();
//
//        noti.flags |= Notification.FLAG_ONGOING_EVENT;
//        noti_id = NOTIFICATION_ID_REQUEST_PERMISSION;
//
//
//        // Build the notification and post it
//        mNotificationManager.notify(noti_id, noti);


    }


    public static void createShowRecordingListNotification(String reviewMode, String title, String message) {

        Log.d(LOG_TAG, "[createShowRecordingListNotification] 1");

        Uri alarmSound =  defaultNotiAlarmSound;
        long[] pattern = defaultNotiPattern;

        if (reviewMode.equals(RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_NONE)) {
            return ;
        }

        Bundle bundle = new Bundle();

        //indicate which session
        bundle.putString(ConfigurationManager.ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING, reviewMode);
        Intent intent = new Intent(mContext, ListRecordingActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pi =
                PendingIntent.getActivity(
                        mContext.getApplicationContext(),
                        generatePendingIntentRequestCode(getCurrentTimeInMillis()),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //create notfication for annotateActivity
        Notification noti=null;

        //depending on the type of the noti...we set the noti id
        int noti_id = NOTIFICATION_ID_LIST_RECORDING;

        noti = new Notification.Builder(mContext)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setVibrate(pattern)
                .setAutoCancel(true)
                .setLights(Color.BLUE, 500, 500)
                .setSound(alarmSound)
                .setContentIntent(pi).build();

        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        noti_id = NOTIFICATION_ID_ANNOTATE;


        LogManager.log(
                LogManager.LOG_TYPE_SYSTEM_LOG,
                LogManager.LOG_TAG_ANNOTATION_NOTI_GENERATED,
                LogManager.LOG_MESSAGE_ANNOTATION_NOTI_GENERATED + "\t" +  "list all recording"
        );

        // Build the notification and post it
        mNotificationManager.notify(noti_id, noti);

    }




    /**
     *
     * @param title
     * @param message
     */

    public static void createAnnotateNotification (int actionId, String title, String message, String type,
                                                   int sessionId, boolean startRecording, int annotateRecordingActionId) {

        Log.d(LOG_TAG, "createAnnotationNotification ready to create notification for annotating session Id " + sessionId + " user start recording " + startRecording
         + " annotateRecordingActionID " + annotateRecordingActionId);

        Uri alarmSound =  defaultNotiAlarmSound;
        long[] pattern = defaultNotiPattern;

        //create the intent for the annotattion activity
        Intent intent;
        //add questionnaire_id to the intent
        Bundle bundle  = new Bundle();
        /**check if the notification should direct users back to the RecordingACtivity or AnnotatingACtivity
         * depending on who initiates the recording**/

        //if the recoridng is initiated by user we should getback to the recordingActivity
        if (actionId==ActionManager.USER_INITIATED_RECORDING_ACTION_ID) {

            intent = new Intent(mContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            bundle.putString("launchTab", Constants.MAIN_ACTIVITY_TAB_RECORD);
        }

        //else if the recording is initiated by system, then the ongoing notification should allow user to annotate
        else {

            intent = new Intent(mContext, AnnotateActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }


        bundle.putInt(DatabaseNameManager.COL_SESSION_ID, sessionId);
        //indicate whether we should start a new recoridng
        bundle.putBoolean(ConfigurationManager.ACTION_PROPERTIES_RECORDING_STARTED_BY_USER, startRecording);
        bundle.putInt("annotateRecordingActionId", annotateRecordingActionId);
        intent.putExtras(bundle);

        PendingIntent pi =
                PendingIntent.getActivity(
                        mContext.getApplicationContext(),
                        generatePendingIntentRequestCode(getCurrentTimeInMillis()),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //create notfication for annotateActivity
        Notification noti=null;

        //depending on the type of the noti...we set the noti id
        int noti_id = NOTIFICATION_ID_ANNOTATE;

        //check if the notification is cancellable or is ongoing
        if (type.equals(NotificationHelper.NOTIFICATION_TYPE_NORMAL)){


            noti = new Notification.Builder(mContext)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setVibrate(pattern)
                    .setAutoCancel(true)
                    .setLights(Color.BLUE, 500, 500)
                    .setSound(alarmSound)
                    .setContentIntent(pi).build();
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
            noti_id = NOTIFICATION_ID_ANNOTATE;

            Log.d(LOG_TAG, "createAnnotationNotification: examineTransportation normal notification for the session " +sessionId + " is with notification " + noti_id );



        }
        else if (type.equals(NotificationHelper.NOTIFICATION_TYPE_ONGOING)){

            noti = new Notification.Builder(mContext)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pi).build();
            noti.flags |= Notification.FLAG_ONGOING_EVENT;
            noti_id = NOTIFICATION_ID_ONGOING_RECORDING_ANNOTATE_IN_PROCESS;

            //we need to set the notification id and set it to the recording session so that when
            //we stop the session, we can also cancel the ongoing notification
            RecordingAndAnnotateManager.setNoficiationIdToSession(noti_id, sessionId);

            Log.d(LOG_TAG, "createAnnotationNotification examineTransportation ongoing notification for the session " +sessionId + " is with notification " + noti_id );

        }

        LogManager.log(
                LogManager.LOG_TYPE_SYSTEM_LOG,
                LogManager.LOG_TAG_ANNOTATION_NOTI_GENERATED,
                LogManager.LOG_MESSAGE_ANNOTATION_NOTI_GENERATED + "\t" + " for session " + sessionId
        );

        // Build the notification and post it
        mNotificationManager.notify(noti_id, noti);

    }


    public static void createEmailQuestionnaireNotification(
            String title,
            String message,
            Intent intent) {

        Uri alarmSound =  defaultNotiAlarmSound;
        long[] pattern = defaultNotiPattern;

        Log.d(LOG_TAG, "createEmailQuestionnaireNotification creting the pi, the request code is " +  generatePendingIntentRequestCode(getCurrentTimeInMillis()) );

        PendingIntent pi =
                PendingIntent.getActivity(
                        mContext.getApplicationContext(),
                        generatePendingIntentRequestCode(getCurrentTimeInMillis()),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Create a notification builder that's compatible with platforms >= version 4
        Notification noti = new Notification.Builder(mContext)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setVibrate(pattern)
                .setAutoCancel(true)
                .setLights(Color.BLUE, 500, 500)
                .setSound(alarmSound)
                .setContentIntent(pi).build();


        LogManager.log(
                LogManager.LOG_TYPE_SYSTEM_LOG,
                LogManager.LOG_TAG_QUESTIONNAIRE_NOTI_GENERATED,
                LogManager.LOG_MESSAGE_EMAIL_QUESTIONNAIRE_NOTI_GENERATED
        );


        // Build the notification and post it
        mNotificationManager.notify(NOTIFICATION_ID_QUESTIONNAIRE, noti);


    }

    /**
     *
     * @param title
     * @param message
     * @param type
     * @param questionnaire_id
     */
	public static void createQuestionnaireNotification(String title, String message, String type, int questionnaire_id) {

		Log.d(LOG_TAG, "createQuestionnaireNotification ready to create notification for the questionnaire " + questionnaire_id);
		
		Uri alarmSound =  defaultNotiAlarmSound;
		long[] pattern = defaultNotiPattern;
		
        //create the intent for the questionnaire activity
		Intent intent = new Intent(mContext, QuestionnaireActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		
		
		//add questionnaire_id to the intent
		Bundle bundle  = new Bundle();
		bundle.putInt(QuestionnaireManager.QUESTIONNAIRE_PROPERTIES_ID, questionnaire_id);
		intent.putExtras(bundle);
		
		PendingIntent pi =
		        PendingIntent.getActivity(
		        mContext.getApplicationContext(),
		        generatePendingIntentRequestCode(getCurrentTimeInMillis()),
		        intent,
		        PendingIntent.FLAG_UPDATE_CURRENT
		);

		
		Log.d(LOG_TAG, "createQuestionnaireNotification creting the pi, the request code is " +  generatePendingIntentRequestCode(getCurrentTimeInMillis()) );
		

        // Create a notification builder that's compatible with platforms >= version 4
		Notification noti = new Notification.Builder(mContext)
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_notification)
        .setVibrate(pattern)
        .setAutoCancel(true)
        .setLights(Color.BLUE, 500, 500)
        .setSound(alarmSound)
        .setContentIntent(pi).build();


        //check if the notification is cancellable or is ongoing
        if (type.equals(NotificationHelper.NOTIFICATION_TYPE_NORMAL)){
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
        }
        else if (type.equals(NotificationHelper.NOTIFICATION_TYPE_ONGOING)){
            noti.flags |= Notification.FLAG_ONGOING_EVENT;
        }



        LogManager.log(
                LogManager.LOG_TYPE_SYSTEM_LOG,
                LogManager.LOG_TAG_QUESTIONNAIRE_NOTI_GENERATED,
                LogManager.LOG_MESSAGE_QUESTIONNAIRE_NOTI_GENERATED+ "\t" + " for questionnaire "+  questionnaire_id
        );

        // Build the notification and post it
        mNotificationManager.notify(NOTIFICATION_ID_QUESTIONNAIRE, noti);
        


    }

	
	/**get the current time in milliseconds**/
	public static long getCurrentTimeInMillis(){		
		//get timzone		
		TimeZone tz = TimeZone.getDefault();		
		Calendar cal = Calendar.getInstance(tz);
		long t = cal.getTimeInMillis();		
		return t;
	}
	
	/**get the current time in string (in the format of "yyyy-MM-dd HH:mm:ss" **/
	public static String getCurrentTimeString(){		
		//get timzone		
		TimeZone tz = TimeZone.getDefault();		
		Calendar cal = Calendar.getInstance(tz);
		
		SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
		String currentTimeString = sdf_now.format(cal.getTime());
		
		return currentTimeString;
	}
	
	/**
     * Get a content Intent for the notification
     */
    private static PendingIntent defaultPendingIntent() {

        // Set the Intent action to open Location Settings
        Intent defaultIntent = new Intent(mContext, MainActivity.class);

        // Create a PendingIntent to start an Activity
        return PendingIntent.getActivity(mContext.getApplicationContext(), 0, defaultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
	public static int generatePendingIntentRequestCode(long time){
		
		int code = 0;
				 
		if (time-time_base > 1000000000){
			time_base = getCurrentTimeInMillis();
		}
		
		return (int) (time-time_base);
	}
	
	public static NotificationManager getNotificationManager(){
		return mNotificationManager;
	}
	
}
