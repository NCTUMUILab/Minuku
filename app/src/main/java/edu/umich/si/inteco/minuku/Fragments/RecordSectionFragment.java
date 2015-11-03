package edu.umich.si.inteco.minuku.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.activities.AnnotateActivity;
import edu.umich.si.inteco.minuku.model.Task;
import edu.umich.si.inteco.minuku.model.actions.SavingRecordAction;
import edu.umich.si.inteco.minuku.services.MinukuMainService;
import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.ConfigurationManager;
import edu.umich.si.inteco.minuku.util.DatabaseNameManager;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.RecordingAndAnnotateManager;
import edu.umich.si.inteco.minuku.util.TaskManager;

/**
 * Created by Armuro on 7/13/14.
 */
public class RecordSectionFragment extends Fragment{

    private static final String LOG_TAG = "RecordSectionFragment";

    private TextView currentRecordingTaskTextView;
    private Chronometer chronometer;
    private Button startButton;
    private Button stopButton;
    private Button labelButton;
    private static SavingRecordAction mLastUserInitiatedSavingRecordingAction;

    @Override
    public void onResume() {
        super.onResume();
/*
            Log.d(LOG_TAG, "the base is " + MinukuMainService.getCentralChrometer().getBase());

            long t= SystemClock.elapsedRealtime() - MinukuMainService.getCentralChrometer().getBase();

            int h   = (int)(t/3600000);
            int m = (int)(t - h*3600000)/60000;
            int s= (int)(t - h*3600000- m*60000)/1000 ;
            String hh = h < 10 ? "0"+h: h+"";
            String mm = m < 10 ? "0"+m: m+"";
            String ss = s < 10 ? "0"+s: s+"";
            MinukuMainService.getCentralChrometer().setText(hh+":"+mm+":"+ss);
*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_record, container, false);

/*
            if (MinukuMainService.getCentralChrometer()==null)
                MinukuMainService.setCentralChrometer((Chronometer) rootView.findViewById(R.id.recording_chronometer));
                */

        chronometer = (Chronometer) rootView.findViewById(R.id.recording_chronometer);
        startButton = (Button) rootView.findViewById(R.id.start_recording_Button);
        stopButton = (Button) rootView.findViewById(R.id.stop_recording_Button);
        labelButton = (Button) rootView.findViewById(R.id.add_details_Button);
        currentRecordingTaskTextView = (TextView) rootView.findViewById(R.id.recording_task_textview);


        //the textview is based on the task of which the recording is for. By default there should be a list of task that signs up for using this interface ( the recording function
        //can be used by multiple tasks, and the user would choose which task the current recording is for.
        //For the labeling study, we change the textview based on which condition they are in

        Log.d(LOG_TAG, "back to creatView the base is " + MinukuMainService.getBaseForChronometer());


        //if the chrometer was running when we leave the fragment, after coming back we should recover it
        if (MinukuMainService.isCentralChrometerRunning()) {

            long time = SystemClock.elapsedRealtime() -  MinukuMainService.getBaseForChronometer();
            int h   = (int)(time/3600000);
            int m = (int)(time - h*3600000)/60000;
            int s= (int)(time - h*3600000- m*60000)/1000 ;
            String hh = h < 10 ? "0"+h: h+"";
            String mm = m < 10 ? "0"+m: m+"";
            String ss = s < 10 ? "0"+s: s+"";

            chronometer.setText(hh+":"+mm+":"+ss);
            chronometer.start();
            MinukuMainService.setCentralChrometerRunning(true);

            startButton.setText("PAUSE");

        }
        else {

            chronometer.setText(MinukuMainService.getCentralChrometerText());
            //if it is not running, check if it's paused
            if (MinukuMainService.isCentralChrometerPaused())
                startButton.setText("RESUME");

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

        //set the text of recording task
        currentRecordingTaskTextView.setText(Constants.CURRENT_STUDY_CONDITION);

        Log.d(LOG_TAG, "[participatory sensing] user clicking on the start action, start recording action"  );


        // clicking on the start button will start or resume the recording
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(LOG_TAG, "[participatory sensing] user clicking on a action" );


                Log.d(LOG_TAG, "[participatory sensing]" + startButton.getText().toString());

                    if (startButton.getText().toString().equals("START")){

                        Log.d(LOG_TAG, "[participatory sensing] user clicking on the start action, start recording action" );

                        MinukuMainService.setBaseForChronometer(SystemClock.elapsedRealtime());

                        //start recording and start the stopwatch
                        chronometer.setBase( MinukuMainService.getBaseForChronometer());
                        chronometer.start();
                        MinukuMainService.setCentralChrometerRunning(true);
                        MinukuMainService.setCentralChrometerPaused(false);


                        //Log.d(LOG_TAG, " MinukuMainService is runnung? " + MinukuMainService.isCentralChrometerRunning() + " start! the base is " + MinukuMainService.getBaseForChronometer());

                        //when clicking on the recording action, execute the saveRecordAction
                        SavingRecordAction action = new SavingRecordAction(
                                ActionManager.USER_INITIATED_RECORDING_ACTION_ID,
                                ActionManager.USER_START_RECORDING_ACTION_NAME,
                                ActionManager.ACTION_TYPE_SAVING_RECORD,
                                ActionManager.ACTION_EXECUTION_STYLE_ONETIME, Constants.LABELING_STUDY_ID);

                        //an user-initiated recoring should allow users to annotate in process
                        action.setAllowAnnotationInProcess(true);

                        mLastUserInitiatedSavingRecordingAction = action;

                        //user initiated recording should be a continuous action; if we don't set this, it will just be a one-time action.
                        action.setContinuous(true);

                        //specify which task this recording is for
                        Task task = getTaskFromRecordingTaskView(currentRecordingTaskTextView.getText().toString() );
//                            	Log.d(LOG_TAG, "[participatory sensing] the recording is for task: " + task.getName() + " " + task.getId());

                        if (task!=null)
                            action.setTaskId((int) task.getId());
                        else
                            action.setTaskId(-1);

                        //start the saving record action
                        ActionManager.startAction(action);
                        Log.d(LOG_TAG, "[participatory sensing] user clicking on the start action, start recording action" + action.getId() + " with session "  + action.getSessionId());

                        //changing the labee of the start button to PAUSE
                        startButton.setText("PAUSE");


                        //Log user action
                        LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                                LogManager.LOG_TAG_USER_CLICKING,
                                "User Click:\t" + "startRecording" + "\t" + "RecordingTab");

                        //Log system
                        LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                                LogManager.LOG_TAG_USER_CLICKING,
                                "User Click:\t" + "startRecording" + "\t" + "RecordingTab");


                        //TODO: request location udpate after hitting the start button
                        //ContextExtractor.getLocationManager().requestLocationUpdate();

                    }

