package edu.umich.si.inteco.minuku.context.ContextStateManagers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.model.ContextSource;
import edu.umich.si.inteco.minuku.model.Record.Record;
import edu.umich.si.inteco.minuku.model.UserInteraction;
import edu.umich.si.inteco.minuku.services.MinukuAccessibilityService;

public class UserInteractionManager extends ContextStateManager {

	/**Table Names**/
	public static final String RECORD_TABLE_NAME_IN_APP_ACTION = "Record_Table_InAppAction";
	public static final String RECORD_TABLE_NAME_ON_DEVICE_ACTION = "Record_Table_OnDeviceAction";
	public static final String RECORD_TABLE_NAME_IN_MINUKU_ACTION = "Record_Table_InMinukuAction";


	public static final String STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_APP_ACTION = "UserInteraction-InAppAction";
	public static final String STRING_CONTEXT_SOURCE_USER_INTERACTION_ON_DEVICE_ACTION = "UserInteraction-OnDeviceAction";
	public static final String STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_MINUKU_ACTION = "UserInteraction-InMinukuAction";

	public static final int CONTEXT_SOURCE_USER_INTERACTION_IN_MINUKU_ACTION = 0;
	public static final int CONTEXT_SOURCE_USER_INTERACTION_IN_APP_ACTION = 1;
	public static final int CONTEXT_SOURCE_USER_INTERACTION_ON_DEVICE_ACTION = 2;

	public static final String CONTEXT_SOURCE_MEASURE_USER_ACTION_CLICK = "Click";
	public static final String CONTEXT_SOURCE_MEASURE_USER_ACTION_SCROLL = "Scroll";
	public static final String CONTEXT_SOURCE_MEASURE_USER_ACTION_TYPE = "Type";

	public static final String RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION = "InAppAction";
	public static final String RECORD_DATA_PROPERTY_USER_INTERACTION_ON_DEVICE_ACTION = "OnDeviceAction";
	public static final String RECORD_DATA_PROPERTY_USER_INTERACTION_IN_MINUKU_ACTION = "InMinukuAction";
	
	public static final String RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_CLICK  = "Click";
	public static final String RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_SCROLL  = "Scroll";
	public static final String RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_TYPE  = "Type";
	public static final String CONTEXT_SOURCE_NOTIFICATION_VIEW = "ViewNotification";
	public static final String CONTEXT_SOURCE_NOTIFICATION_DISMISS_ALL = "DismissAllNotification";
	public static final String CONTEXT_SOURCE_NOTIFICATION_DISMISS_ONE = "DismissOneNotification";
	public static final String CONTEXT_SOURCE_NOTIFICATION_ACTION_ON = "ActionOnNotification";
	public static final String CONTEXT_SOURCE_NOTIFICATION_SELECT = "SelectNotification";


	public static final String RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_ACTION = "Action";
	public static final String RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_TIME = "Time";
	public static final String RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_PACKAGE = "Package";
	public static final String RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_TARGET = "Target";
	public static final String RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_EXTRA = "Extra";


	private static UserInteraction mLatestInAppAction;
	private static UserInteraction mLatestNotificationAction;
	private static UserInteraction mActionOnDevice;

	MinukuAccessibilityService mMinukuAccessibilityService;


	private Context mContext;
	private static final String LOG_TAG = "UserInteractionManager";

	//we should change this based on whether any context source related to it is requested
	private static boolean isEnablingAccessibilityServiceNeeded = true;

	public UserInteractionManager(Context context) {
		super();
		mContext = context;

		setUpContextSourceList();

		//TODO: call this in updating requested context source list
		activateAccessibilityService();

		mLatestInAppAction = new UserInteraction();

		mMinukuAccessibilityService = new MinukuAccessibilityService(this);

	}

	@Override
	protected void setUpContextSourceList() {

		Log.d(LOG_TAG, "setUpContextSourceList");

		//minuku action
		mContextSourceList.add(
				new ContextSource(
						STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_MINUKU_ACTION,
						CONTEXT_SOURCE_USER_INTERACTION_IN_MINUKU_ACTION,
						true));

		//in app action
		mContextSourceList.add(
				new ContextSource(
						STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_APP_ACTION,
						CONTEXT_SOURCE_USER_INTERACTION_IN_APP_ACTION,
						true));


		//actions on physical putton
		mContextSourceList.add(
				new ContextSource(
						STRING_CONTEXT_SOURCE_USER_INTERACTION_ON_DEVICE_ACTION,
						CONTEXT_SOURCE_USER_INTERACTION_ON_DEVICE_ACTION,
						true));



	}



