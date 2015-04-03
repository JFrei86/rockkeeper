package data;

import android.provider.BaseColumns;

public class RouteContract extends Contract implements BaseColumns {

	//Define Columns
	public static String DIFFICULTY = "difficulty";
	public static String NUM_ATTEMPTS = "num_attempts";
	
	//Define Column Types
	public RouteContract() {
		super();
		colTypes.put(DIFFICULTY, TEXT);
		colTypes.put(NUM_ATTEMPTS, INT);
	}
	
	//Table name for contract
	@Override
	public String tableName() {return "routes";}

	//Default values for schema
	public class Route extends Unit{
		public Route(){
			put(DIFFICULTY, "v0");
			put(NUM_ATTEMPTS, 0);
		}
	}
}
