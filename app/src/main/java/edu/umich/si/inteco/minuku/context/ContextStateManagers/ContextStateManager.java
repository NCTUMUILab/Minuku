package edu.umich.si.inteco.minuku.context.ContextStateManagers;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.Condition;
import edu.umich.si.inteco.minuku.model.ContextSource;
import edu.umich.si.inteco.minuku.model.LoggingTask;
import edu.umich.si.inteco.minuku.model.State;
import edu.umich.si.inteco.minuku.model.StateMappingRule;
import edu.umich.si.inteco.minuku.model.Criteria.StateValueCriterion;
import edu.umich.si.inteco.minuku.model.Record.Record;


public abstract class ContextStateManager {

    /** Tag for logging. */
    private static final String LOG_TAG = "ContextStateManager";


    /**ContextSourceType**/
    //Each ContextStateMangager should a define source type and its corresponding string
    public static final int CONTEXT_SOURCE_XYZ = 0;
    public static final String STRING_CONTEXT_SOURCE_XYZ = "XYZ";

    /* context measure **/
    public static final String CONTEXT_SOURCE_MEASURE_LATEST_ONE = "LatestValue";
    public static final String CONTEXT_SOURCE_MEASURE_MOST_FREQUENT_VALUE = "MostFrequentValue";
    public static final String CONTEXT_SOURCE_MEASURE_MEAN = "MeanValue";

    /* relationship **/
    public static final String STATE_MAPPING_RELATIONSHIP_EQUAL = "Equal";
    public static final String STATE_MAPPING_RELATIONSHIP_NUMBER_EQUAL = "=";
    public static final String STATE_MAPPING_RELATIONSHIP_NOT_EQUAL = "Not_Equal";
    public static final String STATE_MAPPING_RELATIONSHIP_NUMBER_NOT_EQUAL = "<>";
    public static final String STATE_MAPPING_RELATIONSHIP_LARGER = ">";
    public static final String STATE_MAPPING_RELATIONSHIP_SMALLER = "<";
    public static final String STATE_MAPPING_RELATIONSHIP_LARGER_AND_EQUAL = ">=";
    public static final String STATE_MAPPING_RELATIONSHIP_SMALLER_AND_EQUAL = "<=";
    public static final String STATE_MAPPING_RELATIONSHIP_BETWEEN = "Between";
    public static final String STATE_MAPPING_RELATIONSHIP_CONTAIN = "Contain";


    protected String mName;
    protected long recordCount;
    protected ArrayList<Record> mLocalRecordPool;
    protected ArrayList<Condition> mConditions;
    protected ArrayList<LoggingTask> mLoggingTasks;
    protected ArrayList<StateMappingRule> mStateMappingRules;
    protected ArrayList<State> mStates;
    /**each contextStateManager has a list of ContextSource available for use**/
    protected ArrayList<ContextSource> mContextSourceList;
    protected ArrayList<ContextSource> mCurrentlyRequestedContextSourceList;
    protected Record mLastSavedRecord;
    protected static int sLocalRecordPoolMaxSize = 50;

    /** KeepAlive **/
    protected int KEEPALIVE_MINUTE = 5;
    protected long sKeepalive;

//    protected static HashMap<Integer, Boolean>mSourceExtractTable

    //size of record pool. If the number of records exceed the size, we remove outdated
    //record pool or clear the record pool if we save it in the public record pool
    private int mSizeOfRecordPool = 300;



    /** each ContextStateManager should override this static method
     * it adds a list of ContextSource that it will manage **/
    protected void setUpContextSourceList(){
        return;
    }

    /**Database table name should be defined by each ContextStateManager. So each CSM should overwrite this**/
    public static String getDatabaseTableNameBySourceName (String sourceName) {
        return null;
    }

    /**
     * this function should return a list of database table names for its contextsource. Must implement it
     * in order to create tables
     * @return
     */
    public ArrayList<String> getAllDatabaseTableNames () {
        ArrayList<String> tablenames = new ArrayList<String>();

        return tablenames;
    }

    /**
     * Allow ContextManager to requestd and remove updates
     */
    public void requestUpdates() {}

