package edu.umich.si.inteco.minuku.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.activities.AnnotateActivity;
import edu.umich.si.inteco.minuku.context.ContextManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.ActivityRecognitionManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.LocationManager;
import edu.umich.si.inteco.minuku.context.ContextStateManagers.TransportationModeManager;
import edu.umich.si.inteco.minuku.model.Checkpoint;
import edu.umich.si.inteco.minuku.model.LoggingTask;
import edu.umich.si.inteco.minuku.model.Task;
import edu.umich.si.inteco.minuku.model.actions.SavingRecordAction;
import edu.umich.si.inteco.minuku.services.MinukuMainService;
import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.ConfigurationManager;
import edu.umich.si.inteco.minuku.util.DatabaseNameManager;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.RecordingAndAnnotateManager;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;
import edu.umich.si.inteco.minuku.util.TaskManager;

/**
 * Created by Armuro on 7/13/14.
 */
public class CheckinSectionFragment extends Fragment{

    private static final String LOG_TAG = "RecordSectionFragment";

    private Chronometer chronometer;
    private Button checkinButton;
    private Button stopButton;
    private LinearLayout checkpointLayout;
    private static SavingRecordAction mLastUserInitiatedSavingRecordingAction;
    private ArrayList<String> CheckpointTimeStrings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //store information of checkpoint views
        CheckpointTimeStrings = new ArrayList<String>();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_checkin, container, false);


