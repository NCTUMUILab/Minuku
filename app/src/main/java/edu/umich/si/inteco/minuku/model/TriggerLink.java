package edu.umich.si.inteco.minuku.model;

public class TriggerLink{
	
	private static final String LOG_TAG = "TriggerLink";
	private int _id = -1;
	private int _studyId = -1;
	private String _triggerClass;
	private int _triggerId; 
	private float _triggerRate = 1;
	private ProbeObject _trigger;
    private ProbeObject _triggeredProbeObject;
    
    public TriggerLink(String trigger_class, int trigger_id, ProbeObject triggered_probe_object, float trigger_rate) {
    	
    	_triggerClass  = trigger_class ;
    	_triggerId = trigger_id;
    	_triggeredProbeObject = triggered_probe_object;
		_triggerRate = trigger_rate;
    }
    
	public TriggerLink (ProbeObject trigger, ProbeObject triggered_probe_object, float trigger_rate){

		_trigger = trigger;
		_triggeredProbeObject = triggered_probe_object;
		_triggerRate = trigger_rate;
	}
    
	public TriggerLink (ProbeObject triggered_probe_object, float trigger_rate){

		_triggeredProbeObject = triggered_probe_object;
		_triggerRate = trigger_rate;
	}
	
	public ProbeObject getTrigger(){
		
		return _trigger; 
	}
	
	
	public ProbeObject getTriggeredProbeObject(){
		
		return _triggeredProbeObject; 
	}
	
	public void setTriggeredProbeObject(ProbeObject probe_object){
		_triggeredProbeObject = probe_object;
	}
	
	public void setTrigger(ProbeObject trigger){
		this._trigger = trigger;
	}
    
    public void setTriggerId(int id){  	
    	this._triggerId = id;
    }
    
    public int getTriggerId(){
    	return _triggerId;
    }
    
    public void setTriggerClass(String trigger_class){
    	
    	this._triggerClass = trigger_class;
    }
    
    public String getTriggerClass(){
    	return _triggerClass;
    }
    

	public int getId(){
		return _id;
	}
	
	public float getTriggerRate(){
		return _triggerRate;
	}
	
	public int getStudyId (){
		return _studyId;
	}
	
	public void setStudyId (int id){
		_studyId = id;
	}
	
	
	
	@Override     	
	public String toString(){
		return _triggeredProbeObject.getId() + " " + _triggerRate;
	}
	
}