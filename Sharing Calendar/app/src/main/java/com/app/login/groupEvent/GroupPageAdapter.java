package com.app.login.groupEvent;

import static com.app.login.ConstantsKt.WEEK_START_TIMESTAMP;
import static com.app.login.ConstantsKt.GROUP_EVENT_START_TIMESTAMP;
import static com.app.login.ConstantsKt.GROUP_EVENT_END_TIMESTAMP;
import static com.app.login.ConstantsKt.GROUP_EVENT_ID;

import android.os.Bundle;
import android.util.SparseArray;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class GroupPageAdapter extends FragmentStatePagerAdapter {
    private List<Long> mWeekTimestamps; // 每周的时间戳
    private long groupEventId; // 群体事件ID
    private long groupEventStartTimestamp; // 群体事件的开始时间
    private long groupEventEndTimestamp; // 群体事件的结束时间
    private SparseArray<GroupViewFragment> mFragments; // 保存 fragment 的引用
    private GroupViewFragment.WeekScrollListener mListener; // 滚动监听器

    // 修改构造函数，增加滚动监听器
    public GroupPageAdapter(FragmentManager fm, List<Long> weekTimestamps, long eventStartTimestamp, long eventEndTimestamp, long eventId, GroupViewFragment.WeekScrollListener listener) {
        super(fm);
        this.mWeekTimestamps = weekTimestamps;
        this.groupEventStartTimestamp = eventStartTimestamp;
        this.groupEventEndTimestamp = eventEndTimestamp;
        this.groupEventId = eventId;
        this.mFragments = new SparseArray<>();
        this.mListener = listener;  // 设置滚动监听器
    }

    @Override
    public int getCount() {
        return mWeekTimestamps.size(); // 返回页数
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        long weekTimestamp = mWeekTimestamps.get(position);

        bundle.putLong(WEEK_START_TIMESTAMP, weekTimestamp);
        bundle.putLong(GROUP_EVENT_START_TIMESTAMP, groupEventStartTimestamp);
        bundle.putLong(GROUP_EVENT_END_TIMESTAMP, groupEventEndTimestamp);
        bundle.putLong(GROUP_EVENT_ID, groupEventId);  // 在 bundle 中传递 groupEventId

        GroupViewFragment fragment = new GroupViewFragment();
        fragment.setArguments(bundle);
        fragment.setListener(mListener); // 设置滚动监听器

        mFragments.put(position, fragment);
        return fragment;
    }

    // 更新滚动 Y 坐标
    public void updateScrollY(int pos, int y) {
        for (int i = -1; i <= 1; i++) {
            GroupViewFragment fragment = mFragments.get(pos + i);
            if (fragment != null) {
                fragment.updateScrollY(y); // 确保调用 fragment 的滚动更新方法
            }
        }
    }

    public GroupViewFragment getFragment(int position) {
        return mFragments.get(position);
    }
}
