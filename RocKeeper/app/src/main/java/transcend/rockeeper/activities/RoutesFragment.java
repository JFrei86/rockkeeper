/** FILENAME: RoutesFragment.java
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
 *    Fragment for the routes page, showing the list of routes filtered by location
 */

package transcend.rockeeper.activities;

import java.util.ArrayList;
import java.util.List;

import transcend.rockeeper.data.Contract.Unit;
import transcend.rockeeper.data.LocationContract;
import transcend.rockeeper.data.RouteContract;
import transcend.rockeeper.data.RouteContract.Route;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
import activities.rockeeper.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;


public class RoutesFragment extends Fragment implements RouteDialogFragment.RouteDialogListener, AdapterView.OnItemClickListener {
	
	private static final String ARG_PARAM1 = "locId";
	private String mParam1;

    private ArrayList<Route> routes = new ArrayList<Route>();    // routes stored here after database retrieval

    private DatabaseHelper dbh;
    private SQLiteDatabase db;

    private Activity mainActivity;

    private ListView listview;
    private int selectedItem = -1;      // the index of the list item selected

    private long initialLocID = 1;      // the ID of the location initially passed to the fragment (default 1)


/******************** LIFECYCLE METHODS ************************/

    /** Returns a new instance of the fragment */
    public static RoutesFragment newInstance( long loc_id ) {
        RoutesFragment fragment = new RoutesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, loc_id + "");
        fragment.setArguments(args);
        Log.d( "rockeeper", "RoutesFragment: newInstance()" );
        return fragment;
    }

    public RoutesFragment() {
        // Required empty public constructor
    }

    /** Called when the fragment is created - handle initializations */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            initialLocID = Long.parseLong(mParam1);
        }
        dbh = new DatabaseHelper(this.getActivity(), null);
        db = dbh.getWritableDatabase();
        getRoutes( initialLocID );
        Log.d( "rockeeper", "RoutesFragment: onCreate()" );
    }

    /** Sets up the view for the fragment */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d( "rockeeper", "RoutesFragment(): onCreateView()" );
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_routes, container, false);
    }

    /** Called when the fragment's associated activity has been created */
    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = this.getActivity();
        listview = (ListView) mainActivity.findViewById(R.id.listview);
        listview.setAdapter( new RouteListAdapter( mainActivity, routes ));
        listview.setOnItemClickListener( this );
        Log.d( "rockeeper", "RoutesFragment(): onActivityCreated()" );
    }

/************************* DIALOG HANDLERS *****************************/

    /** Called if the user has selected the positive button (i.e. 'OK') of the dialog */
    public void onRouteDialogPositiveClick( DialogFragment dialog, final Route edit ) {

        final EditText name = (EditText) dialog.getDialog().findViewById(R.id.routeDialogName);
        final RadioButton rope = (RadioButton) dialog.getDialog().findViewById(R.id.topropeRB);
        final NumberPicker difficulty = (NumberPicker) dialog.getDialog().findViewById(R.id.routeDifficultyPicker);
        final Spinner color = (Spinner) dialog.getDialog().findViewById(R.id.routeColorPicker);
        final EditText points = (EditText) dialog.getDialog().findViewById(R.id.routePoints);

        // Pull the values from the fields
        final int col_val = (Integer)color.getSelectedItem();
        String diff = getResources().getStringArray(R.array.boulder_levels)[difficulty.getValue()];
        if(rope.isChecked())
            diff = getResources().getStringArray(R.array.rope_levels)[difficulty.getValue()];
        final String name_val = name.getText().toString();
        final int pts = Integer.parseInt( points.getText().toString() );
        final Route r = dbh.routes.build(diff, 0, Long.parseLong(mParam1), col_val, name_val, 0, pts);

        // Insert or update the database entry accordingly
        Transaction t = new Transaction(db){
            public void task(SQLiteDatabase db) {
                if(edit == null){
                    dbh.routes.insert(r, db);
                }
                else{
                    dbh.routes.update(r, RouteContract._ID + "=" + edit.get(RouteContract._ID), null, db);
                }
            }
            public void onComplete() {
                if(edit != null){
                    if(selectedItem == -1)
                        return;
                    routes.set(selectedItem, r);
                }
                else{
                    routes.add(r);
                    click(listview, selectedItem);
                }
                ((RouteListAdapter)listview.getAdapter()).notifyDataSetChanged();
            }
            public void onProgressUpdate(Unit... data) {}
        };
        t.run(true,true);
    }

