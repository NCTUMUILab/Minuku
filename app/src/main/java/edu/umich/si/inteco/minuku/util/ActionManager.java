package edu.umich.si.inteco.minuku.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.activities.AnnotateActivity;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.context.EventManager;
import edu.umich.si.inteco.minuku.data.DataHandler;
import edu.umich.si.inteco.minuku.data.LocalDBHelper;
import edu.umich.si.inteco.minuku.data.RemoteDBHelper;
import edu.umich.si.inteco.minuku.model.EmailQuestionnaireTemplate;
import edu.umich.si.inteco.minuku.model.Notification;
import edu.umich.si.inteco.minuku.model.ProbeObjectControl.ActionControl;
import edu.umich.si.inteco.minuku.model.Question;
import edu.umich.si.inteco.minuku.model.Questionnaire;
import edu.umich.si.inteco.minuku.model.Session;
import edu.umich.si.inteco.minuku.model.actions.Action;
import edu.umich.si.inteco.minuku.model.actions.AnnotateAction;
import edu.umich.si.inteco.minuku.model.actions.AnnotateRecordingAction;
import edu.umich.si.inteco.minuku.model.actions.GenerateEmailQuestionnaireAction;
import edu.umich.si.inteco.minuku.model.actions.GeneratingQuestionnaireAction;
import edu.umich.si.inteco.minuku.model.actions.MonitoringCircumstanceAction;
import edu.umich.si.inteco.minuku.model.actions.SavingRecordAction;

public class ActionManager {

	private static final String LOG_TAG = "ActionManager";
	
	private static Context mContext;
	private static ArrayList<Action> mActionList;
	private static ArrayList<ActionControl> mActionControlList;
	private static ArrayList<Action> mRunningActionList;
	private static LocalDBHelper mLocalDBHelper; 
	private static DataHandler mDataHandler;
	
	/**Even Monitor**/
	private static EventManager mEventManager;

    public static int RUNNING_ACTION_INITIAL_DELAY = 0;
    public static int RUNNING_ACTION_INTERVAL_IN_SECONDS = 5;


	/**Handle repeating recoridng**/
	/**Action id for user-initiated action**/
	public static final int USER_INITIATED_RECORDING_ACTION_ID = 1000;
    public static final int SYSTEM_INITIATED_RECORDING_ACTION_ID = 1001;
    public static final int USER_INITIATED_RESPONDING_TO_DAILY_REPORT = 1002;
	
	/***Action Type**/
	public static final String ACTION_TYPE_MONITORING_EVENTS = "monitoring_events";
	public static final String ACTION_TYPE_QUESTIONNAIRE = "questionnaire";
    public static final String ACTION_TYPE_EMAIL_QUESTIONNAIRE = "email_questionnaire";
	public static final String ACTION_TYPE_SAVING_RECORD = "saving_record";
	public static final String ACTION_TYPE_EXTERNAL_ACTION = "external_action";
    public static final String ACTION_TYPE_ANNOTATE_AND_RECORD = "annotate_and_record";
    public static final String ACTION_TYPE_ANNOTATE = "annotate";
	
	/*** Constants for the user recording interface ***/
	public static final String USER_START_RECORDING_ACTION_NAME = "user_start_recording";	
	public static final String USER_STOP_RECORDING_ACTION_NAME = "user_pause_recording";		
	public static final String USER_PAUSE_RECORDING_ACTION_NAME = "user_stop_recording";
    public static final String USER_RESPOND_TO_DAILY_REPORT_ACTION_NAME = "user_respond_to_daily_report";

    /*** Constants for the system recording for annotating ***/
    public static final String SYSTEM_GENERATED_RECORDING_BY_ANNOTATION_ACTION_NAME = "recording_generated_by_annotate_action";

	
	/**ACTION PROPERTIES**/
	public static final String ACTION_PROPERTIES_ID = "Id";
	public static final String ACTION_PROPERTIES_TYPE= "Type";
	public static final String ACTION_PROPERTIES_NAME= "Name";
	public static final String ACTION_PROPERTIES_EXECUTION_STYLE= "Execution_style"; 
	public static final String ACTION_PROPERTIES_CONTROL= "Control"; 
	public static final String ACTION_PROPERTIES_CONTINUITY= "Continuity";
	
	//within control
	public static final String ACTION_PROPERTIES_START  = "Start";
	public static final String ACTION_PROPERTIES_STOP = "Stop";
	public static final String ACTION_PROPERTIES_PAUSE = "Pause";
	public static final String ACTION_PROPERTIES_CANCEL = "Cancel";
	public static final String ACTION_PROPERTIES_LAUNCH= "Launch";
	public static final String ACTION_PROPERTIES_RESUME= "Resume";
	
	public static final int ACTION_CONTROL_TYPE_START  = 1;
	public static final int ACTION_CONTROL_TYPE_STOP = 2;
	public static final int ACTION_CONTROL_TYPE_PAUSE = 3;
	public static final int ACTION_CONTROL_TYPE_CANCEL = 4;
	public static final int ACTION_CONTROL_TYPE_RESUME = 5;
	
	//within control:trigger
	public static final String ACTION_PROPERTIES_TRIGGER= "Trigger";
	public static final String ACTION_TRIGGER_CLASS_PROPERTIES= "Class"; 
	public static final String ACTION_TRIGGER_PROPERTIES_SAMPLING_RATE= "Sampling_rate";
	
	//within control: schedule
	public static final String ACTION_PROPERTIES_SCHEDULE= "Schedule";
	public static final String ACTION_LAUNCH_STYLE_SCHEDULE= "schedule"; 
	public static final String ACTION_LAUNCH_STYLE_TRIGGERED= "triggered"; 
	public static final String ACTION_LAUNCH_STYLE_APP_START= "app_start";  
	
	public static final String ACTION_EXECUTION_STYLE_REPEATED= "repeated"; 
	public static final String ACTION_EXECUTION_STYLE_ONETIME= "one_time";

