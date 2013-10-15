package dk.dtu.sensible.economicsgames;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dk.dtu.sensible.economicsgames.R;
import dk.dtu.sensible.economicsgames.RegistrationHandler.SensibleRegistrationStatus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "AUTH_MainActivity";
	private static boolean serviceRunning = false;

	private List<MessageItem> listMsg;
	private MessagesAdapter listAdapter;
	private ListView listview;
	
	
	private void addMessage(String title, long timestamp, String msg) {
		listMsg.add(0, new MessageItem(title, timestamp, msg));
		listAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		if (!serviceRunning) {
			serviceRunning = true;
			LauncherReceiver.startService(this, RegistrationHandler.class);
		} else {
			Log.d(TAG, "Not starting the service again");
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

//		ImageView imgStatus = (ImageView) findViewById(R.id.imgStatus);
//		TextView txtFilesCount = (TextView) findViewById(R.id.textFilesCount);
//
//		int filesCount = getFilesCount();
//		if (filesCount > 0) {
//			imgStatus.setImageResource(R.drawable.status_problem);
//		} else {
//			imgStatus.setImageResource(R.drawable.status_ok);
//		}
//		txtFilesCount.setText("" + filesCount);

		listview = (ListView) findViewById(R.id.listMessages);
		listMsg = new LinkedList<MessageItem>();
		listAdapter = new MessagesAdapter(this, listMsg);
		listview.setAdapter(listAdapter);
		
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
//            new DownloadListTask().execute(SharedConstants.DOMAIN_URL + 
//            		"sensible-dtu/connectors/connector_economics/list/");
        } else {
            addMessage("No network connection available.", System.currentTimeMillis()/1000, "");
        }
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    Log.d(TAG, "Configuration changed");
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
//	@Override
//	protected void onPause() {
//		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
//		super.onPause();
//	}
//	
//	@Override
//	protected void onResume() {
//		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
//				new IntentFilter(GcmBroadcastReceiver.EVENT_MSG_RECEIVED));
//		super.onResume();
//	}
//
//	private int getFilesCount() {
//		String[] files = null;
//		try {
//			files = new File(Environment.getExternalStorageDirectory(), "dk.dtu.imm.datacollector2013/mainPipeline/archive").list();
//		} catch(Exception ignore) {
//			
//		}
//		return files != null ? files.length : 0;
//	}

	class MessageItem {

		public MessageItem(String title, long timestamp, String body) {
			this.title = title;
			GregorianCalendar.getInstance().setTimeInMillis(timestamp * 1000);
			java.text.DateFormat dateFormat = DateFormat.getDateTimeInstance(); //new SimpleDateFormat("dd MMM yyyy, hh:mm");
			this.date = dateFormat.format(GregorianCalendar.getInstance().getTime());
			this.body = body;
		}

		String title;
		String date;
		String body;
		boolean collapsed = true;
	}

	class MessagesAdapter extends ArrayAdapter<MessageItem> {
		private final Context context;
		private final List<MessageItem> values;

		public MessagesAdapter(Context context, List<MessageItem> values) {
			super(context, R.layout.messageitem_layout, values);
			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View viewMsg = inflater.inflate(R.layout.messageitem_layout, parent, false);
			final TextView tvDate = (TextView) viewMsg.findViewById(R.id.messageDate);
			tvDate.setText(values.get(position).date);
			final TextView tvBody = (TextView) viewMsg.findViewById(R.id.messageBody);
			tvBody.setText(values.get(position).body);
			tvBody.setVisibility(values.get(position).collapsed ? View.GONE : View.VISIBLE);
			final ImageView imgCollapse = (ImageView) viewMsg.findViewById(R.id.imgCollapse);
			imgCollapse.setImageResource(values.get(position).collapsed ? R.drawable.arrow_down : R.drawable.arrow_up);
			final TextView tvTitle = (TextView) viewMsg.findViewById(R.id.messageTitle);
			tvTitle.setText(values.get(position).title);
			viewMsg.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					boolean newCollapsedStatus = !values.get(position).collapsed;
					for(MessageItem msi : values) {
						msi.collapsed = true;
					}
					values.get(position).collapsed = newCollapsedStatus;
					listview.invalidateViews();
				}
			});
			return viewMsg;
		}
	}
	
	private class DownloadListTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
        	Log.d(TAG, "Registration status: "+ RegistrationHandler.getSensibleRegistrationStatus(getApplicationContext()));
        	long startTime = System.currentTimeMillis();
        	
        	// Wait up to 30 seconds for the registration to complete
        	while (!RegistrationHandler.getSensibleRegistrationStatus(getApplicationContext()).equals(SensibleRegistrationStatus.REGISTERED)
        			&& System.currentTimeMillis() - startTime < 10*1000) {
        		try {
        			synchronized (RegistrationHandler.registrationLock) {
        				RegistrationHandler.registrationLock.wait(500);
					}
				} catch (InterruptedException e) {}
        	}
        	
        	Log.d(TAG, "Registration status after: "+ RegistrationHandler.getSensibleRegistrationStatus(getApplicationContext()));
        	
        	Log.d(TAG, "authprefs: "+RegistrationHandler.getAuthPreferences(getApplicationContext()).getAll().toString());
            
        	Log.d(TAG, "token: "+RegistrationHandler.getSensibleToken(getApplicationContext()));
            
        	
        	urls[0] += "?bearer_token="+RegistrationHandler.getSensibleToken(getApplicationContext());
        	Log.d(TAG, "Fetching "+urls[0]);
            
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        	try {
				JSONObject o = new JSONObject(result);
				JSONArray current = o.getJSONArray("current");
				
				for (int i = 0; i<current.length(); i++) {
					JSONObject entry = current.getJSONObject(i);
		            addMessage(entry.getString("title"), entry.getLong("timestamp"), entry.getString("body"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
	            addMessage("Error", 0, result);
			}
       }
    }
	
	private String downloadUrl(String myurl) throws IOException {
	    
		InputStream is = null;
	    // Only display the first 500 characters of the retrieved
	    // web page content.
	    int len = 500;

        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        	public boolean verify(String hostname, javax.net.ssl.SSLSession session) {
        		return true;
        	};
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

	    try {
	        URL url = new URL(myurl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        // Starts the query
	        conn.connect();
	        int response = conn.getResponseCode();
	        Log.d(TAG, "list response: " + response);
	        is = conn.getInputStream();
	        
	        // Convert the InputStream into a string
	        Reader reader = new InputStreamReader(is, "UTF-8");        
	        char[] buffer = new char[len];
	        reader.read(buffer);
	        return new String(buffer);
	        
	    // Makes sure that the InputStream is closed after the app is
	    // finished using it.
	    } finally {
	        if (is != null) {
	            is.close();
	        } 
	    }
	}
}
