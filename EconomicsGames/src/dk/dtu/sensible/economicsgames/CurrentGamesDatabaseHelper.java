package dk.dtu.sensible.economicsgames;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CurrentGamesDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "current_games";
    public static final String GAME_ID = "id";
    public static final String GAME_TYPE = "type";
    public static final String GAME_PARTICIPANTS = "participants";
    public static final String GAME_STARTED = "started";
    public static final String GAME_OPENED = "opened";
    
    private static final String TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                    	GAME_ID + " INTEGER PRIMARY KEY, " +
                    	GAME_TYPE + " TEXT, " +
                    	GAME_PARTICIPANTS + " INTEGER, " +
                    	GAME_STARTED + " INTEGER, " +
                    	GAME_OPENED + " INTEGER);";


	CurrentGamesDatabaseHelper(Context context) {
        super(context, SharedConstants.DATABASE_NAME, null, DATABASE_VERSION);
    }
	

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	public static String gameTypeToString(String type, boolean capitalize) {
		if (type.equals("pgg")) {
			return (capitalize ? "P" : "p")+"ublic goods game";
		} else if (type.equals("dg")) {
			return (capitalize ? "D" : "D")+"ictator game";
		}
		return "unknown game";
	}

}
