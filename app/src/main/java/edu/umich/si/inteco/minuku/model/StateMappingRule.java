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

    private int mSource;

    private String mStateValue;

    private ArrayList<StateValueCriterion> mValueCriteria;

    private ArrayList<TimeCriterion> mTimeCriteria;

    public StateMappingRule() {

    }

    public StateMappingRule(String contextStateManagerName,
                            int source,
                            ArrayList<StateValueCriterion> valueCriteria,
                            String stateValue) {
        mContextStateManagerName = contextStateManagerName;
        mValueCriteria = valueCriteria;
        mStateValue =  stateValue;
        mSource  = source;

        //the name is "source + statevalue + criteria". we make is quite complex so that the rule name will not repeat.
        setName();
    }


    public ArrayList<StateValueCriterion> getCriteria() {
        return mValueCriteria;
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

    public void setSource(int source) {
        this.mSource = source;
    }

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
        mName = ContextManager.getSourceNameFromType(mContextStateManagerName, mSource)
                + "-"+mStateValue
                + getCriteriaString();
    }

    public int getSource() {
        return mSource;
    }

    @Override
    public String toString() {
        return "StateMappingRule{" +
                ", mName='" + mName + '\'' +
                ", mSource='" + ContextManager.getSourceNameFromType(mContextStateManagerName, mSource) + '\'' +
                ", mStateValue='" + mStateValue + '\'' +
                ", mValueCriteria='" + getCriteriaString() + '\'' +
                '}';
    }
}
