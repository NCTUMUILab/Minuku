package edu.umich.si.inteco.minuku.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.adapters.MyTaskArrayAdapter;
import edu.umich.si.inteco.minuku.util.TaskManager;

/**
 * Created by Armuro on 2/13/16.
 */
public class HomeFragment extends Fragment {

    private TextView idTextView;
    private ImageView homeImageView;
    private TextView homeMessageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.home, container, false);

        idTextView = (TextView) rootView.findViewById(R.id.idTextView);
        idTextView.setText("My ID:" + Constants.USER_ID);

        return rootView;
    }
}
