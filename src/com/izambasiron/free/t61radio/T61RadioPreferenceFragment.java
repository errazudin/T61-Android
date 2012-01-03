package com.izambasiron.free.t61radio;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;

public class T61RadioPreferenceFragment extends PreferenceActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
    
    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers_fragment, target);
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    public static class BatteryDataUsagePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesName(
                    T61RadioSharedPreferences.PREFS_NAME);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_battery_data_usage);
        }
    }

    /**
     * This fragment shows the preferences for the second header.
     */
    public static class AlertDialogPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesName(
                    T61RadioSharedPreferences.PREFS_NAME);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_alert_dialog);
        }
    }
}
