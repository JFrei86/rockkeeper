package transcend.rockeeper.data;

import android.database.Cursor;

public class LocationContract extends Contract {

	public static String NAME = "name";
	public static String LATITUDE = "latitude";
	public static String LONGITUDE = "longitude";
	
	public LocationContract() {
		super();
		colTypes.put(NAME, TEXT);
		colTypes.put(LATITUDE, TEXT);
		colTypes.put(LONGITUDE, TEXT);
	}

	@Override
	public String tableName() {return "locations";}
	
	//Default values for schema
	public class Location extends Unit{
		public Location(String string){
			put(NAME, string);
			put(LATITUDE, 0.0);
			put(LONGITUDE, 0.0);
		}
	}

	public Location build(String string) {
		return this.new Location(string);
	}
	public Location build(Cursor c){
		return this.new Location(
				c.getString(c.getColumnIndex(NAME)));
	}

}
