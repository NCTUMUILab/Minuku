package edu.umich.si.inteco.minuku.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.data.DataHandler;
import edu.umich.si.inteco.minuku.model.Annotation;
import edu.umich.si.inteco.minuku.model.AnnotationSet;
import edu.umich.si.inteco.minuku.model.Session;
import edu.umich.si.inteco.minuku.services.CaptureProbeService;
import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.DatabaseNameManager;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.RecordingAndAnnotateManager;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;
import edu.umich.si.inteco.minuku.util.VisualizationManager;

/**
 * Created by Armuro on 6/16/14.
 */
public class AnnotateActivity extends Activity implements OnItemSelectedListener {


    private static final String LOG_TAG = "AnnotateActivity";
    private int mSessionId=-1;

    private static String mDefaultVisualizationType = RecordingAndAnnotateManager.ANNOTATION_VISUALIZATION_TYPE_LOCATION;

    private Annotation mLabel;
    private Annotation mNote;
    //by default we don't show a list of recording
    private String mReviewMode = RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_NONE;

    /**Layout Element**/
    Spinner labelSpinner;
    Button submitButton;
    EditText noteEditText;
    TextView timeLabelViewText;
    GoogleMap map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setting up layout
        setContentView(R.layout.activity_annotate);

        //the bunble should contain the id of the session that the annotation should go to
        Bundle bundle = getIntent().getExtras();

        //know whether we should start a new recording if we haven't started one
        boolean startRecording = bundle.getBoolean(ActionManager.ACTION_PROPERTIES_RECORDING_STARTED_BY_USER, false);
        Log.d(LOG_TAG, "[test start annotate recording] startRecording " + startRecording);
        String reviewMode = bundle.getString(ActionManager.ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING, RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_NONE);
        mReviewMode =reviewMode;

        Log.d(LOG_TAG, "[test start annotate recording] review mode:" + mReviewMode);

        //if we need to start a new recording, we start it first, get a session id, and then allow annotation
        if(startRecording){

            //if the annotateActivity needs to start an action, we need to know which annotateAction is starting this.
            int annotateRecoridngActionId = bundle.getInt("annotateRecordingActionId", -1 );
            if (annotateRecoridngActionId!=-1){
                mSessionId = ActionManager.createSavingRecordAction(annotateRecoridngActionId);
            }

            Log.d(LOG_TAG, "[test start annotate recording] we need to start recording in the annotate Activity, the annotateAction that starts it is " + annotateRecoridngActionId
                    + " the new session we start is " +mSessionId  +" the review mode is " + mReviewMode);

        }

        //we don't need to start a session, we have a session id from the intent
        else {
            //get the session id. If we need to start a new session here, this session Id will be 0 (because it hasn't been set)
            mSessionId = bundle.getInt(DatabaseNameManager.COL_SESSION_ID);
            Log.d(LOG_TAG, "[test start annotate recording] we don't need to start recording in the annotate Activity, the session id is " + mSessionId  +" the review mode is " + mReviewMode);

        }

        //after getting a session id, we get the session Object. We first need to know whether this session is an ongoing session or is a previous session
        //if it is a previous session (not a currently recording session), we get its existing session from the database
        //if it is an ongoing sesison, then we also need to attach the annotation to the session



        //set up the views
        setupViews();

    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        Session session=null;
        //get session
        session = RecordingAndAnnotateManager.getSession(mSessionId);


