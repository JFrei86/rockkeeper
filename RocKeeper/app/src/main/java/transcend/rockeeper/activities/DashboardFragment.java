package transcend.rockeeper.activities;

import transcend.rockeeper.data.Contract.Unit;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
import android.support.v4.app.Fragment;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
//import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
    	
    	sg = new StatsGraph(this, prog);
    }

//	private void getData() {
//		prog.setVisibility(View.VISIBLE);
//		Transaction t = new Transaction(db){
//			public void task(SQLiteDatabase db) {
//				Cursor c = dbh.stats.query(null, where, args, sortBy, descending, limit, db)
//			}
//			public void onComplete() {
//				prog.setVi
//			}
//			public void onProgressUpdate(Unit... data) {}
//		};
//		t.run(true, true);
//	}
}
