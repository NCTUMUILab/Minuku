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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.record.SensorRecord;

public class PhoneSensorManager extends ContextStateManager implements SensorEventListener {


    /** Tag for logging. */
    private static final String LOG_TAG = "PhoneSensorMnger";

    //TODO: we set this temporarily false because we want to test nother source first
    private static boolean isExtractingPhoneSensorData = true;

    /**system components**/
    private static Context mContext;
    private SensorManager mSensorManager ;

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

    private float mLight;
    private float mPressure;
    private float mRelativeHumidity;

    /**Store a list of sensors that PhoneSensorManager is requrested to enable*/
    private ArrayList<Integer> mRequestedSensorList;

    /**Store a list of sensosr that PhoneSensorManager has registered*/
    private ArrayList<Integer> mRegisteredSensorList;

    public PhoneSensorManager(Context context) {

        super();
        Log.d(LOG_TAG, "[testing start service] going to start the context extractor");

        mContext = context;

        //call sensor manager from the service
        mSensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);

        //initiate values of sensors
        mAccele_x = mAccele_y = mAccele_z = -9999;
        mGyroscope_x = mGyroscope_y = mGyroscope_z = -9999;
        mGravity_x = mGravity_y = mGravity_z = -9999;
        mMagneticField_x = mMagneticField_y = mMagneticField_z = -9999;
        mLinearAcceleration_x = mLinearAcceleration_y = mLinearAcceleration_z = -9999;
        mRotationVector_x_sin = mRotationVector_y_sin =  mRotationVector_z_sin = mRotationVector_cos = -9999;

        //initiate requested sensor list
        mRequestedSensorList = new ArrayList<>();

