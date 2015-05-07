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

import transcend.rockeeper.activities.MainActivity;
import transcend.rockeeper.activities.RoutesFragment;
import transcend.rockeeper.data.RouteContract.Route;
import transcend.rockeeper.sqlite.Transaction;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ListView;
import android.widget.Toast;

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
	
	/**
	 * Constructor: sets up goal contract columns & form verbs
	 */
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
	/**
	 * See Contract tableName();
	 */
	@Override
	public String tableName() { return "goals";}
	/**
	 * 
	 * @author Team Transcend
	 *
	 */
	public class Goal extends Unit {
		//Constructors for Goal objects
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
        public String toString() {
            return verbs.get(get(TYPE)) + " " + get(get(TYPE)) + " " + nouns.get(get(TYPE));
        }
	}
	/**
	 * Factory function for Goals
	 * @param c Cursor pointing to a row in the database
	 * @return Goal object from cursor
	 */
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
	/**
	 * Factory function for Goals
	 * @param c Cursor pointing to a row in the database
	 * @return Goal object from cursor
	 */
	public Goal build(String type, long val, long due){
		return this.new Goal(null, type, val, due, 0, new Date().getTime());
	}
	/**
	 * Factory function for Goals
	 * @param c Cursor pointing to a row in the database
	 * @return Goal object
	 */
	public Goal build(String difficulty, long due){
		return this.new Goal(null, difficulty, due, 0, new Date().getTime());
	}
	/**
	 * Update manager for goals. Called when goals in the database should be updated from climbing action
	 * @param r The route that caused the climbing action
	 * @param column RouteContract.NUM_ATTEMPTS or RouteContract.COMPLETED to signal an attempt action
	 * or completion action
	 * @param db A writable database reference
	 */
	public void updateGoals(final Route r, final String column, final MainActivity mainActivity, final ListView listview, SQLiteDatabase db){
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

                    String type = g.get( TYPE );
                    long status = Long.parseLong( g.get( STATUS ));

                    // If the goal has already been satisfied, ignore it
					if( !type.equals( DIFFICULTY ) && Long.parseLong(g.get(type)) <= status) continue;
					else if( type.equals(DIFFICULTY) && status > 0) continue;

                    // Increment the status
                    if( column.equals( ATTEMPTS ) ) {
                        if( type.equals( ATTEMPTS ) ) {
                            g.put( STATUS, status+1 );
                            if( status < Long.parseLong( g.get( ATTEMPTS ) ) && status+1 >= Long.parseLong(g.get(ATTEMPTS)) )
                                mainActivity.showToast( "You completed a goal!", Toast.LENGTH_LONG );
                        }
                        else if( type.equals( DIFFICULTY ) && g.get( DIFFICULTY ).equals( r.get( DIFFICULTY ) ) ) {
                            g.put( STATUS, 1 );
                            if( status == 0 )
                                mainActivity.showToast( "You completed a goal!", Toast.LENGTH_LONG );
                        }
                    }
                    if( column.equals( COMPLETED ) ) {
                        if( type.equals( POINTS ) ) {
                            g.put( STATUS, status + Long.parseLong( r.get( POINTS ) ) );
                            if( status < Long.parseLong(g.get(POINTS)) && status+Long.parseLong(r.get(POINTS)) >= Long.parseLong(g.get(POINTS)) )
                                mainActivity.showToast( "You completed a goal!", Toast.LENGTH_LONG );
                        }
                        else if( type.equals( COMPLETED ) ) {
                            g.put( STATUS, status+1 );
                            if( status < Long.parseLong(g.get(COMPLETED)) && status+1 >= Long.parseLong(g.get(COMPLETED)) )
                                mainActivity.showToast( "You completed a goal!", Toast.LENGTH_LONG );
                        }
                    }
					/*if(g.get(column) != null) g.put(STATUS, Long.parseLong(g.get(STATUS) + 1));
                    //
					if(column.equals( POINTS ) && g.get(POINTS) != null) g.put(STATUS, Long.parseLong(g.get(STATUS)) + Long.parseLong(r.get(POINTS)));
					if(column.equals( ATTEMPTS ) && g.get(DIFFICULTY) != null && g.get(DIFFICULTY).equals( r.get(DIFFICULTY) )) g.put(STATUS, 1);*/
					gs.set(i, g);
				}
				for(int i = 0; i < gs.size(); i++){
					update(gs.get(i), _ID + "=" + gs.get(i).get(_ID), null, db);
				}
				//String where = (_ID + " IN " + delete.toString()).replace('[', '(').replace(']', ')');
				//delete(where, null, db);
			}
			public void onComplete() {
                ((RoutesFragment.RouteListAdapter)listview.getAdapter()).notifyDataSetChanged();
            }
			public void onProgressUpdate(Unit... data) {}
		};
		t.run(true, true);
	}
}
