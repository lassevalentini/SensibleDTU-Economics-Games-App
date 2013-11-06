package dk.dtu.sensible.economicsgames;

import android.database.Cursor;
import android.os.Bundle;

public class Game extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3880123716585106387L;
	
	public String id;
	public int started;
	public int opened;
	public int participants;
	
	
	public Game(String id, String type, int started, int opened, int participants) {
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
		this.id =           cursor.getString(cursor.getColumnIndex(DatabaseHelper.GAME_ID));
		this.type = typeFromString(cursor.getString(cursor.getColumnIndex(DatabaseHelper.GAME_TYPE)));
		this.started =      cursor.getInt(   cursor.getColumnIndex(DatabaseHelper.GAME_STARTED));
		this.opened =       cursor.getInt(   cursor.getColumnIndex(DatabaseHelper.GAME_OPENED));
		this.participants = cursor.getInt(   cursor.getColumnIndex(DatabaseHelper.GAME_PARTICIPANTS));
	}
	
	public Game(Bundle bundle) {
		this(bundle.getString("game-id"), bundle.getString("game-type"), bundle.getInt("game-started"), 0, bundle.getInt("game-participants"));
	}
	

	public String gameTypeToDescriptiveString(boolean capitalize) {
		switch (type) {
			case pdg:
				return (capitalize ? "P" : "p")+"risoners dilemma game";
				
			case dg_proposer:
				return (capitalize ? "D" : "D")+"ictator game";
				
			case dg_responder:
				return (capitalize ? "D" : "D")+"ictator game";
		}
		return "unknown game";
	}
}
