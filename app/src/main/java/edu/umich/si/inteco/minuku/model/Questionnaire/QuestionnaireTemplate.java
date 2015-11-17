package edu.umich.si.inteco.minuku.model.Questionnaire;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.model.ProbeObject;
import edu.umich.si.inteco.minuku.model.Question;
import edu.umich.si.inteco.minuku.util.QuestionnaireManager;

public class QuestionnaireTemplate extends ProbeObject {

	private String mTitle = "Questionnaire";
	private String mDescription = "Please answer the following questions";
	private String mQuestionJSON;
    private String mType = QuestionnaireManager.QUESTIONNAIRE_TYPE_ACTIVITY;
	private ArrayList<Question> mQuestionList;
    

	
	public QuestionnaireTemplate (){
		
	}
	
	public QuestionnaireTemplate (int id, String title, int study_id, String type){
		
		mQuestionList = new ArrayList<Question>();
		_id = id;
		mTitle = title;
		_studyId = study_id;
        mType = type;

	}
	
	public ArrayList<Question> getQuestions(){
		return mQuestionList;
	}
	
	public String getQuestionsJSON(){
		return mQuestionJSON;
		
	}
	
	public void setQuestionsJSON(String json){
		
		mQuestionJSON = json;
	}

	public void setQuestions(ArrayList<Question> questions){
		
		mQuestionList = questions;
	}
	
	public void addQuestion(Question question) {
		mQuestionList.add(question);
	}

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public void setTitle(String title){
		mTitle = title;
	}
	
	public String getTitle(){
		return mTitle;
	}
	
	public void setDescription(String description){
		mDescription = description;
	}
	
	public String getDescription(){
		return mDescription;
	}

	@Override
	public String toString() {
		return "QuestionnaireTemplate{" +
				"mTitle='" + mTitle + '\'' +
				", mDescription='" + mDescription + '\'' +
				", mQuestionJSON='" + mQuestionJSON + '\'' +
				", mType='" + mType + '\'' +
				", mQuestionList=" + mQuestionList +
				'}';
	}
}