	private void activateAccessibilityService() {

		Log.d(LOG_TAG, "testing logging task and requested activateAccessibilityService");
		Intent i = new Intent(mContext, MinukuAccessibilityService.class);
		mContext.startService(i);

	}


	public void saveRecordToLocalRecordPool(Record record) {

		Log.d(LOG_TAG, "[test save app records] the data of " + record.getSource() + " is " + record.getData());
		mLocalRecordPool.add(record);

	}

	public static void updateStateValues() {

	}

	public static UserInteraction getLatestInAppAction() {
		return mLatestInAppAction;
	}

	@Override
	protected void updateContextSourceListRequestStatus() {

		Log.d(LOG_TAG, "[[testing logging task and requested:]] updateContextSourceListRequestStatus" );

		boolean isRequested = false;

		for (int i=0; i<mContextSourceList.size(); i++){

			ContextSource source = mContextSourceList.get(i);

			mContextSourceList.get(i).setIsRequested(updateContextSourceRequestStatus(source));
            Log.d(LOG_TAG, "[[testing logging task and requested:]] check saving data the contextsource "
                    + source.getName() + " requested: " + source.isRequested());

			isRequested = mContextSourceList.get(i).isRequested();

			if (isRequested){

				if (!mCurrentlyRequestedContextSourceList.contains(source)){
                 Log.d(LOG_TAG, "[[testing logging task and requested]], start requesting informatoin");

					//TODO: request update
					requestUpdate(source);
				}

			}
			else {
				//TODO: check if the contextsource is currently getting update, if yes, remove update

				if (mCurrentlyRequestedContextSourceList.contains(source)){
                    Log.d(LOG_TAG, "[[testing request:]], stop requesting informatoin because it is not needed anymore");

					//TODO: request update
					removeUpdate(source);
				}

			}
		}
	}



	/**
	 * request update based on the source
	 * @param source
	 */
	private void requestUpdate(ContextSource source) {

//        Log.d(LOG_TAG, "[[test source being requested]], requestUpdate " + source.getName());

		mCurrentlyRequestedContextSourceList.add(source);

		if (source.getName().equals(STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_APP_ACTION)) {
			//we need to check whether we have permission to get app usage.
			requestInAppActionUpdate();
		}
		else if (source.getName().equals(STRING_CONTEXT_SOURCE_USER_INTERACTION_ON_DEVICE_ACTION)) {
			requestOnDeviceActionUpdate();
			//todo: get battery update
		}
		else if (source.getName().equals(STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_MINUKU_ACTION)) {
			requestInMinukuAction();
		}
	}


	/**
	 * remove update based on the source
	 * @param source
	 */
	private void removeUpdate(ContextSource source) {

		mCurrentlyRequestedContextSourceList.remove(source);

		if (source.getName().equals(STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_APP_ACTION)) {
			//we need to check whether we have permission to get app usage.
			removeInAppAction();
		}
		else if (source.getName().equals(STRING_CONTEXT_SOURCE_USER_INTERACTION_ON_DEVICE_ACTION)) {
			removeOnDeviceAction();
			//todo: get battery update
		}
		else if (source.getName().equals(STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_MINUKU_ACTION)) {
			removeInMinukuAction();
		}

	}


	/**
	 * if we want to get user interaction from any app, we should activate the accessibility service
	 */
	private void requestInAppActionUpdate() {
		Log.d(LOG_TAG, "test accessibility : accessibility service is requested, ready to set InAppAction to be TRUE in the service");

		//prompt user to start Accessibility service
		mMinukuAccessibilityService.setIsInAppActionRequested(true);
	}

	/**
	 * if we wnat to get user interaction with the device, we shold listent to the keyboard event.
	 */
	private void requestOnDeviceActionUpdate()  {
		Log.d(LOG_TAG, "testing logging task and requested on device action ");

	}

	private void requestInMinukuAction() {
		Log.d(LOG_TAG, "testing logging task and requested in minuku action ");
	}

	private void removeInAppAction() {
		Log.d(LOG_TAG, "test accessibility : accessibility service is requested, ready to set InAppAction to be FALSE in the service");

		//prompt user to stop Accessibility service
		mMinukuAccessibilityService.setIsInAppActionRequested(false);
	}

	private void removeOnDeviceAction() {
		Log.d(LOG_TAG, "testing logging task and requested remove on device action ");
	}

