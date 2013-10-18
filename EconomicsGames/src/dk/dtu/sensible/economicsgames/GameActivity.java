package dk.dtu.sensible.economicsgames;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import dk.dtu.sensible.economicsgames.util.SystemUiHider;
import dk.dtu.sensible.economicsgames.util.UrlHelper;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class GameActivity extends Activity {


	private static final String TAG = "PGG";
	private int id;
	private String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        Log.d(TAG, extras.keySet().toString());
        id = extras.getInt("id");
        type = extras.getString("type");
        Log.d(TAG, "Id: "+id);
        Log.d(TAG, "Type: "+type);
        
        if (type.equalsIgnoreCase("pgg")) {
        	setContentView(R.layout.pgg_layout);

            
        } else if (type.equalsIgnoreCase("dg")) {
        	setContentView(R.layout.dg_layout);
        	
        } 
        

    	Button keepButton = (Button) findViewById(R.id.keepButton);
        keepButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				answer("keep");
			}
		});
        
        Button shareButton = (Button) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				answer("share");
			}
		});
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
    }
    
    private void answer(String answer) {
    	(new PostResponseTask()).execute(""+id, answer);
		finish();
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
