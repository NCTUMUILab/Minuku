package edu.umich.si.inteco.minuku.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.R;
import edu.umich.si.inteco.minuku.model.Task;

public class MyTaskArrayAdapter extends ArrayAdapter<Task> implements OnClickListener {

	private static final String LOG_TAG = "MyTaskArrayAdapter";	
	private Context mContext;
	private ArrayList<Task> data;
	
	public MyTaskArrayAdapter(Context context, int resource, ArrayList<Task> tasks) {
		super(context, resource, tasks);
		this.mContext = context;
		data = tasks;

	}
	
    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder{
         
        public TextView taskNameTextView;
        public TextView taskDescriptionTextView;
 
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {

    	View view = convertView;
        ViewHolder holder;
        
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.task_item_view, null);
            
            holder = new ViewHolder();

        }
        else 
            holder=(ViewHolder)view.getTag();

        
        
        if(data.size()<=0)
        {
            holder.taskNameTextView.setText("No Data");
             
        }
        else
        {
            try {
                holder.taskNameTextView = (TextView) view.findViewById(R.id.task_name);
                holder.taskDescriptionTextView = (TextView) view.findViewById(R.id.task_description);

                //task 0 is for background recoridng. Don't need to show to the user
                if (position<0) {
                    holder.taskNameTextView.setVisibility(View.GONE);
                    holder.taskDescriptionTextView.setVisibility(View.GONE);
                }
                //we start listing the tasks from index 1
                else {
                    Task task = getItem(position);
                    if (task!= null) {
                        // My layout has only one TextView

                        if (holder.taskNameTextView != null) {
                            // do whatever you want with your string and long
                            holder.taskNameTextView.setText(String.format("%s", task.getName()));

                        }
                        if (holder.taskDescriptionTextView != null) {
                            // do whatever you want with your string and long
                            holder.taskDescriptionTextView.setText(String.format("%s", task.getDescription()));

                        }
                    }
                }

            }catch (NullPointerException e) {

            }



        }
        
        

        return view;
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
