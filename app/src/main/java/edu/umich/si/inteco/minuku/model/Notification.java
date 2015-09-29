package edu.umich.si.inteco.minuku.model;

/**
 * Created by Armuro on 6/25/14.
 */
public class Notification {

    private String mTitle = "NA";
    private String mMessage = "NA";
    private String mType = "NA";
    private String mLaunch = "NA";

    public Notification (String launch, String type, String title, String message){
        this.mLaunch = launch;
        this.mType = type;
        this.mTitle = title;
        this.mMessage = message;
    }

    public String getTitle(){
        return mTitle;
    }

    public String getMessage(){
        return mMessage;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public String getLaunch() {
        return mLaunch;
    }

    public void setLaunch(String launch) {
        this.mLaunch = launch;
    }
}
