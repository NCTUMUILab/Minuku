package edu.umich.si.inteco.minuku.model.record;

import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import edu.umich.si.inteco.minuku.context.ContextStateManagers.ActivityRecognitionManager;
import edu.umich.si.inteco.minuku.context.ContextManager;

public class ActivityRecord extends Record {

	
	
	
	/** Tag for logging. */
    private static final String LOG_TAG = "ActivityRecord";
    
    private List<DetectedActivity> mProbableActivities;
    private long mDetectionTime = 0;
    
	public ActivityRecord(){
		this._type = ContextManager.CONTEXT_RECORD_TYPE_ACTIVITY;
	}
	
	public void setProbableActivities (List<DetectedActivity> l){
		mProbableActivities = l;
	}

    public void setDetectionTime(long detectionTime){

        this.mDetectionTime = detectionTime;
    }

    public long getDetectionTime() {

        return mDetectionTime;
    }
	public List<DetectedActivity> getProbableActivities (){
		
		return mProbableActivities;
	}
	
	@Override
    public String toString() {
		
		String s = "";
		
		s+= this.getTimeString() + "\t" + 
			this.getTimestamp() +  "\t"	+
			this.getType() +"\t" ;

		for (int i=0; i<mProbableActivities.size();i++){
			//Log.d(LOG_TAG, "ActivityRecord" + ContextExtractor.getActivityNameFromType(mProbableActivities.get(i).getType()) + "\t" + mProbableActivities.get(i).getConfidence());
			s+= ActivityRecognitionManager.getActivityNameFromType(mProbableActivities.get(i).getType()) + "\t" + mProbableActivities.get(i).getConfidence();
        }
		
		s+="\n";
        
        return s;
    }

}
