package edu.umich.si.inteco.minuku.context.ContextStateManagers;

import android.content.Context;
import android.content.Intent;

import edu.umich.si.inteco.minuku.services.MinukuAccessibilityService;

public class UserInteractionManager extends ContextStateManager {

	private Context mContext;
	private static final String LOG_TAG = "UserInteractionManager";

	//we should change this based on whether any context source related to it is requested
	private static boolean isEnablingAccessibilityServiceNeeded = true;

	public UserInteractionManager(Context context) {
		super();
		mContext = context;

		//TODO: call this in updating requested context source list
		activateAccessibilityService();


	}




	private void activateAccessibilityService() {

		Intent i = new Intent(mContext, MinukuAccessibilityService.class);
		mContext.startService(i);

	}

	public static void updateStateValues() {

	}



	public static int getContextSourceTypeFromName(String sourceName) {

//		switch (sourceName){
//
//			case STRING_CONTEXT_SOURCE_LOCATION:
//				return CONTEXT_SOURCE_LOCATION;
//			case STRING_CONTEXT_SOURCE_GEOFENCE:
//				return CONTEXT_SOURCE_GEOFENCE;
//			//the default is most probable activities
//			default:
//				return CONTEXT_SOURCE_LOCATION;
//		}

		return -1;
	}

	public static String getContextSourceNameFromType(int sourceType) {

//		switch (sourceType){
//
//			case CONTEXT_SOURCE_LOCATION:
//				return STRING_CONTEXT_SOURCE_LOCATION;
//			case CONTEXT_SOURCE_GEOFENCE:
//				return STRING_CONTEXT_SOURCE_GEOFENCE;
//			default:
//				return STRING_CONTEXT_SOURCE_LOCATION;
//
//		}

		return "NA";
	}

}
