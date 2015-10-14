package edu.umich.si.inteco.minuku.model;

import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.ContextStateManager;

/**
 * Created by Armuro on 10/9/15.
 */
public class StateMappingRule {

    /** Tag for logging. */
    private static final String LOG_TAG = "Condition";

    private int mId;

    private String mName;

    private String mContextStateManagerName;

    private int mSource;

    private int mMeasure;

    private boolean mIsValueString;

    private int mRelationship;

    private String mStateValue;

    private String mStringTargetValue;

    private float mFloatTargetValue;

    private float mUpper;

    private float mLower;

    public StateMappingRule() {


    }

    //stringEqualTo
    public StateMappingRule(String contextStateManagerName, int source, int measure, int relationship, String targetValue, String stateName){

        mContextStateManagerName = contextStateManagerName;
        mSource = source;
        mMeasure = measure;
        mRelationship = relationship;
        mStringTargetValue = targetValue;
        mStateValue = stateName;
        mIsValueString = true;

        setName();
    }

    //larger, equal, smaller
    public StateMappingRule(String contextStateManagerName, int source, int measure, int relationship, float targetValue, String stateName){

        mContextStateManagerName = contextStateManagerName;
        mSource = source;
        mMeasure = measure;
        mRelationship = relationship;
        mFloatTargetValue = targetValue;
        mStateValue = stateName;
        mIsValueString = false;

        setName();

    }

    public StateMappingRule(String contextStateManagerName, int source, int measure, int relationship, float upper, float lower, String stateName){

        mContextStateManagerName = contextStateManagerName;
        mSource = source;
        mMeasure = measure;
        mRelationship = relationship;
        mUpper = upper;
        mLower = lower;
        mStateValue = stateName;
        mIsValueString = false;

        setName();
    }

    public boolean isValueString() {
        return mIsValueString;
    }

    public float getFloatTargetValue() {
        return mFloatTargetValue;
    }

    public String getStringTargetValue() {
        return mStringTargetValue;
    }

    public int getRelationship(){
        return mRelationship;
    }

    public int getMeasure() {
        return mMeasure;
    }

    public String getName() {
        return mName;
    }

    public String getStateValue() {
        return mStateValue;
    }

    public void setmStateValue(String stateValue) {
        this.mStateValue = stateValue;
    }

    private void setName() {
        mName = ContextManager.getSourceName(mContextStateManagerName, mSource)
                + ContextStateManager.getMeasureName(mMeasure)
                + ContextStateManager.getRelationshipName(mRelationship)
                + mStringTargetValue
                + mStateValue;
    }

    public int getSource() {
        return mSource;
    }

    @Override
    public String toString() {
        return "StateMappingRule{" +
                ", mName='" + mName + '\'' +
                ", mSource='" + ContextManager.getSourceName(mContextStateManagerName, mSource) + '\'' +
                ", mMeasure='" + mMeasure + '\'' +
                ", mRelationship='" + mRelationship + '\'' +
                ", mStateValue='" + mStateValue + '\'' +
                ", mStringTargetValue='" + mStringTargetValue + '\'' +
                ", mFloatTargetValue=" + mFloatTargetValue +
                ", mUpper=" + mUpper +
                ", mLower=" + mLower +
                '}';
    }
}
