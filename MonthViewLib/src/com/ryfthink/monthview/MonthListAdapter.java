package com.ryfthink.monthview;


import java.util.Calendar;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ryfthink.monthview.MonthItemView.OnDayClickListener;


public class MonthListAdapter extends BaseAdapter {

	private static final int MONTH_COUNT = 12;
	private int mYearFrom = 2000;
	private int mYearTo = 2100;
	private int mToday;// 今天
	private int mCurrYear;// 今天所在年
	private int mCurrMonth;// 今天所在月
	private Calendar mAdapterCalendar = Calendar.getInstance();// 适配数据日历
	private Calendar mSelectedCalendar = Calendar.getInstance();// 当前选中日历
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private OnDayClickListener mOnDayClickListener;

	public MonthListAdapter(Context context, int yearFrom, int yearTo, OnDayClickListener l) {
		if (yearFrom > yearTo) {
			throw new IllegalArgumentException("yearTo must >= yearFrom");
		}
		this.mYearFrom = yearFrom;
		this.mYearTo = yearTo;
		this.mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		this.mOnDayClickListener = l;
		mToday = mAdapterCalendar.get(Calendar.DAY_OF_YEAR);
		mCurrYear = mAdapterCalendar.get(Calendar.YEAR);
		mCurrMonth = mAdapterCalendar.get(Calendar.MONTH);
	}

	@Override
	public int getCount() {
		return (mYearTo - mYearFrom + 1) * MONTH_COUNT;
	}

	public int getTodayMonthView() {
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.calendar_item, parent, false);
		}
		MonthItemView item = Holder.get(convertView, R.id.item);
		int month = position % MONTH_COUNT;
		int year = mYearFrom + position / MONTH_COUNT;
		item.setParams(mAdapterCalendar, mSelectedCalendar, year, month, mToday, mCurrMonth, mCurrYear);
		TextView txt = Holder.get(convertView, R.id.txt);
		// here month is start from 0,so plus 1
		txt.setText(mContext.getResources().getString(R.string.year_month, year, (month + 1)));
		item.getMeasuredHeight();
		item.setOnDayClickListener(mOnDayClickListener);
		convertView.setId(position);
		return convertView;
	}
}


// An easy holder Class
class Holder {

	@SuppressWarnings("unchecked")
	public static <T extends View> T get(View view, int id) {
		SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
		if (viewHolder == null) {
			viewHolder = new SparseArray<View>();
			view.setTag(viewHolder);
		}
		View childView = viewHolder.get(id);
		if (childView == null) {
			childView = view.findViewById(id);
			viewHolder.put(id, childView);
		}
		return (T) childView;
	}
}
