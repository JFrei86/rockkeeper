package transcend.rockeeper.activities;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import transcend.rockeeper.data.Contract.Unit;
import transcend.rockeeper.data.StatContract;
import transcend.rockeeper.data.StatContract.Stat;
import transcend.rockeeper.sqlite.DatabaseHelper;
import transcend.rockeeper.sqlite.Transaction;
import activities.rockeeper.R;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.*;
import com.db.chart.view.*;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.QuintEase;

@SuppressLint("UseSparseArrays")
public class StatsGraph {
	
	public static final String WEEK = "WEEK";
	public static final String MONTH = "MONTH";
	public static final String YEAR = "YEAR";
	
	public static final String ATTEMPTS = StatContract.ATTEMPTS;
	public static final String POINTS = StatContract.POINTS;
	public static final String COMPLETED = StatContract.COMPLETED;
	
	private static int[] mCurrOverlapOrder;
	private static int[] mOldOverlapOrder;
	
	private static final int DOT_COLOR = 0xFF63B0FF;
	private static final int LINES = 0x10000000;
	
	private HashMap<Long, Stat> stats;
	private String range;
	private String column;
	private ProgressBar prog;
	private DatabaseHelper dbh;
	private SQLiteDatabase db;
	private float[] lineValues;
	
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
	
	private Object mLineTooltip;
	private Paint mLineGridPaint;
	private LineChartView mLineChart;
	
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
	
