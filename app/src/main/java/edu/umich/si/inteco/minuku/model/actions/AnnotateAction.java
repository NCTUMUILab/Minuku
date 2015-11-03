package edu.umich.si.inteco.minuku.model.actions;

import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.ConfigurationManager;
import edu.umich.si.inteco.minuku.util.RecordingAndAnnotateManager;

/**
 * Created by Armuro on 7/12/14.
 */
public class AnnotateAction extends Action{

    //Mode:  1: manual, 2: auto. By default the mode is manual
    private String mMode = RecordingAndAnnotateManager.ANNOTATE_MODE_MANUAL;
    private String mVizType = ConfigurationManager.ACTION_PROPERTIES_VIZUALIZATION_TYPE;
    private String mReviewRecordingMode = RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_NONE;

    public AnnotateAction (int id, String name, String type,  String executionStyle, int study_id) {
        super(id, name, type, executionStyle, study_id);
    }

    public AnnotateAction (int id, String name, String type,  String executionStyle, int study_id, String mode, String vizType, String reviewMode) {
        super(id, name, type, executionStyle, study_id);
        this.mMode = mode;
        this.mVizType = vizType;
        this.mReviewRecordingMode = reviewMode;
    }



    public String getReviewRecordingMode() {
        return mReviewRecordingMode;
    }

    public void setReviewRecordingMode(String reviewRecordingMode) {
        this.mReviewRecordingMode = reviewRecordingMode;
    }

    public String getMode() {

        return mMode;
    }
    public void setMode(String mMode) {
        this.mMode = mMode;
    }


}
