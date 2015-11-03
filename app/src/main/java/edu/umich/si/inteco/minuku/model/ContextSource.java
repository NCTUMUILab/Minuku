package edu.umich.si.inteco.minuku.model;

/**
 * Created by Armuro on 11/2/15.
 */
public class ContextSource {

    protected String mName;
    protected int mSourceId;
    //by default the
    protected boolean isAvailable = false;
    //by default request is false. We set this true if we see it in the configuration file
    protected boolean isRequested = false;

    public ContextSource(String name, int id) {


    }

    public ContextSource(String name, int id, boolean isAvailable) {
        this.isAvailable = isAvailable;
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
