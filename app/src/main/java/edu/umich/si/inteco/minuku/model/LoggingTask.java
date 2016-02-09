package edu.umich.si.inteco.minuku.model;

/**
 * Created by Armuro on 11/2/15.
 */
public class LoggingTask {

    //determined in the configuration file
    protected String mSourceString;
    protected int mSourceType;
    protected int mId;
    //if there's any current logging session (including background recording) specifying the
    // loggingTask, it is active
    protected boolean mActive;

    public LoggingTask() {
        mActive = false;
    }

    public LoggingTask(int id, String source) {

        mSourceString = source;
        mId = id;
        mActive = false;
    }

    public int getSourceType() {
        return mSourceType;
    }

    public void setSourceType(int sourceType) {
        this.mSourceType =sourceType;
    }

    public String getSource() {
        return mSourceString;
    }

    public int getId() {
        return mId;
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean active) {
        this.mActive = active;
    }
}
