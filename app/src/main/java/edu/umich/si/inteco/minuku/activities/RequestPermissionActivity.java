package edu.umich.si.inteco.minuku.activities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.Notification;
import edu.umich.si.inteco.minuku.util.NotificationHelper;
import edu.umich.si.inteco.minuku.util.PreferenceHelper;

public class RequestPermissionActivity extends Activity {

    private static final String LOG_TAG = "RequestPermission";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        int requestCode =bundle.getInt(NotificationHelper.REQUEST_PERMISSION_CODE);
        String permissionName = bundle.getString(NotificationHelper.REQUEST_PERMISSION_NAME);

        requestPermission(requestCode, permissionName);


        setContentView(R.layout.activity_request_permission);
    }


    private void requestPermission( int requestCode, String permissionName) {
        /** read phone state**/
        if (permissionName.equals(Manifest.permission.READ_PHONE_STATE)) {

            //should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
            //we requestpermission
            else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{permissionName},
                        requestCode);

            }

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case Constants.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {

                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // permission was granted, yay!
                        Log.d(LOG_TAG, "[test permission] READ_PHONE_STATE is granted ");

                        getDeviceID();
                    } else {

                        // permission was not granted.
                        Log.d(LOG_TAG, "[test permission] READ_PHONE_STATE is not granted ");

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_PHONE_STATE)) {

                            Log.d(LOG_TAG, "[test permission] show explanation dialog ");

                        }else{
                            //Never ask again selected, or device policy prohibits the app from having that permission.
                            //So, disable that feature, or fall back to another situation...

                            Constants.DEVICE_ID = "NOT GRANTED PERMISSION";
                            Log.d(LOG_TAG, "[test permission] do not show explanation dialog, just give up, the device id is " + Constants.DEVICE_ID);
                        }

                    }

                }
            }

        }
    }


    /**
     * after level 23 we need to request permission at run time. So Minuku currently doesn't support Android 6!
     * http://developer.android.com/training/permissions/requesting.html
     */
    private void getDeviceID() {

        Log.d(LOG_TAG, "[test permission] we're attempting to get deviceID ");

        /** when the app starts, first obtain the participant ID **/
        TelephonyManager mngr = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);

        Constants.DEVICE_ID = mngr.getDeviceId();

        //combined device id, timestamp, and minuku
        Constants.USER_ID = (Constants.MINUKU_PREFIX + (ContextManager.getCurrentTimeInMillis() + Constants.DEVICE_ID).hashCode());

        //TODO: create user loggin and use that as the id.
        // add a unixtime to it and then hash the whole string. The purpose is try to create an unidentifiable user id

        Log.d(LOG_TAG, "[test permission] RequestPermission get the synTime is " + Constants.DEVICE_ID);

        PreferenceHelper.setPreferenceValue(PreferenceHelper.DEVICE_ID, Constants.DEVICE_ID);
        PreferenceHelper.setPreferenceValue(PreferenceHelper.USER_ID, Constants.USER_ID);

        Log.d(LOG_TAG, "[test permission] RequestPermission already set device ID " + PreferenceHelper.getPreferenceString(PreferenceHelper.DEVICE_ID, "NA")) ;
        Log.d(LOG_TAG, "[test permission] RequestPermission already set user ID " + PreferenceHelper.getPreferenceString(PreferenceHelper.USER_ID, "NA")) ;

    }


}
