package edu.umich.si.inteco.minuku.contextmanager;


import com.google.android.gms.location.DetectedActivity;

/**
 * Created by Armuro on 10/4/15.
 */
public class ActivityRecognitionManager {

    public ActivityRecognitionManager() {

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


}
