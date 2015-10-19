package edu.umich.si.inteco.minuku.model;

import java.util.ArrayList;

/**
 * Created by Armuro on 10/9/15.
 */
public class State {

    private int mId;
    private String mName;
    private String mValue = "default";
    private long mLatestUpdatedTime= -1;
    /**this stores information about which event in Minuku is going to use this state**/
    private ArrayList<Event> mEventList;

    public State(String name) {
        mName = name;
    }

    public State(String name, String value) {
        mName = name;
        mValue = value;

    }

    public ArrayList<Event> getEventList() {
        if (mEventList ==null){
            mEventList = new ArrayList<Event>();
        }

        return mEventList;
    }

    public void addEvent(Event event) {
        if (mEventList ==null){
            mEventList = new ArrayList<Event>();
        }
        mEventList.add(event);
    }

    public void removeEvents(Event event) {
        if (mEventList ==null){
            return;
        }

        mEventList.remove(event);
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
    }

    public long getLatestUpdatedTime() {
        return mLatestUpdatedTime;
    }

    public void setLatestUpdatedTime(long latestUpdatedTime) {
        this.mLatestUpdatedTime = latestUpdatedTime;
    }
}