package edu.umich.si.inteco.minuku.model;

import org.json.JSONArray;

public class UserResponse{

	private int _id;
	private int _questionnaireId;
	private int _studyId;
	private long _startTime;
	private long _endTime;
	private JSONArray _content; 
	
	public UserResponse() {
		
	}
	
	public void setId(int id){
		_id = id;
	}

	public long getId(){
		return _id;
	}

	public void setStartTime(long t){
		_startTime = t;
	}

	public long getStartTime(){
		return _startTime;
	}
	
	public void setEndTime(long t){
		_startTime = t;
	}

	public long getEndTime(){
		return _endTime;
	}
	
	public int getQuestionnaireId (){
		return _questionnaireId;
	}
	
	public void setQuestionnaireId (int id){
		_questionnaireId = id;
	}
	
	public int getStudyId (){
		return _studyId;
	}
	
	public void setStudyId (int id){
		_studyId = id;
	}
	
	public JSONArray getContent() {
		return _content;
	}
	
	public void setContent(JSONArray content){
		_content = content;
	}
	
	
	
}
