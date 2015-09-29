package edu.umich.si.inteco.minuku.model.actions;

public class SavingRecordAction extends Action {

	private int mSessionId = -1;
	private int mTaskId = -1;

    //the flag indicating whether the current recording allows users to annotate through ongoing notifications
    private boolean mAllowAnnotationInProcess = false;
	
	public SavingRecordAction (int id, String name, String type,  String executionStyle, int study_id) {
		super(id, name, type, executionStyle, study_id);
	}
	

	public int getSessionId () {
		
		return mSessionId;
	}
	
	public void setSessionId(int sessionId){
		
		mSessionId = sessionId;
	}
	
	public int getTaskId () {
		
		return mTaskId;
	}
	
	public void setTaskId(int taskId){
		
		mTaskId = taskId;
	}

    public boolean isAllowAnnotationInProcess() {
        return mAllowAnnotationInProcess;
    }

    public void setAllowAnnotationInProcess(boolean allowAnnotationInProcess) {
        this.mAllowAnnotationInProcess = allowAnnotationInProcess;
    }
}

