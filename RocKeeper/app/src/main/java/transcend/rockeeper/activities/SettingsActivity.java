/** FILENAME: SettingsActivity.java
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
 *    A settings activity that maintains the SettingsFragment
 */

package transcend.rockeeper.activities;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity
{
    /** Called when the activity is created - handle initializations */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // Replace the activity's view with the SettingsFragment
        getFragmentManager().beginTransaction().replace( android.R.id.content, new SettingsFragment() ).commit();
    }
}
