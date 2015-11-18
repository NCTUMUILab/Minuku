package edu.umich.si.inteco.minuku.model;

/**
 * Created by Armuro on 11/2/15.
 */
public class ContextSource {

    protected String mName;
    protected int mSourceId;
    private long mSamplingRate=-1;  //in milliseconds
    //In Android there are four modes to choose. We cannot choose our own sampling rate.
    private int mSamplingMode=-1;

    //by default the
    protected boolean isAvailable = false;
    //by default request is false. We set this true if we see it in the configuration file
    protected boolean isRequested = false;

    public ContextSource(){

    }

    public ContextSource(String name, int id) {
        this.mName = name;
        this.mSourceId = id;
    }

    public ContextSource(String name, int id, boolean isAvailable) {
        this.mName = name;
        this.mSourceId = id;
        this.isAvailable = isAvailable;
    }

    public ContextSource(String name, int id, boolean isAvailable, long samplingRate) {
        this.mName = name;
        this.mSourceId = id;
        this.isAvailable = isAvailable;
        this.mSamplingRate = samplingRate;
    }

    public ContextSource(String name, int id, boolean isAvailable, int samplingMode) {
        this.mName = name;
        this.mSourceId = id;
        this.isAvailable = isAvailable;
        this.mSamplingMode = samplingMode;
    }

    public int getSourceId() {
        return mSourceId;
    }

    public void setName(String name){
        mName = name;
    }

    public String getName(){
        return mName;
    }

    public int getSamplingMode() {
        return mSamplingMode;
    }

    public void setSamplingMode(int samplingMode) {
        this.mSamplingMode = samplingMode;
    }

    public long getSamplingRate() {
        return mSamplingRate;
    }

    public void setSamplingRate(long samplingRate) {
        this.mSamplingRate = samplingRate;
    }

    public void setSamplingRate(int samplingMode) {
        this.mSamplingRate = samplingMode;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public boolean isRequested() {
        return isRequested;
    }

    public void setIsRequested(boolean isRequested) {
        this.isRequested = isRequested;
    }
}
