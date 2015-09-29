package edu.umich.si.inteco.minuku.model.Log;

public class ProbeLog {

	private String mContent = "NA"; 
	private long mTimestamp =0;
	private String mTimeString = "NA";
	private String mType = "NA";
	private String mTag = "NA";
	
	public ProbeLog (){
		
	}
	
	public ProbeLog(String type, String tag, long timestamp, String timeString, String content){
		
		mType = type;
		mTag = tag;
		mTimestamp = timestamp;
		mTimeString = timeString;
		mContent = content;
		
	}
	
	public String getContent(){
		return mContent;
	}
	
	public String getTimeString(){
		return mTimeString;
	}
	
	public long getTimestamp(){
		return mTimestamp;
	}
	
	public String getType(){
		return mType;
	}
	
	public String getTag(){
		return mTag;
	}
	
	@Override
	public String toString() {
		
		String s = this.mTag + "\t" + this.mTimeString + "\t" + this.mTimestamp  + "\t" + this.mContent + "\n";
		
		return s;
	}
	
}
