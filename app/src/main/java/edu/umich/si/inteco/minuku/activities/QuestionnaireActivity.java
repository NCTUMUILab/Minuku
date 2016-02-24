package edu.umich.si.inteco.minuku.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.data.LocalDBHelper;
import edu.umich.si.inteco.minuku.model.Log.ProbeLog;
import edu.umich.si.inteco.minuku.model.Question;
import edu.umich.si.inteco.minuku.model.Questionnaire.Questionnaire;
import edu.umich.si.inteco.minuku.model.Views.MinukuCheckBox;
import edu.umich.si.inteco.minuku.model.Views.MinukuEditText;
import edu.umich.si.inteco.minuku.model.Views.MinukuRadioGroup;
import edu.umich.si.inteco.minuku.model.Views.MinukuSubmitButton;
import edu.umich.si.inteco.minuku.model.Views.MinukuTextView;
import edu.umich.si.inteco.minuku.util.DatabaseNameManager;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.QuestionnaireManager;

public class QuestionnaireActivity extends Activity {

	private static final String LOG_TAG = " QuestionnaireActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		int questionnaire_id = bundle.getInt(QuestionnaireManager.QUESTIONNAIRE_PROPERTIES_ID); 
		
		//write questionnaire attendence to system log
		ProbeLog systemLog = new ProbeLog(
    			LogManager.LOG_TYPE_SYSTEM_LOG,
    			LogManager.LOG_TAG_QUESTIONNAIRE_NOTI_ATTENDED,
    			ContextManager.getCurrentTimeInMillis(),
				ContextManager.getCurrentTimeString(),
    			LogManager.LOG_MESSAGE_QUESTIONNAIRE_NOTI_ATTENDED + ":" +  questionnaire_id
    			);
    	   	
    	
    	LogManager.writeLogToFile(systemLog);
		
