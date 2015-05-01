/** FILENAME: SettingsContract.java
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
 *    Store Settings for the user in the database regarding name, level, and location
 */

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
