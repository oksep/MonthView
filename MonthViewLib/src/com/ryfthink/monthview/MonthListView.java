package com.ryfthink.monthview;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;

import com.ryfthink.monthview.MonthItemView.OnDayClickListener;

/**
 * This view do not support adding header nor rolling circle... <br>
 * If you have a nice simple solution, connect me learn learn :) <br>
 * <br>
 * Sorry for my poor English.囧rz
 * @author ryfthink@gmail.com<br>
 */
public class MonthListView extends ListView implements OnScrollListener, OnDayClickListener {

	private final int YEARFROM = 2010;
	private final int YEARTO = 2020;
	private final int MAX_ANIMTIME = 800;
	private int mAnimDuration;
	private boolean mNeedAnim = false;
	private int mScrollDistance;// while SDK<HONEYCOMB ,scroll by this value

	public MonthListView(Context context) {
		super(context);
		init(context);
	}

	public MonthListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MonthListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		setAdapter(new MonthListAdapter(context, YEARFROM, YEARTO, this));
		setOnScrollListener(this);
		setVerticalFadingEdgeEnabled(false);

		getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
	}

	private OnGlobalLayoutListener mGlobalLayoutListener = new OnGlobalLayoutListener() {

		/** adjust this listView's height we wanted */
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
		public void onGlobalLayout() {
			ViewGroup.LayoutParams params = getLayoutParams();
			params.width = getWidth();
			params.height = getChildAt(0).getHeight();
			setLayoutParams(params);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				getViewTreeObserver().removeGlobalOnLayoutListener(mGlobalLayoutListener);
			} else {
				getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
			}
		}
	};

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// here need invoke post() method
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) post(mScrollRunnable);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}

	private Runnable mScrollRunnable = new Runnable() {

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void run() {
			int dstPosition = getDstPosition();
			if (!mNeedAnim) return;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				smoothScrollBy(mScrollDistance, mAnimDuration);
			} else {
				smoothScrollToPositionFromTop(dstPosition, 0, mAnimDuration);
			}
		}
	};

	/** get the dst position */
	private int getDstPosition() {
		int firstVisiblePostion = getFirstVisiblePosition();
		int dstPosition = firstVisiblePostion;
		// !!! @see MonthListAdapter#getView();
		View v = findViewById(firstVisiblePostion);
		if (v == null) {
			mNeedAnim = false;
			return dstPosition;
		}
		int itemTop = v.getTop();
		if (itemTop == 0) {
			mNeedAnim = false;
			return dstPosition;
		} else {
			mNeedAnim = true;
		}
		int itemHeight = v.getHeight();

		if (Math.abs(itemTop) > itemHeight / 2) {
			dstPosition += 1;
			mScrollDistance = itemHeight - Math.abs(itemTop);
			mAnimDuration = MAX_ANIMTIME * (itemHeight - Math.abs(itemTop)) / itemHeight;
		} else {
			mScrollDistance = itemTop;
			mAnimDuration = MAX_ANIMTIME * Math.abs(itemTop) / itemHeight;
		}
		return dstPosition;
	}

	@Override
	/**ingnore */
	public void addHeaderView(View v) {
		return;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCurrMonthDayClick(Calendar c) {
		invoke(c);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onNextMonthDayClick(Calendar c) {
		int dstPosition = getFirstVisiblePosition() + 1;

		dstPosition = dstPosition >= getCount() ? getFirstVisiblePosition() : dstPosition;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			setSelection(dstPosition);
		} else {
			smoothScrollToPositionFromTop(dstPosition, 0, MAX_ANIMTIME / 2);
		}
		invoke(c);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onPreMonthDayClick(Calendar c) {

		int dstPosition = getFirstVisiblePosition() - 1;
		dstPosition = dstPosition < 0 ? 0 : dstPosition;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			setSelection(dstPosition);
		} else {
			smoothScrollToPositionFromTop(dstPosition, 0, MAX_ANIMTIME / 2);
		}
		invoke(c);
	};

	/** After get the selectedCalendar, do whatever u want. */
	private void invoke(Calendar c) {
		Toast.makeText(getContext(),
				c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH), 0)
				.show();
	}
}
