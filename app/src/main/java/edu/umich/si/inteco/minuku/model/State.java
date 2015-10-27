package edu.umich.si.inteco.minuku.model;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.context.ContextManager;

/**
 * Created by Armuro on 10/9/15.
 */
public class State {

    private int mId;
    private String mName;
    private String mValue = "default";
    private StateMappingRule mMappingRule;
    private long mLatestUpdatedTime= -1;
    /** by default a State is enabled. It may be disabled by a stopAction**/
    private boolean mEnabled = true;
    /**this stores information about which event in Minuku is going to use this state**/
    private ArrayList<Circumstance> mCircumstanceList;

    public State(StateMappingRule rule) {
        mMappingRule = rule;
        mName = rule.getName();
        mEnabled = true;
    }

    public ArrayList<Circumstance> getEventList() {
        if (mCircumstanceList ==null){
            mCircumstanceList = new ArrayList<Circumstance>();
        }

        return mCircumstanceList;
    }

    public StateMappingRule getMappingRule() {
        return mMappingRule;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public void addEvent(Circumstance circumstance) {
        if (mCircumstanceList ==null){
            mCircumstanceList = new ArrayList<Circumstance>();
        }
        mCircumstanceList.add(circumstance);
    }

    public void removeEvents(Circumstance circumstance) {
        if (mCircumstanceList ==null){
            return;
        }

        mCircumstanceList.remove(circumstance);
    }

    public int getId() {
        return mId;
    }

    public void setId(int Id) {this.mId = Id;}

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        this.mValue = value;
        mLatestUpdatedTime = ContextManager.getCurrentTimeInMillis();
    }

    public long getLatestUpdatedTime() {
        return mLatestUpdatedTime;
    }

}