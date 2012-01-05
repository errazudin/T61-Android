package com.izambasiron.free.t61radio;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.android.actionbarcompat.ActionBarActivity;

public class MainActivity extends ActionBarActivity {
	private static final String TAG = "MainActivity";
	private static int SWIPE_MIN_DISTANCE;
	private static int SWIPE_THRESHOLD_VELOCITY;
	private static final int WEBVIEW_ID = 1;
	private static final int NOTIFICATION_ID = 2;
	
	private WebView mWebView;
	// TODO: handle device sleep based on pref
	private WakeLock mWake;
	private GestureDetector mGestureDetector;
	private OnTouchListener mGestureListener;
	private PhoneStateListener mPhoneStateListener;
	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private BroadcastsHandler mBroadcastsHandler; // listen to headset plug in/out
	private Boolean isHeadPhonesIn = false;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final ViewConfiguration vc = ViewConfiguration.get(getApplicationContext());

		SWIPE_MIN_DISTANCE = vc.getScaledTouchSlop();
		SWIPE_THRESHOLD_VELOCITY = vc.getScaledMinimumFlingVelocity();

		// TODO: handle device sleep based on pref
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWake = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");

		// Warn before using mobile data
		int online = isOnline();
		
		final Context ctx = getApplicationContext();
		if (T61RadioSharedPreferences.getShowAlertFlag(ctx) && 
				T61RadioSharedPreferences.getAlertDataFlag(ctx)
				&& online != -1 && online != ConnectivityManager.TYPE_WIFI) {
			displayDataAlertDialog();
		} else {
			Intent intent = getIntent();
			// check if this intent is started via url
			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				createWebView(intent.getData().toString());
			} else {
				createWebView(null);
			}
		}

		// Prevent phone from being locked. Lockscreen kills the activity.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		// Show notification
		// TODO: Show only on pause?
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = R.drawable.ic_stat_notify;
		CharSequence tickerText = getResources().getString(R.string.app_name);
		long when = System.currentTimeMillis();

		mNotification = new Notification(icon, tickerText, when);
		mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		mNotification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

		// listen to headset plug in/out
		mBroadcastsHandler = new BroadcastsHandler();
		registerReceiver(mBroadcastsHandler, new android.content.IntentFilter(Intent.ACTION_HEADSET_PLUG));
		
		// TODO: Not working
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
			Log.d(TAG, "LOW PROFILE");
		}
	}
	
	private void displayDataAlertDialog() {
		final Context ctx = getApplicationContext();
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.alert_dialog,
		                               (ViewGroup) findViewById(R.id.layout_root));

		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(getResources().getString(R.string.confirm_using_data));
		
		final CheckBox checkBox = (CheckBox) layout.findViewById(R.id.check_box);

		builder = new AlertDialog.Builder(this);
		builder.setView(layout)
		       .setCancelable(false)
		       .setPositiveButton(getResources().getString(R.string.ok), 
		    		   new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!checkBox.isChecked()) {
							T61RadioSharedPreferences.setAlertDataFlag(ctx, false);
						}
						dialog.cancel();
						Intent intent = getIntent();
						// check if this intent is started via url
						if (Intent.ACTION_VIEW.equals(intent.getAction())) {
							createWebView(intent.getData().toString());
						} else {
							createWebView(null);
						}
					}
				})
			   .setNegativeButton(getResources().getString(R.string.quit), 
		    		   new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!checkBox.isChecked()) {
							T61RadioSharedPreferences.setAlertDataFlag(ctx, false);
						}
						MainActivity.this.finish();
					}
				});
		alertDialog = builder.create();
		alertDialog.show();
	}

	public int isOnline() {
		int netType = -1;
		try{
			ConnectivityManager connectivityManager =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
			if(connectivityManager != null ){
				NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

				if(networkInfo != null){
					netType = networkInfo.getType();
					Log.d("Log", "connetion is available");
				}else {
					Log.d("Log", "connetion is  not available");
				}
			}
			return netType;
		}catch(Exception e){
			Log.d("Log", "checkNetworkConnection" + e.toString());
			return netType;
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, intent.toString());
		super.onNewIntent(intent);
		
		// check if this intent is started via a link to http://www.thesixtyone.com
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			if (mWebView != null) {
				mWebView.destroy();
				mWebView = null;
			}
			createWebView(intent.getData().toString());
		} 
	}

	protected void showNotification(String title) {
		Context context = getApplicationContext();
		CharSequence contentTitle = getResources().getString(R.string.app_name);
		CharSequence contentText = title;
		Intent notificationIntent = new Intent(context, MainActivity.class);
		notificationIntent.setAction(Intent.ACTION_MAIN);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		//notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		mNotification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mNotification);
	}

	private void createWebView(String webAddress) {
		
		// Set the webview url
		if (mWebView == null) {
			FrameLayout layout = (FrameLayout)findViewById(R.id.main);
			// Using 'this' instead of getApplication because getting 'FlashPaintSurface is RGB_565 (not OpenGL)'
			// coming back from another application.
			mWebView = new WebView(this);
			mWebView.setId(WEBVIEW_ID);
			mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setSupportZoom(true);
			// TODO: make this work with gesture
			mWebView.getSettings().setBuiltInZoomControls(true);
			mWebView.getSettings().setDefaultZoom(ZoomDensity.FAR);
			mWebView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
			mWebView.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onReceivedTitle(android.webkit.WebView view, java.lang.String title) {
					super.onReceivedTitle(view, title);
					setTitle(title);

					showNotification(title);
				}
			});

			layout.addView(mWebView);
		}
		
		if (webAddress != null && !webAddress.isEmpty())
			mWebView.loadUrl(webAddress); 
		else
			mWebView.loadUrl(getResources().getString(R.string.thesixtyone));
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) { 
			mWebView.requestFocus(View.FOCUS_DOWN);
			mWebView.setOnTouchListener(new View.OnTouchListener() {
		        @Override
		        public boolean onTouch(View v, MotionEvent event) {
		            switch (event.getAction()) {
		                case MotionEvent.ACTION_DOWN:
		                case MotionEvent.ACTION_UP:
		                    if (!v.hasFocus()) {
		                        v.requestFocus();
		                    }
		                    break;
		            }
		            return false;
		        }
		    });
		}

		mPhoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (state == TelephonyManager.CALL_STATE_RINGING) {
					//Incoming call: Pause music
					mWebView.loadUrl("javascript:t61.miniplayer.pause()");
				} else if(state == TelephonyManager.CALL_STATE_IDLE) {
					//Not in call: Play music
					mWebView.loadUrl("javascript:t61.miniplayer.play()");
				} else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
					//A call is dialing, active or on hold
					mWebView.loadUrl("javascript:t61.miniplayer.pause()");
				}
				super.onCallStateChanged(state, incomingNumber);
			}
		};
		TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if(mgr != null) {
			mgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		}
		
		// Gesture detection
		mGestureDetector = new GestureDetector(new MyGestureDetector());
		mGestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return mGestureDetector.onTouchEvent(event);
			}
		};
		
		// add fling gesture, but only works on the actionbar
		getWindow().getDecorView().setOnTouchListener(mGestureListener);
	}
	
	class BroadcastsHandler extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equalsIgnoreCase(Intent.ACTION_HEADSET_PLUG)) {
				int state = intent.getIntExtra("state", -1);
				if (state == 1) {
					isHeadPhonesIn = true;
				} else {
					if (isHeadPhonesIn) {
						mWebView.loadUrl("javascript:t61.miniplayer.pause()");
					}
					isHeadPhonesIn = false;
				}
				Log.d(TAG, "HEADPHONES:" + state);
			}
		}

	}

	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				// right to left swipe
				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					mWebView.loadUrl("javascript:t61.playlist.play_next_song()");
				}  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					mWebView.loadUrl("javascript:t61.playlist.play_previous_song()");
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			return false;
		}
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if(mgr != null) {
			mgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
		
		if (mWebView != null) {
			mWebView.getSettings().setPluginState(WebSettings.PluginState.OFF);
			mWebView.destroy();
			mWebView = null;
		}

		mNotificationManager.cancel(NOTIFICATION_ID);

		unregisterReceiver(mBroadcastsHandler);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// TODO: handle device sleep based on pref
		mWake.release();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// TODO: handle device sleep based on pref
		mWake.acquire();
		getWindow().closeAllPanels();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);

		// Calling super after populating the menu is necessary here to ensure that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mWebView.loadUrl("javascript:t61.hearts.heart_current_song()");
			break;

		case R.id.menu_next:
			mWebView.loadUrl("javascript:t61.playlist.play_next_song()");
			break;

		case R.id.menu_prev:
			mWebView.loadUrl("javascript:t61.playlist.play_previous_song()");
			break;

		case R.id.menu_play:
			mWebView.loadUrl("javascript:t61.shortcut.toggle_play()");
			break;

		case R.id.settings:
			Class<?> pref = null;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				pref = T61RadioPreferenceActivity.class;
			} else {
				pref = T61RadioPreferenceFragment.class;
			}
			Intent prefIntent = new Intent(getApplicationContext(), pref);
			startActivity(prefIntent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.d(TAG, "KEYCODE: " + event.getKeyCode() );
		if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			lp.screenBrightness = 0.01f;
			getWindow().setAttributes(lp);
			return true;
		} else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			// Already using dispatch key event, so what the hell.
			
	        if (event.getAction() == KeyEvent.ACTION_DOWN
	                && event.getRepeatCount() == 0) {

	            // Tell the framework to start tracking this event.
	        	getWindow().getDecorView().getKeyDispatcherState().startTracking(event, this);
	            return true;

	        } else if (event.getAction() == KeyEvent.ACTION_UP) {
	        	getWindow().getDecorView().getKeyDispatcherState().handleUpEvent(event);
	            if (event.isTracking() && !event.isCanceled()) {

	                // DO BACK ACTION HERE
	    			Boolean showAlerts = T61RadioSharedPreferences.getShowAlertFlag(getApplicationContext());
	    			Boolean alertExit = T61RadioSharedPreferences.getAlertExitFlag(getApplicationContext());
	    			if (showAlerts && alertExit) {
	    				AlertDialog.Builder builder;
	    				AlertDialog alertDialog;

	    				Context mContext = this;
	    				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
	    				View layout = inflater.inflate(R.layout.alert_dialog,
	    				                               (ViewGroup) findViewById(R.id.layout_root));

	    				TextView text = (TextView) layout.findViewById(R.id.text);
	    				text.setText(getResources().getString(R.string.confirm_exit));
	    				
	    				final CheckBox checkBox = (CheckBox) layout.findViewById(R.id.check_box);

	    				builder = new AlertDialog.Builder(mContext);
	    				builder.setView(layout)
	    				       .setCancelable(true)
	    				       .setPositiveButton(getResources().getString(R.string.yes), 
	    				    		   new DialogInterface.OnClickListener() {
								
									@Override
									public void onClick(DialogInterface dialog, int which) {
										if (!checkBox.isChecked()) {
											T61RadioSharedPreferences.setAlertExitFlag(getApplicationContext(), 
													false);
										}
										MainActivity.this.finish();
									}
								})
							   .setNegativeButton(getResources().getString(R.string.no), 
	    				    		   new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										if (!checkBox.isChecked()) {
											T61RadioSharedPreferences.setAlertExitFlag(getApplicationContext(), 
													false);
										}
										dialog.cancel();
									}
								});
	    				alertDialog = builder.create();
	    				alertDialog.show();
	    				return true;
	    			} else {
	    				MainActivity.this.finish();
	    				return true;
	    			}
	            }
	        }
	        return super.dispatchKeyEvent(event);
	    } else {
	        return super.dispatchKeyEvent(event);
	    }
	}

//	@Override
//	public void onClick(View v) {
//	}
}
