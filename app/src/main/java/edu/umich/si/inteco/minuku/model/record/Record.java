package edu.umich.si.inteco.minuku.model.Record;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.umich.si.inteco.minuku.Constants;
import edu.umich.si.inteco.minuku.model.Session;


public class Record {
 
	protected long _id;
	protected long _timestamp;
	protected String _source;
	protected Session _session;
    protected ArrayList<Integer> mSavedBySessionIds;
	protected JSONObject mData;
	protected String mTimestring;

	
	public Record(){
        mSavedBySessionIds = new ArrayList<Integer>();
	}

    public ArrayList<Integer> getSavedSessionIds() {
        return mSavedBySessionIds;
    }

    public void addSavedBySessionId(int sessionId){

        mSavedBySessionIds.add(sessionId);

    }

	@Override
	public String toString() {
		return "Record{" +
				"_id=" + _id +
				", _timestamp=" + _timestamp +
				", _source='" + _source + '\'' +
				", _session=" + _session +
				", mSavedBySessionIds=" + mSavedBySessionIds +
				", mData=" + mData +
				", mTimestring='" + mTimestring + '\'' +
				'}';
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

	public JSONObject getData() {
		return mData;
	}

	public void setData(JSONObject data) {
		this.mData = data;
	}

	public String getTimeString(){
		
		SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
		mTimestring = sdf_now.format(_timestamp);

		return mTimestring;
	}
	
	public String getSource(){
		return _source;
	} 
	
	public void setSource(String source){
		_source = source;
	} 
	
	public Session getSession(){
		return _session;
	}
	
	public void setSession(Session s){
		_session = s;
	}
	
}