    public void removeUpdates() {}

    /**
     * Get ContexrSourceType by SourceName
     * @param sourceName
     * @return
     */
    public static int getContextSourceTypeFromName(String sourceName){return -1;};

    /**
     * Get ContexrSourceName by SourceType
     * @param sourceType
     * @return
     */
    public static String getContextSourceNameFromType(int sourceType){return null;};



    public ContextStateManager() {

        recordCount = 0;

        mLocalRecordPool = new ArrayList<Record>();
        mStateMappingRules = new ArrayList<StateMappingRule>();
        mLoggingTasks = new ArrayList<LoggingTask>();
        mStates = new ArrayList<State>();
        mContextSourceList = new ArrayList<ContextSource>();
        mCurrentlyRequestedContextSourceList = new ArrayList<ContextSource>();

        /** add ContextSources into the contextSourceList
         * do this in each ContextStateManager since each may required different access to the
         * phone resource  **/
        
        //setUpContextSourceList();

        //set keepalive
        setKeepalive(KEEPALIVE_MINUTE * Constants.MILLISECONDS_PER_MINUTE);//5 minute;;
    }


    public ContextStateManager(ArrayList<Record> mlocalRecordPool) {
        this.mLocalRecordPool = mlocalRecordPool;
    }


    /*******************************************************************************************/
    /************************************** ContextSource ****************************************/
    /*******************************************************************************************/



    /**
     * Update the Setting of ContextSourceList
     * @param source
     * @param samplingRate
     */
    public void updateContextSourceList(String source, float samplingRate){

        //1. use general source name to update all sources (e.g. ActivityRecognition, Sensor)

        //2. update individual source by souce name .
        return;
    }

