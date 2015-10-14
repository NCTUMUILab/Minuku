package edu.umich.si.inteco.minuku.context.ContextStateManagers;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import edu.umich.si.inteco.minuku.model.Condition;
import edu.umich.si.inteco.minuku.model.State;
import edu.umich.si.inteco.minuku.model.StateMappingRule;
import edu.umich.si.inteco.minuku.model.record.Record;


public abstract class ContextStateManager {

    /** Tag for logging. */
    private static final String LOG_TAG = "ContextStateManager";

    /* context measure **/
    public static final int CONTEXT_SOURCE_MEASURE_LATEST_ONE = 0;
    public static final int CONTEXT_SOURCE_MEASURE_AVERAGE = 1;

    public static final String CONTEXT_SOURCE_MEASURE_LATEST_ONE_STRING = "Latest";
    public static final String CONTEXT_SOURCE_MEASURE_AVERAGE_STRING = "Average";

    /* relationship **/
    public static final int STATE_MAPPING_RELATIONSHIP_EQUAL = 0;
    public static final int STATE_MAPPING_RELATIONSHIP_NOT_EQUAL = 1;
    public static final int STATE_MAPPING_RELATIONSHIP_LARGER = 2;
    public static final int STATE_MAPPING_RELATIONSHIP_SMALLER = 3;
    public static final int STATE_MAPPING_RELATIONSHIP_LARGER_AND_EQUAL = 4;
    public static final int STATE_MAPPING_RELATIONSHIP_SMALLER_AND_EQUAL = 5;
    public static final int STATE_MAPPING_RELATIONSHIP_BETWEEN = 6;
    public static final int STATE_MAPPING_RELATIONSHIP_STRING_EQUAL = 7;
    public static final int STATE_MAPPING_RELATIONSHIP_STRING_NOT_EQUAL = 8;
    public static final int STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN = 9;


    /* relationship **/
    public static final String STATE_MAPPING_RELATIONSHIP_EQUAL_STRING = "Equal";
    public static final String STATE_MAPPING_RELATIONSHIP_LARGER_STRING = "Larger";
    public static final String STATE_MAPPING_RELATIONSHIP_SMALLER_STRING = "Smaller";
    public static final String STATE_MAPPING_RELATIONSHIP_LARGER_AND_EQUAL_STRING = "LargerEqual";
    public static final String STATE_MAPPING_RELATIONSHIP_SMALLER_AND_EQUAL_STRING = "SmallerEqual";
    public static final String STATE_MAPPING_RELATIONSHIP_BETWEEN_STRING = "Between";
    public static final String STATE_MAPPING_RELATIONSHIP_STRING_EQUAL_STRING = "Equal";
    public static final String STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN_STRING = "Contain";


    protected static String mName;
    protected static ArrayList<Record> mLocalRecordPool;
    protected static ArrayList<Condition> mConditions;
    protected static ArrayList<StateMappingRule> mStateMappingRules;
    protected static ArrayList<State> mStateList;
//    protected static HashMap<Integer, Boolean>mSourceExtractTable

    //size of record pool. If the number of records exceed the size, we remove outdated
    //record pool or clear the record pool if we save it in the public record pool
    private int mSizeOfRecordPool = 300;


    public abstract void stateChanged();
    public abstract void saveRecordsInLocalRecordPool();

    public static int getContextSourceTypeFromName(String sourceName){
        return 0;
    }
    public static String getContextSourceNameFromType(int sourceType){
        return null;
    }



    public ContextStateManager() {
        mLocalRecordPool = new ArrayList<Record>();
        mStateMappingRules = new ArrayList<StateMappingRule>();
        mStateList = new ArrayList<State>();
    }

    /**
     * updateStates()
     * ContextStateManager check the value for each countextual source and determine whether to
     * change the value of the state for every 5 seconds When a ContextStateManager check the values and update states
     * depends on the sampling rate and how it obtains the value (pull vs. push)
     */
    public static void updateStateValues( ){};

