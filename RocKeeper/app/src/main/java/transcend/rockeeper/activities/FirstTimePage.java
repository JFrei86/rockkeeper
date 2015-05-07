/** FILENAME: FirstTimePage.java
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
 *    Activity that displays only when the user has launched the application for the
 *    first time after install, allowing user to enter his/her basic information.
 */

package transcend.rockeeper.activities;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import transcend.rockeeper.data.Contract.Unit;
import transcend.rockeeper.data.GoalContract;
import transcend.rockeeper.data.GoalContract.Goal;
import transcend.rockeeper.data.LocationContract;
import transcend.rockeeper.data.LocationContract.Location;
import transcend.rockeeper.data.RouteContract.Route;
import transcend.rockeeper.data.SettingsContract.Settings;
import transcend.rockeeper.data.StatContract.Stat;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
import activities.rockeeper.BuildConfig;
import activities.rockeeper.R;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;


public class FirstTimePage extends ActionBarActivity {

    /** Called when the activity is created - handle initializations */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_page);

        Spinner spinner = (Spinner)findViewById(R.id.experience_level);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.experience_array, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    /** Initializes the options menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_first_time_page, menu);
        return true;
    }

    /** Called when an options menu item is selected */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Disable user pressing back button */
    @Override
    public void onBackPressed(){}

    /** Prepares the main activity and launches it */
    public void launchMainPage(View view) {
    	final DatabaseHelper dbh = new DatabaseHelper(this, null);
    	SQLiteDatabase db = dbh.getWritableDatabase();

        // Get the values from the fields
    	final String location = ((EditText) this.findViewById(R.id.fav_location)).getText().toString();
        final String city = ((EditText) this.findViewById(R.id.location_city)).getText().toString();
    	final String name = ((EditText) this.findViewById(R.id.user_name)).getText().toString();
    	final Object level = ((Spinner) this.findViewById(R.id.experience_level)).getSelectedItem();

        // Warn the user if some fields are left blank
    	if(location.equals("") || name.equals("") || level == null){
    		this.findViewById(R.id.missingFirstFields).setVisibility(View.VISIBLE);
    		return;
    	}

        // Insert data into database
    	Transaction t = new Transaction(db){
			public void task(SQLiteDatabase db) {
				Settings s = dbh.settings.build(name, level.toString());
		    	Location l = dbh.locations.build(location, city);

                dbh.locations.insert(l, db);
                dbh.settings.insert(s, db);

                long locID = Long.parseLong( l.get( LocationContract._ID ) );
                Log.d( "FirstTimePage", "location ID is "+locID );
		    	
                if(BuildConfig.DEBUG){
                	for(int i = 0; i < 6; i++){
                		Random r = new Random((long)(Math.random() * 100000000));
                		int diff = r.nextInt(14);
                		int attempts = r.nextInt(4);
                		
                		boolean red = r.nextBoolean();
                		boolean green = r.nextBoolean();
                		boolean blue = r.nextBoolean();
                		String name = "Route " + (i + 1);
                		int completed = r.nextInt(2);
                		int points = 50 * (diff + 1);
                		
                		int color = 0xFF000000;
                		if(red) color += 0x00FF0000;
                		if(green) color += 0x0000FF00;
                		if(blue) color += 0x000000FF;
                		
                		Route a = dbh.routes.build("v"+diff, attempts, locID, color, name, completed, points);
                		dbh.routes.insert(a, db);
                	}
                	for(int i = 0; i < 360; i++){
                		Random r = new Random((long)(Math.random() * 100000000));
                		GregorianCalendar c = new GregorianCalendar();
                		c.set(Calendar.HOUR, 0);
                		c.set(Calendar.MINUTE, 0);
                		c.set(Calendar.SECOND, 0);
                		c.set(Calendar.MILLISECOND, 0);
                		c.add(Calendar.DATE, -i);
                		int x = r.nextInt(5);
                		int y = r.nextInt(3);
                		int z = y * (50 + 50 * (1 + r.nextInt(15)));
                		Stat a = dbh.stats.build(c.getTime(), x, y, z);
                		dbh.stats.insert(a, db);
                	}
                }
			}
			public void onComplete(){}
			public void onProgressUpdate(Unit... data){}
    	};
    	t.run(true, false);

        // Create the intent for MainActivity and start it
        Intent mainIntent = new Intent(this, MainActivity.class);
        this.startActivity(mainIntent);
        this.finish();
    }
}