        //initiate registered sneosr list
        mRegisteredSensorList = new ArrayList<>();

    }

    public boolean isExtractingPhoneSensorData() {
        return isExtractingPhoneSensorData;
    }

    public void setExtractingPhoneSensor(boolean flag) {
        isExtractingPhoneSensorData = flag;
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
    protected void enableSensor() {
        registerRequestedSensor();
    }

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


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }


    @Override
    public void saveRecordsInLocalRecordPool() {

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
     * register all the requested sensor list
     */
    protected  void registerRequestedSensor() {

        //we don't register any sensor if isExtractingPhoneSensorData is false.
        if (!isExtractingPhoneSensorData){
            return;
        }
        else {
            //register all of the sensors that are requested
            for (int i=0; i<mRequestedSensorList.size(); i++){
                boolean result =registerSensorByType(mRequestedSensorList.get(i));

                //if registering the requested sensor succesfully, put it in the registered sensor list
                if (result){
                    mRegisteredSensorList.add(mRequestedSensorList.get(i));
                }
            }
        }

    }

    /**
     * unregister all the sensors
     */
    protected void unRegisterSensors(){
        mSensorManager.unregisterListener(this);

        //clear the mRegisteredSensorList too.
        mRegisteredSensorList.clear();
    }



    protected boolean registerSensorByType(int type) {

        boolean result = false;

        switch (type){

            //accelerometer
            case Sensor.TYPE_ACCELEROMETER:
                result = mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_NORMAL);
                return result;
                //gyriscope
            case Sensor.TYPE_GYROSCOPE:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                        SensorManager.SENSOR_DELAY_NORMAL);
                return result;
                //gravity
            case Sensor.TYPE_GRAVITY:
                result =mSensorManager.registerListener(this,
                            mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                            SensorManager.SENSOR_DELAY_NORMAL);
                return result;
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR),
                        SensorManager.SENSOR_DELAY_NORMAL);
                return result;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                        SensorManager.SENSOR_DELAY_NORMAL);
                return result;

            case Sensor.TYPE_PROXIMITY:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                        SensorManager.SENSOR_DELAY_NORMAL);
                return result;


            //ambient temperature
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
                        SensorManager.SENSOR_DELAY_NORMAL);
                return result;
            //light
            case Sensor.TYPE_LIGHT:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                        SensorManager.SENSOR_DELAY_NORMAL);
                return result;
            //pressure
            case Sensor.TYPE_PRESSURE:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                        SensorManager.SENSOR_DELAY_NORMAL);
                return result;

            //humidity
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY),
                        SensorManager.SENSOR_DELAY_NORMAL);
                return result;


            case Sensor.TYPE_HEART_RATE:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE),
                        SensorManager.SENSOR_DELAY_NORMAL);
                return result;


            case Sensor.TYPE_STEP_COUNTER:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                        SensorManager.SENSOR_DELAY_NORMAL);
                return result;


            case Sensor.TYPE_STEP_DETECTOR:
                result =mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                        SensorManager.SENSOR_DELAY_NORMAL);
                return result;
            default:
                return result;

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


    /**get Accelerometer values**/
    private void getAccelerometer(SensorEvent event) {
        mAccele_x = event.values[0];	// Acceleration force along the x axis (including gravity). m/s2
        mAccele_y = event.values[1];	// Acceleration force along the y axis (including gravity). m/s2
        mAccele_z = event.values[2];	// Acceleration force along the z axis (including gravity). m/s2

        //store values into a sensorRecord so that we can store them in the local database
        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mAccele_x);
        record.sensorValues.add(mAccele_y);
        record.sensorValues.add(mAccele_z);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_ACCELEROMETER);

        ContextManager.addRecordToPool(record);
    }


    /**get Gyroscope values**/
    private void getGyroscope(SensorEvent event) {
        mGyroscope_x = event.values[0];	// Rate of rotation around the x axis. rad/s
        mGyroscope_y = event.values[1];	// Rate of rotation around the y axis. rad/s
        mGyroscope_z = event.values[2];	// Rate of rotation around the z axis. rad/s

        //store values into a sensorRecord so that we can store them in the local database
        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mGyroscope_x);
        record.sensorValues.add(mGyroscope_y);
        record.sensorValues.add(mGyroscope_z);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_GYROSCOPE);

        ContextManager.addRecordToPool(record);
    }


    /**get gravity values**/
    private void getGravity(SensorEvent event) {
        mGravity_x = event.values[0];	// Force of gravity along the x axis m/s2
        mGravity_y = event.values[1];	// Force of gravity along the y axis m/s2
        mGravity_z = event.values[2];	// Force of gravity along the z axis m/s2

        //store values into a sensorRecord so that we can store them in the local database
        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mGravity_x);
        record.sensorValues.add(mGravity_y);
        record.sensorValues.add(mGravity_z);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_GRAVITY);

        ContextManager.addRecordToPool(record);
    }
    /**get linear acceleration values**/
    private void getLinearAcceleration(SensorEvent event) {
        mLinearAcceleration_x = event.values[0];	//Acceleration force along the x axis (excluding gravity).  m/s2
        mLinearAcceleration_y = event.values[1];	//Acceleration force along the y axis (excluding gravity).  m/s2
        mLinearAcceleration_z = event.values[2];	//Acceleration force along the z axis (excluding gravity).  m/s2

        //store values into a sensorRecord so that we can store them in the local database
        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mLinearAcceleration_x);
        record.sensorValues.add(mLinearAcceleration_y);
        record.sensorValues.add(mLinearAcceleration_z);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_LINEAR_ACCELERATION);

        ContextManager.addRecordToPool(record);
    }

    /**get rotation vector values**/
    private void getRotationVector(SensorEvent event) {
        mRotationVector_x_sin = event.values[0];	// Rotation vector component along the x axis (x * sin(�c/2))  Unitless
        mRotationVector_y_sin = event.values[1];	// Rotation vector component along the y axis (y * sin(�c/2)). Unitless
        mRotationVector_z_sin = event.values[2];	//  Rotation vector component along the z axis (z * sin(�c/2)). Unitless
        mRotationVector_cos = event.values[3];		// Scalar component of the rotation vector ((cos(�c/2)).1 Unitless

        //store values into a sensorRecord so that we can store them in the local database
        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mRotationVector_x_sin);
        record.sensorValues.add(mRotationVector_y_sin);
        record.sensorValues.add(mRotationVector_z_sin);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_ROTATION_VECTOR);

        ContextManager.addRecordToPool(record);
    }

    /**get magnetic field values**/
    private void getMagneticField(SensorEvent event){
        mMagneticField_x = event.values[0];	// Geomagnetic field strength along the x axis.
        mMagneticField_y = event.values[1];	// Geomagnetic field strength along the y axis.
        mMagneticField_z = event.values[2];	// Geomagnetic field strength along the z axis.

        //store values into a sensorRecord so that we can store them in the local database
        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mMagneticField_x);
        record.sensorValues.add(mMagneticField_y);
        record.sensorValues.add(mMagneticField_z);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_MAGNETIC_FIELD);
    }

    /**get proximity values**/
    private void getProximity(SensorEvent event){
        mProximity = event.values[0];

        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mProximity);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_PROXIMITY);

        ContextManager.addRecordToPool(record);
    }

    private void getAmbientTemperature(SensorEvent event){
        /* Environment Sensors */
        float mAmbientTemperature = event.values[0];

        //store the light value into a sensorRecord
        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mAmbientTemperature);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_AMBIENT_TEMPERATURE);

        ContextManager.addRecordToPool(record);

    }

    private void getLight(SensorEvent event){
        mLight = event.values[0];

        //store the light value into a sensorRecord
        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mLight);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_LIGHT);

        ContextManager.addRecordToPool(record);
    }

    private void getPressure(SensorEvent event){
        mPressure = event.values[0];

        //store the light value into a sensorRecord
        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mPressure);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_PRESSURE);

        ContextManager.addRecordToPool(record);
    }

    private void getRelativeHumidity(SensorEvent event){
        mRelativeHumidity = event.values[0];

        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mRelativeHumidity);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_RELATIVE_HUMIDITY);

        ContextManager.addRecordToPool(record);
    }

    private void getHeartRate (SensorEvent event) {
        mHeartRate = event.values[0];

        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mHeartRate);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_HEART_RATE);

        ContextManager.addRecordToPool(record);
    }

    private void getStepCounter (SensorEvent event) {
        mStepCount = event.values[0];

        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mHeartRate);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_STEP_COUNTER);

        ContextManager.addRecordToPool(record);

    }

    private void getStepDetector (SensorEvent event) {
        mStepDetect = event.values[0];

        SensorRecord record = new SensorRecord();
        record.sensorValues.add(mHeartRate);
        record.setTimestamp(getCurrentTimeInMillis());
        record.setSensorSource(Sensor.TYPE_STEP_DETECTOR);

        ContextManager.addRecordToPool(record);
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
