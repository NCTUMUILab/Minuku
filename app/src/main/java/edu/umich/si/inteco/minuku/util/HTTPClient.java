package edu.umich.si.inteco.minuku.util;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


public class HTTPClient {

	/** Tag for logging. */
    private static final String LOG_TAG = "HTTPClient";
    
	/** The time it takes for client to timeout */
	public static final int HTTP_TIMEOUT = 30 * 1000; // millisecond
	
	/** Single instance of our HttpClient */
	private static DefaultHttpClient mHttpClient;

	static InputStream is = null;
	static StringBuilder sb=null;
	
	private static HttpClient getHttpClient(){
		if (mHttpClient == null){
			try {
				mHttpClient = new DefaultHttpClient();

				final HttpParams params = mHttpClient.getParams();
				HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
				HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
				ConnManagerParams.setTimeout(params, HTTP_TIMEOUT);
			}catch (Exception e){
				Log.e(LOG_TAG, e.getMessage()+"");
			}

		}
		return mHttpClient;
	}


    public static String postJSON(String url, String json) {

        InputStream inputStream = null;
        String result = "";
        HttpClient httpclient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(url);


        // 5. set json to StringEntity
        StringEntity se = null;
        try {
            se = new StringEntity(json);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 6. set httpPost Entity
        httpPost.setEntity(se);

        // 7. Set some headers to inform server about the type of the content
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        // 8. Execute POST request to the given URL
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpclient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 9. receive response as inputStream
        if (httpResponse != null) {
            try {
                inputStream = httpResponse.getEntity().getContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 10. convert inputstream to string
        if(inputStream != null)
            try {
                result = convertInputStreamToString(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        else
            result = "Did not work!";


        Log.d(LOG_TAG, "get http post result" + result);

        return  result;

    }

    private static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                Log.d(LOG_TAG, "[executeHttpPost] result is " + line );
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }


    //use HTTPAsyncTask to post data
    private static class HttpPOSTAsyncTask extends AsyncTask<String, Void, String> {

        private String mData;

        @Override
        protected String doInBackground(String... urls) {


            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.accumulate("name", "testDocument");
                jsonObject.accumulate("country", "testDocument");
                jsonObject.accumulate("twitter", "testDocument");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // 4. convert JSONObject to JSON to String
            String json = jsonObject.toString();


            return postJSON(urls[0], json);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

        }

        public void setData(String data) {
            mData = data;
        }


    }
	
	
	
}
