package edu.umich.si.inteco.minuku.context.ContextStateManagers;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.Condition;
import edu.umich.si.inteco.minuku.model.State;
import edu.umich.si.inteco.minuku.model.StateMappingRule;
import edu.umich.si.inteco.minuku.model.record.Record;
import edu.umich.si.inteco.minuku.util.ConditionManager;


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


    /** if the value of the state is changed, we inform ContextManager about the change so that it can
     * examine the conditions of the events related to the state **/
    public static void stateChanged(State state){
        ContextManager.examineCircumstances(state);
    };



    /*** given the current StetMappingRules, ContextStateManager needs to create the states for
     * ContextManager to mointor. We call this whenever we modify (e.g. add, remove) the current rules
     * We reset the current stateList, and then reconstruct it.
     * TODO: in the future we just update the list for the newly added ones and removed ones.
     */
    public static void updateMonitoredStates() {

        //we reset the stateList
        mStateList.clear();

        // for each StateMappingRule, we create a state, even if two rules use the same source. **/
       for (int i=0; i <mStateMappingRules.size(); i++){
           StateMappingRule rule = mStateMappingRules.get(i);
           //we use the rule name as the name of the state
           State state = new State(rule);
           //we add the state into the StateList
           mStateList.add(state);
           //Log.d(LOG_TAG, "[testing stateMappingRule] creating state: " + state.getName() + " current value: " + state.getValue());
        }
    }

    /**
     * these two functions should be implemented in a ContextStateManager. It examines StateMappingRule with the
     * data and returns a boolean pass.
     * @param sourceType
     * @param measure
     * @param relationship
     * @param targetValue
     * @return
     */
    private static boolean examineStateRule(int sourceType, int measure, int relationship, String targetValue){
        boolean pass = false;
        return pass;
    }

    private static boolean examineStateRule(int sourceType, int measure, int relationship, float targetValue){
        boolean pass = false;
        return pass;
    }


    /**
     * updateStates()
     * ContextStateManager check the value for each countextual source and determine whether to
     * change the value of the state for every 5 seconds When a ContextStateManager check the values and update states
     * depends on the sampling rate and how it obtains the value (pull vs. push)
     */
    protected static void updateStateValues(int sourceType) {

        /** 1. we first make sure whether the sourceType is being monitored. If not, we don't need to update
         //the state values. We call isStateMonitored(int SourceType) to do the examination. **/
        //Log.d(LOG_TAG, "examine statemappingrule, the state is being monitored: " + isStateMonitored(sourceType));
        if (!isStateMonitored(sourceType)) {
            return;
        }

        /** 2. if the state is currently monitored, we get the stateMappingRule by the type
         * then we call examineStateRule() to examine the rule depeneding on the type of the target value
         * Currently, it could be a string or a float number**/
        for (int i=0; i<getStateMappingRules().size(); i++) {
            //get the rule
            StateMappingRule rule = getStateMappingRules().get(i);
            boolean pass= false;

            //1. get the targer value and relaionship
            int relationship = rule.getRelationship();
            int measure = rule.getMeasure();

            if (rule.isValueString()){
                String targetValue = rule.getStringTargetValue();
                pass = examineStateRule(sourceType, measure, relationship, targetValue);
            }
            //the target value is a number
            else {
                float targetValue = rule.getFloatTargetValue();
                pass = examineStateRule(sourceType, measure, relationship, targetValue);
            }

            /** examine criterion specified in the SateMappingRule **/
            Log.d(LOG_TAG, "examine statemappingrule, after the examination the criterion is " + pass);


            /** 3. if the criterion is passed, we set the state value based on the mappingRule **/
            if (pass){

                for (int j=0; j<getStateList().size(); j++){
                    //find the state corresponding to the StateMappingRule

                    boolean valueChanged = false;

                    if (getStateList().get(j).getName().equals(rule.getName())){

                        String stateValue = rule.getStateValue();
                        //change the value based on the mapping rule.

                        /** 5. now we need to check whether the new value is different from its current value
                         * if yes. we need to call StateChange, which will notify ContextManager about the change **/
                        if (!getStateList().get(j).getValue().equals(stateValue) ){
                            //the value is changed to the new value,
                            valueChanged = true;
                        }

                        getStateList().get(j).setValue(stateValue);

                        Log.d(LOG_TAG, "examine statemappingrule, the state " + getStateList().get(j).getName() + " value change to " + getStateList().get(j).getValue());

                    }

                    //if the state changes to a new value
                    if (valueChanged){
                        //we call this method to invoke ContextManager to inspect event conditions.
                        stateChanged(getStateList().get(j));
                    }

                }
            }
        }

    }

    /**
     * Examine whether the context source is needed in order to monitor a state.
     * @param sourceType
     * @return
     */
    private static boolean isStateMonitored(int sourceType) {

        Log.d(LOG_TAG, "examine statemappingrule: in isStateMonitored");

        for (int i=0; i<getStateList().size(); i++){

            State state = getStateList().get(i);

            //find any state that uses the source and that state is currently enabled.
            if (state.getMappingRule().getSource()==sourceType && state.isEnabled()){
                return true;
            }
        }
        return false;
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

    private static StateMappingRule translateStateMappingRule(StateMappingRule rule) {

        return new StateMappingRule();

    }

    public static void addStateMappingRule(StateMappingRule rule){

        //we know that there's some specific information in condition that needs to be translated in specific
        //contextStateManager, so we need call translateStateMappingRule before we add the rule.
        StateMappingRule translatedRule = translateStateMappingRule(rule);

        mStateMappingRules.add(translatedRule);
        //Log.d(LOG_TAG, "[testing stateMappingRule] adding rule: " + rule.toString() + " to " + getName());

        //for each time we add a state, we update the list of State.
        updateMonitoredStates();
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

    /**
     *
     * @param value
     * @param relationship
     * @param targetValue
     * @return
     */
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

        if (relationship== STATE_MAPPING_RELATIONSHIP_EQUAL){
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

    public static String getMeasureName(int measure) {

        if (measure == CONTEXT_SOURCE_MEASURE_LATEST_ONE) {
            return CONTEXT_SOURCE_MEASURE_LATEST_ONE_STRING;
        } else if (measure == CONTEXT_SOURCE_MEASURE_AVERAGE) {
            return CONTEXT_SOURCE_MEASURE_AVERAGE_STRING;
        } else
            return null;
    }

    public static int getMeasure(String measureName) {

        if (measureName.equals(CONTEXT_SOURCE_MEASURE_LATEST_ONE_STRING)) {
            return CONTEXT_SOURCE_MEASURE_LATEST_ONE;
        } else if (measureName.equals(CONTEXT_SOURCE_MEASURE_AVERAGE_STRING)) {
            return CONTEXT_SOURCE_MEASURE_AVERAGE;
        } else
            return -1;
    }




    public static int getRelationship(String  relationshipName) {

        if (relationshipName==STATE_MAPPING_RELATIONSHIP_EQUAL_STRING){
            return STATE_MAPPING_RELATIONSHIP_EQUAL;
        }
        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_LARGER_STRING){
            return STATE_MAPPING_RELATIONSHIP_LARGER;
        }
        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_SMALLER_STRING){
            return STATE_MAPPING_RELATIONSHIP_SMALLER;
        }
        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_LARGER_AND_EQUAL_STRING){
            return STATE_MAPPING_RELATIONSHIP_LARGER_AND_EQUAL;
        }
        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_SMALLER_AND_EQUAL_STRING){
            return STATE_MAPPING_RELATIONSHIP_SMALLER_AND_EQUAL;
        }
        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_BETWEEN_STRING){
            return STATE_MAPPING_RELATIONSHIP_BETWEEN;
        }
        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_STRING_EQUAL_STRING){
            return STATE_MAPPING_RELATIONSHIP_STRING_EQUAL;
        }
        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN_STRING){
            return STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN;
        }
        else
            return -1;
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

}
