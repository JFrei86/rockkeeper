/** FILENAME: LocationContract.java
 *  CREATED: 2015
 *  AUTHORS:
 *    Alex Miropolsky
 *    Chris Berger
 *    Jesse Freitas
 *    Nicole Negedly
 *  LICENSE: GNU General Public License (Version 3)
 *    Please see the LICENSE file in the main project directory for more details.
 *
 *  DESCRIPTION:
 *    Location Contract for storing locations the user climbs
 */
package transcend.rockeeper.data;

import android.database.Cursor;

/**
 * @author Team Transcend
 *
 */
public class LocationContract extends Contract {

	// Column Name Constants
	public static String NAME = "name";
	public static String CITY = "city";
	public static String LATITUDE = "latitude";
	public static String LONGITUDE = "longitude";

	// Default Constructor - Inits column types
	public LocationContract() {
		super();
		colTypes.put(NAME, TEXT);
		colTypes.put(CITY, TEXT);
		colTypes.put(LATITUDE, TEXT);
		colTypes.put(LONGITUDE, TEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see transcend.rockeeper.data.Contract#tableName()
	 */
	@Override
	public String tableName() {
		return "locations";
	}

	// Default values for schema
	/**
	 * @author Jesse
	 *
	 */
	public class Location extends Unit {
		/**
		 * @param _id
		 *            the document ID in the database
		 * @param name
		 *            The name of the location
		 * @param city
		 *            the city of the location
		 */
		public Location(Long _id, String name, String city) {
			if (_id != null)
				put(_ID, _id);
			put(NAME, name);
			put(CITY, city);
			put(LATITUDE, 0.0);
			put(LONGITUDE, 0.0);
		}
	}

	/**
	 * @param name
	 *            The name of the location
	 * @param city
	 *            the city of the location
	 * @return an instance of a Location object
	 */
	public Location build(String name, String city) {
		return this.new Location(null, name, city);
	}

	/**
	 * @param c
	 *            A cursor pointing to a result from a query
	 * @return an instance of a Location object based on the columns that the
	 *         query is currently pointing to
	 */
	public Location build(Cursor c) {
		return this.new Location(c.getLong(c.getColumnIndex(_ID)),
				c.getString(c.getColumnIndex(NAME)), c.getString(c
						.getColumnIndex(CITY)));
	}

}