    /**
     * Update the Setting of ContextSourceList
     * @param source
     * @param samplingRate
     */
    public void updateContextSourceList(String source, long samplingRate){

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

    /**
     *For each ContextSource we check whether it is requested and update its request status
     */
    protected void updateContextSourceListRequestStatus() {

        boolean isRequested = false;

        //update each contextsource
        for (int i=0; i<mContextSourceList.size(); i++){
            mContextSourceList.get(i).setIsRequested(updateContextSourceRequestStatus(mContextSourceList.get(i)));
            Log.d(LOG_TAG, "[updateContextSourceListRequestStatus] check saving data the contextsource " + mContextSourceList.get(i).getName() + " requested: " + mContextSourceList.get(i).isRequested());

            isRequested = mContextSourceList.get(i).isRequested();

            //If neither AllProbableActivities nor MostProbableActivity are requested, we should stop requesting activity information
            if (isRequested){
                Log.d(LOG_TAG, "[updateContextSourceListRequestStatus], stop requesting informatoin because it is not needed anymore");

                //TODO: check if the contextsource is currently getting update, if not, start update
            }
            else {
                //TODO: check if the contextsource is currently getting update, if yes, remove update

            }
        }
    }

    /**
     * We check whether a ContextSource is requested by checking whether it is in the ActiveLoggingTask List or in a StateMappingRuleList
     * @param contextSource
     * @return
     */
    protected boolean updateContextSourceRequestStatus(ContextSource contextSource) {

//        boolean isLoggingInBackGroundLogging = isRequestedByBackgroundLoggingTasks(contextSource);
        boolean isLoggingInAction = isRequestedByActiveLoggingTasks(contextSource);

        boolean isMonitored = isStateMonitored(contextSource.getSourceId());
        boolean isRequested = isLoggingInAction | isMonitored ;

        Log.d(LOG_TAG, "[testing logging task and requested][updateContextSourceListRequestStatus] " +
                "the contextSource " + contextSource.getName() +
                " ActionLogging " + isLoggingInAction + " monitored : " + isMonitored + " isRequested: " + isRequested);

        return isRequested;

    }

    /**
     * Check whether a ContextSource is requested
     * @param sourceName
     * @return
     */
    protected  boolean checkRequestStatusOfContextSource(String sourceName) {

        for (int i=0; i<mContextSourceList.size(); i++){
            if (sourceName.equals(mContextSourceList.get(i).getName())) {
                return mContextSourceList.get(i).isRequested();
            }
        }
        return false;
    }


    /*******************************************************************************************/
    /********************************* Logging Task Related ************************************/
    /*******************************************************************************************/


    /**
     * the function add the logging task into the ActiveLoggingTask.
     * Then it calls updateContextSourceListRequestStatus to update whether contextsource is requested.
     * @param loggingTask
     */
    public void addLoggingTask(LoggingTask loggingTask) {
        //add ActiveLoggingTask
        mLoggingTasks.add(loggingTask);
    }

    /**
     * ContextManager will update the LoggingTasks, i.e. make them active or inactive. This is
     * @param loggingTask
     */
    public void updateLoggingTask(LoggingTask loggingTask, boolean enabled ) {

        Log.d(LOG_TAG, " [testing logging task and requested] update the status of loggingTask " + loggingTask + " to " + enabled);

        for (int i=0; i<mLoggingTasks.size(); i++){
            //if we find the logging task
            if (loggingTask.equals(mLoggingTasks.get(i))){
                mLoggingTasks.get(i).setEnabled(enabled);
                Log.d(LOG_TAG, " [testing logging task and requested] the loggingTask " + loggingTask.getSource() + " is updated to " + mLoggingTasks.get(i).isEnabled());
            }
        }

        //after add a logging task, we need to update the Request Status of the ContextSourceList
        updateContextSourceListRequestStatus();

    }


    /**
     * copy records from LocalRecordPool to PublicRecordPool
      */
    public void copyRecordsToPublicRecordPool() {

        int count = 0;

        for (int i=0; i<mLocalRecordPool.size(); i++ ) {

            Record record = mLocalRecordPool.get(i);

            /** we only copied records that have not been copied to the PublicPool **/
            if (!record.isCopiedToPublicPool()){
                ContextManager.getPublicRecordPool().add(mLocalRecordPool.get(i));
                //after the record has been copied, we need to mark it "copie", so that we won't copied again
                record.setIsCopiedToPublicPool(true);
//                Log.d(LOG_TAG, this.getName() + "[test logging] copying record " + record.getSourceType() + ":" + record.getID()  + " : " + record.getTimeString() + " to public pool" );
                count ++;
            }
        }

//        Log.d(LOG_TAG, this.getName() + "[test logging] moving " + count + " records to public pool, local pool now has " + mLocalRecordPool.size() +
//                " records.  pubolc pool has " + ContextManager.getPublicRecordPool().size() + "records");

    }


    public ArrayList<Record> getLocalRecordPool() {
        return mLocalRecordPool;
    }


    /**
     * ContextStateMAnager needs to override this fundtion to create data content for a Record
     */
    protected void saveRecordToLocalRecordPool () {

        /** store values into a Record so that we can store them in the local database **/
        Record record = new Record();
        record.setTimestamp(ContextManager.getCurrentTimeInMillis());
        record.setSource("YOUR OWN SOURCE TYPE");

        /** create data in a JSON Object. Each CotnextSource will have different formats.
         * So we need each ContextSourceMAnager to implement this part**/
        JSONObject data = new JSONObject();

        // ....
        //implement data....put values of contextsources into data. we expect to see different
        //contextsources have different formats.
        // ....

        /*** Set data to Record **/
        record.setData(data);

        /** Save Record**/
        addRecord(record);

    }


    /**
     * this function remove old record (depending on the maximum size of the local pool)
     */
    protected void removeOutDatedRecord() {

        for (int i=0; i<mLocalRecordPool.size(); i++) {

            Record record = mLocalRecordPool.get(i);

            //calculate time difference
            long diff =  ContextManager.getCurrentTimeInMillis() - mLocalRecordPool.get(i).getTimestamp();

            //remove outdated records.
            if (diff >= sKeepalive){
                mLocalRecordPool.remove(record);
                Log.d(LOG_TAG, "[test logging]remove record " + record.getSource() + record.getID() + " logged at " + record.getTimeString() + " to " + this.getName());

                i--;
            }
        }
    }

    /**
     * this function add record and also remove old record (depending on the maximum size of the local pool)
     * @param record
     */
    protected void addRecord(Record record) {

        /**1. add record to the local pool **/
        long id = recordCount++;
        record.setID(id);

        mLocalRecordPool.add(record);
//        Log.d(LOG_TAG, "[test logging]add record " + record.getSourceType() +record.getID() + "logged at " + record.getTimeString() + " to " + this.getName() );

        /**2. check whether we should remove old record **/
        removeOutDatedRecord();


    }

    public Record getLastSavedRecord (){
        if (mLocalRecordPool.size()>0)
            return mLocalRecordPool.get(mLocalRecordPool.size()-1);
        else
            return null;
    }


    /*******************************************************************************************/
    /********************************* Monitoring Related** ************************************/
    /*******************************************************************************************/


    /** if the value of the state is changed, we inform ContextManager about the change so that it can
     * examine the conditions of the events related to the state **/
    public void stateChanged(State state){
        Log.d(LOG_TAG, "test SMR [stateChanged] state " + state.getName() + " is changed");
                ContextManager.examineSituations(state);
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
        Log.d(LOG_TAG, "[test SMR] creating state: " + state.getName() + "  current value: " + state.getValue());
    }

    /**
     * these functions should be implemented in a ContextStateManager. It examines StateMappingRule with the
     * data and returns a boolean pass.
     * @param sourceType
     * @param measure
     * @param relationship
     * @param targetValue
     * @return
     */
    protected boolean examineStateRule(int sourceType, String measure, String relationship, String targetValue, ArrayList<String> params ){
        boolean pass = false;
        String sourceValue=null;
        if (sourceValue != null) {

            pass = satisfyCriterion(sourceValue, relationship, targetValue);
//                Log.d(LOG_TAG, "examine statemappingrule, get measure "
//                        + getContextSourceNameFromType(sourceType) + " and get value : " +  sourceValue +
//                        "now examine target value : " + targetValue + " so the pass is : " + pass);

        }
        return pass;
    }

    protected boolean examineStateRule(int sourceType, String measure, String relationship, float targetValue,  ArrayList<String> params ){
        boolean pass = false;
        float sourceValue=-1;
        if (sourceValue != -1) {

            pass = satisfyCriterion(sourceValue, relationship, targetValue);
//                Log.d(LOG_TAG, "examine statemappingrule, get measure "
//                        + getContextSourceNameFromType(sourceType) + " and get value : " +  sourceValue +
//                        "now examine target value : " + targetValue + " so the pass is : " + pass);

        }
        return pass;
    }

    protected boolean examineStateRule(int sourceType, String measure, String relationship, int targetValue,  ArrayList<String> params ){
        boolean pass = false;
        int sourceValue=-1;
        if (sourceValue != -1) {

            pass = satisfyCriterion(sourceValue, relationship, targetValue);
//                Log.d(LOG_TAG, "examine statemappingrule, get measure "
//                        + getContextSourceNameFromType(sourceType) + " and get value : " +  sourceValue +
//                        "now examine target value : " + targetValue + " so the pass is : " + pass);

        }
        return pass;
    }

    protected boolean examineStateRule(int sourceType, String measure, String relationship, boolean targetValue,  ArrayList<String> params ){
        boolean pass = false;

//            pass = satisfyCriterion(sourceValue, relationship, targetValue);
//                Log.d(LOG_TAG, "examine statemappingrule, get measure "
//                        + getContextSourceNameFromType(sourceType) + " and get value : " +  sourceValue +
//                        "now examine target value : " + targetValue + " so the pass is : " + pass);

//        }
        return pass;
    }

    protected void updateStateValues(String sourceName) {
        updateStateValues(getContextSourceTypeFromName(sourceName) );
    }


    /**
     * We should call this function at place before we save the records to the local pool
     * updateStates()
     * ContextStateManager check the value for each countextual source and determine whether to
     * change the value of the state for every 5 seconds When a ContextStateManager check the values and update states
     * depends on the sampling rate and how it obtains the value (pull vs. push)
     */
    protected void updateStateValues(int sourceType) {

        /** 1. we first make sure whether the sourceType is being monitored. If not, we don't need to update
         //the state values. We call isStateMonitored(int SourceType) to do the examination. **/

        Log.d(LOG_TAG, "test SMR examine statemappingrule, the state is being monitored: " + isStateMonitored(sourceType));

        if (!isStateMonitored(sourceType)) {
            return;
        }

        /** 2. if the state is currently monitored, we get the stateMappingRule by the type
         * then we call examineStateRule() to examine the rule depending on the type of the target value
         * Currently, it could be a string or a float number**/
        ArrayList<StateMappingRule> relevantStateMappingRules = getStateMappingRules(sourceType);

        for (int i=0; i<relevantStateMappingRules.size(); i++) {

            //get the rule based on the source type. We don't examine all MappingRule, but only the rule
            //that are related to the source.

            StateMappingRule rule = relevantStateMappingRules.get(i);

            Log.d(LOG_TAG, "test SMR app, examine statemappingrule " + rule.getName() );

            boolean pass= false;

            //each rule can have more than one criterion, where each criterion has a measure,
            // relationship and a targetvalue
            ArrayList<StateValueCriterion> criteria = rule.getCriteria();

            for (int j=0; j<criteria.size(); j++){

                //1. get the targer value and relaionship
                String relationship = criteria.get(j).getRelationship();
                String measure = criteria.get(j).getMeasure();

                Log.d(LOG_TAG, "test SMR app, examine statemappingrule " + relationship + " " + measure
                 + criteria.get(j).getTargetValue() );


                //some rules have additional parameters (e.g. latlng for location)
                ArrayList<String> params = criteria.get(j).getParameters();

                //get values depending on whether the target value is a string or a float number
                if (criteria.get(j).getTargetValue() instanceof String){
                    Log.d(LOG_TAG, "test SMR app examine statemappingrule, Going to test source: " + sourceType + " : " + rule.getSource()
                                    + " measure : " + measure + " relationship " + relationship + " targevalue " + (String)criteria.get(j).getTargetValue()

                    );
                    pass = examineStateRule(sourceType, measure, relationship, (String)criteria.get(j).getTargetValue() ,params);
                }
                //the target value is a number
                else if (criteria.get(j).getTargetValue() instanceof Float) {

//                    Log.d(LOG_TAG, "test SMR examine statemappingrule, Going to test source: " + sourceType + " : " + rule.getSource()
//                                    + " measure : " + measure + " relationship " + relationship + " targevalue " + (Integer)criteria.get(j).getTargetValue()
//                    );
                    pass = examineStateRule(sourceType, measure, relationship, (Float) criteria.get(j).getTargetValue(), params);
                }

                //the target value is a number
                else if (criteria.get(j).getTargetValue() instanceof Integer) {

//                    Log.d(LOG_TAG, "test SMR examine statemappingrule, Going to test source: " + sourceType + " : " + rule.getSource()
//                                    + " measure : " + measure + " relationship " + relationship + " targevalue " + (Integer)criteria.get(j).getTargetValue()
//                    );
//                    pass = examineStateRule(sourceType, measure, relationship, (Integer) criteria.get(j).getTargetValue(), params);
//
                }

                /** examine criterion specified in the SateMappingRule **/
                Log.d(LOG_TAG, "test SMR examine statemappingrule, after the examination the criterion is " + pass);

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

                        Log.d(LOG_TAG, "test SMR examine statemappingrule, the state " + getStateList().get(j).getName() + " value change to " + getStateList().get(j).getValue());

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

            if (contextSource.getSourceId()==mStateMappingRules.get(i).getSourceType()){
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

        for (int i=0; i<mLoggingTasks.size(); i++) {
//
//            Log.d(LOG_TAG, "[testing logging task and requested] isRequestedByActiveLoggingTasks " +
//                    "checking ContextSource " + contextSource.getName() + " with logging task" +
//                    mLoggingTasks.get(i).getSourceType());
//
//            Log.d(LOG_TAG, "[testing logging task and requested] comparing " +
//                    "ContextSourceName " + contextSource.getName() + " with source in LoggingTask Name" +
//                    mLoggingTasks.get(i).getSourceType());

            //find the logging task containing the contextsource and see if the loggingTask is enabled
            if (contextSource.getName().equals( mLoggingTasks.get(i).getSource() )
                    &&  mLoggingTasks.get(i).isEnabled() ){
                Log.d(LOG_TAG, "[testing logging task and requested] the ContextSource " + contextSource.getName() +
                        " indeed is requested by the logging task" );

                return true;
            }
        }
        return false;
    }

//    /**
//     * This function takes a ContextSource and examines whether it is requested by a BackgroundRecording
//     * @param contextSource
//     * @return
//     */
//    protected boolean isRequestedByBackgroundLoggingTasks(ContextSource contextSource) {
//
//        Log.d(LOG_TAG, "[testing logging task and requested] check whether ContextSource " + contextSource.getName() +
//                " is requested  by BackgroundLogging ");
//
//        boolean isRequested = false;
//
//        //find the logging Task and see if that's in backgroundLogging
//        for (int i=0; i<mLoggingTasks.size(); i++) {
//
//            Log.d(LOG_TAG, "[testing logging task and requested] checking loggingTask" +   mLoggingTasks.get(i).getSourceType());
//
//            //find the loggingtask containing the contextsource
//            if (contextSource.getName().equals( mLoggingTasks.get(i).getSourceType() )){
//                Log.d(LOG_TAG, "[testing logging task and requested] find the loggingTask " + mLoggingTasks.get(i).getSourceType()
//                 + mLoggingTasks.get(i).getId());
//
//                //find if that's in BackgroundLogging (using id)
//                isRequested = ContextManager.isLoggingTaskContainedInBackGroundLogging(mLoggingTasks.get(i).getId());
//
//            }
//        }
//
//        Log.d(LOG_TAG, "[testing logging task and requested] the loggingTask requested by BackgroundLogging " + isRequested);
//
//        return isRequested;
//    }


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
            if (state.getMappingRule().getSourceType()==sourceType && state.isEnabled()){
                Log.d(LOG_TAG, "examine statemappingrule: state " + state.getName() + " is monitored");
                return true;
            }
        }
        return false;
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
            if (rule.getSourceType()==sourceType){
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


    public void removeActiveLoggingTask (LoggingTask loggingTask) {
        mLoggingTasks.remove(loggingTask);
    }

    public void addStateMappingRule(StateMappingRule rule){

        //we know that there's some specific information in condition that needs to be translated in specific
        //contextStateManager, so we need call translateStateMappingRule before we add the rule.
        StateMappingRule translatedRule = translateStateMappingRule(rule);

        mStateMappingRules.add(translatedRule);
        Log.d(LOG_TAG, "[test SMR] adding rule: " + rule.toString() + " to " + getName()
         + " the criteria is " + rule.getCriteria()
         + " the time criteria is " + rule.getTimeCriteria());

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
    protected static boolean satisfyCriterion(String value, String relationship, String targetValue ){

        boolean pass=false;



        if (relationship.equals(STATE_MAPPING_RELATIONSHIP_EQUAL)){
            if (value.equals(targetValue)) pass = true;
        }
        else if (relationship.equals(STATE_MAPPING_RELATIONSHIP_NOT_EQUAL)){
            if (!value.equals(targetValue)) pass = true;
        }
        else if (relationship.equals(STATE_MAPPING_RELATIONSHIP_CONTAIN)){
            if (value.contains(targetValue)) pass = true;
        }

        Log.d(LOG_TAG, "test smr app [examine statemappingrule] comparing value " + value +" and targetvalue " + targetValue + " rel: " + relationship  + " pass: " + pass) ;

        return pass;

    }

    protected static boolean satisfyCriterion(float value, String relationship, float targetValue ) {

//        Log.d(LOG_TAG, " test smr locaiton [examine statemappingrule]" );

        boolean pass=false;

        if (relationship.equals(STATE_MAPPING_RELATIONSHIP_NUMBER_EQUAL)){
            if (value==targetValue) pass = true;
        }else if (relationship.equals(STATE_MAPPING_RELATIONSHIP_NUMBER_NOT_EQUAL)){
            if (value!=targetValue)  pass = true;
        }
        else if (relationship.equals(STATE_MAPPING_RELATIONSHIP_LARGER)){
            if (value>targetValue) pass = true;
        }
        else if (relationship.equals(STATE_MAPPING_RELATIONSHIP_LARGER_AND_EQUAL)){
            if (value>=targetValue) pass = true;
        }
        else if (relationship.equals(STATE_MAPPING_RELATIONSHIP_SMALLER)){
            if (value<targetValue) pass = true;
        }
        else if (relationship.equals(STATE_MAPPING_RELATIONSHIP_SMALLER_AND_EQUAL)){
            if (value<=targetValue) pass = true;
        }


        Log.d(LOG_TAG, "test smr locaiton  comparing value " + value +" and targetvalue " + targetValue + " relship: " + relationship  + " pass: " + pass) ;

        return pass;

    }


    protected static boolean satisfyCriterion(boolean value, String relationship, boolean targetValue ) {

//        Log.d(LOG_TAG, " test smr locaiton [examine statemappingrule]" );

        boolean pass=false;

        if (relationship.equals(STATE_MAPPING_RELATIONSHIP_NUMBER_EQUAL)){
            if (value==targetValue) pass = true;
        }

        Log.d(LOG_TAG, "test smr locaiton  comparing value " + value +" and targetvalue " + targetValue + " relship: " + relationship  + " pass: " + pass) ;

        return pass;

    }

    public ArrayList<ContextSource> getContextSourceList() {
        return mContextSourceList;
    }

    protected boolean isContextSourceRequested(ContextSource source) {

        for (int i=0; i<mContextSourceList.size(); i++){
            if (source.equals(mContextSourceList.get(i))){
                Log.d(LOG_TAG, "isContextSourceRequested : source " + source.getName() + " is requested");
               return source.isRequested();
            }
        }
        Log.d(LOG_TAG, "isContextSourceRequested : source " + source.getName() + " is NOT requested");
        return false;
    }


    public long getKeepalive() {
        return sKeepalive;
    }

    public void setKeepalive(long keepalive) {
        this.sKeepalive = keepalive;
    }

//    public static int getRelationship(String  relationshipName) {
//
//        if (relationshipName==STATE_MAPPING_RELATIONSHIP_EQUAL_STRING){
//            return STATE_MAPPING_RELATIONSHIP_EQUAL;
//        }
//        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_NOT_EQUAL_STRING){
//            return STATE_MAPPING_RELATIONSHIP_NOT_EQUAL;
//        }
//        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_LARGER_STRING){
//            return STATE_MAPPING_RELATIONSHIP_LARGER;
//        }
//        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_SMALLER_STRING){
//            return STATE_MAPPING_RELATIONSHIP_SMALLER;
//        }
//        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_LARGER_AND_EQUAL_STRING){
//            return STATE_MAPPING_RELATIONSHIP_LARGER_AND_EQUAL;
//        }
//        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_SMALLER_AND_EQUAL_STRING){
//            return STATE_MAPPING_RELATIONSHIP_SMALLER_AND_EQUAL;
//        }
//        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_BETWEEN_STRING){
//            return STATE_MAPPING_RELATIONSHIP_BETWEEN;
//        }
//        else if (relationshipName==STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN_STRING){
//            return STATE_MAPPING_RELATIONSHIP_STRING_CONTAIN;
//        }
//        else
//            return -1;
//    }

    public ContextSource getContextSourceBySourceId(int sourceId) {

        for (int i=0; i<mContextSourceList.size(); i++){
            if (mContextSourceList.get(i).getSourceId()==sourceId)
                return mContextSourceList.get(i);
        }
        return null;
    }

    public boolean isContextSourceInCurrentRequestedList(String sourceName) {

        for (int i=0; i<mCurrentlyRequestedContextSourceList.size(); i++){
            if (mCurrentlyRequestedContextSourceList.get(i).getName().equals(sourceName)){
                return true;
            }
        }
            return false;
    }



    public ContextSource getContextSourceBySourceName(String sourceName) {

        for (int i=0; i<mContextSourceList.size(); i++){
            if (mContextSourceList.get(i).getName().equals(sourceName))
                return mContextSourceList.get(i);
        }
        return null;
    }


}
