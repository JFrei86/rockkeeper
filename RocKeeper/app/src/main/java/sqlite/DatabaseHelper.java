package sqlite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import data.*;

@SuppressLint("NewApi")
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "RocKeeper";
	private static final int DB_VERSION = 1;

	private static final RouteContract routes = new RouteContract();
	private static final GoalContract goals = new GoalContract();
	private static final NoteContract notes = new NoteContract();
	private static final LocationContract locations = new LocationContract();
	private static final SettingsContract settings = new SettingsContract();
	
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version, DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(routes.createTable() +
				   goals.createTable() +
				   notes.createTable() +
				   locations.createTable() +
				   settings.createTable());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(routes.dropTable() +
				   goals.dropTable() +
				   notes.dropTable() +
				   locations.dropTable() +
				   settings.dropTable());
		onCreate(db);
	}
	
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(routes.dropTable() +
				   goals.dropTable() +
				   notes.dropTable() +
				   locations.dropTable() +
				   settings.dropTable());
		onCreate(db);
	}
}
