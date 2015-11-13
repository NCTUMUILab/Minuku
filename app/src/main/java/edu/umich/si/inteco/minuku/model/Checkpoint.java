package edu.umich.si.inteco.minuku.model;

import android.location.Location;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Armuro on 11/12/15.
 */
public class Checkpoint {

    private Location mLocation;
    private List<DetectedActivity> mActivities;
    private String mTransportation;
    private String mTransportationDetectionStage;
    private long mTimestamp;

    public Checkpoint(Location location,
                      List<DetectedActivity> activities,
                      String transportation,
                      String transportationStage,
                      long timestamp
    ) {

        mLocation = location;
        mActivities = activities;
        mTransportation = transportation;
        mTransportationDetectionStage = transportationStage;
        mTimestamp = timestamp;

    }

    public Location getLocation() {
        return mLocation;
    }

    public List<DetectedActivity> getActivies() {
        return mActivities;
    }

    public String getTransportation() {
        return mTransportation;
    }

    public String getTransportationDetectionStage() {
        return mTransportationDetectionStage;
    }

    public long getTimestamp() {
        return mTimestamp;
    }
}
