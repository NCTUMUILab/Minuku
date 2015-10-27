package edu.umich.si.inteco.minuku.model.actions;

import java.util.ArrayList;

public class MonitoringCircumstanceAction extends Action {

	
	private int _questionnaire_id = -1;
	private ArrayList<Integer> mMonitoredEventIds;
	
	public MonitoringCircumstanceAction(int id, String name, String type, String executionStyle, int study_id){
		super(id, name, type, executionStyle, study_id);
	}


	public void addMonitoredEvent (int id){
		
		if (mMonitoredEventIds==null){
			mMonitoredEventIds = new ArrayList<Integer> ();
		}
		
		mMonitoredEventIds.add(id);
	}
	
	public ArrayList<Integer> getMonitoredEventIds (){
		
		if (mMonitoredEventIds==null){
			mMonitoredEventIds = new ArrayList<Integer> ();
		}
		
		return mMonitoredEventIds;
	}
	
	
	

}
