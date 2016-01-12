package edu.umich.si.inteco.minuku.model;

import java.util.ArrayList;

/**
 * Created by Armuro on 1/12/16.
 */
public class BackgroundLoggingSetting {

    private boolean mIsEnabled = false;
    private ArrayList<Integer> mLoggingTasks;
    private int mLoggingRate = 5;


    public boolean isEnabled() {
        return mIsEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.mIsEnabled = isEnabled;
    }

    public int getLoggingRate() {
        return mLoggingRate;
    }

    public void setLoggingRate(int loggingRate) {
        this.mLoggingRate = loggingRate;
    }

    public void addLoggingTask(int id){
        if (mLoggingTasks==null){
            mLoggingTasks = new ArrayList<Integer> ();
        }

        mLoggingTasks.add(id);
    }

    public ArrayList<Integer> getLoggingTasks() {
        return mLoggingTasks;
    }

}
