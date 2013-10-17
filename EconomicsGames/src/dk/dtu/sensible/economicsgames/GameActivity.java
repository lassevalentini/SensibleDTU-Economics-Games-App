package dk.dtu.sensible.economicsgames;

import dk.dtu.sensible.economicsgames.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
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

        	Button keepButton = (Button) findViewById(R.id.keepButton);
            keepButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			});
            
            Button shareButton = (Button) findViewById(R.id.shareButton);
            shareButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
			});
            
        } else if (type.equalsIgnoreCase("dg")) {
        	setContentView(R.layout.pgg_layout);
        	
        } 
        
        
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
    }


}
