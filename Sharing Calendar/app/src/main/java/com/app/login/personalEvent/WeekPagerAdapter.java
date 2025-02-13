package com.app.login.personalEvent;


import static com.app.login.ConstantsKt.WEEK_START_TIMESTAMP;

import android.os.Bundle;
import android.util.SparseArray;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import java.util.List;
public class WeekPagerAdapter extends FragmentStatePagerAdapter {
    private List<Long> mWeekTimestamps;
    private WeekFragment.WeekScrollListener mListener;
    private SparseArray<WeekFragment> mFragments;

    public WeekPagerAdapter(FragmentManager fm, List<Long> weekTimestamps, WeekFragment.WeekScrollListener listener) {
        super(fm);
        this.mWeekTimestamps = weekTimestamps;
        this.mListener = listener;
        this.mFragments = new SparseArray<>();
    }

    @Override
    public int getCount() {
        return mWeekTimestamps.size();
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        long weekTimestamp = mWeekTimestamps.get(position);
        bundle.putLong(WEEK_START_TIMESTAMP, weekTimestamp);

        WeekFragment fragment = new WeekFragment();
        fragment.setArguments(bundle);
        fragment.setListener(mListener);

        mFragments.put(position, fragment);
        return fragment;
    }

    public void updateScrollY(int pos, int y) {
        for (int i = -1; i <= 1; i++) {
            WeekFragment fragment = mFragments.get(pos + i);
            if (fragment != null) {
                fragment.updateScrollY(y); // 确保这里调用的是正确的方法
            }
        }
    }


}
