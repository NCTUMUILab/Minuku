package edu.umich.si.inteco.minuku.model;

import android.util.Log;

import org.json.JSONObject;

public class Configuration {

	/** Tag for logging. */
    private static final String LOG_TAG = "Configuration";
	private int _id = -1;
	private int _studyId = -1;
	private int _version = -1;
	private String _name = "Configuration";
	private JSONObject _content;

	/**backend**/
	private String mBackendService = "NA";
	private String mBackendDatabaseType = "NA";
	private String mBackendDatabaseName = "NA";
	private String mBackendServiceURL = "NA";
	
	public Configuration (int id, int study_id, int version, String name, JSONObject content) {
		
		Log.d(LOG_TAG, "[Configuration]  save the content of the configuration " + content);
		
		_id = id;
		_studyId = study_id;
		_version = version; 
		_name = name;
		_content = content;
		
	}
	
	public int getVersion (){
		return _version;
	}
	
	public void setVersion (int version){
		_version = version;
	}
	
	public void setStudyId(int id){
		_studyId = id;
	}

	public int getStudyId(){
		return _studyId;
	}
	
	public void setId(int id){
		_id = id;
	}

	public int getId(){
		return _id;
	}
	
	public String getName(){
		return _name;
	} 
	
	public void setName(String name){
		this._name = name;
	}
	
	public JSONObject getContent () {
		return _content;
	}
	
	public void setContent (JSONObject content){
		this._content = content;
	}

	public String getBackendService() {
		return mBackendService;
	}

	public void setBackendService(String backendService) {
		this.mBackendService = backendService;
	}

	public String getBackendDatabaseType() {
		return mBackendDatabaseType;
	}

	public void setBackendDatabaseType(String backendDatabaseType) {
		this.mBackendDatabaseType = backendDatabaseType;
	}

	public String getBackendDatabaseName() {
		return mBackendDatabaseName;
	}

	public void setBackendDatabaseName(String backendDatabaseName) {
		this.mBackendDatabaseName = backendDatabaseName;
	}

	public String getBackendServiceURL() {
		return mBackendServiceURL;
	}

	public void setBackendServiceURL(String backendServiceURL) {
		this.mBackendServiceURL = backendServiceURL;
	}
}
