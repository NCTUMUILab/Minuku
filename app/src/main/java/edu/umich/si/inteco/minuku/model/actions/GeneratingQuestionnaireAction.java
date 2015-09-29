package edu.umich.si.inteco.minuku.model.actions;

public class GeneratingQuestionnaireAction extends Action{


	private int _questionnaire_id = -1;
	
	public GeneratingQuestionnaireAction(int id, String name, String type,  String executionStyle, int study_id){
		super(id, name, type, executionStyle, study_id);
	}
	
	public int getQuestionnaireId(){
		
		return _questionnaire_id ;
	}
	
	public void setQuestionnaireId(int id){
		_questionnaire_id  = id;
	}
	
	
	
}