                    //if the user clicks on button labeled "PAUSE", we should pause the watch and the recording.
                    else if (startButton.getText().toString().equals("PAUSE")){


                        //stop the watch
                        chronometer.stop();
                        MinukuMainService.setCentralChrometerRunning(false);
                        MinukuMainService.setCentralChrometerPaused(true);
                        //rememeber the text of the chrometer
                        MinukuMainService.setCentralChrometerText(chronometer.getText().toString());

                        //remember the time
                        MinukuMainService.setTimeWhenStopped(SystemClock.elapsedRealtime());


                        Log.d(LOG_TAG, "pause at time" + SystemClock.elapsedRealtime() + " remember text " + MinukuMainService.getCentralChrometerText() + " and time " + MinukuMainService.getTimeWhenStopped());


                        SavingRecordAction action  = (SavingRecordAction) ActionManager.getRunningAction(ActionManager.USER_INITIATED_RECORDING_ACTION_ID);

                        //pause the saving record action
                        if (action!=null)
                            ActionManager.pauseAction(action);

                        //changing the labee of the start button to RESUME
                        startButton.setText("RESUME");

                        //Log user action
                        LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                                LogManager.LOG_TAG_USER_CLICKING,
                                "User Click:\t" + "pauseRecording"+ "\t" + "RecordingTab");

