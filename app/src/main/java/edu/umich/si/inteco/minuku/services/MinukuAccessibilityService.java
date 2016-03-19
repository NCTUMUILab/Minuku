package edu.umich.si.inteco.minuku.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.UserInteractionManager;
import edu.umich.si.inteco.minuku.model.UserInteraction;
import edu.umich.si.inteco.minuku.util.PreferenceHelper;

/**
 * Created by Armuro on 3/15/16.
 */
public class MinukuAccessibilityService extends AccessibilityService {


    private static final String LOG_TAG = "MinukuAcsbltyService";
    /** Comma separator. */
    private static final String SEPARATOR = ", ";
    /** The class name of TaskListView - for simplicity we speak only its items. */
    private static final String TASK_LIST_VIEW_CLASS_NAME =
            "io.appium.android.apis.accessibility.TaskListView";



    private static boolean isInAppActionRequested = false;

    private static UserInteractionManager mUserInteractionManager;

    public MinukuAccessibilityService() {
        super();
    }

    public MinukuAccessibilityService(UserInteractionManager userInteractionManager) {
        super();
        mUserInteractionManager = userInteractionManager;
    }

    @Override
    protected void onServiceConnected() {

        isInAppActionRequested = PreferenceHelper.getPreferenceBoolean(PreferenceHelper.USER_INTERACTION_IN_APP_USE_REQUESTED, false);

        Log.d(LOG_TAG, "test accessibility  preference set to " +
                PreferenceHelper.getPreferenceBoolean(PreferenceHelper.USER_INTERACTION_IN_APP_USE_REQUESTED, false));

        updateListeningAccessibilityEvents();

    }


    /**
     * this function will determine whether to subscribe to events based on whether the ContextSource
     * InAppAction is requested in UserInteractionManagerment or not.
     */
    public void updateListeningAccessibilityEvents () {


        Log.d(LOG_TAG, "test accessibility  isInAppActionRequested is " + isInAppActionRequested);

        /**** if inAppAction is not requested we should not subscribe to any event **/
        if (!isInAppActionRequested()){
            Log.d(LOG_TAG, "test accessibility  the inappaction is requested so we DO NOT define events to subscribe");
            unsubscribeInAppEvents();
        }

        /**if it is requested we should subscrieb to the required set of event **/
        else {
            Log.d(LOG_TAG, "test accessibility  the inappaction is requested so we DO  define events to subscribe");
            subscribedInAppEvents();

        }


    }


    private void unsubscribeInAppEvents() {
        Log.d(LOG_TAG, "test accessibility subscribing......");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        setServiceInfo(info);
    }


