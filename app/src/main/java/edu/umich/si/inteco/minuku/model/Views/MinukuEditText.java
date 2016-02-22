package edu.umich.si.inteco.minuku.model.Views;

import android.content.Context;
import android.widget.EditText;

/**
 * Created by Armuro on 2/22/16.
 */
public class MinukuEditText extends EditText {

    private int quesitonIndex = -1;

    public MinukuEditText(Context context) {
        super(context);
    }

    public int getQuesitonIndex() {
        return quesitonIndex;
    }

    public void setQuesitonIndex(int quesitonIndex) {
        this.quesitonIndex = quesitonIndex;
    }

}
