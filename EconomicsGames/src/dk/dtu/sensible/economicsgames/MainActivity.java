package dk.dtu.sensible.economicsgames;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import dk.dtu.sensible.economicsgames.R;
import dk.dtu.sensible.economicsgames.RegistrationHandler.SensibleRegistrationStatus;
import dk.dtu.sensible.economicsgames.util.SerializationUtils;
import dk.dtu.sensible.economicsgames.util.UrlHelper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private static boolean serviceRunning = false;
	

	private List<ListItem> listMsg;
	private MessagesAdapter listAdapter;
	
//	private List<MessageItem> listCodes;
//	private MessagesAdapter listCodesAdapter;
	
	private long lastUpdate = 0;
	private BroadcastReceiver br;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    
	@Override
	protected void onStart() {
		super.onStart();
		
		startService();
		
		fetchList();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		
		checkPlayServices();
		
		ListView gamesView = (ListView) findViewById(R.id.listMessages);
		listMsg = new ArrayList<ListItem>();
		listAdapter = new MessagesAdapter(this, listMsg);
		gamesView.setAdapter(listAdapter);
		
		gamesView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				ListItem listItem = listMsg.get(pos);
				Log.d(TAG, "pressed pos: "+pos+" type: "+listItem);
				if (listItem instanceof GameItem) {
					GameItem gameItem = (GameItem) listItem;
					Intent dialogIntent = new Intent(getBaseContext(), GameActivity.class);
			        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			        
			        dialogIntent.putExtra("game", SerializationUtils.serialize(gameItem.game));
			        
			        getApplication().startActivity(dialogIntent);
				} else if (listItem instanceof CodeItem) {
					CodeItem codeItem = (CodeItem) listItem;
					Intent dialogIntent = new Intent(getBaseContext(), GameFinishedActivity.class);
			        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			        
			        dialogIntent.putExtra("code", codeItem.code);
			        
			        getApplication().startActivity(dialogIntent);
				}
			}
		});
		
		// Get a new list when re-registration is done
		br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                fetchList();
                
            }
        };
        registerReceiver(br, new IntentFilter(RegistrationHandler.REGISTRATION_DONE_INTENT) );
        
		updateViews();
		

    	DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	Cursor answers = DatabaseHelper.getAnswers(db);
    	
    	for (int i = 0; i < answers.getCount(); i++) {
    		answers.moveToNext();
    		String game_id = answers.getString(answers.getColumnIndex(DatabaseHelper.ANSWERS_GAME_ID));
    		String answer = answers.getString(answers.getColumnIndex(DatabaseHelper.ANSWERS_ANSWER));
    		int opened = answers.getInt(answers.getColumnIndex(DatabaseHelper.ANSWERS_OPENED));
    		
    		(new PostAnswerTask(getApplicationContext(), game_id, answer, opened)).execute();
		}
		
		// TODO: display spinner if not authenticated.
	}


	private void updateViews() {
		TextView topTextView = (TextView) findViewById(R.id.mainTopText);
		if (RegistrationHandler.getSensibleRegistrationStatus(getApplicationContext()) == SensibleRegistrationStatus.REGISTERED) {
//			topTextView.setText("Current games");
//			
//			TextView codesTextView = (TextView) findViewById(R.id.mainSecondText);
//			codesTextView.setText("Codes won");
			
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

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device is not supported.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}
	
	private void updateListViews() {
		// Should probably use a SimpleCursorAdapter instead. But now it's working, and it's not going to be a performance issue.
		
		listMsg.clear();
		
		DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor games = DatabaseHelper.getUnsentGames(db);
		
		if (games.getCount() > 0) {
			listMsg.add(new SectionHeaderItem("Current games"));
		} else {
			listMsg.add(new SectionHeaderItem("No games yet"));
		}
		
		for (int i = 0; i<games.getCount(); i++) {
			Game game = new Game(games); // also moves cursor to next
			
			listMsg.add(new GameItem(
					game.gameTypeToDescriptiveString(true), 
					game.started, 
					"",
					game,
					R.drawable.game));
			
		}
		

		Cursor codes = DatabaseHelper.getCodes(db);

		if (codes.getCount() > 0) {
			listMsg.add(new SectionHeaderItem("Vouchers"));
		}
		
		for (int i = 0; i<codes.getCount(); i++) {
			codes.moveToNext();
			
			listMsg.add(new CodeItem(
					codes.getString(codes.getColumnIndex(DatabaseHelper.CODES_CODE)), 
					codes.getInt(codes.getColumnIndex(DatabaseHelper.CODES_TIMESTAMP))));
			
		}
		
		dbHelper.close();
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
	
	private void fetchList() {
		
		if (!RegistrationHandler.getSensibleRegistrationStatus(getApplicationContext()).equals(
				SensibleRegistrationStatus.NOT_REGISTERED_NO_CODE) && // Dont fetch when the app is started the first time.
				lastUpdate < (System.currentTimeMillis() - 5*60*1000)) {// 5 min
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
	
	abstract class ListItem {}
	
	class SectionHeaderItem extends ListItem {
		String title;
		public SectionHeaderItem(String title) {
			this.title = title;
		}
		
	}
	
	class MessageItem extends ListItem {

		String title;
		String date;
		String body;
		int image = R.drawable.money;
		
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
		}
		
	}

	class GameItem extends MessageItem {

		Game game;

		public GameItem(String title, long timestamp, String body, Game game) {
			super(title, timestamp, body);
			this.game = game;
		}
		
		public GameItem(String title, long timestamp, String body, Game game, int image) {
			super(title, timestamp, body, image);
			this.game = game;
		}
	}
	class CodeItem extends MessageItem {

		String code;

		public CodeItem(String code, int timestamp) {
			super(code, timestamp, "");
			this.code = code;
		}
		
	}

	class MessagesAdapter extends ArrayAdapter<ListItem> {
		private final Context context;
		private final List<ListItem> values;

		public MessagesAdapter(Context context, List<ListItem> values) {
			super(context, R.layout.messageitem_layout, values);
			this.context = context;
			this.values = values;
			
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			final View viewMsg;
			
			ListItem listItem = values.get(position);
			
			if (listItem instanceof MessageItem) {
				MessageItem messageItem = (MessageItem) listItem;
				
				viewMsg = inflater.inflate(R.layout.messageitem_layout, parent, false);
				
	//			Set image
				final ImageView viewImage = (ImageView) viewMsg.findViewById(R.id.imgCollapse);
				viewImage.setImageResource(messageItem.image);
				
				final TextView tvDescription = (TextView) viewMsg.findViewById(R.id.messageDescription);
				tvDescription.setText(messageItem.date);
				
				final TextView tvBody = (TextView) viewMsg.findViewById(R.id.messageExtra);
				tvBody.setText(messageItem.body);
				
				final TextView tvTitle = (TextView) viewMsg.findViewById(R.id.messageTitle);
				tvTitle.setText(messageItem.title);
			} else if (listItem instanceof SectionHeaderItem) {
				SectionHeaderItem sectionItem = (SectionHeaderItem) listItem;
				
				viewMsg = inflater.inflate(R.layout.section_header, parent, false);
				
				final TextView title = (TextView) viewMsg.findViewById(R.id.sectionText);
				title.setText(sectionItem.title);
				
				viewMsg.setFocusable(false);
				viewMsg.setEnabled(false);
				
			} else {
				viewMsg = null;
			}
			return viewMsg;
		}
	}
	
	private class DownloadListTask extends AsyncTask<Void, Integer, String> {
		
        @Override
        protected String doInBackground(Void... v) {
        	
        	Log.d(TAG, "Downloadlist - Registration status: "+ RegistrationHandler.getSensibleRegistrationStatus(getApplicationContext()));
        	
        	String token = RegistrationHandler.getSensibleToken(getApplicationContext());
        	
        	Map<String, String> getMap = new HashMap<String, String>();
    		getMap.put("bearer_token", token);
    		
        	if (token != null && token.length() > 0) {	        	
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
						DatabaseHelper.updateGame(db, entry.getString("_id"), entry.getString("type"), entry.getInt("started"), game.opened, entry.getInt("participants"), game.answered);
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
	
	
}
