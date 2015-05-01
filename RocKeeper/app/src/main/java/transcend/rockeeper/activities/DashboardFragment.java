/** FILENAME: DashboardFragment.java
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
 *    Fragment for the dashboard page, showing the graph and top goals
 */

package transcend.rockeeper.activities;

import transcend.rockeeper.sqlite.DatabaseHelper;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import activities.rockeeper.R;

public class DashboardFragment extends Fragment {

    private static final String PREF_GRAPH_TYPE = "default_graph_type";
    private static final String PREF_GRAPH_TIME = "default_graph_time";
	
	public StatsGraph sg;
	private ProgressBar prog;

	private DatabaseHelper dbh;
	private SQLiteDatabase db;

    private SharedPreferences sharedPreferences;
	
	private int currentColumn = 0;
	private int currentRange = 0;
	
	private final int POINTS = 0;
	private final int ATTEMPTS = 1;
	private final int COMPLETED = 2;
	private final int WEEK = 0;
	private final int MONTH = 1;
	private final int YEAR = 2;

    /** Returns a new instance of the fragment */
    public static DashboardFragment newInstance( ) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle b = new Bundle();
        fragment.setArguments(b);
        return fragment;
    }

	public DashboardFragment() {
        // Required empty public constructor
    }

    /** Called when the fragment is created - handle initializations */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences( getActivity() );
    }

    /** Sets up the view of the fragment */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    /** Called when the activity associated with the fragment has been created */
    public void onActivityCreated(Bundle b){
    	super.onActivityCreated(b);
    	dbh = new DatabaseHelper(this.getActivity(), null);
    	db = dbh.getReadableDatabase();
    	prog = (ProgressBar)this.getActivity().findViewById(R.id.progress);
    	Spinner spinner1 = (Spinner)this.getActivity().findViewById(R.id.columnSelector);
    	Spinner spinner2 = (Spinner)this.getActivity().findViewById(R.id.rangeSelector);
    	ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this.getActivity(), R.array.columns, R.layout.spinner_small);
    	ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this.getActivity(), R.array.ranges, R.layout.spinner_small);
    	spinner1.setAdapter(adapter1);
        spinner2.setAdapter(adapter2);

        sg = new StatsGraph(this, prog);

        /* Get defaults from Preferences */
        int defaultColumn = Integer.parseInt( sharedPreferences.getString( PREF_GRAPH_TYPE, "0" ) );
        spinner1.setSelection( defaultColumn );
        if( defaultColumn == POINTS )
            sg.setColumn( StatsGraph.POINTS );
        else if( defaultColumn == ATTEMPTS )
            sg.setColumn( StatsGraph.ATTEMPTS );
        else if( defaultColumn == COMPLETED )
            sg.setColumn( StatsGraph.COMPLETED );
        int defaultRange = Integer.parseInt( sharedPreferences.getString( PREF_GRAPH_TIME, "0" ) );
        Log.d("DashboardFragment", ""+defaultRange);
        spinner2.setSelection( defaultRange );
        if( defaultRange == WEEK )
            sg.setRange( StatsGraph.WEEK );
        else if( defaultRange == MONTH )
            sg.setRange( StatsGraph.MONTH );
        else if( defaultRange == YEAR )
            sg.setRange( StatsGraph.YEAR );
        sg.refresh();
    	
    	currentColumn = spinner1.getSelectedItemPosition();
    	currentRange = spinner2.getSelectedItemPosition();
    	
    	spinner1.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(currentColumn != position){
					if(position == POINTS){
						sg.setColumn(StatsGraph.POINTS);
					}else if(position == ATTEMPTS){
						sg.setColumn(StatsGraph.ATTEMPTS);
					}else if(position == COMPLETED){
						sg.setColumn(StatsGraph.COMPLETED);
					}
					sg.refresh();
				}
				currentColumn = position;
			}
			public void onNothingSelected(AdapterView<?> parent) {}
    	});
    	//spinner2.setAdapter(adapter2);
    	spinner2.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(currentRange != position){
					if(position == WEEK){
						sg.setRange(StatsGraph.WEEK);
					}else if(position == MONTH){
						sg.setRange(StatsGraph.MONTH);
					}else if(position == YEAR){
						sg.setRange(StatsGraph.YEAR);
					}
					sg.refresh();
				}
				currentRange = position;
			}
			public void onNothingSelected(AdapterView<?> parent) {}
    	});
    	

    }

    /** Refreshes the chart */
	public void refreshChart() {
		if(sg != null)
			sg.refresh();
	}
}
