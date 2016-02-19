package edu.umich.si.inteco.minuku.context.ContextStateManagers;

/**
 * Created by Armuro on 10/2/15.
 */
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.ContextSource;
import edu.umich.si.inteco.minuku.model.Record.Record;

public class PhoneSensorManager extends ContextStateManager implements SensorEventListener {


    /** Tag for logging. */
    private static final String LOG_TAG = "PhoneSensorMnger";

    public static String CONTEXT_SOURCE_PHONE_SENSOR = "PhoneSensor";


    /**Table Name**/
    public static final String RECORD_TABLE_NAME_SENSOR_ACCELEROMETER = "Record_Table_Accelerometer";
    public static final String RECORD_TABLE_NAME_SENSOR_GRAVITY  = "Record_Table_Sensor_Gravity";
    public static final String RECORD_TABLE_NAME_SENSOR_GYRSCOPE = "Record_Table_Sensor_Gyroscope";
    public static final String RECORD_TABLE_NAME_SENSOR_ACCELERATION = "Record_Table_Sensor_Linear_Acceleration";
    public static final String RECORD_TABLE_NAME_SENSOR_ROTATION_VECTOR = "Record_Table_Sensor_Rotation_Vector";
    public static final String RECORD_TABLE_NAME_SENSOR_MAGNETIC_FIELD = "Record_Table_Sensor_Magnetic_Field";
    public static final String RECORD_TABLE_NAME_SENSOR_ORIENTATION = "Record_Table_Sensor_Orientation";
    public static final String RECORD_TABLE_NAME_SENSOR_PROXIMITY = "Record_Table_Sensor_Proximity";
    public static final String RECORD_TABLE_NAME_SENSOR_AMBIENT_TEMPERATURE = "Record_Table_Sensor_Ambient_Temperature";
    public static final String RECORD_TABLE_NAME_SENSOR_LIGHT = "Record_Table_Sensor_Light";
    public static final String RECORD_TABLE_NAME_SENSOR_PRESSURE = "Record_Table_Sensor_Pressure";
    public static final String RECORD_TABLE_NAME_SENSOR_HUMIDITY = "Record_Table_Sensor_Humidity";
    public static final String RECORD_TABLE_NAME_SENSOR_HEART_RATE = "Record_Table_Sensor_Hear_Rate";
    public static final String RECORD_TABLE_NAME_SENSOR_STEP_COUNTER = "Record_Table_Sensor_Step_Counter";
    public static final String RECORD_TABLE_NAME_SENSOR_STEP_DETECTION = "Record_Table_Sensor_Step_Detection";


    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_NAME = "SensorValues";

    /**system components**/
    private static Context mContext;
    private static SensorManager mSensorManager ;

    public static final String PHONE_SENSOR_ACCELEROMETER = "Sensor.Accelerometer";
    public static final String PHONE_SENSOR_LINEAR_ACCELERATION = "Sensor.LinearAcceleration";
    public static final String PHONE_SENSOR_ROTATION_VECTOR = "Sensor.RotationVector";
    public static final String PHONE_SENSOR_GRAVITY = "Sensor.Gravity";
    public static final String PHONE_SENSOR_GYROSCOPE = "Sensor.Gyroscope";
    public static final String PHONE_SENSOR_LIGHT = "Sensor.Light";
    public static final String PHONE_SENSOR_MAGNETIC_FIELD = "Sensor.MagneticField";
    public static final String PHONE_SENSOR_PRESSURE = "Sensor.Pressure";
    public static final String PHONE_SENSOR_PROXIMITY = "Sensor.Proximity";
    public static final String PHONE_SENSOR_AMBIENT_TEMPERATURE = "Sensor.AmbientTemperature";
    public static final String PHONE_SENSOR_RELATIVE_HUMIDITY = "Sensor.RelativeHumidity";
    public static final String PHONE_SENSOR_STEP_COUNTER = "Sensor.StepCounter";
    public static final String PHONE_SENSOR_STEP_DETECTOR = "Sensor.StepDetector";
    public static final String PHONE_SENSOR_HEART_RATE = "Sensor.HeartRate";

