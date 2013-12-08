package dk.dtu.sensible.economicsgames;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import dk.dtu.sensible.economicsgames.util.UrlHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

public class PostAnswerTask extends AsyncTask<String, Integer, String> {
	
	private final String TAG = "PostAnswerTask";
	
	private Context context;
	private String id;
	private String answer;
	private int opened;
	
	public PostAnswerTask(Context context, String id, String answer, int opened) {
		super();
		this.context = context;
		this.id = id;
		this.answer = answer;
		this.opened = opened;
	}
	
    @Override
    protected String doInBackground(String... args) {
    	
    	String token = RegistrationHandler.getSensibleToken(context);
    	
    	Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("bearer_token", RegistrationHandler.getSensibleToken(context));
		dataMap.put("game_id", id);
		dataMap.put("answer", answer);
		dataMap.put("opened", Integer.toString(opened));
    	
    	if (token != null && token.length() > 0) {
    		try {
    			String response = UrlHelper.post(SharedConstants.CONNECTOR_URL+"answer/", dataMap);
    			
    			DatabaseHelper dbHelper = new DatabaseHelper(context);
		    	SQLiteDatabase db = dbHelper.getWritableDatabase();
		    	DatabaseHelper.removeAnswer(db, id);
		    	dbHelper.close();
		    	
            	return response;
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
    protected void onPostExecute(String result) {}
}