/**************************** BUTTON HANDLERS ***************************/

    /** Launch the route dialog in add mode */
    public void addRoute( View v ){
        Bundle args = new Bundle();
        args.putInt( "selectedItem", -1 );
        RouteDialogFragment d = new RouteDialogFragment();
        d.setArguments( args );
        d.setTargetFragment( this, 1 );
        d.show( getFragmentManager(), "RouteDialog" );
	}

    /** Launch the route dialog in edit mode */
	public void editRoute( View v ){
        Bundle args = new Bundle();
        args.putInt( "selectedItem", selectedItem );
        RouteDialogFragment d = new RouteDialogFragment();
        d.setArguments( args );
        d.setTargetFragment( this, 1 );
        d.show( getFragmentManager(), "RouteDialog" );
	}

    /** Remove the selected route from the list and the database */
	public void deleteRoute( View v ){
		final Route delete = (Route) listview.getAdapter().getItem(selectedItem);
		routes.remove(delete);
		Transaction t = new Transaction(db){
			public void task(SQLiteDatabase db) {
				 dbh.routes.delete(RouteContract._ID + "=" + delete.get(RouteContract._ID), null, db);
			}
			public void onComplete() {
				routes.remove(delete);
				click(listview, selectedItem);
                ((RouteListAdapter)listview.getAdapter()).notifyDataSetChanged();
			}
			public void onProgressUpdate(Unit... data) {}
		};
		t.run(true, true);
	}