/*
            if (MinukuMainService.getCentralChrometer()==null)
                MinukuMainService.setCentralChrometer((Chronometer) rootView.findViewById(R.id.recording_chronometer));
                */

        chronometer = (Chronometer) rootView.findViewById(R.id.recording_chronometer);
        checkinButton = (Button) rootView.findViewById(R.id.checkin_Button);
        stopButton = (Button) rootView.findViewById(R.id.stop_Button);
        checkpointLayout  = (LinearLayout) rootView.findViewById(R.id.checkin_layout);

        Log.d(LOG_TAG, "back to creatView the base is " + MinukuMainService.getBaseForChronometer() + " there are "
                + CheckpointTimeStrings.size() + " in the arraylist");

        //if there are existing views in the arraylist, we should add it in the layout.

        if (CheckpointTimeStrings.size() >0 ){

            for (int i=0; i<CheckpointTimeStrings.size(); i++){

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                );

                layoutParams.gravity = Gravity.LEFT;
                TextView tv = new TextView(getContext());
                tv.setPadding(20, 0, 0, 0);// in pixels (left, top, right, bottom)
                tv.setTextSize(18);
                tv.setTextColor(Color.YELLOW);
                tv.setLayoutParams(layoutParams);

                //we add the current elapse time
                int numOfCheckpoints = checkpointLayout.getChildCount();

                tv.setText(numOfCheckpoints + ". " + CheckpointTimeStrings.get(i));

                checkpointLayout.addView(tv);

            }
        }

        //the textview is based on the task of which the recording is for. By default there should be a list of task that signs up for using this interface ( the recording function
        //can be used by multiple tasks, and the user would choose which task the current recording is for.
        //For the labeling study, we change the textview based on which condition they are in

        Log.d(LOG_TAG, "back to creatView the base is " + MinukuMainService.getBaseForChronometer() + " there are "
         + checkpointLayout.getChildCount() + " subviews in  checkpointLayout");


        //if the chrometer was running when we leave the fragment, after coming back we should recover it
        if (MinukuMainService.isCentralChrometerRunning()) {

            long time = SystemClock.elapsedRealtime() -  MinukuMainService.getBaseForChronometer();
            int h   = (int)(time/3600000);
            int m = (int)(time - h*3600000)/60000;
            int s= (int)(time - h*3600000- m*60000)/1000 ;
            String hh = h < 10 ? "0"+h: h+"";
            String mm = m < 10 ? "0"+m: m+"";
            String ss = s < 10 ? "0"+s: s+"";

            chronometer.setText(hh + ":" + mm + ":" + ss);
            chronometer.start();
            MinukuMainService.setCentralChrometerRunning(true);

            checkinButton.setText(getString(R.string.checkin_btn));
        }
        else {

            chronometer.setText(MinukuMainService.getCentralChrometerText());

        }


        //setup component
        chronometer.setOnChronometerTickListener(

                new Chronometer.OnChronometerTickListener(){
                    @Override
                    public void onChronometerTick(Chronometer cArg) {
                        long time = SystemClock.elapsedRealtime() -  MinukuMainService.getBaseForChronometer();
                        int h   = (int)(time/3600000);
                        int m = (int)(time - h*3600000)/60000;
                        int s= (int)(time - h*3600000- m*60000)/1000 ;
                        String hh = h < 10 ? "0"+h: h+"";
                        String mm = m < 10 ? "0"+m: m+"";
                        String ss = s < 10 ? "0"+s: s+"";
                        cArg.setText(hh+":"+mm+":"+ss);
                    }

                });


        // clicking on the start button will start or resume the recording
        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(LOG_TAG, "[checkin]" + checkinButton.getText().toString());


                /***create checkpoint object**/
                Checkpoint curCheckpoint = new Checkpoint(
                        LocationManager.getCurrentLocation(),
                        ActivityRecognitionManager.getProbableActivities(),
                        TransportationModeManager.getConfirmedActvitiyString(),
                        TransportationModeManager.getCurrentStateString(),
                        ContextManager.getCurrentTimeInMillis()
                        );


                //When the button is shown "START" we start the stopwatch
                if (checkinButton.getText().toString().equals(getString(R.string.start_btn))){

                    //the first checkpoint (when clicking on the START button) should be set as the previous checkpoint
                    MinukuMainService.setPreviousCheckpoint(curCheckpoint);

                    Log.d(LOG_TAG, "[checkin] user clicking on the START action");

                    MinukuMainService.setBaseForChronometer(SystemClock.elapsedRealtime());

                    //start recording and start the stopwatch
                    chronometer.setBase(MinukuMainService.getBaseForChronometer());
                    chronometer.start();
                    MinukuMainService.setCentralChrometerRunning(true);
                    MinukuMainService.setCentralChrometerPaused(false);

                    //change the text of the button to checkpoint
                    checkinButton.setText(getString(R.string.checkin_btn));


                    //when clicking on the recording action, execute the saveRecordAction
                    SavingRecordAction action = new SavingRecordAction(
                            ActionManager.USER_INITIATED_RECORDING_ACTION_ID,
                            ActionManager.USER_START_RECORDING_ACTION_NAME,
                            ActionManager.ACTION_TYPE_SAVING_RECORD,
                            ActionManager.ACTION_EXECUTION_STYLE_ONETIME, Constants.LABELING_STUDY_ID);

                    //an user-initiated recoring should allow users to annotate in process
                    action.setAllowAnnotationInProcess(true);


                    //set Loggingtask to the SavingRecordAction;
                    action.setLoggingTasks(ContextManager.getBackgroundLoggingSetting().getLoggingTasks());
                    Log.d(LOG_TAG, "row: the manual saving action has the logging tasks:" + action.getLoggingTasks());



                    mLastUserInitiatedSavingRecordingAction = action;

                    //user initiated recording should be a continuous action; if we don't set this, it will just be a one-time action.
                    action.setContinuous(true);


                    //start the saving record action
                    ActionManager.startAction(action);
                    Log.d(LOG_TAG, "[hybrid sensing] user clicking on the start action, start recording action" + action.getId() + " with session "  + action.getSessionId());



                    //Log user action
                    LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                            LogManager.LOG_TAG_USER_CLICKING,
                            "User Click:\t" + "startRecording" + "\t" + "RecordingTab");

                    //Log system
                    LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                            LogManager.LOG_TAG_USER_CLICKING,
                            "User Click:\t" + "startRecording" + "\t" + "RecordingTab");


                }



                //if the button is checkin, add check point times
                else if (checkinButton.getText().toString().equals(getString(R.string.checkin_btn))) {


                    /***1. create textView**/
                    Log.d(LOG_TAG, "[checkin] user clicking on the CHECK IN action");

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    );

                    layoutParams.gravity = Gravity.LEFT;
                    TextView tv = new TextView(getContext());
                    tv.setPadding(20, 0, 0, 0);// in pixels (left, top, right, bottom)
                    tv.setTextSize(18);
                    tv.setTextColor(Color.YELLOW);
                    tv.setLayoutParams(layoutParams);

                    //we add the current elapse time
                    int numOfCheckpoints = checkpointLayout.getChildCount();

                    tv.setText(numOfCheckpoints + ". " + ContextManager.getCurrentTimeStringNoTimezone());

                    checkpointLayout.addView(tv);

                    //save the textView so that we can retrieve it when the screen is rotated
                    //the CreatContentView will be called again.
                    CheckpointTimeStrings.add(tv.getText().toString());

                    /***2. create checkpoint and log data of the two checkpoints **/

                try{
//                    Checkpoint previousCheckpoint = MinukuMainService.getPreviousCheckpoint();
//                    String betweenCheckpointContentMessage =
//                            ScheduleAndSampleManager.getTimeString(previousCheckpoint.getTimestamp()) + "\t" +
//                                    previousCheckpoint.getTimestamp() + "\t" +
//                                    ScheduleAndSampleManager.getTimeString(curCheckpoint.getTimestamp()) + "\t" +
//                                    curCheckpoint.getTimestamp()+ "\t" +
//                                    previousCheckpoint.getLocation().toString() + "\t" +
//                                    curCheckpoint.getLocation().toString();

                    Checkpoint previousCheckpoint = MinukuMainService.getPreviousCheckpoint();
                    
                    String betweenCheckpointContentMessage =
                            ScheduleAndSampleManager.getTimeString(previousCheckpoint.getTimestamp()) + "\t" +
                                    ScheduleAndSampleManager.getTimeString(curCheckpoint.getTimestamp()) + "\t" +
                                    "Ground Truth";



                    //for each checkpoint, generate a "previous checkpoint time transportation - current checkpoint time  ground truth"
                    LogManager.log(LogManager.LOG_TYPE_CHECKPOINT_LOG,
                            LogManager.LOG_TAG_USER_CHECKIN,
                            betweenCheckpointContentMessage);

                    Log.d(LOG_TAG, "check checkpoint message" + betweenCheckpointContentMessage);



                }catch(Exception e){

                    }



                    //after we log, we make replace Minuku's previousCheckpoint with the curCheckpoint
                    MinukuMainService.setPreviousCheckpoint(curCheckpoint);


                    /** we need to create a checkpoint annotation to the current recording session**/


                }


