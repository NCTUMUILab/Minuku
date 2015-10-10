package edu.umich.si.inteco.minuku.contextmanager;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.umich.si.inteco.minuku.model.Condition;
import edu.umich.si.inteco.minuku.model.State;
import edu.umich.si.inteco.minuku.model.StateMappingRule;
import edu.umich.si.inteco.minuku.model.record.Record;


public abstract class ContextStateManager {

    /** Tag for logging. */
    private static final String LOG_TAG = "ContextStateManager";

    protected static ArrayList<Record> mLocalRecordPool;
    protected static ArrayList<Condition> mConditions;
    protected static ArrayList<StateMappingRule> mStateMappingRules;
    protected static ArrayList<State> mStateList;

    //size of record pool. If the number of records exceed the size, we remove outdated
    //record pool or clear the record pool if we save it in the public record pool
    private int mSizeOfRecordPool = 300;


    public abstract void examineConditions();
    public abstract void stateChanged();
    public abstract void saveRecordsInLocalRecordPool();
    public abstract int getContextSourceTypeFromName(String sourceName);

    /**
     * updateStates()
     * ContextStateManager check the value for each countextual source and determine whether to
     * change the value of the state. When a ContextStateManager check the values and update states
     * depends on the sampling rate and how it obtains the value (pull vs. push)
     */
    public abstract void updateStates(int typeOfSource, String value);
    public abstract void updateStates(int typeOfSource, int value);

    public ContextStateManager() {
        mLocalRecordPool = new ArrayList<Record>();
        mStateMappingRules = new ArrayList<StateMappingRule>();
        mStateList = new ArrayList<State>();



    }

    /*** given the current StetMappingRules, ContextStateManager needs to create the states for
     * ContextManager to mointor.
     */
    public void setupStates() {

        /** for each StateMappingRule, we create a state, even if two rules use the same source. **/
       for (int i=0; i <mStateMappingRules.size(); i++){
           StateMappingRule rule = mStateMappingRules.get(i);
           //we use the rule name as the name of the state
           State state = new State(rule.getName());
           //we add the state into the StateList
           mStateList.add(state);
        }
    }



    public ContextStateManager(ArrayList<Record> mlocalRecordPool) {
        this.mLocalRecordPool = mlocalRecordPool;
    }


    public static void addRecord(Record record){
        mLocalRecordPool.add(record);
    }

    public static Record getLastSavedRecord() {
        if (!mLocalRecordPool.isEmpty()){
            return mLocalRecordPool.get(mLocalRecordPool.size()-1);
        }
        return null;
    }

    public static void removeRecord(Record record){
        mLocalRecordPool.remove(record);
    }

    public static void clearRecordPool(){
        mLocalRecordPool.clear();
    }


    public static ArrayList<Condition> getConditions() {
        return mConditions;
    }

    public static void setConditions(ArrayList<Condition> conditions) {
        mConditions = conditions;
    }

    public static ArrayList<State> getStateList() {
        return mStateList;
    }

    public static void setStateList(ArrayList<State> stateList) {
        ContextStateManager.mStateList = stateList;
    }

    public static void addState(State state){
        mStateList.add(state);
    }

    public static ArrayList<StateMappingRule> getStateMappingRules() {
        return mStateMappingRules;
    }

    public static void setStateMappingRules(ArrayList<StateMappingRule> rules) {
        mStateMappingRules = rules;
    }

    public static void addStateMappingRule(StateMappingRule rule){
        mStateMappingRules.add(rule);
    }

    public static void removeStateMappingRule(StateMappingRule rule) {
        if (mStateMappingRules.contains(rule))
            mStateMappingRules.remove(rule);
    }

    public static void removeAllStateMappingRules() {
        mStateMappingRules.clear();
    }

    public static ArrayList<Record> getLocalRecordPool() {
        return mLocalRecordPool;
    }

    public static void setLocalRecordPool(ArrayList<Record> localRecordPool) {
        mLocalRecordPool = localRecordPool;
    }

}
