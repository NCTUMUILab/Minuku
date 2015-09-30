package edu.umich.si.inteco.minuku.contextmanager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.data.DataHandler;
import edu.umich.si.inteco.minuku.model.SimpleGeofence;
import edu.umich.si.inteco.minuku.model.record.ActivityRecord;
import edu.umich.si.inteco.minuku.model.record.AppActivityRecord;
import edu.umich.si.inteco.minuku.model.record.LocationRecord;
import edu.umich.si.inteco.minuku.model.record.SensorRecord;
import edu.umich.si.inteco.minuku.util.AppManager;
import edu.umich.si.inteco.minuku.util.GooglePlayServiceUtil;
import edu.umich.si.inteco.minuku.util.GooglePlayServiceUtil.ACTIVITY_REQUEST_TYPE;
import edu.umich.si.inteco.minuku.util.GooglePlayServiceUtil.LOCATION_REQUEST_TYPE;
import edu.umich.si.inteco.minuku.util.GooglePlayServiceUtil.GEOFENCE_REQUEST_TYPE;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.NotificationHelper;

public class ContextExtractor implements SensorEventListener {

	/** Tag for logging. */
    private static final String LOG_TAG = "ContextExtractor";
    
    /**configurable value**/
    private int mRecordFrequency = 1000;
    
    /**system components**/
	private static Context mContext;
	private DataHandler mDataHandler;
	private SensorManager mSensorManager ;
	private ConnectivityManager mConnectivityManager; 	
	private TelephonyManager mTelephonyManager;
	private AudioManager mAudioManager;
	private AppManager mAppManager;
	private static PowerManager mPowerManager;
	
	
	/**Flag**/
	
	// Store the current activity recognition request type (ADD or REMOVE)
    private ACTIVITY_REQUEST_TYPE mActivityRecognitionRequestType;
    private LOCATION_REQUEST_TYPE mLocationRequestType;
    private GEOFENCE_REQUEST_TYPE mGeofenceRequestType;
	private boolean isExtractingContext = false;
	private boolean isExtractinAppInfo = true;
	private boolean hasStoppedExtractingContext = true;
	

	/**Flag for context source use status: 
	 * Monitor: the context source is used for monitoring
	 * Record: the context source is used for recording
	 * Unused: the sensor is not used for monitoring or recording
	 * **/
	public enum CONTEXT_SOURCE_STATUS {UNUSED, MONITOR, RECORD};
	public CONTEXT_SOURCE_STATUS mLocationInUseStatus;
	public CONTEXT_SOURCE_STATUS mActivityInUseStatus;
	public CONTEXT_SOURCE_STATUS mAppInUseStatus;
	public CONTEXT_SOURCE_STATUS mGeofenceInUseStatus;

	
	/***sensor values***/
	private float mAccelationSquareRoot;
	
	/**
	 * ** Context Information to Extract **
	 * 
	 * **/

	 // The activity recognition update request object
    private ActivityRecognitionRequester mActivityRecognitionRequester;
    // The activity recognition update removal object
    private ActivityRecognitionRemover mActivityRecognitionRemover; 
    // The geofence update request object
	private GeofenceManager mGeofenceManager;
	// the location update manager
	private static LocationManager mLocationManager;
	
    
	/**Location & Activity Recognition & Geofence**/
	private static Location mLocation;
	private static DetectedActivity mMostProbableActivity;
	private static List<DetectedActivity> mProbableActivities;
    private static long mProbableActivitiesDetectionTime;
	private static List<Geofence> mTriggeringGeofences;

	
	/**App **/
	private static String mCurForegroundActivity="defaultActivity";
	private static String mCurForegroundPackage="defaultPackagename";
	
	/**Motion Sensors**/
	private static float mAccele_x, mAccele_y, mAccele_z;
	private static float mGyroscope_x, mGyroscope_y, mGyroscope_z;
	private static float mGravity_x, mGravity_y, mGravity_z;
	private static float mLinearAcceleration_x, mLinearAcceleration_y, mLinearAcceleration_z;
	private static float mRotationVector_x_sin, mRotationVector_y_sin, mRotationVector_z_sin, mRotationVector_cos;
	
