package edu.umich.si.inteco.minuku.model;

/**
 * Created by Armuro on 11/2/15.
 */
public class LoggingTask {

    //determined in the configuration file
    protected String mSourceString;
    protected int mSourceType;
    protected int mId;

    public LoggingTask() {

    }

    public LoggingTask(int id, String source) {

        mSourceString = source;
        mId = id;
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
}
