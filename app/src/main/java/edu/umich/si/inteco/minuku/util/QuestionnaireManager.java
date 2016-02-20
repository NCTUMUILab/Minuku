package edu.umich.si.inteco.minuku.util;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.data.LocalDBHelper;
import edu.umich.si.inteco.minuku.model.Annotation;
import edu.umich.si.inteco.minuku.model.AnnotationSet;
import edu.umich.si.inteco.minuku.model.Questionnaire.EmailQuestionnaireTemplate;
import edu.umich.si.inteco.minuku.model.Question;
import edu.umich.si.inteco.minuku.model.Questionnaire.Questionnaire;
import edu.umich.si.inteco.minuku.model.Questionnaire.QuestionnaireTemplate;
import edu.umich.si.inteco.minuku.model.Session;

public class QuestionnaireManager {

	private static final String LOG_TAG = "QuestionnaireManager";
	private static ArrayList<QuestionnaireTemplate> mQuestionnaireTemplateList;
	private static ArrayList<Questionnaire> mQuestionnaireList;
	private static Context mContext;
	private static LocalDBHelper mLocalDBHelper; 
	
	/**Whether the email should be sent out from the server or from the client*/
    public static final String QUESTIONNAIRE_FROM_CLIENT = "client";
    public static final String QUESTIONNAIRE_FROM_SERVER = "server";
    public static final String QUESTIONNAIRE_SENT_FROM_SOURCE = QUESTIONNAIRE_FROM_SERVER;

	/**Properties**/
	public static final String QUESTIONNAIRE_TEMPLATE_PROPERTIES_ID = "Id";
	public static final String QUESTIONNAIRE_PROPERTIES_ID = "Id";
	public static final String QUESTIONNAIRE_PROPERTIES_TITLE = "Title";
    public static final String QUESTIONNAIRE_PROPERTIES_TYPE = "Type";
	public static final String QUESTIONNAIRE_PROPERTIES_DESCRIPTION = "Description";
	public static final String QUESTIONNAIRE_PROPERTIES_QUESTIONS = "Questions";
    public static final String QUESTIONNAIRE_PROPERTIES_EMAIL ="Email";

    //for email quesitonnaire
    public static final String QUESTIONNAIRE_EMAIL_PROPERTIES_RECIPIENTS ="Recipients";
    public static final String QUESTIONNAIRE_EMAIL_PROPERTIES_CC ="CC";
    public static final String QUESTIONNAIRE_EMAIL_PROPERTIES_BCC ="BCC";
    public static final String QUESTIONNAIRE_EMAIL_PROPERTIES_SUBJECT ="Subject";

	public static final String QUESTION_PROPERTIES_INDEX = "Index";
	public static final String QUESTION_PROPERTIES_TYPE = "Type";
	public static final String QUESTION_PROPERTIES_QUESTION_TEXT ="Question_text";
	public static final String QUESTION_PROPERTIES_OPTION = "Option";
    public static final String QUESTION_PROPERTIES_DATA = "Data";
	public static final String QUESTION_PROPERTIES_OPTION_TEXT = "Option_text";
	public static final String QUESTION_PROPERTIES_HAS_OTHER_FIELD ="Has_other_field";
    public static final String QUESTION_PROPERTIES_DATA_FORMAT = "Format";

    //data for questions
    public static final String QUESTION_DATA_TARGET_SESSION = "Session";
    public static final String QUESTION_DATA_TARGET_RECORD = "Record";

    //variable
    public static final String QUESTION_DATA_VARIABLE_SESSION_START_TIME = "$startTime";
    public static final String QUESTION_DATA_VARIABLE_SESSION_END_TIME = "$endTime";
    public static final String QUESTION_DATA_VARIABLE_SESSION_LABEL = "$label";
	
	public static final String QUESTION_PROPERTIES_RESPONSE = "Response";
	public static final String QUESTION_PROPERTIES_ANSWER = "Answer";

    public static final String QUESTIONNAIRE_TYPE_ACTIVITY = "activity";
    public static final String QUESTIONNAIRE_TYPE_EMAIL = "email";
	