	private void removeInMinukuAction() {
		Log.d(LOG_TAG, "testing logging task and requested remove in minuku action ");
	}


	public static void setActionOnDevice(UserInteraction actionOnDevice) {

		Log.d(LOG_TAG, " setLatestOnDeviceAction " + mActionOnDevice.toString());

		UserInteractionManager.mActionOnDevice = actionOnDevice;
	}


	public void setLatestInAppAction(UserInteraction latestInAppAction) {

		/**if InAppAction is not requested, we should not save it. Ideally we should prompt user to stop
		 * the accsessibility service. But in case they do not do it, we should not save the data
		 * (It's impossible to programmatically stop the Accessibility service)*/

		Log.d(LOG_TAG, " test accessibility setLatestInAppAction " + latestInAppAction.toString());

		mLatestInAppAction = latestInAppAction;

		/** store values into a Record so that we can store them in the local database **/
		Record record = new Record();
		record.setTimestamp(ContextManager.getCurrentTimeInMillis());
		record.setSource(STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_APP_ACTION);

		/** create data in a JSON Object. Each CotnextSource will have different formats.
		 * So we need each ContextSourceMAnager to implement this part**/
		JSONObject data = new JSONObject();

		try {
			data.put(RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_ACTION, latestInAppAction.getAction());
			data.put(RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_TIME, latestInAppAction.getActionTime());
			data.put(RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_PACKAGE, latestInAppAction.getPackageName());
			data.put(RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_TARGET, latestInAppAction.getTarget());
			data.put(RECORD_DATA_PROPERTY_USER_INTERACTION_IN_APP_ACTION_EXTRA, latestInAppAction.getExtra());
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		/*** Set data to Record **/
		record.setData(data);

		/** Save Record**/
		saveRecordToLocalRecordPool(record);

	}



	public static int getContextSourceTypeFromName(String sourceName) {

		switch (sourceName){

			case STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_APP_ACTION:
				return CONTEXT_SOURCE_USER_INTERACTION_IN_APP_ACTION;
			case STRING_CONTEXT_SOURCE_USER_INTERACTION_ON_DEVICE_ACTION:
				return CONTEXT_SOURCE_USER_INTERACTION_ON_DEVICE_ACTION;
			case STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_MINUKU_ACTION:
				return CONTEXT_SOURCE_USER_INTERACTION_IN_MINUKU_ACTION;
			//the default is most probable activities
			default:
				return -1;
		}
	}

	/**
	 * this function should return a list of database table names for its contextsource. Must implement it
	 * in order to create tables
	 * @return
	 */
	@Override
	public ArrayList<String> getAllDatabaseTableNames () {
		ArrayList<String> tablenames = new ArrayList<String>();

		tablenames.add(RECORD_TABLE_NAME_IN_APP_ACTION);
		tablenames.add(RECORD_TABLE_NAME_IN_MINUKU_ACTION);
		tablenames.add(RECORD_TABLE_NAME_ON_DEVICE_ACTION);

		return tablenames;
	}


	/**Database table name should be defined by each ContextStateManager. So each CSM should overwrite this**/
	public static String getDatabaseTableNameBySourceName (String sourceName) {

		if (sourceName.equals(STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_APP_ACTION))
			return RECORD_TABLE_NAME_IN_APP_ACTION;
		else if (sourceName.equals(STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_MINUKU_ACTION))
			return RECORD_TABLE_NAME_IN_MINUKU_ACTION;
		else if (sourceName.equals(STRING_CONTEXT_SOURCE_USER_INTERACTION_ON_DEVICE_ACTION))
			return RECORD_TABLE_NAME_ON_DEVICE_ACTION;
		else
			return null;

	}


	public static String getContextSourceNameFromType(int sourceType) {

		switch (sourceType){

			case CONTEXT_SOURCE_USER_INTERACTION_IN_APP_ACTION:
				return STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_APP_ACTION;
			case CONTEXT_SOURCE_USER_INTERACTION_ON_DEVICE_ACTION:
				return STRING_CONTEXT_SOURCE_USER_INTERACTION_ON_DEVICE_ACTION;
			case CONTEXT_SOURCE_USER_INTERACTION_IN_MINUKU_ACTION:
				return STRING_CONTEXT_SOURCE_USER_INTERACTION_IN_MINUKU_ACTION;
			//the default is most probable activities
			default:
				return "NA";
		}
	}

}
