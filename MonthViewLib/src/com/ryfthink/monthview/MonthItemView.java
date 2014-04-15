package com.ryfthink.monthview;


import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MonthItemView extends View {

	private static final String TAG = MonthItemView.class.getSimpleName();

	/** week labels */
	private static final String[] mWeekLabelValues = Arrays.copyOfRange(
			(DateFormatSymbols.getInstance().getShortWeekdays()), 1, 8);
	/** month labels */
	private static final String[] mMonthLabelValues = Arrays.copyOfRange(
			(DateFormatSymbols.getInstance().getShortMonths()), 0, 12);
	private final int ROW_COUNT = 6;// 6行
	/** 填充数据 */
	private DayEntity[] mValues;

	{
		mValues = new DayEntity[ROW_COUNT * mWeekLabelValues.length];
		for (int i = 0; i < mValues.length; i++) {
			mValues[i] = new DayEntity();
		}
	}

	/** default: android.R.Color.holo_blue_dark */
	private int mWeekLabelBgColor = 0xff0099cc;
	/** default: height=100 */
	private int mWeekLabelHeight = 100;
	private Rect mWeekLableRect;
	/** weekLable text size */
	private float mWeekLableTxtSize = 35.0f;
	private int mWeekLableTxTColor = 0xFF9EB4BF;

	private int mLineColor = Color.RED;
	private float mLineWidth = 2.0f;

	private float mAverageColumnW;// day平均宽度
	private float mAverageRowH;// day平均高度

	private int mDayTxtColor = Color.RED;
	private float mDayTxtSize = 20.0f;

	private int mYear;// 当前视图所在年
	private int mMonth;// 当前视图所在月
	private Calendar mSelectedCalendar;// 被选中的那一天日历

	public MonthItemView(Context context) {
		this(context, null);
	}

	public MonthItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MonthItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarStyle, R.attr.CalendarStyle, 0);

			mWeekLabelBgColor = a.getColor(R.styleable.CalendarStyle_weekLabelBg, mWeekLabelBgColor);
			mWeekLabelHeight = (int) a.getDimension(R.styleable.CalendarStyle_weekLabelHeight, mWeekLabelHeight);
			mWeekLableTxTColor = a.getColor(R.styleable.CalendarStyle_weekLabelTxtColor, mWeekLableTxTColor);
			mWeekLableTxtSize = a.getDimension(R.styleable.CalendarStyle_weekLabelTxtSize, mWeekLableTxtSize);

			mLineColor = a.getColor(R.styleable.CalendarStyle_lineColor, mLineColor);
			mLineWidth = a.getDimension(R.styleable.CalendarStyle_lineWidth, mLineWidth);

			mDayTxtColor = a.getColor(R.styleable.CalendarStyle_datTxtColor_Normal, mDayTxtColor);
			mDayTxtSize = a.getDimension(R.styleable.CalendarStyle_dayTxtSize_Normal, mDayTxtSize);

			mSelectedDayColor = a.getColor(R.styleable.CalendarStyle_selectedColor, mSelectedDayColor);
			mOutMonthColor = a.getColor(R.styleable.CalendarStyle_outMonthColor, mOutMonthColor);

			a.recycle();
		}
		init();
		setGestureListener();
	}

	private void init() {
		mPaintWeekBg = new Paint();
		mPaintWeekBg.setColor(mWeekLabelBgColor);
		mPaintWeekBg.setStrokeWidth(10);

		mWeekLabelTxtPaint = new TextPaint();
		mWeekLabelTxtPaint.setTextSize(mWeekLableTxtSize);
		mWeekLabelTxtPaint.setColor(mWeekLableTxTColor);
		mWeekLabelTxtPaint.setFakeBoldText(true);
		mWeekLableRect = new Rect();

		mLinePaint = new Paint();
		mLinePaint.setStrokeWidth(mLineWidth);
		mLinePaint.setColor(mLineColor);

		mTextPaint = new TextPaint();
		mTextPaint.setTextSize(mDayTxtSize);
		mTextPaint.setColor(mDayTxtColor);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setDither(true);
		mTextPaint.setUnderlineText(true);

		mDayPaint = new Paint();
		mDayRect = new RectF();
	}

	/**
	 * 加载数据
	 * @param adaptCal 适配日历
	 * @param selectCal 选中日历
	 * @param year所在年
	 * @param month所在月
	 * @param today今天
	 * @param currMonth今天所在月
	 * @param currYear今天坐在年
	 */
	public void setParams(Calendar adaptCal, Calendar selectCal, int year, int month, int today, int currMonth,
			int currYear) {
		mSelectedCalendar = selectCal;
		mYear = year;
		mMonth = month;
		if (adaptCal == null) adaptCal = Calendar.getInstance();
		adaptCal.set(Calendar.YEAR, year);
		adaptCal.set(Calendar.MONTH, month);
		adaptCal.set(Calendar.DAY_OF_MONTH, 1);
		adaptCal.add(Calendar.DAY_OF_MONTH, 1 - adaptCal.get(Calendar.DAY_OF_WEEK));
		for (DayEntity entity : mValues) {
			int m = adaptCal.get(Calendar.MONTH);
			boolean inMonth = m == month;
			int y = adaptCal.get(Calendar.YEAR);
			boolean inYear = y == year;
			int d = adaptCal.get(Calendar.DAY_OF_MONTH);

			boolean isToday = (currYear == year) && (month == currMonth) && adaptCal.get(Calendar.DAY_OF_YEAR) == today;

			entity.setData(y, m, d, isToday, inMonth);
			boolean selected = mSelectedCalendar.get(Calendar.YEAR) == y && mSelectedCalendar.get(Calendar.MONTH) == m
					&& mSelectedCalendar.get(Calendar.DAY_OF_MONTH) == d;
			entity.setSelected(selected);

			boolean isFirstOfMonth = inMonth && adaptCal.get(Calendar.DAY_OF_MONTH) == 1;// mark
			if (isFirstOfMonth) {// 每月第一天

			}
			adaptCal.add(Calendar.DAY_OF_MONTH, 1);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		mAverageColumnW = 1f * width / mWeekLabelValues.length;
		mAverageRowH = mAverageColumnW;
		setMeasuredDimension(width, (int) (mWeekLabelHeight + ROW_COUNT * mAverageRowH));
		mWeekLableRect.set(0, 0, width, mWeekLabelHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawWeekLable(canvas);
		drawDays(canvas);
		drawLines(canvas);
	}

	private Paint mPaintWeekBg;// week背景
	private TextPaint mWeekLabelTxtPaint;// week文字

	/** 绘制星期label */
	private void drawWeekLable(Canvas canvas) {
		canvas.drawRect(mWeekLableRect, mPaintWeekBg);

		float weekLabelTxtW;// week文字宽度
		float y = mWeekLableRect.height() / 2 + mWeekLabelTxtPaint.getTextSize() / 2;
		float x;
		for (int i = 0; i < mWeekLabelValues.length; i++) {
			weekLabelTxtW = mWeekLabelTxtPaint.measureText(mWeekLabelValues[i]);
			x = (mAverageColumnW - weekLabelTxtW) / 2 + mAverageColumnW * i;
			canvas.drawText(mWeekLabelValues[i], x, y, mWeekLabelTxtPaint);
		}
	}

	private Paint mLinePaint;

	/** 绘制分隔线 */
	private void drawLines(Canvas canvas) {
		// column
		float x;
		for (int i = 1; i < mWeekLabelValues.length; i++) {
			x = mAverageColumnW * i;
			canvas.drawLine(x, mWeekLabelHeight, x, getHeight(), mLinePaint);
		}
		// row
		float y;
		for (int i = 0; i < ROW_COUNT; i++) {
			y = mWeekLabelHeight + mAverageRowH * i;
			canvas.drawLine(0, y, getWidth(), y, mLinePaint);
		}
	}

	private TextPaint mTextPaint;
	private Paint mDayPaint;
	private RectF mDayRect;

	/** 绘制每一天 */
	private void drawDays(Canvas canvas) {
		int index = 0;
		float x;
		float y;
		float txtW;
		String str;
		DayEntity entity;
		for (int i = 0; i < ROW_COUNT; i++) {
			for (int j = 0; j < mWeekLabelValues.length; j++) {
				entity = mValues[index];
				str = String.valueOf(entity.day);
				x = mAverageColumnW * j;
				y = mAverageRowH * i + mWeekLabelHeight;
				mDayRect.set(x, y, x + mAverageColumnW, y + mAverageRowH);
				drawOutMonthDays(mDayRect, canvas, entity);
				drawSelectedDays(mDayRect, canvas, entity);
				txtW = mTextPaint.measureText(str);
				x = (mAverageColumnW - txtW) / 2 + mAverageColumnW * j;
				y = mAverageRowH / 2 + mDayTxtSize / 2 + mWeekLabelHeight + mAverageRowH * i;
				canvas.drawText(str, x, y, mTextPaint);
				index++;
			}
		}
	}

	/** 选中的day颜色 */
	private int mSelectedDayColor = Color.parseColor("#FF1A992C");

	private void drawSelectedDays(RectF rect, Canvas canvas, DayEntity dayEntity) {
		if (dayEntity.selected) {
			mDayPaint.setColor(mSelectedDayColor);
			canvas.drawRect(rect, mDayPaint);
		}
	}

	/** 不在本月的蒙版颜色 */
	private int mOutMonthColor = Color.parseColor("#FFE2E3E4");

	/** 绘制非本月方块 */
	private void drawOutMonthDays(RectF rect, Canvas canvas, DayEntity entity) {
		if (entity.isToday) {
			mDayPaint.setColor(mWeekLabelBgColor);
		} else if (entity.inMonth) {
			return;
		} else {
			mDayPaint.setColor(mOutMonthColor);
		}
		canvas.drawRect(rect, mDayPaint);
	}

	/** 元数据 */
	private class DayEntity {

		int year, month, day;
		boolean isToday, inMonth, selected;

		DayEntity() {
		}

		void setData(int y, int m, int d, boolean isToday, boolean inMonth) {
			year = y;
			month = m;
			day = d;
			this.isToday = isToday;
			this.inMonth = inMonth;
		}

		public void setSelected(boolean b) {
			selected = b;
		}

		@Override
		public String toString() {
			return year + "-" + (month + 1) + "-" + day;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			mGestureDetector.onTouchEvent(event);
			return true;
		}
		return super.onTouchEvent(event);
	}

	private GestureDetectorCompat mGestureDetector;

	private void setGestureListener() {
		OnGestureListener listener = new SimpleOnGestureListener() {

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				Calendar c = getDayByLocation(e.getX(), e.getY());
				if (mOnDayClickListener != null && c != null) {
					int y = c.get(Calendar.YEAR);
					int m = c.get(Calendar.MONTH);
					if (y == mYear && m == mMonth) {
						mOnDayClickListener.onCurrMonthDayClick(c);
					} else if (y < mYear || (y == mYear && m < mMonth)) {
						mOnDayClickListener.onPreMonthDayClick(c);
					} else {
						mOnDayClickListener.onNextMonthDayClick(c);
					}
				}
				return true;
			}
		};
		mGestureDetector = new GestureDetectorCompat(getContext(), listener);
	}

	private Rect mLocalVisibleRect = new Rect();

	private Calendar getDayByLocation(float x, float y) {
		getLocalVisibleRect(mLocalVisibleRect);
		mLocalVisibleRect.set(mLocalVisibleRect.left, mLocalVisibleRect.top + mWeekLabelHeight,
				mLocalVisibleRect.right, mLocalVisibleRect.bottom);
		if (!mLocalVisibleRect.contains((int) x, (int) y)) {
			return null;
		}
		int index = (int) (x / mAverageColumnW) + (int) ((y / mAverageRowH) - 1) * mWeekLabelValues.length;
		if (index < 0 || index >= mValues.length) {
			return null;
		}
		for (DayEntity d : mValues) {
			d.setSelected(false);
		}
		DayEntity entity = mValues[index];
		entity.setSelected(true);
		invalidate();
		mSelectedCalendar.set(entity.year, entity.month, entity.day);
		return mSelectedCalendar;
	}

	private OnDayClickListener mOnDayClickListener;

	public void setOnDayClickListener(OnDayClickListener mOnDayClickListener) {
		this.mOnDayClickListener = mOnDayClickListener;
	}

	public interface OnDayClickListener {

		void onCurrMonthDayClick(Calendar c);

		void onNextMonthDayClick(Calendar c);

		void onPreMonthDayClick(Calendar c);
	}
}