//                /*we create check in log whenever the button is pressed, regardless is is START or CHECK IN**/
//
//                try {
//                    String checkpointContentMessage=
//
//                            LocationManager.getCurrentLocation().getLatitude() + "," +
//                                    LocationManager.getCurrentLocation().getLongitude() + "," +
//                                    LocationManager.getCurrentLocation().getAccuracy() + "\t" +
//                                    TransportationModeManager.getConfirmedActvitiyString() + "\t" +
//                                    "FSM:" + TransportationModeManager.getCurrentStateString() + "\t" +
//                                    ActivityRecognitionManager.getProbableActivities().toString() + "\t" ;
//
//                    /**for generate check-point file**/
//                    LogManager.log(LogManager.LOG_TYPE_CHECKPOINT_LOG,
//                            LogManager.LOG_TAG_USER_CHECKIN,
//                            checkpointContentMessage);
//                }catch(Exception e){
//
//                }
//


                //Log user action
                LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                        LogManager.LOG_TAG_USER_CLICKING,
                        "User Click:\t" + "checkpoint" );

                //Log system
                LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                        LogManager.LOG_TAG_USER_CLICKING,
                        "User Click:\t" + "checkpoint");


            }
        });



        // clicking on the stop button will start the recording

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                /***create checkpoint object**/
                Checkpoint curCheckpoint = new Checkpoint(
                        LocationManager.getCurrentLocation(),
                        ActivityRecognitionManager.getProbableActivities(),
                        TransportationModeManager.getConfirmedActvitiyString(),
                        TransportationModeManager.getCurrentStateString(),
                        ContextManager.getCurrentTimeInMillis()
                );


                //stop recording and stop the stopwatch
                chronometer.stop();
                MinukuMainService.setCentralChrometerRunning(false);
                MinukuMainService.setCentralChrometerPaused(false);
                //reset
                chronometer.setText("00:00:00");
                //rememeber the text of the chrometer
                MinukuMainService.setCentralChrometerText(chronometer.getText().toString());


                //remove all checkpoint views in the Layout
                if (checkpointLayout.getChildCount() > 0){
                    checkpointLayout.removeAllViews();
                    CheckpointTimeStrings.clear();
                }


                /**stop the user-initiated recording action **/

                //find the action from the runningAction List
                SavingRecordAction action  = (SavingRecordAction) ActionManager.getRunningAction(ActionManager.USER_INITIATED_RECORDING_ACTION_ID);

                //stop the recording action
                if (action!=null){
                    ActionManager.stopAction(action);
                    Log.d(LOG_TAG, "[participatory sensing] user clicking on the stop action, stop recording action " + action.getId() + " with session "  + action.getSessionId());
                }

                //changing the labee of the start button back to START
                checkinButton.setText(getString(R.string.start_btn));





                /***2. create checkpoint and log data of the two checkpoints **/
                Checkpoint previousCheckpoint = MinukuMainService.getPreviousCheckpoint();

                try {
//                    String betweenCheckpointContentMessage =
//                            ScheduleAndSampleManager.getTimeString(previousCheckpoint.getTimestamp()) + "\t" +
//                                    previousCheckpoint.getTimestamp() + "\t" +
//                                    ScheduleAndSampleManager.getTimeString(curCheckpoint.getTimestamp()) + "\t" +
//                                    curCheckpoint.getTimestamp()+ "\t" +
//                                    previousCheckpoint.getLocation().toString() + "\t" +
//                                    curCheckpoint.getLocation().toString();
//
//                    Log.d(LOG_TAG, "betweencheckpoint: " + betweenCheckpointContentMessage);
//
//                    /**for generate between check-point file**/
//                    LogManager.log(LogManager.LOG_TYPE_BETWEEN_CHECKPOINTS_LOG,
//                            LogManager.LOG_TAG_USER_CHECKIN,
//                            betweenCheckpointContentMessage);

                    String betweenCheckpointContentMessage =
                            ScheduleAndSampleManager.getTimeString(previousCheckpoint.getTimestamp()) + "\t" +
                                    ScheduleAndSampleManager.getTimeString(curCheckpoint.getTimestamp()) + "\t" +
                                    "Ground Truth";



                    //for each checkpoint, generate a "previous checkpoint time transportation - current checkpoint time  ground truth"
                    LogManager.log(LogManager.LOG_TYPE_CHECKPOINT_LOG,
                            LogManager.LOG_TAG_USER_CHECKIN,
                            betweenCheckpointContentMessage);


                }catch(Exception e){

                }



