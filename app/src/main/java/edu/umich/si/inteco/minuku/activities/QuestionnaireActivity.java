package edu.umich.si.inteco.minuku.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.Log.ProbeLog;
import edu.umich.si.inteco.minuku.model.Question;
import edu.umich.si.inteco.minuku.model.Views.MinukuCheckBox;
import edu.umich.si.inteco.minuku.model.Views.MinukuEditText;
import edu.umich.si.inteco.minuku.model.Views.MinukuRadioGroup;
import edu.umich.si.inteco.minuku.model.Views.MinukuSubmitButton;
import edu.umich.si.inteco.minuku.model.Views.MinukuTextView;
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


		/**2 then we add listeners to the submit button to get responses **/

		/**2.1 get all the components in the questionnaire first**/
		LinearLayout ll = (LinearLayout)sv.getChildAt(0);

		//get all views in the layout
		for (int i=0; i<ll.getChildCount(); i++) {

			if (ll.getChildAt(i) instanceof MinukuTextView){

				Log.d(LOG_TAG, "the question is " + ((TextView) ll.getChildAt(i)).getText());
			}

			//QUESTION_TYPE_MULTICHOICE_ONE_ANSWER
			else if (ll.getChildAt(i) instanceof MinukuRadioGroup){

				Log.d(LOG_TAG, "the radio group has " + ((RadioGroup) ll.getChildAt(i)).getChildCount() + " buttons" );
			}

			//QUESTION_TYPE_MULTICHOICE_MULTIPLE_ANSWER
			else if (ll.getChildAt(i) instanceof MinukuCheckBox) {

				//check if the checkbox is checked
				MinukuCheckBox checkBox = (MinukuCheckBox)ll.getChildAt(i);

//				Log.d(LOG_TAG, "[test qu] minukucheckbox text is " + checkBox.getText() + " belong to question " +
//						checkBox.getQuesitonIndex());

			}

			else if (ll.getChildAt(i) instanceof MinukuEditText) {

				//check if the checkbox is checked
				MinukuEditText editText = (MinukuEditText)ll.getChildAt(i);

//				Log.d(LOG_TAG, "[test qu] minuku edit text is " + editText.getText() + " belong to question " +
//						editText.getQuesitonIndex());

			}

			//submit button
			else if (ll.getChildAt(i) instanceof Button) {

				MinukuSubmitButton submitButton = (MinukuSubmitButton)ll.getChildAt(i);

//				Log.d(LOG_TAG, "[test qu] the submit button is associtated with questionnaire "  + submitButton.getQuestionnaire().getId()
//				 + submitButton.getQuestionnaire().getDescription());

				//add a submit button click listener to add response to the questionnaire
				submitButton.setOnClickListener(
						new View.OnClickListener() {
							@Override
							public void onClick(View v) {

								MinukuSubmitButton button = (MinukuSubmitButton) v;
								//collect responses from the questions
								Log.d(LOG_TAG, "[test qu] the submit button is associtated with questionnaire "  + button.getQuestionnaire().getId()
										+ button.getQuestionnaire().getDescription());


							}
						});

			}


		}







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
