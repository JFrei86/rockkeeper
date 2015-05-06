/** FILENAME: MainActivity.java
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
 *    The main activity of the application - maintains the three fragments that make
 *    up the different page tabs and handles location information
 */

package transcend.rockeeper.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import transcend.rockeeper.data.Contract;
import transcend.rockeeper.data.LocationContract;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
import activities.rockeeper.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("InflateParams")
@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity implements ActionBar.TabListener
{
    private static final String PREF_LOCATION = "location";
    private static final int DASHBOARD_POS = 1;
    private static final int GOALS_POS = 2;

    private SharedPreferences sharedPrefs;

    RoutesFragment routes;
    DashboardFragment dash;
    GoalsFragment goals;

    private DatabaseHelper dbh = new DatabaseHelper(this, null);
    private SQLiteDatabase db;

    @SuppressLint("UseSparseArrays")
	private HashMap<Long, LocationContract.Location> locationMap = new HashMap<Long, LocationContract.Location>();
    private long currentLocId = 1;
    private LocationContract.Location currentLoc;
	private ActionBar actionBar;
	private ViewPager mViewPager;
	private SectionsPagerAdapter mSectionsPagerAdapter;

    public void onBackPressed(){}

/*********************************** LIFECYCLE METHODS ********************************/

    /** Called when the activity is created - handle initializations */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve any saved preferences
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences( this );
        currentLocId = sharedPrefs.getLong( PREF_LOCATION, 1 );

        // Set up the action bar.
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Retrieve the database and refresh locations
        db = dbh.getWritableDatabase();
        refreshLocations(currentLocId);
    }

    /** Initializes the page fragments and sets up the page view */
    public void setupPager() {

        routes = RoutesFragment.newInstance( currentLocId );
        dash = DashboardFragment.newInstance();
        goals = GoalsFragment.newInstance();

        // Adapter that will return a fragment for each section of the activity
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding tab
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++)
        {
            actionBar.addTab( actionBar.newTab()
                                       .setText(mSectionsPagerAdapter.getPageTitle(i))
                                       .setTabListener(this));
        }

        mViewPager.setCurrentItem( 1 );
    }

    /** Called when the activity is stopped (i.e. no longer visible on screen) */
    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong( PREF_LOCATION, currentLocId );
        editor.apply();
        Log.i( "rockeeper", "Preferences saved" );
    }

