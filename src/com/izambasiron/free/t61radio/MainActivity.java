package com.izambasiron.free.t61radio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.example.android.actionbarcompat.ActionBarActivity;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	private static final String TAG = "com.izambasiron.free.t61radio.MainActivity";
	WebView mWebView;
	// TODO: handle device sleep based on pref
	private WakeLock mWake;
	private GestureDetector mGestureDetector;
	private OnTouchListener mGestureListener;
	private PhoneStateListener mPhoneStateListener;
	private static int SWIPE_MIN_DISTANCE;
	private static int SWIPE_THRESHOLD_VELOCITY;

	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private BroadcastsHandler mBroadcastsHandler;
	private static final int WEBVIEW_ID = 1;
	private static final int NOTIFICATION_ID = 2;
	private Boolean isHeadPhonesIn = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final ViewConfiguration vc = ViewConfiguration.get(getApplication());

		SWIPE_MIN_DISTANCE = vc.getScaledTouchSlop();
		SWIPE_THRESHOLD_VELOCITY = vc.getScaledMinimumFlingVelocity();;

		// TODO: handle device sleep based on pref
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWake = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");


		Intent intent = getIntent();
		// check if this intent is started via url
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			createWebView(intent.getData().toString());
		} else {
			createWebView(null);
		}
		

		// Gesture detection
		mGestureDetector = new GestureDetector(new MyGestureDetector());
		mGestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return mGestureDetector.onTouchEvent(event);
			}
		};

		mWebView.setOnClickListener(this); 
		mWebView.setOnTouchListener(mGestureListener);

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

		// Prevent phone from being locked. Lockscreen kills the activity.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		// Show notification
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = R.drawable.ic_action_play;
		CharSequence tickerText = getResources().getString(R.string.app_name);
		long when = System.currentTimeMillis();

		mNotification = new Notification(icon, tickerText, when);
		mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		mNotification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

		// listen to headset plug in/out
		mBroadcastsHandler = new BroadcastsHandler();
		registerReceiver(mBroadcastsHandler, new android.content.IntentFilter(Intent.ACTION_HEADSET_PLUG));
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, intent.toString());
		super.onNewIntent(intent);
		
		// check if this intent is started via custom scheme link
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			mWebView.destroy();
			mWebView = null;
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

			// TODO: Not working
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
				layout.getRootView().setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		}
		
		if (webAddress != null && !webAddress.isEmpty())
			mWebView.loadUrl(webAddress); 
		else
			mWebView.loadUrl(getResources().getString(R.string.thesixtyone));
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
		mWebView.getSettings().setPluginState(WebSettings.PluginState.OFF);
		mWebView.destroy();
		mWebView = null;

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
			mWebView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
			mWebView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE));
			break;

		case R.id.settings:
			Class<?> pref = null;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				pref = MyPreferenceActivity.class;
			} else {
				pref = MyPreferenceFragment.class;
			}
			Intent prefIntent = new Intent(getApplication(), pref);
			startActivity(prefIntent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			lp.screenBrightness = 0.01f;
			getWindow().setAttributes(lp);
			return true;
		}

		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onClick(View v) {
	}
}
