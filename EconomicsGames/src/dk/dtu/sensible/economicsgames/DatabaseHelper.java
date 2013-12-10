package dk.dtu.sensible.economicsgames;

import dk.dtu.sensible.economicsgames.Message.Type;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    public static final String GAMES_TABLE_NAME = "current_games";
    public static final String GAME_ID = "id";
    public static final String GAME_TYPE = "type";
    public static final String GAME_PARTICIPANTS = "participants";
    public static final String GAME_STARTED = "started";
    public static final String GAME_OPENED = "opened";
    public static final String GAME_ANSWERED = "answered";
    
    private static final String GAMES_TABLE_CREATE =
                "CREATE TABLE " + GAMES_TABLE_NAME + " (" +
                    	GAME_ID + " TEXT PRIMARY KEY NOT NULL, " +
                    	GAME_TYPE + " TEXT, " +
                    	GAME_PARTICIPANTS + " INTEGER, " +
                    	GAME_STARTED + " INTEGER, " +
                    	GAME_OPENED + " INTEGER, "+
                		GAME_ANSWERED +" INTEGER)";
    
    

    public static final String CODES_TABLE_NAME = "codes";
    public static final String CODES_CODE = "code";
    public static final String CODES_TIMESTAMP = "timestamp";
    
    private static final String CODES_TABLE_CREATE =
                "CREATE TABLE " + CODES_TABLE_NAME + " (" +
                		CODES_CODE + " TEXT PRIMARY KEY NOT NULL,"+
                		CODES_TIMESTAMP + " INTEGER)";
    

    public static final String ANSWERS_TABLE_NAME = "answers";
    public static final String ANSWERS_GAME_ID = "id";
    public static final String ANSWERS_ANSWER = "answer";
    public static final String ANSWERS_OPENED = "opened";
    
    private static final String ANSWERS_TABLE_CREATE =
                "CREATE TABLE " + ANSWERS_TABLE_NAME + " (" +
                		ANSWERS_GAME_ID + " TEXT PRIMARY KEY NOT NULL,"+
                		ANSWERS_ANSWER + " TEXT,"+
                		ANSWERS_OPENED + " INTEGER);";
    
    private static final String[] TABLES = new String[]{GAMES_TABLE_NAME, CODES_TABLE_NAME, ANSWERS_TABLE_NAME};
	private static final String TAG = "DatabaseHelper";
    
    // TODO: Let this class handle the db - make all the methods non-static (or make non-static versions)
    
	DatabaseHelper(Context context) {
        super(context, SharedConstants.DATABASE_NAME, null, DATABASE_VERSION);
    }
	

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GAMES_TABLE_CREATE);
        db.execSQL(CODES_TABLE_CREATE);
        db.execSQL(ANSWERS_TABLE_CREATE);
    }
    
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	this.onUpgrade(db, oldVersion, newVersion);
    }
    
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade("+oldVersion+", "+newVersion+")");
		for (int i = 0; i < TABLES.length; i++) {
			db.execSQL("DROP TABLE IF EXISTS "+TABLES[i]);
		}
		this.onCreate(db);
	}
	
	public static void insertAnswer(SQLiteDatabase db, String id, String answer, int opened) {
		db.beginTransaction();
		//SQLiteDatabase.CONFLICT_IGNORE means ignore if already there
		db.insertWithOnConflict(ANSWERS_TABLE_NAME, 
				null, 
				getAnswersValues(id, answer, opened), 
				SQLiteDatabase.CONFLICT_IGNORE);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public static Cursor getAnswers(SQLiteDatabase db) {
		Cursor results = db.query(ANSWERS_TABLE_NAME, 
				null, // select all cols
				null, // Where clause - select all 
				null, // Selection args 
				null, // Group by
				null, // Having
				null); // Order by started ascending
		
		return results;
	}

	public static void setGameAnswered(SQLiteDatabase db, String id) {

		ContentValues values = new ContentValues();
		values.put(GAME_ANSWERED, 1);
		
		db.beginTransaction();
		db.update(GAMES_TABLE_NAME, values, GAME_ID+"=\""+id+"\"", null);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public static void removeAnswer(SQLiteDatabase db, String id) {
		db.beginTransaction();
		db.delete(ANSWERS_TABLE_NAME, ANSWERS_GAME_ID+"=\""+id+"\"", null);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	
	public static void insertCode(SQLiteDatabase db, String code, int timestamp) {
		db.beginTransaction();
		//SQLiteDatabase.CONFLICT_IGNORE means ignore if already there
		db.insertWithOnConflict(CODES_TABLE_NAME, 
				null, 
				getCodeValues(code, timestamp), 
				SQLiteDatabase.CONFLICT_IGNORE);
		db.setTransactionSuccessful();
		db.endTransaction();
	}


	public static Cursor getCodes(SQLiteDatabase db) {
		Cursor results = db.query(CODES_TABLE_NAME, 
				null, // select all cols
				null, // Where clause - select all 
				null, // Selection args 
				null, // Group by
				null, // Having
				DatabaseHelper.CODES_TIMESTAMP + " ASC"); // Order by started ascending
		
		return results;
	}
	
	public static void removeCodesNotIn(SQLiteDatabase db, String[] codes) {
		String codeString = new String();
		
		for (String code : codes) {
			if (codeString.length() > 0) {
				codeString += "\",\"";
			}
			codeString += code;
		}
		
		db.beginTransaction();
		db.delete(CODES_TABLE_NAME, CODES_CODE+" NOT IN (\""+codeString+"\")", null);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public static Game getGame(SQLiteDatabase db, String id) {
		
		Cursor cursor = db.query(GAMES_TABLE_NAME, null, GAME_ID+"=\""+id+"\"", null, null, null, null);
		
		if (cursor.getCount() < 1) {
			return null;
		}
		return new Game(cursor);
	}

	public static Cursor getGames(SQLiteDatabase db) {
		return getGames(db, null);
	}

	public static Cursor getUnsentGames(SQLiteDatabase db) {
		return getGames(db, GAME_ANSWERED+"=0");
	}
	
	public static Cursor getSentGames(SQLiteDatabase db) {
		return getGames(db, GAME_ANSWERED+"=1");
	}
	
	public static Cursor getGames(SQLiteDatabase db, String where) {
				
//		Cursor results = db.rawQuery("SELECT ", selectionArgs)
		
		Cursor results = db.query(GAMES_TABLE_NAME, 
//				new String[]{CurrentGamesDatabaseHelper.GAME_ID, // Selected cols
//					CurrentGamesDatabaseHelper.GAME_TYPE,
//					CurrentGamesDatabaseHelper.GAME_STARTED, 
//					CurrentGamesDatabaseHelper.GAME_PARTICIPANTS}, 
				null, // select all cols - easier, and only an int that is possibly read too much
				where, // Where clause - select all 
				null, // Selection args 
				null, // Group by
				null, // Having
				DatabaseHelper.GAME_STARTED + " ASC"); // Order by started ascending
		
		return results;
	}
	

	public static void removeGame(SQLiteDatabase db, String id) {
		db.beginTransaction();
		db.delete(GAMES_TABLE_NAME, GAME_ID+"=\""+id+"\"", null);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public static void removeGamesNotIn(SQLiteDatabase db, String[] ids) {
		String idString = new String();
		
		for (String id : ids) {
			if (idString.length() > 0) {
				idString += "\",\"";
			}
			idString += id;
		}
		
		db.beginTransaction();
		db.delete(GAMES_TABLE_NAME, GAME_ID+" NOT IN (\""+idString+"\")", null);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public static void insertGame(SQLiteDatabase db, Game game) {
		insertGame(db, game.id, game.typeToString(), game.started, game.opened, game.participants);
	}
	
	public static void insertGame(SQLiteDatabase db, String id, Type type, int started, int opened, int participants) {
		insertGame(db, id, Game.typeToString(type), started, opened, participants);
	}
	
	public static void insertGame(SQLiteDatabase db, String id, String type, int started, int opened, int participants) {
		db.beginTransaction();
		//SQLiteDatabase.CONFLICT_IGNORE means ignore if already there
		db.insertWithOnConflict(GAMES_TABLE_NAME, 
				null, 
				getGameValues(id, type, started, opened, participants), 
				SQLiteDatabase.CONFLICT_IGNORE);
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public static void updateGame(SQLiteDatabase db, String id, Type type, int started, int opened, int participants) {
		updateGame(db, id, Game.typeToString(type), started, opened, participants);
	}
	
	public static void updateGame(SQLiteDatabase db, String id, String type, int started, int opened, int participants) {
		db.beginTransaction();
		//SQLiteDatabase.CONFLICT_IGNORE means ignore if already there
		db.update(GAMES_TABLE_NAME, 
				getGameValues(id, type, started, opened, participants), 
				GAME_ID+"=\""+id+"\"",
				null);
		db.setTransactionSuccessful();
		db.endTransaction();
	}


	public static ContentValues getGameValues(String id, String type, int started, int opened, int participants) {
		ContentValues values = new ContentValues();
		values.put(GAME_ID, id);
		values.put(GAME_TYPE, type);
		values.put(GAME_STARTED, started);
		values.put(GAME_OPENED, opened);
		values.put(GAME_PARTICIPANTS, participants);
		values.put(GAME_ANSWERED, 0);
		return values;
	}

	
	public static ContentValues getCodeValues(String code, int timestamp) {
		ContentValues values = new ContentValues();
		values.put(CODES_CODE, code);
		values.put(CODES_TIMESTAMP, timestamp);
		return values;
	}
	
	
	public static ContentValues getAnswersValues(String id, String answer, int opened) {
		ContentValues values = new ContentValues();
		values.put(ANSWERS_GAME_ID, id);
		values.put(ANSWERS_ANSWER, answer);
		values.put(ANSWERS_OPENED, opened);
		return values;
	}
}