    /**Motion Sensors**/
    private static float mAccele_x, mAccele_y, mAccele_z;
    private static float mGyroscope_x, mGyroscope_y, mGyroscope_z;
    private static float mGravity_x, mGravity_y, mGravity_z;
    private static float mLinearAcceleration_x, mLinearAcceleration_y, mLinearAcceleration_z;
    private static float mRotationVector_x_sin, mRotationVector_y_sin, mRotationVector_z_sin, mRotationVector_cos;
    private static float mHeartRate, mStepCount, mStepDetect;

    /**Position Sensors**/
    private static float mProximity ;
    private static float mMagneticField_x, mMagneticField_y, mMagneticField_z;

    private float mLight, mPressure, mRelativeHumidity, mAmbientTemperature ;

    /**Store a list of sensosr that PhoneSensorManager has registered. PhoneSensorManager only obtains values from registered sensors */
    private ArrayList<ContextSource> mRegisteredSensorList;

    public PhoneSensorManager(Context context) {

        super();
        Log.d(LOG_TAG, "[testing start service] going to start the context extractor");

        mContext = context;

        //call sensor manager from the service
        mSensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);

        //initiate values of sensors
        mAccele_x = mAccele_y = mAccele_z = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_FLOAT; //-9999
        mGyroscope_x = mGyroscope_y = mGyroscope_z = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mGravity_x = mGravity_y = mGravity_z = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mMagneticField_x = mMagneticField_y = mMagneticField_z = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mLinearAcceleration_x = mLinearAcceleration_y = mLinearAcceleration_z = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mRotationVector_x_sin = mRotationVector_y_sin =  mRotationVector_z_sin = mRotationVector_cos = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mHeartRate = mStepCount = mStepDetect =ContextManager.CONTEXT_SOURCE_INVALID_VALUE_FLOAT;
        mLight = mPressure = mRelativeHumidity = mProximity = mAmbientTemperature =ContextManager.CONTEXT_SOURCE_INVALID_VALUE_FLOAT;

        //initiate registered sneosr list
        mRegisteredSensorList = new ArrayList<ContextSource>();

