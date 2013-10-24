package dk.dtu.sensible.economicsgames;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import dk.dtu.sensible.economicsgames.Message.Type;
import dk.dtu.sensible.economicsgames.util.SerializationUtils;
import dk.dtu.sensible.economicsgames.util.SystemUiHider;
import dk.dtu.sensible.economicsgames.util.UrlHelper;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage.MessageClass;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class GameActivity extends Activity {


	private static final String TAG = "GameActivity";
	private Game game;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        Log.d(TAG, extras.keySet().toString());
        game = (Game) SerializationUtils.deserialize(extras.getByteArray("game"));
        
        Log.d(TAG, "Id: "+game.id);
        Log.d(TAG, "Type: "+game.gameTypeToDescriptiveString(false));
        Log.d(TAG, "extras: "+extras.keySet());
        
        switch(game.type) {
        	case pgg:
	        	setContentView(R.layout.pgg_layout);
	        	TextView text = (TextView)findViewById(R.id.fullscreen_content);
	        	text.setText(String.format(getResources().getString(R.string.pgg_description), extras.getInt("participants")));
	            bindKeepButton();
	            bindShareButton();
	            break;
	            
	        case dg_proposer:
	        	setContentView(R.layout.dg_proposer_layout);
	            bindKeepButton();
	            bindGiveButton();
	            bindSplitButton();
	            break;
							
	        case dg_responder:
	        	setContentView(R.layout.dg_responder_layout);
	        	bindOkButton();
	        	break;
        	
        } 
        
        // TODO: Explanation activity that is shown the first time and then a button to open again.
        
        // TODO:  W/SQLiteConnectionPool(1404): A SQLiteConnection object for database '/data/data/dk.dtu.sensible.economicsgames/databases/economics_games_db' was leaked!  Please fix your application to end transactions in progress properly and to close the database when it is no longer needed.
        // bør være fixet
    	
        
        CurrentGamesDatabaseHelper dbHelper = new CurrentGamesDatabaseHelper(getApplicationContext());
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	CurrentGamesDatabaseHelper.updateGame(db, game.id, game.typeToString(), game.started, (int)(System.currentTimeMillis()*1000), game.participants);
        dbHelper.close();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
    }
    
    private void answer(String answer) {
    	(new PostResponseTask()).execute(""+game.id, answer);
    	CurrentGamesDatabaseHelper dbHelper = new CurrentGamesDatabaseHelper(getApplicationContext());
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	CurrentGamesDatabaseHelper.removeGame(db, game.id);
    	dbHelper.close();
		finish();
    }

    private void bindOkButton() {
    	Button keepButton = (Button) findViewById(R.id.okButton);
        keepButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
    }
    
    private void bindSplitButton() {
    	Button keepButton = (Button) findViewById(R.id.splitButton);
        keepButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				answer("split");
			}
		});
    }


    private void bindGiveButton() {
        Button shareButton = (Button) findViewById(R.id.giveButton);
        shareButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				answer("give");
			}
		});
    }
    
    private void bindKeepButton() {
    	Button keepButton = (Button) findViewById(R.id.keepButton);
        keepButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				answer("keep");
			}
		});
    }
    
    private void bindShareButton() {
        Button shareButton = (Button) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				answer("share");
			}
		});
    }
    
    private class PostResponseTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... args) {
        	String id = args[0];
        	String answer = args[1];
        	String token = RegistrationHandler.getSensibleToken(getApplicationContext());
        	
        	Map<String, String> dataMap = new HashMap<String, String>();
    		dataMap.put("bearer_token", RegistrationHandler.getSensibleToken(getApplicationContext()));
    		dataMap.put("id", id);
    		dataMap.put("answer", answer);
        	
        	if (token != null && token.length() > 0) {
        		try {
	                return UrlHelper.post(SharedConstants.CONNECTOR_URL+"answer/", dataMap);
	            } catch (IOException e) {
	            	Log.e(TAG, "Error posting "+dataMap+" to "+SharedConstants.CONNECTOR_URL+"answer/: "+e.getMessage());
					e.printStackTrace();
	                return "Unable to retrieve web page. URL may be invalid.";
	            }
        	} 
        	Log.e(TAG, "Not authenticated yet. Tried posting "+dataMap+" to "+SharedConstants.CONNECTOR_URL+"answer/");
        	return "Not authenticated yet"; 
        }
        

        @Override
        protected void onPostExecute(String result) {
        	
       }
    }
}
