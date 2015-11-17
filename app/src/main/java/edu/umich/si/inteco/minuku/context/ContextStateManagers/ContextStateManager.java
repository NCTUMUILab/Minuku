package edu.umich.si.inteco.minuku.context.ContextStateManagers;

import android.util.Log;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.Condition;
import edu.umich.si.inteco.minuku.model.ContextSource;
import edu.umich.si.inteco.minuku.model.LoggingTask;
import edu.umich.si.inteco.minuku.model.State;
import edu.umich.si.inteco.minuku.model.StateMappingRule;
import edu.umich.si.inteco.minuku.model.StateValueCriterion;
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
    public static final int STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN = 7;


    /* relationship **/
    public static final String STATE_MAPPING_RELATIONSHIP_EQUAL_STRING = "Equal";
    public static final String STATE_MAPPING_RELATIONSHIP_NOT_EQUAL_STRING = "Not_Equal";
    public static final String STATE_MAPPING_RELATIONSHIP_LARGER_STRING = "Larger";
    public static final String STATE_MAPPING_RELATIONSHIP_SMALLER_STRING = "Smaller";
    public static final String STATE_MAPPING_RELATIONSHIP_LARGER_AND_EQUAL_STRING = "LargerEqual";
    public static final String STATE_MAPPING_RELATIONSHIP_SMALLER_AND_EQUAL_STRING = "SmallerEqual";
    public static final String STATE_MAPPING_RELATIONSHIP_BETWEEN_STRING = "Between";
    public static final String STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN_STRING = "Contain";


    protected String mName;
    protected ArrayList<Record> mLocalRecordPool;
    protected ArrayList<Condition> mConditions;
    protected ArrayList<LoggingTask> mActiveLoggingTasks;
    protected ArrayList<StateMappingRule> mStateMappingRules;
    protected ArrayList<State> mStates;
    /**each contextStateManager has a list of ContextSource available for use**/
    protected ArrayList<ContextSource> mContextSourceList;