/******************************* LIST HANDLERS ******************************/

    /** Called when the user selects an item in the list */
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        click(view, position);
    }

    /** Handle what happens when a list item is clicked */
    private void click(View view, int position){
    	Button editB = (Button) getActivity().findViewById(R.id.editRouteButton);
        Button deleteB = (Button) getActivity().findViewById(R.id.deleteRouteButton);
        if( position == selectedItem ) {
            view.setSelected(false);
            view.setActivated(false);
            editB.setEnabled(false);
            deleteB.setEnabled(false);
            listview.clearChoices();
            selectedItem = -1;
        } else {
            view.setSelected( true );
            view.setActivated( true );
            editB.setEnabled( true );
            deleteB.setEnabled( true );
            selectedItem = position;
        }
    }

    /** Queries the database for the list of available routes */
    public void getRoutes( final long loc_id ) {
		Transaction t = new Transaction(db){
			public void task(SQLiteDatabase db) {
				routes.clear();
                Cursor c = dbh.routes.query(null, RouteContract.LOCATION + "=" + loc_id, null, RouteContract.DIFFICULTY, true, null, db);
                c.moveToFirst();
				while(!c.isAfterLast()){
					routes.add(dbh.routes.build(c));
                    c.moveToNext();
				}
			}
			public void onComplete(){
                Log.i("RoutesFragment", "Routes Loaded.");
                ((RouteListAdapter)listview.getAdapter()).notifyDataSetChanged();
            }
			public void onProgressUpdate(Unit... data) {}
		};
		t.run(true, true);
	}

    /** Custom adapter for the list of routes */
    private class RouteListAdapter extends ArrayAdapter<Route> {

        List<Route> routes;
        LayoutInflater inflater = null;

        public RouteListAdapter( Context context, ArrayList<Route> routes ) {
            super( context, 0, routes );
            this.routes = routes;
            this.inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        }

        /** Return the number of items in the list */
        @Override
        public int getCount() {
            return routes.size();
        }

        /** Return the Route at the given position */
        @Override
        public Route getItem( int position ) {
            return routes.get( position );
        }

        /** Return the ID of the item at the given position */
        @Override
        public long getItemId( int position ) {
            return position;
        }

        /** Get the view associated with the selected list item */
        @SuppressLint("InflateParams")
		@Override
        public View getView( final int position, View convertView, ViewGroup parent ) {
            View vi = convertView;
            if( vi == null )
                vi = inflater.inflate( R.layout.row, null );

            /* Handle displaying difficulty, color label, route name, location, etc. */
            TextView diffLevel = (TextView)vi.findViewById(R.id.DifficultyLevel);
            diffLevel.setText(routes.get(position).get(RouteContract.DIFFICULTY));

            TextView points = (TextView)vi.findViewById(R.id.Points);
            points.setText(routes.get(position).get(RouteContract.POINTS));

            View colorlabel = vi.findViewById( R.id.ColorLabel );
            colorlabel.setBackgroundColor( Integer.parseInt(routes.get(position).get(RouteContract.COLOR)) );

            TextView routeName = (TextView)vi.findViewById( R.id.RouteName );
            routeName.setText(routes.get(position).get(RouteContract.NAME));

            TextView routeLoc = (TextView)vi.findViewById( R.id.Location );
            long locID = Long.parseLong(routes.get(position).get(RouteContract.LOCATION));
            LocationContract.Location loc = ((MainActivity)mainActivity).getLocationFromId( locID );
            routeLoc.setText( loc.get( LocationContract.NAME ) );

            final TextView timesClimbed = (TextView)vi.findViewById( R.id.TimesClimbed );
            timesClimbed.setText(routes.get(position).get(RouteContract.NUM_ATTEMPTS));
            
            // Define what happens when the user clicks the "completed" check box
            final CheckBox completed = (CheckBox)vi.findViewById( R.id.checkboxComplete );
            int comp = Integer.parseInt(routes.get(position).get(RouteContract.COMPLETED));
            completed.setChecked(comp != 0);
            if(comp != 0) completed.setClickable(false);
            completed.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,
						final boolean isChecked) {
					Transaction t = new Transaction(db){
						public void task(SQLiteDatabase db) {
							routes.get(position).put(RouteContract.COMPLETED, (isChecked)?1:0);
							dbh.routes.update(routes.get(position), 
									RouteContract._ID + "=" + routes.get(position).get(RouteContract._ID), null, db);
						}
						public void onComplete() {
							if(isChecked){
								completed.setClickable(false);
							}
							dbh.stats.incrementStat(routes.get(position), RouteContract.COMPLETED, db);
							dbh.stats.incrementStat(routes.get(position), RouteContract.NUM_ATTEMPTS, db);
							dbh.goals.updateGoals(routes.get(position), RouteContract.COMPLETED, db);
							dbh.goals.updateGoals(routes.get(position), RouteContract.NUM_ATTEMPTS, db);
							listview.invalidateViews();
						}
						public void onProgressUpdate(Unit... data) {}
					};
					t.run(true, true);
				}
            });

            // Define what happens when the user clicks the attempt increment button
            Button inc = (Button)vi.findViewById(R.id.TimesClimbedIncrementor);
            inc.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					Transaction incAttempts = new Transaction(db){
						public void task(SQLiteDatabase db) {
							routes.get(position).put(RouteContract.NUM_ATTEMPTS, 
									Long.parseLong(routes.get(position).get(RouteContract.NUM_ATTEMPTS)) + 1);
							dbh.routes.update(routes.get(position), 
									RouteContract._ID + "=" + routes.get(position).get(RouteContract._ID), null, db);
							//Log.i("DEBUG", "Route update attempted. " + routes.get(position).toString());
						}
						public void onComplete() {
							dbh.stats.incrementStat(routes.get(position), RouteContract.NUM_ATTEMPTS, db);
							dbh.goals.updateGoals(routes.get(position), RouteContract.NUM_ATTEMPTS, db);
							listview.invalidateViews();
						}
						public void onProgressUpdate(Unit... data) {}
					};
					incAttempts.run(true, true);
				}
            });
            
            return vi;
        }
    }
}
