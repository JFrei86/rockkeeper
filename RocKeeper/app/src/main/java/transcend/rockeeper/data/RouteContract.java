package transcend.rockeeper.data;

import transcend.rockeeper.data.RouteContract.Route;
import android.database.Cursor;
import android.provider.BaseColumns;

public class RouteContract extends Contract implements BaseColumns {

	//Define Columns
	public static String DIFFICULTY = "difficulty";
	public static String NUM_ATTEMPTS = "num_attempts";
	public static String COLOR = "color";
	public static String NAME = "name";
	public static String LOCATION = "location";
	
	//Define Column Types
	public RouteContract() {
		super();
		colTypes.put(DIFFICULTY, TEXT);
		colTypes.put(NUM_ATTEMPTS, INT);
		colTypes.put(LOCATION, INT);
		colTypes.put(COLOR, TEXT);
		colTypes.put(NAME, TEXT);
	}
	
	//Table name for contract
	@Override
	public String tableName() {return "routes";}

	public Route build(String difficulty, int attempts, long loc_id, String color, String name){
		return this.new Route(difficulty, attempts, loc_id, color, name);
	}
	
	//Default values for schema
	public class Route extends Unit{
		public Route(String difficulty, int attempts, long loc_id, String color, String name){
			put(DIFFICULTY, difficulty);
			put(NUM_ATTEMPTS, attempts);
			put(LOCATION, loc_id);
			put(COLOR, color);
			put(NAME, name);
		}
	}

	public Route build(Cursor c) {
		return this.new Route(
				c.getString(c.getColumnIndex(DIFFICULTY)),
				c.getInt(c.getColumnIndex(NUM_ATTEMPTS)),
				c.getLong(c.getColumnIndex(LOCATION)),
				c.getString(c.getColumnIndex(COLOR)),
				c.getString(c.getColumnIndex(NAME)));
	}
}
