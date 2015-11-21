package edu.umich.si.inteco.minuku.context.ContextStateManagers;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.ContextSource;
import edu.umich.si.inteco.minuku.model.LoggingTask;
import edu.umich.si.inteco.minuku.model.Record.ActivityRecognitionRecord;
import edu.umich.si.inteco.minuku.model.Record.LocationRecord;
import edu.umich.si.inteco.minuku.model.Record.Record;

import java.text.DateFormat;
import java.util.Date;

public class LocationManager extends ContextStateManager implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{

	/** Tag for logging. */
    private static final String LOG_TAG = "LocationManager";

    /**constants**/

    //The interval for location updates. Inexact. Updates may be more or less frequent.
    public static final long UPDATE_INTERVAL_IN_SECONDS = 5;
     //The fastest rate for active location updates.
    public static final long FASTEST_UPDATE_INTERVAL_IN_SECONDS = 2;

    //the frequency of requesting location from the google play service
    public static final int SLOW_UPDATE_INTERVAL_IN_SECONDS = 60 ;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_SECONDS *
            Constants.MILLISECONDS_PER_SECOND;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = FASTEST_UPDATE_INTERVAL_IN_SECONDS*
            Constants.MILLISECONDS_PER_SECOND;

    public static final long SLOW_UPDATE_INTERVAL_IN_MILLISECONDS = SLOW_UPDATE_INTERVAL_IN_SECONDS*
            Constants.MILLISECONDS_PER_SECOND;

    //the accuracy of location update
    public static final int LOCATION_UPDATE_LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;

    //initial value
    private static long sUpdateIntervalInMilliSeconds = UPDATE_INTERVAL_IN_MILLISECONDS;

    public static final String STRING_CONTEXT_SOURCE_LOCATION = "Location";


    /**Properties for Record**/
    public static final String RECORD_DATA_PROPERTY_LATITUDE = "Latitude";
    public static final String RECORD_DATA_PROPERTY_LONGITUDE = "Longitude";
    public static final String RECORD_DATA_PROPERTY_ACCURACY = "Accuracy";
    public static final String RECORD_DATA_PROPERTY_ALTITUDE = "Altitude";
    public static final String RECORD_DATA_PROPERTY_PROVIDER = "Provider";
    public static final String RECORD_DATA_PROPERTY_SPEED = "Speed";
    public static final String RECORD_DATA_PROPERTY_BEARING = "Bearing";
    public static final String RECORD_DATA_PROPERTY_EXTRAS = "Extras";


    public static final int CONTEXT_SOURCE_LOCATION = 0;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";


    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private static LocationRequest mLocationRequest;

    private static Context mContext;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected static Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. The value is true when Context Manager
     * requests Minuku to request locaiton
     */
    protected static Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected static String mLastUpdateTime;


    public LocationManager(Context c){

        super();

    	mContext = c;

        setName(ContextManager.CONTEXT_STATE_MANAGER_LOCATION);

        //when the app starts we don't request location.
        mRequestingLocationUpdates = false;

        mLastUpdateTime = "";

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();

        setUpContextSourceList();

    }

    /** each ContextStateManager should override this static method
     * it adds a list of ContextSource that it will manage **/
    protected void setUpContextSourceList(){

        Log.d(LOG_TAG, "setUpContextSourceList in ActivityRecognitionManager. mContextSource:  " + mContextSourceList);

        boolean isAvailable;

        // Google Play Service is available after api level 15
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            isAvailable = true;
        }
        else
            isAvailable = false;


        //add two context sources: most probable activity and all probable activities, with defaul sampling rate.
        mContextSourceList.add(
                new ContextSource(
                        STRING_CONTEXT_SOURCE_LOCATION,
                        CONTEXT_SOURCE_LOCATION,
                        isAvailable,
                        sUpdateIntervalInMilliSeconds
                ));

