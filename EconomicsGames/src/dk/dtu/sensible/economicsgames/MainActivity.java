package dk.dtu.sensible.economicsgames;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import dk.dtu.sensible.economicsgames.util.SerializationUtils;
import dk.dtu.sensible.economicsgames.util.UrlHelper;

import android.app.Activity;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "AUTH_MainActivity";
	private static boolean serviceRunning = false;
	

	private List<MessageItem> listMsg;
	private MessagesAdapter listAdapter;
	
	private List<MessageItem> listCodes;
	private MessagesAdapter listCodesAdapter;
	
	private long lastUpdate = 0;
		
	@Override
	protected void onStart() {
		super.onStart();
		
		fetchList();
        
		startService();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		
		trustAllHosts(); // During devel - disables ssl warnings etc.
		
		ListView gamesView = (ListView) findViewById(R.id.listMessages);
		listMsg = new ArrayList<MessageItem>();
		listAdapter = new MessagesAdapter(this, listMsg);
		gamesView.setAdapter(listAdapter);
		
		gamesView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				Log.d(TAG, "pressed pos: "+pos+" type: "+listMsg.get(pos).game.type);
				Intent dialogIntent = new Intent(getBaseContext(), GameActivity.class);
		        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        
		        dialogIntent.putExtra("game", SerializationUtils.serialize(listMsg.get(pos).game));
		        
		        getApplication().startActivity(dialogIntent);
			}
		});
		
		ListView codesView = (ListView) findViewById(R.id.listCodes);
		listCodes = new ArrayList<MessageItem>();
		listCodesAdapter = new MessagesAdapter(this, listCodes);
		codesView.setAdapter(listCodesAdapter);
		
