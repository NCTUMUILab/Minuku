package edu.umich.si.inteco.minuku.util;

/**
 * Created by Armuro on 2/6/16.
 */
public class GoogleAnalyticHelper {

    private static boolean isGoogleAnalyticTrackingEnabled = true;

    public static boolean isGoogleAnalyticTrackingEnabled() {
        return isGoogleAnalyticTrackingEnabled;
    }

    public static void setGoogleAnalyticTrackingEnabled(boolean enabled) {
        GoogleAnalyticHelper.isGoogleAnalyticTrackingEnabled = enabled;
    }
}