        return;

    }

    /**
     * Start the activity recognition update request process by
     * getting a connection.
     */
    public void requestLocationUpdate() {

        Log.d(LOG_TAG, "[testLocationUpdate] going to request location update ");
        //we need to get location. Set this true
        mRequestingLocationUpdates = true;

        //first check whether we have GoogleAPIClient connected. if yes, we request location. Otherwise
        //we connect the client and then in onConnected() we request location
        if (!mGoogleApiClient.isConnected()){
            Log.d(LOG_TAG,"[testLocationUpdate] Google Service is not connected, need to connect ");
            connentClient();
        }
        else {
            Log.d(LOG_TAG, "[testLocationUpdate] Google Service is connected, now starts to start location update ");
            startLocationUpdates();
            disconnectClient();
        }
    }


    /**
     *For each ContextSource we check whether it is requested and update its request status.
     * Each ContextStateManager should override this function because they will need to determine
     * whether to stop or start extracting certain data source, depending on its Request Status
     */
    @Override
    protected void updateContextSourceListRequestStatus() {

        boolean isRequested = false;

        //for each contextSource we need to check its requeststatus
        for (int i=0; i<mContextSourceList.size(); i++){
            mContextSourceList.get(i).setIsRequested(updateContextSourceRequestStatus(mContextSourceList.get(i)));
            Log.d(LOG_TAG, "[updateContextSourceListRequestStatus] check saving data the contextsource " + mContextSourceList.get(i).getName() + " requested: " + mContextSourceList.get(i).isRequested());

            //if any source needing location is requested, we still receive location update
            isRequested = isRequested | mContextSourceList.get(i).isRequested();

        }

        //If no location is requested, we should stop requesting location update
        if (!isRequested){
            Log.d(LOG_TAG, "[updateContextSourceListRequestStatus], stop requesting AR informatoin because it is not needed anymore");
            //TODO: need to create this in the study json to test (triggered logging location)

            //if there's an location update going on, we should remove it. Otherwise, we don't need to do anything
            if (mRequestingLocationUpdates)
                removeLocationUpdate();
        }

        else {
            //if we haven't started a location update, now start to update. Otherwise, we don't need to do anything.
            if (!mRequestingLocationUpdates)
                requestLocationUpdate();
        }
    }

    /**
     * ContextStateMAnager needs to override this fundtion to implement writing a Record and save it to the LocalDataPool
     */
    public void saveRecordToLocalRecordPool() {

        /** create a Record to save timestamp, session it belongs to, and Data**/

        //we create LocationRecord instead of record because we expect to use some of these data later in memory
        LocationRecord record = new LocationRecord(
                mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(),
                mCurrentLocation.getAccuracy());


        /** create data in a JSON Object. Each CotnextSource will have different formats.
         * So we need each ContextSourceMAnager to implement this part**/
        JSONObject data = new JSONObject();

        //add location to data
        try {
            data.put(RECORD_DATA_PROPERTY_LATITUDE, mCurrentLocation.getLatitude());
            data.put(RECORD_DATA_PROPERTY_LONGITUDE, mCurrentLocation.getLongitude());
            data.put(RECORD_DATA_PROPERTY_ALTITUDE, mCurrentLocation.getAltitude());
            data.put(RECORD_DATA_PROPERTY_ACCURACY, mCurrentLocation.getAccuracy());
            data.put(RECORD_DATA_PROPERTY_SPEED, mCurrentLocation.getSpeed());
            data.put(RECORD_DATA_PROPERTY_BEARING, mCurrentLocation.getBearing());
            data.put(RECORD_DATA_PROPERTY_PROVIDER, mCurrentLocation.getProvider());
            data.put(RECORD_DATA_PROPERTY_EXTRAS, mCurrentLocation.getExtras());


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, "testing saving records at " + record.getTimeString() + " data: " + record.getData());


        /**we set data in Record**/
        record.setData(data);
        record.setTimestamp(ContextManager.getCurrentTimeInMillis());

        /**add it to the LocalRecordPool**/
        addRecord(record);

    }


    public void removeLocationUpdate() {
        //stop requesting location udpates

        mRequestingLocationUpdates = false;
        Log.d(LOG_TAG, "[testLocationUpdate]  going to remove location update ");

        if (!mGoogleApiClient.isConnected()) {
            connentClient();
        }
        else {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d(LOG_TAG, "[testLocationUpdate] we have removed location update ");
            disconnectClient();
        }
    }

    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    private void connentClient() {
        mGoogleApiClient.connect();
    }


    private void disconnectClient() {
        // Disconnect the client
        mGoogleApiClient.disconnect();
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(LOG_TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        //set intervals for the locaiton request
        mLocationRequest.setInterval(sUpdateIntervalInMilliSeconds);

        Log.d(LOG_TAG, "[testLocationUpdate] the interval is  " + sUpdateIntervalInMilliSeconds);

        // Sets the fastest rate for active location updates.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        //set accuracy
        mLocationRequest.setPriority(LOCATION_UPDATE_LOCATION_PRIORITY);
    }


    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        if (mGoogleApiClient.isConnected()){
            Log.d(LOG_TAG, "[testLocationUpdate] send out location udpate request");

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

            Log.d(LOG_TAG, "[testLocationUpdate] after send out location udpate request");
        }

    }

	/**this function is where we got the updated location information**/
    @Override
	public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Log.d(LOG_TAG, "[onLocationChanged] get location " +
                mCurrentLocation.getLatitude() + " , " +
                mCurrentLocation.getLongitude() + " , " +
                mCurrentLocation.getAccuracy());

        //if location is requested, save location
        boolean isRequested = checkRequestStatusOfContextSource(STRING_CONTEXT_SOURCE_LOCATION);

        if (isRequested){
            saveRecordToLocalRecordPool();
        }


        //TODO: we remove this after we don't need tthis anymore,
        Toast.makeText(mContext, mCurrentLocation.getLatitude() + " , " + mCurrentLocation.getLongitude()
                        + " , " + mCurrentLocation.getAccuracy(),
                Toast.LENGTH_SHORT).show();

    }


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }


    /*
    * Called by Location Services when the request to connect the
    * client finishes successfully. At this point, you can
    * request the current location or start periodic updates
    */	
	@Override
	public void onConnected(Bundle dataBundle) {

        Log.d(LOG_TAG,"[test location update] the location servce is connected, going to request locaiton updates");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in mCurrentLocation.

        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }

        // If Minuku requests location updates before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (See requestLocationUpdates). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        else {
            removeLocationUpdate();
        }

	}

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(LOG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    public static Location getCurrentLocation(){
        return mCurrentLocation;
    }


    public static long getLocationUpdateIntervalInMillis() {
        return UPDATE_INTERVAL_IN_MILLISECONDS;
    }

    public void setLocationUpdateInterval(long updateInterval) {

        Log.i(LOG_TAG, "[testLocationUpdate] attempt to update the location request interval to " + updateInterval);

        //before we update we make sure GoogleClient is connected.
        if (!mGoogleApiClient.isConnected()){
            //do nothing
        }
        else{
            sUpdateIntervalInMilliSeconds = updateInterval;

            //after we get location we need to update the location request
            //1. remove the update
            removeLocationUpdate();
            //2. create new update, and then start update
            createLocationRequest();
            requestLocationUpdate();
        }


    }


    public static int getContextSourceTypeFromName(String sourceName) {
        return -1;
    }

    public static String getContextSourceNameFromType(int sourceType) {

        return "NA";
    }

}