	public static final String QUESTION_TYPE_TEXT = "textbox";
	public static final String QUESTION_TYPE_MULTICHOICE_ONE_ANSWER = "multichoice_one_answer";
	public static final String QUESTION_TYPE_MULTICHOICE_MULTIPLE_ANSWER = "multichoice_multiple_answer";
    public static final String QUESTION_TYPE_DESCRIPTION = "description";
	
	
	/**Questionnaire Layout paramter**/
	
	public static final int QUESTIONNAIRE_LAYOUT_PADDING_LEFT = 40;
	public static final int QUESTIONNAIRE_LAYOUT_PADDING_TOP = 20;
	public static final int QUESTIONNAIRE_LAYOUT_PADDING_RIGHT = 40;
	public static final int QUESTIONNAIRE_LAYOUT_PADDING_BOTTOM = 20;
	
	
	public static final int QUESTION_LAYOUT_MARGIN_LEFT = 0;
	public static final int QUESTION_LAYOUT_MARGIN_TOP = 20;
	public static final int QUESTION_LAYOUT_MARGIN_RIGHT = 0;
	public static final int QUESTION_LAYOUT_MARGIN_BOTTOM = 20;
	
	
	
	
	
	public QuestionnaireManager (Context context){
		
		mQuestionnaireList = new ArrayList<Questionnaire>();
		mQuestionnaireTemplateList = new ArrayList<QuestionnaireTemplate>();
		mContext = context;
		mLocalDBHelper = new LocalDBHelper(mContext, Constants.TEST_DATABASE_NAME);

		
	}
	

	public static void addQuestionnaire(Questionnaire questionnaire){		
		mQuestionnaireList.add(questionnaire);
	}
	
	public static ArrayList<Questionnaire> getQuestionnaireList (){
		return mQuestionnaireList;
	}
	
	
	public static void addQuestionnaireTemplate(QuestionnaireTemplate questionnaire){		
		mQuestionnaireTemplateList.add(questionnaire);
	}
	
	public static ArrayList<QuestionnaireTemplate> getQuestionnaireTemplateList (){
		return mQuestionnaireTemplateList;
	}
	
	public static QuestionnaireTemplate getQuestionnaireTemplate(int id) {
		
		if (mQuestionnaireTemplateList==null)
			Log.d(LOG_TAG, "mQuestionnaireTemplateList is null");
		
		for (int i=0; i<mQuestionnaireTemplateList.size(); i++){		
			if (mQuestionnaireTemplateList.get(i).getId()==id){
				Log.d(LOG_TAG, "[getQuestionnaire] questionnaire " + mQuestionnaireTemplateList.get(i).getId() + " found, its type is " +  mQuestionnaireTemplateList.get(i).getType());
				return mQuestionnaireTemplateList.get(i);
			}
				
		}

		return null;
	}
	
	public static Questionnaire getQuestionnaire(int id) {
		
		if (mQuestionnaireList==null)
			Log.d(LOG_TAG, "mQuestionnaireList is null");
		
		for (int i=0; i<mQuestionnaireList.size(); i++){		
			if (mQuestionnaireList.get(i).getId()==id){
				//Log.d(LOG_TAG, "[getQuestionnaire] questionnaire " + mQuestionnaireTemplateList.get(i).getId() + " found");
				return mQuestionnaireList.get(i);
			}
				
		}

		return null;
	}
	
	
	private static void setupQuestionnaireFormat(Questionnaire questionnaire){
		
		int templateId  = questionnaire.getTemplateId();
		QuestionnaireTemplate template = QuestionnaireManager.getQuestionnaireTemplate(templateId);
		
		//get content from the template
		questionnaire.setTitle(template.getTitle());
		questionnaire.setDescription(template.getDescription());
		
		//0: original order;  1: random order
		int mode = 0;
		
		//decide the order of the questions in the questionnaire
		questionnaire.setQuestions( composeQuestionList(template, mode) );
		
	}
	
	
	private static ArrayList<Question> composeQuestionList( QuestionnaireTemplate template, int mode) {
		
		//create a new question list, deep copy. 
		ArrayList<Question> newQuestionList = new ArrayList<Question>();
		ArrayList<Question> oldQuestionList = template.getQuestions();
		
		//maintain the original order, clone the arraylist
		if (mode==0){
			
			for (Question old_q : oldQuestionList){
				
				Question new_q = new Question(old_q);
				Log.d(LOG_TAG, "[composeQuestionList] the new question is" + new_q + " the old question is " + old_q);
				newQuestionList.add(new_q);
				
			}
			
			for (int i=0; i<newQuestionList.size(); i++){
				Log.d(LOG_TAG, "[composeQuestionList] the " + i + "th question in the new questionlist is Q" +  newQuestionList.get(i).getIndex());
			}
		}
		
		//randomize the order
		else if (mode==1){
			
			for (Question old_q : oldQuestionList){
				
				Question new_q = new Question(old_q);
				Log.d(LOG_TAG, "[composeQuestionList] the new question is" + new_q + " the old question is " + old_q);
				newQuestionList.add(new_q);
				Collections.shuffle(newQuestionList);
				
			}
			
			for (int i=0; i<newQuestionList.size(); i++){
				Log.d(LOG_TAG, "[composeQuestionList] the " + i + "th question in the new questionlist is Q" +  newQuestionList.get(i).getIndex());
			}
			
		}
		return newQuestionList;
		
	}
	
