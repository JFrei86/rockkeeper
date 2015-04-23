package transcend.rockeeper.activities;

import transcend.rockeeper.data.SettingsContract;
import transcend.rockeeper.sqlite.DatabaseHelper;
import activities.rockeeper.R;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
//import android.view.Menu;

public class Splash extends ActionBarActivity {

	/** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 2000;

    /** Creating database if first time */
    DatabaseHelper dbh = new DatabaseHelper(this, null);
    SQLiteDatabase db;
    
    /** Called when the activity is first created. */
    @Override
    
    public void onBackPressed(){}
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        
        
        /* New Handler to start the Menu-Activity 
         * and close this Splash-Screen after some seconds.*/
        db = dbh.getWritableDatabase();
        if(firstTime()){
	        new Handler().postDelayed(new Runnable(){
	            @Override
	            public void run() {
	                /* Create an Intent that will start the Menu-Activity. */
	                Intent mainIntent = new Intent(Splash.this, FirstTimePage.class);
	                Splash.this.startActivity(mainIntent);
	                Splash.this.finish();
	            }
	        }, SPLASH_DISPLAY_LENGTH);
        } else {
        	new Handler().postDelayed(new Runnable(){
	            @Override
	            public void run() {
	                /* Create an Intent that will start the Menu-Activity. */
	                Intent mainIntent = new Intent(Splash.this, MainActivity.class);
	                Splash.this.startActivity(mainIntent);
	                Splash.this.finish();
	            }
	        }, SPLASH_DISPLAY_LENGTH);
        }
    }

	private boolean firstTime() {
		db = dbh.getWritableDatabase();
		String[] rows = { SettingsContract.USER, SettingsContract.LEVEL };
		Cursor c = dbh.settings.query(rows, null, null, null, false, 1, db);
		return c.getCount() == 0;
	}
}
