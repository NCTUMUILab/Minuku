package edu.umich.si.inteco.minuku.model.Criteria;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Armuro on 2/23/16.
 */
public class LocationStateValueCriterion extends StateValueCriterion{

    private LatLng targetLocaiton;

    public void setTargetLocaiton(LatLng targetLocaiton) {
        this.targetLocaiton = targetLocaiton;
    }

    public void setTargetLocaiton(double lat, double lng) {
        targetLocaiton = new LatLng(lat, lng);
    }
}
