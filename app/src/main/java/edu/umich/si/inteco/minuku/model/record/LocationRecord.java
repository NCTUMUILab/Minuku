package edu.umich.si.inteco.minuku.model.record;

import android.location.Location;

import edu.umich.si.inteco.minuku.context.ContextManager;

public class LocationRecord extends Record {

	/** Tag for logging. */
    private static final String LOG_TAG = "LocationRecord.Class";
    
    private Location mLocation;
    
	public LocationRecord(){		
		this._type = ContextManager.CONTEXT_RECORD_TYPE_LOCATION;
	}
	
	@Override
    public String toString() {
		
		String s = "";
		
		s+= this.getTimeString() + "\t" + 
			this.getTimestamp() +  "\t"	+
			this.getType() +"\t" ;
		
		s+=mLocation.getLatitude() 
				+ "\t" + mLocation.getLongitude() 
				+ "\t" + mLocation.getAltitude() 
				+ "\t" + mLocation.getBearing()
				+ "\t" + mLocation.getSpeed()
				+ "\t" + mLocation.getProvider()
				+ "\t" + mLocation.getAccuracy();
		
		s+="\n";
        
        return s;
    }
	
	public void setLocation (Location l){
		mLocation = l;
	}
	
	public Location getLocation (){
		return mLocation;
	}
	
}
