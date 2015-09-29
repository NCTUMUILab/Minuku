package edu.umich.si.inteco.minuku.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import edu.umich.si.inteco.minuku.GlobalNames;
import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.model.actions.GenerateEmailQuestionnaireAction;
import edu.umich.si.inteco.minuku.util.ActionManager;
import edu.umich.si.inteco.minuku.util.LogManager;

/**
 * Created by Armuro on 7/13/14.
 */
public class DailyJournalSectionFragment extends Fragment{

    private static final String LOG_TAG = "DailyJournalSectionFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_daily_report, container, false);

        Button respondToJournalButton = (Button) rootView.findViewById(R.id.DailyReportButton);

        //define click listener

        respondToJournalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //create email questionnaire action
                GenerateEmailQuestionnaireAction generateEmailQuestionnaireAction =
                        new GenerateEmailQuestionnaireAction(
                                ActionManager.USER_INITIATED_RESPONDING_TO_DAILY_REPORT,
                                ActionManager.USER_RESPOND_TO_DAILY_REPORT_ACTION_NAME,
                                ActionManager.ACTION_TYPE_EMAIL_QUESTIONNAIRE,
                                ActionManager.ACTION_EXECUTION_STYLE_ONETIME, GlobalNames.LABELING_STUDY_ID);


                generateEmailQuestionnaireAction.setQuestionnaireId(1);

                ActionManager.executeAction(generateEmailQuestionnaireAction);

                //Log user action
                LogManager.log(LogManager.LOG_TYPE_USER_ACTION_LOG,
                        LogManager.LOG_TAG_USER_CLICKING,
                        "User Click:\t" + "dailyReport Button" + "\t" + "DaiyJournalTab");

            }
        });

        return rootView;

    }
}
