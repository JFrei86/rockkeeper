package transcend.rockeeper.data;

import android.database.Cursor;
import android.provider.BaseColumns;

public class RouteContract extends Contract implements BaseColumns {

	//Define Columns
	public static String DIFFICULTY = "difficulty";
	public static String NUM_ATTEMPTS = "num_attempts";
	public static String COLOR = "color";
	public static String NAME = "name";
	public static String LOCATION = "location";
	public static String COMPLETED = "complete";
    public static String POINTS = "points";

	
	//Define Column Types
	public RouteContract() {
		super();
		colTypes.put(DIFFICULTY, TEXT);
		colTypes.put(NUM_ATTEMPTS, INT);
		colTypes.put(LOCATION, INT);
		colTypes.put(COLOR, INT);
		colTypes.put(NAME, TEXT);
		colTypes.put(COMPLETED, INT);
        colTypes.put(POINTS, INT);
	}
	
	//Table name for contract
	@Override
	public String tableName() {return "routes";}

	public Route build(String difficulty, int attempts, long loc_id, int color, String name, int completed, int points){
		return this.new Route(null, difficulty, attempts, loc_id, color, name, completed, points);
	}
	
	//Default values for schema
	public class Route extends Unit{
		public Route(Long _id, String difficulty, int attempts, long loc_id, int color, String name, int completed, int points){
			if(_id != null)
				put(_ID, _id);
			put(DIFFICULTY, difficulty);
			put(NUM_ATTEMPTS, attempts);
			put(LOCATION, loc_id);
			put(COLOR, color);
			put(NAME, name);
			put(COMPLETED, completed);
            put(POINTS, points);
		}
	}

	public Route build(Cursor c) {
		return this.new Route(
				c.getLong(c.getColumnIndex(_ID)),
				c.getString(c.getColumnIndex(DIFFICULTY)),
				c.getInt(c.getColumnIndex(NUM_ATTEMPTS)),
				c.getLong(c.getColumnIndex(LOCATION)),
				c.getInt(c.getColumnIndex(COLOR)),
				c.getString(c.getColumnIndex(NAME)),
				c.getInt(c.getColumnIndex(COMPLETED)),
                c.getInt(c.getColumnIndex(POINTS)));
	}
}
