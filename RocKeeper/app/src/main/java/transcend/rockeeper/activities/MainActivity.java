package transcend.rockeeper.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import activities.rockeeper.R;
import transcend.rockeeper.data.Contract;
import transcend.rockeeper.data.LocationContract;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
//import transcend.rockeeper.activities.DashboardFragment;

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity implements ActionBar.TabListener
{
    //private static final String PREF_FILE = "rockeeper_preferences";
    private static final String PREF_LOCATION = "location";
    SharedPreferences sharedPrefs;

    ActionBar actionBar;

    SectionsPagerAdapter mSectionsPagerAdapter;

    RoutesFragment routes;
    DashboardFragment dash;
    
    ViewPager mViewPager;

    @SuppressLint("UseSparseArrays")
	private HashMap<Long, LocationContract.Location> locationMap = new HashMap<Long, LocationContract.Location>();

    private DatabaseHelper dbh = new DatabaseHelper(this, null);
    private SQLiteDatabase db;

    private long currentLocId = 1;
    private LocationContract.Location currentLoc;

    public void onBackPressed(){}

/*********************************** LIFECYCLE METHODS ********************************/

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

        db = dbh.getWritableDatabase();
        refreshLocations(currentLocId);
    }

    public void setupPager() {

        routes = RoutesFragment.newInstance( currentLocId );
        dash = DashboardFragment.newInstance();

        // Create the adapter that will return a fragment for each of the four
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++)
        {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        //db = dbh.getReadableDatabase();

        mViewPager.setCurrentItem( 1 );

        //routes.getRoutes( currentLocId );
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong( PREF_LOCATION, currentLocId );
        editor.apply();
        Log.i( "rockeeper", "Preferences saved" );
    }

/****************************** MENU METHODS *****************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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
                // settings stuff
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

/****************************** TAB METHODS *********************************/

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
        /*if( tab.getPosition() == 0 ) {
            routes.getRoutes( currentLocId );
        }*/

        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
    }

/****************************** LOCATION METHODS ********************************/

    public LocationContract.Location getCurrentLocation() {
        return currentLoc;
    }

    public LocationContract.Location getLocationFromId( long loc_id ) {
        return locationMap.get( loc_id );
    }

    public void updateCurrentLocation( final long loc_id ) {
        LocationContract.Location loc = locationMap.get( loc_id );
        currentLocId = loc_id;
        currentLoc = loc;
        actionBar.setTitle( currentLoc.get( LocationContract.NAME ) );
        actionBar.setSubtitle( currentLoc.get( LocationContract.CITY ) );
        if( routes != null ) routes.getRoutes( loc_id );
    }

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

    public void showChangeLocationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder( this, AlertDialog.THEME_HOLO_LIGHT );
        builder.setTitle( R.string.title_location_dialog );

        final ArrayList<LocationContract.Location> locations = new ArrayList<LocationContract.Location>( locationMap.values() );
        Iterator<LocationContract.Location> iter = locations.iterator();
        String[] locNames = new String[locations.size()];
        int c = 0;
        while( iter.hasNext() ) {
            locNames[c] = (iter.next()).get( LocationContract.NAME );
            ++c;
        }

        builder.setItems( locNames, new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
                Log.d("LocationDialog", ""+locations.get(which).get( LocationContract.NAME ));
                updateCurrentLocation(Long.parseLong(locations.get(which).get(LocationContract._ID)));
            }
        });
        builder.show();
    }

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

                if( name == null || city == null ) {
                    // TODO: Toast or something else here?
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

/********************************** ADAPTERS *********************************/

    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            if( position == 0 ){	
            	return routes;
            }//TODO: make this the actual loc_id
            if( position == 1 ){
            	return dash;
            }
            else
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
                return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount()
        {
            // Show 3 total pages.
            return 3;
        }

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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
    {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber)
        {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
