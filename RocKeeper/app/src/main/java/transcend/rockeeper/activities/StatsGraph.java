package transcend.rockeeper.activities;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import transcend.rockeeper.data.StatContract.Stat;
import activities.rockeeper.R;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.*;
import com.db.chart.view.*;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.QuintEase;
import com.db.chart.view.animation.style.DashAnimation;

public class StatsGraph {

	private Object mLineTooltip;
	private Paint mLineGridPaint;
	private LineChartView mLineChart;
	
	private static final String WEEK = "WEEK";
	private static final String MONTH = "MONTH";
	private static final String YEAR = "YEAR";
	
	private float mCurrOverlapFactor = 1;
	private QuintEase mCurrEasing = new QuintEase();
	private int mCurrStartX = -1;
	private int mCurrStartY = 0;	
	private int mCurrAlpha = -1;
	
	private float mOldOverlapFactor = 1;
	private QuintEase mOldEasing = new QuintEase();
	private int mOldStartX = -1;
	private int mOldStartY = 0;	
	private int mOldAlpha = -1;
	
	private static int[] mCurrOverlapOrder;
	private static int[] mOldOverlapOrder;
	
	private static final int DOT_COLOR = 0xFF63B0FF;
	private static final int LINES = 0x10000000;
	
	private final OnEntryClickListener lineEntryListener = new OnEntryClickListener(){
		@Override
		public void onClick(int setIndex, int entryIndex, Rect rect) {

			if(mLineTooltip == null)
				showLineTooltip(setIndex, entryIndex, rect);
			else
				dismissLineTooltip(setIndex, entryIndex, rect);
		}
	};
	
	private final OnClickListener lineClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(mLineTooltip != null)
				dismissLineTooltip(-1, -1, null);
		}
	};

	
	
	private void showLineTooltip(int setIndex, int entryIndex, Rect rect) {
		
	}
	
	private void dismissLineTooltip(int setIndex, int entryIndex,
			Rect rect) {
		
	}
	
	public StatsGraph(Fragment f, ArrayList<Stat> stats, String range) {
		mLineChart = (LineChartView)f.getActivity().findViewById(R.id.rockStats);
		mLineChart.setOnEntryClickListener(lineEntryListener);
		mLineChart.setOnClickListener(lineClickListener);
		
		mLineGridPaint = new Paint();
		mLineGridPaint.setColor(LINES);
		mLineGridPaint.setPathEffect(new DashPathEffect(new float[] {5,0}, 0));
		mLineGridPaint.setStyle(Paint.Style.STROKE);
		mLineGridPaint.setAntiAlias(true);
		mLineGridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));
		
		updateView(range);
	}
	
	public void updateView(String range){
		
		mLineChart.reset();
		
		Date now = new Date();
		
		String[] lineLabels = getLineLabels(now, range);
		
		LineSet dataSet = new LineSet(lineLabels, lineValues[0]);
		dataSet.setDotsColor(Color.WHITE)
			.setDotsRadius(Tools.fromDpToPx(5))
			.setDotsStrokeThickness(Tools.fromDpToPx(2))
			.setDotsStrokeColor(DOT_COLOR)
			.setColor(DOT_COLOR)
			.setThickness(Tools.fromDpToPx(3))
			.beginAt(0).endAt(lineLabels.length);
		mLineChart.addData(dataSet);
		
		dataSet = new LineSet(lineLabels, lineValues[1]);
		dataSet.setColor(DOT_COLOR)
			.setThickness(Tools.fromDpToPx(3))
			.setSmooth(true);
			//.setDashed(new float[]{10, 10});
		mLineChart.addData(dataSet);
		
		mLineChart.setBorderSpacing(Tools.fromDpToPx(4))
			.setGrid(LineChartView.GridType.HORIZONTAL, mLineGridPaint)
			.setXAxis(false)
			.setXLabels(XController.LabelPosition.OUTSIDE)
			.setYAxis(false)
			.setYLabels(YController.LabelPosition.OUTSIDE)
			.setAxisBorderValues(LINE_MIN, LINE_MAX, 5)
			.setLabelsFormat(new DecimalFormat("##'u'"))
			//.show(getAnimation(true))
			.show()
			;
		
		mLineChart.animateSet(1, new DashAnimation());
	}
	
	private String[] getLineLabels(Date now, String range) {
		String[] labels = null;
		if(range.equals(WEEK)){
			labels = new String[7];
			Calendar c = new GregorianCalendar();
			c.setTime(now);
			for(int i = 6; i <= 0; i++){
				labels[i] = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
				c.add(Calendar.DATE, -1);
			}
		} else if(range.equals(MONTH)){
			labels = new String[30];
			Calendar c = new GregorianCalendar();
			c.setTime(now);
			for(int i = 29; i <= 0; i++){
				labels[i] = c.getDisplayName(Calendar.DATE, Calendar.SHORT, Locale.getDefault());
				c.add(Calendar.DATE, -1);
			}
		} else if(range.equals(YEAR)){
			labels = new String[12];
			Calendar c = new GregorianCalendar();
			c.setTime(now);
			for(int i = 11; i <= 0; i++){
				labels[i] = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
				c.add(Calendar.MONTH, -1);
			}
		}
		return labels;
	}

	private Animation getAnimation(boolean newAnim){
		if(newAnim)
			return new Animation()
					.setAlpha(mCurrAlpha)
					.setEasing(mCurrEasing)
					.setOverlap(mCurrOverlapFactor, mCurrOverlapOrder)
					.setStartPoint(mCurrStartX, mCurrStartY);
		else
			return new Animation()
					.setAlpha(mOldAlpha)
					.setEasing(mOldEasing)
					.setOverlap(mOldOverlapFactor, mOldOverlapOrder)
					.setStartPoint(mOldStartX, mOldStartY);
	}
}