        //show trip time
        long startTime = session.getStartTime();
        long endTime = session.getEndTime();
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DATE_TEXT_HOUR_MIN_SEC);

        if (endTime!=0){
            timeLabelViewText.setText( "Time: " + ScheduleAndSampleManager.getTimeString(startTime, sdf) + " - " + ScheduleAndSampleManager.getTimeString(endTime, sdf) );
        }
        else {

            if (RecordingAndAnnotateManager.getCurRecordingSession(mSessionId)!=null)
                timeLabelViewText.setText( "Time: " + ScheduleAndSampleManager.getTimeString(startTime, sdf) + " - Now"  );
            else
                timeLabelViewText.setText( "Time: " + ScheduleAndSampleManager.getTimeString(startTime, sdf) + " - Unkown"  );

        }



        /** After we got a new session id, we retrieve annotation that has been added to the session from the database **/
        if (session!=null) {
            AnnotationSet annotationSet = session.getAnnotationsSet();
            ArrayList<Annotation> annotations = annotationSet.getAnnotations();

            //if the session has had existing annotations
            if (annotationSet.getAnnotations()!=null)

                for (int i=0; i<annotations.size(); i++){

                    String content = annotations.get(i).getContent();
                    //Log.d(LOG_TAG, "[AnnotateActivity] the annotation of session " + mSessionId + " is " + content);
                    ArrayList<String> tags = annotations.get(i).getTags();

                    //the annotation is a label
                    if (tags.contains("Label")){
                        //use adapter to find the array that the label spinner uses
                        ArrayAdapter myAdap = (ArrayAdapter) labelSpinner.getAdapter();
                        //get the position of the label (content)
                        int selectionPos = myAdap.getPosition(content);
                        //set the label of the spinner by position
                        labelSpinner.setSelection(selectionPos);
                    }

                    //the annotation is a note
                    else if (tags.contains("Note")) {
                        //set the content of the Note form
                        noteEditText.setText( annotations.get(i).getContent());
                    }
                }

            showRecordingVizualization((int) session.getId());

            //if the annotate activity include visualization


        }
    }


    private void setUpMapIfNeeded() {

        if (map==null) {
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment)).getMap();
        }

    }

    public ArrayList<LatLng> getLocationPointsToDrawOnMap(int sessionId) {

        ArrayList<LatLng> points = new ArrayList<LatLng>();



        //get data from the database
        ArrayList<String> data = DataHandler.getDataBySession(sessionId,RecordingAndAnnotateManager.ANNOTATION_VISUALIZATION_TYPE_LOCATION);


        for (int i=0; i<data.size(); i++){

            String[] record = data.get(i).split(Constants.DELIMITER);

            //get location parameters
            double lat = Double.parseDouble(record[DatabaseNameManager.COL_INDEX_RECORD_LOC_LATITUDE_] );
            double lng = Double.parseDouble(record[DatabaseNameManager.COL_INDEX_RECORD_LOC_LONGITUDE] );
            points.add(new LatLng(lat, lng));
           // Log.d(LOG_TAG, "[showRecordingVizualization] lat ( " + lat + ", " + lng + " )"  );

        }

        return points;
    }


    private void showRecordingVizualization(final int sessionId){

        //TODO: viztype is got from action...
        String vizType = mDefaultVisualizationType;

        //if the visualization is map
        if (vizType.equals(RecordingAndAnnotateManager.ANNOTATION_VISUALIZATION_TYPE_LOCATION)) {

            //validate map
            setUpMapIfNeeded();

            //draw map
            if (map!=null){

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(VisualizationManager. GOOGLE_MAP_DEFAULT_CAMERA_CENTER, VisualizationManager.GOOGLE_MAP_DEFAULT_ZOOM_LEVEL));


                //if we're reviewing a previous session, get session from the database (note that we have to use session id to check instead of a session instance)
                if (RecordingAndAnnotateManager.getCurRecordingSession(sessionId)==null) {

                    //because there could be many points for already ended trace, so we use asynch to download the annotations
                    new LoadDataAsyncTask().execute(sessionId);

                }
                //the recording is ongoing, so we periodically query the database to show the latest path
                //TODO: draw my current location from the starting point.
                else {

                    Log.d(LOG_TAG, "[showRecordingVizualization] the session is in the currently recording session");

                    final Handler updateMapHandler = new Handler();

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {

                            try{

                                Log.d(LOG_TAG, "[showRecordingVizualization] the session is in the currently recording session, update map!!! Get new points!");

                                //get location points to draw on the map..
                                ArrayList<LatLng> points = getLocationPointsToDrawOnMap(sessionId);

                                LatLng startLatLng;
                                //we use endLatLng, which is the user's current location as the center of the camera
                                LatLng endLatLng;


                                //only has one point
                                if (points.size()==1){

                                    startLatLng  = points.get(0);
                                    endLatLng = points.get(0);

                                    VisualizationManager.showMapWithPathsAndCurLocation(map, points, endLatLng);
                                }
                                //when have multiple locaiton points
                                else if (points.size()>1) {

                                    startLatLng  = points.get(0);
                                    endLatLng = points.get(points.size()-1);

                                    VisualizationManager.showMapWithPathsAndCurLocation(map, points, endLatLng);
                                }




                            }catch (IllegalArgumentException e){
                                //Log.e(LOG_TAG, "Could not unregister receiver " + e.getMessage()+"");
                            }
                            updateMapHandler.postDelayed(this, CaptureProbeService.DEFAULT_ACTION_RATE_INTERVAL);
                        }
                    };

                    /**start repeatedly store the extracted contextual information into Record objects**/
                    updateMapHandler.post(runnable);


                }

            }
        }

    }

    public void onItemSelected(AdapterView<?> parent, View view,  int pos, long id) {

        Log.d(LOG_TAG, "[AnnotateActivity] selected item " + labelSpinner.getSelectedItem() + " at " + labelSpinner.getSelectedItemPosition() );

        //Log user action
        LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                LogManager.LOG_TAG_USER_SELECTING,
                "User Select:\t" + labelSpinner.getSelectedItem() + "\t" + "LabelSpinner" + "\t" + "AnnotateActivity");
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback


    }


    private void setupViews() {

        timeLabelViewText = (TextView) findViewById(R.id.timeLabel);


        noteEditText = (EditText) findViewById(R.id.noteEditText);
        noteEditText.addTextChangedListener( new TextWatcher() {
                     @Override
                     public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        // Log.d(LOG_TAG, "before change the text is " +s );
                     }

                     @Override
                     public void onTextChanged(CharSequence s, int start, int before, int count) {

                         Log.d(LOG_TAG, "user type " + s  + " start " + start + " before " + before + " count " + count);

                         //Log user action
                         LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                                 LogManager.LOG_TAG_USER_TYPING,
                                 "User Type:\t" + s+ "\t" + "AnnotateActivity");

                     }

                     @Override
                     public void afterTextChanged(Editable editable) {

                        // Log.d(LOG_TAG, "after change user type: " + editable.toString() );
                     }
                 }


        );

        //add a itemselected listener to the spinner
        labelSpinner = (Spinner) findViewById(R.id.labelSpinner);
        labelSpinner.setOnItemSelectedListener(this);

        //add a click listener to the submit button
        submitButton = (Button) findViewById(R.id.annotationSubmitButton);
        submitButton.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View view) {

                //clicking on the button will submit the lable and the note to the annotation
                //Log.d(LOG_TAG, "[AnnotateActivity] submit annotation: " + labelSpinner.getSelectedItem()  + " note : " +  noteEditText.getText());


                //create annotaiton set
                AnnotationSet annotationSet = new AnnotationSet();
                annotationSet.setName(Constants.CURRENT_STUDY_CONDITION);


                //create annotation for label
                Annotation label = new Annotation();
                //add Label tag to the label annotation
                label.addTag("Label");
                //add tag to the current study condition (for the labelign study)
                //set label content
                label.setContent(labelSpinner.getSelectedItem().toString());

                //create annotaiton for comment
                Annotation note  = new Annotation();
                //add Comment tag to the Comment annotation
                note.addTag("Note");
                //set note content
                note.setContent(noteEditText.getText().toString());

                //add annotations  to the annotation set
                annotationSet.addAnnotation(label);
                annotationSet.addAnnotation(note);

                Log.d(LOG_TAG, "[AnnotateActivity] [test listrecording review mode] submit annotation set " + annotationSet.toJSONObject().toString());

                //associate annotationSet to the session
                RecordingAndAnnotateManager.addAnnotationToSession(annotationSet, mSessionId);

                Log.d(LOG_TAG, "[AnnotateActivity] !mReviewMode " + mReviewMode);
                //if in the review mode is not "NA", we bring users to the list of recording.
                if (!mReviewMode.equals(RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_NONE)){
                    //if we will review a list of recording..
                    Log.d(LOG_TAG, "[AnnotateActivity] [test listrecording review mode] going to start list recording activity " );
                    RecordingAndAnnotateManager.startListRecordingActivity(mReviewMode);
                }




                //Log user action
                LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                        LogManager.LOG_TAG_USER_CLICKING,
                        "User Click:\t" + "Submit"+ "\t" + "AnnotateActivity");


                //exit the activity
                finish();
            }
        });

    }

    //use Asynk task to load sessions
    private class LoadDataAsyncTask extends AsyncTask<Integer, Void, ArrayList<LatLng>> {
        private final ProgressDialog dialog = new ProgressDialog(AnnotateActivity.this);

        @Override
        protected ArrayList<LatLng> doInBackground(Integer... params) {
            int sessionId = params[0];

            return getLocationPointsToDrawOnMap(sessionId);
        }

        // can use UI thread here
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Loading...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList<LatLng> points) {
            super.onPostExecute(points);

            if (RecordingAndAnnotateManager.getCurRecordingSession(mSessionId)==null) {

                if (points.size()>0){
                    LatLng startLatLng  = points.get(0);
                    LatLng endLatLng = points.get(points.size()-1);
                    LatLng middleLagLng = points.get((points.size()/2));

                    Log.d(LOG_TAG, "[showRecordingVizualization] the session is not in the currently recording session");
                    //we first need to know what visualization we want to use, then we get data for that visualization

                    //show maps with path (draw polylines)
                    VisualizationManager.showMapWithPaths(map, points, middleLagLng);
                }

            }

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
        }




    }
}