/****************************** MENU METHODS *****************************/

    /** Initializes options menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /** Called when an options menu item is selected - take the appropriate action */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch( id ) {
            case R.id.action_place:
                showChangeLocationDialog();
                return true;
            case R.id.action_add_place:
                showAddLocationDialog();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent( MainActivity.this, SettingsActivity.class );
                MainActivity.this.startActivity( intent );
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

/****************************** TAB METHODS *********************************/

    /** Called when the user selects a particular tab */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());

        // Refresh the appropriate views
        if( tab.getPosition() == DASHBOARD_POS ){
        	dash.refreshChart();
        } else if( tab.getPosition() == GOALS_POS ){
        	goals.refresh();
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

/****************************** LOCATION METHODS ********************************/

    /** Return the current location object */
    public LocationContract.Location getCurrentLocation() {
        return currentLoc;
    }

    /** Return the location map */
    public HashMap<Long, LocationContract.Location> getLocations() {
        return locationMap;
    }

    /** Retrieves the location matching the given id */
    public LocationContract.Location getLocationFromId( long loc_id ) {
        return locationMap.get( loc_id );
    }

    /** Updates the current location to the location matching loc_id */
    public void updateCurrentLocation( final long loc_id ) {
        LocationContract.Location loc = locationMap.get( loc_id );
        currentLocId = loc_id;
        currentLoc = loc;
        actionBar.setTitle( currentLoc.get( LocationContract.NAME ) );
        actionBar.setSubtitle( currentLoc.get( LocationContract.CITY ) );
        if( routes != null ) routes.getRoutes( loc_id );
    }

    /** Refreshes the location map by pulling from the database */
    public void refreshLocations( final long loc_id ) {
        Transaction t = new Transaction(db) {
            public void task(SQLiteDatabase db) {
                locationMap.clear();
                Cursor c = dbh.locations.query(new String[] { LocationContract._ID, LocationContract.NAME, LocationContract.CITY }, null, null, LocationContract._ID, true, null, db);
                c.moveToFirst();
                while( !c.isAfterLast() ) {
                    locationMap.put(c.getLong(c.getColumnIndex(LocationContract._ID)), dbh.locations.build(c));
                    c.moveToNext();
                }
            }
            public void onComplete() {
                //Log.i("UpdateLocation", "Location Updated.");
                updateCurrentLocation( loc_id );
                if( mViewPager == null ) setupPager();
            }
            public void onProgressUpdate(Contract.Unit... data) {}
        };
        t.run(true, true);
    }

    /** Displays a dialog allowing the user to select a location */
    public void showChangeLocationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder( this, AlertDialog.THEME_HOLO_LIGHT );
        builder.setTitle( R.string.title_location_dialog );

        // Get array of locations from the location map
        final ArrayList<LocationContract.Location> locations = new ArrayList<LocationContract.Location>( locationMap.values() );
        Iterator<LocationContract.Location> iter = locations.iterator();
        String[] locNames = new String[locations.size()];
        int c = 0;
        while( iter.hasNext() ) {
            locNames[c] = (iter.next()).get( LocationContract.NAME );
            ++c;
        }

        // Set up the dialog builder and show the dialog
        builder.setItems( locNames, new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
                Log.d("LocationDialog", ""+locations.get(which).get( LocationContract.NAME ));
                updateCurrentLocation(Long.parseLong(locations.get(which).get(LocationContract._ID)));
            }
        });
        builder.show();
    }

    /** Displays a dialog allowing the user to add a new location */
    public void showAddLocationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder( this, AlertDialog.THEME_HOLO_LIGHT );
        builder.setTitle( R.string.title_add_loc_dialog );

        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate( R.layout.dialog_add_location, null );
        builder.setView( dialogView );

        builder.setPositiveButton( "Add", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
                final EditText locName = (EditText) dialogView.findViewById(R.id.add_location_name);
                final EditText locCity = (EditText) dialogView.findViewById(R.id.add_location_city);
                final CheckBox switchTo = (CheckBox) dialogView.findViewById(R.id.add_location_switch);

                final String name = locName.getText().toString();
                final String city = locCity.getText().toString();

                if( name.equals("") || city.equals("") ) {
                    Toast.makeText( MainActivity.this, "Field(s) left blank. Try again", Toast.LENGTH_LONG ).show();
                    return;
                }

                final LocationContract.Location newLoc = dbh.locations.build( name, city );
                Transaction t = new Transaction(db) {
                    public void task( SQLiteDatabase db ) {
                        dbh.locations.insert( newLoc, db );
                    }
                    public void onComplete() {
                        if( switchTo.isChecked() ) {
                            refreshLocations( Long.parseLong(newLoc.get(LocationContract._ID)) );
                        } else {
                            refreshLocations( currentLocId );
                        }
                    }
                    public void onProgressUpdate( Contract.Unit... data ) {}
                };
                t.run(true, true);
            }
        });
        builder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

/************************** BUTTON LISTENER METHODS **************************/

    public void addRoute(View v){
		this.routes.addRoute(v);
	}
	
	public void editRoute(View v){
		this.routes.editRoute(v);
	}
	
	public void deleteRoute(View v){
		this.routes.deleteRoute(v);
	}

    public void addGoal(View v){
		this.goals.addGoal(v);
	}
	
	public void editGoal(View v){
		this.goals.editGoal(v);
	}
	
	public void deleteGoal(View v){
		this.goals.deleteGoal(v);
	}

/********************************** ADAPTERS *********************************/

    /** Custom adapter for the ViewPager */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        // Returns the fragment at the given position
        @Override
        public Fragment getItem(int position)
        {
            if( position == 0 ){	
            	return routes;
            }
            else if( position == 1 ){
            	return dash;
            }
            else //( position == 2 )
            	return goals;
        }

        // Returns the number of pages
        @Override
        public int getCount()
        {
            return 3;
        }

        // Returns the title of the selected page.
        @Override
        public CharSequence getPageTitle(int position)
        {
            Locale l = Locale.getDefault();
            switch (position)
            {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }
}
