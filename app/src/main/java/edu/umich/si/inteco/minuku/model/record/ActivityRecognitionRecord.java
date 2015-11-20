package edu.umich.si.inteco.minuku.model.Record;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

import edu.umich.si.inteco.minuku.context.ContextStateManagers.ActivityRecognitionManager;

/**
 * Created by Armuro on 11/20/15.
 */
public class ActivityRecognitionRecord extends Record{


    List<DetectedActivity> mProbableActivities;

    public ActivityRecognitionRecord() {
        super();
        _source = ActivityRecognitionManager.CONTEXT_SOURCE_ACTIVITY_RECOGNITION;
    }

    public void setProbableActivities(List<DetectedActivity> activities) {
        mProbableActivities = activities;
    }

    public List<DetectedActivity> getProbableActivities() {
        return mProbableActivities;
    }
}