        setUpContextSourceList();

    }

    /** each ContextStateManager should override this static method
     * it adds a list of ContextSource that it will manage **/
    @Override
    protected void setUpContextSourceList(){

        Log.d(LOG_TAG, "testing registerSensor in setUpContextSourceList");

        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_ACCELEROMETER,
                        Sensor.TYPE_ACCELEROMETER,
                        //if it is not null, return true, else, return false
                        (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null),
                        SensorManager.SENSOR_DELAY_NORMAL));


        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_LINEAR_ACCELERATION,
                        Sensor.TYPE_LINEAR_ACCELERATION,
                        (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!=null),
                        SensorManager.SENSOR_DELAY_NORMAL));

        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_ROTATION_VECTOR,
                        Sensor.TYPE_ROTATION_VECTOR,
                        (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null),
                        SensorManager.SENSOR_DELAY_NORMAL));

        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_GRAVITY,
                        Sensor.TYPE_GRAVITY,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null,
                        SensorManager.SENSOR_DELAY_NORMAL));


        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_GYROSCOPE,
                        Sensor.TYPE_GYROSCOPE,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null,
                        SensorManager.SENSOR_DELAY_NORMAL));


        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_LIGHT,
                        Sensor.TYPE_LIGHT,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!=null,
                        SensorManager.SENSOR_DELAY_NORMAL));

        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_MAGNETIC_FIELD,
                        Sensor.TYPE_MAGNETIC_FIELD,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!=null,
                        SensorManager.SENSOR_DELAY_NORMAL));


        mContextSourceList.add(
                new ContextSource(
                PHONE_SENSOR_PRESSURE,
                Sensor.TYPE_PRESSURE,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)!=null,
                        SensorManager.SENSOR_DELAY_NORMAL));

        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_PROXIMITY,
                        Sensor.TYPE_PROXIMITY,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)!=null,
                        SensorManager.SENSOR_DELAY_NORMAL));

        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_AMBIENT_TEMPERATURE,
                        Sensor.TYPE_AMBIENT_TEMPERATURE,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)!=null,
                        SensorManager.SENSOR_DELAY_NORMAL));


        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_RELATIVE_HUMIDITY,
                        Sensor.TYPE_RELATIVE_HUMIDITY,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)!=null,
                        SensorManager.SENSOR_DELAY_NORMAL));


        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_STEP_COUNTER,
                        Sensor.TYPE_STEP_COUNTER,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!=null,
                        SensorManager.SENSOR_DELAY_NORMAL));

        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_STEP_DETECTOR,
                        Sensor.TYPE_STEP_DETECTOR,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)!=null,
                        SensorManager.SENSOR_DELAY_NORMAL));

        mContextSourceList.add(
                new ContextSource(
                        PHONE_SENSOR_HEART_RATE,
                        Sensor.TYPE_HEART_RATE,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)!=null,
                        SensorManager.SENSOR_DELAY_NORMAL));

        Log.d(LOG_TAG, "testing registerSensor in setUpContextSourceList " + mContextSourceList.size() + " contextsources have been initiated");


        return;

    }

    /** this function allows ConfigurationManager to adjust the configuration of each ContextSource,
     * e.g sampling rate. */
    public void updateContextSourceList(String source, long samplingRate){

        Log.d(LOG_TAG, "testing registerSensor in updateContextSourceList ");


        //update all sources if the source name is a general name (e.g. ActivityRecognition)
        if (source.equals(CONTEXT_SOURCE_PHONE_SENSOR)) {
            for (int i=0; i<mContextSourceList.size(); i++){
                getContextSourceBySourceName(mContextSourceList.get(i).getName()).setSamplingRate(samplingRate);
            }
        }

        //if not using a general name, update individual sources by source name
        else {
            if (getContextSourceBySourceName(source)!=null){
                getContextSourceBySourceName(source).setSamplingRate(samplingRate);
            }

        }

        return;
    }

    /**
     *For each ContextSource we check whether it is requested and update its request status
     */
    @Override
    protected void updateContextSourceListRequestStatus() {

        boolean isRequested = false;

        for (int i=0; i<mContextSourceList.size(); i++){

            ContextSource source = mContextSourceList.get(i);

            mContextSourceList.get(i).setIsRequested(updateContextSourceRequestStatus(source));
            Log.d(LOG_TAG, "[updateContextSourceListRequestStatus] check saving data the contextsource "
                    + source.getName() + " requested: " + source.isRequested());

            isRequested = mContextSourceList.get(i).isRequested();

            //If neither AllProbableActivities nor MostProbableActivity are requested, we should stop requesting activity information
            if (isRequested){

                //TODO: check if the contextsource is currently getting update, if not, start update
                if (!mRegisteredSensorList.contains(source)){
                    registerRequestedSensor(source);
                }
            }
            else {

                Log.d(LOG_TAG, "[updateContextSourceListRequestStatus], stop requesting informatoin because it is not needed anymore");

                //TODO: check if the contextsource is currently getting update, if yes, remove update
                if (!mRegisteredSensorList.contains(source)){
                    unRegisterSensor(source);
                }
            }
        }
    }


    /** this takes samplingMode as a String value and then use SensorManager's four update delay
     * Normal, UI, Game, and Fatest */
    public void updateContextSourceList(String source, String samplingMode){

        //1. use general source name to update all sources (e.g. ActivityRecognition, Sensor)

        //2. update individual source by souce name .



        return;
    }


    /**
     * let ContextManager to call to unregister sensors
     */
    protected void disableSensor() {
        unRegisterSensors();
    }

    /**
     * let ContextManager to call to unregister sensors
     */
    @Override
    public void requestUpdates() {
        //only register the requested sensors
        registerRequestedSensors();
    }

    /**
     * let ContextManager to unregister sensors
     */
    @Override
    public void removeUpdates() {
        unRegisterSensors();
    }

    /*
    protected void removeRequestedSensors () {
        mRequestedSensorList.clear();
    }

    protected boolean removeRequestSensor (int sensorType) {

        for (int i=0; i<mRequestedSensorList.size(); i++){

            //if we found the specified sensortype in the request list, remove it and return true
            if (mRequestedSensorList.get(i)==sensorType){
                mRequestedSensorList.remove(i);
                return true;
            }
        }

        return false;
    }

    protected void addRequestSensor (int sensorType){

        //check if the requested type is already in the list
        for (int i=0; i<mRequestedSensorList.size(); i++){
            //if we found the specified sensortype in the request list, remove it and return true
            if (mRequestedSensorList.get(i)==sensorType){
                //if already in the list, return false
                return ;
            }
        }

        //if the requested Sensor Type is not in the mRequestedSensorList yet, add it in the list
        mRequestedSensorList.add(sensorType);

    }
    */


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }


    public static int getContextSourceTypeFromName(String sourceName) {
        return 0;
    }

    public static String getContextSourceNameFromType(int sourceType) {
        return "NA";
    }

    public static void updateStateValues() {

    }


    /**
     * register all the available and requested sensor list
     */

    protected  void registerRequestedSensor(ContextSource sensor) {

        Log.d(LOG_TAG, "[updateContextSourceListRequestStatus] now checking sensor " +sensor.getName() + " requested ? " + sensor.isRequested() + " avaiiability: " + sensor.isAvailable() );

        //only register sensor that is available and is requested.
        if (sensor.isAvailable() && sensor.isRequested()) {

            //we register sensor using its name and samplingMode: Normal, UI, Game, or Fastest
            boolean result =registerSensorByName(sensor.getName(), sensor.getSamplingMode());

            //if registering the requested sensor succesfully, put it in the registered sensor list
            if (result){
                Log.d(LOG_TAG, "[updateContextSourceListRequestStatus] add Sensor :" + sensor.getName() + " to the  mRegisteredSensorList");
                mRegisteredSensorList.add(sensor);
            }

        }

    }

    /**
     * register all the available and requested sensor list
     */

    protected  void registerRequestedSensors() {

        Log.d(LOG_TAG, "testing registerSensor in updateContextSourceList \");\n] registerRequestedSensor, the size of mContextSource is" + mContextSourceList.size());
        /** If for some reason you do need to change the delay, you will have to unregister and reregister the sensor listener.
         *
         */
        for (int i=0; i<mContextSourceList.size(); i++){

            ContextSource  sensor = mContextSourceList.get(i);
            Log.d(LOG_TAG, "[testing Sensor] now checking sensor " +sensor.getName() + " requested ? " + sensor.isRequested() + " avaiiability: " + sensor.isAvailable() );

            //register the sensor
            registerRequestedSensor(sensor);

        }
    }

    /**
     * unregister all the sensors
     */
    protected void unRegisterSensor(ContextSource source){
       
        //unregister
        unregisterSensorByName(source.getName(), source.getSamplingMode());

        //clear the mRegisteredSensorList too.
        mRegisteredSensorList.remove(source);
    }

    
    /**
     * unregister all the sensors
     */
    protected void unRegisterSensors(){
        mSensorManager.unregisterListener(this);

        //clear the mRegisteredSensorList too.
        mRegisteredSensorList.clear();
    }


    /** we need to use the setting: use ContextSourceList to get the setting for each ContextSource**/
    protected boolean registerSensorByName(String name, int mode) {

        boolean result = false;

        switch (name){

            //accelerometer
            case PHONE_SENSOR_ACCELEROMETER:
                result = mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        mode);
                return result;
                //gyriscope
            case PHONE_SENSOR_GYROSCOPE:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                        mode);
                return result;
                //gravity
            case PHONE_SENSOR_GRAVITY:
                result =mSensorManager.registerListener(this,
                            mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                            mode);
                return result;
            case PHONE_SENSOR_ROTATION_VECTOR:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                        mode);
                return result;
            case PHONE_SENSOR_LINEAR_ACCELERATION:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                        mode);
                return result;

            case PHONE_SENSOR_PROXIMITY:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                        mode);
                return result;


            //ambient temperature
            case PHONE_SENSOR_AMBIENT_TEMPERATURE:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
                        mode);
                return result;
            //light
            case PHONE_SENSOR_LIGHT:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                        mode);

                return result;
            //pressure
            case PHONE_SENSOR_PRESSURE:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                        mode);
                return result;

            //humidity
            case PHONE_SENSOR_RELATIVE_HUMIDITY:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY),
                        mode);
                return result;


            case PHONE_SENSOR_HEART_RATE:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE),
                        mode);
                return result;


            case PHONE_SENSOR_STEP_COUNTER:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                        mode);
                return result;


            case PHONE_SENSOR_STEP_DETECTOR:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                        mode);
                return result;
            default:
                return result;

        }
    }

    /** we need to use the setting: use ContextSourceList to get the setting for each ContextSource**/
    protected void unregisterSensorByName(String name, int mode) {

        switch (name){

            //accelerometer
            case PHONE_SENSOR_ACCELEROMETER:

                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)) ;

                return ;
            //gyriscope
            case PHONE_SENSOR_GYROSCOPE:
                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
                return ;
            //gravity
            case PHONE_SENSOR_GRAVITY:
                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));

                return;
            case PHONE_SENSOR_ROTATION_VECTOR:
                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));

                return;
            case PHONE_SENSOR_LINEAR_ACCELERATION:
                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));

                return;

            case PHONE_SENSOR_PROXIMITY:
                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));

                return;


            //ambient temperature
            case PHONE_SENSOR_AMBIENT_TEMPERATURE:
                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE));

                return;
            //light
            case PHONE_SENSOR_LIGHT:
                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));


                return;
            //pressure
            case PHONE_SENSOR_PRESSURE:
                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE));

                return;

            //humidity
            case PHONE_SENSOR_RELATIVE_HUMIDITY:
                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY));

                return;


            case PHONE_SENSOR_HEART_RATE:
                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE));

                return;


            case PHONE_SENSOR_STEP_COUNTER:
                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER));

                return;

            case PHONE_SENSOR_STEP_DETECTOR:
                mSensorManager.unregisterListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR));

                return;
            default:
                return;

        }
    }



    @Override
    public void onSensorChanged(SensorEvent event) {

        /**Motion Sensor**/
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            getAccelerometer(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            getGyroscope(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
            getGravity(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            getLinearAcceleration(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            getRotationVector(event);
        }

        /**Position Sensor**/
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            //Log.d(LOG_TAG, "in [onSensorChange] Proximity: " +  event.values[0] );
            getProximity(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            //Log.d(LOG_TAG, "in [onSensorChange] Proximity: " +  event.values[0] );
            getMagneticField(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR){
            //Log.d(LOG_TAG, "in [onSensorChange] Proximity: " +  event.values[0] );
            getMagneticField(event);
        }

        /**Environment Sensor**/
        if (event.sensor.getType() == Sensor.TYPE_LIGHT){
            getLight(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
            getAmbientTemperature(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE){
            getPressure(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY){
            getRelativeHumidity(event);
        }

        /**health related**/
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE){
            getHeartRate (event);
        }
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            getStepCounter(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            getStepDetector(event);
        }

    }



    /**
     * In PhoneSensorManager, all the values are float numbers
     */
    protected void saveRecordToLocalRecordPool (String sourceName, float[] values) {

        /** store values into a Record so that we can store them in the local database **/
        Record record = new Record();
        record.setTimestamp(ContextManager.getCurrentTimeInMillis());
        record.setSource(sourceName);

        /** create data in a JSON Object. Each CotnextSource will have different formats.
         * So we need each ContextSourceMAnager to implement this part**/
        JSONObject data = new JSONObject();
        JSONArray array = new JSONArray();

        try {

            //put all values in Data JSONObject
            for (int i=0; i< values.length; i++) {
                array.put(values[i]);
            }

            data.put(RECORD_DATA_PROPERTY_NAME, array);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*** Set data to Record **/
        record.setData(data);


        /** Save Record**/
        mLocalRecordPool.add(record);

    }



    /**get Accelerometer values**/
    private void getAccelerometer(SensorEvent event) {


        Log.d(LOG_TAG, "getting accelerometer:" + mAccele_x + " : " +  mAccele_y +  " : " + mAccele_y);

        mAccele_x = event.values[0];	// Acceleration force along the x axis (including gravity). m/s2
        mAccele_y = event.values[1];	// Acceleration force along the y axis (including gravity). m/s2
        mAccele_z = event.values[2];	// Acceleration force along the z axis (including gravity). m/s2

        saveRecordToLocalRecordPool(PHONE_SENSOR_ACCELEROMETER, event.values);
    }


    /**get Gyroscope values**/
    private void getGyroscope(SensorEvent event) {
        mGyroscope_x = event.values[0];	// Rate of rotation around the x axis. rad/s
        mGyroscope_y = event.values[1];	// Rate of rotation around the y axis. rad/s
        mGyroscope_z = event.values[2];	// Rate of rotation around the z axis. rad/s

        saveRecordToLocalRecordPool(PHONE_SENSOR_GYROSCOPE, event.values);

    }


    /**get gravity values**/
    private void getGravity(SensorEvent event) {
        mGravity_x = event.values[0];	// Force of gravity along the x axis m/s2
        mGravity_y = event.values[1];	// Force of gravity along the y axis m/s2
        mGravity_z = event.values[2];	// Force of gravity along the z axis m/s2

        saveRecordToLocalRecordPool(PHONE_SENSOR_GRAVITY, event.values);
    }
    /**get linear acceleration values**/
    private void getLinearAcceleration(SensorEvent event) {
        mLinearAcceleration_x = event.values[0];	//Acceleration force along the x axis (excluding gravity).  m/s2
        mLinearAcceleration_y = event.values[1];	//Acceleration force along the y axis (excluding gravity).  m/s2
        mLinearAcceleration_z = event.values[2];	//Acceleration force along the z axis (excluding gravity).  m/s2

        saveRecordToLocalRecordPool(PHONE_SENSOR_LINEAR_ACCELERATION, event.values);
    }

    /**get rotation vector values**/
    private void getRotationVector(SensorEvent event) {
        mRotationVector_x_sin = event.values[0];	// Rotation vector component along the x axis (x * sin(�c/2))  Unitless
        mRotationVector_y_sin = event.values[1];	// Rotation vector component along the y axis (y * sin(�c/2)). Unitless
        mRotationVector_z_sin = event.values[2];	//  Rotation vector component along the z axis (z * sin(�c/2)). Unitless
        mRotationVector_cos = event.values[3];		// Scalar component of the rotation vector ((cos(�c/2)).1 Unitless

        saveRecordToLocalRecordPool(PHONE_SENSOR_ROTATION_VECTOR, event.values);
    }



    /**get magnetic field values**/
    private void getMagneticField(SensorEvent event){
        mMagneticField_x = event.values[0];	// Geomagnetic field strength along the x axis.
        mMagneticField_y = event.values[1];	// Geomagnetic field strength along the y axis.
        mMagneticField_z = event.values[2];	// Geomagnetic field strength along the z axis.

        saveRecordToLocalRecordPool(PHONE_SENSOR_MAGNETIC_FIELD, event.values);
    }

    /**get proximity values**/
    private void getProximity(SensorEvent event){

//        Log.d(LOG_TAG, "getting proximity" + mProximity);

        mProximity = event.values[0];

        saveRecordToLocalRecordPool(PHONE_SENSOR_PROXIMITY, event.values);
    }

    private void getAmbientTemperature(SensorEvent event){
        /* Environment Sensors */
        mAmbientTemperature = event.values[0];

        saveRecordToLocalRecordPool(PHONE_SENSOR_AMBIENT_TEMPERATURE, event.values);

    }

    private void getLight(SensorEvent event){

        Log.d(LOG_TAG, "getting light" + mLight);

        mLight = event.values[0];

        saveRecordToLocalRecordPool(PHONE_SENSOR_LIGHT, event.values);
    }

    private void getPressure(SensorEvent event){
        mPressure = event.values[0];

        saveRecordToLocalRecordPool(PHONE_SENSOR_PRESSURE, event.values);
    }

    private void getRelativeHumidity(SensorEvent event){
        mRelativeHumidity = event.values[0];

        saveRecordToLocalRecordPool(PHONE_SENSOR_RELATIVE_HUMIDITY, event.values);
    }

    private void getHeartRate (SensorEvent event) {
        mHeartRate = event.values[0];

        saveRecordToLocalRecordPool(PHONE_SENSOR_HEART_RATE, event.values);
    }

    private void getStepCounter (SensorEvent event) {
        mStepCount = event.values[0];

        saveRecordToLocalRecordPool(PHONE_SENSOR_STEP_COUNTER, event.values);

    }

    private void getStepDetector (SensorEvent event) {
        mStepDetect = event.values[0];

        saveRecordToLocalRecordPool(PHONE_SENSOR_STEP_DETECTOR, event.values);
    }


    /**Database table name should be defined by each ContextStateManager. So each CSM should overwrite this**/
    public static String getDatabaseTableNameBySourceName (String sourceName) {

        switch (sourceName) {
            case PHONE_SENSOR_ACCELEROMETER:
                return RECORD_TABLE_NAME_SENSOR_ACCELEROMETER;
            case PHONE_SENSOR_GYROSCOPE:
                return RECORD_TABLE_NAME_SENSOR_GYRSCOPE;
            case PHONE_SENSOR_GRAVITY:
                return RECORD_TABLE_NAME_SENSOR_GRAVITY;
            case PHONE_SENSOR_ROTATION_VECTOR:
                return RECORD_TABLE_NAME_SENSOR_ROTATION_VECTOR;
            case PHONE_SENSOR_LINEAR_ACCELERATION:
                return RECORD_TABLE_NAME_SENSOR_ACCELERATION;
            case PHONE_SENSOR_PROXIMITY:
                return RECORD_TABLE_NAME_SENSOR_PROXIMITY;
            case PHONE_SENSOR_AMBIENT_TEMPERATURE:
                return RECORD_TABLE_NAME_SENSOR_AMBIENT_TEMPERATURE;
            case PHONE_SENSOR_LIGHT:
                return RECORD_TABLE_NAME_SENSOR_LIGHT;
            case PHONE_SENSOR_PRESSURE:
                return RECORD_TABLE_NAME_SENSOR_PRESSURE;
            case PHONE_SENSOR_RELATIVE_HUMIDITY:
                return RECORD_TABLE_NAME_SENSOR_HUMIDITY;
            case PHONE_SENSOR_HEART_RATE:
                return RECORD_TABLE_NAME_SENSOR_HEART_RATE;
            case PHONE_SENSOR_STEP_COUNTER:
                return RECORD_TABLE_NAME_SENSOR_STEP_COUNTER;
            case PHONE_SENSOR_STEP_DETECTOR:
                return RECORD_TABLE_NAME_SENSOR_STEP_DETECTION;
            default:
                return null;
        }
    }

    /**
     * this function should return a list of database table names for its contextsource. Must implement it
     * in order to create tables
     * @return
     */
    @Override
    public ArrayList<String> getAllDatabaseTableNames () {
        ArrayList<String> tablenames = new ArrayList<String>();

        tablenames.add(RECORD_TABLE_NAME_SENSOR_ACCELEROMETER);
        tablenames.add(RECORD_TABLE_NAME_SENSOR_GYRSCOPE);
        tablenames.add(RECORD_TABLE_NAME_SENSOR_GRAVITY);
        tablenames.add(RECORD_TABLE_NAME_SENSOR_ROTATION_VECTOR);
        tablenames.add(RECORD_TABLE_NAME_SENSOR_ACCELERATION);
        tablenames.add(RECORD_TABLE_NAME_SENSOR_PROXIMITY);
        tablenames.add(RECORD_TABLE_NAME_SENSOR_AMBIENT_TEMPERATURE);
        tablenames.add(RECORD_TABLE_NAME_SENSOR_LIGHT);
        tablenames.add(RECORD_TABLE_NAME_SENSOR_PRESSURE);
        tablenames.add(RECORD_TABLE_NAME_SENSOR_HUMIDITY);
        tablenames.add(RECORD_TABLE_NAME_SENSOR_HEART_RATE);
        tablenames.add(RECORD_TABLE_NAME_SENSOR_STEP_COUNTER);
        tablenames.add(RECORD_TABLE_NAME_SENSOR_STEP_DETECTION);

        return tablenames;
    }

    /**get the current time in milliseconds**/
    public static long getCurrentTimeInMillis(){
        //get timzone
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        long t = cal.getTimeInMillis();
        return t;
    }
}
