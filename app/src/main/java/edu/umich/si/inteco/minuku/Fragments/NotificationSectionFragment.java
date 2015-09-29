package edu.umich.si.inteco.minuku.Fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by Armuro on 7/13/14.
 */
public class NotificationSectionFragment extends ListFragment{
    String[] numbers_text = new String[] { "Notification_one", "Notification_two", "Notification_three", "Notification_four",
            "Notification_five", "Notification_six", "Notification_seven", "Notification_eight", "Notification_nine", "Notification_ten"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                inflater.getContext(),
                android.R.layout.simple_list_item_1,
                numbers_text);
        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);

    }
}
