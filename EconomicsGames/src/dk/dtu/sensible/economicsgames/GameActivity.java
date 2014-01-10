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
        	case pdg:
	        	setContentView(R.layout.pdg_layout);
	        	TextView text = (TextView)findViewById(R.id.fullscreen_content);
	        	text.setText(String.format(getResources().getString(R.string.pdg_description), extras.getInt("participants")));
	            bindKeepButton();
	            bindGiveButton();
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
    	
        game.opened = (int)(System.currentTimeMillis()/1000);
        
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	DatabaseHelper.updateGame(db, game.id, game.typeToString(), game.started, game.opened, game.participants);
        dbHelper.close();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
    }
    
    private void answer(String answer) {
    	(new PostAnswerTask(getApplicationContext(), game.id, answer, game.opened)).execute();
    	DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	DatabaseHelper.insertAnswer(db, game.id, answer, game.opened);
    	DatabaseHelper.setGameAnswered(db, game.id);
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
    
//    private void bindShareButton() {
//        Button shareButton = (Button) findViewById(R.id.shareButton);
//        shareButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				answer("share");
//			}
//		});
//    }
    
}
