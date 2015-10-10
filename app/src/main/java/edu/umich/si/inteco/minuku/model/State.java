package edu.umich.si.inteco.minuku.model;

/**
 * Created by Armuro on 10/9/15.
 */
public class State {

    private int mId;
    private String mName;
    private String mValue;

    public State(String name) {
        mName = name;
    }

    public State(String name, String value) {
        mName = name;
        mValue = value;
    }


    public int getId() {
        return mId;
    }

    public void setId(int Id) {this.mId = Id;}

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getValue() {
        return mValue;
    }

    public void setmValue(String value) {
        this.mValue = value;
    }
}