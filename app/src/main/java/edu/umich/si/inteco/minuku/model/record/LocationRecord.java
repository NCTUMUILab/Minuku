package edu.umich.si.inteco.minuku.model.Record;

import com.google.android.gms.maps.model.LatLng;

import edu.umich.si.inteco.minuku.Constants;

/**
 * Created by Armuro on 11/21/15.
 */
public class LocationRecord extends Record {

    private double mLat = Constants.NULL_NUMERIC_VALUE;
    private double mLng = Constants.NULL_NUMERIC_VALUE;
    private float mAccuracy = Constants.NULL_NUMERIC_VALUE;

    public LocationRecord(double lat, double lng, float accuracy) {
        super();

        this.mLat = lat;
        this.mLng = lng;
        this.mAccuracy = accuracy;

    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double lat) {
        this.mLat = lat;
    }

    public double getLng() {
        return mLng;
    }

    public void setLng(double lng) {
        this.mLng = lng;
    }

    public double getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(float accuracy) {
        this.mAccuracy = accuracy;
    }
}
