package edu.umich.si.inteco.minuku.model;

import org.json.JSONObject;

import java.util.ArrayList;

public class Question {

	private int mIndex = -1;
	private String mText=null;
	private String mResponse=null; 
	private String mType=null;
	private boolean mHasOtherFields = false;
    private JSONObject mDataJSON=null;
	
	private ArrayList<String> mOptions;
	
	//this is a copy-constructor
	public Question (Question q) {
		
		this.mIndex = q.getIndex();
		this.mText = q.getText();
		this.mType = q.getType();
		this.mHasOtherFields = q.hasOtherField();
		this.mOptions = q.getOptions();
	}
	
	public Question (int index, String questionmText, String type){
		mIndex = index;
		mText = questionmText;
		mType = type;
	}
	
	public Question (String text){
		mText = text;
	}
	
	public void setIndex (int index){
		
		mIndex = index;
	}
	
	public int getIndex(){
		
		return mIndex;
	}
	
	public String getText (){
		return mText;
	}
	
	public String getResponse(){
		return mResponse;
	}
	
	public String getType(){
		return mType;
	}
	
	public void setType(String type){
		
		mType = type;
	}
	
	public void setOptions(ArrayList<String> options){		
		mOptions  = options;		
	}
	
	public ArrayList<String> getOptions(){
		
		return mOptions;
	}
	
	public void addOptions(String option) {
		
		if (mOptions==null){
			mOptions  = new ArrayList<String>();
		}
		mOptions.add(option);
	}
	
	public void setHasOtherField(boolean flag){
		mHasOtherFields = flag;
	}
	
	public boolean hasOtherField(){
		return mHasOtherFields;
	}

    //the question needs to attach some data from the database
    public JSONObject getDataJSON() {
        return mDataJSON;
    }

    public void setDataJSON(JSONObject dataJSON) {
        this.mDataJSON = dataJSON;
    }
}
