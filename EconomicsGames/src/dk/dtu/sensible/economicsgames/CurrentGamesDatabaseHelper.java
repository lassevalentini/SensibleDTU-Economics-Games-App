package dk.dtu.sensible.economicsgames;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
    	db.beginTransaction();
        db.execSQL(TABLE_CREATE);
		db.endTransaction();
    }

    
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	public static Game getGame(SQLiteDatabase db, int id) {
		db.beginTransaction();
		Cursor cursor = db.query(TABLE_NAME, null, "id="+id, null, null, null, null);
		db.endTransaction();
		
		if (cursor.getCount() < 1) {
			return null;
		}
		return new Game(cursor);
	}
	
//	public static Game[] getGamesNotIn(SQLiteDatabase db, int[] ids) {
//		String idString = new String();
//		
//		for (int id : ids) {
//			if (idString.length() > 0) {
//				idString += ",";
//			}
//			idString += id;
//		}
//		
//		Cursor cursor = db.query(TABLE_NAME, null, "id NOT IN ("+idString+")", null, null, null, null);
//		
//		Game[] games = new Game[cursor.getCount()];
//		
//		for (int i = 0; i < games.length; i++) {
//			games[i] = new Game(cursor);
//		}
//		
//		return games;
//	}
	

	public static void removeGame(SQLiteDatabase db, int id) {
		db.beginTransaction();
		db.delete(TABLE_NAME, "id="+id, null);
		db.endTransaction();
	}
	
	public static void removeGamesNotIn(SQLiteDatabase db, int[] ids) {
		String idString = new String();
		
		for (int id : ids) {
			if (idString.length() > 0) {
				idString += ",";
			}
			idString += id;
		}
		
		db.beginTransaction();
		db.delete(TABLE_NAME, "id NOT IN ("+idString+")", null);
		db.endTransaction();
	}

	public static void insertGame(SQLiteDatabase db, int id, String type, int started, int opened, int participants) {
		db.beginTransaction();
		//SQLiteDatabase.CONFLICT_IGNORE means ignore if already there
		db.insertWithOnConflict(TABLE_NAME, 
				null, 
				getValues(id, type, started, opened, participants), 
				SQLiteDatabase.CONFLICT_IGNORE);
		db.endTransaction();
	}
	
	public static void updateGame(SQLiteDatabase db, int id, String type, int started, int opened, int participants) {
		db.beginTransaction();
		//SQLiteDatabase.CONFLICT_IGNORE means ignore if already there
		db.update(TABLE_NAME, 
				getValues(id, type, started, opened, participants), 
				"id="+id,
				null);
		db.endTransaction();
	}
	
	public static ContentValues getValues(int id, String type, int started, int opened, int participants) {
		ContentValues values = new ContentValues();
		values.put(GAME_ID, id);
		values.put(GAME_TYPE, type);
		values.put(GAME_STARTED, started);
		values.put(GAME_OPENED, opened);
		values.put(GAME_PARTICIPANTS, participants);
		return values;
	}

}