	public StatsGraph(Fragment f, ProgressBar prog) {
		mLineChart = (LineChartView)f.getActivity().findViewById(R.id.rockStats);
		mLineChart.setOnEntryClickListener(lineEntryListener);
		mLineChart.setOnClickListener(lineClickListener);
		
		mLineGridPaint = new Paint();
		mLineGridPaint.setColor(LINES);
		mLineGridPaint.setPathEffect(new DashPathEffect(new float[] {5,0}, 0));
		mLineGridPaint.setStyle(Paint.Style.STROKE);
		mLineGridPaint.setAntiAlias(true);
		mLineGridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));
		
		this.prog = prog;
		this.range = WEEK;
		this.column = COMPLETED;
		this.stats = new HashMap<Long, Stat>();
		
		this.dbh = new DatabaseHelper(f.getActivity(), null);
		this.db = dbh.getReadableDatabase();
		
		refresh();
	}
	
	public void setRange(String range){
		this.range = range;
		refresh();
	}
	
	public void setColumn(String column){
		this.column = column;
		refresh();
	}
	
	public void updateView(){
		
		mLineChart.reset();
		Date now = new Date();
		String[] lineLabels = getLineLabels(now, range);
		
		if(lineValues == null || lineValues.length != lineLabels.length){
			mLineChart.removeAllViews();
			return;
		}
		Log.i("DEBUG", Arrays.toString(lineLabels));
		Log.i("DEBUG", Arrays.toString(lineValues));
		
		LineSet dataSet = new LineSet(lineLabels, lineValues);
		dataSet.setDotsColor(DOT_COLOR)
			.setDotsRadius(Tools.fromDpToPx(5))
			.setDotsStrokeThickness(Tools.fromDpToPx(3))
			.setDotsStrokeColor(DOT_COLOR)
			.setColor(DOT_COLOR)
			.setDotsColor(Color.WHITE)
			.setThickness(Tools.fromDpToPx(3))
			.beginAt(0).endAt(lineLabels.length);
		
		mLineChart.addData(dataSet);
		
		int max = getMax();
		int i = 2;
		if(max > 10);
			i = 5;
		if(max > 50)
			i = 10;
		if(max > 100)
			i = 50;
		if(max > 500)
			i = 100;
		
		mLineChart.setBorderSpacing(Tools.fromDpToPx(4))
			.setGrid(LineChartView.GridType.HORIZONTAL, mLineGridPaint)
			.setXAxis(false)
			.setXLabels(XController.LabelPosition.OUTSIDE)
			.setYAxis(false)
			.setYLabels(YController.LabelPosition.OUTSIDE)
			.setAxisBorderValues(0, max, i)
			.setLabelsFormat(new DecimalFormat("##"))
			.show(getAnimation(true));
		
		//mLineChart.animateSet(1, new DashAnimation());
	}

	private int getMax(){
		if(lineValues == null || lineValues.length == 0)
			return 10;
		float max = lineValues[0];
		for(int i = 0; i < lineValues.length; i++){
			if(max < lineValues[i])
				max = lineValues[i];
		}
		if(max < 10)
			return 10;
		int i = 2;
		if(max >= 10);
			i = 5;
		if(max >= 50)
			i = 10;
		if(max >= 100)
			i = 50;
		if(max >= 500)
			i = 100;
		return (int)(Math.ceil(max / i) * i);
	}
	
	private String[] getLineLabels(Date now, String range) {
		String[] labels = null;
		if(range.equals(WEEK)){
			labels = new String[7];
			Calendar c = new GregorianCalendar();
			c.setTime(now);
			for(int i = 6; i >= 0; i--){
				labels[i] = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()).toUpperCase(Locale.US);
				c.add(Calendar.DATE, -1);
			}
		} else if(range.equals(MONTH)){
			labels = new String[10];
			Calendar c = new GregorianCalendar();
			c.setTime(now);
			for(int i = 29; i >= 0; i-= 3){
				labels[i / 3] = "";
				if(c.get(Calendar.DATE) < 4)
					labels[i / 3] += c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()).toUpperCase(Locale.US) + " " + c.get(Calendar.DATE);
				else
					labels[i / 3] += c.get(Calendar.DATE);
				c.add(Calendar.DATE, -3);
			}
		} else if(range.equals(YEAR)){
			labels = new String[12];
			Calendar c = new GregorianCalendar();
			c.setTime(now);
			for(int i = 11; i >= 0; i--){
				labels[i] = "" + c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()).toUpperCase(Locale.US);
				c.add(Calendar.MONTH, -1);
			}
		}
		return labels;
	}
	
	public void refresh() {
		stats.clear();
		prog.setVisibility(View.VISIBLE);
		Transaction t = new Transaction(db){
			private Date then;
			public void task(SQLiteDatabase db) {
				then = when();
				Cursor c = dbh.stats.query(null, StatContract.DATE + ">" + then.getTime(), null, StatContract.DATE, false, null, db);
				c.moveToFirst();
				while(!c.isAfterLast()){
					Stat s = dbh.stats.build(c);
					stats.put(Long.parseLong(s.get(StatContract.DATE)), s);
					c.moveToNext();
				}
			}
			private Date when() {
				GregorianCalendar c = new GregorianCalendar();
				c.set(Calendar.HOUR, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
				if(range == WEEK){
					c.add(Calendar.DATE, -7);
				} else if(range == MONTH){
					c.add(Calendar.MONTH, -1);
				} else if(range == YEAR){
					c.add(Calendar.YEAR, -1);
				}
				return c.getTime();
			}
			public void onComplete() {
				lineValues = inflateStats(then);
				updateView();
				prog.setVisibility(View.INVISIBLE);
			}
			public void onProgressUpdate(Unit... data) {}
		};
		t.run(true, true);
	}
	
	private float[] inflateStats(Date then){
		
		GregorianCalendar c = new GregorianCalendar();
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		float[] inflatedStats = new float[0];
		int size = 0;
		if(range == WEEK){
			inflatedStats = new float[7];
			size = 7;
		} else if(range == MONTH){
			inflatedStats = new float[10];
			size = 30;
		} else if(range == YEAR){
			inflatedStats = new float[12];
			size = 365;
		}
		for(int i = size - 1; i >= 0; i--){
			Stat s = stats.get(c.getTimeInMillis());
			if(s == null)
				s = dbh.stats.build(c.getTime(), 0, 0, 0);
			if(range == WEEK)
				inflatedStats[i] += Long.parseLong(s.get(column));
			else if(range == MONTH)
				inflatedStats[i / 3] += Long.parseLong(s.get(column));
			else{
				Date tmp = c.getTime();
				c.setTime(new Date());
				int offset = 12 - c.get(Calendar.MONTH) - 1;
				c.setTime(tmp);
				int index = (c.get(Calendar.MONTH) + offset) % 12;
				c.setTime(tmp);
				inflatedStats[index] += Long.parseLong(s.get(column));
			}
			c.add(Calendar.DATE, -1);
		}
		return inflatedStats;
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
