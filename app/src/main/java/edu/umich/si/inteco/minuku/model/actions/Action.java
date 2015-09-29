package edu.umich.si.inteco.minuku.model.actions;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.model.Notification;
import edu.umich.si.inteco.minuku.model.ProbeObject;
import edu.umich.si.inteco.minuku.model.ProbeObjectControl.ActionControl;
import edu.umich.si.inteco.minuku.util.TriggerManager;

public class Action extends ProbeObject{

private static final String LOG_TAG = "Action";
	

	/**required field**/
	protected String mType;
	protected String mName;
	protected String mExecutionStyle ;
	protected long mLastExecutionTime = -1;
	protected boolean mPaused = false;
    protected int mCount = 0;
	
	//JSON String
	private String mControlJSON = "NA";
	private String mScheduleJSON="NA";
	private String mContentJSON="";

	
	//the action can be continous or not. if an action is continuous, it also has the property of "Rate" and "Duration"
	private boolean mContinuous = false;
	private float mContinuousActionRate = -1;
	private int mContinuousActionDuration = -1;

	
	/**Action Control**/
	private ArrayList<ActionControl> mActionControls;
	private ArrayList<Notification> mNotifications;

	public Action (){
		super();
		_class = TriggerManager.PROBE_OBJECT_CLASS_ACTION;
	}
	
	public Action(int id, String name, String type, String executionStyle,  int study_id){
		super();
		_id = id;
		mName = name;
		mType = type;
		mExecutionStyle = executionStyle;
		_studyId=  study_id;
		_class = TriggerManager.PROBE_OBJECT_CLASS_ACTION;
	}
	
	//for actions that is triggered by an Event
	public Action(int id, String type){
		super();
		_id = id;
		mType = type;	
		_class = TriggerManager.PROBE_OBJECT_CLASS_ACTION;
	}


	public String getType(){
		return mType;
	} 
	
	public void setType(String t){
		mType = t;
	}
	
	public String getName(){
		return mName;
	} 
	
	public void setName(String name){
		mName = name;
	}

    public int getExecutionCount() {
        return mCount;
    }

    public void addExecutionCount() {
        this.mCount = mCount + 1;
    }

    public void setScheduleJSON(String scheduleJSON){
    	mScheduleJSON = scheduleJSON;
    }
    
    public String getScheduleJSON(){
    	return mScheduleJSON;
    }
    
    public void setControlJSON(String controlJSON){
    	mControlJSON = controlJSON;
    }
    
    public String getControlJSON(){
    	return mControlJSON;
    }
    
    
    public void setContentJSON(String contentJSON){
    	mContentJSON = contentJSON;
    }
    
    public String getContentJSON(){
    	return mContentJSON;
    }
    
    public String getExecutionStyle(){
    	return mExecutionStyle;
    }
    
    public void setExecutionStyle(String executionStyle){
    	mExecutionStyle = executionStyle;
    }
    
    public ArrayList<Notification> getNotifications(){

        if (mNotifications==null){
            mNotifications = new ArrayList<Notification>();
        }
    	return mNotifications;
    }

    public void addNotification (Notification notification){
        if (mNotifications==null){
            mNotifications = new ArrayList<Notification>();
        }

        mNotifications.add(notification);
    }

    public void setNotifications(ArrayList<Notification> notifications){
    	
    	mNotifications = notifications;
    }
    
    /** For contiunous action**/
    public boolean isContinuous () {
    	   	
    	return mContinuous;
    }
    
    public void setContinuous (boolean continuous) {  	
    	this.mContinuous = continuous;     	
    }
    
    /**only for continuous actions **/
    public void setActionRate (float rate) {    	
    	this.mContinuousActionRate = rate;    	
    }
   
    
    public float getActionRate(){
    	return mContinuousActionRate;
    }
    
    public void setActionControl (ArrayList<ActionControl> action_controls) {
    
    	mActionControls = action_controls;
    }
    

    public ArrayList<ActionControl> getActionControls(){
    	return mActionControls;
    }
    
    public void addActionControl (ActionControl action_control) {
    	
    	if (mActionControls==null){
    		mActionControls = new ArrayList<ActionControl>();
    	}
    	
    	mActionControls.add(action_control);

    }
    
    
    public int getActionDuration () {
    	return mContinuousActionDuration;
    }
    
    public void setActionDuration (int duration) {
    	this.mContinuousActionDuration = duration;
    }
    

    public boolean hasNotification (){
    	
    	if (mNotifications!=null && mNotifications.size()>0)
    		return true;
    	else 
    		return false;
    	
    }

    public long getLastExecutionTime(){
    	return mLastExecutionTime;
    }
    
    public void setLastExecutionTime(long time){
    	this.mLastExecutionTime = time;
    }
    
    public void setPaused(boolean flag){
    	mPaused = flag;
    }
    
    public boolean isPaused(){
    	return mPaused;
    }


}
