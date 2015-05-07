/** FILENAME: GoalsFragment.java
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
 *    Fragment for the goals page, showing the list of goals
 */

package transcend.rockeeper.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import transcend.rockeeper.data.Contract.Unit;
import transcend.rockeeper.data.GoalContract;
import transcend.rockeeper.data.GoalContract.Goal;
import transcend.rockeeper.data.RouteContract;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
import activities.rockeeper.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GoalsFragment extends Fragment implements GoalDialogFragment.GoalDialogListener, AdapterView.OnItemClickListener{

	private ArrayList<Goal> goals = new ArrayList<Goal>();
	private DatabaseHelper dbh;
	private SQLiteDatabase db;
	private FragmentActivity mainActivity;
	private ListView listview;
    private int selectedItem = -1;      // the index of the list item selected

/****************************** LIFECYCLE METHODS *****************************/

    /** Returns a new instance of the fragment */
	public static GoalsFragment newInstance() {
		GoalsFragment fragment = new GoalsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
	}
	
	 public GoalsFragment() {
        // Required empty public constructor
    }

    /** Called when the fragment is created - handle initializations */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbh = new DatabaseHelper(this.getActivity(), null);
        db = dbh.getWritableDatabase();
        getGoals(db);
    }

    /** Sets up the view for the fragment */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }

    /** Called when the activity associated with the fragment has been created */
	@Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = this.getActivity();
        listview = (ListView) mainActivity.findViewById(R.id.listviewGoals);
        listview.setAdapter( new GoalListAdapter( this.getActivity(), goals ));
        listview.setOnItemClickListener( this );
    }

/********************************** DIALOG HANDLERS ***************************/

    /** Called when the user selects the positive button (i.e. 'OK') of the dialog */
    public void onGoalDialogPositiveClick(DialogFragment dialog, final Goal edit) {

        final Spinner verb = (Spinner) dialog.getDialog().findViewById( R.id.verbSpinner );
        final EditText value = (EditText) dialog.getDialog().findViewById( R.id.goalValue );
        final Spinner diff = (Spinner) dialog.getDialog().findViewById( R.id.goalDifficulty );
        final DatePicker date = (DatePicker) dialog.getDialog().findViewById( R.id.goalDatePicker );

        // Get the values from the fields
        final String verb_val = (String) verb.getSelectedItem();
        if( value.getText().toString().equals("") && !verb_val.equals("Climb a") ) {
            Toast.makeText( getActivity(), "Field(s) left blank. Try again", Toast.LENGTH_LONG ).show();
            return;
        }
        final int value_val = ( value.getText().toString().equals("") )?0:Integer.parseInt( value.getText().toString() );
        final String diff_val = (String) diff.getSelectedItem();
        final Calendar cal = Calendar.getInstance();
        cal.set( date.getYear(), date.getMonth(), date.getDayOfMonth() );
        final long date_val = cal.getTimeInMillis();

        // Determine the type of goal
        String goalType = "";
        if( verb_val.equals( "Earn" ) ) goalType = GoalContract.POINTS;
        else if( verb_val.equals( "Attempt" ) ) goalType = GoalContract.ATTEMPTS;
        else if( verb_val.equals( "Complete" ) ) goalType = GoalContract.COMPLETED;
        else if( verb_val.equals( "Climb a" ) ) goalType = GoalContract.DIFFICULTY;

        final GoalListAdapter adapter = (GoalListAdapter)listview.getAdapter();

        // Build the goal object and add/update it in the database
        final Goal g = (goalType.equals( GoalContract.DIFFICULTY ) ) ? dbh.goals.build( diff_val, date_val ) : dbh.goals.build( goalType, value_val, date_val );
        Transaction t = new Transaction(db) {
            public void task(SQLiteDatabase db) {
                if(edit == null){
                    dbh.goals.insert(g, db);
                }
                else{
                    dbh.goals.update(g, GoalContract._ID + "=" + edit.get(GoalContract._ID), null, db);
                }
            }
            public void onComplete() {
                if(edit != null){
                    if(selectedItem == -1)
                        return;
                    //goals.set(selectedItem, g);
                    adapter.remove( edit );
                    adapter.insert( g, selectedItem );
                }
                else{
                    //goals.add(g);
                    adapter.add( g );
                    click(listview, selectedItem);
                }

                //adapter.notifyDataSetChanged();
            }
            public void onProgressUpdate(Unit... data) {}
        };
        t.run(true, true);
    }