//    protected static HashMap<Integer, Boolean>mSourceExtractTable

    //size of record pool. If the number of records exceed the size, we remove outdated
    //record pool or clear the record pool if we save it in the public record pool
    private int mSizeOfRecordPool = 300;



    public ContextStateManager() {
        mLocalRecordPool = new ArrayList<Record>();
        mStateMappingRules = new ArrayList<StateMappingRule>();
        mActiveLoggingTasks = new ArrayList<LoggingTask>();
        mStates = new ArrayList<State>();
        mContextSourceList = new ArrayList<ContextSource>();
    }


    /** each ContextStateManager should override this static method
     * it adds a list of ContextSource that it will manage **/
    protected void setUpContextSourceList(){
        return;
    }


    private void updateContextSourceListRequestStatus() {

        Log.d(LOG_TAG, " [testing logging task and requested] in  updateContextSourceListRequestStatus " +
                "there are " + mContextSourceList.size() + " context sources");

        for (int i=0; i<mContextSourceList.size(); i++){
            mContextSourceList.get(i).setIsRequested( updateContextSourceRequestStatus( mContextSourceList.get(i) ) );
            Log.d(LOG_TAG, "[updateContextSourceListRequestStatus] the contextsource " + mContextSourceList.get(i).getName() + " requested: " + mContextSourceList.get(i).isRequested());
        }
    }


    private boolean updateContextSourceRequestStatus(ContextSource contextSource) {

        boolean isLogging = isRequestedByActiveLoggingTasks(contextSource);

        boolean isMonitored = isStateMonitored(contextSource.getSourceId());

        boolean isRequested = isLogging | isMonitored;

        return isRequested;

    }


    /** this function allows ConfigurationManager to adjust the configuration of each ContextSource,
     * e.g sampling rate. */

    public void updateContextSourceList(String source){

        //1. use general source name to update all sources (e.g. ActivityRecognition, Sensor)

        //2. update individual source by souce name .
        return;
    }


    public  void updateContextSourceList(String source, float samplingRate){

        //1. use general source name to update all sources (e.g. ActivityRecognition, Sensor)

        //2. update individual source by souce name .
        return;
    }


    /** this function allows ConfigurationManager to adjust the configuration of each ContextSource,
     * e.g sampling rate. */
    public void updateContextSourceList(String source, String samplingMode){

        //1. use general source name to update all sources (e.g. ActivityRecognition, Sensor)

        //2. update individual source by souce name .
        return;
    }


    abstract public void saveRecordsInLocalRecordPool();
    public static int getContextSourceTypeFromName(String sourceName){return -1;};
    public static String getContextSourceNameFromType(int sourceType){return null;};



    /** if the value of the state is changed, we inform ContextManager about the change so that it can
     * examine the conditions of the events related to the state **/
    public void stateChanged(State state){
        Log.d(LOG_TAG, "[stateChanged] state " + state.getName() + " is changed" );
                ContextManager.examineCircumstances(state);
    };



    /*** given the current StetMappingRules, ContextStateManager needs to create the states for
     * ContextManager to mointor. We call this whenever we modify (e.g. add, remove) the current rules
     * We reset the current stateList, and then reconstruct it.
     * TODO: in the future we just update the list for the newly added ones and removed ones.
     */
    public void updateMonitoredState(StateMappingRule rule) {

        //we reset the stateList
        mStates.clear();

        State state = new State(rule);
        //we add the state into the StateList
        mStates.add(state);
        Log.d(LOG_TAG, "[testing stateMappingRule] creating state: " + state.getName() + " current value: " + state.getValue());
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
    private boolean examineStateRule(int sourceType, int measure, int relationship, String targetValue){
        boolean pass = false;
        return pass;
    }

    private boolean examineStateRule(int sourceType, int measure, int relationship, float targetValue){
        boolean pass = false;
        return pass;
    }


    /**
     * updateStates()
     * ContextStateManager check the value for each countextual source and determine whether to
     * change the value of the state for every 5 seconds When a ContextStateManager check the values and update states
     * depends on the sampling rate and how it obtains the value (pull vs. push)
     */
    protected void updateStateValues(int sourceType) {

        /** 1. we first make sure whether the sourceType is being monitored. If not, we don't need to update
         //the state values. We call isStateMonitored(int SourceType) to do the examination. **/
        //Log.d(LOG_TAG, "examine statemappingrule, the state is being monitored: " + isStateMonitored(sourceType));
        if (!isStateMonitored(sourceType)) {
            return;
        }

        /** 2. if a state using the source is currently monitored, we get the stateMappingRule by the type
         * then we call examineStateRule() to examine the rule depending on the type of the target value
         * Currently, it could be a string or a float number**/
        for (int i=0; i<getStateMappingRules().size(); i++) {

            //get the rule based on the source type. We don't examine all MappingRule, but only the rule
            //that are related to the source.
            StateMappingRule rule = getStateMappingRules(sourceType).get(i);
            boolean pass= false;

            //each rule can have more than one criterion, where each criterion has a measure,
            // relationship and a targetvalue
            ArrayList<StateValueCriterion> criteria = rule.getCriteria();

            for (int j=0; j<criteria.size(); j++){

                //1. get the targer value and relaionship
                int relationship = criteria.get(j).getRelationship();
                int measure = criteria.get(j).getMeasureType();

                //get values depending on whether the target value is a string or a float number
                if (criteria.get(j).isTargetString()){
                    String targetValue = criteria.get(j).getTargetStringValue();
                    pass = examineStateRule(sourceType, measure, relationship, targetValue);
                }
                //the target value is a number
                else {
                    float targetValue = criteria.get(j).getTargetFloatValue();
                    pass = examineStateRule(sourceType, measure, relationship, targetValue);
                }

                /** examine criterion specified in the SateMappingRule **/
                Log.d(LOG_TAG, "examine statemappingrule, after the examination the criterion is " + pass);

            }


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
     * this function takes a ContextSource and examines whether the Contextsouce will be used by any
     * StateMappingRule
     * @return
     */
    protected boolean isRequestedByStateMapping(ContextSource contextSource) {

        for (int i=0; i<mStateMappingRules.size(); i++){

            if (contextSource.getSourceId()==mStateMappingRules.get(i).getSource()){
                return true;
            }
        }
        return false;
    }


    /**
     * This function takes a ContextSource and examines whether it will used by a loggingTask
     * @param contextSource
     * @return
     */
    protected boolean isRequestedByActiveLoggingTasks(ContextSource contextSource) {

        for (int i=0; i<mActiveLoggingTasks.size(); i++) {

            //if contextsource is requested by an active logging task
            if (contextSource.getSourceId()==mActiveLoggingTasks.get(i).getSourceType()){
                return true;
            }
        }

        return false;
    }


    /**
     * Examine whether the context source is needed in order to monitor a state.
     * @param sourceType
     * @return
     */
    protected boolean isStateMonitored(int sourceType) {

        Log.d(LOG_TAG, "examine statemappingrule: in isStateMonitored");

        for (int i=0; i<getStateList().size(); i++){

            State state = getStateList().get(i);

            //find any state that uses the source and that state is currently enabled.
            if (state.getMappingRule().getSource()==sourceType && state.isEnabled()){
                Log.d(LOG_TAG, "examine statemappingrule: state " + state.getName() + " is monitored");
                return true;
            }
        }
        return false;
    }






    /*
    protected static boolean isSourceRequested(int sourceId) {

        Log.d(LOG_TAG, "examine statemappingrule: in isSourceRequested " + getContextSourceNameFromType(sourceId));

        for (int i=0; i<mContextSourceList.size(); i++){

            //we first find the source by id
            if (mContextSourceList.get(i).getSourceId()== sourceId){

                //we return true when the source is both available and requested
                if (mContextSourceList.get(i).isRequested() & mContextSourceList.get(i).isAvailable()){
                    return true;
                }
            }
        }

        return false;

    }
*/
    public ContextStateManager(ArrayList<Record> mlocalRecordPool) {
        this.mLocalRecordPool = mlocalRecordPool;
    }


    public void addRecord(Record record){
        mLocalRecordPool.add(record);
    }

    public Record getLastSavedRecord() {
        if (!mLocalRecordPool.isEmpty()){
            return mLocalRecordPool.get(mLocalRecordPool.size()-1);
        }
        return null;
    }

    public void removeRecord(Record record){
        mLocalRecordPool.remove(record);
    }

    public void clearRecordPool(){
        mLocalRecordPool.clear();
    }


    public ArrayList<Condition> getConditions() {
        return mConditions;
    }

    public void setConditions(ArrayList<Condition> conditions) {
        mConditions = conditions;
    }

    public ArrayList<State> getStateList() {
        return mStates;
    }

    public void setStateList(ArrayList<State> stateList) {
        mStates = stateList;
    }

    public void addState(State state){
        mStates.add(state);
    }

    public ArrayList<StateMappingRule> getStateMappingRules() {
        return mStateMappingRules;
    }


    /**
     * return a list of stateMappingRule that use the source
     * @param sourceType
     * @return
     */
    protected ArrayList<StateMappingRule> getStateMappingRules(int sourceType) {

        ArrayList<StateMappingRule> rules = new ArrayList<StateMappingRule>();

       //get stateMapping rule that involves the sourceType
        for (int i=0; i<getStateMappingRules().size(); i++){

            StateMappingRule rule = getStateMappingRules().get(i);

            //find any state that uses the source and that state is currently enabled.
            if (rule.getSource()==sourceType){
               rules.add(rule);
            }
        }
        return rules;
    }

    public void setStateMappingRules(ArrayList<StateMappingRule> rules) {
        mStateMappingRules = rules;
    }

    private StateMappingRule translateStateMappingRule(StateMappingRule rule) {
        return rule;
    }

    /**
     * the function add the logging task into the ActiveLoggingTask. Then it calls updateContextSourceListRequestStatus
     * to update whether contextsource is requested.
     * @param loggingTask
     */
    public void addActiveLoggingTask(LoggingTask loggingTask) {

        Log.d(LOG_TAG, " [testing logging task and requested] in addActiveLoggingTask in CSM ");

        mActiveLoggingTasks.add(loggingTask);

        Log.d(LOG_TAG, " [testing logging task and requested] in addActiveLoggingTask in CSM there are "
         + mActiveLoggingTasks.size() + " logging tasks " + this.mName  );

        updateContextSourceListRequestStatus();

    }


    public void removeActiveLoggingTask (LoggingTask loggingTask) {
        mActiveLoggingTasks.remove(loggingTask);
    }

    public void addStateMappingRule(StateMappingRule rule){

        //we know that there's some specific information in condition that needs to be translated in specific
        //contextStateManager, so we need call translateStateMappingRule before we add the rule.
        StateMappingRule translatedRule = translateStateMappingRule(rule);

        mStateMappingRules.add(translatedRule);
        //Log.d(LOG_TAG, "[testing stateMappingRule] adding rule: " + rule.toString() + " to " + getName());

        //for each time we add a state, we update the list of State.
        updateMonitoredState(rule);
    }

    public void removeStateMappingRule(StateMappingRule rule) {
        if (mStateMappingRules.contains(rule))
            mStateMappingRules.remove(rule);
    }

    public void removeAllStateMappingRules() {
        mStateMappingRules.clear();
    }

    public ArrayList<Record> getLocalRecordPool() {
        return mLocalRecordPool;
    }

    public void setLocalRecordPool(ArrayList<Record> localRecordPool) {
        mLocalRecordPool = localRecordPool;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    /**
     *
     * @param value
     * @param relationship
     * @param targetValue
     * @return
     */
    protected static boolean satisfyCriterion(String value, int relationship, String targetValue ){

        boolean pass=false;

        if (relationship==STATE_MAPPING_RELATIONSHIP_EQUAL){
            if (value.equals(targetValue)) pass = true;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_NOT_EQUAL){
            if (!value.equals(targetValue)) pass = true;
        }
        else if (relationship==STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN){
            if (value.equals(targetValue)) pass = true;
        }

        Log.d(LOG_TAG, "[examine statemappingrule] comparing value " + value +" and targetvalue " + targetValue + " rel: " + relationship  + " pass: " + pass) ;

        return pass;

    }

    protected static boolean satisfyCriterion(float value, int relationship, float targetValue ) {

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
        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_NOT_EQUAL_STRING){
            return STATE_MAPPING_RELATIONSHIP_NOT_EQUAL;
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
        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN_STRING){
            return STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN;
        }
        else
            return -1;
    }

    public ContextSource getContextSourceBySourceId(int sourceId) {

        for (int i=0; i<mContextSourceList.size(); i++){
            if (mContextSourceList.get(i).getSourceId()==sourceId)
                return mContextSourceList.get(i);
        }
        return null;
    }



    public ContextSource getContextSourceBySourceName(String sourceName) {

        for (int i=0; i<mContextSourceList.size(); i++){
            if (mContextSourceList.get(i).getName().equals(sourceName))
                return mContextSourceList.get(i);
        }
        return null;
    }

    public static String getRelationshipName(int relationship) {

        if (relationship==STATE_MAPPING_RELATIONSHIP_EQUAL){
            return STATE_MAPPING_RELATIONSHIP_EQUAL_STRING;
        }
        else if (relationship ==STATE_MAPPING_RELATIONSHIP_NOT_EQUAL){
            return STATE_MAPPING_RELATIONSHIP_NOT_EQUAL_STRING;
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
        else if (relationship==STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN){
            return STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN_STRING;
        }
        else
            return null;
    }

}
