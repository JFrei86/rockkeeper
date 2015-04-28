package transcend.rockeeper.activities;

import java.util.Date;

import transcend.rockeeper.data.Contract.Unit;
import transcend.rockeeper.data.GoalContract;
import transcend.rockeeper.data.GoalContract.Goal;
import transcend.rockeeper.data.LocationContract;
import transcend.rockeeper.data.LocationContract.Location;
import transcend.rockeeper.data.RouteContract.Route;
import transcend.rockeeper.data.SettingsContract.Settings;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
import activities.rockeeper.BuildConfig;
import activities.rockeeper.R;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;


public class FirstTimePage extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_page);

        Spinner spinner = (Spinner)findViewById(R.id.experience_level);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.experience_array, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_first_time_page, menu);
        return true;
    }

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

    @Override
    public void onBackPressed(){}
    
    public void launchMainPage(View view) {
    	final DatabaseHelper dbh = new DatabaseHelper(this, null);
    	SQLiteDatabase db = dbh.getWritableDatabase();
    	
    	final String location = ((EditText) this.findViewById(R.id.fav_location)).getText().toString();
        final String city = ((EditText) this.findViewById(R.id.location_city)).getText().toString();
    	final String name = ((EditText) this.findViewById(R.id.user_name)).getText().toString();
    	final Object level = ((Spinner) this.findViewById(R.id.experience_level)).getSelectedItem();
    	
    	if(location.equals("") || name.equals("") || level == null){
    		this.findViewById(R.id.missingFirstFields).setVisibility(View.VISIBLE);
    		return;
    	}
    	
    	Transaction t = new Transaction(db){
			public void task(SQLiteDatabase db) {
				Settings s = dbh.settings.build(name, level.toString());
		    	Location l = dbh.locations.build(location, city);

                dbh.locations.insert(l, db);
                dbh.settings.insert(s, db);

                long locID = Long.parseLong( l.get( LocationContract._ID ) );
                Log.d( "FirstTimePage", "location ID is "+locID );
		    	
                if(BuildConfig.DEBUG){
			    	Route r1 = dbh.routes.build("v0", 2, locID, 0xFF00FF00, "Rainbow Road", 0, 50);
			    	Route r2 = dbh.routes.build("v3", 0, locID, 0xFFFF0000, "Death Drop", 0, 200);
			    	Route r3 = dbh.routes.build("v2", 1, locID, 0xFF0000FF, "Inner Peaks", 0, 100);
			    	Goal g1 = dbh.goals.build(GoalContract.POINTS, 4000, new Date().getTime() + 2592000000l);
			    	Goal g2 = dbh.goals.build(GoalContract.COMPLETED, 10, new Date().getTime() + 2592000000l);
			    	Goal g3 = dbh.goals.build(GoalContract.ATTEMPTS, 20, new Date().getTime() + (2592000000l / 4));
			    	
			    	dbh.routes.insert(r1, db);
			    	dbh.routes.insert(r2, db);
			    	dbh.routes.insert(r3, db);
			    	dbh.goals.insert(g1, db);
			    	dbh.goals.insert(g2, db);
			    	dbh.goals.insert(g3, db);
                }
			}
			public void onComplete(){}
			public void onProgressUpdate(Unit... data){}
    	};
    	
    	t.run(true, true);
    	
        Intent mainIntent = new Intent(this, MainActivity.class);
        this.startActivity(mainIntent);
        this.finish();
    }
}
