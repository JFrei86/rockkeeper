// I want to get XXXXX points all time. I'm currently at XXXXXX points.
// I want to atttempt XX routes and one day. I want to complete at least XX of them.
// I want to climb a VX by XX/XX/XXXX .

// Alex start start XML - get us an idea of what we're looking it - visualize it
// Jesse think about how the skelleton framework (null values in the database, column with climb/attempt/get...)
// Dropdown to choose question format and interchangeable verbs
// column to indicate type (difficulty, points, completed). THe columsn will be null except for column indicated by thte type.

package transcend.rockeeper.data;

import java.util.Date;
import java.util.HashMap;

import android.database.Cursor;

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
		public Goal(Long _id, String type, long val, long due, long progress, long created_on){
			if( _id != null ) put(_ID, _id);
            put(TYPE, type);
			put(type, val);
			put(DUE_DATE, due);
			put(CREATED_ON, created_on);
			put(STATUS, progress);
		}
		public Goal(Long _id, String difficulty, long due, long progress, long created_on){
            if( _id != null ) put(_ID, _id);
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
}
