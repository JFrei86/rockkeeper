package transcend.rockeeper.activities;

import java.text.DecimalFormat;
import java.util.ArrayList;

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
	
	private final static int LINE_MAX = 10;
	private final static int LINE_MIN = -10;
	private final static String[] lineLabels = {"", "ANT", "GNU", "OWL", "APE", "JAY", ""};
	private final static float[][] lineValues = { {-5f, 6f, 2f, 9f, 0f, 1f, 5f},
												  {-9f, -2f, -4f, -3f, -7f, -5f, -3f}};
	
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
	
	private final static int[] beginOrder = {0, 1, 2, 3, 4, 5, 6};
	private final static int[] middleOrder = {3, 2, 4, 1, 5, 0, 6};
	private final static int[] endOrder = {6, 5, 4, 3, 2, 1, 0};
	private static final int DOT_COLOR = 0x0063B0FF * 1/2 + 0xFF000000;
	private static final int FOREGROUND = 0xFF63B0FF;
	private static final int LINES = 0x10000000;
	
	private final Fragment f;
	
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
	
	public StatsGraph(Fragment f, ArrayList<Stat> stats) {
		this.f = f;
		mLineChart = (LineChartView)f.getActivity().findViewById(R.id.rockStats);
		mLineChart.setOnEntryClickListener(lineEntryListener);
		mLineChart.setOnClickListener(lineClickListener);
		
		mLineGridPaint = new Paint();
		mLineGridPaint.setColor(LINES);
		mLineGridPaint.setPathEffect(new DashPathEffect(new float[] {5,0}, 0));
		mLineGridPaint.setStyle(Paint.Style.STROKE);
		mLineGridPaint.setAntiAlias(true);
		mLineGridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));
		
		updateView();
	}
	
	public void updateView(){
		mLineChart.reset();
		
		LineSet dataSet = new LineSet(lineLabels, lineValues[0]);
		dataSet.setDotsColor(DOT_COLOR)
			.setDotsRadius(Tools.fromDpToPx(5))
			.setDotsStrokeThickness(Tools.fromDpToPx(2))
			.setDotsStrokeColor(DOT_COLOR)
			.setColor(FOREGROUND)
			.setThickness(Tools.fromDpToPx(3))
			.beginAt(1).endAt(lineLabels.length - 1);
		mLineChart.addData(dataSet);
		
		dataSet = new LineSet(lineLabels, lineValues[1]);
		dataSet.setColor(FOREGROUND)
			.setThickness(Tools.fromDpToPx(3))
			.setSmooth(true)
			.setDashed(new float[]{10, 10});
		mLineChart.addData(dataSet);
		
		mLineChart.setBorderSpacing(Tools.fromDpToPx(4))
			.setGrid(LineChartView.GridType.HORIZONTAL, mLineGridPaint)
			.setXAxis(false)
			.setXLabels(XController.LabelPosition.OUTSIDE)
			.setYAxis(false)
			.setYLabels(YController.LabelPosition.OUTSIDE)
			.setAxisBorderValues(LINE_MIN, LINE_MAX, 5)
			.setLabelsFormat(new DecimalFormat("##'u'"))
			.show(getAnimation(true))
			//.show()
			;
		
		mLineChart.animateSet(1, new DashAnimation());
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
