package edu.umich.si.inteco.minuku.util;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.ContextStateManager;
import edu.umich.si.inteco.minuku.data.LocalDBHelper;
import edu.umich.si.inteco.minuku.model.Condition;
import edu.umich.si.inteco.minuku.model.Configuration;
import edu.umich.si.inteco.minuku.model.Criterion;
import edu.umich.si.inteco.minuku.model.EmailQuestionnaireTemplate;
import edu.umich.si.inteco.minuku.model.Circumstance;
import edu.umich.si.inteco.minuku.model.Notification;
import edu.umich.si.inteco.minuku.model.ProbeObjectControl.ActionControl;
import edu.umich.si.inteco.minuku.model.Question;
import edu.umich.si.inteco.minuku.model.QuestionnaireTemplate;
import edu.umich.si.inteco.minuku.model.StateValueCriterion;
import edu.umich.si.inteco.minuku.model.TimeCriterion;
import edu.umich.si.inteco.minuku.model.actions.Action;
import edu.umich.si.inteco.minuku.model.actions.AnnotateAction;
import edu.umich.si.inteco.minuku.model.actions.AnnotateRecordingAction;
import edu.umich.si.inteco.minuku.model.actions.GenerateEmailQuestionnaireAction;
import edu.umich.si.inteco.minuku.model.actions.GeneratingQuestionnaireAction;
import edu.umich.si.inteco.minuku.model.actions.MonitoringCircumstanceAction;
import edu.umich.si.inteco.minuku.model.actions.SavingRecordAction;

public class ConfigurationManager {

	private static final String LOG_TAG = "ConfigurationManager";

	private static final String TEST_FILE_NAME = "new_study.json";

	public static final String CONFIGURATION_FILE_NAME = Constants.CONFIGURATION_FILE_NAME_PARTI;

	public static final String CONFIGURATION_PROPERTIES_ID = "Id";
	public static final String CONFIGURATION_PROPERTIES_STUDY = "Study";
	public static final String CONFIGURATION_PROPERTIES_VERSION = "Version";
	public static final String CONFIGURATION_PROPERTIES_NAME = "Name";
	public static final String CONFIGURATION_PROPERTIES_CONTENT = "Content";
	public static final String CONFIGURATION_PROPERTIES_CONFIGURATION = "Configuration";
	public static final String CONFIGURATION_PROPERTIES_DESCRIPTION = "Description";
	public static final String TASK_PROPERTIES_TIMESTAMP_STRING = "Timestamp_string";
	public static final String TASK_PROPERTIES_CREATED_TIME = "Created_time";
	public static final String TASK_PROPERTIES_START_TIME = "Start_time";
	public static final String TASK_PROPERTIES_END_TIME = "End_time";

	public static final String CONFIGURATION_CATEGORY_CONDITIONS = "Conditions";
	public static final String CONFIGURATION_CATEGORY_ACTION = "Action";
	public static final String CONFIGURATION_CATEGORY_TASK = "Task";
	public static final String CONFIGURATION_CATEGORY_EVENT = "Circumstances";
	public static final String CONFIGURATION_CATEGORY_QUESTIONNAIRE = "Questionnaire";

    public static final String SERVICE_SETTING_STOP_SERVICE_DURING_MIDNIGHT = "StopServiceDuringMidNight";

	//properties
	public static final String CONDITION_PROPERTIES_STATE = "State";
	public static final String CONDITION_PROPERTIES_SOURCE = "Source";
	public static final String CONDITION_PROPERTIES_RELATIONSHIP = "Relationship";
	public static final String CONDITION_PROPERTIES_TARGETVALUE ="TargetValue";
	public static final String CONDITION_PROPERTIES_MEASURE ="Measure";
	public static final String CONDITION_PROPERTIES_VALUE_CRITERION ="Value_Criteria";
	public static final String CONDITION_PROPERTIES_TIME_CRITERION ="Value_Criteria";


	private static LocalDBHelper mLocalDBHelper;
	private static Context mContext;
	
	public ConfigurationManager(Context context){		
		
		mContext = context;
		mLocalDBHelper = new LocalDBHelper(mContext, Constants.TEST_DATABASE_NAME);
		loadConfiguration();
	}
	
	
	
