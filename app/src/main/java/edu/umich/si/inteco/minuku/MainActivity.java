package edu.umich.si.inteco.minuku;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import edu.umich.si.inteco.minuku.Fragments.CheckinSectionFragment;
import edu.umich.si.inteco.minuku.Fragments.DailyJournalSectionFragment;
import edu.umich.si.inteco.minuku.Fragments.HomeFragment;
import edu.umich.si.inteco.minuku.Fragments.ListRecordingSectionFragment;
import edu.umich.si.inteco.minuku.Fragments.RecordSectionFragment;
import edu.umich.si.inteco.minuku.Fragments.TaskSectionFragment;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.services.MinukuMainService;
import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.ConfigurationManager;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.PreferenceHelper;
import edu.umich.si.inteco.minuku.util.RecordingAndAnnotateManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.Manifest;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private static final String LOG_TAG = "MainActivity";

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    public static final int PAGE_POSITION_RECORDING = 0;
    public static final int PAGE_POSITION_TASKS = 1;
    public static final int PAGE_POSITION_DAILY_JOURMAL = 2;

    // Permission related variables
    String[] PERMISSIONS = {Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_SMS, Manifest.permission.CAMERA};
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    protected static final int FINE_LOCATION = 100;
    protected  static final int CAMERA = 101;
    private static final int REQUEST_WRITE_STORAGE = 102;

    private static String mReviewMode = RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_ALL;
    private static String mLaunchTab = Constants.MAIN_ACTIVITY_TAB_RECORD;
    private int currentTabPos = -1;

    //provide fragments for each of the three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter} 
    //derivative, which will keep every loaded fragment in memory
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;


    //display the three primary sections of the app, one at a time
    ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        setContentView(R.layout.activity_main);

        /**For Google Analytic**/
        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        AnalyticsMinuku application = (AnalyticsMinuku) getApplication();
        mTracker = application.getDefaultTracker();

        //RemoteDBHelper.syncWithRemoteDatabase();
        // Log.d(LOG_TAG, "[queryLastBackgroundLoggingSyncHourUsingPOST] get the synTime is " + lastSynhour);


        //permissions
        if(checkAndRequestPermissions()) {
            // carry on the normal flow, as the case of  permissions  granted.
        }


        /**start the contextManager service**/
        if (!MinukuMainService.isServiceRunning()){
            Log.d(LOG_TAG, "[test service running]  going start the probe service isServiceRunning:" + MinukuMainService.isServiceRunning());
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MinukuMainService.class);
            //start the Minuku service
            startService(intent);
        }


        /** Create the adapter that will return a fragment for each of the three primary sections
        // of the app.**/
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.main_layout);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);

            }
        });




        //we first set the mLaunchtab parameter based on the study condition
        //TODO: Probe should not have conditions, remove this after the labeling study..

        //Minuku is not running with any condition, just normaly. there's only profile interface in this condition.
        if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.NORMAL_CONDITION)) {
            mLaunchTab = Constants.MAIN_ACTIVITY_TAB_HOME;
            actionBar.addTab(actionBar.newTab().setText(Constants.MAIN_ACTIVITY_TAB_HOME).setTabListener(this));
        }

        //Minuku is running with Conditions
        else {
            if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.PARTICIPATORY_LABELING_CONDITION)) {
                mLaunchTab = Constants.MAIN_ACTIVITY_TAB_RECORD;
                actionBar.addTab(actionBar.newTab().setText(Constants.MAIN_ACTIVITY_TAB_RECORD).setTabListener(this));
                actionBar.addTab(actionBar.newTab().setText(Constants.MAIN_ACTIVITY_TAB_RECORDINGS).setTabListener(this));
            }
            else if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.IN_STIU_LABELING_CONDITION)) {
                mLaunchTab = Constants.MAIN_ACTIVITY_TAB_DAILY_REPORT;
            }

            else if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.POST_HOC_LABELING_CONDITION)) {
                mLaunchTab = Constants.MAIN_ACTIVITY_TAB_DAILY_REPORT;
                actionBar.addTab(actionBar.newTab().setText(Constants.MAIN_ACTIVITY_TAB_RECORDINGS).setTabListener(this));
            }
            else if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.HYRBID_LABELING_CONDITION)) {
                mLaunchTab = Constants.MAIN_ACTIVITY_TAB_RECORD;
                actionBar.addTab(actionBar.newTab().setText(Constants.MAIN_ACTIVITY_TAB_RECORD).setTabListener(this));
                actionBar.addTab(actionBar.newTab().setText(Constants.MAIN_ACTIVITY_TAB_RECORDINGS).setTabListener(this));
            }

            actionBar.addTab(actionBar.newTab().setText(Constants.MAIN_ACTIVITY_TAB_DAILY_REPORT).setTabListener(this));
            actionBar.addTab(actionBar.newTab().setText(Constants.MAIN_ACTIVITY_TAB_TASKS).setTabListener(this));

        }



        currentTabPos = -1;

    }


    private  boolean checkAndRequestPermissions() {

        int permissionReadExternalStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteExternalStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

//    public static boolean hasPermissions(Context context, String... permissions) {
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
//            for (String permission : permissions) {
//                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(LOG_TAG, "[permission test]Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();

                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            ) {
                        Log.d(LOG_TAG, "[permission test]all permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(LOG_TAG, "[permission test]Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                            showDialogOK("all Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }
    }


    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }



    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first


        // Google Analytic [START custom_event]
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("MinukuAppIsReopened")
                .build());
        // Google Analytic [END custom_event]


        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        //get bundles and set up environment
        Bundle bundle = getIntent().getExtras();



        if (bundle!=null ) {

            if (bundle.containsKey(ConfigurationManager.ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING)) {

                String reviewMode = bundle.getString(ConfigurationManager.ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING);
                if (reviewMode!=null){
                    mReviewMode = reviewMode;
                    //showRecordingList(mReviewMode);
                }
            }

            if (bundle.containsKey("launchTab")) {

                mLaunchTab = bundle.getString("launchTab");
            }

        }


        //if the user specify which tab to launch, we launch that tab
        if (mLaunchTab!=null) {
            for (int i=0; i<actionBar.getTabCount(); i++) {
                if (actionBar.getTabAt(i).getText().equals(mLaunchTab)) {
                    actionBar.selectTab(actionBar.getTabAt(i));
                }
            }

            mLaunchTab = null;
        }
        //otherwise we go to the tab that the user stayed last time
        else {
            //  actionBar.selectTab(actionBar.getTabAt(currentTabPos));
        }





    }

