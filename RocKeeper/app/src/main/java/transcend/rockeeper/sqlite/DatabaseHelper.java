package transcend.rockeeper.sqlite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import transcend.rockeeper.data.*;

@SuppressLint("NewApi")
public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "rockeeper.db";
	public static final int DB_VERSION = 2;

	public static final RouteContract routes = new RouteContract();
	public static final GoalContract goals = new GoalContract();
	public static final NoteContract notes = new NoteContract();
	public static final LocationContract locations = new LocationContract();
	public static final SettingsContract settings = new SettingsContract();
	
	
	public DatabaseHelper(Context context, CursorFactory c){
		super(context, DATABASE_NAME, c, DB_VERSION, new DatabaseErrorHandler(){
			public void onCorruption(SQLiteDatabase dbObj) {
				Log.e("Splash.java:29", "Corruption detected: " + dbObj.getPath());
				Log.e("Splash.java:30", "Closing Database");
				dbObj.close();
			}
		});
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(routes.createTable());
		db.execSQL(goals.createTable());
		db.execSQL(notes.createTable());
		db.execSQL(locations.createTable());
		db.execSQL(settings.createTable());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(routes.dropTable());
		db.execSQL(goals.dropTable());
		db.execSQL(notes.dropTable());
		db.execSQL(locations.dropTable());
		db.execSQL(settings.dropTable());
		onCreate(db);
	}
	
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(routes.dropTable());
		db.execSQL(goals.dropTable());
		db.execSQL(notes.dropTable());
		db.execSQL(locations.dropTable());
		db.execSQL(settings.dropTable());
		onCreate(db);
	}
}