		//setup layout based on the questionnaire
		ScrollView sv = setUpQuestionnaireView (questionnaire_id);
		setContentView(sv);
		
	}
	
	private ScrollView setUpQuestionnaireView(int questionnaire_id){
		
		/**1. the questionnaire is scollable, we setup the questionnaire based on the questionnaire template**/
		ScrollView sv = QuestionnaireManager.getQuestionnaireView(this, questionnaire_id);

		final LinearLayout ll = (LinearLayout)sv.getChildAt(0);

		/**2 then we add listeners to the submit button to get responses **/
		//the submit button is the last child in the linear layout
		MinukuSubmitButton submitButton = (MinukuSubmitButton)ll.getChildAt(ll.getChildCount()-1);

		Log.d(LOG_TAG, "[test qu] the submit button is associtated with questionnaire "  + submitButton.getQuestionnaire().getId()
		 + submitButton.getQuestionnaire().getDescription());


		//add a submit button click listener to collect response from the questionnaire
		submitButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						MinukuSubmitButton button = (MinukuSubmitButton) v;
						Questionnaire questionnaire = button.getQuestionnaire();

						//collect responses from the questions
//						Log.d(LOG_TAG, "[test qu] the submit button is associtated with questionnaire "  +questionnaire.getId()
//								+ questionnaire.getDescription() + " has " + questionnaire.getQuestionCount() + " questions");

						//get all views in the layout
						for (int i=0; i<ll.getChildCount(); i++) {

							if (ll.getChildAt(i) instanceof MinukuTextView){

//								Log.d(LOG_TAG, "[test qu] the question is " + ((TextView) ll.getChildAt(i)).getText());
							}

							//QUESTION_TYPE_MULTICHOICE_ONE_ANSWER
							else if (ll.getChildAt(i) instanceof MinukuRadioGroup){

								MinukuRadioGroup radioGroup = (MinukuRadioGroup) ll.getChildAt(i);

								int radioButtonCheckedId = radioGroup.getCheckedRadioButtonId();

								//get the quesiton text
								RadioButton radioButton = (RadioButton) radioGroup.findViewById(radioButtonCheckedId);

								Question question = questionnaire.getQuestion(radioGroup.getQuesitonIndex());

								//set response for one choice
								if (question!=null){
									question.setResponse(radioButton.getText().toString());
//									Log.d(LOG_TAG, "[test qu] user choose " + radioButton.getText() + " for question "
//											+ " " + radioGroup.getQuesitonIndex() + ". " + questionnaire.getQuestion(radioGroup.getQuesitonIndex()).getText()
//											+ " response : " + questionnaire.getQuestion(radioGroup.getQuesitonIndex()).getResponse());
//

								}



							}

							//QUESTION_TYPE_MULTICHOICE_MULTIPLE_ANSWER
							else if (ll.getChildAt(i) instanceof MinukuCheckBox) {

								//check if the checkbox is checked
								MinukuCheckBox checkBox = (MinukuCheckBox)ll.getChildAt(i);

								//we only collect responses of the box being checked
								if (checkBox.isChecked()){

									Question question = questionnaire.getQuestion(checkBox.getQuesitonIndex());

									if (question!=null) {

										//if there's not been a response, we set response
										if (questionnaire.getQuestion(checkBox.getQuesitonIndex()).getResponse()==null){
											//set response for one choice
											questionnaire.getQuestion(checkBox.getQuesitonIndex()).setResponse(checkBox.getText().toString());
										}
										//we append response if there's been responses
										else {
											questionnaire.getQuestion(checkBox.getQuesitonIndex()).appendReponse(checkBox.getText().toString());
										}
//
//										Log.d(LOG_TAG, "[test qu] user check " + checkBox.getText() + " for question " +
//												checkBox.getQuesitonIndex() + ". " + questionnaire.getQuestion(checkBox.getQuesitonIndex()).getText()
//												+ " response : " + questionnaire.getQuestion(checkBox.getQuesitonIndex()).getResponse() );


									}

								}


							}

							else if (ll.getChildAt(i) instanceof MinukuEditText) {

								MinukuEditText editText = (MinukuEditText)ll.getChildAt(i);

								//we need to see whether the editText is the Other field for other types of question
								Question question = questionnaire.getQuestion(editText.getQuesitonIndex());

								if (question!=null) {

									//Other field of MULTICHOICE_MULTIPLE_ANSWER
									if (question.getType().equals(QuestionnaireManager.QUESTION_TYPE_MULTICHOICE_MULTIPLE_ANSWER)){

										question.appendReponse(editText.getText().toString());
									}
									else if (question.getType().equals(QuestionnaireManager.QUESTION_TYPE_MULTICHOICE_ONE_ANSWER)){

										question.appendReponse(editText.getText().toString());
									}

									else if (question.getType().equals(QuestionnaireManager.QUESTION_TYPE_TEXT)){

										question.setResponse(editText.getText().toString());

									}
//
//									Log.d(LOG_TAG, "[test qu] user type  " + editText.getText() + " for question " +
//											editText.getQuesitonIndex() + ". " + questionnaire.getQuestion(editText.getQuesitonIndex()).getText()
//											+ " response : " + question.getResponse() );

								}


							}


						}

						/**3 we save the respones to the questionnaire **/

						//save submitted time
						questionnaire.setSubmitted(true);
						questionnaire.setSubmittedTime(ContextManager.getCurrentTimeInMillis());
						JSONArray responses = new JSONArray();

						for (int i=0; i<questionnaire.getQuestions().size() ;i++) {

							JSONObject response = new JSONObject();
							Question question = questionnaire.getQuestions().get(i);
//							Log.d(LOG_TAG, "test qu getting  question " + question);
//							Log.d(LOG_TAG, "test qu getting test responses from question " + question.getIndex() + " is " + question.getResponse());

							try{
								response.put(question.getIndex()+"", question.getResponse() );
								responses.put(response);
							}catch(Exception e){
								e.printStackTrace();
							}
						}

						questionnaire.setResponses(responses);
						Log.d(LOG_TAG, "test qu getting questionnaire response : " + questionnaire.getResponses());

						//save to the db
						LocalDBHelper.updateQuestionnaireResponseTable(questionnaire, DatabaseNameManager.QUESTIONNAIRE_TABLE_NAME);

						//after answering the questionnaire, finish the app.
						finish();

//						ArrayList<String> results = LocalDBHelper.queryQuestionnaire(questionnaire.getId());
//						Log.d(LOG_TAG, "test qu after query the qeustionnaire is  " + results);

					}
				});





		return sv;
				
	}
	
	private View setUpQuestionLayout(Question question, View view){
		

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
	
	
}