	public static ScrollView getQuestionnaireView(Context context, int questionnaire_id){
		
		//when we are going to setup the quesitonnaire, we also need to set the attended time to the questionnaire
		
		//first we get the questionnaire
		Questionnaire questionnaire = getQuestionnaire(questionnaire_id);
		
		//setup the parameters of the questionnaire based on its template
		setupQuestionnaireFormat(questionnaire);
			
		Log.d(LOG_TAG, "[setUpQuestionnaireView] enter getQuestionnaire View" );
		
		
		Log.d(LOG_TAG, "[setUpQuestionnaireView] the questionnaire " + questionnaire);
		
		//then we set the attended time
		long attendedTime = getCurrentTimeInMillis();
		questionnaire.setAttendedTime(attendedTime);
		
		//then we get the template of the questionnaire so that we can create the view of the questionnaire
		
		
		Log.d(LOG_TAG, "[setUpQuestionnaireView] the questionnaire " + questionnaire.getId() + " 's tempalte is "
				 + questionnaire.getTemplateId() + ", it is attended at time " + getTimeString(attendedTime));
		
		//create a scrollview for the questionnaire
		ScrollView sv = new ScrollView(context);

        //create a layout
		LinearLayout ll = new LinearLayout(context);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setPadding(QuestionnaireManager.QUESTIONNAIRE_LAYOUT_PADDING_LEFT,
				QuestionnaireManager.QUESTIONNAIRE_LAYOUT_PADDING_TOP, 
				QuestionnaireManager.QUESTIONNAIRE_LAYOUT_PADDING_RIGHT,
				QuestionnaireManager.QUESTIONNAIRE_LAYOUT_PADDING_BOTTOM);
		
		//add the view of the layout
		sv.addView(ll);
		

		/**going to set up questionnaire view **/
		//title
		TextView titleTextView = new TextView(context);
		Log.d(LOG_TAG, "the  questionnaire's title is " + questionnaire.getTitle() );

		titleTextView.setText(questionnaire.getTitle());
		titleTextView.setTextSize(24);
		ll.addView(titleTextView);
		
		//description
		TextView desciptionTextView = new TextView(context);
		desciptionTextView.setText(questionnaire.getDescription());
		desciptionTextView.setTextSize(18);
		ll.addView(desciptionTextView);
				
		//if the questionnaire exists
		if (questionnaire!=null){			

            //get all the questions from the questionnarie
			ArrayList<Question> questions = questionnaire.getQuestions();



            /**
             * * Iterate all the questions and set up the view for each **
             * */

 			for (int i=0; i<questions.size(); i++){
				
				Question qu = questions.get(i);
				//Log.d(LOG_TAG, "the current question "  + i + " type is " + qu.getType() + " content: " + qu.getText());
				
				//the question text
				TextView questionTextView = new TextView(context);

				//index of the question
				questionTextView.setText( (i+1) + "." + qu.getText());
				questionTextView.setTextSize(18);				
				questionTextView = (TextView) setUpQuestionLayout (questions.get(i), questionTextView);
				
				ll.addView(questionTextView);


				/** set up the questionnaire view based on the type of the question **/


                /**if the question is a text field **/
				if (qu.getType().equals(QuestionnaireManager.QUESTION_TYPE_TEXT)){
					
					EditText editText = new EditText(context);
					ll.addView(editText);
					
				}

                /**if the question is the multichoice question with one answer (i.e. radio box)**/
				else if (qu.getType().equals(QuestionnaireManager.QUESTION_TYPE_MULTICHOICE_ONE_ANSWER)){
				
					//Log.d(LOG_TAG, "the current question type has "  + qu.getOptions().size() + "options");
					RadioGroup radioGroup = new RadioGroup(context);
					radioGroup.setOrientation(RadioGroup.VERTICAL);
					
					//create radio buttons
					for (int j=0; j<qu.getOptions().size(); j++){
						
						RadioButton radioButton = new RadioButton (context);					
						radioButton.setText(qu.getOptions().get(j) );
						radioGroup.addView(radioButton);
						
					}
					//add the view
					ll.addView(radioGroup);

                    //if the question has the "other" field
					if (qu.hasOtherField()){
						
						RadioButton radioButton = new RadioButton (context);		
						radioButton.setText("Other");
						ll.addView(radioButton);
						
						//add other field
						EditText editText = new EditText(context);
						ll.addView(editText);
						
					}
					
				}

                /**if the question is the multichoice question with multiple answer (i.e. check box)**/
				else if (qu.getType().equals(QuestionnaireManager.QUESTION_TYPE_MULTICHOICE_MULTIPLE_ANSWER)){
	
					Log.d(LOG_TAG, "the current question type has "  + qu.getOptions().size() + "options");
					
					for (int j=0; j<qu.getOptions().size(); j++){
						
						CheckBox cb = new CheckBox (context);
						cb.setText(qu.getOptions().get(j) );
						ll.addView(cb);
						
					}
					
					if (qu.hasOtherField()){
						
						CheckBox cb = new CheckBox (context);
						cb.setText("Other");
						ll.addView(cb);
						
						EditText editText = new EditText(context);
						ll.addView(editText);
					}
					
				}

			}

            /** add the view of the submit button **/
            Button submitButton  = new Button (context);
            submitButton.setText("Submit");
            ll.addView(submitButton);


		}
		return sv;
	}
	
