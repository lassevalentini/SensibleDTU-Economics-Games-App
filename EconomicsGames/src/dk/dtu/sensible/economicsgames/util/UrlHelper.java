package dk.dtu.sensible.economicsgames.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;


public class UrlHelper {

	private static final String TAG = "UrlHelper";

	public static String post(String url, Map<String,String> data) throws IOException {
		Log.d(TAG, "POSTing to "+url);
		
		InputStream is = null;
		OutputStream os = null;
		
	    try {
	        HttpURLConnection conn = getConn(url);
	        conn.setRequestMethod("POST");
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        
	        
	        UrlEncodedFormEntity form = mapToFormEntity(data);
	        
	        // Starts the query
	        conn.connect();
	        os = conn.getOutputStream();
	        Log.d(TAG, url+" "+form.toString());
	        form.writeTo(os);
	        os.close();
	        
	        is = conn.getInputStream();

	        String result = readInput(is);
	        
	        conn.disconnect();
	        
	        return result;
	     // Makes sure that the InputStream is closed after the app is
	    // finished using it.
	    } finally {
	        if (is != null) {
	            is.close();
	        } 
	        if (os != null) {
	            os.close();
	        } 
	    }
	}
	

	public static String get(String url) throws IOException {
		Log.d(TAG, "GETing "+url);
		
		InputStream is = null;
		
	    try {
	    	HttpURLConnection conn = getConn(url);
	    	
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        
	        // Starts the query
	        conn.connect();
//	        int response = conn.getResponseCode();
	        is = conn.getInputStream();
	        
	        String result = readInput(is);
	        
	        conn.disconnect();
	        
	        return result;
	        
	    // Makes sure that the InputStream is closed after the app is
	    // finished using it.
	    } finally {
	        if (is != null) {
	            is.close();
	        } 
	    }
	}
	
	public static String get(String url, Map<String,String> data) throws IOException {
		UrlEncodedFormEntity form = mapToFormEntity(data);
		
		return get(url+"?"+readInput(form.getContent()));
	}
	
	
	private static String readInput(InputStream is) {
		// Stupid scanner trick to make it read the input string.
        java.util.Scanner s = new java.util.Scanner(is, "utf-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
	}

	private static HttpURLConnection getConn(String myurl) throws IOException {
		URL url = new URL(myurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        return conn;
	}

	private static UrlEncodedFormEntity mapToFormEntity(Map<String, String> data) throws UnsupportedEncodingException {
		ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
		
        for (String key : data.keySet()) {
        	nameValuePairs.add(new BasicNameValuePair(key, data.get(key)));
		}
        
        return new UrlEncodedFormEntity(nameValuePairs, "utf-8");
	}
}
