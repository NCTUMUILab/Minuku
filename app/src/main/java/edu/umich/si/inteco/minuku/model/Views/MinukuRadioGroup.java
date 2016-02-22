package edu.umich.si.inteco.minuku.model.Views;

import android.content.Context;
import android.widget.RadioGroup;

/**
 * Created by Armuro on 2/22/16.
 */
public class MinukuRadioGroup extends RadioGroup{

    private int quesitonIndex = -1;

    public MinukuRadioGroup(Context context) {
        super(context);
    }

    public int getQuesitonIndex() {
        return quesitonIndex;
    }

    public void setQuesitonIndex(int quesitonIndex) {
        this.quesitonIndex = quesitonIndex;
    }
}
