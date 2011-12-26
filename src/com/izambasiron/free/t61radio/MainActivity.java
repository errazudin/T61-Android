package com.izambasiron.free.t61radio;

import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.example.android.actionbarcompat.ActionBarActivity;

public class MainActivity extends ActionBarActivity implements OnClickListener {
    private static final String TAG = "com.izambasiron.free.t61radio.MainActivity";
    WebView mWebView;
	private WakeLock mWake;
	private GestureDetector mGestureDetector;
	private OnTouchListener mGestureListener;
	private static int SWIPE_MIN_DISTANCE;
    private static int SWIPE_THRESHOLD_VELOCITY;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final ViewConfiguration vc = ViewConfiguration.get(this);
        
        SWIPE_MIN_DISTANCE = vc.getScaledTouchSlop();
        SWIPE_THRESHOLD_VELOCITY = vc.getScaledMinimumFlingVelocity();;

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWake = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
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
                // nothing
            }
            return false;
        }

    }
    
    @Override
    protected void onPause() {
     super.onPause();
     
     // Destroy Flash along with it so it will not start 2 instance of Flash when this resumes
     mWebView.destroy();
 	 mWebView = null;
     mWake.release();
    }

    @Override
    protected void onResume() {
     super.onResume();
     // Set the webview url
     if (mWebView == null) {
     	mWebView = new WebView(this);
     	mWebView.getSettings().setPluginsEnabled(true);
     	mWebView.getSettings().setJavaScriptEnabled(true);
     	mWebView.setWebChromeClient(new WebChromeClient() {
     		@Override
     		public void onReceivedTitle(android.webkit.WebView view, java.lang.String title) {
     			super.onReceivedTitle(view, title);
     			Log.d(TAG, "Title: " + title);
     			setTitle(title);
     		}
     	});
     	
     	mWebView.loadUrl("http://www.thesixtyone.com/");
     	
     	FrameLayout layout = (FrameLayout)findViewById(R.id.main);
     	layout.addView(mWebView);
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
     
     mWake.acquire();
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
                //Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
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
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}
}
