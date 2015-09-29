package edu.umich.si.inteco.minuku.model;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A SessionDocument object is created for sending records of a Session to a web service through HTTP POST.
 * A SessionDocument is converted into the JSON format that matches the structure a Session Document in the MongoDB.
 * Because the purpose of creating a SessionDocument is to create JSONObject, many properties would be in
 * a JSONObject format
 * Created by Armuro on 6/20/14.
 */


public class SessionDocument {

    //the id field in a SessionDocument is a combination of deviceId and sessionId.
    private int _id = -1;

    //mPeriod is a human-readable time string, e.g. [06/16/2014 12:30:24 UTC - 06/16/2014 12:45:24 UTC]
    private String mPeriod;

    //startTime and endTime are String. When the strings are sent to the web service, they will be converted
    //into a Python dateTime format.
    private String startTime;
    private String endTime;

    //mRecords is a JSONObject that contains all records in the session

    //we directly retrieve this from the localDB, so it will be in JSON format
    private JSONObject AnnotationSetInJSON;
    private JSONObject mRecords;


    public SessionDocument (int sessionId) {
        this._id = sessionId;
    }

    /**
     * generate record JSONArray
     * @return
     */
    public static JSONArray recordsToJSONArray () {

        JSONArray recordsJSONArray = new JSONArray();

        return recordsJSONArray;

    }
}



    /* Example:

 {

    "task":{
        "id":1,
        "name":"Participatory Labeling"
    },
    "start_time":"2014-05-25 14:45:15 -0700",
    "end_time":"2014-05-25 14:50:15 -0700",
    "updated_time": 1403454593,
    "session":
    "device_id":"9437120392032",
    "annotationSet":
    [
    	{
    		"content":"walking",
    		"tag":["Participatory Labeling","label"]
    	},
    	{
    		"content":"walking on the street",
    		"tag":["Participatory Labeling","note"]
    	}
    ],
    "records":
    [
	    {
			"timestamp_hour": "ISODate(2014-5-25t14:00:00Z)",
	        "location":
	        {
	            "1": {
	                "1": {"lat":42.121, "lng":118.12}, "4": {"lat":42.121, "lng":118.12}

	            },
	            "2": {

	                 "2": {"lat":42.121, "lng":118.12}, "5": {"lat":42.121, "lng":118.12}
	            }
			},
	        "activity":{
	        	"1": {
	                "1": {"activity":"in_vehicle", "confidence":44}, "4": {"activity":"in_vehicle", "confidence":54}

	            },
	            "2": {

	                 "23": {"activity":"on_foot", "confidence":64}, "54": {"activity":"still", "confidence":54}
	            }
	        }
	    }

    ]

}
*/

