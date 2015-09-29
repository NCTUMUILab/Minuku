package edu.umich.si.inteco.minuku.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;

import java.util.List;

import edu.umich.si.inteco.minuku.contextmanager.ContextExtractor;
import edu.umich.si.inteco.minuku.services.CaptureProbeService;

public class AppManager {

	/** Tag for logging. */
    private static final String LOG_TAG = "AppsManager";
    
    
	private Context mContext;
	private static ActivityManager mActivityManager;
	private static Handler mMainThread;
	private static final String SCREEN_OFF = "Screen_off";
	
	/**activity related*/
	private String previousActivity;
	
	public AppManager(Context context){
		
		mContext = context;
		mActivityManager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
		previousActivity = null;
		
	}
	
	public static void runMonitoringAppThread (){

		//Log.d(LOG_TAG, "[runMonitoringAppThread ] run the thread of monitoring apps");
		
		mMainThread = new Handler();

		Runnable runnable = new Runnable() {
		    @Override 
		    public void run() {
		    	
		    	getForegroundActivity();
		    	
		    	mMainThread.postDelayed(this, CaptureProbeService.DEFAULT_APP_MONITOR_RATE_INTERVAL);
		    	
		    }
		  };	
		  
		  /**start repeatedly store the extracted contextual information into Record objects**/
		  mMainThread.post(runnable); 

	}
	public static void getForegroundActivity(){		

		String curRunningForegrndActivity="";
		String curRunningForegrndPackNamge="";
		/** get the info from the currently foreground running activity **/
		List<ActivityManager.RunningTaskInfo> taskInfo=null;
		
		//get the latest (or currently running) foreground activity and package name
		if ( mActivityManager!=null){

			taskInfo = mActivityManager.getRunningTasks(1);
			
			//if the screen is on, get the information of the currently running app
			if (ContextExtractor.getPowerManager().isScreenOn()){
				curRunningForegrndActivity = taskInfo.get(0).topActivity.getClassName();
				curRunningForegrndPackNamge = taskInfo.get(0).topActivity.getPackageName();
				//Log.d(LOG_TAG, "[getForegroundActivity] the current running package is " + curRunningForegrndPackNamge + " and the activity is "
			//			+ curRunningForegrndActivity);
			}
			
			//if the screen is off, says the screen is off. 
			else {
				
				curRunningForegrndActivity = SCREEN_OFF;
				curRunningForegrndPackNamge = SCREEN_OFF;
				//Log.d(LOG_TAG, "[getForegroundActivity] the current running package is " + curRunningForegrndPackNamge + " and the activity is "
				//		+ curRunningForegrndActivity);
				
			}
			
			
			//store the running activity and its package name in the Context Extractor
			if(taskInfo!=null){			
				ContextExtractor.setCurrentForegroundActivityAndPackage(curRunningForegrndActivity, curRunningForegrndPackNamge);
			}
			
		}

	}
	
}