	private static View setUpQuestionLayout(Question question, View view){
		

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		lp.setMargins(
				QuestionnaireManager.QUESTION_LAYOUT_MARGIN_LEFT,
				QuestionnaireManager.QUESTION_LAYOUT_MARGIN_TOP,
				QuestionnaireManager.QUESTION_LAYOUT_MARGIN_RIGHT,
				QuestionnaireManager.QUESTION_LAYOUT_MARGIN_BOTTOM);
		
		view.setLayoutParams(lp);
		
		return view;

	}


    /****
     *
     *
     * For emailQuestionnaire
     *
     *
     */


    public static String getEmailQuestionnaireSubject(int questionnaireTemplateId) {

        String subject = "Subject";
        //get the questionnaire template
        EmailQuestionnaireTemplate template = (EmailQuestionnaireTemplate) QuestionnaireManager.getQuestionnaireTemplate(questionnaireTemplateId);

        if (template!=null)
            subject =template.getSubject();


        //if the subject contains $id, we need to replace it with participant Id
        if (subject.contains("$id")){
            Log.d(LOG_TAG, "we need to insert id");
            subject = subject.replace("$id", Constants.DEVICE_ID);
        }

        else if (subject.contains("$date")){
            Log.d(LOG_TAG, "we need to insert date");
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW_DAY);
            long now = ScheduleAndSampleManager.getCurrentTimeInMillis();
            subject = subject.replace("$date", ScheduleAndSampleManager.getTimeString(now, sdf));
        }

