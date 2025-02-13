package com.app.login.personalEvent;

import static com.app.login.ConstantsKt.EVENT_ID;
import static com.app.login.ConstantsKt.WEEK_SECONDS;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.app.login.Config;
import com.app.login.MainActivity;
import com.app.login.R;
import com.app.login.extensions.ContextExtensions;
import com.app.login.extensions.DateTimeExtensions;
import com.app.login.util.FormatterUtils;
import com.app.login.view.MyScrollView;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class PersonalEventFragment extends Fragment {
    private static final int PREFILLED_WEEKS = 53;
    private Config config;
    public static int mWeekScrollY = 0;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("PersonalEventFragment", "onCreateView called");
        rootView = inflater.inflate(R.layout.fragment_personal_event, container, false);
        config = ContextExtensions.getConfig(requireActivity().getApplicationContext());
        mWeekScrollY = (int) (getResources().getDimension(R.dimen.weekly_view_row_height) * DateTime.now().getHourOfDay());
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            // 如果需要的话，还可以设置导航图标
            // toolbar.setNavigationIcon(R.drawable.your_icon);
            // toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        }
        fillWeeklyViewPager();
        fillTimeLatter();
        setHasOptionsMenu(true);
        return rootView;
    }

    private void fillWeeklyViewPager() {
        List<Long> weekTSs = getWeekTimestamps();
        WeekPagerAdapter weeklyAdapter = new WeekPagerAdapter(getChildFragmentManager(), weekTSs, new WeekFragment.WeekScrollListener() {
            @Override
            public void scrollTo(int y) {
                MyScrollView weekViewHoursScrollview = rootView.findViewById(R.id.week_view_hours_scrollview);
                weekViewHoursScrollview.scrollTo(0, y);
                mWeekScrollY = y;
            }
        });

        ViewPager weekViewViewPager = rootView.findViewById(R.id.weekViewViewPager);
        weekViewViewPager.setAdapter(weeklyAdapter);
        weekViewViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                setupActionbarTitle(weekTSs.get(position));
            }
        });
        weekViewViewPager.setCurrentItem(weekTSs.size() / 2);

        MyScrollView weekViewHoursScrollview = rootView.findViewById(R.id.week_view_hours_scrollview);
        weekViewHoursScrollview.setOnScrollviewListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                mWeekScrollY = y;
                weeklyAdapter.updateScrollY(weekViewViewPager.getCurrentItem(), y);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).showToolbar();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) requireActivity()).hideToolbar();
    }

    private void fillTimeLatter() {
        ViewGroup weekViewHoursHolder = rootView.findViewById(R.id.week_view_hours_holder);
        weekViewHoursHolder.removeAllViews();
        for (int i = 1; i <= 23; i++) {
            TextView view = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.item_weekly_view_hour_textview, null, false);
            String value = String.valueOf(i);
            view.setText(value.length() == 2 ? value : "0" + value);
            if (DateTime.now().getHourOfDay() == i) {
                view.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent));
                view.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }
            weekViewHoursHolder.addView(view);
        }
    }

    private List<Long> getWeekTimestamps() {
        int dayInCalendar = config.getDayInCalendar();
        DateTime dateTime = (dayInCalendar == 7) ? DateTime.now().withDayOfWeek(1).withTime(0, 0, 0, 0) : DateTime.now().withTime(0, 0, 0, 0);
        DateTime currWeek = (dayInCalendar == 7) ? dateTime.minusWeeks(PREFILLED_WEEKS / 2) : dateTime.minusDays(PREFILLED_WEEKS * dayInCalendar / 2 - (dayInCalendar / 2));

        List<Long> weekTSs = new ArrayList<>(PREFILLED_WEEKS);
        for (int i = 0; i < PREFILLED_WEEKS; i++) {
            weekTSs.add(DateTimeExtensions.seconds(currWeek));
            currWeek = currWeek.plusDays(dayInCalendar);
        }
        return weekTSs;
    }

    private void setupActionbarTitle(long timestamp) {
        DateTime startDateTime = FormatterUtils.getDateTimeFromTS(timestamp);
        DateTime endDateTime = FormatterUtils.getDateTimeFromTS(timestamp + WEEK_SECONDS);
        String startMonthName = FormatterUtils.getMonthName(requireContext(), startDateTime.getMonthOfYear());

        if (startDateTime.getMonthOfYear() == endDateTime.getMonthOfYear()) {
            String newTitle = startMonthName;
            if (startDateTime.getYear() != DateTime.now().getYear()) {
                newTitle += " - " + startDateTime.getYear();
            }
            requireActivity().setTitle(newTitle);
        } else {
            String endMonthName = FormatterUtils.getMonthName(requireContext(), endDateTime.getMonthOfYear());
            requireActivity().setTitle(startMonthName + " - " + endMonthName);
        }
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setSubtitle(String.format(getString(R.string.week), startDateTime.plusDays(3).getWeekOfWeekyear()));
        } else {
            Log.e("PersonalEventFragment", "ActionBar is null");
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.main_add_event) {
            Intent intent = new Intent(requireContext(), AddNewEventActivity.class);
            intent.putExtra(EVENT_ID, -1L);
            startActivity(intent);
            return true;
            // 其他菜单选项的处理...
        }
        return super.onOptionsItemSelected(item);
    }
}
