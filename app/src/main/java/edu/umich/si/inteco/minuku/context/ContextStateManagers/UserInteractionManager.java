package edu.umich.si.inteco.minuku.context.ContextStateManagers;

import android.content.Context;

public class UserInteractionManager extends ContextStateManager {

	private Context mContext;


	public UserInteractionManager(Context context) {
		super();
		mContext = context;
	}


	public static int getContextSourceTypeFromName(String sourceName) {
		return -1;
	}

	public static String getContextSourceNameFromType(int sourceType) {

				return "NA";
	}

	public static void updateStateValues() {

	}
}