//                //after we log, we make replace Minuku's previousCheckpoint with the curCheckpoint
//                MinukuMainService.setPreviousCheckpoint(curCheckpoint);
//
//
//                /*we create check in log whenever the button is pressed, regardless is is START or CHECK IN**/
//                String checkinContentMessage="NA";
//
//
//                if (ActivityRecognitionManager.getProbableActivities()!=null &&
//                        LocationManager.getCurrentLocation()!=null ){
//
//                    try {
//                        checkinContentMessage=
//                                LocationManager.getCurrentLocation().getLatitude() + "," +
//                                        LocationManager.getCurrentLocation().getLongitude() + "," +
//                                        LocationManager.getCurrentLocation().getAccuracy() + "\t" +
//                                        TransportationModeManager.getConfirmedActvitiyString() + "\t" +
//                                        "FSM:" + TransportationModeManager.getCurrentStateString() + "\t" +
//                                        ActivityRecognitionManager.getProbableActivities().toString() + "\t" ;
//
//                        LogManager.log(LogManager.LOG_TYPE_CHECKPOINT_LOG,
//                                LogManager.LOG_TAG_USER_CHECKIN,
//                                checkinContentMessage);
//
//                    }catch(Exception e){
//
//                    }
//
//                }




                //Log user action
                LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                        LogManager.LOG_TAG_USER_CLICKING,
                        "User Click:\t" + "stoptRecording"+ "\t" + "RecordingTab");


                LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                        LogManager.LOG_TAG_USER_CLICKING,
                        "User Click:\t" + "stoptRecording"+ "\t" + "RecordingTab");

                //TODO: remove location udpate after hitting the stop button
                //ContextExtractor.getLocationManager().removeLocationUpdate();

            }
        });



        return rootView;



    }


    public void startAnnotateActivity(int sessionId){

        Bundle bundle = new Bundle();
        bundle.putInt(DatabaseNameManager.COL_SESSION_ID, sessionId);
        //if we enter the list recording interface by clicking the labeling button, the review mode should be "NA", i.e. users are returning to the recording fragment
        bundle.putString(ConfigurationManager.ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING, RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_NONE);

        Intent intent = new Intent(getActivity(), AnnotateActivity.class);
        intent.putExtras(bundle);

        startActivity(intent);


    }

    private static Task getTaskFromRecordingTaskView(String selectedTask) {

        //the selectedTask is its name

        Log.d(LOG_TAG, "the selected Task text is " + selectedTask);

        Task task = TaskManager.getTaskByName(selectedTask);

        if (task!=null){
            return task;
        }
        else
            return null;


    }

}