        return subject;

    }



    public static JSONArray createDataJSONContent(JSONObject dataJSON) {

        String dataStr = "";
        //JSONArray for storing sessions
        JSONArray trips = new JSONArray();

        //get data from the database
        if (dataJSON!=null){

            try {
                String dataFormat = dataJSON.getString(QUESTION_PROPERTIES_DATA_FORMAT);

                String targetData = dataJSON.getString(QUESTION_PROPERTIES_DATA);

                ArrayList<String> tableAndColumnNames = new ArrayList<String>();

                //Log.d(LOG_TAG, "[getDataFromDatabase] we need to get data" );

                if (targetData.equals("Session")) {

                    ArrayList<Session> sessions = RecordingAndAnnotateManager.getRecentSessions();

                    Log.d(LOG_TAG, "[getDataFromDatabase] we got sessiosn from database " + sessions);



                    for (int i=0; i<sessions.size(); i++) {

                        dataFormat = dataJSON.getString(QUESTION_PROPERTIES_DATA_FORMAT);
                        Session session = sessions.get(i);

                        Log.d(LOG_TAG, "[getDataFromDatabase] the "  + i + " session is " + session.getId() + " from " + ScheduleAndSampleManager.getTimeString(session.getStartTime()) + " to " +
                                ScheduleAndSampleManager.getTimeString(session.getEndTime()));

                        String label = null;

                        //replace $startTime with the timestamp of the session
                        if (dataFormat.contains(QUESTION_DATA_VARIABLE_SESSION_START_TIME)){

                            SimpleDateFormat sdf_date = new SimpleDateFormat(Constants.DATE_FORMAT_DATE_TEXT_HOUR_MIN);
                            String startTime = ScheduleAndSampleManager.getTimeString(session.getStartTime(), sdf_date) ;

                            //replace the tempalte with real value
                            dataFormat = dataFormat.replace(QUESTION_DATA_VARIABLE_SESSION_START_TIME, startTime);

                        }


                        //replace $startTime with the timestamp of the session
                        if (dataFormat.contains(QUESTION_DATA_VARIABLE_SESSION_END_TIME)){

                            SimpleDateFormat sdf_date = new SimpleDateFormat(Constants.DATE_FORMAT_DATE_TEXT_HOUR_MIN);

                            long endTimeLong = session.getEndTime();

                            //the end time of the session is updated
                            if (endTimeLong!=0){
                                String endTime = ScheduleAndSampleManager.getTimeString(session.getEndTime(), sdf_date) ;
                                //replace the tempalte with real value
                                dataFormat = dataFormat.replace(QUESTION_DATA_VARIABLE_SESSION_END_TIME, endTime);
                            }

                            //there is no end time of the session
                            else {

                                dataFormat = dataFormat.replace(QUESTION_DATA_VARIABLE_SESSION_END_TIME, "unknown");
                            }




                        }


                        //TODO: get end time

                        //replace $label with the label
                        if (dataFormat.contains(QUESTION_DATA_VARIABLE_SESSION_LABEL)){

                            AnnotationSet annotationSet = session.getAnnotationsSet();
                            if (annotationSet!=null && annotationSet.getAnnotations()!=null){

                                for (int j=0; j<annotationSet.getAnnotations().size(); j++){

                                    Annotation annotation = annotationSet.getAnnotations().get(j);

                                    if (annotation.getTags().contains("Label")){

                                        //replace the tempalte with real value
                                        label = annotation.getContent();
                                        if (label.equals("Choose an activity"))
                                            label = "unkown";

                                        dataFormat = dataFormat.replace(QUESTION_DATA_VARIABLE_SESSION_LABEL, label);
                                        //Log.d(LOG_TAG, "[getDataFromDatabase] the dataformat becomes " + dataFormat );

                                    }
                                }
                            }

                            //if the template is not replaced (ie.. there is no value to replace the template)
                            dataFormat = dataFormat.replace(QUESTION_DATA_VARIABLE_SESSION_LABEL, "unknown");
                        }

                        //                     Log.d(LOG_TAG, "[getDataFromDatabase] after replacing the dataformat becomes " + dataFormat );

                        if (QuestionnaireManager.QUESTIONNAIRE_SENT_FROM_SOURCE.equals(QuestionnaireManager.QUESTIONNAIRE_FROM_SERVER)){
                            //creating string
                            // dataStr+=(i+1)+". " + dataFormat;

                            //create json
                            JSONObject trip = new JSONObject();
                            SimpleDateFormat sdf_id = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_ID);
                            trip.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_SESSION_ID, Constants.DEVICE_ID +"-"+ScheduleAndSampleManager.getTimeString(session.getStartTime(), sdf_id) );

                            trip.put("trip_time_label", dataFormat);

                            boolean isLabelUnknown = false;
                            if (label!=null){
                                if (label.equals("unknown"))
                                    isLabelUnknown = true;
                            }
                            else
                                isLabelUnknown = true;

                            trip.put("label_unknown", isLabelUnknown);
                            trips.put(trip);
                        }

                        else
                            dataStr+=(i+1)+". " + dataFormat + "\n\n\n\n";



                        Log.d(LOG_TAG, "[getDataFromDatabase] adding dataformat the string becomes " + dataStr );

                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        Log.d(LOG_TAG, "[getDataFromDatabase] the final data content for session is  " + trips.toString() );
        return trips;
    }



    public static String createDataContent(JSONObject dataJSON) {

        String dataStr = "";
        //JSONArray for storing sessions
        JSONArray trips = new JSONArray();

        //get data from the database
        if (dataJSON!=null){

            try {
                String dataFormat = dataJSON.getString(QUESTION_PROPERTIES_DATA_FORMAT);

                String targetData = dataJSON.getString(QUESTION_PROPERTIES_DATA);

                ArrayList<String> tableAndColumnNames = new ArrayList<String>();

                //Log.d(LOG_TAG, "[getDataFromDatabase] we need to get data" );

                if (targetData.equals("Session")) {

                    ArrayList<Session> sessions = RecordingAndAnnotateManager.getRecentSessions();

                   // Log.d(LOG_TAG, "[getDataFromDatabase] we got sessiosn from database " + sessions);



                    for (int i=0; i<sessions.size(); i++) {

                        dataFormat = dataJSON.getString(QUESTION_PROPERTIES_DATA_FORMAT);
                        Session session = sessions.get(i);

                        String label = null;

                   //replace $startTime with the timestamp of the session
                        if (dataFormat.contains(QUESTION_DATA_VARIABLE_SESSION_START_TIME)){

                            SimpleDateFormat sdf_date = new SimpleDateFormat(Constants.DATE_FORMAT_DATE_TEXT_HOUR_MIN);
                            String startTime = ScheduleAndSampleManager.getTimeString(session.getStartTime(), sdf_date) ;

                            //replace the tempalte with real value
                            dataFormat = dataFormat.replace(QUESTION_DATA_VARIABLE_SESSION_START_TIME, startTime);

                        }


                        //replace $startTime with the timestamp of the session
                        if (dataFormat.contains(QUESTION_DATA_VARIABLE_SESSION_END_TIME)){

                            SimpleDateFormat sdf_date = new SimpleDateFormat(Constants.DATE_FORMAT_DATE_TEXT_HOUR_MIN);

                            long endTimeLong = session.getEndTime();

                            //the end time of the session is updated
                            if (endTimeLong!=0){
                                String endTime = ScheduleAndSampleManager.getTimeString(session.getEndTime(), sdf_date) ;
                                //replace the tempalte with real value
                                dataFormat = dataFormat.replace(QUESTION_DATA_VARIABLE_SESSION_END_TIME, endTime);
                            }

                            //there is no end time of the session
                            else {

                                dataFormat = dataFormat.replace(QUESTION_DATA_VARIABLE_SESSION_END_TIME, "unknown");
                            }




                        }


                        //TODO: get end time

                        //replace $label with the label
                        if (dataFormat.contains(QUESTION_DATA_VARIABLE_SESSION_LABEL)){

                            AnnotationSet annotationSet = session.getAnnotationsSet();
                            if (annotationSet!=null && annotationSet.getAnnotations()!=null){

                                for (int j=0; j<annotationSet.getAnnotations().size(); j++){

                                    Annotation annotation = annotationSet.getAnnotations().get(j);

                                    if (annotation.getTags().contains("Label")){

                                        //replace the tempalte with real value
                                        label = annotation.getContent();
                                        dataFormat = dataFormat.replace(QUESTION_DATA_VARIABLE_SESSION_LABEL, label);
                                        //Log.d(LOG_TAG, "[getDataFromDatabase] the dataformat becomes " + dataFormat );

                                    }
                                }
                            }

                            //if the template is not replaced (ie.. there is no value to replace the template)
                            dataFormat = dataFormat.replace(QUESTION_DATA_VARIABLE_SESSION_LABEL, "unknown");
                        }

   //                     Log.d(LOG_TAG, "[getDataFromDatabase] after replacing the dataformat becomes " + dataFormat );

                        if (QuestionnaireManager.QUESTIONNAIRE_SENT_FROM_SOURCE.equals(QuestionnaireManager.QUESTIONNAIRE_FROM_SERVER)){
                           //creating string
                           // dataStr+=(i+1)+". " + dataFormat;

                            //create json
                            JSONObject trip = new JSONObject();
                            SimpleDateFormat sdf_id = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_ID);
                            trip.put(DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_SESSION_ID, Constants.DEVICE_ID +"-"+ScheduleAndSampleManager.getTimeString(session.getStartTime(), sdf_id) );

                            trip.put("trip_time_label", dataFormat);

                            boolean isLabelUnknown = false;
                            if (label!=null){
                                if (label.equals("unknown"))
                                    isLabelUnknown = true;
                            }
                            else
                                isLabelUnknown = true;

                            trip.put("label_unknown", isLabelUnknown);
                            trips.put(trip);
                        }

                        else
                            dataStr+=(i+1)+". " + dataFormat + "\n\n\n\n";



                        Log.d(LOG_TAG, "[getDataFromDatabase] adding dataformat the string becomes " + dataStr );

                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        Log.d(LOG_TAG, "[getDataFromDatabase] the final data content for session is  " + trips.toString() );
        return trips.toString();
    }


    //TODO
    public static void getEmailQuestionnaireContentInHtml(int questionnaireTemplateId) {




    }



    //generate the content of the emailQuestionnaire
    public static String getEmailQuestionnaireContent(int questionnaireTemplateId) {

        //get the questionnaire template
        EmailQuestionnaireTemplate template = (EmailQuestionnaireTemplate) QuestionnaireManager.getQuestionnaireTemplate(questionnaireTemplateId);

        String content="";

        if (template!=null){

            String title = template.getTitle();
            String description = template.getDescription();
            //Log.d(LOG_TAG, "[getEmailQuestionnaireContent] we use the template " + questionnaireTemplateId + " the title is " + title);



            //get questions from the template
            ArrayList<Question> questions = template.getQuestions();

            for (int i=0; i<questions.size(); i++) {

                Question qu = questions.get(i);

                String questionText = qu.getText();

                if (qu.getType().equals(QuestionnaireManager.QUESTION_TYPE_DESCRIPTION)){

                    content += questionText+"\n\n";

                    if (qu.getDataJSON()!=null){

                        String dataStr = createDataContent(qu.getDataJSON());
                        content += dataStr + "\n\n\n";

                    }

                }
                else if (qu.getType().equals(QuestionnaireManager.QUESTION_TYPE_TEXT)){

                    content += questionText+"\n\n\n\n\n\n";

                    if (qu.getDataJSON()!=null){

                        Log.d(LOG_TAG, "we need to insert data here");

                    }

                }

                else if (qu.getType().equals(QuestionnaireManager.QUESTION_TYPE_MULTICHOICE_MULTIPLE_ANSWER) ||
                    qu.getType().equals(QuestionnaireManager.QUESTION_TYPE_MULTICHOICE_ONE_ANSWER ) ){

                    content += questionText+"\n\n";

                    for (int j=0; j<qu.getOptions().size(); j++){

                        String option = qu.getOptions().get(j);
                        content += option +"\n";
                    }

                    if (qu.getDataJSON()!=null){

                        Log.d(LOG_TAG, "we need to insert data here");

                    }

                }

            }


        }

        return content;
    }

    public static String[] getEmailQuestionnaireRecipients(int questionnaireTemplateId) {

        //get the questionnaire template
        EmailQuestionnaireTemplate template = (EmailQuestionnaireTemplate) QuestionnaireManager.getQuestionnaireTemplate(questionnaireTemplateId);
        String [] recipients= template.getRecipients();

        return recipients;
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
	
	
}

