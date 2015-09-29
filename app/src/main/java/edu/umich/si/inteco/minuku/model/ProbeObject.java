package edu.umich.si.inteco.minuku.model;

import java.util.ArrayList;

/**Each ProbeObject can trigger a number of ProbeObjects, but can be triggered only by one another ProbeObject. As a result, 
 * it has a list of triggerLinks, in each of which itself is the trigger; and has a trigger_id that refers to the its trigger
 * 
 **/
public class ProbeObject {

	
	/**property**/
	protected int _probeObjectId = -1;//Probe also maintains a list of probeobject Ids, the purpose is to quickly locate the probect object
    protected int _id=-1;	//based on the class, each object has an id for its class. An Action has an actionId, an event has an event id   
    protected String _class="";
    protected int _studyId = -1;//know which study owns this probe Object
    	
	//a list of triggerlinks in each of which itself is the trigger
    protected ArrayList<TriggerLink> mTriggerLinks;
    
    
    public ProbeObject(){    	
    	mTriggerLinks = new ArrayList<TriggerLink>();
    }
    
    
	public int getProbeObjectId() {		
		return _probeObjectId;
	}
	
	public void setProbeObjectId(int id) {		
		_probeObjectId = id;
	}
    
	public void setId(int id){
		_id = id;
	}

	public int getId(){
		return _id;
	}

	public String getProbeObjectClass(){
		return _class;
	}
	
	
	public String getTriggeredProbeObjectIdToString(){
		
		String str = "";
		
		if (mTriggerLinks!=null){
			
			for (int i=0; i< mTriggerLinks.size(); i++){	
				str+= mTriggerLinks.get(i).toString();
				if (i<mTriggerLinks.size()-1){
					str+=",";
				}
			}
			
		}

			return str;
		
	}
	

	public void addTriggerLink (TriggerLink tl){
		
		if (mTriggerLinks==null){
			mTriggerLinks = new ArrayList<TriggerLink>();
		}
		
		mTriggerLinks.add(tl);
		
	}
	
	public void addTriggerLink (ProbeObject trigger, ProbeObject triggered_probe_object, float sampling_rate){
		
		if (mTriggerLinks==null){
			mTriggerLinks = new ArrayList<TriggerLink>();
		}
		
		TriggerLink tl = new TriggerLink (trigger, triggered_probe_object, sampling_rate);
	
		mTriggerLinks.add(tl);
	
	}
	
	public TriggerLink getTriggerLink (int id){
		
		for (int i=0; i<mTriggerLinks.size(); i++){
			if (mTriggerLinks.get(i).getId()==id){
				
				return mTriggerLinks.get(i);
			}
		}		
		return null;
	}
    

    
	public void addTriggeredProbeObject (TriggerLink triggerLink){
	
		if (mTriggerLinks==null){
			mTriggerLinks = new ArrayList<TriggerLink>();
		}
		
		mTriggerLinks.add(triggerLink);
	
	}

	public ProbeObject getTriggeredProbeObjectAt(int index){	
	
		return mTriggerLinks.get(index).getTriggeredProbeObject();	
	
	}

	public ArrayList<TriggerLink> getTriggerLinks (){
	
		return mTriggerLinks;
	
	}
	
	
	public int getStudyId (){
		return _studyId;
	}
	
	public void setStudyId (int id){
		_studyId = id;
	}
	
	
	
}