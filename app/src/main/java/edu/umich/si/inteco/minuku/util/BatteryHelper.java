package edu.umich.si.inteco.minuku.util;

import android.content.Context;

/**
 * Created by Armuro on 7/21/14.
 */
public class BatteryHelper {

    public static float BATTERY_LIFE_PERCENTAGE_THRESHOLD = (float) 0.8;

    private static int mBatteryLevel = -1;
    private static float mBatteryPercentage = -1;
    private static Context mContext;
    private static String mBatteryChargingState = "NA";
    private static boolean mCharging = false;

    public static final String NO_CHARGING = "Not Charging";
    public static final String USB_CHARGING = "USB Charging";
    public static final String AC_CHARGING = "AC Charging";

    private static boolean saveBatteryOnMobility = true;


    public BatteryHelper(Context context ) {
        mContext = context;
    }



    public static int getBatteryLevel() {
        return mBatteryLevel;
    }

    public static void setBatteryLevel(int batteryLevel) {
        mBatteryLevel = batteryLevel;
    }

    public static float getBatteryPercentage() {
        return mBatteryPercentage;
    }

    public static void setBatteryPercentage(float batteryPercentage) {
        mBatteryPercentage = batteryPercentage;
    }

    public static String getBatteryChargingState() {
        return mBatteryChargingState;
    }

    public static void setBatteryChargingState(String batteryChargingState) {
        BatteryHelper.mBatteryChargingState = batteryChargingState;
    }

    public static boolean isCharging() {
        return mCharging;
    }

    public static void setCharging(boolean charging) {
        mCharging = charging;
    }

    public static boolean isSaveBatteryOnMobility() {
        return saveBatteryOnMobility;
    }

    public static void setSaveBatteryOnMobility(boolean saveBatteryOnMobility) {
        BatteryHelper.saveBatteryOnMobility = saveBatteryOnMobility;
    }
}
