package com.izambasiron.free.t61radio;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class BatteryDataUsagePreferenceActivity extends PreferenceActivity {
	private static final String TAG = "BatteryDataUsagePreferenceActivity";
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(2);
	private XYSeries mBatterySeries;
	private XYSeries mDataSeries;
	private XYSeriesRenderer mCurrentRenderer;
	private BatteryDataDbAdapter mBatteryDataDbAdapter;
	private GraphicalView mChartView;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getPreferenceManager().setSharedPreferencesName(
                T61RadioSharedPreferences.PREFS_NAME);
        addPreferencesFromResource(R.xml.preference_battery_data_usage);
        setContentView(R.layout.battery_data_usage_preference);
        
        try {
        	mBatteryDataDbAdapter = new BatteryDataDbAdapter(getApplicationContext());
        	mBatteryDataDbAdapter.open();
        } catch (Exception e) {
        	
        }
		
		String seriesTitle = "Battery Level";
		mBatterySeries = new XYSeries(seriesTitle, 0);
        mDataset.addSeries(0, mBatterySeries);
        
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        //renderer.setPointStyle(PointStyle.CIRCLE);
        //renderer.setFillPoints(true);
        renderer.setFillBelowLine(true);
        mRenderer.setYAxisMin(0, 0);
        mRenderer.setYAxisMax(100, 0);
        mRenderer.setYTitle("Percent", 0);
        mRenderer.addSeriesRenderer(0, renderer);
        
        XYSeriesRenderer renderer1 = new XYSeriesRenderer();
        //renderer1.setPointStyle(PointStyle.DIAMOND);
        //renderer1.setFillPoints(true);
        renderer1.setColor(Color.YELLOW);
        mRenderer.setYTitle("Bytes", 1);
        mRenderer.setYAxisMin(0, 1);
        mRenderer.setYAxisAlign(Align.RIGHT, 1);
        mRenderer.setYLabelsAlign(Align.LEFT, 1);
        mRenderer.addSeriesRenderer(1, renderer1);
        
        mRenderer.setZoomRate(2);
        mRenderer.setZoomEnabled(false, true);
        
        seriesTitle = "Network Usage";
		mDataSeries = new XYSeries(seriesTitle, 1);
        mDataset.addSeries(1, mDataSeries);
        
        //mCurrentRenderer = renderer;
        
        if (mChartView == null) {
        	LinearLayout layout = (LinearLayout) this.getWindow().findViewById(R.id.graph_layout);
            mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
            layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
                    200));
        }
        
        new GetBatteryDataUsage().execute();
	}
	
	private class GetBatteryDataUsage extends AsyncTask<Void, Number, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Cursor cursor = mBatteryDataDbAdapter.fetchAll();
			for (boolean hasItem = cursor.moveToFirst(); hasItem; hasItem = cursor.moveToNext()) {
				//int pathColId = filesCursor.getColumnIndex(FilesDbAdapter.KEY_PATH);
				int batteryColId = cursor.getColumnIndex(BatteryDataDbAdapter.KEY_BATTERY);
				int dataColId = cursor.getColumnIndex(BatteryDataDbAdapter.KEY_DATA);
				int rowIdColId = cursor.getColumnIndex(BatteryDataDbAdapter.KEY_ROWID);
				
				publishProgress(cursor.getInt(batteryColId), cursor.getLong(dataColId), cursor.getInt(rowIdColId));
			    
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Number... progress) {
			Log.d(TAG, progress[0] + " batt : " + progress[1] + " data : " + progress[2] + " row -- ");
			mBatterySeries.add(progress[2].longValue(), progress[0].intValue());
			mDataSeries.add(progress[2].longValue(), progress[1].longValue());
			if (mChartView != null) {
	          mChartView.repaint();
	        }
	    }
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBatteryDataDbAdapter.close();
	}
}
