package edu.umich.si.inteco.minuku.context;

/**
 * Created by Armuro on 9/29/15.
 */
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umich.si.inteco.minuku.util.PreferenceHelper;


public class GeofenceManager implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback{


    /**parameters**/
    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km


    /** Tag for logging. */
    private static final String LOG_TAG = "GeofenceMgr";
    private final Context mContext;

    //Client for Google Play service
    private GoogleApiClient mGoogleApiClient;

    //the list of geofences to add
    private ArrayList<Geofence> mGeofenceList;

    //keep track of whether geogences were added
    private boolean mGeofencesAdded;

    //requesting to add or remove geofences
    private PendingIntent mGeofencePendingIntent;

    ///Flag that indicates whether an add or remove request is underway.
    private boolean mAddingGeofenceInProgress;

    //Used to persist application state about whether geofences were added.
    private SharedPreferences mSharedPreferences;

    public GeofenceManager(Context context) {

        // Save the context
        mContext = context;
        //initiate the pendingIntent for adding or removing geofence
        mGeofencePendingIntent = null;

        //empty list for storing geofences
        mGeofenceList = new ArrayList<Geofence>();

        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = mContext.getSharedPreferences(PreferenceHelper.SHARED_PREFERENCE_NAME,
                Context.MODE_PRIVATE);

        populateGeofenceList();

        //build GoogleAPIClient to connect to the Google Play Service
        buildGoogleApiClient();

        mAddingGeofenceInProgress = false;





    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * get the current GoogleAPIClient
     * @return
     */
    private GoogleApiClient getGoogleApiClient() {

        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        return mGoogleApiClient;
    }

    /**
     * connect to the service
     */
    private void requestConnection() {
        getGoogleApiClient().connect();
    }


    private void requestDisconnection() {
        getGoogleApiClient().disconnect();
    }



    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG_TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost
        Log.i(LOG_TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    @Override
    public void onResult(Result status) {
        if (status.getStatus().isSuccess()) {

            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(PreferenceHelper.CONTEXT_GEOFENCE_ADDED_KEY, mGeofencesAdded);
            editor.commit();

            Toast.makeText(
                    mContext,
                    "geofence successfully added",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            Log.e(LOG_TAG, "Registering geofence failed: " + status.getStatus().getStatusMessage() +
                    " : " + status.getStatus().getStatusCode());
        }
    }

    /**
     * Whenever we add a geofence, we need to connect to the server and add geofence..
     * the service sets alerts to be notified when the device enters or exists one of the specified geogfences.
     * Handles the success or failer results reutnred by addGeofences()
     * @param geofences
     * @throws UnsupportedOperationException
     */
    public void addGeofences(List<Geofence> geofences) {

        //if the client is not connected
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(mContext, "the Geofence is not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,

                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().


        }catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }

        //save the geofences
        mGeofenceList = (ArrayList<Geofence>) geofences;


    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofences() {
        //if the client is not connected
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(mContext, "the Geofence is not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().

        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }


    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {

        //create a geofence request builder
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceTransitionService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(LOG_TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }



    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    public void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : BAY_AREA_LANDMARKS.entrySet()) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                            // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            GEOFENCE_RADIUS_IN_METERS
                    )

                            // Set the expiration duration of the geofence. This geofence gets automatically
                            // removed after this period of time.
                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                            // Set the transition types of interest. Alerts are only generated for these
                            // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                            // Create the geofence.
                    .build());
        }
    }

    /**
     * Map for storing information about airports in the San Francisco bay area.
     */
    public static final HashMap<String, LatLng> BAY_AREA_LANDMARKS = new HashMap<String, LatLng>();
    static {
        // San Francisco International Airport.
        BAY_AREA_LANDMARKS.put("SFO", new LatLng(37.621313, -122.378955));

        // Googleplex.
        BAY_AREA_LANDMARKS.put("GOOGLE", new LatLng(37.422611,-122.0840577));
    }

}