//		
//		CurrentGamesDatabaseHelper dbHelper = new CurrentGamesDatabaseHelper(getApplicationContext());
//		SQLiteDatabase db = dbHelper.getReadableDatabase();
//		Cursor cursor = CurrentGamesDatabaseHelper.getGames(db);
//		dbHelper.close();
//		SimpleCursorAdapter codesAdapter = new SimpleCursorAdapter(this, 
//				R.layout.messageitem_layout, 
//				cursor, 
//				new String[]{CurrentGamesDatabaseHelper.GAME_TYPE, CurrentGamesDatabaseHelper.GAME_STARTED, CurrentGamesDatabaseHelper.GAME_PARTICIPANTS}, 
//				new int[]{R.id.messageTitle, R.id.messageDate, R.id.messageExtra},
//				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER); 
//		
//		ListView codesView = (ListView) findViewById(R.id.listCodes);
//		codesView.setAdapter(codesAdapter);
		
		updateViews();
		
		// TODO: display spinner if not authenticated.
	}


	private void updateViews() {
		TextView topTextView = (TextView) findViewById(R.id.mainTopText);
		if (RegistrationHandler.getSensibleRegistrationStatus(getApplicationContext()) == SensibleRegistrationStatus.REGISTERED) {
			topTextView.setText("Current games");
			
			TextView codesTextView = (TextView) findViewById(R.id.mainSecondText);
			codesTextView.setText("Codes won");
			
			updateListViews();
		} else {
			topTextView.setText("Please wait.");
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
	@Override
	protected void onResume() {
//		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
//				new IntentFilter(GcmBroadcastReceiver.EVENT_MSG_RECEIVED));
		super.onResume();
		startService(); // Make sure to be logged in even if something went wrong.
		setStatus("");
		
		fetchList();
		updateViews();
	}


	
	private void updateListViews() {
		// Should probably use a SimpleCursorAdapter instead. But now it's working, and it's not going to be a performance issue.
		
		DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor games = DatabaseHelper.getGames(db);

		listMsg.clear();
		for (int i = 0; i<games.getCount(); i++) {
			Game game = new Game(games); // also moves cursor to next
			
			listMsg.add(i, new MessageItem(
					game.gameTypeToDescriptiveString(true), 
					game.started, 
					"Participants: "+game.participants,
					game,
					R.drawable.game));
			
		}
		

		Cursor codes = DatabaseHelper.getCodes(db);
		
		listCodes.clear();
		for (int i = 0; i<codes.getCount(); i++) {
			codes.moveToNext();
			
			listCodes.add(i, new MessageItem(
					codes.getString(codes.getColumnIndex(DatabaseHelper.CODES_CODE)), 
					codes.getInt(codes.getColumnIndex(DatabaseHelper.CODES_TIMESTAMP)), 
					""));
			
		}
		
		dbHelper.close();
		listAdapter.notifyDataSetChanged();
		listCodesAdapter.notifyDataSetChanged();
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
	
	private void fetchList() {
		
		
		if (lastUpdate < (System.currentTimeMillis() - 5*60*1000)) {// 5 min
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	        if (networkInfo != null && networkInfo.isConnected()) {
	            new DownloadListTask().execute();
	        } else {
	            setStatus("No network connection available.");
	        }
		}
	}
	
	
	private void startService() {
		if (!serviceRunning) {
			serviceRunning = true;
			LauncherReceiver.startService(this, RegistrationHandler.class);
		} else {
			Log.d(TAG, "Not starting the service again");
		}
	}


	class MessageItem {

		String title;
		String date;
		String body;
		Game game;
		int image = R.drawable.money;

		public MessageItem(String title, long timestamp, String body, Game game) {
			this(title, timestamp, body);
			this.game = game;
		}
		
		public MessageItem(String title, long timestamp, String body, Game game, int image) {
			this(title, timestamp, body, game);
			this.image = image;
		}
		
		public MessageItem(String title, long timestamp, String body, int image) {
			this(title, timestamp, body);
			this.image = image;
		}
		
		public MessageItem(String title, long timestamp, String body) {
			this.title = title;
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTimeInMillis(timestamp * 1000);
			java.text.DateFormat dateFormat = DateFormat.getDateTimeInstance();
			this.date = dateFormat.format(cal.getTime());
			this.body = body;
			
//			Log.d(TAG, timestamp + " => "+date);
		}
		
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
			
			MessageItem messageItem = values.get(position);
			
//			Set image
			final ImageView viewImage = (ImageView) viewMsg.findViewById(R.id.imgCollapse);
			viewImage.setImageResource(messageItem.image);
			
			final TextView tvDate = (TextView) viewMsg.findViewById(R.id.messageDate);
			tvDate.setText(messageItem.date);
			
			final TextView tvBody = (TextView) viewMsg.findViewById(R.id.messageExtra);
			tvBody.setText(messageItem.body);
			
			final TextView tvTitle = (TextView) viewMsg.findViewById(R.id.messageTitle);
			tvTitle.setText(messageItem.title);
			
			return viewMsg;
		}
	}
	
	private class DownloadListTask extends AsyncTask<Void, Integer, String> {
		
        @Override
        protected String doInBackground(Void... v) {
        	Log.d(TAG, "Registration status: "+ RegistrationHandler.getSensibleRegistrationStatus(getApplicationContext()));
        	long startTime = System.currentTimeMillis();
        	
        	try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {}
        	
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
        	
        	String token = RegistrationHandler.getSensibleToken(getApplicationContext());
        	
        	Map<String, String> getMap = new HashMap<String, String>();
    		getMap.put("bearer_token", token);
    		
        	if (token != null && token.length() > 0) {
        		
	//        	((ProgressBar) findViewById(R.id.mainProgressBar)).setVisibility(View.VISIBLE);
	        	
	            try {
	                return UrlHelper.get(SharedConstants.CONNECTOR_URL+"list/", getMap);
	            } catch (IOException e) {
	            	Log.e(TAG, "Error GETing "+getMap+" from "+SharedConstants.CONNECTOR_URL+"list/: "+e.getMessage());
					e.printStackTrace();
	                return "Unable to retrieve web page. URL may be invalid.";
	            }
        	} 
        	Log.e(TAG, "Not authenticated yet. Tried GETing "+getMap+" from "+SharedConstants.CONNECTOR_URL+"list/");
        	return "Not authenticated yet"; 
        }
        
        
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        	Log.d(TAG, "/list received: "+result);
//        	((ProgressBar) findViewById(R.id.mainProgressBar)).setVisibility(View.INVISIBLE);
        	try {
				JSONObject o = new JSONObject(result);
				
				DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				

				JSONArray current = o.getJSONArray("current");
				// Current ids - for removing non-current games.
				String[] ids = new String[current.length()];
				
				for (int i = 0; i<current.length(); i++) {
					JSONObject entry = current.getJSONObject(i);
					
					ids[i] = entry.getString("_id");
					
					Game game = DatabaseHelper.getGame(db, entry.getString("_id"));
					
					// Update some values if game exists (the "INSERT ... ON DUPLICATE KEY UPDATE" in sqlite is not flexible)
					if (game == null) {
						DatabaseHelper.insertGame(db, entry.getString("_id"), entry.getString("type"), entry.getInt("started"), 0, entry.getInt("participants"));
					} else {					
						DatabaseHelper.updateGame(db, entry.getString("_id"), entry.getString("type"), entry.getInt("started"), game.opened, entry.getInt("participants"));
					}
				}
				
				DatabaseHelper.removeGamesNotIn(db, ids);
				
				lastUpdate = System.currentTimeMillis();
				
				JSONArray codeArray = o.getJSONArray("codes");
				
				String[] codes = new String[codeArray.length()];
				
				for (int i = 0; i<codeArray.length(); i++) {
					JSONObject entry = codeArray.getJSONObject(i);
					codes[i] = entry.getString("code");
					DatabaseHelper.insertCode(db, codes[i], entry.getInt("timestamp"));
				}

				DatabaseHelper.removeCodesNotIn(db, codes);
				
				dbHelper.close();
				
				updateViews();
			} catch (JSONException e) {
				e.printStackTrace();
				setStatus("Error: "+result);
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
