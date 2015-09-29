package edu.umich.si.inteco.minuku.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import edu.umich.si.inteco.minuku.GlobalNames;
import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.adapters.MyTaskArrayAdapter;
import edu.umich.si.inteco.minuku.util.TaskManager;

/**
 * Created by Armuro on 7/13/14.
 */
public class TaskSectionFragment extends Fragment{

    private TextView idTextView;
    private ListView taskListView;

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

        View rootView = inflater.inflate(R.layout.fragment_my_task, container, false);


        idTextView = (TextView) rootView.findViewById(R.id.idTextView);
        idTextView.setText("My ID:" + GlobalNames.DEVICE_ID);

        taskListView = (ListView) rootView.findViewById(R.id.task_list);

        MyTaskArrayAdapter adapter = new MyTaskArrayAdapter(
                inflater.getContext(),
                R.id.task_list,
                TaskManager.getTaskList()
        );

        taskListView.setAdapter(adapter);

        //setListAdapter(adapter);

        return rootView;
    }


}