	//within content 
	public static final String ACTION_PROPERTIES_MONITORING_EVENTS= "Monitoring_events"; 
	public static final String ACTION_PROPERTIES_QUESTIONNAIRE_ID= "Questionnaire_id";
    public static final String ACTION_PROPERTIES_NOTIFICATION = "Notification";

    //for Annotate action
    public static final String ACTION_PROPERTIES_ANNOTATE= "Annotate";
    public static final String ACTION_PROPERTIES_ANNOTATE_MODE = "Mode";
    public static final String ACTION_PROPERTIES_ANNOTATE_RECORDING_TYPE = "Recording_type";
    public static final String ACTION_PROPERTIES_VIZUALIZATION_TYPE = "Viz_type";
    public static final String ACTION_PROPERTIES_ANNOTATE_ALLOW_ANNOTATE_IN_PROCESS = "Allow_annotate_in_process";
    public static final String ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING = "Review_recording";
    //recording needs user's permission
    public static final String ACTION_PROPERTIES_RECORDING_STARTED_BY_USER  = "Recording_started_by_user";

	//notificaiton property
	public static final String ACTION_PROPERTIES_NOTIFICATION_TITLE = "Title";
	public static final String ACTION_PROPERTIES_NOTIFICATION_MESSAGE = "Message";
    public static final String ACTION_PROPERTIES_NOTIFICATION_LAUNCH = "Launch";
    public static final String ACTION_PROPERTIES_NOTIFICATION_TYPE = "Type";

    /** ProbeObject Class**/
    public static final String ACTION_TRIGGER_CLASS_EVENT= "Circumstance";
    public static final String ACTION_TRIGGER_CLASS_ACTION_STOP= "Action.Stop";
    public static final String ACTION_TRIGGER_CLASS_ACTION_START= "Action.Start";
    public static final String ACTION_TRIGGER_CLASS_ACTION_PAUSE= "Action.Pause";
    public static final String ACTION_TRIGGER_CLASS_ACTION_RESUME= "Action.Resume";
    public static final String ACTION_TRIGGER_CLASS_ACTION_CANCEL= "Action.Cancel";
    public static final String ACTION_TRIGGER_CLASS_ACTIONCONTROL= "ActionControl";


	//continuity property
	public static final String ACTION_CONTINUITY_PROPERTIES_RATE = "Rate";
	public static final String ACTION_CONTINUITY_PROPERTIES_DURATION = "Duration";

    //for running the runningAction thread
    private static ScheduledExecutorService mRunningActionExecutor;

	public ActionManager(Context context){
		
		mContext = context;
		mActionList = new ArrayList<Action> ();
		mActionControlList = new ArrayList<ActionControl>();
		mRunningActionList = new ArrayList<Action> ();
		mLocalDBHelper = new LocalDBHelper(mContext, Constants.TEST_DATABASE_NAME);
		mDataHandler = new DataHandler (mContext);
        mRunningActionExecutor = Executors.newScheduledThreadPool(5);
		//registerActions();
	}
	
	
	
	/**
	 * Given the action id, find out the action instance and execute it by calling Execute (action)
	 * @param id
	 */
	public static void startAction(int id){
		
		Log.d(LOG_TAG, " [ActionManager Execute] the action id is  " + id +" find actions...");
		
		Action action =getAction(id);
		
		startAction(action);
		
	}



    /**
     * The function starts a thread to run background recording to save records.
     */
    public static void startRunningActionThread() {

        Runnable recordContextRunnable = new Runnable() {
            @Override
            public void run() {
                try{

                    Log.d(LOG_TAG, "[test pause resume]running in  recordContextRunnable  ");

                    for (int i=0; i < ActionManager.getRunningActionList().size(); i++){

                        Action action = ActionManager.getRunningActionList().get(i);

                        Log.d(LOG_TAG, "examineTransportation [test pause resume]running continuous actions: pause " + action.getId() + action.isPaused());
                        //if the action is not paused, run the action
                        if (!action.isPaused()){

                            Log.d(LOG_TAG, "examineTransportation [test pause resume]running continuous and non-paused actions" + action.getId());
                            ActionManager.executeAction(action);
                        }
                    }


                }catch (IllegalArgumentException e){
                    //Log.e(LOG_TAG, "Could not unregister receiver " + e.getMessage()+"");
                }
            }
        };

        mRunningActionExecutor.scheduleAtFixedRate(recordContextRunnable,
                RUNNING_ACTION_INITIAL_DELAY,
                RUNNING_ACTION_INTERVAL_IN_SECONDS,
                TimeUnit.SECONDS);


    }
	
	
	/***
	 * Get the input action and start it 
	 * @param action
	 */
	public static void startAction (Action action) {
		
		//if found the action, execute the action
		if (action!=null){

			//First check whether the action is continuous. If the action is continuous, instantiate the action and put the action into the runninActionList.
			//if the action is not continuous, then directly execute it. Usually a continuous action is either a monitoring action or a saving record action. 
			if (action.isContinuous()){
				
				
				//if the action is to monitor events
				if (action.getType().equals(ActionManager.ACTION_TYPE_MONITORING_EVENTS)){
			
					//instantiate a monitoring action first, and then put it into the runningAction list
					MonitoringCircumstanceAction monitoringAction = (MonitoringCircumstanceAction) action;
					ArrayList<Integer> evtIds = monitoringAction.getMonitoredCircumstanceIds();
					
					Log.d(LOG_TAG, " [ActionManager startAction] Start a new monitoring action" + monitoringAction.getId() + ", which monitor events  " + evtIds.toString());
					
					addRunningAction(monitoringAction);

                    //log
                    LogManager.log( LogManager.LOG_TYPE_SYSTEM_LOG,
                            LogManager.LOG_TAG_ACTION_START,
                            "Stop Action:\t" + monitoringAction.getType() + "\t" + monitoringAction.getId() + "\t" +monitoringAction.getName());
					
				}
				
				
				//if the action is to recording records  
				else if (action.getType().equals(ActionManager.ACTION_TYPE_SAVING_RECORD)){

					Log.d(LOG_TAG, "start saving record action ");
					
					SavingRecordAction savingRecordAction = (SavingRecordAction) action;
					
					//when start a new recorind action, we need to start a new session, and add the session id into the action
					//so that Probe knows where to store records associated with this action. 
					//So we need to first know how many sessions have been stored in the database
					int sessionId  = (int) mLocalDBHelper.querySessionCount()+1;

                    Log.d(LOG_TAG, " [ActionManager startAction] we start recording session " + sessionId);

                    //save the session info to the saveRecord action
					savingRecordAction.setSessionId(sessionId);
					
					//create a session and insert the session into the session table
					Session session = new Session(ContextManager.getCurrentTimeInMillis(), savingRecordAction.getTaskId());
                    session.setId(sessionId);

                    //if not specified otherwise, record all types of records (specify that the session record all types of records)
                    session.setRecordTypes(ContextManager.RECORD_TYPE_LIST);


                    //add session to the curRecordingSession
                    RecordingAndAnnotateManager.addCurRecordingSession(session);

					//Log.d(LOG_TAG, " [ActionManager startAction] we start recording session " + savingRecordAction.getSessionId());
                	
					
					
					//TODO: get a list of record to save and put it into the recording list. 

					addRunningAction(savingRecordAction);

                    //log
                    LogManager.log( LogManager.LOG_TYPE_SYSTEM_LOG,
                            LogManager.LOG_TAG_ACTION_START,
                            "Start Action:\t" + savingRecordAction.getType() + "\t" + savingRecordAction.getId()  + "\t" +savingRecordAction.getName());

					
				}
		
			}
			//directly execute it 
			else {
				
				executeAction(action);


                //log
                LogManager.log( LogManager.LOG_TYPE_SYSTEM_LOG,
                        LogManager.LOG_TAG_ACTION_START,
                        "Start Action:\t" + action.getType() + "\t" + action.getId() + "\t" +action.getName());
				
			}
			
			
		}
		
	}
	
	
	