    private void subscribedInAppEvents() {
        Log.d(LOG_TAG, "test accessibility subscribing......");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.notificationTimeout = 500;

        info.flags = AccessibilityServiceInfo.DEFAULT;
        //define the event we want to listen
        info.eventTypes =
                /** interaction **/

                //Represents the event of clicking on a View like Button, CompoundButton, etc.  1
                AccessibilityEvent.TYPE_VIEW_CLICKED

                        //Represents the event of long clicking on a View like Button, CompoundButton, etc. 2
                        | AccessibilityEvent.TYPE_VIEW_LONG_CLICKED

                        //Represents the event of scrolling a view.
                        | AccessibilityEvent.TYPE_VIEW_SCROLLED     //4096

                        //Represents the event of selecting an item usually in the context of an AdapterView.  4
                        | AccessibilityEvent.TYPE_VIEW_SELECTED

                        //Represents the event of opening a PopupWindow, Menu, Dialog, etc
                        | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

//                        // Represents the event of changing the selection in an EditText  8192
//                        | AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED

                        //Represents the event of starting a touch exploration gesture.  512
                        | AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START

                        //Represents the event of the user ending to touch the screen. 2097152
                        | AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END

                        //represents the event of starting a user gesture detection. 262144
                        | AccessibilityEvent.TYPE_GESTURE_DETECTION_START

                        //Represents the event of ending gesture detection.  524288
                        | AccessibilityEvent.TYPE_GESTURE_DETECTION_END

                        //Represents the event of the user starting to touch the screen.  1048576
                        | AccessibilityEvent.TYPE_TOUCH_INTERACTION_START

                        //Represents the event of the user ending to touch the screen. 2097152
                        | AccessibilityEvent.TYPE_TOUCH_INTERACTION_END

//                        //Represents the event of a context click on a View. 8388608
//                        | AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED

                        //Represents the event showing a Notification    64
                        | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED

                        //Represents the event of changing the text of an EditText. 16
                        | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED

                        //represents the event of an application making an announcement  16384
                        | AccessibilityEvent.TYPE_ANNOUNCEMENT;


        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        setServiceInfo(info);


    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent evt) {


//        Log.d(LOG_TAG, "test accessibility  the type is " + evt.getEventType() + " string: " + evt.toString());


        /**
         *  2. detect incoming notification
         *  **/
        if (evt.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
//            Log.d(LOG_TAG, "test accessibility : notificaiton state changed" );
            interpretNotificationStateChanged(evt);
        }


        /**
         *  recording text within the certain application
         *  **/
        if(evt.getEventType()==AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {//if detecting typing
//            Log.d(LOG_TAG, "test accessibility : type view text changed" );
            interpretViewTextChanged(evt);
        }


        /**
         *  detecting click action
         *  **/
        if(evt.getEventType()==AccessibilityEvent.TYPE_VIEW_CLICKED) {//if detecting typing

//            Log.d(LOG_TAG, "test accessibility : type view clicked " );
            interpreViewClickedEvent(evt);

        }

        /**
         *  detecting scroll action
         *  **/
        if(evt.getEventType()==AccessibilityEvent.TYPE_VIEW_SCROLLED) {//if detecting typing
//            Log.d(LOG_TAG, "test accessibility : type view scrolled" );
            interpreViewScolledEvent(evt);

        }
//
//
//        /**not sure how is would be useful**/
//        if(evt.getEventType()==AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
//            Log.d(LOG_TAG, "test accessibility : text selection changed" );
//            interpretTextSelectionChangedEvent(evt);
//        }


        /**might be related to dismissing notification **/
        if (evt.getEventType()==AccessibilityEvent.TYPE_ANNOUNCEMENT) {
//            Log.d(LOG_TAG, "test accessibility : announcement changed" );
            interpretAnnouncementEvent(evt);

        }



        /**might be related to pulling down the notificaiton bar and detaled view people are looking at  **/
        if (evt.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//            Log.d(LOG_TAG, "test accessibility : window state changed" );
            interpretWindowStateChangeEvent(evt);

        }


    }

    @Override
    public void onInterrupt() {
        Log.d(LOG_TAG, "onInterrupt");

    }




    private void interpretWindowStateChangeEvent(AccessibilityEvent evt) {


        //if the windows changed event is relevant to notification
        if (evt.getText().toString().contains("Notification") || evt.getText().toString().contains("notification")   ){

            //the user pull down the notificatiion bar

            String notificationActionType = UserInteractionManager.CONTEXT_SOURCE_NOTIFICATION_VIEW;
            String packageName = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
            String targetText = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
            String extra = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;

            if (evt.getPackageName()!=null){
                packageName = evt.getPackageName().toString();
            }

            if (evt.getClassName()!=null ) {
                targetText = evt.getClassName().toString();
            }

            if (evt.getText()!=null) {
                targetText += Constants.CONTEXT_SOURCE_DELIMITER +  evt.getText().toString();
            }

            if (evt.getContentDescription()!=null) {
                extra = evt.getContentDescription().toString();
            }


            long time = ContextManager.getCurrentTimeInMillis();

            UserInteraction notificationViewAction = new UserInteraction(notificationActionType, targetText, packageName, extra, time);
            mUserInteractionManager.setLatestInAppAction(notificationViewAction);


        }

    }

    private void interpretNotificationStateChanged(AccessibilityEvent evt) {

    }

    private void interpretViewTextChanged(AccessibilityEvent evt) {

        String actionType = UserInteractionManager.CONTEXT_SOURCE_MEASURE_USER_ACTION_TYPE;
        String packageName = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
        String targetText = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
        String extra = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;

        if (evt.getPackageName()!=null){
            packageName = evt.getPackageName().toString();
        }

        if (evt.getClassName()!=null ) {
            targetText = evt.getClassName().toString();
        }

        if (evt.getText()!=null) {
            targetText += Constants.CONTEXT_SOURCE_DELIMITER +  evt.getText().toString();
        }

        if (evt.getContentDescription()!=null) {
            extra = evt.getContentDescription().toString();
        }


        long time = ContextManager.getCurrentTimeInMillis();

        UserInteraction inAppAction = new UserInteraction(actionType, targetText, packageName, extra, time);

        if (mUserInteractionManager!=null)
            mUserInteractionManager.setLatestInAppAction(inAppAction);


    }

    private void interpreViewClickedEvent(AccessibilityEvent evt ) {

        String actionType = UserInteractionManager.CONTEXT_SOURCE_MEASURE_USER_ACTION_CLICK;
        String packageName = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
        String targetText = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
        String extra = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;

        if (evt.getPackageName()!=null){
            packageName = evt.getPackageName().toString();
        }

        if (evt.getClassName()!=null ) {
           targetText = evt.getClassName().toString();
        }

        if (evt.getText()!=null) {
            targetText += Constants.CONTEXT_SOURCE_DELIMITER +  evt.getText().toString();
        }

        if (evt.getContentDescription()!=null) {
            extra = evt.getContentDescription().toString();
        }


        long time = ContextManager.getCurrentTimeInMillis();

        UserInteraction inAppAction = new UserInteraction(actionType, targetText, packageName, extra, time);

        if (mUserInteractionManager!=null)
            mUserInteractionManager.setLatestInAppAction(inAppAction);

    }

    private void interpreViewScolledEvent(AccessibilityEvent evt) {

        String actionType = UserInteractionManager.CONTEXT_SOURCE_MEASURE_USER_ACTION_SCROLL;
        String packageName = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
        String targetText = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;
        String extra = ContextManager.CONTEXT_SOURCE_INVALID_VALUE_STRING;

        if (evt.getPackageName()!=null){
            packageName = evt.getPackageName().toString();
        }

        if (evt.getClassName()!=null ) {
            targetText = evt.getClassName().toString();
        }

        if (evt.getText()!=null) {
            targetText += Constants.CONTEXT_SOURCE_DELIMITER +  evt.getText().toString();
        }

        if (evt.getContentDescription()!=null) {
            extra = evt.getContentDescription().toString();
        }


        long time = ContextManager.getCurrentTimeInMillis();

        UserInteraction inAppAction = new UserInteraction(actionType, targetText, packageName, extra, time);

        if (mUserInteractionManager!=null)
            mUserInteractionManager.setLatestInAppAction(inAppAction);

    }

    private void interpretTextSelectionChangedEvent(AccessibilityEvent evt) {


    }

    private void interpretAnnouncementEvent(AccessibilityEvent evt) {


    }

    public boolean isInAppActionRequested() {
        return isInAppActionRequested;
    }

    public void setIsInAppActionRequested(boolean isInAppActionRequested) {
        this.isInAppActionRequested = isInAppActionRequested;

        //also set to preference. because OnServiceConnected is called before the ContextMAnager when the phone restarts.
        PreferenceHelper.setPreferenceValue(PreferenceHelper.USER_INTERACTION_IN_APP_USE_REQUESTED, isInAppActionRequested);

        Log.d(LOG_TAG, "test accessibility  preference set to " +
                PreferenceHelper.getPreferenceBoolean(PreferenceHelper.USER_INTERACTION_IN_APP_USE_REQUESTED, false));


        updateListeningAccessibilityEvents();


    }




}
