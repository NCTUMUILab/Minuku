package edu.umich.si.inteco.minuku.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Armuro on 10/9/15.
 */
public class StateMappingRule {

    /** Tag for logging. */
    private static final String LOG_TAG = "Condition";

    private int mId;

    private String mName;

    private String mSource;

    private String mValueType;

    private boolean mIsValueString;

    private String mRelationship;

    private String mStateValue;

    private String mStringTargetValue;

    private float mFloatTargetValue;

    private float mUpper;

    private float mLower;

    public StateMappingRule() {


    }

    //stringEqualTo
    public StateMappingRule(String source, String type, String relationship, String targetValue, String stateName){

        mSource = source;
        mValueType = type;
        mRelationship = relationship;
        mStringTargetValue = targetValue;
        mStateValue = stateName;
        mIsValueString = true;

        mName = mSource + mValueType + mRelationship + mStringTargetValue + mStateValue;

    }

    //larger, equal, smaller
    public StateMappingRule(String source, String type, String relationship, float targetValue, String stateName){

        mSource = source;
        mValueType = type;
        mRelationship = relationship;
        mFloatTargetValue = targetValue;
        mStateValue = stateName;
        mIsValueString = false;

        mName = mSource + mValueType + mRelationship + mFloatTargetValue + mStateValue;

    }

    public StateMappingRule(String source, String type, String relationship, float upper, float lower, String stateName){

        mSource = source;
        mValueType = type;
        mRelationship = relationship;
        mUpper = upper;
        mLower = lower;
        mStateValue = stateName;
        mIsValueString = false;

        mName = mSource + mValueType + mRelationship + mUpper + "-" + mLower + mStateValue;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return "StateMappingRule{" +
                ", mName='" + mName + '\'' +
                ", mSource='" + mSource + '\'' +
                ", mValueType='" + mValueType + '\'' +
                ", mRelationship='" + mRelationship + '\'' +
                ", mStateValue='" + mStateValue + '\'' +
                ", mStringTargetValue='" + mStringTargetValue + '\'' +
                ", mFloatTargetValue=" + mFloatTargetValue +
                ", mUpper=" + mUpper +
                ", mLower=" + mLower +
                '}';
    }
}