	/**
	 * actually execute action  
	 * @param action
	 */
	public static void executeAction (Action action) {
		
		//if found the action, execute the action
		if (action!=null){
			
			//if the action is not currently paused, then we execute it.
			if (!action.isPaused()){
				
				Log.d(LOG_TAG, " [ActionManager Execute] going to execute the action.., the type of the action " + action.getId() + " is " + action.getType());
				
				/***decide action based on the action type**/

                action.addExecutionCount();

				//if the action is questionnaire..
				if (action.getType().equals(ActionManager.ACTION_TYPE_QUESTIONNAIRE)){
					
					GeneratingQuestionnaireAction a  = (GeneratingQuestionnaireAction) action; 
					
					//first create questionnaire object, then create notification. The reason is that we need to 
					//record the generated time of the questionnaire. 
					//The time when a user clicks on the notificaiton is defined as "attendedTime", different from the 
					//"generatedTime"
					
					//1. first we insert a questionnaire in the databse with generated time, and get the id of the questionnaire
					
					
					int study_id = action.getStudyId();
                    //know which template the questionnaire will use
					int questionnaireTemplateId = a.getQuestionnaireId();
                    //log the time when the questionnaire is generated
					long generatedTime = getCurrentTimeInMillis();
					
					//the questionnaire needs to remember which template it uses, which study it belongs, and generated time
					Questionnaire questionnaire = new Questionnaire (generatedTime, study_id, questionnaireTemplateId);				
					
					//we retrieve the number of the questionnaire that have been generated in the app
					int row = (int) mLocalDBHelper.insertQuestionnaireTable(questionnaire, DatabaseNameManager.QUESTIONNAIRE_TABLE_NAME);
					
					//we set the id of the questionnaire with the row number. 
					questionnaire.setId(row);
					QuestionnaireManager.addQuestionnaire(questionnaire);
			
					//the action has a notification
					if (action.hasNotification()){

                        //exectute all notifications that need to be executed
                        for (int i=0; i<action.getNotifications().size(); i++){

                            Notification noti = action.getNotifications().get(i);

                            NotificationHelper.createQuestionnaireNotification(
                                    noti.getTitle(),
                                    noti.getMessage(),
                                    noti.getType(),
                                    questionnaire.getId()
                            );
                        }
					}

				}

                else if (action.getType().equals(ActionManager.ACTION_TYPE_EMAIL_QUESTIONNAIRE)){

                    GenerateEmailQuestionnaireAction generateEmailQuestionnaireAction  = (GenerateEmailQuestionnaireAction) action;

                    //get the template of the questionnaire
                    int questionnaireTemplateId = generateEmailQuestionnaireAction.getQuestionnaireId();

                    Log.d(LOG_TAG,"[execute EmailQuestionnaire Action]  the email questionnaire id is " + questionnaireTemplateId);



                    //create an intent for starting the email composing activity





                    /** 1. produce email compose window  **/
                    if (QuestionnaireManager.QUESTIONNAIRE_SENT_FROM_SOURCE.equals(QuestionnaireManager.QUESTIONNAIRE_FROM_CLIENT)) {


                        Intent intent = generateEmailQuestionnaireIntent(questionnaireTemplateId);

                        //if the action has a notification, we send the notification to start composing
                        if (action.hasNotification()){

                            // Log.d(LOG_TAG,"[execute EmailQuestionnaire Action] the user starts the daily journal from notification");
                            //exectute all notifications that need to be executed
                            for (int i=0; i<action.getNotifications().size(); i++){

                                Notification noti = action.getNotifications().get(i);

                                NotificationHelper.createEmailQuestionnaireNotification(
                                        noti.getTitle(),
                                        noti.getMessage(),
                                        intent
                                );
                            }

                        }
                        //otherwise we just start the email composing window
                        else {

                            // Log.d(LOG_TAG,"[execute EmailQuestionnaire Action]  the user clicks on the button, we perform the daily journal directly");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }

                    }
                    /** 2. request server to send email **/
                    else if (QuestionnaireManager.QUESTIONNAIRE_SENT_FROM_SOURCE.equals(QuestionnaireManager.QUESTIONNAIRE_FROM_SERVER)) {

                        EmailQuestionnaireTemplate template = (EmailQuestionnaireTemplate) QuestionnaireManager.getQuestionnaireTemplate(questionnaireTemplateId);
                        JSONArray trips  = new JSONArray();
                        String emailBody="";
                        //we get the dynamic content
                        if (template!=null) {
                            //Log.d(LOG_TAG, "[getEmailQuestionnaireContent] we use the template " + questionnaireTemplateId + " the title is " + title);
                            ArrayList<Question> questions = template.getQuestions();

                            for (int i=0; i<questions.size(); i++) {

                                Question qu = questions.get(i);

                                String questionText = qu.getText();


                                if (qu.getDataJSON()!=null){

                                   // emailBody += questionText+"\n\n<br /><br />";
                                    trips = QuestionnaireManager.createDataJSONContent(qu.getDataJSON());
                                 //   emailBody += dataStr + "\n\n\n<br />";

                                }


                            }

                            Log.d(LOG_TAG, "emailBody:" + trips);

                        }

                        Log.d(LOG_TAG,"[execute EmailQuestionnaire Action] emailBody " + trips);
                        Log.d(LOG_TAG,"[execute EmailQuestionnaire Action] deviceId " + Constants.DEVICE_ID);
                        Log.d(LOG_TAG,"[execute EmailQuestionnaire Action] Study condition " + Constants.CURRENT_STUDY_CONDITION);
                        //we need to tell the server which condition it is in, email subject, email content. device id
                        String emailSubject = QuestionnaireManager.getEmailQuestionnaireSubject(questionnaireTemplateId);
                        Log.d(LOG_TAG,"[execute EmailQuestionnaire Action] emailSubject " + emailSubject);


                        JSONObject requestEmailJSON = new JSONObject();
                        try {

                            requestEmailJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_EMAIL_SUBJECT, emailSubject);
                            requestEmailJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_TRIPS, trips);
                            requestEmailJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_DEVICE_ID, Constants.DEVICE_ID);
                            requestEmailJSON.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_STUDY_CONDITION, Constants.CURRENT_STUDY_CONDITION);

                            RemoteDBHelper.requestEmailFromServer(requestEmailJSON);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }




                }


				/** if the action is to monitor events, ContextMAnager will inform ContextStateManagers
                 * of relevance to convert raw data into states. **/
				else if (action.getType().equals(ActionManager.ACTION_TYPE_MONITORING_EVENTS)){
			
					MonitoringCircumstanceAction a = (MonitoringCircumstanceAction) action;
					
					ArrayList<Integer> ids = a.getMonitoredCircumstanceIds();
					
					//Log.d(LOG_TAG, " [ActionManager Execute] Ready to execute monitoring action" + a.getId() + ", which monitor events  " + evt_ids.toString());
					

					//EventManager.examineEventConditions(evt_ids);
					
				}
				
				
				/** if the action is to recording records, we save records in the public record pool in the local SQLLite database  **/
				else if (action.getType().equals(ActionManager.ACTION_TYPE_SAVING_RECORD)){

				    Log.d(LOG_TAG, "[participatory sensing] execute saving record action, this is the " + action.getExecutionCount() + "th time of this action");

					SavingRecordAction savingRecordAction = (SavingRecordAction) action;
					
					//TODO: get a list of record to save and put it into the recording list. 

					//save records in the record pool
					mDataHandler.SaveRecordsToLocalDatabase(ContextManager.getRecordPool(), savingRecordAction.getSessionId() );

                    //check if the recording allows users to add annotation in process. If yes, we add an
                    //"ongoing notification" to indicate that there is an ongoing recording


                    if (savingRecordAction.getExecutionCount()<2 && savingRecordAction.isAllowAnnotationInProcess()){

                        NotificationHelper.createAnnotateNotification(
                                savingRecordAction.getId(),
                                NotificationHelper.NOTIFICATION_TITLE_RECORDING,
                                NotificationHelper.NOTIFICATION_MESSAGE_RECORDING_TAP_TO_ANNOTATE,
                                NotificationHelper.NOTIFICATION_TYPE_ONGOING,
                                savingRecordAction.getSessionId(),
                                false,
                                -1
                        );


                    }

					
				}


                else if (action.getType().equals(ActionManager.ACTION_TYPE_ANNOTATE)) {


                    AnnotateAction annotateAction = (AnnotateAction) action;
                    String reviewMode = annotateAction.getReviewRecordingMode();


                    if (annotateAction.getMode().equals(RecordingAndAnnotateManager.ANNOTATE_MODE_MANUAL)){
                        //if we will review a list of recording..


                        if (annotateAction.hasNotification()) {
                            for (int i=0; i<annotateAction.getNotifications().size(); i++){

                                Notification noti = annotateAction.getNotifications().get(i);

                                //see what notification needs to be fired when the action is started
                                if (noti.getLaunch().equals(NotificationHelper.NOTIFICATION_LAUNCH_WHEN_START_ACTION) ){

                                    NotificationHelper.createShowRecordingListNotification(
                                            reviewMode,
                                            noti.getTitle(),
                                            noti.getMessage());

                                }
                            }


                        }


                      //  RecordingAndAnnotateManager.startListRecordingActivity(reviewMode);
                    }


                    //if auto, the system will annotate the recordings
                    else if (annotateAction.getMode().equals(RecordingAndAnnotateManager.ANNOTATE_MODE_AUTO)) {


                    }

                }


                /** Annotate Action ***/
                else if (action.getType().equals(ActionManager.ACTION_TYPE_ANNOTATE_AND_RECORD)){


                    AnnotateRecordingAction annotateRecordingAction= (AnnotateRecordingAction) action;
                    int sessionId = 0;

                    Log.d(LOG_TAG, " [ActionManager Execute] the annotateRecording action is "  + action.getName() + " we need the recording to start by user ? "
                            + annotateRecordingAction.isRecordingStartByUser());

                    //check the recording type to determine whether we need to start a new recording

                    if (annotateRecordingAction.getRecordingType().equals(RecordingAndAnnotateManager.ANNOTATE_RECORDING_NEW)){

                        //1. if the annotation is for a new recording, then we start a new SavingRecordAction. but we need to check whether the recording
                        //should be started automatically or after the user clicks on notification

                        //the system automatically starts a new session, regardless of whether there's a notification
                        if (!annotateRecordingAction.isRecordingStartByUser()){

                           sessionId = createSavingRecordAction(annotateRecordingAction.getId());

                        }


                    }
                    else if (annotateRecordingAction.getRecordingType().equals(RecordingAndAnnotateManager.ANNOTATE_RECORDING_BACKGROUND)){

                        //if the annotation is for the background recording, then the session number is 1, and we don't need to start a new recording
                        sessionId = (Constants.BACKGOUND_RECORDING_SESSION_ID);

                        //TODO: if annotating background recording, we need to specify the startTime and endTime of the annotation


                    }


                    /** check annotate mode, if the mode is manual, we need to lead the user to the annotate activity through notification  **/

                    if (annotateRecordingAction.getMode().equals(RecordingAndAnnotateManager.ANNOTATE_MODE_MANUAL)){

                        //if the mode is manual, bring users to the annotateActivity. The annotation created in the annotateActivity by default is
                        //applied to the entire session.
                        //2. then we start the annotateActivity to let users manually annotate recording

                        //the action has a notification
                        if (annotateRecordingAction.hasNotification()){

                            //exectute all notifications that need to be executed
                            for (int i=0; i<action.getNotifications().size(); i++){

                                Notification noti = action.getNotifications().get(i);

                                //see what notification needs to be fired when the action is started
                                if (noti.getLaunch().equals(NotificationHelper.NOTIFICATION_LAUNCH_WHEN_START_ACTION) ){

                                    NotificationHelper.createAnnotateNotification(
                                            action.getId(),
                                            noti.getTitle(),
                                            noti.getMessage(),
                                            noti.getType(),
                                            sessionId,
                                            annotateRecordingAction.isRecordingStartByUser(),
                                            annotateRecordingAction.getId()
                                    );
                                }
                            }


                        }
                        //if there's no notification
                        else {

                            //directly bring users to the annotating process
                            Log.d(LOG_TAG, " start by useR? " +annotateRecordingAction.isRecordingStartByUser());
                            startAnnotateActivity(sessionId, annotateRecordingAction.isRecordingStartByUser(), annotateRecordingAction.getId(), annotateRecordingAction.getReviewRecordingMode());

                        }


                    }else if (annotateRecordingAction.getMode().equals(RecordingAndAnnotateManager.ANNOTATE_MODE_AUTO)) {

                        //automatically add annotations to the recording


                    }




                }

                //not record continuous action
                if (!action.isContinuous())
				//log the action
				LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                        LogManager.LOG_TAG_ACTION_EXECUTION,
                        "Execute Action:\t" + action.getId() + "\t" +action.getName()
                );
				
			}//if not paused

		}// (if action!=null)
        else {

            Log.e(LOG_TAG, "the action is null");

        }
		
	}


    /**
     * Create savingRecording action for an annotateRecordingAction
     * @param annotateRecordingActionId
     * @return
     */
    public static int createSavingRecordAction(int annotateRecordingActionId) {

        int sessionId=0;

        //when clicking on the recording action, execute the saveRecordAction
        SavingRecordAction savingRecordAction = new SavingRecordAction(

                //the recording is initiated by the system (for annotating)
                ActionManager.SYSTEM_INITIATED_RECORDING_ACTION_ID + annotateRecordingActionId,
                ActionManager.SYSTEM_GENERATED_RECORDING_BY_ANNOTATION_ACTION_NAME,
                ActionManager.ACTION_TYPE_SAVING_RECORD,
                ActionManager.ACTION_EXECUTION_STYLE_ONETIME, Constants.LABELING_STUDY_ID
        );

        //recording is a continuous action.
        savingRecordAction.setContinuous(true);

        AnnotateRecordingAction annotateRecordingAction = (AnnotateRecordingAction) ActionManager.getAction(annotateRecordingActionId);

        //if the annotateRecordingAction allows annotation in process, we also set that in the savingRecordAction
        savingRecordAction.setAllowAnnotationInProcess(annotateRecordingAction.isAllowAnnotationInProcess());

        //associate the savingRecordAction with the annotate action
        annotateRecordingAction.setAssociatedSavingRecordAction(savingRecordAction);

        //get the session number and assign it to the session Id
        sessionId  = (int) mLocalDBHelper.querySessionCount() + 1;
        savingRecordAction.setSessionId(sessionId);


        //create a session and save it
        Session session = new Session(ContextManager.getCurrentTimeInMillis(), savingRecordAction.getTaskId());
        session.setId(sessionId);
        RecordingAndAnnotateManager.addCurRecordingSession(session);


        Log.d(LOG_TAG, " [ActionManager executeAction if ACTION_TYPE_ANNOTATE_RECORDING] we start session " + savingRecordAction.getSessionId());

        //add the savingRecord to the runningaction list to execute the savingRecordAction
        addRunningAction(savingRecordAction);


        //log the stop Action
        LogManager.log( LogManager.LOG_TYPE_SYSTEM_LOG,
                LogManager.LOG_TAG_ACTION_GENERATION,
                "Generate Action:"  + "\t"  + savingRecordAction.getType()  + "\t"  + savingRecordAction.getId()  + "\t"  +savingRecordAction.getName());

        return sessionId;
    }
	
	/**
	 * 
	 * @param action
	 */
	public static void stopAction (Action action) {

        if (action!=null) {

            Log.d(LOG_TAG, "[stopAction] going to stop Action " + action.getId() + " " + action.getName() + " " +action.getType() + " action is continuous? " + action.isContinuous());

            //check if the action is a continuous action. If the action is continuous, just remove it from the running action list
            if (action.isContinuous()){
                Log.d(LOG_TAG, "[stopAction] going to remove Action " + action.getId() + " " + action.getName() + " " +action.getType() +" from the running action");
                getRunningActionList().remove(action);
            }

            //if the action is saving record, we need to add the endtime of the recording
            if (action.getType().equals(ActionManager.ACTION_TYPE_SAVING_RECORD)){

                SavingRecordAction savingRecordAction = (SavingRecordAction) action;
                int sessionId = savingRecordAction.getSessionId();
                Log.d(LOG_TAG,"[stopAction] examineTransportation the recording session " + sessionId + " has to be stoped");
                Session session = RecordingAndAnnotateManager.getCurRecordingSession(sessionId);

                //the session does exist (sometimes we stop the savingRecord that has never existed)
                if (session!=null) {

                    //add EndTime to the session
                    long endTime = ContextManager.getCurrentTimeInMillis();
                    mLocalDBHelper.updateSessionEndTime(savingRecordAction.getSessionId(), DatabaseNameManager.SESSION_TABLE_NAME, endTime);
                    Log.d(LOG_TAG,"[stopAction] examineTransportation stop saving record, the " + session.getId() + " endtime is " + ScheduleAndSampleManager.getTimeString(endTime));
                    //remove the session from the curRuningSession
                    RecordingAndAnnotateManager.getCurRecordingSessions().remove(session);
                    NotificationHelper.cancelNotification(NotificationHelper.NOTIFICATION_ID_ONGOING_RECORDING_ANNOTATE_IN_PROCESS);
                }

            }

            else if (action.getType().equals(ActionManager.ACTION_TYPE_ANNOTATE)) {


            }

            //if the action is AnnotateRecording. we need to check its associated continuous action.
            else if (action.getType().equals(ActionManager.ACTION_TYPE_ANNOTATE_AND_RECORD)) {

                AnnotateRecordingAction annotateRecordingAction = (AnnotateRecordingAction) action;

                //get it's asscoaited recording action
                SavingRecordAction savingRecordAction = annotateRecordingAction.getAssociatedSavingRecordAction();
                Log.d(LOG_TAG, "[stopAction]savingRecordAction " + savingRecordAction);

                //if the annotateRecordingAction has created an associated savingRecordingAction..
                //we need to stop the savingrecord action and see if there's a notification we need to generate
                if (savingRecordAction!=null) {
                    Log.d(LOG_TAG, "[stopAction]stop the action of saving session  " + savingRecordAction.getSessionId());
                    stopAction(savingRecordAction);


                    /**check if we need to generate annotate notification when we stop it */
                    if (annotateRecordingAction.hasNotification()){

                        //exectute all notifications that need to be executed
                        for (int i=0; i<annotateRecordingAction.getNotifications().size(); i++){

                            Notification noti = annotateRecordingAction.getNotifications().get(i);

                            //If there is a notification to be generated when the action is stopped
                            if (noti.getLaunch().equals(NotificationHelper.NOTIFICATION_LAUNCH_WHEN_STOP_ACTION)) {

                                NotificationHelper.createAnnotateNotification(
                                        action.getId(),
                                        noti.getTitle(),
                                        noti.getMessage(),
                                        noti.getType(),
                                        savingRecordAction.getSessionId(),
                                        annotateRecordingAction.isRecordingStartByUser(),
                                        action.getId()
                                );
                            }


                            //cancel the previous notifications that is generated when start, if they have not been dismissed by users
                            else if (noti.getLaunch().equals(NotificationHelper.NOTIFICATION_LAUNCH_WHEN_START_ACTION)) {

                                Log.d(LOG_TAG, "[stopAction] the action has start notification, cancel it");
                                NotificationHelper.cancelNotification(NotificationHelper.NOTIFICATION_ID_ANNOTATE);

                            }
                        }


                    }

                }

            }

            //log the stop Action
            LogManager.log( LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ACTION_EXECUTION,
                    "Stop Action:" + "\t"  + action.getType() + "\t"  + action.getId() + "\t"  +action.getName());

        }
        else {

            Log.e(LOG_TAG, "the action is null");

        }


	}
	
	
	/**
	 * 
	 * @param action
	 */
	public static void pauseAction (Action action) {

        if (action!=null) {

            Log.d(LOG_TAG, " [pauseAction] [test pause resume] Going to pause the action " + action.getId());

            //we just need to set the pause flag true, and the system will not execute them.
            action.setPaused(true);


            //if the action is for saving records, we also need to mark the curRunningSession paused so that
            //the recordPool will not wait for this paused session.
            if (action.getType().equals(ActionManager.ACTION_TYPE_SAVING_RECORD)){

                SavingRecordAction savingRecordAction = (SavingRecordAction) action;
                int sessionId = savingRecordAction.getSessionId();
                Session session = RecordingAndAnnotateManager.getCurRecordingSession(sessionId);
                session.setPaused(true);

                Log.d(LOG_TAG, " [pauseAction] pause the session " + session.getId() + " pause is " + session.isPaused()  );

            }

            //log
            LogManager.log( LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ACTION_PAUSE,
                    "Pause Action:" + "\t"  + action.getType() + "\t"  + action.getId() + "\t"  +action.getName());

        }
        else {

            Log.e(LOG_TAG, "the action is null");

        }


		
	}
	
	public static void resumeAction(Action action) {

        if (action!=null) {


            Log.d(LOG_TAG, " [resumeAction] [test pause resume] Going to resume the action " + action.getId() );

            //set pause first because it is resumed;
            //while the system continouously checks whether action is paused before they are executed,
            //we just need to set the pause flag false and then it will resume automatically...
            action.setPaused(false);

            //if the action is for saving records, we also need to mark the curRunningSession back to unpaused
            if (action.getType().equals(ActionManager.ACTION_TYPE_SAVING_RECORD)){

                SavingRecordAction savingRecordAction = (SavingRecordAction) action;
                int sessionId = savingRecordAction.getSessionId();
                Session session = RecordingAndAnnotateManager.getCurRecordingSession(sessionId);
                session.setPaused(false);

                Log.d(LOG_TAG, " [resumeAction] resume the session " + session.getId() + " pause is " + session.isPaused()  );

            }

            //log
            LogManager.log( LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ACTION_RESUME,
                    "Resume Action:" + "\t"  + action.getType() + "\t"  + action.getId() + "\t"  +action.getName());
        }
        else {

            Log.e(LOG_TAG, "the action is null");

        }
		
	}
	
	
	/**
	 * 
	 * @param action
	 */
	public static void cancelAction (Action action) {


        if (action!=null) {

            Log.d(LOG_TAG, " [stopAction] Going to cancel/unregister the action " );
        }
        else {

            Log.e(LOG_TAG, "the action is null");

        }
		
		
		
		
	}
	
	
	public static void executeActionControl (int id) {

		for (int i=0; i< mActionControlList.size(); i++){
			if (mActionControlList.get(i).getId()==id){				
				//Log.d(LOG_TAG, "[executeActionControl] examine the action control" +mActionControlList.get(i).getId() + " match with the target action control " + id);
				executeActionControl(mActionControlList.get(i));
			}
				
		}		
	}


    /**
     * Here will decide what operation to take on actions based on the action control type
     * @param actionControl
     */
	public static void executeActionControl (ActionControl actionControl) {
		
		Log.d(LOG_TAG, "[executeActionControl] executing the action control " + actionControl.getId() + " which is to " 
				+getActionControlTypeName(actionControl.getType()) + " action " + actionControl.getAction().getId() + " " + actionControl.getAction().getName()  );

		Action action= actionControl.getAction();
		
		if (actionControl.getType()==ActionManager.ACTION_CONTROL_TYPE_START){
			
			startAction(action);

            //see if the action control will trigger event, action control or action

            Log.d(LOG_TAG, "[executeActionControl] the action control start has  " + actionControl.getTriggerLinks().size() + " triggerlinks, should start trigger!");
            TriggerManager.executeTriggers(actionControl);
		}
		
		else if (actionControl.getType()==ActionManager.ACTION_CONTROL_TYPE_CANCEL){
		
			cancelAction(action);
            TriggerManager.executeTriggers(actionControl);
		}
		
		else if (actionControl.getType()==ActionManager.ACTION_CONTROL_TYPE_STOP){
			
			stopAction(action);
            TriggerManager.executeTriggers(actionControl);
		}
				
				
		else if (actionControl.getType()==ActionManager.ACTION_CONTROL_TYPE_PAUSE){
			
			pauseAction(action);
            TriggerManager.executeTriggers(actionControl);
		}
		
		//if the action control is to resume a currently paused action
		else if (actionControl.getType()==ActionManager.ACTION_CONTROL_TYPE_RESUME) {
			
			//checked whether the action is being paused now..if yes, we resumed the action
			if (actionControl.getAction().isPaused()){
				resumeAction(action);
			}
            TriggerManager.executeTriggers(actionControl);
			
		}
		
		
	}


	/**
	 * this function is called when the probe service starts . It will execute all that need to launch when the service starts
     * and all that are scheduled without triggers.
	 *
	 */
	public static void registerActionControls(){
		Log.d(LOG_TAG, "[ registerActionControls] enter registerActionControls");

		
		for (int i=0; i<mActionControlList.size(); i++){
			
			Action action = mActionControlList.get(i).getAction();
			
					
			Log.d(LOG_TAG, "[ registerActionControls] examine action control " + mActionControlList.get(i).getId() + 
					" of action " + action.getId() + " of which the type is " + action.getType() + " and the continuity is " + action.isContinuous()
					+" the rate is " + action.getActionRate()
					);
			
			
			//get the action control which should launch when the Probe service starts. 
			if (mActionControlList.get(i).getLaunchMethod().equals(ActionManager.ACTION_LAUNCH_STYLE_APP_START)){
				
				Log.d(LOG_TAG, "[ registerActionControls] action control " + mActionControlList.get(i).getId() + 
						" needs to start at the beginning, it continuity is " + action.isContinuous());
				
				//if the action is contibuous, the action is put into the runningAction list instead of starting them.
				if (action.isContinuous()){
					//put into the runningActionList
					mRunningActionList.add(action);					
				}
				else {
					//start the action
					startAction(mActionControlList.get(i).getAction());					
				}
				
				
			}
			//get the actionContorl which belongs to scheduled type	
			else if (mActionControlList.get(i).getLaunchMethod().equals(ActionManager.ACTION_LAUNCH_STYLE_SCHEDULE)){
				
				Log.d(LOG_TAG, "[ registerActionControls] action control " + mActionControlList.get(i).getId() + 
						" needs to be scheduled: " + mActionControlList.get(i).getSchedule());
				
				//register the actioncontrol
                ScheduleAndSampleManager.registerActionControl(mActionControlList.get(i));
			}
			
			
		}
		
		
	}

    /**
     * This is called everyday in the early morning (after bed time) to reschedule action controls that need to be
     * scheduled everyday.
     */
    public static void updateActionControlSchedules() {


        for (int i=0; i<mActionControlList.size(); i++){

            Action action = mActionControlList.get(i).getAction();

            //get the actionContorl which belongs to scheduled type
            if (mActionControlList.get(i).getLaunchMethod().equals(ActionManager.ACTION_LAUNCH_STYLE_SCHEDULE)){

                Log.d(LOG_TAG, "[test reschedule][ updateActionControlSchedules] action control " + mActionControlList.get(i).getId() +
                        " needs to be scheduled: " + mActionControlList.get(i).getSchedule());

                //update shcedule of the action control
                ScheduleAndSampleManager.registerActionControl(mActionControlList.get(i));
            }


        }

    }


	

	public static ActionControl getActionControl (int id){

		for (int i=0; i<mActionControlList.size(); i++){
			if (id==mActionControlList.get(i).getId()){
				return mActionControlList.get(i);
			}
		}		
		return null;
		
	}
	
	public static Action getAction (int id){

		for (int i=0; i<mActionList.size(); i++){
			if (id==mActionList.get(i).getId()){
				return mActionList.get(i);
			}
		}		
		return null;
		
	}

    //Get all the action controls in a certain type of a specific action
    public static ArrayList<ActionControl> getActionControls (Action action, int actionControlType){

        ArrayList<ActionControl> actionControls = new ArrayList<ActionControl>();

        Log.d(LOG_TAG, "[getActionControls] going to find actioncontrols of action " + action.getId() + " " + action.getName() );

        for (int i = 0; i < getActionControlList().size(); i++) {

            ActionControl ac = getActionControlList().get(i);

            if (ac.getAction()==action && ac.getType()== actionControlType) {
                Log.d(LOG_TAG, "[getActionControls] found action control  " + ac.getId() + " of which the action is " +ac.getAction().getId());

                actionControls.add(ac);
            }

        }

        return actionControls;
    }


    public static void addAction(Action action) {

        if (mActionList==null){
            mActionList = new ArrayList<Action>();
        }

        mActionList.add(action);
    }

	
	public static ArrayList<ActionControl> getActionControlList() {		
		return mActionControlList;
	}
	
	public static ArrayList<Action> getActionList() {		
		return mActionList;
	}
	
	public static ArrayList<Action> getRunningActionList() {		
		return mRunningActionList;
	}

	
	public static void addRunningAction(Action action){
		
		if (mRunningActionList==null){
			mRunningActionList = new ArrayList<Action>();
		}
		
		mRunningActionList.add(action);
	}

	public static Action getRunningAction (int id){

		for (int i=0; i<mRunningActionList.size(); i++){
			if (id==mRunningActionList.get(i).getId()){
				return mRunningActionList.get(i);
			}
		}		
		return null;
		
	}
		
	
	
	
	
	/**get the current time in milliseconds**/
	public static long getCurrentTimeInMillis(){		
		//get timzone		
		TimeZone tz = TimeZone.getDefault();		
		Calendar cal = Calendar.getInstance(tz);
		long t = cal.getTimeInMillis();		
		return t;
	}
	
	/**convert long to timestring**/
	
	public static String getTimeString(long time){		

		SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
		String currentTimeString = sdf_now.format(time);
		
		return currentTimeString;
	}

    /**
     *
     * @param sessionId
     * @param startRecording: indicate whether we should start a new recording when entering the activity.
     */
    public static void startAnnotateActivity(int sessionId, boolean startRecording, int annotateRecordingActionId, String reviewMode){

        Log.d(LOG_TAG, " [startAnnotateActivity] start by user? " + startRecording);

        Bundle bundle = new Bundle();

        //indicate which session
        bundle.putInt(DatabaseNameManager.COL_SESSION_ID, sessionId);

        //indicate whether we should start a recording when entering the annotate activity
        bundle.putBoolean(ACTION_PROPERTIES_RECORDING_STARTED_BY_USER, startRecording);
        bundle.putInt("annotateRecordingActionId", annotateRecordingActionId);
        bundle.putString(ActionManager.ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING, reviewMode);

        Intent intent = new Intent(mContext, AnnotateActivity.class);
        intent.putExtras(bundle);

        mContext.startActivity(intent);

        //log
        LogManager.log( LogManager.LOG_TYPE_SYSTEM_LOG,
                LogManager.LOG_TAG_ACTIVITY_START,
                "Start manually annotate session" + "\t" + sessionId + "\t" + "for action " + "\t" + annotateRecordingActionId);

    }


    public static Intent generateEmailQuestionnaireIntent(int questionnaireTemplateId) {

        //create intent for composing email

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");

        String emailSubject = QuestionnaireManager.getEmailQuestionnaireSubject(questionnaireTemplateId);
        String emailBody = QuestionnaireManager.getEmailQuestionnaireContent(questionnaireTemplateId);
        String[] recipients = QuestionnaireManager.getEmailQuestionnaireRecipients(questionnaireTemplateId);

        //we need to attach title and body of the email to the intent
        intent.putExtra(Intent.EXTRA_EMAIL  , recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        intent.putExtra(Intent.EXTRA_TEXT,  emailBody);

        //log
        LogManager.log( LogManager.LOG_TYPE_SYSTEM_LOG,
                LogManager.LOG_TAG_ACTIVITY_START,
                "Generate daily report using questionnaire template" + "\t" + questionnaireTemplateId);

        return intent;
    }

	
   public static String getActionControlTypeName(int actionControlType) {

        switch(actionControlType) {
        case ActionManager.ACTION_CONTROL_TYPE_START:
            return ActionManager.ACTION_PROPERTIES_START;
        case ActionManager.ACTION_CONTROL_TYPE_CANCEL:
            return ActionManager.ACTION_PROPERTIES_CANCEL;
        case ActionManager.ACTION_CONTROL_TYPE_STOP:
            return ActionManager.ACTION_PROPERTIES_STOP;
        case ActionManager.ACTION_CONTROL_TYPE_PAUSE:
            return ActionManager.ACTION_PROPERTIES_PAUSE;
        case ActionManager.ACTION_CONTROL_TYPE_RESUME:
            return ActionManager.ACTION_PROPERTIES_RESUME;
        }
        return "unknown";
	}
	
}



