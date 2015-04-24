package transcend.rockeeper.activities;

import transcend.rockeeper.sqlite.DatabaseHelper;
import android.support.v4.app.Fragment;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import activities.rockeeper.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DashboardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    //private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment DashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
	
	public StatsGraph sg;

	private ProgressBar prog;

	private DatabaseHelper dbh;

	private SQLiteDatabase db;
	
	private int currentColumn = 0;
	private int currentRange = 0;
	
	private final int POINTS = 0;
	private final int ATTEMPTS = 1;
	private final int COMPLETED = 2;
	private final int WEEK = 0;
	private final int MONTH = 1;
	private final int YEAR = 2;
	
    public static DashboardFragment newInstance( ) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle b = new Bundle();
        fragment.setArguments(b);
        return fragment;
    }

	public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

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
    	spinner2.setAdapter(adapter2);
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
    	
    	sg = new StatsGraph(this, prog);
    }

	public void refreshChart() {
		if(sg != null)
			sg.refresh();
	}
}
