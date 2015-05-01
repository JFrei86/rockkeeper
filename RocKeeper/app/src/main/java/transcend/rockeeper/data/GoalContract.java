/** FILENAME: GoalContract.java
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
 *    Goal Contract for managing goals in the database
 */

package transcend.rockeeper.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import transcend.rockeeper.data.RouteContract.Route;
import transcend.rockeeper.sqlite.Transaction;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GoalContract extends Contract {

	public static final String TYPE = "type";
	public static final String ATTEMPTS = RouteContract.NUM_ATTEMPTS;
	public static final String COMPLETED = RouteContract.COMPLETED;
	public static final String POINTS = RouteContract.POINTS;
	public static final String DIFFICULTY = RouteContract.DIFFICULTY;
	public static final String DUE_DATE = "due_date";
	public static final String STATUS = "status";
	
	public HashMap<String, String> verbs = new HashMap<String, String>();
	public HashMap<String, String> nouns = new HashMap<String, String>();
	
	public GoalContract(){
		super();
		colTypes.put(TYPE, TEXT);
		colTypes.put(ATTEMPTS, INT);
		colTypes.put(COMPLETED, INT);
		colTypes.put(POINTS, INT);
		colTypes.put(DIFFICULTY, TEXT);
		colTypes.put(DUE_DATE, INT);
		colTypes.put(STATUS, INT);
		
		verbs.put(ATTEMPTS, "Attempt ");
		verbs.put(COMPLETED, "Complete ");
		verbs.put(POINTS, "Earn ");
		verbs.put(DIFFICULTY, "Climb a ");
		nouns.put(ATTEMPTS, " routes");
		nouns.put(COMPLETED, " routes");
		nouns.put(POINTS, " points");
		nouns.put(DIFFICULTY, " route");
	}
	
	@Override
	public String tableName() { return "goals";}
	
	public class Goal extends Unit {
		public Goal(Long l, String type, long val, long due, long progress, long created_on){
			if(l != null)
				put(_ID, l);
			put(TYPE, type);
			put(type, val);
			put(DUE_DATE, due);
			put(CREATED_ON, created_on);
			put(STATUS, progress);
		}

		public Goal(Long l, String difficulty, long due, long progress, long created_on){
			if(l != null)
				put(_ID, l);
			put(TYPE, DIFFICULTY);
			put(DIFFICULTY, difficulty);
			put(DUE_DATE, due);
			put(CREATED_ON, created_on);
			put(STATUS, progress);
		}
	}
	public Goal build(Cursor c){
		if(c.getString(c.getColumnIndex(TYPE)) != DIFFICULTY)
			return this.new Goal(
				c.getLong(c.getColumnIndex(_ID)),
                c.getString(c.getColumnIndex(TYPE)),
				c.getLong(c.getColumnIndex(c.getString(c.getColumnIndex(TYPE)))),
				c.getLong(c.getColumnIndex(DUE_DATE)),
				c.getLong(c.getColumnIndex(STATUS)),
				Long.parseLong((c.getString(c.getColumnIndex(CREATED_ON)))));
		else
			return this.new Goal(
					c.getLong(c.getColumnIndex(_ID)),
					c.getString(c.getColumnIndex(DIFFICULTY)),
					c.getLong(c.getColumnIndex(DUE_DATE)),
					c.getLong(c.getColumnIndex(STATUS)),
					Long.parseLong((c.getString(c.getColumnIndex(CREATED_ON)))));
	}
	public Goal build(String type, long val, long due){
		return this.new Goal(null, type, val, due, 0, new Date().getTime());
	}
	public Goal build(String difficulty, long due){
		return this.new Goal(null, difficulty, due, 0, new Date().getTime());
	}
	
	public void updateGoals(final Route r, final String column, SQLiteDatabase db){
		Transaction t = new Transaction(db){
			public void task(SQLiteDatabase db) {
				ArrayList<Goal> gs = new ArrayList<Goal>();
				//ArrayList<Long> delete = new ArrayList<Long>();
				Cursor c = query(null, null, null, _ID, false, null, db);
				c.moveToFirst();
				while(c.getCount() > 0 && !c.isAfterLast()){
					gs.add(build(c));
					c.moveToNext();
				}
				
				for(int i = 0; i < gs.size(); i++){
					Goal g = gs.get(i);
					
					if(g.get(TYPE) != DIFFICULTY && Long.parseLong(g.get(g.get(TYPE))) <= Long.parseLong(g.get(STATUS))) continue;
					else if(g.get(TYPE) == DIFFICULTY && Long.parseLong(g.get(STATUS)) > 0) continue;
					
					if(g.get(column) != null) g.put(STATUS, Long.parseLong(g.get(STATUS) + 1));
					if(column != ATTEMPTS && g.get(POINTS) != null) g.put(STATUS, Long.parseLong(g.get(STATUS) + Long.parseLong(r.get(POINTS))));
					if(column != ATTEMPTS && g.get(DIFFICULTY) != null && g.get(DIFFICULTY) == r.get(DIFFICULTY)) g.put(STATUS, 1);
					gs.set(i, g);
				}
				for(int i = 0; i < gs.size(); i++){
					update(gs.get(i), _ID + "=" + gs.get(i).get(_ID), null, db);
				}
				//String where = (_ID + " IN " + delete.toString()).replace('[', '(').replace(']', ')');
				//delete(where, null, db);
			}
			public void onComplete() {}
			public void onProgressUpdate(Unit... data) {}
		};
		t.run(true, true);
	}
}
