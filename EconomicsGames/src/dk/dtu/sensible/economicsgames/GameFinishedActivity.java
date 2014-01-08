package dk.dtu.sensible.economicsgames;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
		Button okButton = (Button)findViewById(R.id.okButton);
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(extras.getInt("timestamp") * 1000);
		java.text.DateFormat dateFormat = DateFormat.getDateTimeInstance();
		String date = dateFormat.format(cal.getTime());
		
		description.setText("Game ended on "+date);
		codeTextBox.setText(extras.getString("code"));
		codeTextBox.addTextChangedListener(new NoEditsTextWatcher(extras.getString("code")));
		
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.game_finished, menu);
//		return true;
//	}
	
	private class NoEditsTextWatcher implements TextWatcher {
		private String text;
		public NoEditsTextWatcher(String text) {
			this.text = text;
		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void afterTextChanged(Editable s) {
			if (s.toString().equals(text)) {
				return;
			}
			s.replace(0, s.length(), text);
		}
	}
}
