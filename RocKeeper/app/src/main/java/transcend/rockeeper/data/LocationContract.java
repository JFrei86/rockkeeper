package transcend.rockeeper.data;

import android.database.Cursor;

public class LocationContract extends Contract {

	public static String NAME = "name";
    public static String CITY = "city";
	public static String LATITUDE = "latitude";
	public static String LONGITUDE = "longitude";
	
	public LocationContract() {
		super();
		colTypes.put(NAME, TEXT);
        colTypes.put(CITY, TEXT);
		colTypes.put(LATITUDE, TEXT);
		colTypes.put(LONGITUDE, TEXT);
	}

	@Override
	public String tableName() {return "locations";}
	
	//Default values for schema
	public class Location extends Unit{
		public Location(String name, String city){
			put(NAME, name);
            put(CITY, city);
			put(LATITUDE, 0.0);
			put(LONGITUDE, 0.0);
		}
	}

	public Location build(String name, String city) {
		return this.new Location(name, city);
	}
	public Location build(Cursor c){
		return this.new Location(
				c.getString(c.getColumnIndex(NAME)),
                c.getString(c.getColumnIndex(CITY)));
	}

}
