package dk.dtu.sensible.economicsgames;

import java.io.Serializable;

import android.database.Cursor;

public class Game extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3880123716585106387L;
	
	public int id;
	public int started;
	public int opened;
	public int participants;
	
	
	public Game(int id, String type, int started, int opened, int participants) {
		super();
		this.id = id;
		this.type = typeFromString(type);
		this.started = started;
		this.opened = opened;
		this.participants = participants;
	}

	public Game(Cursor cursor) {
		super();
		cursor.moveToNext();
		this.id =           cursor.getInt(   cursor.getColumnIndex(CurrentGamesDatabaseHelper.GAME_ID));
		this.type = typeFromString(cursor.getString(cursor.getColumnIndex(CurrentGamesDatabaseHelper.GAME_TYPE)));
		this.started =      cursor.getInt(   cursor.getColumnIndex(CurrentGamesDatabaseHelper.GAME_STARTED));
		this.opened =       cursor.getInt(   cursor.getColumnIndex(CurrentGamesDatabaseHelper.GAME_OPENED));
		this.participants = cursor.getInt(   cursor.getColumnIndex(CurrentGamesDatabaseHelper.GAME_PARTICIPANTS));
	}
	

	public String gameTypeToDescriptiveString(boolean capitalize) {
		switch (type) {
			case pgg:
				return (capitalize ? "P" : "p")+"ublic good game";
				
			case dg_proposer:
				return (capitalize ? "D" : "D")+"ictator game";
				
			case dg_responder:
				return (capitalize ? "D" : "D")+"ictator game";
		}
		return "unknown game";
	}
}