//    public void requestAllPermissions() {
//        View mLayout = findViewById(R.id.main_layout);
//
//        /**permission for location **/
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {
//
//                Log.i("MainActivity",
//                        "Displaying location permission rationale to provide additional context.");
//
//                Snackbar.make(mLayout, R.string.permission_location_rationale,
//                        Snackbar.LENGTH_INDEFINITE)
//                        .setAction(R.string.ok, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                ActivityCompat.requestPermissions(MainActivity.this,
//                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                        FINE_LOCATION);
//                            }
//                        })
//                        .show();
//            } else {
//                // Location permission has not been granted yet. Request it directly.
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        FINE_LOCATION);
//            }
//
//        }
//
//        /**permission for camera **/
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.CAMERA)) {
//
//                Log.i("MainActivity",
//                        "Displaying camera permission rationale to provide additional context.");
//
//                Snackbar.make(mLayout, R.string.permission_camera_rationale,
//                        Snackbar.LENGTH_INDEFINITE)
//                        .setAction(R.string.ok, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                ActivityCompat.requestPermissions(MainActivity.this,
//                                        new String[]{Manifest.permission.CAMERA},
//                                        CAMERA);
//                            }
//                        })
//                        .show();
//            } else {
//
//                // Location permission has not been granted yet. Request it directly.
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.CAMERA},
//                        CAMERA);
//            }
//        }
//
//
//        /**permission for writing external drive **/
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//
//                Log.i("MainActivity",
//                        "Displaying external permission rationale to provide additional context.");
//
//                Snackbar.make(mLayout, R.string.permission_write_exterinal_storage_rationale,
//                        Snackbar.LENGTH_INDEFINITE)
//                        .setAction(R.string.ok, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                ActivityCompat.requestPermissions(MainActivity.this,
//                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                                        REQUEST_WRITE_STORAGE);
//                            }
//                        })
//                        .show();
//            } else {
//
//                // permission has not been granted yet. Request it directly.
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        REQUEST_WRITE_STORAGE);
//            }
//        }
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

        mViewPager.setCurrentItem(tab.getPosition());

        //save the current page
        currentTabPos = tab.getPosition();

        Log.d(LOG_TAG, "[onTabSelected] the selected page position is " + tab.getPosition());

        //refrest list recording if users click on the tab
        if (tab.getText().equals(Constants.MAIN_ACTIVITY_TAB_RECORDINGS)){
            ListRecordingSectionFragment.refreshRecordingList();
        }

        //Log user action
        LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                LogManager.LOG_TAG_USER_CLICKING,
                "User Click:\t" + "Tab " + tab.getText());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    private void sendScreenNameToGoogleAnalytic(String screenname) {

        // Google Analystic [START screen_view_hit]
        mTracker.setScreenName("screenName : " + screenname);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // Google Analystic  [END screen_view_hit]

    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            //Google Analytic
            sendScreenNameToGoogleAnalytic("Opening Minuku");

            switch (i) {

                case 0:
                    if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.NORMAL_CONDITION)){
                    HomeFragment homeSectionFragment = new HomeFragment();
                        homeSectionFragment.setRetainInstance(true);
                        return homeSectionFragment;
                    }

                    else if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.PARTICIPATORY_LABELING_CONDITION)){
                        RecordSectionFragment recordSectionFragment = new RecordSectionFragment();
                        recordSectionFragment.setRetainInstance(true);

                        return recordSectionFragment;
                    }
                    else if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.HYRBID_LABELING_CONDITION)){
                        CheckinSectionFragment checkinSectionFragment = new CheckinSectionFragment();
                        checkinSectionFragment.setRetainInstance(true);

                        return checkinSectionFragment;
                    }

                    else {
                        DailyJournalSectionFragment dailyJournalSectionFragment = new DailyJournalSectionFragment();
                        dailyJournalSectionFragment.setRetainInstance(true);
                        return dailyJournalSectionFragment;
                    }

                    // The first section of the app is the most interesting -- it offers
                    // a launchpad into the other demonstrations in this example application.

                case 1:
                    if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.PARTICIPATORY_LABELING_CONDITION)){
                        mReviewMode = RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_ALL;
                        ListRecordingSectionFragment listRecordingSectionFragment = new ListRecordingSectionFragment();
                        listRecordingSectionFragment.setReviewMode(mReviewMode);
                        listRecordingSectionFragment.setRetainInstance(true);

                        return listRecordingSectionFragment;
                    }
                    else if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.HYRBID_LABELING_CONDITION)){
                        mReviewMode = RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_ALL;
                        ListRecordingSectionFragment listRecordingSectionFragment = new ListRecordingSectionFragment();
                        listRecordingSectionFragment.setReviewMode(mReviewMode);
                        listRecordingSectionFragment.setRetainInstance(true);

                        return listRecordingSectionFragment;
                    }
                    else if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.IN_STIU_LABELING_CONDITION)){
                        mReviewMode = RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_ALL;
                        ListRecordingSectionFragment listRecordingSectionFragment = new ListRecordingSectionFragment();
                        listRecordingSectionFragment.setReviewMode(mReviewMode);
                        listRecordingSectionFragment.setRetainInstance(true);

                        return listRecordingSectionFragment;
                    }
                    else {

                        TaskSectionFragment taskSectionFragment = new TaskSectionFragment();
                        taskSectionFragment.setRetainInstance(true);
                        return taskSectionFragment;
                    }

                case 2:
                    DailyJournalSectionFragment dailyJournalSectionFragment = new DailyJournalSectionFragment();
                    dailyJournalSectionFragment.setRetainInstance(true);
                    return dailyJournalSectionFragment;

                default:
                    TaskSectionFragment taskSectionFragment1 = new TaskSectionFragment();
                    taskSectionFragment1.setRetainInstance(true);
                    return taskSectionFragment1;
            }
        }

        @Override
        public int getCount() {

            if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.PARTICIPATORY_LABELING_CONDITION))
                return 4;
            else if (Constants.CURRENT_STUDY_CONDITION.equals(Constants.IN_STIU_LABELING_CONDITION))
                return 2;
            else
                return 3;


        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Section " + (position + 1);
        }
    }



}
