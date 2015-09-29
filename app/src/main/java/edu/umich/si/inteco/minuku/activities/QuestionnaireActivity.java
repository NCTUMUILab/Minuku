package edu.umich.si.inteco.minuku.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import edu.umich.si.inteco.minuku.contextmanager.ContextExtractor;
import edu.umich.si.inteco.minuku.model.Log.ProbeLog;
import edu.umich.si.inteco.minuku.model.Question;
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
    			ContextExtractor.getCurrentTimeInMillis(),
    			ContextExtractor.getCurrentTimeString(), 
    			LogManager.LOG_MESSAGE_QUESTIONNAIRE_NOTI_ATTENDED + ":" +  questionnaire_id
    			);
    	   	
    	
    	LogManager.writeLogToFile(systemLog);
		
		//setup layout based on the questionnaire
		ScrollView sv = setUpQuestionnaireView (questionnaire_id);
		setContentView(sv);
		
	}
	
	private ScrollView setUpQuestionnaireView(int questionnaire_id){
		
		//the questionnaire is scollable	
		ScrollView sv = QuestionnaireManager.getQuestionnaireView(this, questionnaire_id);
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
