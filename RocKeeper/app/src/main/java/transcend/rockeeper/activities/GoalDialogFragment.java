package transcend.rockeeper.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import activities.rockeeper.R;
import transcend.rockeeper.data.GoalContract;
import transcend.rockeeper.data.GoalContract.Goal;
import transcend.rockeeper.sqlite.DatabaseHelper;

public class GoalDialogFragment extends DialogFragment {

    public interface GoalDialogListener {
        public void onGoalDialogPositiveClick(DialogFragment dialog, Goal edit);
    }

    //DatabaseHelper dbh;
    //SQLiteDatabase db;

    Goal edit;
    int listIndex;

    GoalDialogListener mListener;

    HashMap<Integer, String> goalNouns = new HashMap<Integer, String>();

	private DatabaseHelper dbh;
	private SQLiteDatabase db;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        listIndex = args.getInt("selectedItem");
        if( listIndex == -1 )
            edit = null;
        else {
            ListView lv = (ListView) this.getActivity().findViewById(R.id.listview);
            edit = (Goal) lv.getAdapter().getItem(listIndex);
        }

        dbh = new DatabaseHelper(this.getActivity(), null);
        db = dbh.getWritableDatabase();

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
        verb.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
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
        });

        final DatePicker date = (DatePicker) dialogView.findViewById( R.id.goalDatePicker );

        if( edit != null ) {
            String type = edit.get( GoalContract.TYPE );
            if( type == null ) {
                verb.setSelection( 3 );
                noun.setText( "route" );
                value.setVisibility( View.GONE );
                diff.setVisibility(View.VISIBLE);
            }
            else {
                verb.setSelection(Integer.parseInt(edit.get(GoalContract.TYPE)));
                noun.setText(goalNouns.get(Integer.parseInt(edit.get(GoalContract.TYPE))));
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

        Dialog d = builder.create();
        d.setCanceledOnTouchOutside(true);
        d.setCancelable(true);
        return d;

//        final NumberPicker difficulty = (NumberPicker) dialogView.findViewById(R.id.GoalDifficultyPicker);
//        difficulty.setDisplayedValues( getResources().getStringArray(R.array.boulder_levels) );
//        difficulty.setMinValue(0);
//        difficulty.setMaxValue(14);
//
//        final EditText name = (EditText) dialogView.findViewById(R.id.GoalDialogName);
//
//        final Spinner color = (Spinner) dialogView.findViewById(R.id.GoalColorPicker);
//        color.setAdapter( new ColorSpinnerAdapter( getActivity(), R.id.colorSpinner, colorsArray ) );
//
//        final EditText points = (EditText) dialogView.findViewById(R.id.GoalPoints);
//
//        final RadioButton rope = (RadioButton) dialogView.findViewById(R.id.topropeRB);
//        final RadioButton boulder = (RadioButton) dialogView.findViewById(R.id.boulderRB);
//
//        // If opened in edit mode, populate the fields with existing values
//        if( edit != null ) {
//            name.setText( edit.get( GoalContract.NAME ));
//            String GoalDiff = edit.get( GoalContract.DIFFICULTY );
//            if( GoalDiff.charAt(0) == '5' ) {
//                difficulty.setMaxValue(11);
//                difficulty.setDisplayedValues(getResources().getStringArray(R.array.rope_levels));
//                difficulty.setValue( Integer.parseInt( GoalDiff.substring( 2, GoalDiff.length() )) - 5 );
//                difficulty.invalidate();
//                rope.toggle();
//            }
//            else
//                difficulty.setValue( Integer.parseInt( GoalDiff.substring( 1, GoalDiff.length() ) ) );
//
//            color.setSelection( colorsArray.indexOf( Integer.parseInt(edit.get(GoalContract.COLOR))));
//            points.setText( edit.get( GoalContract.POINTS ));
//        }
//
//        rope.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
//            public void onCheckedChanged(CompoundButton buttonView,
//                                         boolean isChecked) {
//                if(isChecked){
//                    difficulty.setMinValue(0);
//                    difficulty.setMaxValue(11);
//                    difficulty.setDisplayedValues( getResources().getStringArray(R.array.rope_levels) );
//                    //difficulty.setValue(0);
//                    difficulty.invalidate();
//                }
//            }
//        });
//        boulder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
//            public void onCheckedChanged(CompoundButton buttonView,
//                                         boolean isChecked) {
//                if(isChecked){
//                    difficulty.setDisplayedValues( getResources().getStringArray(R.array.boulder_levels) );
//                    difficulty.setMinValue(0);
//                    difficulty.setMaxValue(14);
//                    //difficulty.setValue(0);
//                    difficulty.invalidate();
//                }
//            }
//        });
//
//        String positiveButtonText = (edit == null)?"Add":"Edit";
//        // Add action buttons
//        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface d, int id) {
//                mListener.onGoalDialogPositiveClick( GoalDialogFragment.this, edit );
//            }
//        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.cancel();
//            }
//        });
    }

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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*private class ColorSpinnerAdapter extends ArrayAdapter<Integer> {

        Context context;
        List<Integer> colors;
        LayoutInflater inflater;

        public ColorSpinnerAdapter( Context context, int resourceid, ArrayList<Integer> colors ) {
            super( context, resourceid, colors );
            this.context = context;
            this.colors = colors;
            this.inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        }

        @Override
        public int getCount() {
            return colors.size();
        }

        @Override
        public Integer getItem( int position ) {
            return colors.get( position );
        }

        @Override
        public long getItemId( int position ) {
            return position;
        }

        @Override
        public View getDropDownView( int position, View convertView, ViewGroup parent ) {
            return getCustomView( position, convertView, parent );
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            return getCustomView( position, convertView, parent );
        }

        public View getCustomView( final int position, View convertView, ViewGroup parent ) {
            View vi = convertView;
            if( vi == null )
                vi = inflater.inflate( R.layout.color_spinner, null );

            View colorBox = vi.findViewById(R.id.colorSpinner);
            colorBox.setBackgroundColor(getItem(position));

            return vi;
        }
    }*/
}
