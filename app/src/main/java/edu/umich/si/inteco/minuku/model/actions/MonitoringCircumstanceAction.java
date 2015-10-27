package edu.umich.si.inteco.minuku.model.actions;

import java.util.ArrayList;

public class MonitoringCircumstanceAction extends Action {

	
	private int _questionnaire_id = -1;
	private ArrayList<Integer> mMonitoredCircumstanceIds;
	
	public MonitoringCircumstanceAction(int id, String name, String type, String executionStyle, int study_id){
		super(id, name, type, executionStyle, study_id);
	}


	public void addMonitoredCircumstance (int id){
		
		if (mMonitoredCircumstanceIds==null){
			mMonitoredCircumstanceIds = new ArrayList<Integer> ();
		}
		
		mMonitoredCircumstanceIds.add(id);
	}
	
	public ArrayList<Integer> getMonitoredCircumstanceIds (){
		
		if (mMonitoredCircumstanceIds==null){
			mMonitoredCircumstanceIds = new ArrayList<Integer> ();
		}
		
		return mMonitoredCircumstanceIds;
	}
	
	
	

}
