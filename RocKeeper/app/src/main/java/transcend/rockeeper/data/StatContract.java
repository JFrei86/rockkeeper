package transcend.rockeeper.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import transcend.rockeeper.data.RouteContract.Route;
import transcend.rockeeper.sqlite.Transaction;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class StatContract extends Contract {
	
	public static final String DATE = "date";
	public static final String ATTEMPTS = RouteContract.NUM_ATTEMPTS;
	public static final String COMPLETED = RouteContract.COMPLETED;
	public static final String POINTS = RouteContract.POINTS;

	public StatContract() {
		super();
		colTypes.put(DATE, INT);
		colTypes.put(ATTEMPTS, INT);
		colTypes.put(COMPLETED, INT);
		colTypes.put(POINTS, INT);
	}
	
	public void incrementStat(final Route r, final String column , SQLiteDatabase db){
		final GregorianCalendar c = new GregorianCalendar();
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		final Cursor data = query(null, DATE + "=" + c.getTimeInMillis(), null, column, false, 1, db);
		Transaction t = new Transaction(db){
			public void task(SQLiteDatabase db) {
				if(data.getCount() == 0 && !db.isReadOnly()){
					Stat s = null;
					if(column == ATTEMPTS)
						s = build(c.getTime(), 1,0,0);
					else
						s = build(c.getTime(), 0, 1, Long.parseLong(r.get(POINTS)));
					insert(s, db);
				} else {
					if(db.isReadOnly()) return;
					data.moveToFirst();
					Stat s = build(data);
					if(column == ATTEMPTS) {
						s.put(ATTEMPTS, Long.parseLong(s.get(ATTEMPTS)) + 1);
					} else {
						s.put(COMPLETED, Long.parseLong(s.get(COMPLETED)) + 1);
						s.put(POINTS, Long.parseLong(s.get(POINTS)) + Long.parseLong(r.get(POINTS)));
					}
					update(s, _ID + "=" + s.get(_ID), null, db);
				}
			}
			public void onComplete() {}
			public void onProgressUpdate(Unit... data) {}
		};
		t.run(true, true);
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
