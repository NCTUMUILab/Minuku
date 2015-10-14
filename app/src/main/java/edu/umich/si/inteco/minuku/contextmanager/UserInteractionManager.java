package edu.umich.si.inteco.minuku.contextmanager;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.umich.si.inteco.minuku.model.record.PhoneActivityRecord;
import edu.umich.si.inteco.minuku.services.MinukuMainService;

public class UserInteractionManager extends ContextStateManager {

	private Context mContext;


	public UserInteractionManager(Context context) {
		super();
		mContext = context;
	}


	@Override
	public void stateChanged() {

	}

	@Override
	public void saveRecordsInLocalRecordPool() {

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