    /*** given the current StetMappingRules, ContextStateManager needs to create the states for
     * ContextManager to mointor. We call this whenever we modify (e.g. add, remove) the current rules
     * We reset the current stateList, and then reconstruct it.
     * TODO: in the future we just update the list for the newly added ones and removed ones.
     */
    public static void updateStates() {

        //we reset the stateList
        mStateList.clear();

        // for each StateMappingRule, we create a state, even if two rules use the same source. **/
       for (int i=0; i <mStateMappingRules.size(); i++){
           StateMappingRule rule = mStateMappingRules.get(i);
           //we use the rule name as the name of the state
           State state = new State(rule.getName());
           //we add the state into the StateList
           mStateList.add(state);
           //Log.d(LOG_TAG, "[testing stateMappingRule] creating state: " + state.getName() + " current value: " + state.getValue());
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
        //Log.d(LOG_TAG, "[testing stateMappingRule] adding rule: " + rule.toString() + " to " + getName());

        //for each time we add a state, we update the list of State.
        updateStates();
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

    public static String getName() {
        return mName;
    }

    public static void setName(String name) {
        mName = name;
    }

    protected  static boolean satisfyCriterion(String value, int relationship, String targetValue ){

        boolean pass=false;

        if (relationship==STATE_MAPPING_RELATIONSHIP_STRING_EQUAL){
            if (value.equals(targetValue)) pass = true;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_STRING_NOT_EQUAL){
            if (!value.equals(targetValue)) pass = true;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN){
            if (value.equals(targetValue)) pass = true;
        }

        Log.d(LOG_TAG, "[examine statemappingrule] comparing value " + value +" and targetvalue " + targetValue + " rel: " + relationship  + " pass: " + pass) ;

        return pass;

    }



    protected  static boolean satisfyCriterion(float value, int relationship, float targetValue ) {

        boolean pass=false;

        if (relationship==STATE_MAPPING_RELATIONSHIP_EQUAL){
            if (value==targetValue) pass = true;
        }else if (relationship==STATE_MAPPING_RELATIONSHIP_NOT_EQUAL){
            if (value!=targetValue)  pass = true;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_LARGER){
            if (value>targetValue) pass = true;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_LARGER_AND_EQUAL){
            if (value>=targetValue) pass = true;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_SMALLER){
            if (value<targetValue) pass = true;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_SMALLER_AND_EQUAL){
            if (value<=targetValue) pass = true;
        }


        Log.d(LOG_TAG, "satisfyCriterion] comparing value " + value +" and targetvalue " + targetValue + " relship: " + relationship  + " pass: " + pass) ;

        return pass;

    }

    public static String getRelationshipName(int relationship) {
        
        if (relationship==STATE_MAPPING_RELATIONSHIP_EQUAL){
            return STATE_MAPPING_RELATIONSHIP_EQUAL_STRING;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_LARGER){
            return STATE_MAPPING_RELATIONSHIP_LARGER_STRING;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_SMALLER){
            return STATE_MAPPING_RELATIONSHIP_SMALLER_STRING;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_LARGER_AND_EQUAL){
            return STATE_MAPPING_RELATIONSHIP_LARGER_AND_EQUAL_STRING;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_SMALLER_AND_EQUAL){
            return STATE_MAPPING_RELATIONSHIP_SMALLER_AND_EQUAL_STRING;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_BETWEEN){
            return STATE_MAPPING_RELATIONSHIP_BETWEEN_STRING;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_STRING_EQUAL){
            return STATE_MAPPING_RELATIONSHIP_STRING_EQUAL_STRING;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN){
            return STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN_STRING;
        }
        else
            return null;
    }

    public static String getMeasureName(int measure) {

        if (measure == CONTEXT_SOURCE_MEASURE_LATEST_ONE) {
            return CONTEXT_SOURCE_MEASURE_LATEST_ONE_STRING;
        } else if (measure == CONTEXT_SOURCE_MEASURE_AVERAGE) {
            return CONTEXT_SOURCE_MEASURE_AVERAGE_STRING;
        } else
            return null;
    }
}