	/**Position Sensors**/
	private static float mProximity ;
	private static float mMagneticField_x, mMagneticField_y, mMagneticField_z;
	
	/**Environment Sensors**/
	private float mAmbientTemperature, mLight, mPressure,mRelativeHumidity; 
	
	
	/**Context Records**/
	// each Record object is uniqute to the ContextExtractor. The ContextExtractor updates each of the Record based on the RecordFrequency 
	private SensorRecord mSensorRecord;
	
	public ContextExtractor (Context context){

        Log.d(LOG_TAG, "[testing start service] going to start the context extractor");

		mContext = context;		
		
		mPowerManager = (PowerManager) mContext.getSystemService(mContext.POWER_SERVICE);
		
		//call sensor manager from the service
		mSensorManager = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);
		
		//call connectivity manager from the service
		mConnectivityManager =(ConnectivityManager)mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
		
		//call telephony manager from the service
		mTelephonyManager = (TelephonyManager)mContext.getSystemService(mContext.TELEPHONY_SERVICE);
		
		//call audio manager from the service
		mAudioManager = (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);
		
		mAppManager = new AppManager(context);
		
		/**initiaize the sensor**/
		
		mLocation = new Location("NA");
		mMostProbableActivity = new DetectedActivity(-9999, -9999);
		mAccele_x = mAccele_y = mAccele_z = -9999;
		mGyroscope_x = mGyroscope_y = mGyroscope_z = -9999;
		mGravity_x = mGravity_y = mGravity_z = -9999;
		mMagneticField_x = mMagneticField_y = mMagneticField_z = -9999;
		mLinearAcceleration_x = mLinearAcceleration_y = mLinearAcceleration_z = -9999;
		mRotationVector_x_sin = mRotationVector_y_sin =  mRotationVector_z_sin = mRotationVector_cos = -9999;
		
		mProximity = -9999;
		mLight=mAmbientTemperature=mPressure=mRelativeHumidity -9999;

		/**Activity Recognition**/
		mActivityRecognitionRequester = new ActivityRecognitionRequester(mContext);
		mActivityRecognitionRemover = new ActivityRecognitionRemover(mContext);
		
		/**Geofence**/
		mGeofenceManager = new GeofenceManager(mContext);
		
