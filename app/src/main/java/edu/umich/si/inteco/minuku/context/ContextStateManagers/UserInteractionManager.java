package edu.umich.si.inteco.minuku.context.ContextStateManagers;

import android.content.Context;

public class UserInteractionManager extends ContextStateManager {

	private Context mContext;


	public UserInteractionManager(Context context) {
		super();
		mContext = context;
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