	/**
	 * When the app is back to active, the app loads configurations from the database
	 */
	public void loadConfiguration() {
		
		Log.d(LOG_TAG, "[loadConfiguration]");
		
		//connect to the DB and load configuration from the DB
		ArrayList<String> res = new ArrayList<String>();		
	
		/** 1. first try to load configurations from the database **/
		res = mLocalDBHelper.queryConfigurations();
		Log.d(LOG_TAG, "[loadConfiguration] there are " + res.size() + " configurations in the database");
		/*
		for (int i=0; i<res.size() ; i++){
			
			String cline = res.get(i);			
			String [] separated = cline.split(Constants.DELIMITER);
			//Log.d(LOG_TAG, "[loadConfiguration] the first configuration from the database has " + separated.length + " attributes the content is " + cline);
			
			int id = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_CONFIGURATION_ID]);
			int study_id = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_CONFIGURATION_STUDY_ID]);
			int version = Integer.parseInt(separated[DatabaseNameManager.COL_INDEX_CONFIGURATION_VERSION]);
			String name = separated[DatabaseNameManager.COL_INDEX_CONFIGURATION_NAME];
			String content_str = separated[DatabaseNameManager.COL_INDEX_CONFIGURATION_CONTENT];	
			
			JSONObject content=null;
			try {
				content = new JSONObject(content_str);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//create configuration
			Configuration config = new Configuration (id, study_id, version, name, content) ;

			//load the content of the configuration
			loadConfigurationContent (config);
			
		}
		*/

		/*** 2 if there is no data in the DB ( or sharedpreference), then load file **/

		
		if (res.size()==0){

			//load files
            String filename =  TEST_FILE_NAME;

            Log.d(LOG_TAG, "[loadConfiguration] no configuration in the database, load file.." + filename);
            String study_str = new FileHelper(mContext).loadFileFromAsset(filename);
			
			//load circumstances and conditions
			try {
		
				JSONArray studyJSONArray = new JSONArray(study_str);
				
				for (int i=0; i< studyJSONArray.length(); i++){
					
					JSONObject studyJSON = studyJSONArray.getJSONObject(i);							
					
					//get the properties of the current study
					int study_id = studyJSON.getInt(CONFIGURATION_PROPERTIES_ID);
					String study_name = studyJSON.getString(CONFIGURATION_PROPERTIES_NAME);
					Log.d(LOG_TAG, "[loadConfiguration]  Now reading the study " + study_id + " : " + study_name);
					
					
					//now get configuration JSON
					JSONObject configJSON = studyJSON.getJSONObject(CONFIGURATION_PROPERTIES_CONFIGURATION);
					
					//get properties of the config
					int id = configJSON.getInt(CONFIGURATION_PROPERTIES_ID);
					int version = configJSON.getInt(CONFIGURATION_PROPERTIES_VERSION);
					String name = configJSON.getString(CONFIGURATION_PROPERTIES_NAME);					
					JSONObject content = configJSON.getJSONObject(CONFIGURATION_PROPERTIES_CONTENT);			
					Configuration config = new Configuration (id, study_id, version, name, content) ;
					
					//Load the content of the configuration
					loadConfigurationContent (config);
					
					//store the configuration into the database
				//	Log.d(LOG_TAG, "[loadConfiguration]  After creating the configuration object, inser the configuration into the database");
					
				//	mLocalDBHelper.insertConfigurationTable(config, DatabaseNameManager.CONFIGURATION_TABLE_NAME);

					//TODO: save configuration in SharedPreference

	
				}
				
			
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
		}
		
		
		
	}


	
	/**
	 * The Configuration has a source in JSON format. The function takse a JSON and configurate the app
	 */
	//TODO: we need to change the configutaiton.
	public void loadConfigurationContent(Configuration config) {


		//TODO: just for testing condition and staterule, we create some here.
		//loadTestStateRule();

		//source is in JSON format
		JSONObject content = config.getContent();
		
		Log.d(LOG_TAG, "[loadConfigurationContent]  load the configuration content of study " + config.getStudyId());

        //load configuration settings
        try {
            if (content.has(ConfigurationManager.SERVICE_SETTING_STOP_SERVICE_DURING_MIDNIGHT)){
                boolean stopServiceDuringMidNight = content.getBoolean(ConfigurationManager.SERVICE_SETTING_STOP_SERVICE_DURING_MIDNIGHT);
                Log.d(LOG_TAG, "stop service at night is" + stopServiceDuringMidNight);
				//write into the preference
				PreferenceHelper.setPreferenceValue(ConfigurationManager.SERVICE_SETTING_STOP_SERVICE_DURING_MIDNIGHT, stopServiceDuringMidNight);

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


		/** load circumstances **/
		try {
			if (content.has(ConfigurationManager.CONFIGURATION_CATEGORY_EVENT)){
                JSONArray circumstancesJSON = content.getJSONArray(ConfigurationManager.CONFIGURATION_CATEGORY_EVENT);
				loadCircumstancesFromJSON(circumstancesJSON, config.getStudyId());
            }
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		/** load actions **/
		try {
            if (content.has(ConfigurationManager.CONFIGURATION_CATEGORY_ACTION)){
                JSONArray actionsJSON = content.getJSONArray(ConfigurationManager.CONFIGURATION_CATEGORY_ACTION);
                loadActionsFromJSON (actionsJSON, config.getStudyId());
            }

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/** load questionnaires**/
		try {

            if (content.has(ConfigurationManager.CONFIGURATION_CATEGORY_QUESTIONNAIRE)){
                JSONArray questionnairesJSON = content.getJSONArray(ConfigurationManager.CONFIGURATION_CATEGORY_QUESTIONNAIRE);
                loadQuestionnairesFromJSON (questionnairesJSON, config.getStudyId());
            }

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * 
	 * @param circumstancesJSON
	 */
	public static void loadCircumstancesFromJSON (JSONArray circumstancesJSON, int study_id){

		Log.d(LOG_TAG, "[loadCircumstancesFromJSON] load the circumstance content of study " + study_id);
		
		
		for (int i = 0; i < circumstancesJSON.length(); i++){
			
			Circumstance circumstance = null;
			JSONObject circumstanceJSON = null;
			
			try {
				circumstanceJSON = circumstancesJSON.getJSONObject(i);
				
				int id= circumstanceJSON.getInt(CONFIGURATION_PROPERTIES_ID);
				String name = circumstanceJSON.getString(CONFIGURATION_PROPERTIES_NAME);
				String description = circumstanceJSON.getString(CONFIGURATION_PROPERTIES_DESCRIPTION);

				//creat the circumstance object
				circumstance = new Circumstance(id, name, study_id);
				
				//add the conditionJSON to the circumstance
				if (circumstanceJSON.has(CONFIGURATION_CATEGORY_CONDITIONS)){

					//There could be multiple conditions. So it is a JSONArray
					JSONArray conditionJSONArray = circumstanceJSON.getJSONArray(CONFIGURATION_CATEGORY_CONDITIONS);
					
					//get the list of conditions from each circumstance
					ArrayList<Condition> conditions = loadConditionsFromJSON(conditionJSONArray);
					
					Log.d(LOG_TAG, "[ In loadCircumstancesFromJSON] setting conditionJSONArray: " + conditionJSONArray);

					//set the condition object arraylist to the circumstance.
					circumstance.setConditionList(conditions);
				}
				
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			
			/** after creating the circumstance object, add circumstance to circumstanceList, and to the databasse..**/
			//add to the list
			ContextManager.addCircumstance(circumstance);
			//TODO: add circumstance to the database (sharedpreference)

		}//end of reading circumstanceJSONArray
	}
	
	
	/**
	 *
     */
	public static void loadActionsFromJSON(JSONArray actionJSONArray, int study_id) {

		Log.d(LOG_TAG, " [loadActionsFromJSON] there are "  +   actionJSONArray.length() + " actions" );
		
		//load details for each action
		for (int i = 0; i < actionJSONArray.length(); i++){
			
			JSONObject actionJSON = null;
			JSONObject controlJSON = null;				
			
			Action action=null;
			
			try {
				
				//get JSON for action and schedule within the action
				actionJSON = actionJSONArray.getJSONObject(i);

				/** 1. first create action and schedule object based on the required field, then set propoerties based on the schedule type**/
				
				//get action required fields
				int action_id= actionJSON.getInt(ActionManager.ACTION_PROPERTIES_ID);
				String type = actionJSON.getString(ActionManager.ACTION_PROPERTIES_TYPE);
				String execution_style = actionJSON.getString(ActionManager.ACTION_PROPERTIES_EXECUTION_STYLE);
				controlJSON = actionJSON.getJSONObject(ActionManager.ACTION_PROPERTIES_CONTROL);
				String name = actionJSON.getString(ActionManager.ACTION_PROPERTIES_NAME);
				
				Log.d(LOG_TAG, "[loadActionsFromJSON] examine action" + " action: " + action_id + " , for type " + type
						+ " execution style " + execution_style );
				
				
				action = new Action (action_id, name, type, execution_style, study_id);

				/** 2 We generate actions based on the type. Different actions have different properties**/
				//Action of phone questionnaire
				if (type.equals(ActionManager.ACTION_TYPE_QUESTIONNAIRE)){
					
					int questionnaire_id = actionJSON.getInt(ActionManager.ACTION_PROPERTIES_QUESTIONNAIRE_ID);
					GeneratingQuestionnaireAction a = new GeneratingQuestionnaireAction (action_id, name, type,execution_style, study_id);
					a.setQuestionnaireId(questionnaire_id);
					action = a; 
					//Log.d(LOG_TAG, " the aciton" + action.getId() + " questionnaire id:  " + a.getQuestionnaireId());
					
				}
				//Action of email questionnaire
                else if (type.equals(ActionManager.ACTION_TYPE_EMAIL_QUESTIONNAIRE)){

                    int questionnaire_id = actionJSON.getInt(ActionManager.ACTION_PROPERTIES_QUESTIONNAIRE_ID);
                    GenerateEmailQuestionnaireAction a = new GenerateEmailQuestionnaireAction (action_id, name, type,execution_style, study_id);
                    a.setQuestionnaireId(questionnaire_id);
                    action = a;

                    Log.d(LOG_TAG, "[loadActionsFromJSON] examine action" + " action: " + action_id + " , for type " + type
                            + " execution style " + execution_style );

                }
				
				////Action of monitoring circumstances. We associate circumstance ids with the action.
				else if (type.equals(ActionManager.ACTION_TYPE_MONITORING_EVENTS)){						
					
					String monitor_circumstance_ids = actionJSON.getString(ActionManager.ACTION_PROPERTIES_MONITORING_EVENTS);
					String [] ids = monitor_circumstance_ids.split(",");
					
					MonitoringCircumstanceAction a = new MonitoringCircumstanceAction (action_id, name, type, execution_style, study_id);

					//associate circumstance ids to the monitoring action.
					for (int j=0; j<ids.length; j++){
						int id = Integer.parseInt(ids[j]);
						a.addMonitoredCircumstance(id);
						Log.d(LOG_TAG, " [loadActionsFromJSON] the aciton" + action.getId() + " monitors circumstance:  "  +  id);

					}
					
					action  = a;

				}

                //Action of saving record
                else if (type.equals(ActionManager.ACTION_TYPE_SAVING_RECORD)) {

                    SavingRecordAction a = new SavingRecordAction(action_id,name, type,execution_style, study_id );
                    action = a;
                }

                //Action of annotating data (no saving records)
                else if (type.equals(ActionManager.ACTION_TYPE_ANNOTATE)) {

                    JSONObject annotateJSON = actionJSON.getJSONObject(ActionManager.ACTION_PROPERTIES_ANNOTATE);

                    String mode = annotateJSON.getString(ActionManager.ACTION_PROPERTIES_ANNOTATE_MODE);
                    String vizType = annotateJSON.getString(ActionManager.ACTION_PROPERTIES_VIZUALIZATION_TYPE);
                    String reviewRecordingMode = annotateJSON.getString(ActionManager.ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING);

                    AnnotateAction a = new AnnotateAction (action_id, name, type, execution_style, study_id, mode, vizType, reviewRecordingMode);
                    action = a;
                }

                //Action of saving and apply annotation together.
				//TODO: check if we can make this cleaner.
                else if (type.equals(ActionManager.ACTION_TYPE_ANNOTATE_AND_RECORD)) {

                    JSONObject annotateJSON = actionJSON.getJSONObject(ActionManager.ACTION_PROPERTIES_ANNOTATE);

                    String mode = annotateJSON.getString(ActionManager.ACTION_PROPERTIES_ANNOTATE_MODE);
                    String recordingType = annotateJSON.getString(ActionManager.ACTION_PROPERTIES_ANNOTATE_RECORDING_TYPE);
                    String vizType = annotateJSON.getString(ActionManager.ACTION_PROPERTIES_VIZUALIZATION_TYPE);
                    boolean allowAnnotateInProcess = annotateJSON.getBoolean(ActionManager.ACTION_PROPERTIES_ANNOTATE_ALLOW_ANNOTATE_IN_PROCESS);
                    String reviewRecordingMode = annotateJSON.getString(ActionManager.ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING);
                    boolean recordingStartByUser = annotateJSON.getBoolean(ActionManager.ACTION_PROPERTIES_RECORDING_STARTED_BY_USER);

                    AnnotateRecordingAction annotateRecordingAction =
                            new AnnotateRecordingAction(
                                    action_id,
                                    name,
                                    type,
                                    execution_style,
                                    study_id,
                                    mode,
                                    recordingType,
                                    vizType,
                                    allowAnnotateInProcess,
                                    reviewRecordingMode,
                                    recordingStartByUser);

                    Log.d(LOG_TAG, "[loadActionsFromJSON] the action is annotateRecording, mode: "
                            + mode + " recordType: " + recordingType + " vizType " + vizType + " allowannotate " + allowAnnotateInProcess + " review recording: " + reviewRecordingMode
                             + " recording start by user " + recordingStartByUser
                            );


                    action  = annotateRecordingAction;

                }



				/**4. examine whether the action is continuous or not**/
				if (actionJSON.has(ActionManager.ACTION_PROPERTIES_CONTINUITY)){
					
					JSONObject continuityJSON = actionJSON.getJSONObject(ActionManager.ACTION_PROPERTIES_CONTINUITY);
					Log.d(LOG_TAG, "[loadActionsFromJSON] the continuityJSON:  " + continuityJSON.toString());
					
					float rate= (float) continuityJSON.getDouble(ActionManager.ACTION_CONTINUITY_PROPERTIES_RATE);
					int duration = continuityJSON.getInt(ActionManager.ACTION_CONTINUITY_PROPERTIES_DURATION);
					
					action.setActionDuration(duration);
					action.setActionRate(rate);
					action.setContinuous(true);
					Log.d(LOG_TAG, "[loadActionsFromJSON] the action " + action.getId() + " is continuous " + action.isContinuous() + " rate: "
							+ action.getActionRate()  +" duration  " + action.getActionDuration());
					
					
					
				}else {
					action.setContinuous(false);
					Log.d(LOG_TAG, "[loadActionsFromJSON] the action " + action.getId() + " is not continuous " + action.isContinuous() );
				}
				
			
				
				/**5. check whether there are notification of the action **/			
				if (actionJSON.has(ActionManager.ACTION_PROPERTIES_NOTIFICATION)){

                    //notification is an array. It may have a typical notification and a ongoing notifications
                    JSONArray notiJSONArray = actionJSON.getJSONArray(ActionManager.ACTION_PROPERTIES_NOTIFICATION);

                    for (int j=0; j<notiJSONArray.length(); j++){

                        JSONObject notiJSONObject  = notiJSONArray.getJSONObject(j);

						//notification has type, launch style, title, and message.
						//TODO: improve the notification style.
                        String notiType = notiJSONObject.getString(ActionManager.ACTION_PROPERTIES_NOTIFICATION_TYPE);
                        String notiLaunch = notiJSONObject.getString(ActionManager.ACTION_PROPERTIES_NOTIFICATION_LAUNCH);
                        String notiTitle = notiJSONObject.getString(ActionManager.ACTION_PROPERTIES_NOTIFICATION_TITLE);
                        String notiMessage = notiJSONObject.getString(ActionManager.ACTION_PROPERTIES_NOTIFICATION_MESSAGE);

                        Notification notification = new Notification(notiLaunch, notiType, notiTitle, notiMessage);

						//add notification to the action
                        action.addNotification(notification);
                    }


				}				
				
				/** 6 load controls to actions**/					
				loadActionControlsFromJSON (controlJSON, action);

			}catch (JSONException e1) {

				e1.printStackTrace();
			}

			
			if (action!=null){					
				//add action into the list
				ActionManager.getActionList().add(action);
			}
		}//for each action
	}
	
	
	/**
	 * 
	 * @param conditionJSONArray
	 * @return
	 */
	public static ArrayList<Condition> loadConditionsFromJSON(JSONArray conditionJSONArray) {


		 ArrayList<Condition> conditions = new  ArrayList<Condition>();


			Log.d(LOG_TAG, "[loadConditionsFromJSON] the conditions of the current circumstance is:  " + conditionJSONArray.toString());

			for (int i = 0; i < conditionJSONArray.length(); i++){

				try {

					JSONObject conditionJSON = conditionJSONArray.getJSONObject(i);

					String stateValue = conditionJSON.getString(CONDITION_PROPERTIES_STATE);
					String source = conditionJSON.getString(CONDITION_PROPERTIES_SOURCE);

					//need to convert source into integer

					/** 1 Read StateValueCriteria for Condition **/
					JSONArray valueCriteria = conditionJSON.getJSONArray(CONDITION_PROPERTIES_VALUE_CRITERION);

					//create a list of criterion (criteria) to save all the criteria
					ArrayList<StateValueCriterion> critera  = new ArrayList<StateValueCriterion>();

					//analyze criteria in the JSONArray and create objects to save them
					for (int j=0; j<valueCriteria.length(); j++ ){

						JSONObject valueCriterion = valueCriteria.getJSONObject(j);

						//we specify default values for measures and relationships

						/**get measure and relationship **/
						//by default (if users don't specify any measure), we assume users want the latest value
						int measure = ContextStateManager.CONTEXT_SOURCE_MEASURE_LATEST_ONE;
						//by default, the user wants relationship be "equal."

						int relationship = ContextStateManager.STATE_MAPPING_RELATIONSHIP_EQUAL;

						//if the user does specify the measure, we use that measure
						if (valueCriterion.has(CONDITION_PROPERTIES_MEASURE)){
							//we conver the string into int
							measure = ContextStateManager.getMeasure(valueCriterion.getString(CONDITION_PROPERTIES_MEASURE));
						}

						if (valueCriterion.has(CONDITION_PROPERTIES_RELATIONSHIP)){
							//we conver the string into a int number
							relationship = ContextStateManager.getRelationship(valueCriterion.getString(CONDITION_PROPERTIES_RELATIONSHIP));
						}


						/**create criterion object based on the type of target value**/
						//if the target value is numeric, we use float number, otherwise we save it as a String value
						if (isNumeric(valueCriterion.getString(CONDITION_PROPERTIES_TARGETVALUE))){
							float targetValue = (float)valueCriterion.getDouble(CONDITION_PROPERTIES_TARGETVALUE);

							//after we read all the properties of a citerion, we creat a criterion object
							critera.add(new StateValueCriterion(measure, relationship,targetValue));


						}
						else{
							String targetValue = valueCriterion.getString(CONDITION_PROPERTIES_TARGETVALUE);

							//after we read all the properties of a citerion, we creat a criterion object
							critera.add(new StateValueCriterion(measure, relationship,targetValue));
						}
					}


					/** 2. add criteria to the Condition **/
					Condition condition = new Condition(source,  stateValue, critera);

					/** 3. Read TimeeCriteria for Condition **/
					if (conditionJSON.has(CONDITION_PROPERTIES_TIME_CRITERION)){

						ArrayList<TimeCriterion> timeCriteria = new ArrayList<TimeCriterion>();
						try {

							//time criterion specificies how recently Minuku observes that state and how long it observes the state.
							JSONArray constraintJSONArray = conditionJSON.getJSONArray(CONDITION_PROPERTIES_TIME_CRITERION);


							for (int k = 0; k < constraintJSONArray.length(); k++){

								JSONObject timeCriterion = constraintJSONArray.getJSONObject(k);

								int measure =  ContextStateManager.getRelationship(timeCriterion.getString(CONDITION_PROPERTIES_MEASURE));
								int relationship = ContextStateManager.getRelationship(timeCriterion.getString(CONDITION_PROPERTIES_RELATIONSHIP));
								float value = Float.parseFloat(timeCriterion.getString(CONDITION_PROPERTIES_TARGETVALUE))  ;

								timeCriteria.add( new TimeCriterion(measure, relationship, value));
							}
						}catch (JSONException e2) {
							e2.printStackTrace();
						}


						//add timecriteria to the condition
						condition.setTimeCriteria(timeCriteria);

						Log.d(LOG_TAG, "[loadConditionsFromJSON] get condition from the file: "
								+ condition.getSource() + " , " + condition.getStateValue() + condition.getCriterion().toString());


					}


					//finally we add condition to the conditionlist
					conditions.add(condition);

				}catch (JSONException e) {

					e.printStackTrace();
				}






				}//end of reading conditionJSONArray




		
		//Log.d(LOG_TAG, "[loadConditionsFromJSON] the current circumstance has " + conditions.size() + " condition");
		return conditions;

	}



	
	/***
	 * read controlJSON and add control objects to the action
	 * @param controlJSON
	 * @param action
	 */
	public static void loadActionControlsFromJSON (JSONObject controlJSON, Action action) {

		//if the action control is to start an action. Most action controls belong to this type. 
		if (controlJSON.has(ActionManager.ACTION_PROPERTIES_START)){
			
			JSONArray startJSONArray = null;
			try {
				startJSONArray = controlJSON.getJSONArray(ActionManager.ACTION_PROPERTIES_START);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (startJSONArray!=null){
				
				for (int i = 0; i < startJSONArray.length(); i++){
					
					JSONObject startJSONObject=null;
					try {
						startJSONObject = startJSONArray.getJSONObject(i);
						//instantiate an Action Control with type "Start"
						//set id based on the number of existing action contorl
						int id = ActionManager.getActionControlList().size()+1;
						
						//create an ActionControl object 
						ActionControl ac = new ActionControl (id, startJSONObject, ActionManager.ACTION_CONTROL_TYPE_START, action);
						
						//add the ActionControl object to the list
						ActionManager.getActionControlList().add(ac);

						
						Log.d(LOG_TAG, "[loadActionControlsFromJSON]  the start acitonControl id is " + ac.getId() + " connects to action " + ac.getAction().getId() + " " + ac.getAction().getName() +
								" and has schedule : " + ac.getSchedule());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
		}
		//if the action control is to stop an action
		if (controlJSON.has(ActionManager.ACTION_PROPERTIES_STOP)) {
			
			JSONArray stopJSONArray;
			try {
				stopJSONArray = controlJSON.getJSONArray(ActionManager.ACTION_PROPERTIES_STOP);
				
				for (int i = 0; i < stopJSONArray.length(); i++){
					
					JSONObject stopJSONObject = stopJSONArray.getJSONObject(i);
					//instantiate an Action Control with type "Stop"
					int id = ActionManager.getActionControlList().size()+1;
					ActionControl ac = new ActionControl (id, stopJSONObject, ActionManager.ACTION_CONTROL_TYPE_STOP, action);
					ActionManager.getActionControlList().add(ac);
					
					Log.d(LOG_TAG, "[loadActionControlsFromJSON]  the stop acitonControl id is " + ac.getId() + " connects to action " + ac.getAction().getId() + 
							" and has schedule : " + ac.getSchedule());
					
					
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//if an action control is to pause an action 
		if (controlJSON.has(ActionManager.ACTION_PROPERTIES_PAUSE)) {
			
			JSONArray pauseJSONArray;		
			
			try {
				pauseJSONArray = controlJSON.getJSONArray(ActionManager.ACTION_PROPERTIES_PAUSE);
				
				Log.d(LOG_TAG, "[loadActionControlsFromJSON] found pause JSON " +  pauseJSONArray);
				
				
				for (int i = 0; i < pauseJSONArray.length(); i++){
					
					JSONObject pauseJSONObject = pauseJSONArray.getJSONObject(i);
					//instantiate an Action Control with type "Pause"
					int id = ActionManager.getActionControlList().size()+1;
					ActionControl ac = new ActionControl (id, pauseJSONObject, ActionManager.ACTION_CONTROL_TYPE_PAUSE, action);
					ActionManager.getActionControlList().add(ac);
					
					Log.d(LOG_TAG, "[loadActionControlsFromJSON]  the pause acitonControl id is " + ac.getId() + " connects to action " + ac.getAction().getId() + 
							" and has schedule : " + ac.getSchedule());
					
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//if the action control is to resume an action 
		if (controlJSON.has(ActionManager.ACTION_PROPERTIES_RESUME)) {
			
			JSONArray resumeJSONArray;
			try {
				resumeJSONArray = controlJSON.getJSONArray(ActionManager.ACTION_PROPERTIES_RESUME);
				
				Log.d(LOG_TAG, "[loadActionControlsFromJSON] found resume JSON " +  resumeJSONArray);
				
				
				for (int i = 0; i < resumeJSONArray.length(); i++){
					
					JSONObject resumeJSONObject = resumeJSONArray.getJSONObject(i);
					//instantiate an Action Control with type "Resume"
					int id = ActionManager.getActionControlList().size()+1;
					ActionControl ac = new ActionControl (id, resumeJSONObject, ActionManager.ACTION_CONTROL_TYPE_RESUME, action);			
					ActionManager.getActionControlList().add(ac);
					
					Log.d(LOG_TAG, "[loadActionControlsFromJSON]  the resume acitonControl id is " + ac.getId() + " connects to action " + ac.getAction().getId() + 
							" and has schedule : " + ac.getSchedule());
					
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//if the action control is to cancel an action
		if (controlJSON.has(ActionManager.ACTION_PROPERTIES_CANCEL)) {
			
			JSONArray cancelJSONArray;
			try {
				cancelJSONArray = controlJSON.getJSONArray(ActionManager.ACTION_PROPERTIES_CANCEL);
				
				for (int i = 0; i < cancelJSONArray.length(); i++){
					
					JSONObject cancelJSONObject = cancelJSONArray.getJSONObject(i);
					//instantiate an Action Control with type "Cancel"
					int id = ActionManager.getActionControlList().size()+1;
					ActionControl ac = new ActionControl (id, cancelJSONObject, ActionManager.ACTION_CONTROL_TYPE_CANCEL, action);
					ActionManager.getActionControlList().add(ac);
					
					Log.d(LOG_TAG, "[loadActionControlsFromJSON]  the acitonControl id is " + ac.getId() + " connects to action " + ac.getAction().getId() + 
							" and has schedule : " + ac.getSchedule());
					
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Log.d(LOG_TAG, "[loadActionControlsFromJSON] after reading action " + action.getId() + " the service has " + ActionManager.getActionControlList().size() + " actioncontrols");
		
	}
	

	

	/**
	 * Load Questions from JSONArray
	 * @param questionnJSONArray
	 * @return
	 */
	private static ArrayList<Question> loadQuestionsFromJSON(JSONArray questionnJSONArray){
		
		
		Log.d(LOG_TAG, "the questionJSONArray is " + questionnJSONArray.toString());
		
		ArrayList<Question> questions = new ArrayList<Question>();
		
		for (int i = 0; i < questionnJSONArray.length(); i++){
			
			JSONObject questionJSON=null;
			Question question = null;

			try {
				
				questionJSON = questionnJSONArray.getJSONObject(i);
				
				
				int index = questionJSON.getInt(QuestionnaireManager.QUESTION_PROPERTIES_INDEX);
				String type = questionJSON.getString(QuestionnaireManager.QUESTION_PROPERTIES_TYPE);
				String question_text = questionJSON.getString(QuestionnaireManager.QUESTION_PROPERTIES_QUESTION_TEXT);
					
				
				question = new Question(index, question_text, type);
				
				Log.d (LOG_TAG, "[loadQuestionsFromJSON]  the question object is " + question.getIndex() + " text: " + question.getText() + " type " + question.getType());
					
				//get options (e.g. multi choice
				if (questionJSON.has(QuestionnaireManager.QUESTION_PROPERTIES_OPTION)){
					
					JSONArray optionJSONArray=null;		
					ArrayList<String> options = new ArrayList<String>();
					
					optionJSONArray = questionJSON.getJSONArray(QuestionnaireManager.QUESTION_PROPERTIES_OPTION);
					
					Log.d (LOG_TAG, "[loadQuestionsFromJSON] the question also has "  + optionJSONArray.length() + " options"); 
							
					for (int j=0; j<optionJSONArray.length(); j++){
						
						JSONObject optionJSON = optionJSONArray.getJSONObject(j);
						
						String option_text = optionJSON.getString(QuestionnaireManager.QUESTION_PROPERTIES_OPTION_TEXT);
						options.add(option_text);
						
						Log.d (LOG_TAG, " option " + j + " : "  + option_text); 
						
					}
					
					question.setOptions(options);

				}

				//other fields of the question...
				if (questionJSON.has(QuestionnaireManager.QUESTION_PROPERTIES_HAS_OTHER_FIELD)){

					question.setHasOtherField(questionJSON.getBoolean(QuestionnaireManager.QUESTION_PROPERTIES_HAS_OTHER_FIELD));
					
				}

                //the questions has dynamic content that needs to be extracted from the database
                if (questionJSON.has(QuestionnaireManager.QUESTION_PROPERTIES_DATA)){

                    question.setDataJSON(questionJSON.getJSONObject(QuestionnaireManager.QUESTION_PROPERTIES_DATA));

                }
				
				
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			questions.add(question);
			
		}
			
		
		
		
		
		
		return questions;
				
	}
	
	
	/**
	 * Load Questionnaire from JSON file
	 * @param study_id
	 */
	private static void loadQuestionnairesFromJSON(JSONArray questionnaireJSONArray, int study_id){
				
		Log.d(LOG_TAG, "loadQuestionnaireFromJSON there are "  +   questionnaireJSONArray.length() + " questionnaires" );

		//load details for each action
		for (int i = 0; i < questionnaireJSONArray.length(); i++){
			
			JSONObject questionnaireJSON;
			JSONArray questionsJSONArray;
			
			QuestionnaireTemplate questionnaireTemplate;
			ArrayList<Question> questions = new ArrayList<Question> ();
			
			try {
				
				questionnaireJSON = questionnaireJSONArray.getJSONObject(i);
				
				int id = questionnaireJSON.getInt(QuestionnaireManager.QUESTIONNAIRE_PROPERTIES_ID);
				String title = questionnaireJSON.getString(QuestionnaireManager.QUESTIONNAIRE_PROPERTIES_TITLE);
                String type = questionnaireJSON.getString(QuestionnaireManager.QUESTIONNAIRE_PROPERTIES_TYPE);
				questionsJSONArray = questionnaireJSON.getJSONArray(QuestionnaireManager.QUESTIONNAIRE_PROPERTIES_QUESTIONS);

                questionnaireTemplate = new QuestionnaireTemplate(id, title, study_id, type);

				if (questionnaireJSON.has(QuestionnaireManager.QUESTIONNAIRE_PROPERTIES_DESCRIPTION)){
					String description = questionnaireJSON.getString(QuestionnaireManager.QUESTIONNAIRE_PROPERTIES_DESCRIPTION);
					questionnaireTemplate.setDescription(description);					
				}

                //if the questionnaire is through email
				if (type.equals(QuestionnaireManager.QUESTIONNAIRE_TYPE_EMAIL)) {

                    EmailQuestionnaireTemplate template = new EmailQuestionnaireTemplate (id, title, study_id, type);

                    //the questionnaire shoud has "Email" field
                    JSONObject emailJSON = questionnaireJSON.getJSONObject(QuestionnaireManager.QUESTIONNAIRE_PROPERTIES_EMAIL);
                    JSONArray recipientsJSONArray = emailJSON.getJSONArray(QuestionnaireManager.QUESTIONNAIRE_EMAIL_PROPERTIES_RECIPIENTS);
                    String subject = emailJSON.getString(QuestionnaireManager.QUESTIONNAIRE_EMAIL_PROPERTIES_SUBJECT);

                    //get recipients from the JSONArray
                    ArrayList<String> recipients = new ArrayList<String>();
                    for (int j=0; j<recipientsJSONArray.length(); j++){
                        String recipient = recipientsJSONArray.getString(j);
                        recipients.add(recipient);
                    }

                    //convert arraylist to String[]
                    String [] re = new String [recipients.size()];
                    recipients.toArray(re);


                    //add information to the template
                    template.setSubject(subject);
                    template.setRecipients(re);

                    //referenc the template back to this emailTemplate
                    questionnaireTemplate = template;
                }


				//read Questions..
				questions = loadQuestionsFromJSON(questionsJSONArray);
				
				//add questions to the questionnaire
				questionnaireTemplate.setQuestions(questions);
				QuestionnaireManager.addQuestionnaireTemplate(questionnaireTemplate);

				
			} catch (JSONException e1) {

			}
			
			
		}

	}

	public static boolean isNumeric(String str)
	{
		try
		{
			double d = Double.parseDouble(str);
		}
		catch(NumberFormatException nfe)
		{
			return false;
		}
		return true;
	}


	
}
