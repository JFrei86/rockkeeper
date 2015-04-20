package transcend.rockeeper.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import transcend.rockeeper.data.Contract.Unit;
import transcend.rockeeper.data.LocationContract.Location;
import transcend.rockeeper.data.LocationContract;
import transcend.rockeeper.data.RouteContract;
import transcend.rockeeper.data.RouteContract.Route;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
import android.os.Bundle;
//import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import activities.rockeeper.R;


public class RoutesFragment extends Fragment implements OnClickListener, AdapterView.OnItemClickListener {

	private final int BATCH = 10;       // buffer size for grabbing items from database

	private static final String ARG_PARAM1 = "locId";
    
	private String mParam1;
    private List<Route> routes = new ArrayList<Route>();    // routes stored here after database retrieval


    private DatabaseHelper dbh;
    private SQLiteDatabase db;

    private ListView listview;
    private int selectedItem = -1;      // the index of the list item selected

    HashMap<String, String> colorMap = new HashMap<>();

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            long loc_id = Long.parseLong(mParam1);
            dbh = new DatabaseHelper(this.getActivity(), null);
            db = dbh.getWritableDatabase();
            getRoutes(loc_id);
        }

        colorMap.put("0xFFFF0000", "Red");
        colorMap.put("0xFFFF8800", "Orange");
        colorMap.put("0xFFFFFF00", "Yellow");
        colorMap.put("0xFF00FF00", "Green");
        colorMap.put("0xFF0000FF", "Blue");
        colorMap.put("0xFFFF00FF", "Purple");
        colorMap.put("0xFFFFFFFF", "White");
        colorMap.put("0xFF000000", "Black");
    }
   
    public Dialog createDialog(final Route edit, final ListView lv) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View dialogView = inflater.inflate(R.layout.fragment_create_route, null);
        builder.setView( dialogView );

        final NumberPicker difficulty = (NumberPicker) dialogView.findViewById(R.id.routeDifficultyPicker);
        difficulty.setDisplayedValues( getResources().getStringArray(R.array.boulder_levels) );
        difficulty.setMinValue(0);
        difficulty.setMaxValue(14);

        final EditText name = (EditText) dialogView.findViewById(R.id.routeDialogName);
        
        NumberPicker color = (NumberPicker) dialogView.findViewById(R.id.routeColorPicker);
        String[] colors = colorMap.values().toArray( new String[colorMap.size()] );
        color.setDisplayedValues( colors );
        color.setMinValue(0);
        color.setMaxValue(colors.length-1);

        // If opened in edit mode, populate the fields with existing values
        if( edit != null ) {
            //Route routeToEdit = (Route) listview.getAdapter().getItem(selectedItem);
            name.setText( edit.get( RouteContract.NAME ));
            String routeDiff = edit.get( RouteContract.DIFFICULTY );
            if( routeDiff.charAt(0) == '5' ) {
                difficulty.setDisplayedValues( getResources().getStringArray(R.array.rope_levels));
                difficulty.setMaxValue(11);
                // TODO: actually set value here
                difficulty.invalidate();
            }
            //else
                // TODO: actually set value here

            // TODO: properly set color value
            //color.setValue( colors.indexOf(colorMap.get(Integer.parseInt(edit.get(RouteContract.COLOR)))));
        }
        
        final RadioButton rope = (RadioButton) dialogView.findViewById(R.id.topropeRB);
        final RadioButton boulder = (RadioButton) dialogView.findViewById(R.id.boulderRB);
        rope.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
                    difficulty.setMinValue(0);
                    difficulty.setMaxValue(11);
					difficulty.setDisplayedValues( getResources().getStringArray(R.array.rope_levels) );
					//difficulty.setValue(0);
					difficulty.invalidate();
				}
			}
             });
        boulder.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					difficulty.setDisplayedValues( getResources().getStringArray(R.array.boulder_levels) );
                    difficulty.setMinValue(0);
                    difficulty.setMaxValue(14);
					//difficulty.setValue(0);
					difficulty.invalidate();
				}
			}
             });

        String positiveButtonText = (edit == null)?"Add":"Edit";
        // Add action buttons
        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int id) {
         	   final int color = 0;
         	   String diff = getResources().getStringArray(R.array.boulder_levels)[difficulty.getValue()];
         	   if(rope.isChecked())
         		  diff = getResources().getStringArray(R.array.rope_levels)[difficulty.getValue()];
         	   final String val = name.getText().toString();
         	   final Route r = dbh.routes.build(diff, 0, Long.parseLong(mParam1), color, val, 0);
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
								routes.set(selectedItem, edit);
							}
							else{
								routes.add(r);
								click(listview, selectedItem);
							}
							lv.invalidateViews();
							
						}
						public void onProgressUpdate(Unit... data) {}
         	   };
         	   t.run(true,true);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        Dialog d = builder.create();
        d.setCanceledOnTouchOutside(true);
        d.setCancelable(true);
		return d;
    }
    
    public void addRoute(View v){
    	final ListView lv = (ListView) this.getActivity().findViewById(R.id.listview);
    	Dialog d = createDialog(null, lv);
		d.show();
	}
	
	public void editRoute(View v){
		final ListView lv = (ListView) this.getActivity().findViewById(R.id.listview);
		final Route edit = (Route) lv.getAdapter().getItem(selectedItem);
		Dialog d = createDialog(edit, lv);
		d.show();
	}
	
	public void deleteRoute(View v){
		final Route delete = (Route) listview.getAdapter().getItem(selectedItem);
		routes.remove(delete);
		Transaction t = new Transaction(db){
			public void task(SQLiteDatabase db) {
				 dbh.routes.delete(RouteContract._ID + "=" + delete.get(RouteContract._ID), null, db);
			}
			public void onComplete() {
				routes.remove(delete);
				click(listview, selectedItem);
				listview.invalidateViews();
			}
			public void onProgressUpdate(Unit... data) {}
		};
		t.run(true, true);
	}


    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
    	super.onActivityCreated(savedInstanceState);
        Activity thisActivity = this.getActivity();
    	listview = (ListView) thisActivity.findViewById(R.id.listview);
        listview.setAdapter( new RouteListAdapter( this.getActivity(), routes ));
        listview.setOnItemClickListener( this );
    }

    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        click(view, position);
    }
    
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

    @Override
    public void onClick( View view ) {
        switch( view.getId() ) {
            case R.id.addRouteButton:
                // open dialog with blank fields
                break;
            case R.id.editRouteButton:
                // open dialog with filled-in fields
                break;
            case R.id.deleteRouteButton:
                // delete the route
                break;
        }
    }
    
    private void getRoutes(final long loc_id) {
		Transaction t = new Transaction(db){
			public void task(SQLiteDatabase db) {
				Cursor c = dbh.routes.query(null, RouteContract.LOCATION + "=" + loc_id, null, RouteContract.DIFFICULTY, true, null, db);
                c.moveToFirst();
				while(!c.isAfterLast()){
					routes.add(dbh.routes.build(c));
                    c.moveToNext();
				}
			}
			public void onComplete(){Log.i("RoutesFragment", "Routes Loaded.");}
			public void onProgressUpdate(Unit... data) {}
		};
		t.run(true, true);
	}

    /*private void getLocation(final long loc_id) {
    	Transaction t = new Transaction(db){
			public void task(SQLiteDatabase db) {
    			if(loc_id == -1){
    	    		Cursor c = dbh.locations.query(new String[] { LocationContract.NAME }, null, null, LocationContract._ID, true, null, db);
    	    		for(int i = 0; i < c.getCount(); i++){
    	    			c.moveToNext();
    	    			locations.put(c.getLong(c.getColumnIndex(LocationContract._ID)), dbh.locations.build(c));
    	    		}
    	    	} else {
    	    		Cursor c = dbh.locations.query(new String[] { LocationContract.NAME }, LocationContract._ID + "=" + loc_id, null, LocationContract._ID, true, null, db);
    	    		c.moveToLast();
    	    		locations.put(c.getLong(c.getColumnIndex(LocationContract._ID)), dbh.locations.build(c));
    	    	}
			}
			public void onComplete() {Log.i("RoutesFragment", "Locations Loaded.");}
			public void onProgressUpdate(Unit... data) {}
    	};
    	
    }*/
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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

    /* Custom adapter for the list of routes */
    private class RouteListAdapter extends BaseAdapter {

        Context context;
        List<Route> routes;
        LayoutInflater inflater = null;

        public RouteListAdapter( Context context, List<Route> routes ) {
            this.context = context;
            this.routes = routes;
            this.inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        }

        @Override
        public int getCount() {
            return routes.size();
        }

        @Override
        public Object getItem( int position ) {
            return routes.get( position );
            // return routes[position];
        }

        @Override
        public long getItemId( int position ) {
            return position;
        }

        @Override
        public View getView( final int position, View convertView, ViewGroup parent ) {
            View vi = convertView;
            if( vi == null )
                vi = inflater.inflate( R.layout.row, null );

            /* Handle displaying difficulty, color label, route name, location, etc. */
            TextView diffLevel = (TextView)vi.findViewById(R.id.DifficultyLevel);
            diffLevel.setText(routes.get(position).get(RouteContract.DIFFICULTY));

            View colorlabel = vi.findViewById( R.id.ColorLabel );
            colorlabel.setBackgroundColor( Integer.parseInt(routes.get(position).get(RouteContract.COLOR)) );

            TextView routeName = (TextView)vi.findViewById( R.id.RouteName );
            routeName.setText(routes.get(position).get(RouteContract.NAME));

            TextView routeLoc = (TextView)vi.findViewById( R.id.Location );
            routeLoc.setText(routes.get(position).get(RouteContract.LOCATION));

            final TextView timesClimbed = (TextView)vi.findViewById( R.id.TimesClimbed );
            timesClimbed.setText(routes.get(position).get(RouteContract.NUM_ATTEMPTS));
            
            final CheckBox completed = (CheckBox)vi.findViewById( R.id.checkboxComplete );
            int comp = Integer.parseInt(routes.get(position).get(RouteContract.COMPLETED));
            completed.setChecked(comp != 0);
            completed.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Transaction t = new Transaction(db){
						public void task(SQLiteDatabase db) {
							routes.get(position).put(RouteContract.COMPLETED, (completed.isChecked())?1:0);
							dbh.routes.update(routes.get(position), 
									RouteContract._ID + "=" + routes.get(position).get(RouteContract._ID), null, db);
						}
						public void onComplete() {
							listview.invalidateViews();
						}
						public void onProgressUpdate(Unit... data) {}
					};
					t.run(true, true);
				}
            });
            
            Button inc = (Button)vi.findViewById(R.id.TimesClimbedIncrementor);
            inc.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					Transaction t = new Transaction(db){
						public void task(SQLiteDatabase db) {
							routes.get(position).put(RouteContract.NUM_ATTEMPTS, 
									Long.parseLong(routes.get(position).get(RouteContract.NUM_ATTEMPTS)) + 1);
							dbh.routes.update(routes.get(position), 
									RouteContract._ID + "=" + routes.get(position).get(RouteContract._ID), null, db);
							//Log.i("DEBUG", "Route update attempted. " + routes.get(position).toString());
						}
						public void onComplete() {
							listview.invalidateViews();
						}
						public void onProgressUpdate(Unit... data) {}
					};
					t.run(false, false);
				}
            });
            
            return vi;
        }
    }
}
