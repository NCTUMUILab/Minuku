package edu.umich.si.inteco.minuku.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

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

    @Override
    protected void onServiceConnected() {

        Log.d(LOG_TAG, "test accessibility onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.notificationTimeout = 500;


        //call setServiceInfo()

    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent evt) {


        // NOTE: Every method that returns an AccessibilityNodeInfo may return null,
        // because the explored window is in another process and the
        // corresponding View might be gone by the time your request reaches the
        // view hierarchy.
        AccessibilityNodeInfo source = evt.getSource();
        if (source == null) {
            return;
        }

        // Grab the parent of the view that fired the event.
        AccessibilityNodeInfo rowNode = getListItemNodeInfo(source);
        if (rowNode == null) {
            return;
        }

        // Using this parent, get references to both child nodes, the label and the checkbox.
        AccessibilityNodeInfo labelNode = rowNode.getChild(0);
        if (labelNode == null) {
            rowNode.recycle();
            return;
        }

        AccessibilityNodeInfo completeNode = rowNode.getChild(1);
        if (completeNode == null) {
            rowNode.recycle();
            return;
        }


        //get text of the event
        CharSequence taskLabel = labelNode.getText();



        /** 1. Widows State Change **/
        if (evt.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && evt.getText().size()>=3){
            Log.d(LOG_TAG, "test accessibility : windows state changed" );
            interpretWindowStateChangeEvent(evt);

        }


        /**
         *  2. detect incoming notification
         *  **/
        if (evt.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Log.d(LOG_TAG, "test accessibility : notificaiton state changed" );
            interpretNotificationStateChanged(evt);
        }


        /**
         *  recording text within the certain application
         *  **/
        if(evt.getEventType()==AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {//if detecting typing
            Log.d(LOG_TAG, "test accessibility : type view text changed" );
            interpretViewTextChanged(evt);
        }


        /**
         *  detecting click action
         *  **/
        if(evt.getEventType()==AccessibilityEvent.TYPE_VIEW_CLICKED) {//if detecting typing
            Log.d(LOG_TAG, "test accessibility : type view clicked" );
            interpreViewClickedEvent(evt);

            /** get target and application name **/

            //get applicaiton information from PhoneStatus



        }


        /**not sure how is would be useful**/
        if(evt.getEventType()==AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
            Log.d(LOG_TAG, "test accessibility : text selection changed" );
            interpretTextSelectionChangedEvent(evt);
        }


        /**might be related to dismissing notification **/
        if (evt.getEventType()==AccessibilityEvent.TYPE_ANNOUNCEMENT) {
            Log.d(LOG_TAG, "test accessibility : announcement changed" );
            interpretAnnouncementEvent(evt);

        }


    }

    @Override
    public void onInterrupt() {
        Log.d(LOG_TAG,"onInterrupt");

    }



    private AccessibilityNodeInfo getListItemNodeInfo(AccessibilityNodeInfo source) {
        AccessibilityNodeInfo current = source;

        while (true) {
            AccessibilityNodeInfo parent = current.getParent();
            if (parent == null) {
                return null;
            }
            if (TASK_LIST_VIEW_CLASS_NAME.equals(parent.getClassName())) {
                return current;
            }
            // NOTE: Recycle the infos.
            AccessibilityNodeInfo oldCurrent = current;
            current = parent;
            oldCurrent.recycle();
        }
    }

    private void interpretWindowStateChangeEvent(AccessibilityEvent evt) {

    }

    private void interpretNotificationStateChanged(AccessibilityEvent evt) {

    }

    private void interpretViewTextChanged(AccessibilityEvent evt) {


    }

    private void interpreViewClickedEvent(AccessibilityEvent evt) {

    }

    private void interpretTextSelectionChangedEvent(AccessibilityEvent evt) {

    }

    private void interpretAnnouncementEvent(AccessibilityEvent evt) {


    }



}
