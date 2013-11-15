package dk.dtu.sensible.economicsgames.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;


public class UrlHelper {

	private static final String TAG = "UrlHelper";

	public static String post(String url, Map<String,String> data) throws IOException {
		trustAllHosts(); // During devel - trusts the certificates
		
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
	        

	        int response = conn.getResponseCode();
	        Log.d(TAG, "Response: "+response);
	        
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
		trustAllHosts(); // During devel - trusts the certificates
		
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
        conn.setConnectTimeout(20000 /* milliseconds */);
        return conn;
	}

	private static UrlEncodedFormEntity mapToFormEntity(Map<String, String> data) throws UnsupportedEncodingException {
		ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
		
        for (String key : data.keySet()) {
        	nameValuePairs.add(new BasicNameValuePair(key, data.get(key)));
		}
        
        return new UrlEncodedFormEntity(nameValuePairs, "utf-8");
	}
	


    private static void trustAllHosts() {
    	HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        	public boolean verify(String hostname, javax.net.ssl.SSLSession session) {
        		return true;
        	};
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override	
                public X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[] {};
                }

				@Override
                public void checkClientTrusted(X509Certificate[] chain,
                                String authType) throws CertificateException {
                }

				@Override
                public void checkServerTrusted(X509Certificate[] chain,
                                String authType) throws CertificateException {
                }
        } };

        // Install the all-trusting trust manager
        try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
                e.printStackTrace();
        }
    }
}
