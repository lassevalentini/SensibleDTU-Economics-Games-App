package dk.dtu.sensible.economicsgames;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

public class GameFinishedActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_finished);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		
		TextView description = (TextView)findViewById(R.id.codeDescription);
		EditText codeTextBox = (EditText)findViewById(R.id.codeTextBox);
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(extras.getInt("timestamp") * 1000);
		java.text.DateFormat dateFormat = DateFormat.getDateTimeInstance();
		String date = dateFormat.format(cal.getTime());
		
		description.setText("Game ended on "+date);
		codeTextBox.setText(extras.getString("code"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_finished, menu);
		return true;
	}

}
