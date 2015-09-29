package edu.umich.si.inteco.minuku.model.record;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.contextmanager.ContextManager;

public class SensorRecord extends Record {

	/** Tag for logging. */
    private static final String LOG_TAG = "SensorRecord";
    
    //the index of each sensor source is defined in GloableNames.class
    private int mSensorSource;
    
    public ArrayList<Float> sensorValues;
	 
	public SensorRecord(){
		
		this._type = ContextManager.CONTEXT_RECORD_TYPE_SENSOR;
		sensorValues = new ArrayList<Float>();
	}
	
	@Override
    public String toString() {
		
		String s = "";
		
		s+= this.getTimeString() + "\t" + 
			this.getTimestamp() +  "\t"	+
			this.getType() +"\t" ;
		
		//add sensor values to string
		for (int i=0; i<sensorValues.size(); i++){
			s+= sensorValues.get(i).toString() +"\t";
		}
		
		s+="\n";
        
        return s;
    }	
	
	public int getSensorSource(){
		return mSensorSource;
	} 
	
	public void setSensorSource(int s){
		mSensorSource = s;
	} 
}
