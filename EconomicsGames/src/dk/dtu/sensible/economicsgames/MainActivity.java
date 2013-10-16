package dk.dtu.sensible.economicsgames;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dk.dtu.sensible.economicsgames.R;
import dk.dtu.sensible.economicsgames.RegistrationHandler.SensibleRegistrationStatus;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "AUTH_MainActivity";
	private static boolean serviceRunning = false;

	private List<MessageItem> listMsg;
	private MessagesAdapter listAdapter;
	private ListView listview;
	
	
	private void updateListView() {
		// Should probably use a SimpleCursorAdapter instead. But now it's working, and it's not going to be a performance issue.
		
		listMsg.clear();
		CurrentGamesDatabaseHelper dbHelper = new CurrentGamesDatabaseHelper(getApplicationContext());
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor results = db.query(CurrentGamesDatabaseHelper.TABLE_NAME, 
				new String[]{CurrentGamesDatabaseHelper.GAME_ID, // Selected cols
					CurrentGamesDatabaseHelper.GAME_TYPE,
					CurrentGamesDatabaseHelper.GAME_STARTED, 
					CurrentGamesDatabaseHelper.GAME_PARTICIPANTS}, 
				null, // Where clause - select all 
				null, // Selection args 
				null, // Group by
				null, // Having
				CurrentGamesDatabaseHelper.GAME_STARTED + " ASC"); // Order by started ascending
		
		
		for (int i = 0; i<results.getCount(); i++) {
			results.moveToNext();
			
			int idIndex = results.getColumnIndex(CurrentGamesDatabaseHelper.GAME_ID);
			int typeIndex = results.getColumnIndex(CurrentGamesDatabaseHelper.GAME_TYPE);
			int startedIndex = results.getColumnIndex(CurrentGamesDatabaseHelper.GAME_STARTED);
			int participantsIndex = results.getColumnIndex(CurrentGamesDatabaseHelper.GAME_PARTICIPANTS);
			
			
			listMsg.add(i, new MessageItem(
					results.getInt(idIndex),
					CurrentGamesDatabaseHelper.gameTypeToString(results.getString(typeIndex), true), 
					results.getLong(startedIndex), 
					"Participants: "+results.getInt(participantsIndex)));
			
		}
		
		listAdapter.notifyDataSetChanged();
	}

	private void setStatus(String status) {
		setStatus(status, false);
	}
	
	private void setStatus(String status, boolean error) {
		TextView statusView = (TextView) findViewById(R.id.mainStatus);
		statusView.setText(status);
		if (error) {
			statusView.setTextColor(getResources().getColor(R.color.status_error));
		} else {
			statusView.setTextColor(getResources().getColor(R.color.status_info));
		}
		// TODO: make a runnable that can wait 10 sec and remove the status. Pass status as a parameter so that it doesnt remove new statuses.
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		
		trustAllHosts(); // During devel - disables ssl warnings etc.

		listview = (ListView) findViewById(R.id.listMessages);
		listMsg = new ArrayList<MessageItem>();
		listAdapter = new MessagesAdapter(this, listMsg);
		listview.setAdapter(listAdapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				Intent dialogIntent = new Intent(getBaseContext(), PGGActivity.class);
		        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        
		        dialogIntent.putExtra("id", listMsg.get(pos).id);
		        
		        getApplication().startActivity(dialogIntent);
			}
		});
		
		updateListView();
		
	}

	
	@Override
	protected void onStart() {
		super.onStart();

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadListTask().execute(SharedConstants.DOMAIN_URL + 
            		"sensible-dtu/connectors/connector_economics/list/");
        } else {
            setStatus("No network connection available.");
        }
        
		if (!serviceRunning) {
			serviceRunning = true;
			LauncherReceiver.startService(this, RegistrationHandler.class);
		} else {
			Log.d(TAG, "Not starting the service again");
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


	class MessageItem {
		
		public MessageItem(int id, String title, long timestamp, String body) {
			this.id = id;
			this.title = title;
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTimeInMillis(timestamp * 1000);
			java.text.DateFormat dateFormat = DateFormat.getDateTimeInstance();
			this.date = dateFormat.format(cal.getTime());
			this.body = body;
			
			Log.d(TAG, timestamp + " => "+date);
		}
		
		int id;
		String title;
		String date;
		String body;
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
			
			final TextView tvBody = (TextView) viewMsg.findViewById(R.id.messageExtra);
			tvBody.setText(values.get(position).body);
			
			final TextView tvTitle = (TextView) viewMsg.findViewById(R.id.messageTitle);
			tvTitle.setText(values.get(position).title);
			
//			viewMsg.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View view) {
//					int pos = viewMsg.getPositionForView(view);
//					Intent dialogIntent = new Intent(getBaseContext(), PGGActivity.class);
//			        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			        
//			        dialogIntent.putExtra("id", 1);
//			        getApplication().startActivity(dialogIntent);
//				}
//			});
			return viewMsg;
		}
	}
	
	private class DownloadListTask extends AsyncTask<String, Integer, String> {
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
        	
        	
        	urls[0] += "?bearer_token="+RegistrationHandler.getSensibleToken(getApplicationContext());
        	Log.d(TAG, "Fetching "+urls[0]);
        	
//        	((ProgressBar) findViewById(R.id.mainProgressBar)).setVisibility(View.VISIBLE);
        	
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        
        
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        	Log.d(TAG, "/list received: "+result);
//        	((ProgressBar) findViewById(R.id.mainProgressBar)).setVisibility(View.INVISIBLE);
        	try {
				JSONObject o = new JSONObject(result);
				JSONArray current = o.getJSONArray("current");
				
				CurrentGamesDatabaseHelper dbHelper = new CurrentGamesDatabaseHelper(getApplicationContext());
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				
				for (int i = 0; i<current.length(); i++) {
					JSONObject entry = current.getJSONObject(i);
					ContentValues values = new ContentValues();
					values.put(CurrentGamesDatabaseHelper.GAME_ID, entry.getLong("id"));
					values.put(CurrentGamesDatabaseHelper.GAME_TYPE, entry.getString("type"));
					values.put(CurrentGamesDatabaseHelper.GAME_STARTED, entry.getInt("started"));
					values.put(CurrentGamesDatabaseHelper.GAME_OPENED, 0);
					values.put(CurrentGamesDatabaseHelper.GAME_PARTICIPANTS, entry.getInt("participants"));
					
					//SQLiteDatabase.CONFLICT_IGNORE means ignore if already there
					db.insertWithOnConflict(CurrentGamesDatabaseHelper.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
				}
				updateListView();
			} catch (JSONException e) {
				e.printStackTrace();
				setStatus("Error: "+result);
			}
       }
    }
	
	private String downloadUrl(String myurl) throws IOException {
	    
		InputStream is = null;

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
	        
	        // Stupid scanner trick to make it read the input string.
	        java.util.Scanner s = new java.util.Scanner(is, "utf-8").useDelimiter("\\A");
	        String result = s.hasNext() ? s.next() : "";
	        
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
