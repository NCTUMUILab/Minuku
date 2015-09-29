package edu.umich.si.inteco.minuku.model.record;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.umich.si.inteco.minuku.GlobalNames;
import edu.umich.si.inteco.minuku.model.Session;


public class Record {
 
	protected long _id;
	protected long _timestamp;
	protected int _type;
	protected Session _session;
    protected ArrayList<Integer> mSavedBySessionIds;

	
	protected String mTimestring;

	//by default each record should store values (integer). But subclasses of Record can have different types of values such as DoubleValues, StringValues
	public ArrayList<Integer> _values;

	
	public Record(){
        _values = new ArrayList<Integer>();
        mSavedBySessionIds = new ArrayList<Integer>();
	}

    public ArrayList<Integer> getSavedSessionIds() {
        return mSavedBySessionIds;
    }

    public void addSavedBySessionId(int sessionId){

        mSavedBySessionIds.add(sessionId);

    }

    public void setID(int id){
		_id = id;
	}

	public long getID(){
		return _id;
	}
	
	public void setTimestamp(long t){
		_timestamp = t;
	}

	public long getTimestamp(){
		return _timestamp;
	}
	

	public String getTimeString(){
		
		SimpleDateFormat sdf_now = new SimpleDateFormat(GlobalNames.DATE_FORMAT_NOW);
		mTimestring = sdf_now.format(_timestamp);

		return mTimestring;
	}
	
	public int getType(){
		return _type;
	} 
	
	public void setType(int t){
		_type = t;
	} 
	
	public Session getSession(){
		return _session;
	}
	
	public void setSession(Session s){
		_session = s;
	}
	
}
