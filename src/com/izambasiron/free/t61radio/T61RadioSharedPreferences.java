package com.izambasiron.free.t61radio;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class T61RadioSharedPreferences {
	public static final String PREFS_NAME = "T61RadioPrefsFile";
	public static final String PREFS_ALERT_PLUGIN = "alertPlugin";
	public static final String PREFS_ALERT_HEART = "alertHeart";
	public static final String PREFS_MONITOR_BATTERY = "monitorBattery";
	public static final String PREFS_MONITOR_MOBILE = "monitorMobile";
	
	public static boolean getShowAlertFlag(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.pref_key_show_alert),
                true);
    }
 
    public static void setShowAlertFlag(Context context, boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.pref_key_show_alert),
                newValue);
        prefsEditor.commit();
    }
    
    public static boolean getAlertExitFlag(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.pref_key_alert_exit),
                true);
    }
 
    public static void setAlertExitFlag(Context context, boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.pref_key_alert_exit),
                newValue);
        prefsEditor.commit();
    }
}
