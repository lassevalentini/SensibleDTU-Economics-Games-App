package dk.dtu.sensible.economicsgames;

import android.database.Cursor;

public class Game {
	public int id;
	public String type;
	public int started;
	public int opened;
	public int participants;
	
	
	public Game(int id, String type, int started, int opened, int participants) {
		super();
		this.id = id;
		this.type = type;
		this.started = started;
		this.opened = opened;
		this.participants = participants;
	}

	public Game(Cursor cursor) {
		super();
		cursor.moveToNext();
		this.id =           cursor.getInt(   cursor.getColumnIndex(CurrentGamesDatabaseHelper.GAME_ID));
		this.type =         cursor.getString(cursor.getColumnIndex(CurrentGamesDatabaseHelper.GAME_TYPE));
		this.started =      cursor.getInt(   cursor.getColumnIndex(CurrentGamesDatabaseHelper.GAME_STARTED));
		this.opened =       cursor.getInt(   cursor.getColumnIndex(CurrentGamesDatabaseHelper.GAME_OPENED));
		this.participants = cursor.getInt(   cursor.getColumnIndex(CurrentGamesDatabaseHelper.GAME_PARTICIPANTS));
	}

	public String gameTypeToDescriptiveString(boolean capitalize) {
		if (type.equalsIgnoreCase("pgg")) {
			return (capitalize ? "P" : "p")+"ublic good game";
		} else if (type.equalsIgnoreCase("dg")) {
			return (capitalize ? "D" : "D")+"ictator game";
		}
		return "unknown game";
	}

}
