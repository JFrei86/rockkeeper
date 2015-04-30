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
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.util.ArrayList;
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

    ArrayList<Integer> colorsArray = new ArrayList<Integer>();

    Goal edit;
    int listIndex;

    GoalDialogListener mListener;

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

        colorsArray.add( 0xFFFF0000 );
        colorsArray.add( 0xFFFF8800 );
        colorsArray.add( 0xFFFFFF00 );
        colorsArray.add( 0xFF00FF00 );
        colorsArray.add( 0xFF0000FF );
        colorsArray.add( 0xFFFF00FF );
        colorsArray.add( 0xFFFFFFFF );
        colorsArray.add( 0xFF000000 );

        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        LayoutInflater inflater = getActivity().getLayoutInflater();
//
//        View dialogView = inflater.inflate(R.layout.fragment_create_goal, null);
//        builder.setView( dialogView );
//
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

        Dialog d = builder.create();
        d.setCanceledOnTouchOutside(true);
        d.setCancelable(true);
        return d;
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

    private class ColorSpinnerAdapter extends ArrayAdapter<Integer> {

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
    }
}
