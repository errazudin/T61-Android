package com.izambasiron.free.t61radio;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class T61RadioPreferenceActivity extends PreferenceActivity {
	private static final String TAG = "T61RadioPreferenceActivity";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getPreferenceManager().setSharedPreferencesName(
                T61RadioSharedPreferences.PREFS_NAME);
        addPreferencesFromResource(R.xml.preference_headers_activity);
    }
}
