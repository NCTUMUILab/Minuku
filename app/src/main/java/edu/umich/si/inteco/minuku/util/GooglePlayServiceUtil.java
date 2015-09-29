package edu.umich.si.inteco.minuku.util;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.common.ConnectionResult;

import edu.umich.si.inteco.minuku.R;

public class GooglePlayServiceUtil {

	// track what type of request is in process
    public enum ACTIVITY_REQUEST_TYPE {ADD, REMOVE}

    public enum LOCATION_REQUEST_TYPE {ADD, REMOVE}
    
    // Used to track what type of geofence removal request was made.
    public enum GEO_FENCE_REMOVE_TYPE {INTENT, LIST}

    // Used to track what type of request is in process
    public enum GEOFENCE_REQUEST_TYPE {ADD, REMOVE}

    public static final String LOG_TAG = "ActivityUtils";

    /**
     * PendingIntent Request Code
     */
	public static final int LOCATION_NOTIFICATION_PENDING_INTENT_REQUEST_CODE = 1;
	
	public static final int ACTIVITY_RECOGNITION_PENDING_INTENT_REQUEST_CODE = 2;
	
	public static final int GEOFENCE_TRANSITION_PENDING_INTENT_REQUEST_CODE = 4;
    
    
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Intent actions and extras for sending information from the IntentService to the Activity
    public static final String ACTION_CONNECTION_ERROR =
            "edu.umich.si.inteco.captureprobe.activityrecognition.ACTION_CONNECTION_ERROR";

    public static final String ACTION_REFRESH_STATUS_LIST =
                    "edu.umich.si.inteco.captureprobe.activityrecognition.ACTION_REFRESH_STATUS_LIST";

    public static final String CATEGORY_LOCATION_SERVICES =
            "edu.umich.si.inteco.captureprobe.activityrecognition.CATEGORY_LOCATION_SERVICES";

    public static final String EXTRA_CONNECTION_ERROR_CODE =
            "edu.umich.si.inteco.captureprobe.activityrecognition.EXTRA_CONNECTION_ERROR_CODE";

    public static final String EXTRA_CONNECTION_ERROR_MESSAGE =
            "edu.umich.si.inteco.captureprobe.activityrecognition.EXTRA_CONNECTION_ERROR_MESSAGE";
	public GooglePlayServiceUtil() {
		
	}
	
    public static String getErrorString(Context context, int errorCode) {

        // Get a handle to resources, to allow the method to retrieve messages.
        Resources mResources = context.getResources();

        // Define a string to contain the error message
        String errorString;

        // Decide which error message to get, based on the error code.
        switch (errorCode) {

            case ConnectionResult.DEVELOPER_ERROR:
                errorString = mResources.getString(R.string.connection_error_misconfigured);
                break;

            case ConnectionResult.INTERNAL_ERROR:
                errorString = mResources.getString(R.string.connection_error_internal);
                break;

            case ConnectionResult.INVALID_ACCOUNT:
                errorString = mResources.getString(R.string.connection_error_invalid_account);
                break;

            case ConnectionResult.LICENSE_CHECK_FAILED:
                errorString = mResources.getString(R.string.connection_error_license_check_failed);
                break;

            case ConnectionResult.NETWORK_ERROR:
                errorString = mResources.getString(R.string.connection_error_network);
                break;

            case ConnectionResult.RESOLUTION_REQUIRED:
                errorString = mResources.getString(R.string.connection_error_needs_resolution);
                break;

            case ConnectionResult.SERVICE_DISABLED:
                errorString = mResources.getString(R.string.connection_error_disabled);
                break;

            case ConnectionResult.SERVICE_INVALID:
                errorString = mResources.getString(R.string.connection_error_invalid);
                break;

            case ConnectionResult.SERVICE_MISSING:
                errorString = mResources.getString(R.string.connection_error_missing);
                break;

            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                errorString = mResources.getString(R.string.connection_error_outdated);
                break;

            case ConnectionResult.SIGN_IN_REQUIRED:
                errorString = mResources.getString(
                        R.string.connection_error_sign_in_required);
                break;

            default:
                errorString = mResources.getString(R.string.connection_error_unknown);
                break;
        }

        // Return the error message
        return errorString;
    }
	
    public static String getIdFromGeofenceResult(String geofenceResult){
    	
    	String id=null;
    	
    	if (geofenceResult!=null){
    		
    		int start = geofenceResult.indexOf(":");
    		int end = geofenceResult.indexOf(" ", start);
    		id = geofenceResult.substring(start, end);
    	}
    	
    	return id;
    }
    
    public static String getDwellTimeFromGeofenceResult(String geofenceResult) {
    	
    	String dwellTime=null;
    	
    	if (geofenceResult!=null){
    		
    		int start = geofenceResult.indexOf("dwell");
    		int end = geofenceResult.indexOf(",", start);
    		dwellTime = geofenceResult.substring(start, end);
    	}
    	
    	return dwellTime;
    }
    

    public static String getLatLngRadiusFromGeofenceResult(String geofenceResult) {
    	
    	String latLngRadius=null;
    	
    	/**
    	 [CIRCLE id:1 transitions:5 42.279456, -83.740991 200m, resp=0s, dwell=30000ms, @-1]

    	 */
    	if (geofenceResult!=null){
    		
    		int start = geofenceResult.indexOf("transitions") + 14;
    		int firstComma = geofenceResult.indexOf(",",start);
    		int secondComma = geofenceResult.indexOf(",",firstComma+1);
    		latLngRadius = geofenceResult.substring(start, secondComma);

    	}
    	
    	return latLngRadius;
    }
	
}
