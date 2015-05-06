/** FILENAME: RouteDialogFragment.java
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
 *    DialogFragment for the routes dialog, allowing the user to add/edit a route
 */

package transcend.rockeeper.activities;

import java.util.ArrayList;
import java.util.List;

import transcend.rockeeper.data.LocationContract;
import transcend.rockeeper.data.RouteContract;
import transcend.rockeeper.data.RouteContract.Route;
import activities.rockeeper.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

@SuppressLint("InflateParams")
public class RouteDialogFragment extends DialogFragment {

    // Interface which must be implemented by the RoutesFragment
    public interface RouteDialogListener {
        public void onRouteDialogPositiveClick( DialogFragment dialog, Route edit );
    }

    private ArrayList<Integer> colorsArray = new ArrayList<Integer>();

    private Route edit;
    private int listIndex;

    private RouteDialogListener mListener;

    /** Called when the dialog is created - handle initializations */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the selected Route object (if one was selected)
        Bundle args = getArguments();
        listIndex = args.getInt("selectedItem");
        if( listIndex == -1 )
            edit = null;
        else {
            ListView lv = (ListView) this.getActivity().findViewById(R.id.listview);
            edit = (Route) lv.getAdapter().getItem(listIndex);
        }

        colorsArray.add( 0xFFFF0000 );
        colorsArray.add( 0xFFFF8800 );
        colorsArray.add( 0xFFFFFF00 );
        colorsArray.add( 0xFF00FF00 );
        colorsArray.add( 0xFF0000FF );
        colorsArray.add( 0xFFFF00FF );
        colorsArray.add( 0xFFFFFFFF );
        colorsArray.add( 0xFF000000 );

        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity(), AlertDialog.THEME_HOLO_LIGHT );
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setTitle( ((edit==null)?"Create":"Edit") + " route" );

        View dialogView = inflater.inflate(R.layout.fragment_create_route, null);
        builder.setView( dialogView );

        final NumberPicker difficulty = (NumberPicker) dialogView.findViewById(R.id.routeDifficultyPicker);
        difficulty.setDisplayedValues( getResources().getStringArray(R.array.boulder_levels) );
        difficulty.setMinValue(0);
        difficulty.setMaxValue(14);

        final EditText name = (EditText) dialogView.findViewById(R.id.routeDialogName);

        final Spinner color = (Spinner) dialogView.findViewById(R.id.routeColorPicker);
        color.setAdapter( new ColorSpinnerAdapter( getActivity(), R.id.colorSpinner, colorsArray ) );

        final EditText points = (EditText) dialogView.findViewById(R.id.routePoints);

        final RadioButton rope = (RadioButton) dialogView.findViewById(R.id.topropeRB);
        final RadioButton boulder = (RadioButton) dialogView.findViewById(R.id.boulderRB);

        // If opened in edit mode, populate the fields with existing values
        if( edit != null ) {
            name.setText( edit.get( RouteContract.NAME ));
            String routeDiff = edit.get( RouteContract.DIFFICULTY );
            if( routeDiff.charAt(0) == '5' ) {
                difficulty.setMaxValue(11);
                difficulty.setDisplayedValues(getResources().getStringArray(R.array.rope_levels));
                difficulty.setValue( Integer.parseInt( routeDiff.substring( 2, routeDiff.length() )) - 5 );
                difficulty.invalidate();
                rope.toggle();
            }
            else
                difficulty.setValue( Integer.parseInt( routeDiff.substring( 1, routeDiff.length() ) ) );

            color.setSelection( colorsArray.indexOf( Integer.parseInt(edit.get(RouteContract.COLOR))));
            points.setText( edit.get( RouteContract.POINTS ));
        }

        // Define what happens when the radio buttons are clicked - in this case, change the
        //   difficulty spinner to the appropriate set of difficulties
        rope.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
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
        boulder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
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
                mListener.onRouteDialogPositiveClick( RouteDialogFragment.this, edit );
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

    /** Called when the dialog is attached to its activity */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (RouteDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RouteDialogListener");
        }
    }

    /** Called when the dialog is detached from its activity */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /** Custom adapter for the ColorSpinner */
    private class ColorSpinnerAdapter extends ArrayAdapter<Integer> {

        List<Integer> colors;
        LayoutInflater inflater;

        public ColorSpinnerAdapter( Context context, int resourceid, ArrayList<Integer> colors ) {
            super( context, resourceid, colors );
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
