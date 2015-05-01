/** FILENAME: GoalDialogFragment.java
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
 *    DialogFragment for the goals dialog, allowing the user to add/edit a goal
 */

package transcend.rockeeper.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.Calendar;
import java.util.HashMap;
import activities.rockeeper.R;
import transcend.rockeeper.data.GoalContract;
import transcend.rockeeper.data.GoalContract.Goal;
import transcend.rockeeper.sqlite.DatabaseHelper;


@SuppressLint("InflateParams")
public class GoalDialogFragment extends DialogFragment {

    // Interface which must be implemented by the GoalsFragment
    public interface GoalDialogListener {
        public void onGoalDialogPositiveClick(DialogFragment dialog, Goal edit);
    }

    private Goal edit;
    private int listIndex;

    private GoalDialogListener mListener;

    @SuppressLint("UseSparseArrays")
	private HashMap<Integer, String> goalNouns = new HashMap<Integer, String>();

	private DatabaseHelper dbh;
	/** Called when the dialog is created - handle initializations */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Retrieve the selected goal, if one was selected
        Bundle args = getArguments();
        listIndex = args.getInt("selectedItem");
        if( listIndex == -1 )
            edit = null;
        else {
            ListView lv = (ListView) this.getActivity().findViewById(R.id.listviewGoals);
            edit = (Goal) lv.getAdapter().getItem(listIndex);
        }

        dbh = new DatabaseHelper(this.getActivity(), null);
        dbh.getWritableDatabase();

        goalNouns.put( 0, "routes" );
        goalNouns.put( 1, "routes" );
        goalNouns.put( 2, "points" );
        goalNouns.put( 3, "route" );

        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.fragment_create_goal, null);
        builder.setView( dialogView );

        final EditText value = (EditText) dialogView.findViewById( R.id.goalValue );
        final TextView noun = (TextView) dialogView.findViewById( R.id.nounView );

        final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById( R.id.goalRadioGroup );
        final RadioButton toprope = (RadioButton) dialogView.findViewById( R.id.goalTopRope );
        final RadioButton boulder = (RadioButton) dialogView.findViewById( R.id.goalBoulder );

        final Spinner diff = (Spinner) dialogView.findViewById( R.id.goalDifficulty );
        final ArrayAdapter<CharSequence> diffAdapterRope = ArrayAdapter.createFromResource( getActivity(), R.array.rope_levels, android.R.layout.simple_spinner_item );
        final ArrayAdapter<CharSequence> diffAdapterBoulder = ArrayAdapter.createFromResource( getActivity(), R.array.boulder_levels, android.R.layout.simple_spinner_item );
        diff.setAdapter( diffAdapterRope );

        final Spinner verb = (Spinner) dialogView.findViewById( R.id.verbSpinner );
        ArrayAdapter<CharSequence> verbAdapter = ArrayAdapter.createFromResource( getActivity(), R.array.spinner_verbs, android.R.layout.simple_spinner_item );
        verb.setAdapter( verbAdapter );
        verb.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if( position == 3 ) {
                    value.setVisibility( View.GONE );
                    diff.setVisibility( View.VISIBLE );
                    radioGroup.setVisibility( View.VISIBLE );
                    noun.setText( "route" );
                }
                else {
                    value.setVisibility( View.VISIBLE );
                    diff.setVisibility( View.GONE );
                    radioGroup.setVisibility( View.GONE );

                    if( position == 0 || position == 1 ) noun.setText( "routes" );
                    else noun.setText( "points" );
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        final DatePicker date = (DatePicker) dialogView.findViewById( R.id.goalDatePicker );

        // If a goal was selected, populate the fields with the appropriate data
        if( edit != null ) {
            String type = edit.get( GoalContract.TYPE );
            if( type == "difficulty" ) {
                verb.setSelection( 3 );
                noun.setText( "route" );
                value.setVisibility( View.GONE );
                diff.setVisibility(View.VISIBLE);
            }
            else if( type == "points" ) {
                verb.setSelection( 2 );
                noun.setText(goalNouns.get(2));
            }
            else if( type == "num_attempts" ) {
                verb.setSelection( 0 );
                noun.setText(goalNouns.get(0));
            }
            else if( type == "complete" ) {
                verb.setSelection( 1 );
                noun.setText(goalNouns.get(1));
            }
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis( Long.parseLong( edit.get( GoalContract.DUE_DATE ) ) );
            date.updateDate( cal.get( Calendar.YEAR ), cal.get( Calendar.MONTH ), cal.get( Calendar.DAY_OF_MONTH ) );

            //TODO: Fetch difficulty data and toggle radio buttons accordingly
        }

        toprope.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
                if( isChecked ) {
                    diff.setAdapter( diffAdapterRope );
                    diff.invalidate();
                }
            }
        });
        boulder.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
                if( isChecked ) {
                    diff.setAdapter( diffAdapterBoulder );
                    diff.invalidate();
                }
            }
        });

        String positiveButtonText = (edit == null)?"Add":"Edit";
        // Add action buttons
        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int id) {
                mListener.onGoalDialogPositiveClick( GoalDialogFragment.this, edit );
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // Build the dialog and return it
        Dialog d = builder.create();
        d.setCanceledOnTouchOutside(true);
        d.setCancelable(true);
        return d;
    }

    /** Called when the fragment is attached to its activity */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (GoalDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement GoalDialogListener");
        }
    }

    /** Called when the fragment is detached from its activity */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
