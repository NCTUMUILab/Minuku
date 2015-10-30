package edu.umich.si.inteco.minuku.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.Constants;
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

    private String mStateValue;

    private ArrayList<StateValueCriterion> mCriteria;


    public StateMappingRule() {

    }

    public StateMappingRule(String contextStateManagerName,
                            int source,
                            ArrayList<StateValueCriterion> criteria,
                            String stateValue) {
        mContextStateManagerName = contextStateManagerName;
        mStateValue =  stateValue;
        mCriteria = criteria;
        mSource  = source;

        setName();
    }


    public ArrayList<StateValueCriterion> getCriteria() {
        return mCriteria;
    }

    public void setCriteria(ArrayList<StateValueCriterion> criteria) {
        this.mCriteria = criteria;
    }

    public void setSource(int source) {
        this.mSource = source;
    }

    private String getCriteriaString() {
        String s = "";
        for (int i=0; i<mCriteria.size(); i++){
            s += mCriteria.toString();
            if (i<mCriteria.size()-1){
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
                + mStateValue
                +getCriteriaString();
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
                ", mCriteria='" + getCriteriaString() + '\'' +
                '}';
    }
}
