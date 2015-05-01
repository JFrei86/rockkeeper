/** FILENAME: SettingsFragment.java
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
 *    Fragment which maintains a list of settings
 */

package transcend.rockeeper.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.DropBoxManager;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.util.Log;
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

public class SettingsFragment extends PreferenceFragment
{
    private static final int GRAPH_PREF_POINTS = 0;
    private static final int GRAPH_PREF_ATTEMPTS = 1;
    private static final int GRAPH_PREF_COMPLETED = 2;
    private static final int GRAPH_PREF_WEEK = 0;
    private static final int GRAPH_PREF_MONTH = 1;
    private static final int GRAPH_PREF_YEAR = 2;

    SharedPreferences.OnSharedPreferenceChangeListener listener;

    //MainActivity mainActivity;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        addPreferencesFromResource( R.xml.preferences );

        ListPreference graphTypePref = (ListPreference) findPreference( "default_graph_type" );
        int val = Integer.parseInt( graphTypePref.getValue() );
        if( val == GRAPH_PREF_POINTS )
            graphTypePref.setSummary( "Points over time" );
        else if( val == GRAPH_PREF_ATTEMPTS )
            graphTypePref.setSummary( "Attempted routes over time" );
        else if( val == GRAPH_PREF_COMPLETED )
            graphTypePref.setSummary( "Completed routes over time" );

        ListPreference graphTimePref = (ListPreference) findPreference( "default_graph_time" );
        val = Integer.parseInt( graphTimePref.getValue() );
        if( val == GRAPH_PREF_WEEK )
            graphTimePref.setSummary( "Week" );
        else if( val == GRAPH_PREF_MONTH )
            graphTimePref.setSummary( "Month" );
        else if( val == GRAPH_PREF_YEAR )
            graphTimePref.setSummary( "Year" );

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                if( key.equals("location")) return;

                Log.d("SettingsFragment", key);

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
        };
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
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener( listener );
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( listener );
        super.onPause();
    }

    /*@Override
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
        *//*if( key.equals( "default_location" ) ) {
            long loc_id = Long.parseLong( sharedPreferences.getString( key, "" ) );
            String locName = mainActivity.getLocationFromId( loc_id ).get( LocationContract.NAME );
            ListPreference defLoc = (ListPreference) findPreference( key );
            defLoc.setSummary( locName );
        }*//*
        if( key.equals("location")) return;

        ListPreference defLoc = (ListPreference) findPreference( key );
        int type = Integer.parseInt( sharedPreferences.getString( key, "" ) );

        *//*if( key.equals( "default_graph_type" ) ) {
            if( type == GRAPH_PREF_POINTS )
                defLoc.setSummary( "Points over time" );
            else if( type == GRAPH_PREF_ATTEMPTS )
                defLoc.setSummary( "Attempted routes over time" );
            else if( type == GRAPH_PREF_COMPLETED )
                defLoc.setSummary( "Completed routes over time" );
        }
        else*//* if( key.equals( "default_graph_time" ) ) {
            if( type == GRAPH_PREF_WEEK )
                defLoc.setSummary( "Week" );
            else if( type == GRAPH_PREF_MONTH )
                defLoc.setSummary( "Month" );
            else if( type == GRAPH_PREF_YEAR )
                defLoc.setSummary( "Year" );
        }
    }*/
}
