package transcend.rockeeper.activities;

import transcend.rockeeper.data.LocationContract.Location;
import transcend.rockeeper.data.SettingsContract.Settings;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
import activities.rockeeper.R;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

    public void onBackPressed(){}
    
    public void launchMainPage(View view) {
    	final DatabaseHelper dbh = new DatabaseHelper(this, null);
    	SQLiteDatabase db = dbh.getWritableDatabase();
    	
    	final String location = ((TextView) this.findViewById(R.id.fav_location)).getText().toString();
    	final String name = ((TextView) this.findViewById(R.id.user_name)).getText().toString();
    	final Object level = ((Spinner) this.findViewById(R.id.experience_level)).getSelectedItem();
    	
    	if(location.equals("") || name.equals("") || level == null){
    		this.findViewById(R.id.missingFirstFields).setVisibility(View.VISIBLE);
    		return;
    	}
    	
    	Transaction t = new Transaction(db){
			public void task(SQLiteDatabase db) {
				Settings s = dbh.settings.build(name, level.toString());
		    	Location l = dbh.locations.build(location);
		    	dbh.locations.insert(l, db);
		    	dbh.settings.insert(s, db);
			}
			public void onComplete(){}
    	};
    	
    	t.run(true, true);
    	
        Intent mainIntent = new Intent(this, MainActivity.class);
        this.startActivity(mainIntent);
        this.finish();
    }
}