/**************************** BUTTON HANDLERS ***************************/

    /** Displays the goals dialog in add mode */
    public void addGoal( View v ){
        Bundle args = new Bundle();
        args.putInt( "selectedItem", -1 );
        GoalDialogFragment d = new GoalDialogFragment();
        d.setArguments( args );
        d.setTargetFragment( this, 1 );
        d.show( getFragmentManager(), "GoalDialog" );
    }

    /** Displays the goals dialog in edit mode */
    public void editGoal( View v ){
        //final Goal edit = (Goal) listview.getAdapter().getItem(selectedItem);
        Bundle args = new Bundle();
        args.putInt( "selectedItem", selectedItem );
        GoalDialogFragment d = new GoalDialogFragment();
        d.setArguments( args );
        d.setTargetFragment( this, 1 );
        d.show( getFragmentManager(), "GoalDialog" );
    }

    /** Deletes the selected goal from the list and the database */
    public void deleteGoal( View v ){
        GoalListAdapter adapter = (GoalListAdapter)listview.getAdapter();
        final GoalContract.Goal delete = adapter.getItem(selectedItem);
        click( adapter.getView( selectedItem, null, null ), selectedItem);
        adapter.remove( delete );
        adapter.notifyDataSetChanged();
        listview.invalidateViews();

        Transaction t = new Transaction(db){
            public void task(SQLiteDatabase db) {
                dbh.goals.delete(GoalContract._ID + "=" + delete.get(GoalContract._ID), null, db);
            }
            public void onComplete() {
                //goals.remove(delete);
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
        Button editB = (Button) getActivity().findViewById(R.id.editGoalButton);
        Button deleteB = (Button) getActivity().findViewById(R.id.deleteGoalButton);
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

/***************************** GOAL METHODS ******************************/

    /** Queries the database for goals */
    private void getGoals(SQLiteDatabase db2) {
        //final GoalListAdapter adapter = (GoalListAdapter)listview.getAdapter();
        Transaction t = new Transaction(db){
            public void task(SQLiteDatabase db) {
                goals.clear();
                Cursor c = dbh.goals.query(null, null, null, GoalContract.DUE_DATE, false, null, db);
                c.moveToFirst();
                while(c.getCount() > 0 && !c.isAfterLast()) {
                    goals.add(dbh.goals.build(c));
                    c.moveToNext();
                }
            }
            public void onComplete(){
                Log.i("GoalsFragment", "Goals Loaded.");
                ((GoalListAdapter)listview.getAdapter()).notifyDataSetChanged();
                listview.invalidateViews();
            }
            public void onProgressUpdate(Unit... data) {}
        };
        t.run(true, true);
    }

    public void updateGoals( final RouteContract.Route r, final String column, SQLiteDatabase db ) {
        final GoalListAdapter adapter = (GoalListAdapter)listview.getAdapter();
        final ArrayList<Goal> changedGoals = new ArrayList<Goal>();
        for( int i = 0; i < adapter.getCount(); ++i ) {
            Goal g = adapter.getItem( i );
            String type = g.get( GoalContract.TYPE );
            long status = Long.parseLong( g.get( GoalContract.STATUS ));

            // If the goal has already been satisfied, ignore it
            if( !type.equals( GoalContract.DIFFICULTY ) && Long.parseLong(g.get(type)) <= status) continue;
            else if( type.equals(GoalContract.DIFFICULTY) && status > 0) continue;

            // Increment the status
            if( column.equals( GoalContract.ATTEMPTS ) ) {
                if( type.equals( GoalContract.ATTEMPTS ) ) {
                    g.put( GoalContract.STATUS, status+1 );
                    changedGoals.add( g );
                    if( status < Long.parseLong( g.get( GoalContract.ATTEMPTS ) ) && status+1 >= Long.parseLong(g.get(GoalContract.ATTEMPTS)) )
                        ((MainActivity)mainActivity).showToast( "Goal \""+g.toString()+"\" completed!", Toast.LENGTH_LONG );
                }
                else if( type.equals( GoalContract.DIFFICULTY ) && g.get( GoalContract.DIFFICULTY ).equals( r.get( GoalContract.DIFFICULTY ) ) ) {
                    g.put( GoalContract.STATUS, 1 );
                    changedGoals.add( g );
                    if( status == 0 )
                        ((MainActivity)mainActivity).showToast( "Goal \""+g.toString()+"\" completed!", Toast.LENGTH_LONG );
                }
            }
            if( column.equals( GoalContract.COMPLETED ) ) {
                if( type.equals( GoalContract.POINTS ) ) {
                    g.put( GoalContract.STATUS, status + Long.parseLong( r.get( GoalContract.POINTS ) ) );
                    changedGoals.add( g );
                    if( status < Long.parseLong(g.get(GoalContract.POINTS)) && status+Long.parseLong(r.get(GoalContract.POINTS)) >= Long.parseLong(g.get(GoalContract.POINTS)) )
                        ((MainActivity)mainActivity).showToast( "Goal \""+g.toString()+"\" completed!", Toast.LENGTH_LONG );
                }
                else if( type.equals( GoalContract.COMPLETED ) ) {
                    g.put( GoalContract.STATUS, status+1 );
                    changedGoals.add( g );
                    if( status < Long.parseLong(g.get(GoalContract.COMPLETED)) && status+1 >= Long.parseLong(g.get(GoalContract.COMPLETED)) )
                        ((MainActivity)mainActivity).showToast( "Goal \""+g.toString()+"\" completed!", Toast.LENGTH_LONG );
                }
            }
            adapter.remove( adapter.getItem( i ) );
            adapter.insert( g, i );
        }

        Transaction t = new Transaction(db){
            public void task(SQLiteDatabase db) {

                for( int i = 0; i < changedGoals.size(); ++i ) {
                    dbh.goals.update(changedGoals.get(i), GoalContract._ID + "=" + changedGoals.get(i).get(GoalContract._ID), null, db);
                }

                /*ArrayList<Goal> gs = new ArrayList<Goal>();
                //ArrayList<Long> delete = new ArrayList<Long>();
                Cursor c = dbh.goals.query(null, null, null, GoalContract._ID, false, null, db);
                c.moveToFirst();
                while(c.getCount() > 0 && !c.isAfterLast()){
                    gs.add(dbh.goals.build(c));
                    c.moveToNext();
                }

                for(int i = 0; i < gs.size(); i++){
                    Goal g = gs.get(i);

                    String type = g.get( GoalContract.TYPE );
                    long status = Long.parseLong( g.get( GoalContract.STATUS ));

                    // If the goal has already been satisfied, ignore it
                    if( !type.equals( GoalContract.DIFFICULTY ) && Long.parseLong(g.get(type)) <= status) continue;
                    else if( type.equals(GoalContract.DIFFICULTY) && status > 0) continue;

                    // Increment the status
                    if( column.equals( GoalContract.ATTEMPTS ) ) {
                        if( type.equals( GoalContract.ATTEMPTS ) ) {
                            g.put( GoalContract.STATUS, status+1 );
                            //if( status < Long.parseLong( g.get( GoalContract.ATTEMPTS ) ) && status+1 >= Long.parseLong(g.get(GoalContract.ATTEMPTS)) )
                            //    ((MainActivity)mainActivity).showToast( "You completed a goal!", Toast.LENGTH_LONG );
                        }
                        else if( type.equals( GoalContract.DIFFICULTY ) && g.get( GoalContract.DIFFICULTY ).equals( r.get( GoalContract.DIFFICULTY ) ) ) {
                            g.put( GoalContract.STATUS, 1 );
                            //if( status == 0 )
                            //    ((MainActivity)mainActivity).showToast( "You completed a goal!", Toast.LENGTH_LONG );
                        }
                    }
                    if( column.equals( GoalContract.COMPLETED ) ) {
                        if( type.equals( GoalContract.POINTS ) ) {
                            g.put( GoalContract.STATUS, status + Long.parseLong( r.get( GoalContract.POINTS ) ) );
                            //if( status < Long.parseLong(g.get(GoalContract.POINTS)) && status+Long.parseLong(r.get(GoalContract.POINTS)) >= Long.parseLong(g.get(GoalContract.POINTS)) )
                            //    ((MainActivity)mainActivity).showToast( "You completed a goal!", Toast.LENGTH_LONG );
                        }
                        else if( type.equals( GoalContract.COMPLETED ) ) {
                            g.put( GoalContract.STATUS, status+1 );
                            //if( status < Long.parseLong(g.get(GoalContract.COMPLETED)) && status+1 >= Long.parseLong(g.get(GoalContract.COMPLETED)) )
                            //    ((MainActivity)mainActivity).showToast( "You completed a goal!", Toast.LENGTH_LONG );
                        }
                    }
					/*if(g.get(column) != null) g.put(STATUS, Long.parseLong(g.get(STATUS) + 1));
                    //
					if(column.equals( POINTS ) && g.get(POINTS) != null) g.put(STATUS, Long.parseLong(g.get(STATUS)) + Long.parseLong(r.get(POINTS)));
					if(column.equals( ATTEMPTS ) && g.get(DIFFICULTY) != null && g.get(DIFFICULTY).equals( r.get(DIFFICULTY) )) g.put(STATUS, 1);*/
                    /*gs.set(i, g);
                }
                for(int i = 0; i < gs.size(); i++){
                    dbh.goals.update(gs.get(i), GoalContract._ID + "=" + gs.get(i).get(GoalContract._ID), null, db);
                }
                //String where = (_ID + " IN " + delete.toString()).replace('[', '(').replace(']', ')');
                //delete(where, null, db);*/
            }
            public void onComplete() {
                //((GoalListAdapter)listview.getAdapter()).notifyDataSetChanged();
                listview.invalidateViews();
            }
            public void onProgressUpdate(Unit... data) {}
        };
        t.run(true, true);
    }

    /** Refreshes the list of goals */
    public void refresh() {
        //goals.clear();
        getGoals(db);
        //((GoalListAdapter)listview.getAdapter()).notifyDataSetChanged();
    }

/********************************* ADAPTERS *******************************************/
    
    /** Custom adapter for the list of goals */
    @SuppressLint("InflateParams")
	public class GoalListAdapter extends ArrayAdapter<Goal> {

        Context context;
        LayoutInflater inflater = null;

        public GoalListAdapter( Context context, ArrayList<Goal> goals ) {
            super( context, 0, goals );
            this.context = context;
            this.inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        }

        public int getCount() {
            return goals.size();
        }

        public Goal getItem( int position ) {
            return goals.get( position );
        }

        public long getItemId( int position ) {
            return position;
        }

        public View getView( final int position, View convertView, ViewGroup parent ) {
            if(convertView == null){
        		convertView = inflater.inflate(R.layout.goal_row, null);
        		Goal g = getItem(position);
                TextView goalText = (TextView) convertView.findViewById(R.id.goalName);
                TextView started = (TextView) convertView.findViewById(R.id.dueDate);
                CheckBox completed = (CheckBox) convertView.findViewById(R.id.checkboxComplete);
                if(g.get(GoalContract.TYPE).equals( GoalContract.DIFFICULTY )){
                	if( Long.parseLong(g.get(GoalContract.STATUS)) > 0 ) {
                        completed.setChecked( true );
                        //Toast.makeText( getActivity(), "You completed a goal!", Toast.LENGTH_LONG ).show();
                    }
                } else {
                	if( Long.parseLong(g.get(GoalContract.STATUS)) >= Long.parseLong(g.get(g.get(GoalContract.TYPE))) ) {
                        completed.setChecked( true );
                        //Toast.makeText(getActivity(), "You completed a goal!", Toast.LENGTH_LONG).show();
                    }
                }
                
                String goal = dbh.goals.verbs.get(g.get(GoalContract.TYPE)) + 
                		g.get(g.get(GoalContract.TYPE)) + 
                		dbh.goals.nouns.get(g.get(GoalContract.TYPE));
                
                goalText.setText(goal.toUpperCase(Locale.US));
                started.setText("Due on " + DateFormat.getDateFormat(context).format(Long.parseLong(g.get(GoalContract.DUE_DATE))));
            }
            return convertView;
        }
    }



}
