package transcend.rockeeper.data;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * A base contract containing helper functions for interacting with the database
 * 
 * @author Team Transcend
 */
public abstract class Contract implements BaseColumns {

	public static final String TEXT = "TEXT";
	public static final String INT = "INT";
	public static final String NUM = "NUM";
	public static final String REAL = "REAL";

	public int id = 0;

	protected Map<String, String> colTypes = new TreeMap<String, String>();

	public static final String CREATED_ON = "created_on";
	public static final String MODIFIED_ON = "modified_on";

	/**
	 * @return the table name of the contract
	 */
	public abstract String tableName();

	/**
	 * Default constructor for all database objects
	 */
	public Contract() {
		colTypes.put(CREATED_ON, TEXT);
		colTypes.put(MODIFIED_ON, TEXT);
	}

	// Create table SQL Command String
	/**
	 * @return a SQL executable string for creating a table in SQLite
	 */
	public String createTable() {
		String command = "CREATE TABLE " + tableName() + " (" + Contract._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL";
		Iterator<String> i = colTypes.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			command += ", " + key + " " + colTypes.get(key);
		}
		return command + "); ";
	}

	// Drop table SQL Command String
	/**
	 * @return a SQL executable string for dropping a table in SQLite
	 */
	public String dropTable() {
		return "DROP TABLE IF EXISTS " + tableName() + ";";
	}

	// Put a new document in the database
	/**
	 * @param d
	 *            A Unit of data containing keys to insert into the table
	 * @param db
	 *            The database to insert into. Database must be read/write
	 * @return the ID of the Unit created. Unit will be modified to contain the
	 *         new id as well.
	 */
	public long insert(Unit d, SQLiteDatabase db) {
		d.put(CREATED_ON, new Date().toString());
		d.put(MODIFIED_ON, new Date().toString());
		ContentValues values = new ContentValues();
		Iterator<String> i = d.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			if (colTypes.containsKey(key)) {
				if (colTypes.get(key) == INT) {
					values.put(key, Long.parseLong(d.get(key)));
				}
				if (colTypes.get(key) == NUM) {
					values.put(key, Double.parseDouble(d.get(key)));
				}
				if (colTypes.get(key) == TEXT) {
					values.put(key, d.get(key));
				}
			}
		}
		long id = db.insert(tableName(), null, values);
		d.put(_ID, id);
		return id;
	}

	/**
	 * @param retrieve
	 *            An array of columns to retrieve from the database
	 * @param where
	 *            A SQL where clause that the query will use to look up values
	 * @param args
	 *            Query arguments
	 * @param sortBy
	 *            A SQL column string to sort by
	 * @param descending
	 *            A boolean to determine the sort direction (descending = true,
	 *            ascending = false)
	 * @param limit
	 *            A limit value (null is no limit clause)
	 * @param db
	 *            A read only or read/write database reference
	 * @return A cursor containing the results of the query
	 */
	public Cursor query(String[] retrieve, String where, String[] args,
			String sortBy, boolean descending, Integer limit, SQLiteDatabase db) {
		if (limit != null)
			return db.query(tableName(), retrieve, where, args, null, null,
					sortBy + ((descending) ? " DESC" : " ASC"),
					limit.toString());
		else
			return db.query(tableName(), retrieve, where, args, null, null,
					sortBy + ((descending) ? " DESC" : " ASC"), null);
	}

	/**
	 * @param where
	 *            A SQL where clause that the query will use to look up values
	 * @param args
	 *            Query arguments
	 * @param db
	 *            A read/write database reference
	 */
	public void delete(String where, String[] args, SQLiteDatabase db) {
		db.delete(tableName(), where, args);
	}

	/**
	 * @param d
	 * @param where
	 *            A SQL where clause that the query will use to look up values
	 * @param args
	 *            Query arguments
	 * @param db
	 *            A read/write database reference
	 * @return the number of documents in the database that were updated
	 */
	public long update(Unit d, String where, String[] args, SQLiteDatabase db) {
		d.put(Contract.MODIFIED_ON, new Date().toString());
		ContentValues values = new ContentValues();
		Iterator<String> i = d.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			if (colTypes.containsKey(key)) {
				if (colTypes.get(key) == INT) {
					values.put(key, Long.parseLong(d.get(key)));
				}
				if (colTypes.get(key) == NUM) {
					values.put(key, Double.parseDouble(d.get(key)));
				}
				if (colTypes.get(key) == TEXT) {
					values.put(key, d.get(key));
				}
			}
		}
		return db.update(tableName(), values, where, args);
	}

	/**
	 * A model for inserting keys into the database
	 * 
	 * @author Team Transcend
	 *
	 */
	public class Unit {
		public TreeMap<String, String> data = new TreeMap<String, String>();

		/**
		 * @return The set of keys in the unit currently
		 */
		public Set<String> keySet() {
			return data.keySet();
		}

		/**
		 * @param key
		 *            the column to retrieve from the unit
		 * @return The value from the unit corresponding to the key
		 */
		public String get(String key) {
			return data.get(key);
		}

		/**
		 * @param col
		 *            the column to insert a value
		 * @param val
		 *            the value to insert into the unit
		 */
		public void put(String col, String val) {
			data.put(col, val);
		}

		public void put(String col, Integer val) {
			put(col, "" + val);
		}

		public void put(String col, Long val) {
			put(col, "" + val);
		}

		public void put(String col, Double val) {
			put(col, "" + val);
		}

		/**
		 * @param col
		 *            remove a value from the unit if it exists
		 */
		public void remove(String col) {
			data.remove(col);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return data.toString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object a) {
			if (a != null && ((Unit) a).get(_ID) != null)
				return ((Unit) a).get(_ID).equals(this.get(_ID));
			else
				return false;
		}

		public Unit() {
		}
	}
}
