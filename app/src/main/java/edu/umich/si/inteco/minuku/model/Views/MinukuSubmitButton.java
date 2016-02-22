package edu.umich.si.inteco.minuku.model.Views;

import android.content.Context;
import android.widget.Button;

import edu.umich.si.inteco.minuku.model.Questionnaire.Questionnaire;

/**
 * Created by Armuro on 2/22/16.
 */
public class MinukuSubmitButton extends Button {

    private Questionnaire questionnaire;
    public MinukuSubmitButton(Context context) {
        super(context);
    }

    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    public void setQuestionnaire(Questionnaire questionnaire) {
        this.questionnaire = questionnaire;
    }
}
