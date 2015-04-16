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

public abstract class Contract implements BaseColumns{

	public static final String TEXT = "TEXT";
	public static final String INT = "INT";
	public static final String NUM = "NUM";
	public static final String REAL = "REAL";
	
	public int id = 0;
	
	protected Map<String, String> colTypes = new TreeMap<String, String>();
	
	public static final String CREATED_ON = "created_on";
	public static final String MODIFIED_ON = "modified_on";
	
	public abstract String tableName();
	
	public Contract(){
		colTypes.put(CREATED_ON, TEXT);
		colTypes.put(MODIFIED_ON, TEXT);
	}
	
	//Create table SQL Command String
	public String createTable() {
		String command = "CREATE TABLE " + tableName() + " (" +
		Contract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL";
		Iterator<String> i = colTypes.keySet().iterator();
		while(i.hasNext()){
			String key = i.next();
			command += ", " + key + " " + colTypes.get(key);
		}
		return command + "); ";
	}
	
	//Drop table SQL Command String
	public String dropTable() {
		return "DROP TABLE IF EXISTS " + tableName() + ";";
	}
	
	//Put a new document in the database
	public long insert(Unit d, SQLiteDatabase db){
		d.put(CREATED_ON, new Date().toString());
		d.put(MODIFIED_ON, new Date().toString());
		ContentValues values = new ContentValues();
		Iterator<String> i = d.keySet().iterator();
		while(i.hasNext()){
			String key = i.next();
			if(colTypes.containsKey(key)){
				if(colTypes.get(key) == INT){
					values.put(key, Integer.parseInt(d.get(key)));
				}
				if(colTypes.get(key) == NUM){
					values.put(key, Double.parseDouble(d.get(key)));
				}
				if(colTypes.get(key) == TEXT){
					values.put(key, d.get(key));
				}
			}
		}
		return db.insert(tableName(), null, values);
	}
	
	public Cursor query(String[] retrieve, String where, String[] args, String sortBy, boolean descending, Integer limit, SQLiteDatabase db){
		if(limit != null)
			return db.query(tableName(), retrieve, where, args, null, null, sortBy + ((descending)?" DESC":" ASC"), limit.toString());
		else
			return db.query(tableName(), retrieve, where, args, null, null, sortBy + ((descending)?" DESC":" ASC"), null);
	}
	
	public void delete(String where, String[] args, SQLiteDatabase db){
		db.delete(tableName(), where, args);
	}
	
	public long update(Unit d, String where, String[] args, SQLiteDatabase db){
		d.put(Contract.MODIFIED_ON, new Date().toString());
		ContentValues values = new ContentValues();
		Iterator<String> i = d.keySet().iterator();
		while(i.hasNext()){
			String key = i.next();
			if(colTypes.containsKey(key)){
				if(colTypes.get(key) == INT){
					values.put(key, Long.parseLong(d.get(key)));
				}
				if(colTypes.get(key) == NUM){
					values.put(key, Double.parseDouble(d.get(key)));
				}
				if(colTypes.get(key) == TEXT){
					values.put(key, d.get(key));
				}
			}
		}
		return db.update(tableName(), values, where, args);
	}
	
	public class Unit{
		public TreeMap<String, String> data = new TreeMap<String, String>();
		public Set<String> keySet() { return data.keySet(); }
		public String get(String key) { return data.get(key); }
		public void put(String col, String val) { data.put(col, val); }
		public void put(String col, int val) { put(col, "" + val); }
		public void put(String col, long val) { put(col, "" + val); }
		public void put(String col, double val) { put(col, "" + val); }
		public void remove(String col) { data.remove(col); }
		public String toString(){return data.toString();}
		public Unit(){}
	}
}
