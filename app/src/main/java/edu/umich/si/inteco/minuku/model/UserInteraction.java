package edu.umich.si.inteco.minuku.model;

import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;

/**
 * Created by Armuro on 3/16/16.
 */
public class UserInteraction {


    private String action = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    private String target = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    private String packageName = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    private String extra = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
    private long actionTime = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_LONG_INTEGER;


    public UserInteraction(){

    }


    public UserInteraction(String action, String target, String packageName, String extra, long actionTime) {

        this.action = action;
        this.target = target;
        this.packageName = packageName;
        this.extra = extra;
        this.actionTime = actionTime;

    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public long getActionTime() {
        return actionTime;
    }

    public void setActionTime(long actionTime) {
        this.actionTime = actionTime;
    }


    @Override
    public String toString() {

        String str = null;

        str = "Action: " + action + ";" +
                "Target: " + target + ";" +
                "Package: " + packageName + ";" +
                "Extra: " + extra + ";" +
                "Time: " + ScheduleAndSampleManager.getTimeString(actionTime) + ";";


        return str;

    }

}
