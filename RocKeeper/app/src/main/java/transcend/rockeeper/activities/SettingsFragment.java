package transcend.rockeeper.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.DropBoxManager;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import activities.rockeeper.R;
import transcend.rockeeper.data.LocationContract;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final int GRAPH_PREF_POINTS = 0;
    private static final int GRAPH_PREF_ATTEMPTS = 1;
    private static final int GRAPH_PREF_COMPLETED = 2;
    private static final int GRAPH_PREF_WEEK = 0;
    private static final int GRAPH_PREF_MONTH = 1;
    private static final int GRAPH_PREF_YEAR = 2;

    //MainActivity mainActivity;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        addPreferencesFromResource( R.xml.preferences );
    }

    /*@Override
    public void onAttach( Activity activity ) {
        mainActivity = ((SettingsActivity)activity).getMainActivity();

        Collection<LocationContract.Location> locations = mainActivity.getLocations().values();
        String[] locNames = new String[locations.size()];
        String[] locIDs = new String[locations.size()];
        Iterator<LocationContract.Location> iter = locations.iterator();
        int c = 0;
        while( iter.hasNext() ) {
            LocationContract.Location loc = iter.next();
            locNames[c] = loc.get( LocationContract.NAME );
            locIDs[c] = loc.get( LocationContract._ID );
            ++c;
        }
        ListPreference defLoc = (ListPreference) findPreference( "default_location" );
        defLoc.setEntries( locNames );
        defLoc.setEntryValues( locIDs );
    }*/

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
        /*if( key.equals( "default_location" ) ) {
            long loc_id = Long.parseLong( sharedPreferences.getString( key, "" ) );
            String locName = mainActivity.getLocationFromId( loc_id ).get( LocationContract.NAME );
            ListPreference defLoc = (ListPreference) findPreference( key );
            defLoc.setSummary( locName );
        }*/
        ListPreference defLoc = (ListPreference) findPreference( key );
        int type = Integer.parseInt( sharedPreferences.getString( key, "" ) );

        if( key.equals( "default_graph_type" ) ) {
            if( type == GRAPH_PREF_POINTS )
                defLoc.setSummary( "Points over time" );
            else if( type == GRAPH_PREF_ATTEMPTS )
                defLoc.setSummary( "Attempted routes over time" );
            else if( type == GRAPH_PREF_COMPLETED )
                defLoc.setSummary( "Completed routes over time" );
        }
        else if( key.equals( "default_graph_time" ) ) {
            if( type == GRAPH_PREF_WEEK )
                defLoc.setSummary( "Week" );
            else if( type == GRAPH_PREF_MONTH )
                defLoc.setSummary( "Month" );
            else if( type == GRAPH_PREF_YEAR )
                defLoc.setSummary( "Year" );
        }
    }
}
