package edu.umich.si.inteco.minuku.Fragments;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.activities.AnnotateActivity;
import edu.umich.si.inteco.minuku.MainActivity;
import edu.umich.si.inteco.minuku.adapters.RecordingListAdapter;
import edu.umich.si.inteco.minuku.model.Session;
import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.ConfigurationManager;
import edu.umich.si.inteco.minuku.util.DatabaseNameManager;
import edu.umich.si.inteco.minuku.util.LogManager;
import edu.umich.si.inteco.minuku.util.RecordingAndAnnotateManager;

/**
 * Created by Armuro on 7/13/14.
 */
public class ListRecordingSectionFragment extends Fragment {


    private static final String LOG_TAG = "LstRcrdSecFrgmt";

    private static String mReviewMode= RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_ALL;
    private static ArrayList<Session> mSessions;
    private static ListView mRecordingListView;
    private static LayoutInflater mInflater;
    private static RecordingListAdapter mListRecordingAdapter;

    public ListRecordingSectionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_list_recording, container, false);
        mInflater = inflater;
        showRecordingList(rootView, inflater);

        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
    }

    public static void refreshRecordingList() {

        Log.d(LOG_TAG, "[refreshRecordingList] the review mode is " + mReviewMode);

        if (getRecordedSessions()==null){
            Log.d(LOG_TAG, "[showRecordingList] there is no recording");
            mSessions = new ArrayList<Session>();
        }
        else {
            Log.d(LOG_TAG, "[showRecordingList] there are session recordings");
            mSessions = getRecordedSessions();
        }


        if (mListRecordingAdapter!=null) {
            mListRecordingAdapter.notifyDataSetChanged();
            mListRecordingAdapter = new RecordingListAdapter(
                    mInflater.getContext(),
                    R.id.recording_list,
                    mSessions
            );

            mRecordingListView.setAdapter(mListRecordingAdapter);
        }

    }

    public static RecordingListAdapter getRecordingListAdapter() {
        return mListRecordingAdapter;
    }

    private static ArrayList<Session> getRecordedSessions() {
        ArrayList<Session> sessions =null;

        //review all recoridngs
        if (mReviewMode.equals(RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_ALL)) {
            //sessions = RecordingAndAnnotateManager.getCurRecordingSessions();

            Log.d(LOG_TAG, "[showRecordingList][show all sessions]");

            sessions = RecordingAndAnnotateManager.getAllSessions();

            Log.d(LOG_TAG, "[showRecordingList][show all sessions]  there are " + sessions.size() + " sessions");

        }
        else if (mReviewMode.equals(RecordingAndAnnotateManager.ANNOTATE_REVIEW_RECORDING_RECENT)) {

            sessions = RecordingAndAnnotateManager.getRecentSessions();

            Log.d(LOG_TAG, "[showRecordingList][show recent sessions] there are " + sessions.size() + " sessions");
        }
        //assign to mSession
        mSessions = sessions;

        return sessions;
    }

    private void showRecordingList(View rootView, LayoutInflater inflater) {

        ArrayList<Session> sessions =null;

        mRecordingListView = (ListView)rootView.findViewById(R.id.recording_list);


        if (getRecordedSessions()==null){
            Log.d(LOG_TAG, "[showRecordingList] there is no recording");
            mSessions = new ArrayList<Session>();
        }
        else {
            Log.d(LOG_TAG, "[showRecordingList] there are session recordings");
            mSessions = getRecordedSessions();
        }


        mListRecordingAdapter = new RecordingListAdapter(
                mInflater.getContext(),
                R.id.recording_list,
                mSessions
        );

        Log.d(LOG_TAG, "[showRecordingList] the mListRecordingAdapter is " +  mListRecordingAdapter);

        if (mRecordingListView!=null) {
            if (mListRecordingAdapter!=null && mListRecordingAdapter.getCount()>0){
                mRecordingListView.setAdapter(mListRecordingAdapter);

                mRecordingListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                        Log.d(LOG_TAG, "[onItemSelected] position " + position + " is clicked");

                        //Log user action
                        LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                                LogManager.LOG_TAG_USER_CLICKING,
                                "User Click:\t" + "session" + mSessions.get(position).getId() + "\t" + "ListRecordingActivity");

                        startAnnotateActivity(position);

                    }
                });
            }
        }


    }

    private void startAnnotateActivity(int position) {

        Session session =  mSessions.get(position);
        Log.d(LOG_TAG, "the session at " + position + " being selected is " + session.getId());

        Bundle bundle = new Bundle();
        bundle.putInt(DatabaseNameManager.COL_SESSION_ID, (int) session.getId());
        bundle.putString(ConfigurationManager.ACTION_PROPERTIES_ANNOTATE_REVIEW_RECORDING, mReviewMode);
        Intent intent = new Intent(getActivity(), AnnotateActivity.class);
        intent.putExtras(bundle);

        startActivity(intent);

    }

    public String getReviewMode() {
        return mReviewMode;
    }

    public void setReviewMode(String reviewMode) {
        this.mReviewMode = reviewMode;
    }
/*
    //use Asynk task to load sessions
    private class ListRecordAsyncTask extends AsyncTask<String, Void, ArrayList<Session>> {
        private final ProgressDialog dialog = new ProgressDialog(getActivity());

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
    */
}
