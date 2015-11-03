package edu.umich.si.inteco.minuku.model.actions;

import android.util.Log;

import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.ConfigurationManager;
import edu.umich.si.inteco.minuku.util.RecordingAndAnnotateManager;

/**
 * Created by Armuro on 6/17/14.
 */
public class AnnotateRecordingAction extends  Action{

    //Mode:  1: manual, 2: auto. By default the mode is manual
    private String mMode = RecordingAndAnnotateManager.ANNOTATE_MODE_MANUAL;
    //Recording Type: 1: new recording, 2: backgroudn recording. By default the recording type is new recording
    private String mRecordingType = RecordingAndAnnotateManager.ANNOTATE_RECORDING_NEW;
    private String mVizType = ConfigurationManager.ACTION_PROPERTIES_VIZUALIZATION_TYPE;
    private boolean mAllowAnnotationInProcess = false;
    private String mReviewRecordingMode = RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_NONE;
    private boolean mRecordingStartByUser  = false;

    private SavingRecordAction mSavingRecordAction;


    public AnnotateRecordingAction(int id,
                                   String name,
                                   String type,
                                   String executionStyle,
                                   int studyId,
                                   String mode,
                                   String recordingType,
                                   String vizType,
                                   boolean allowAnnotateInProcess,
                                   String reviewRecordingMode,
                                   boolean startByUser) {

        super(id, name, type, executionStyle, studyId);
        this.mMode = mode;
        this.mRecordingType = recordingType;
        this.mVizType = vizType;
        this.mAllowAnnotationInProcess = allowAnnotateInProcess;
        this.mReviewRecordingMode = reviewRecordingMode;
        this.mRecordingStartByUser  = startByUser;
    }

    public boolean isRecordingStartByUser() {
        return mRecordingStartByUser;
    }

    public void setRecordingStartByUser(boolean recordingStartByUser) {
        this.mRecordingStartByUser = recordingStartByUser;
    }

    public String getReviewRecordingMode() {
        return mReviewRecordingMode;
    }

    public void setReviewRecordingMode(String reviewRecordingMode) {
        this.mReviewRecordingMode = reviewRecordingMode;
    }

    public String getRecordingType() {
        return mRecordingType;
    }

    public void setRecordingType(String mRecordingType) {
        this.mRecordingType = mRecordingType;
    }

    public String getMode() {

        return mMode;
    }

    public boolean isAllowAnnotationInProcess() {
        return mAllowAnnotationInProcess;
    }

    public void setAllowAnnotationInProcess(boolean allowAnnotationInProcess) {
        this.mAllowAnnotationInProcess = allowAnnotationInProcess;
    }

    public void setMode(String mMode) {
        this.mMode = mMode;
    }

    public SavingRecordAction getAssociatedSavingRecordAction() {
        Log.d("AnnotateRecordingAction", "getting the savingRecordingAction " + this );
        Log.d("AnnotateRecordingAction", " getting the savingRecordingAction " +mSavingRecordAction + " to annotateAction "  + this.getId() + ":" + this.getName()   );
        return mSavingRecordAction;
    }

    public void setAssociatedSavingRecordAction(SavingRecordAction savingRecordAction) {
        Log.d("AnnotateRecordingAction", " setting the savingRecordingAction " + savingRecordAction + " to annotateAction "  + this + " " + this.getId() + ":" + this.getName()   );
        this.mSavingRecordAction = savingRecordAction;
    }
}

