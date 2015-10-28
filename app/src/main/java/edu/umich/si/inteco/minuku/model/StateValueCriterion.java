package edu.umich.si.inteco.minuku.model;

import edu.umich.si.inteco.minuku.context.ContextStateManagers.ContextStateManager;

/**
 * Created by Armuro on 10/28/15.
 */
public class StateValueCriterion extends Criterion{

    private int mMeasure = ContextStateManager.CONTEXT_SOURCE_MEASURE_LATEST_ONE;
    private int mRelationship=ContextStateManager.STATE_MAPPING_RELATIONSHIP_EQUAL;
    private String mTargetStringValue=null;
    private float mTargetFloatValue = 0;

    public StateValueCriterion(){
        super();
    }

    public StateValueCriterion(int measure, int relationship, float targetValue){
        super();
        mMeasure = measure;
        mRelationship =relationship;
        mTargetFloatValue = targetValue;
    }

    public StateValueCriterion(int measure, int relationship, String targetValue){
        super();
        mMeasure = measure;
        mRelationship =relationship;
        mTargetStringValue = targetValue;
    }


    public void setRelationship(int relationship){
        mRelationship = relationship;
    }

    public int getRelationship(){
        return mRelationship;
    }

    public void setTargetValue(float value){
        mTargetFloatValue = value;
    }

    public void setTargetValue(String value){
        mTargetStringValue = value;
    }

    public String getTargetStringValue() {
        return mTargetStringValue;
    }

    public float getTargetFloatValue() {
        return mTargetFloatValue;
    }

    public int getMeasureType(){
        return mMeasure;
    }

    public void setMeasureType(int m){
        mMeasure = m;
    }

}
