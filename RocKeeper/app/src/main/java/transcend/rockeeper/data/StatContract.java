package transcend.rockeeper.data;

import java.util.Date;

import android.database.Cursor;

public class StatContract extends Contract {
	
	public static final String DATE = "date";
	public static final String ATTEMPTS = "attempts";
	public static final String COMPLETED = "completed";
	public static final String POINTS = "points";

	public StatContract() {
		super();
		colTypes.put(DATE, INT);
		colTypes.put(ATTEMPTS, INT);
		colTypes.put(COMPLETED, INT);
		colTypes.put(POINTS, INT);
	}
	
	public Stat build(Date d, long attempts, long completed, long points){
		return new Stat(null, d, attempts, completed, points);
	}

	@Override
	public String tableName() {
		return "stats";
	}

	public class Stat extends Unit{
		public Stat(Long _id, Date d, long attempts, long completed, long points){
			if(_id != null)
				put(_ID, _id);
			put(DATE, d.getTime());
			put(ATTEMPTS, attempts);
			put(COMPLETED, completed);
			put(POINTS, points);
		}
		public Stat(Long _id, long d, long attempts, long completed, long points){
			if(_id != null)
				put(_ID, _id);
			put(DATE, d);
			put(ATTEMPTS, attempts);
			put(COMPLETED, completed);
			put(POINTS, points);
		}
	}
	
	public Stat build(Cursor c) {
		return this.new Stat(
				c.getLong(c.getColumnIndex(_ID)),
				c.getLong(c.getColumnIndex(DATE)),
				c.getLong(c.getColumnIndex(ATTEMPTS)),
				c.getLong(c.getColumnIndex(COMPLETED)),
				c.getLong(c.getColumnIndex(POINTS)));
	}
}
