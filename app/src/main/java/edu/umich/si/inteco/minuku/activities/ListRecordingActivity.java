package edu.umich.si.inteco.minuku.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.adapters.RecordingListAdapter;
import edu.umich.si.inteco.minuku.model.Session;
import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.ConfigurationManager;
import edu.umich.si.inteco.minuku.util.DatabaseNameManager;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.RecordingAndAnnotateManager;

/**
 * Created by Armuro on 7/1/14.
 */
public class ListRecordingActivity extends Activity{

    private static final String LOG_TAG = " ListRecordingActivity";

    ArrayList<Session> mSessions;
    String mReviewMode = RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_recording);
    }

    private void startAnnotateActivity(int position) {

        Session session =  mSessions.get(position);
        Log.d(LOG_TAG, "the session at " + position + " being selected is " + session.getId());

        Bundle bundle = new Bundle();
        bundle.putInt(DatabaseNameManager.COL_SESSION_ID, (int) session.getId());
        bundle.putString(ConfigurationManager.ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING, mReviewMode);
        Intent intent = new Intent(this, AnnotateActivity.class);
        intent.putExtras(bundle);

        startActivity(intent);

    }

    private void showRecordingList(String reviewMode) {

        ArrayList<Session> sessions =null;

        ListView recordingListView = (ListView)findViewById (R.id.recording_list);

        Log.d(LOG_TAG, "[showRecordingList] the review mode is " + reviewMode);

        //get sessions
        try {
            sessions = new ListRecordAsyncTask().execute(mReviewMode).get();
            //assign to mSession
            mSessions = sessions;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        RecordingListAdapter adapter = new RecordingListAdapter(
                this,
                R.id.recording_list,
                mSessions
        );

        recordingListView.setAdapter(adapter);

        recordingListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Log.d(LOG_TAG, "[onItemSelected] position " + position + " is clicked");

                //Log user action
                LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                        LogManager.LOG_TAG_USER_CLICKING,
                        "User Click:\t" + "session"+mSessions.get(position).getId()+ "\t" + "ListRecordingActivity");

                startAnnotateActivity(position);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        Bundle bundle = getIntent().getExtras();
        String reviewMode = bundle.getString(ConfigurationManager.ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING);
        if (reviewMode!=null)
            mReviewMode = reviewMode;

        // Get the Camera instance as the activity achieves full user focus
        if (mReviewMode != null) {
            showRecordingList(mReviewMode);
        }
    }



    //use Asynk task to load sessions
    private class ListRecordAsyncTask extends AsyncTask<String, Void, ArrayList<Session>> {
        private final ProgressDialog dialog = new ProgressDialog(ListRecordingActivity.this);

        // can use UI thread here
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Loading...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        @Override
        protected ArrayList<Session> doInBackground(String... params) {

            String reviewMode = params[0];
            Log.d(LOG_TAG, "listRecordAsyncTask going to list recording with mode" + reviewMode);

            ArrayList<Session> sessions =null;

            //review all recoridngs
            if (reviewMode.equals(RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_ALL)) {
                //sessions = RecordingAndAnnotateManager.getCurRecordingSessions();

                sessions = RecordingAndAnnotateManager.getAllSessions();

                Log.d(LOG_TAG, "[showRecordingList][show all sessions]  there are " + sessions.size() + " sessions");

            }
            else if (reviewMode.equals(RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_RECENT)) {
                //TODO: sessions in the recent 24 hours

                sessions = RecordingAndAnnotateManager.getRecentSessions();

                Log.d(LOG_TAG, "[showRecordingList][show recent sessions] there are " + sessions.size() + " sessions");
            }
            else{
                sessions = new ArrayList<Session>();
            }

            return sessions;

        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList<Session> result) {
            super.onPostExecute(result);

            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
        }




    }

}
