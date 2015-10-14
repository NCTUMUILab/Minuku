package edu.umich.si.inteco.minuku.model.record;

import edu.umich.si.inteco.minuku.context.ContextManager;

/**
 * Created by Armuro on 6/18/14.
 */
public class PhoneActivityRecord extends Record{

    private String mAppPackageName = "defaultPackage";
    private String mAppActivityName = "defaultActivity";;


    public PhoneActivityRecord(String appPackageName, String appActivityName) {
        this._type = ContextManager.CONTEXT_RECORD_TYPE_APPLICATION_ACTIVITY;
        this.mAppPackageName = appPackageName;
        this.mAppActivityName = appActivityName;
    }

    public String getAppPackageName() {
        return mAppPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.mAppPackageName = appPackageName;
    }

    public String getAppActivityName() {
        return mAppActivityName;
    }

    public void setAppActivityName(String appActivityName) {
        this.mAppActivityName = appActivityName;
    }

    @Override
    public String toString() {

        String s = "";

        s+= this.getTimeString() + "\t" +
                this.getTimestamp() +  "\t"	+
                this.getType() +"\t" ;

        s+= "\t" +mAppPackageName +
            "\t" +mAppActivityName;

        s+="\n";

        return s;
    }
}

