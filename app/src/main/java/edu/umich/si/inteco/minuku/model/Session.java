package edu.umich.si.inteco.minuku.model;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.contextmanager.ContextManager;

public class Session {

	private long mStartTime=0;
    private long mEndtime=0;
	private int mId;
	private int mTaskId;
    private boolean mPaused=false;
    private float mBatteryLife = -1;
    //we need to rememeber this number in order to cancel the ongoing notification when the current session is done.
    private int mOngoingNotificationId=-1;
	protected AnnotationSet mAnnotationSet;
    //TODO: this should be defined based on what's being activated.
    ArrayList<Integer> mRecordTypes= ContextManager.RECORD_TYPE_LIST;

	public Session (int taskId){
        mTaskId = taskId;
        mAnnotationSet = new AnnotationSet();
	}

    public Session (long timestamp){
        mStartTime = timestamp;
        mAnnotationSet = new AnnotationSet();
    }

	public Session (long timestamp, int taskId){		
		mStartTime = timestamp;
		mTaskId = taskId;
        mAnnotationSet = new AnnotationSet();
	}

    public Session (int id, long timestamp, int taskId){
        mId = id;
        mStartTime = timestamp;
        mTaskId = taskId;
        mAnnotationSet = new AnnotationSet();
    }

    public ArrayList<Integer> getRecordTypes() {
        return mRecordTypes;
    }

    public void setRecordTypes(ArrayList<Integer> recordTypes) {
        this.mRecordTypes = recordTypes;
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void setPaused(boolean paused) {
        this.mPaused = paused;
    }

    public void setId(int id){
		mId = id;
	}

	public long getId(){
		return mId;
	}

    public int getOngoingNotificationId() {
        return mOngoingNotificationId;
    }

    public void setOngoingNotificationId(int ongoingNotificationId) {
        this.mOngoingNotificationId = ongoingNotificationId;
    }

    public void setTask(int taskId) {
        mTaskId = taskId;
    }

    public void setTaskId(int taskId) {
        this.mTaskId = taskId;
    }


	public int getTaskId(){
		return mTaskId;
	}
	
	
	public void setStartTime(long t){
		mStartTime = t;
	}

	public long getStartTime(){
		return mStartTime;
	}

    public long getEndTime() {
        return mEndtime;
    }

    public void setEndTime(long endtime) {
        this.mEndtime = endtime;
    }

    public AnnotationSet getAnnotationsSet(){

		return mAnnotationSet;
	}

    public float getBatteryLife() {
        return mBatteryLife;
    }

    public void setBatteryLife(float batteryStatus) {
        this.mBatteryLife = batteryStatus;
    }

    public void setAnnotationSet(AnnotationSet annotationSet){
		
		mAnnotationSet = annotationSet;
	}

    public void addAnnotaiton (Annotation annotation) {

        if (mAnnotationSet==null){
            mAnnotationSet = new AnnotationSet();
        }
        mAnnotationSet.addAnnotation(annotation);

    }

	
}
