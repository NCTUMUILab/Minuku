package edu.umich.si.inteco.minuku.contextmanager;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import edu.umich.si.inteco.minuku.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.services.CaptureProbeService;

import java.text.DateFormat;
import java.util.Date;

public class LocationManager implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{

	/** Tag for logging. */
    private static final String LOG_TAG = "LocationManager";

    /**constants**/

    //The interval for location updates. Inexact. Updates may be more or less frequent.
    public static long UPDATE_INTERVAL_IN_SECONDS = 10;
     //The fastest rate for active location updates.
    public static final long FASTEST_UPDATE_INTERVAL_IN_SECONDS = 2;

    //the frequency of requesting location from the google play service
    public static int SLOW_UPDATE_INTERVAL_IN_SECONDS = 60 ;

    public static long UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_SECONDS *
            Constants.MILLISECONDS_PER_SECOND;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = FASTEST_UPDATE_INTERVAL_IN_SECONDS*
            Constants.MILLISECONDS_PER_SECOND;

    public static final long SLOW_UPDATE_INTERVAL_IN_MILLISECONDS = SLOW_UPDATE_INTERVAL_IN_SECONDS*
            Constants.MILLISECONDS_PER_SECOND;

    //the accuracy of location update
    public static int LOCATION_UPDATE_LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;


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
    protected Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. The value is true when Context Manager
     * requests Minuku to request locaiton
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;


    public LocationManager(Context c){

    	mContext = c;

        //when the app starts we don't request location.
        mRequestingLocationUpdates = false;

        mLastUpdateTime = "";

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();

    }

    /**
     * Start the activity recognition update request process by
     * getting a connection.
     */
    public void requestLocationUpdate() {

        Log.d(LOG_TAG,"[requestUpdates] going to request location update ");
        //we need to get location. Set this true
        mRequestingLocationUpdates = true;

        //first check whether we have GoogleAPIClient connected. if yes, we request location. Otherwise
        //we connect the client and then in onConnected() we request location
        if (!mGoogleApiClient.isConnected()){
            connentClient();
        }
        else {
            startLocationUpdates();
        }
    }

    public void removeLocationUpdate() {
        //stop requesting location udpates
        mRequestingLocationUpdates = false;
        Log.d(LOG_TAG,"[removeUpdates] going to remove location update ");

        //we stop location update and then disconnect the Clinet.
        stopLocationUpdates();
        disconnectClient();
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
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

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
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


	/**this function is where we got the updated location information**/
    @Override
	public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Log.d(LOG_TAG,"[onLocationChanged] get location " + mCurrentLocation.getLatitude() + mCurrentLocation.getLongitude());

        Toast.makeText(mContext, mContext.getString(R.string.location_updated_message),
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

	}

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(LOG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


    public long getLocationUpdateIntervalInMillis() {
        return UPDATE_INTERVAL_IN_MILLISECONDS;
    }

    public void setLocationUpdateInterval(long updateInterval) {

        UPDATE_INTERVAL_IN_MILLISECONDS = updateInterval;

        //after we get location we need to update the location request
        //1. remove the update
        removeLocationUpdate();
        //2. create new update, and then start update
        createLocationRequest();
        startLocationUpdates();

    }

}
