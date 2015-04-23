package transcend.rockeeper.data;

import android.database.Cursor;

public class SettingsContract extends Contract {

	public static String USER = "username";
	public static String LEVEL = "level";
	
	public SettingsContract() {
		super();
		colTypes.put(USER, TEXT);
		colTypes.put(LEVEL, TEXT);
	}

	@Override
	public String tableName() {return "settings";}

	//Default values for schema
	public class Settings extends Unit{
		public Settings(String name, String level){
			put(USER, name);
			put(LEVEL, level);
		}
	}

	public Settings build(String name, String level) {
		return this.new Settings(name, level);
	}
	public Settings build(Cursor c){
		return this.new Settings(
				c.getString(c.getColumnIndex(USER)),
				c.getString(c.getColumnIndex(LEVEL)));
	}
}
