package edu.umich.si.inteco.minuku.model;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.Criteria.StateValueCriterion;
import edu.umich.si.inteco.minuku.model.Criteria.TimeCriterion;

/**
 * Created by Armuro on 10/9/15.
 */
public class StateMappingRule {

    /** Tag for logging. */
    private static final String LOG_TAG = "Condition";

    private int mId;

    private String mName;

    private String mContextStateManagerName;

    private int mSourceType;

    private String mSource;

    private String mStateValue;

    private ArrayList<StateValueCriterion> mValueCriteria;

    private ArrayList<TimeCriterion> mTimeCriteria;

    public StateMappingRule() {

    }

    public StateMappingRule(int id,
                            String contextStateManagerName,
                            int sourcetype,
                            String source,
                            ArrayList<StateValueCriterion> valueCriteria,
                            String stateValue) {
        mId = id;
        mContextStateManagerName = contextStateManagerName;
        mValueCriteria = valueCriteria;
        mStateValue =  stateValue;
        mSourceType = sourcetype;
        mSource = source;

        //the name is "source + statevalue + criteria". we make is quite complex so that the rule name will not repeat.
        setName();
    }


    public ArrayList<StateValueCriterion> getCriteria() {
        return mValueCriteria;
    }

    public ArrayList<TimeCriterion> getTimeCriteria() {
        return mTimeCriteria;
    }

    public void setTimeCriteria(ArrayList<TimeCriterion> criteria) {
        this.mTimeCriteria = criteria;
    }

    public void addTimeCriterion(TimeCriterion criterion){

        if (mTimeCriteria==null) {
            mTimeCriteria = new ArrayList<TimeCriterion>();
        }
        mTimeCriteria.add(criterion);
    }


    public void setValueCriteria(ArrayList<StateValueCriterion> criteria) {
        this.mValueCriteria = criteria;
    }

    public void setSourceType(int source) {
        this.mSourceType = source;
    }

    public void setSource(String source) {this.mSource = source;};


    private String getCriteriaString() {
        String s = "";
        for (int i=0; i< mValueCriteria.size(); i++){
            s += mValueCriteria.toString();
            if (i< mValueCriteria.size()-1){
                s+= Constants.DELIMITER;
            }
        }
        return s;
    }

    public int getId() {return mId;};

    public String getName() {
        return mName;
    }

    public String getStateValue() {
        return mStateValue;
    }

    public String getSource() {return mSource;};

    public void setmStateValue(String stateValue) {
        this.mStateValue = stateValue;
    }

    private void setName() {
        mName = ContextManager.getSourceNameFromType(mContextStateManagerName, mSourceType)
                + "-" + mSource
                + "-"+mStateValue
                + getCriteriaString();
    }

    public int getSourceType() {
        return mSourceType;
    }

    @Override
    public String toString() {
        return "StateMappingRule{" +
                ", mName='" + mName + '\'' +
                ", mSourceType='" + ContextManager.getSourceNameFromType(mContextStateManagerName, mSourceType) + '\'' +
                ", mStateValue='" + mStateValue + '\'' +
                ", mValueCriteria='" + getCriteriaString() + '\'' +
                '}';
    }
}
