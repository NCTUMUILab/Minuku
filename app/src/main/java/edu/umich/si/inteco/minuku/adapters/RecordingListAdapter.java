package edu.umich.si.inteco.minuku.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.umich.si.inteco.minuku.GlobalNames;
import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.model.Annotation;
import edu.umich.si.inteco.minuku.model.Session;
import edu.umich.si.inteco.minuku.util.ScheduleAndSampleManager;

/**
 * Created by Armuro on 7/1/14.
 */
public class RecordingListAdapter extends ArrayAdapter<Session> {

    private static final String LOG_TAG = "MyTaskArrayAdapter";
    private Context mContext;
    private ArrayList<Session> data;

    public RecordingListAdapter(Context context, int resource, ArrayList<Session> sessions) {
        super(context, resource, sessions);
        this.mContext = context;
        this.data = sessions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recording_item_view, parent, false);

        Log.d(LOG_TAG, "enter get view...");


        ImageView recordingImageView = (ImageView) view.findViewById(R.id.recording_image);
        TextView recordingLabelTextView = (TextView) view.findViewById(R.id.recording_label);
        TextView recordingNoteTextView = (TextView) view.findViewById(R.id.recording_note);
        TextView recordingDateTextView = (TextView) view.findViewById(R.id.recording_date);
        TextView recordingTimeTextView  = (TextView) view.findViewById(R.id.recording_time);


        if(data.size()<=0)
        {
            recordingLabelTextView.setText("No recording available");
        }
        else
        {
            Session session = getItem(position);

            if (session!= null) {

                String content;
                String labelText = "No label yet";
                String noteText = "No note yet";

                //if the session has label and note...
                if (session.getAnnotationsSet()!=null && session.getAnnotationsSet().getAnnotations()!=null) {
                    //get annotation
                    for (int j=0; j<session.getAnnotationsSet().getAnnotations().size(); j++) {

                        Annotation annotation = session.getAnnotationsSet().getAnnotations().get(j);
                        content = annotation.getContent();

                        //the annotation is the label
                        if (annotation.getTags().contains("Label")){
                            labelText = content;
                        }
                        //the annotation is the note
                        if (annotation.getTags().contains("Note")){
                            noteText = content;
                        }
                    }
                }


                Log.d(LOG_TAG, "[getView in recording List] the session id is " + session.getId() + " start Time is " + session.getStartTime() );
                // My layout has only one TextView


                if (recordingLabelTextView != null) {

                    recordingLabelTextView.setText(String.format("%s",labelText ));

                }
                if (recordingNoteTextView != null) {

                    recordingNoteTextView.setText(String.format("%s", noteText));
                }

                if ( recordingDateTextView != null) {

                    SimpleDateFormat sdf_date = new SimpleDateFormat(GlobalNames.DATE_FORMAT_DATE_TEXT);
                    recordingDateTextView.setText(String.format("%s", ScheduleAndSampleManager.getTimeString(session.getStartTime(), sdf_date) ));
                }

                if (recordingTimeTextView  != null) {

                    SimpleDateFormat sdf_time = new SimpleDateFormat(GlobalNames.DATE_FORMAT_HOUR_MIN);
                    recordingTimeTextView .setText(String.format("%s", ScheduleAndSampleManager.getTimeString(session.getStartTime(), sdf_time) ));
                }

                if (recordingImageView != null) {

                    if (labelText.contains("driver")){
                        recordingImageView.setImageResource(R.drawable.car);
                    }
                    else if (labelText.contains("driver")){
                        recordingImageView.setImageResource(R.drawable.car);
                    }
                    else if (labelText.equals("Taking a bus")){
                        recordingImageView.setImageResource(R.drawable.bus);
                    }
                    else if (labelText.equals("Walking")){
                        recordingImageView.setImageResource(R.drawable.walk);
                    }
                    else if (labelText.equals("Biking")){
                        recordingImageView.setImageResource(R.drawable.bike);
                    }
                    else if (labelText.equals("I'm indoors")){

                    }


                }

            }

        }



        return view;
    }

}
