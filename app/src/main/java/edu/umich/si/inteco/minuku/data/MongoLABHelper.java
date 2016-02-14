package edu.umich.si.inteco.minuku.data;

import edu.umich.si.inteco.minuku.util.DatabaseNameManager;

/**
 * Created by Armuro on 2/11/16.
 */
public class MongoLabHelper {

    /**MONGODBLAB**/

    public static String MongoAPIKey = "NA";

    public static final String MONGOLAB_APIKEY_PARAMETER="apiKey";

    public static final String MONGOLAB_DATABASE = "minuku";
    public static final String MONGOLAB_URL = "https://api.mongolab.com/api/1/databases/";

    public static final String MONGOLAB_SORT_PARAMETER = "s"; //specify the order in which to sort each specified field (1- ascending; -1 - descending)
    public static final String MONGOLAB_SINGLE_DOCUMENT_PARAMETER = "fo"; //return a single document from the result set (same as findOne() using the mongo shell
    public static final String MONGOLAB_FILTER_PARAMETER = "f"; //specify the set of fields to include or exclude in each document (1 - include; 0 - exclude)
    public static final String MONGOLAB_COUNT_PARAMETER = "c"; //return the result count for this query
    public static final String MONGOLAB_NUMBER_SKIP_PARAMETER = "sk"; //specify the number of results to skip in the result set; useful for paging
    public static final String MONGOLAB_QUERY_PARAMETER = "q"; //restrict results by the specified JSON query
    public static final String MONGOLAB_LIMIT_PARAMETER = "l"; //specify the limit for the number of results (default is 1000)

    public static final int MONGOLAB_SORT_ASCENDING_PARAMETER = 1;
    public static final int MONGOLAB_SORT_DESCENDING_PARAMETER = -1;


    public static String getQueryOfSynLatestDocumentURL(String database, String collection) {

        return  ( MONGOLAB_URL + database + "/" + "collections/" + collection + "/?" +

                //sort in the descender order
                MONGOLAB_SORT_PARAMETER + "=" + "{\"" + DatabaseNameManager.MONGO_DB_DOCUMENT_PROPERTIES_TIMESTAMP_HOUR + "\":" + MONGOLAB_SORT_DESCENDING_PARAMETER + "}" +

                "&" + MONGOLAB_LIMIT_PARAMETER + "=" + 1 +
                "&" + MONGOLAB_APIKEY_PARAMETER + "=" + MongoAPIKey);


    }


    public static String postDocumentURL(String database, String collection) {

        return  ( MONGOLAB_URL + database + "/" + "collections/" + collection + "/?" + MONGOLAB_APIKEY_PARAMETER + "=" + MongoAPIKey);

    }

    public static void setMongolabApikey (String key) {

        MongoAPIKey = key;
    }



}