		/**Location**/
        //Location is a little difference from activity recognition service and geofence. The location client can just remove updates when it is disconnected from the play service.
		mLocationManager = new LocationManager(mContext);
	}

	/**functions called by the ContextManager**/
	
	public void startExtractingContext(){
		
		this.isExtractingContext = true;
		this.hasStoppedExtractingContext = false;
		

		/**register sensors**/    
		//registerSensors(); 
		
		//get activity information
		startRequestingActivityRecognition();

        startRequestingLocation();
		
		//get geofence transitions
		startRequestingGeofence();
		
		//extrating app information
		if (isExtractinAppInfo){
			Log.d(LOG_TAG, "[startExtractingContext] ready to extract app info");
			mAppManager.runMonitoringAppThread();
		}
		
	}
	
	public void stopExtractingContext(){
		
		Log.d("LOG_TAG", "[stopExtractingContext]");
		this.isExtractingContext = false;
		this.hasStoppedExtractingContext = true;

		//unregister sensors
		unRegisterSensors();

		//remove activity update
		stopRequestingActivityRecognition();
		
		//remove location update
        stopRequestingLocation();
	
	}


    private void startRequestingLocation(){
        Log.d(LOG_TAG, "[startRequestingLocation] start to request location udpate");

        mLocationRequestType = GooglePlayServiceUtil.LOCATION_REQUEST_TYPE.ADD;

        //check Google Place first
        if (!servicesConnected()) {

            return;
        }

        mLocationManager.requestLocationUpdate();
    }

    private void stopRequestingLocation(){
        Log.d(LOG_TAG, "[stopRequestingActivityRecognition] stop to request location");

        // Check for Google Play services
        if (!servicesConnected()) {
            return;
        }

        //if Google Play service is available, stop the update
        mLocationRequestType= GooglePlayServiceUtil.LOCATION_REQUEST_TYPE.REMOVE;

        // Pass the remove request to the remover object (the intent is the same as the request intent)
        mLocationManager.removeLocationUpdate();

    }

	/***
	 * 
	 * Requesting and Removing Activity Recognition Service
	 * 
	 */
	private void startRequestingActivityRecognition(){
		Log.d(LOG_TAG, "[startRequestingActivityRecognition] start to request activity udpate");
		
		mActivityRecognitionRequestType = GooglePlayServiceUtil.ACTIVITY_REQUEST_TYPE.ADD;	
		
		//check Google Place first
		if (!servicesConnected()) {

            return;
        }
		
		mActivityRecognitionRequester.requestUpdates();
	}
	
	private void stopRequestingActivityRecognition(){
		Log.d(LOG_TAG, "[stopRequestingActivityRecognition] stop to request activity udpate");
		
		// Check for Google Play services
        if (!servicesConnected()) {
            return;
        }
        
        //if Google Play service is available, stop the update
        mActivityRecognitionRequestType = GooglePlayServiceUtil.ACTIVITY_REQUEST_TYPE.REMOVE;

        // Pass the remove request to the remover object (the intent is the same as the request intent)
        mActivityRecognitionRemover.removeUpdates(
        		mActivityRecognitionRequester.getRequestPendingIntent());

        /*
         * Cancel the PendingIntent of the requester. Even if the removal request fails, canceling the PendingIntent
         * will stop the updates.
         */

        if ( mActivityRecognitionRequester.getRequestPendingIntent()!=null)
            mActivityRecognitionRequester.getRequestPendingIntent().cancel();

	}
	
	
	
	
	/***

	 * Requesting and Removing Geofence Service
	 * 
	 */
	private void startRequestingGeofence(){
		Log.d(LOG_TAG, "[startRequestingGeofence] start to request Geofence udpate");

		/*
		mGeofenceRequestType = GooglePlayServiceUtil.GEOFENCE_REQUEST_TYPE.ADD;	
		
		//check Google Place first
		if (!servicesConnected()) {
            return;
        }
		
		try {

			//TODO: need to get the geofences from eventMonitor, for the first testing, we just hard code the geofence
			List<Geofence> monitoredGeofences = getTestingGeofences();
			
			//add geofenece here,,,through getting eventmonitor's simple geofence list
			
			
			//get geofence from the eventMonitor
			
			Log.d(LOG_TAG, "[startRequestingGeofence] going to addGeofence to the google playservice");
			
			
            // Try to add geofences
            mGeofenceRequester.addGeofences(monitoredGeofences);
            
            Log.d(LOG_TAG, "[startRequestingGeofence] after requesting the adding geofence from the googleplay service");
    		
            
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
        	Log.d(LOG_TAG, "[startRequestingGeofence] the previosu request is not finished yet");
    		
        }
        */
	}
	
	
    /*
     * Remove the geofence by creating a List of geofences to
     * remove and sending it to Location Services. The List
     * contains the id of geofence 1 ("1").
     *  The removal happens asynchronously; Location Services calls
         * onRemoveGeofencesByPendingIntentResult() when the removal is done.
     * */
	private void stopRequestingGeofence(){
		/*
		Log.d(LOG_TAG, "[stopRequestingActivityRecognition] stop to remove Geofence from the server");
		
		// Check for Google Play services
        if (!servicesConnected()) {
            return;
        }
        
        //if Google Play service is available, stop the update
        mGeofenceRequestType = GooglePlayServiceUtil.GEOFENCE_REQUEST_TYPE.REMOVE;

        // Pass the remove request to the remover object (the intent is the same as the request intent)

        mGeofenceRemover.removeGeofencesByIntent(
                                    mGeofenceRequester.getRequestPendingIntent());



        mGeofenceRequester.getRequestPendingIntent().cancel();
		*/

	}
	

	
	private ArrayList<Geofence> getTestingGeofences(){
		
		Log.d(LOG_TAG, "[getTestingGeofences] before adding geofences");
		
		
		ArrayList<Geofence> geofences = new ArrayList<Geofence>();
		
		SimpleGeofence Geofence1 = new SimpleGeofence(
	            "Starbucks on State",
	           //42.279456, -83.740991: starbucks on State st
	            
	            42.279456,
	            -83.740991,
	            200,
	            Geofence.NEVER_EXPIRE, //never expire
	            // detect all transitions
	            Geofence.GEOFENCE_TRANSITION_ENTER |
	            Geofence.GEOFENCE_TRANSITION_DWELL | 
	            Geofence.GEOFENCE_TRANSITION_EXIT
	            );
		
		//we did not set the threshold of dwell yet, our default is 30000, i.e. 5 minute
		
		SimpleGeofence Geofence2 = new SimpleGeofence(
	            "Home",
	           //42.273844, -83.751055: south 1st st
	            
	            42.273844,
	            -83.751055,
	            200,
	            Geofence.NEVER_EXPIRE, //never expire
	            // detect all transitions
	            Geofence.GEOFENCE_TRANSITION_ENTER | 
	            Geofence.GEOFENCE_TRANSITION_EXIT|
	            Geofence.GEOFENCE_TRANSITION_DWELL );
		
		EventManager.addMonitoredGeofence(Geofence1);
		EventManager.addMonitoredGeofence(Geofence2);
		
		for (int i=0; i< EventManager.getMonitoredGeofences().size(); i++){
			//convert the simeplgeofence to geofence
			geofences.add(EventManager.getMonitoredGeofences().get(i).toGeofence());
		}

		return geofences;
		
	}

	
	
	
	public boolean isExtractingContext(){
		return this.isExtractingContext;		
	}	
	
	public void ExtractingContext(boolean flag){
		isExtractingContext = flag;
	}
	
	public boolean isExtractinAppInfo(){
		return this.isExtractinAppInfo();		
	}	
	
	public void ExtractinAppInfo(boolean flag){
		isExtractinAppInfo = flag;
	}
	
	public void setRecordFrequency(int rf){
		
		mRecordFrequency = rf;
	}
	
	public SensorRecord getSensorRecord () {
		
		return mSensorRecord;
	}
	
	
	@SuppressWarnings("deprecation")
	private void registerSensors (){
		
		/**Motion Sensor**/
		if (ContextManager.accelerometerSensorIsEnabled)
			mSensorManager.registerListener(this, 
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
					SensorManager.SENSOR_DELAY_NORMAL);
		
		if (ContextManager.gyrscopeSensorIsEnabled)
			mSensorManager.registerListener(this, 
					mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 
					SensorManager.SENSOR_DELAY_NORMAL);
		
		if (ContextManager.gravitySensorIsEnabled)
			mSensorManager.registerListener(this, 
					mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), 
					SensorManager.SENSOR_DELAY_NORMAL);
		
		if (ContextManager.linearAcceleerationSensorIsEnabled)
			mSensorManager.registerListener(this, 
					mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 
					SensorManager.SENSOR_DELAY_NORMAL);
		
		if (ContextManager.rotationVectorSensorIsEnabled)
			mSensorManager.registerListener(this, 
					mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), 
					SensorManager.SENSOR_DELAY_NORMAL);
		
		
		/**Position Sensor**/
		if (ContextManager.magneticFieldSensorIsEnabled)
			mSensorManager.registerListener(this, 
					mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 
					SensorManager.SENSOR_DELAY_NORMAL);
		
		//Note for Orientation
		//the use of orientation sensor is discouraged in the Android official documentation. 
		
		//TODO : use getOrientation() to get the orientation values.
		
		if (ContextManager.proximitySensorIsEnabled)
			mSensorManager.registerListener(this, 
					mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), 
					SensorManager.SENSOR_DELAY_NORMAL);

		
		/**Environment Sensor**/
		if (ContextManager.ambientTemperatureSensorIsEnabled)
			mSensorManager.registerListener(this, 
					mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), 
					SensorManager.SENSOR_DELAY_NORMAL);
		
		if (ContextManager.lightSensorIsEnabled)
			mSensorManager.registerListener(this, 
					mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), 
					SensorManager.SENSOR_DELAY_NORMAL);
		
		if (ContextManager.pressureSensorIsEnabled)
			mSensorManager.registerListener(this, 
					mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), 
					SensorManager.SENSOR_DELAY_NORMAL);
		
		if (ContextManager.relativeHumiditySensorIsEnabled)
			mSensorManager.registerListener(this, 
					mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY), 
					SensorManager.SENSOR_DELAY_NORMAL);


		
	}
	
	
	private void unRegisterSensors(){
		
		mSensorManager.unregisterListener(this);
	}
	
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		/**Motion Sensor**/
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){   
			getAccelerometer (event);
		}
		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){   
			getGyroscope (event);
		}
		if (event.sensor.getType() == Sensor.TYPE_GRAVITY){   
			getGravity (event);
		}
		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){   
			getLinearAcceleration (event);
		}
		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){   
			getRotationVector (event);
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

		
		/**Environment Sensor**/
		if (event.sensor.getType() == Sensor.TYPE_LIGHT){
			getLight(event);
		}
		if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){   
			getAmbientTemperature (event);
		}
		if (event.sensor.getType() == Sensor.TYPE_PRESSURE){   
			getPressure (event);
		}
		if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY){   
			getRelativeHumidity (event);
		}

		
	}
	
	
	/**get Accelerometer values**/
	private void getAccelerometer(SensorEvent event) {		
	
		mAccele_x = event.values[0];	// Acceleration force along the x axis (including gravity). m/s2
		mAccele_y = event.values[1];	// Acceleration force along the y axis (including gravity). m/s2
		mAccele_z = event.values[2];	// Acceleration force along the z axis (including gravity). m/s2
		float accuracy = event.accuracy;

	
		//float accelationSquareRoot = (x * x + y * y + z * z)/(SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);	
		
		//store the accelerometer values into a sensorRecord
		SensorRecord record = new SensorRecord();	
		record.sensorValues.add(mAccele_x);
		record.sensorValues.add(mAccele_y);
		record.sensorValues.add(mAccele_z);
		record.sensorValues.add(accuracy);
		record.setTimestamp(getCurrentTimeInMillis());
		record.setSensorSource(ContextManager.SENSOR_SOURCE_PHONE_ACCELEROMETER);
		
		//Log.d(LOG_TAG, " the current timestamp is " + record.getTimestamp() +  " " + record.getTimeString());
		
		ContextManager.addRecordToPool(record);
		
	}
	
	
	/**get Gyroscope values**/
	private void getGyroscope(SensorEvent event) {
		
		long now = System.currentTimeMillis();
	
		mGyroscope_x = event.values[0];	// Rate of rotation around the x axis. rad/s
		mGyroscope_y = event.values[1];	// Rate of rotation around the y axis. rad/s
		mGyroscope_z = event.values[2];	// Rate of rotation around the z axis. rad/s
		float accuracy = event.accuracy;

		//store the accelerometer values into a sensorRecord
		SensorRecord record = new SensorRecord();	
		record.sensorValues.add(mGyroscope_x);
		record.sensorValues.add(mGyroscope_y);
		record.sensorValues.add(mGyroscope_z);
		record.sensorValues.add(accuracy);
		record.setTimestamp(getCurrentTimeInMillis());
		record.setSensorSource(ContextManager.SENSOR_SOURCE_PHONE_GYRSCOPE);
		
		//Log.d(LOG_TAG, " the current timestamp is " + record.getTimestamp() +  " " + record.getTimeString());

        ContextManager.addRecordToPool(record);
		
	}
	
	
	/**get gravity values**/
	private void getGravity(SensorEvent event) {

		mGravity_x = event.values[0];	// Force of gravity along the x axis m/s2
		mGravity_y = event.values[1];	// Force of gravity along the y axis m/s2
		mGravity_z = event.values[2];	// Force of gravity along the z axis m/s2 
		float accuracy = event.accuracy;

		//store the accelerometer values into a sensorRecord
		SensorRecord record = new SensorRecord();	
		record.sensorValues.add(mGravity_x);
		record.sensorValues.add(mGravity_y);
		record.sensorValues.add(mGravity_z);
		record.sensorValues.add(accuracy);
		record.setTimestamp(getCurrentTimeInMillis());
		record.setSensorSource(ContextManager.SENSOR_SOURCE_PHONE_GRAVITY);
		
		//Log.d(LOG_TAG, " the current timestamp is " + record.getTimestamp() +  " " + record.getTimeString());
		
		ContextManager.addRecordToPool(record);
		
	}
	
	
	
	/**get linear acceleration values**/
	private void getLinearAcceleration(SensorEvent event) {

		mLinearAcceleration_x = event.values[0];	//Acceleration force along the x axis (excluding gravity).  m/s2
		mLinearAcceleration_y = event.values[1];	//Acceleration force along the y axis (excluding gravity).  m/s2
		mLinearAcceleration_z = event.values[2];	//Acceleration force along the z axis (excluding gravity).  m/s2
		float accuracy = event.accuracy;
	
		//store the linear acceleration values into a sensorRecord
		SensorRecord record = new SensorRecord();	
		record.sensorValues.add(mLinearAcceleration_x);
		record.sensorValues.add(mLinearAcceleration_y);
		record.sensorValues.add(mLinearAcceleration_z);
		record.sensorValues.add(accuracy);
		record.setTimestamp(getCurrentTimeInMillis());
		record.setSensorSource(ContextManager.SENSOR_SOURCE_PHONE_LINEAR_ACCELERATION);
		
		//Log.d(LOG_TAG, " the current timestamp is " + record.getTimestamp() +  " " + record.getTimeString());
		
		ContextManager.addRecordToPool(record);
		
	}
	
	
	/**get rotation vector values**/
	private void getRotationVector(SensorEvent event) {

		mRotationVector_x_sin = event.values[0];	// Rotation vector component along the x axis (x * sin(�c/2))  Unitless
		mRotationVector_y_sin = event.values[1];	// Rotation vector component along the y axis (y * sin(�c/2)). Unitless 
		mRotationVector_z_sin = event.values[2];	//  Rotation vector component along the z axis (z * sin(�c/2)). Unitless
		mRotationVector_cos = event.values[3];		// Scalar component of the rotation vector ((cos(�c/2)).1 Unitless
		
		float accuracy = event.accuracy;

		//store the rotation vector values into a sensorRecord
		SensorRecord record = new SensorRecord();	
		record.sensorValues.add(mRotationVector_x_sin);
		record.sensorValues.add(mRotationVector_y_sin);
		record.sensorValues.add(mRotationVector_z_sin);
		record.sensorValues.add(mRotationVector_cos);	
		record.sensorValues.add(accuracy);
		record.setTimestamp(getCurrentTimeInMillis());
		record.setSensorSource(ContextManager.SENSOR_SOURCE_PHONE_ROTATION_VECTOR);
		
		//Log.d(LOG_TAG, " the current timestamp is " + record.getTimestamp() +  " " + record.getTimeString());
		
		ContextManager.addRecordToPool(record);
		
	}



	/**get magnetic field values**/
	private void getMagneticField(SensorEvent event){
		
		mMagneticField_x = event.values[0];	// Geomagnetic field strength along the x axis.
		mMagneticField_y = event.values[1];	// Geomagnetic field strength along the y axis.
		mMagneticField_z = event.values[2];	// Geomagnetic field strength along the z axis.
		float accuracy = event.accuracy;
	
		//store the linear acceleration values into a sensorRecord
		SensorRecord record = new SensorRecord();	
		record.sensorValues.add(mMagneticField_x);
		record.sensorValues.add(mMagneticField_y);
		record.sensorValues.add(mMagneticField_z);
		record.sensorValues.add(accuracy);
		record.setTimestamp(getCurrentTimeInMillis());
		record.setSensorSource(ContextManager.SENSOR_SOURCE_PHONE_MAGNETIC_FIELD);
	}
	
	/**get proximity values**/
	private void getProximity(SensorEvent event){
		
		mProximity = event.values[0];
		float accuracy = event.accuracy;
		
		//store the proximity value into a sensorRecord
	    SensorRecord record = new SensorRecord();		
		record.sensorValues.add(mProximity); 
		record.sensorValues.add(accuracy);
		record.setTimestamp(getCurrentTimeInMillis());
		record.setSensorSource(ContextManager.SENSOR_SOURCE_PHONE_PROXIMITY);
		
		ContextManager.addRecordToPool(record);	
		
	}
	
	private void getAmbientTemperature(SensorEvent event){
		mAmbientTemperature = event.values[0];
		float accuracy = event.accuracy;
		
	    //store the light value into a sensorRecord
	    SensorRecord record = new SensorRecord();		
		record.sensorValues.add(mAmbientTemperature); 	  
		record.sensorValues.add(accuracy);
		record.setTimestamp(getCurrentTimeInMillis());
		record.setSensorSource(ContextManager.SENSOR_SOURCE_PHONE_AMBIENT_TEMPERATURE);
		
		ContextManager.addRecordToPool(record);
	}

	private void getLight(SensorEvent event){

		mLight = event.values[0];
		float accuracy = event.accuracy;
		
	    //store the light value into a sensorRecord
	    SensorRecord record = new SensorRecord();		
		record.sensorValues.add(mLight); 	  
		record.sensorValues.add(accuracy);
		record.setTimestamp(getCurrentTimeInMillis());
		record.setSensorSource(ContextManager.SENSOR_SOURCE_PHONE_LIGHT);
		
		ContextManager.addRecordToPool(record);
	}
	
	private void getPressure(SensorEvent event){
		
		mPressure = event.values[0];
		float accuracy = event.accuracy;
		
	    //store the light value into a sensorRecord
	    SensorRecord record = new SensorRecord();		
		record.sensorValues.add(mPressure); 	  
		record.sensorValues.add(accuracy);
		record.setTimestamp(getCurrentTimeInMillis());
		record.setSensorSource(ContextManager.SENSOR_SOURCE_PHONE_PRESSURE);
		
		ContextManager.addRecordToPool(record);
	}
	
	private void getRelativeHumidity(SensorEvent event){
		
		mRelativeHumidity = event.values[0];
		float accuracy = event.accuracy;
		
	    //store the light value into a sensorRecord
	    SensorRecord record = new SensorRecord();		
		record.sensorValues.add(mRelativeHumidity); 	  
		record.sensorValues.add(accuracy);
		record.setTimestamp(getCurrentTimeInMillis());
		record.setSensorSource(ContextManager.SENSOR_SOURCE_PHONE_RELATIVE_HUMIDITY);
		
		ContextManager.addRecordToPool(record);
	}
	
	public static void setLocation(Location location){
		
		mLocation = location;
		//when getting a new record 
		
		LocationRecord record = new LocationRecord();		
		record.setLocation(mLocation);
		record.setTimestamp(getCurrentTimeInMillis());;
		
	//	Log.d(LOG_TAG, "in [setLocation] : set location " + mLocation.toString()  );

        //sendNotification("Location", mLocation.getLatitude() + ", " + mLocation.getLongitude());

		ContextManager.addRecordToPool(record);
	}

	public static Location getLocation(){
		return mLocation;
	}
	
	
	public static void setProbableActivities (List<DetectedActivity> da, long detectionTime ){
		mProbableActivities = da;
        mProbableActivitiesDetectionTime = detectionTime;
		
		//store activityRecord
		ActivityRecord record = new ActivityRecord();		
		record.setProbableActivities(mProbableActivities);
		record.setTimestamp(getCurrentTimeInMillis());
        record.setDetectionTime(detectionTime);

		ContextManager.addRecordToPool(record);

        //log...
        String message = "";

        for (int i=0; i<mProbableActivities.size(); i++){
            message += getActivityNameFromType(mProbableActivities.get(i).getType()) + ":" + mProbableActivities.get(i).getConfidence();
            if (i<mProbableActivities.size()-1){
                message+= ";;";
            }
        }

        //and location
        message += "\t" + mLocation.getLatitude() + "," + mLocation.getLongitude();

        LogManager.log(LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                LogManager.LOG_TAG_ACTIVITY_RECOGNITION,
                message);;
	}

    public static void setProbableActivitiesDetectionTime(long time ) {
        mProbableActivitiesDetectionTime = time;
    }


	public static void setLatestGeofenceTransition(){
		
		//TODO: add geofence record
		
	}
	
	public static List<DetectedActivity> getProbableActivities(){
		return mProbableActivities;
	}

    public static long getProbableActivitiesDetectionTime() {

        return mProbableActivitiesDetectionTime;
    }

	public static void setMostProbableActivity(DetectedActivity activity, long detectionTime){
		mMostProbableActivity = activity;
        mProbableActivitiesDetectionTime = detectionTime;
	}
	
	public static DetectedActivity getMostProbableActivity(){
		return mMostProbableActivity;
	}

	
    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            Log.d(LOG_TAG, "the Google Play Service is available");
            return true;

        // Google Play services was not available for some reason
        } else {

        	Log.d(LOG_TAG, "the Google Play Service is not available");
            return false;
        }
    }
    
	public static void setCurrentForegroundActivityAndPackage(String curForegroundActivity, String curForegroundPackage) {

        mCurForegroundActivity=curForegroundActivity;
		mCurForegroundPackage=curForegroundPackage;

        //save into record
        AppActivityRecord record = new AppActivityRecord(mCurForegroundPackage,  mCurForegroundActivity);
        record.setTimestamp(getCurrentTimeInMillis());
        ContextManager.addRecordToPool(record);

		//Log.d(LOG_TAG, "[setCurrentForegroundActivityAndPackage] the current running package is " + mCurForegroundActivity + " and the activity is "+ mCurForegroundPackage);
	}

    public static boolean isWifiConnected() {

        ConnectivityManager conMngr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = conMngr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifi.isConnected();
    }

    public static LocationManager getLocationManager(){
        return mLocationManager;
    }

	public static String getCurrentForegroundActivity(){
		return mCurForegroundActivity;
	}
	
	public static String getCurrentForegroundPackage(){
		return mCurForegroundPackage;
	}
	
	public static PowerManager getPowerManager(){
		return mPowerManager;
	}
	
	
	/***
	 * 
	 * 
	 * *Utility Functions**
	 * 
	 * 
	 * 
	 * ***/
	
	
	
	/**get the current time in milliseconds**/
	public static long getCurrentTimeInMillis(){		
		//get timzone		
		TimeZone tz = TimeZone.getDefault();		
		Calendar cal = Calendar.getInstance(tz);
		long t = cal.getTimeInMillis();		
		return t;
	}
	
	/**get the current time in string (in the format of "yyyy-MM-dd HH:mm:ss" **/
	public static String getCurrentTimeString(){		
		//get timzone		
		TimeZone tz = TimeZone.getDefault();		
		Calendar cal = Calendar.getInstance(tz);
		
		SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
		String currentTimeString = sdf_now.format(cal.getTime());
		
		return currentTimeString;
	}


	/**
     * Map detected activity types to strings
     */
    public static String getActivityNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }




    private static void sendNotification(String title, String message) {

        // Create a notification builder that's compatible with platforms >= version 4
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext);


        // Set the title, text, and icon
        builder.setContentTitle(title)
                .setContentText( message)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        // Get the Intent that starts the Location settings panel
                .setContentIntent(getContentIntent());

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(NotificationHelper.NOTIFICATION_ID_TEST, builder.build());
    }

    private static PendingIntent getContentIntent() {

        // Set the Intent action to open Location Settings
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        // Create a PendingIntent to start an Activity
        return PendingIntent.getService(mContext.getApplicationContext(), GooglePlayServiceUtil.GEOFENCE_TRANSITION_PENDING_INTENT_REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
