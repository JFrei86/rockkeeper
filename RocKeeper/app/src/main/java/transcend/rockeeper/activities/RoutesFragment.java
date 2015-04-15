package transcend.rockeeper.activities;

import java.util.ArrayList;
import java.util.List;

import transcend.rockeeper.data.Contract.Unit;
import transcend.rockeeper.data.RouteContract;
import transcend.rockeeper.data.RouteContract.Route;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
//import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import activities.rockeeper.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RoutesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RoutesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoutesFragment extends Fragment {
	
	private final int BATCH = 10;
	private static final String ARG_PARAM1 = "locId";
    
	private String mParam1;
    private List<Route> routes = new ArrayList<Route>();

    private DatabaseHelper dbh = new DatabaseHelper(this.getActivity(), null);
    private SQLiteDatabase db;

    private ListView listview;

    //private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param loc_id An INTEGER (long) id for a location in the database. 
     * This is used to retrieve all routes for an specific location
     * @return A new instance of fragment RoutesFragment.
     */

    // TODO: Rename and change types and number of parameters
    public static RoutesFragment newInstance( long loc_id ) {
        RoutesFragment fragment = new RoutesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, loc_id + "");
        fragment.setArguments(args);
        return fragment;
    }

    public RoutesFragment() {
        // Required empty public constructor
    }

    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            long loc_id = Long.parseLong(mParam1);
            db = dbh.getWritableDatabase();
            getRoutes(loc_id);
        }
    }*/

    private void getRoutes(final long loc_id) {
		Transaction t = new Transaction(db){
			public void task(SQLiteDatabase db) {
				Cursor c = dbh.routes.query(null, RouteContract.LOCATION + "=" + loc_id, null, RouteContract.DIFFICULTY, true, null, db);
				while(!c.isAfterLast()){
					for(int i = 0; i < BATCH && !c.isAfterLast(); i++){
						c.moveToNext();
						routes.add(dbh.routes.build(c));
					}
				}
			}
			public void onComplete(){Log.i("RoutesFragment", "Routes Loaded.");}
			public void onProgressUpdate(Unit... data) {}
		};
		t.run(true, true);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        listview = (ListView) container.findViewById(R.id.listview);
        listview.setAdapter( new RouteListAdapter( this.getActivity() ));
        return inflater.inflate(R.layout.fragment_routes, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
   /* public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    /*@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }*/


    private class RouteListAdapter extends BaseAdapter {

        Context context;
        // Data stored how?
        LayoutInflater inflater = null;

        public RouteListAdapter( Context context /* Data here */ ) {
            this.context = context;
            // data intialization
            this.inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        }

        @Override
        public int getCount() {
            return 0;   // data size
        }

        @Override
        public Object getItem( int position ) {
            return null;
            // return routes[position];
        }

        @Override
        public long getItemId( int position ) {
            return position;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            View vi = convertView;
            if( vi == null )
                vi = inflater.inflate( R.layout.row, null );

            /* Handle displaying difficulty, color label, route name, location, etc. */

            return vi;
        }
    }

}