                        //Log system
                        LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                                LogManager.LOG_TAG_USER_CLICKING,
                                "User Click:\t" + "pauseRecording" + "\t" + "RecordingTab");

                    }


                    //if the user clicks on button labeled "PAUSE", we should pause the watch and the recording.
                    else if (startButton.getText().toString().equals("RESUME")){

                        //new base = original base + (the time of resuming - the time of pausing)
                        MinukuMainService.setBaseForChronometer(MinukuMainService.getBaseForChronometer() + (SystemClock.elapsedRealtime() - MinukuMainService.getTimeWhenStopped()));
                        //stop the watch
                        chronometer.setBase( MinukuMainService.getBaseForChronometer());
                        chronometer.start();
                        MinukuMainService.setCentralChrometerRunning(true);
                        MinukuMainService.setCentralChrometerPaused(false);

                        Log.d(LOG_TAG, "resume at time" + SystemClock.elapsedRealtime() + " need to add base " + ( SystemClock.elapsedRealtime() - MinukuMainService.getTimeWhenStopped() )  );


                        Log.d(LOG_TAG, " MinukuMainService is runnung? " + MinukuMainService.isCentralChrometerRunning() + " resume the base is " + MinukuMainService.getBaseForChronometer());


                        SavingRecordAction action  = (SavingRecordAction) ActionManager.getRunningAction(ActionManager.USER_INITIATED_RECORDING_ACTION_ID);

                        //pause the saving record action
                        if (action!=null)
                            ActionManager.resumeAction(action);


                        //changing the labee of the start button to PAUSE
                        startButton.setText("PAUSE");

                        //Log user action
                        LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                                LogManager.LOG_TAG_USER_CLICKING,
                                "User Click:\t" + "resumeRecording"+ "\t" + "RecordingTab");

                        //Log user action
                        LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                                LogManager.LOG_TAG_USER_CLICKING,
                                "User Click:\t" + "resumeRecording"+ "\t" + "RecordingTab");

                    }

            }
        });



        // clicking on the stop button will start the recording

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //stop recording and stop the stopwatch
                chronometer.stop();
                MinukuMainService.setCentralChrometerRunning(false);
                MinukuMainService.setCentralChrometerPaused(false);
                //reset
                chronometer.setText("00:00:00");
                //rememeber the text of the chrometer
                MinukuMainService.setCentralChrometerText(chronometer.getText().toString());


                /**stop the user-initiated recording action **/

                //find the action from the runningAction List
                SavingRecordAction action  = (SavingRecordAction) ActionManager.getRunningAction(ActionManager.USER_INITIATED_RECORDING_ACTION_ID);

                //stop the recording action
                if (action!=null){
                    ActionManager.stopAction(action);
                    Log.d(LOG_TAG, "[participatory sensing] user clicking on the stop action, stop recording action " + action.getId() + " with session "  + action.getSessionId());
                }

                //changing the labee of the start button back to START
                startButton.setText("START");

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


        /***When user clicks on Label button they will enter annotations to the recordin that is initiated by themselves*
         * To add annotaiton we need to know which session the recording is. So we need to first fine the aciton that gets the session id**/

        labelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mLastUserInitiatedSavingRecordingAction!=null){

                    Log.d(LOG_TAG, "[participatory sensing] user clicking on the label action, going to annotate " + mLastUserInitiatedSavingRecordingAction.getId() + " with session "  + mLastUserInitiatedSavingRecordingAction.getSessionId());

                    int sessionId = mLastUserInitiatedSavingRecordingAction.getSessionId();

                    startAnnotateActivity(sessionId);

                    //Log user action
                    LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                            LogManager.LOG_TAG_USER_CLICKING,
                            "User Click:\t" + "annotateRecording"+ "\t" + "RecordingTab");


                    LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                            LogManager.LOG_TAG_USER_CLICKING,
                            "User Click:\t" + "annotateRecording"+ "\t" + "RecordingTab");
                }
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
