package transcend.rockeeper.sqlite;

import transcend.rockeeper.data.Contract.Unit;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public abstract class Transaction extends AsyncTask<Boolean, Unit, Void>{

	private SQLiteDatabase db;
	public Transaction(SQLiteDatabase db) {this.db = db;}
	
	private void runHelper(boolean exclusive){
		if(exclusive)
			db.beginTransaction();
		else
			db.beginTransactionNonExclusive();
		try{
			task(db);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	public abstract void task(SQLiteDatabase db);
	public abstract void onComplete();
	public final void run(boolean exclusive, boolean background){
		if(!background){
			runHelper(exclusive);
			onComplete();
		} else {
			this.execute(exclusive);
		}
	}
	//Async Task Functions
	public Void doInBackground(Boolean... exclusive){
		runHelper(exclusive[0]);
		return null;
	}
	public abstract void onProgressUpdate(Unit... data);
	public void onPostExecute(Void foo){
		onComplete();
		return;
	}
}
