package edu.umich.si.inteco.minuku.model.Views;

import android.content.Context;
import android.widget.CheckBox;

/**
 * Created by Armuro on 2/22/16.
 */
public class MinukuCheckBox extends CheckBox {

    private int quesitonIndex = -1;
    public MinukuCheckBox(Context context) {
        super(context);
    }

    public int getQuesitonIndex() {
        return quesitonIndex;
    }

    public void setQuesitonIndex(int quesitonIndex) {
        this.quesitonIndex = quesitonIndex;
    }
}
