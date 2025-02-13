package com.app.login.groupEvent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.app.login.Config;
import com.app.login.R;
import com.app.login.dao.EventDAO;
import com.app.login.dao.GroupEventDAO;
import com.app.login.dao.GroupEventParticipantDAO;
import com.app.login.dao.UserDao;
import com.app.login.entity.Event;
import com.app.login.entity.GroupEvent;
import com.app.login.entity.GroupEventParticipant;
import com.app.login.entity.User;
import com.app.login.extensions.ContextExtensions;
import com.app.login.extensions.DateTimeExtensions;
import com.app.login.view.MyScrollView;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GroupEventDetailsActivity extends AppCompatActivity {
    private int eventId;
    private TextView availableTimesTextView;
    private Button btnInputPreferredTimes, btnVote;
    private String startDate, endDate,title,description;
    private static final int PREFILLED_WEEKS = 53;
    private Config config;
    public static int mWeekScrollY = 0;
    private TextView startDateTextView, endDateTextView, titleTextView,descripTextView;
    private MyScrollView weekViewHoursScrollview;
    private View weekViewHoursHolder;
    private GroupEventDAO groupEventDAO=new GroupEventDAO();
    private GroupEventParticipantDAO groupEventParticipantDAO=new GroupEventParticipantDAO();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_event_details);
        // Find views using findViewById

        weekViewHoursScrollview = findViewById(R.id.week_view_hours_scrollview);
        //weekViewViewPager = findViewById(R.id.weekViewViewPager);
        weekViewHoursHolder = findViewById(R.id.week_view_hours_holder);
        // Find views using findViewById
        startDateTextView = findViewById(R.id.start_date_text);
        endDateTextView = findViewById(R.id.end_date_text);
        titleTextView=findViewById(R.id.event_title);
        descripTextView=findViewById(R.id.event_description);
        config = ContextExtensions.getConfig(this.getApplicationContext());

        mWeekScrollY = (int) (getResources().getDimension(R.dimen.weekly_view_row_height) * new DateTime().getHourOfDay());  // Move to current time or default work start time

        btnInputPreferredTimes = findViewById(R.id.btn_input_preferred_times);

        btnVote = findViewById(R.id.btn_vote);
        eventId = getIntent().getIntExtra("eventId", -1);
        Log.e("GroupEventDetailsActivity","eventId: "+eventId);
        startDate = getIntent().getStringExtra("startDate");  // 获取 startDate
        endDate = getIntent().getStringExtra("endDate");  // 获取 endDate
        Log.d("GroupEventDetailsActivity", "startDate: " + startDate + ", endDate: " + endDate);
        title=getIntent().getStringExtra("title");
        description=getIntent().getStringExtra("description");
        // Handle button clicks
        btnInputPreferredTimes.setOnClickListener(v -> {
            int userId = getCurrentUserId();
            Intent intent = new Intent(GroupEventDetailsActivity.this, InputPreferredTimeActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("userId",userId);
            intent.putExtra("startDate",startDate);
            intent.putExtra("endDate",endDate);
            startActivity(intent);
        });

        btnVote.setOnClickListener(v -> showVotingDialog());
        startDateTextView.setText(startDate);
        endDateTextView.setText(endDate);
        titleTextView.setText("Group Event Title: "+title);
        descripTextView.setText("Description: "+description);
        fillWeeklyViewPager();  // Fill the view pager
        fillTimeLatter();
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
    private void fillTimeLatter() {
        ViewGroup weekViewHoursHolder = findViewById(R.id.week_view_hours_holder);
        weekViewHoursHolder.removeAllViews();
        for (int i = 1; i <= 23; i++) {
            TextView view = (TextView) LayoutInflater.from(this).inflate(R.layout.item_weekly_view_hour_textview, null, false);
            String value = String.valueOf(i);
            view.setText(value.length() == 2 ? value : "0" + value);
            if (DateTime.now().getHourOfDay() == i) {
                view.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                view.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }
            weekViewHoursHolder.addView(view);
        }
    }
    private void fillWeeklyViewPager() {
        List<Long> weekTSs = getWeekTimestamps();

        // 从 intent 获取群组事件的开始和结束时间戳
        long groupEventStartTimestamp = DateTimeExtensions.seconds(DateTime.parse(startDate));
        Log.e("fillWeeklyViewPager111","groupEventStartTimestamp:"+groupEventStartTimestamp);


        long groupEventEndTimestamp = DateTimeExtensions.seconds(DateTime.parse(endDate).withTime(23,59,59,999));
        Log.e("fillWeeklyViewPager222","groupEventEndTimestamp:"+groupEventEndTimestamp);
        // 创建 GroupPageAdapter 并传入必要的参数
        GroupPageAdapter groupPageAdapter = new GroupPageAdapter(
                getSupportFragmentManager(),
                weekTSs,
                groupEventStartTimestamp,
                groupEventEndTimestamp,
                eventId,new GroupViewFragment.WeekScrollListener() {
                    @Override
                    public void scrollTo(int y) {
                        MyScrollView weekViewHoursScrollview = findViewById(R.id.week_view_hours_scrollview);
                        weekViewHoursScrollview.scrollTo(0, y);
                        mWeekScrollY = y;
                    }
                });

        ViewPager weekViewViewPager = findViewById(R.id.weekViewViewPager);
        // 设置适配器到 ViewPager
        weekViewViewPager.setAdapter(groupPageAdapter);

        // 添加页面切换监听器
        weekViewViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                // 设置标题或进行其他操作
                // setupActionbarTitle(weekTSs.get(position));
            }
        });

        // 设置当前页面为中间位置
        weekViewViewPager.setCurrentItem(weekTSs.size() / 2);

        // 添加滚动事件监听器以同步滚动
        MyScrollView weekViewHoursScrollview = findViewById(R.id.week_view_hours_scrollview);
        weekViewHoursScrollview.setOnScrollviewListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                mWeekScrollY = y;
                groupPageAdapter.updateScrollY(weekViewViewPager.getCurrentItem(), y);
            }
        });
    }

    private void showVotingDialog() {
        // 获取 SharedPreferences 中的 "AppPreferences"
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);

        // 获取 "FREE_TIME_RANGES" 中保存的数据，假设它是一个 JSON 字符串集合
        Set<String> jsonRanges = sharedPreferences.getStringSet("FREE_TIME_RANGES", new HashSet<>());

        // 将 JSON 字符串解析回 LongRange 列表，并生成时间段的字符串数组
        List<String> availableTimeSlotsList = new ArrayList<>();
        for (String jsonString : jsonRanges) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                long start = jsonArray.getLong(0);
                long end = jsonArray.getLong(1);
                String formattedTimeSlot = formatTimestamp(start) + " - " + formatTimestamp(end);
                availableTimeSlotsList.add(formattedTimeSlot);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 将 availableTimeSlotsList 按照开始时间排序
        Collections.sort(availableTimeSlotsList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                try {
                    // 从时间段中解析开始时间
                    long start1 = parseTimestamp(o1.split(" - ")[0]);
                    long start2 = parseTimestamp(o2.split(" - ")[0]);
                    return Long.compare(start1, start2);
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        // 将列表转换为字符串数组
        String[] availableTimeSlots = availableTimeSlotsList.toArray(new String[0]);

        // 用于存储用户选择的索引
        final boolean[] selectedIndices = new boolean[availableTimeSlots.length];

        // 创建并显示对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Vote for the time slots")
                .setMultiChoiceItems(availableTimeSlots, selectedIndices, (dialog, which, isChecked) -> {
                    // 更新用户选择的索引
                    selectedIndices[which] = isChecked;
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    // 处理用户选择的时间段
                    List<String> selectedTimeSlotList = new ArrayList<>();
                    for (int i = 0; i < selectedIndices.length; i++) {
                        if (selectedIndices[i]) {
                            selectedTimeSlotList.add(availableTimeSlots[i]);
                        }
                    }

                    // 异步提交用户选择的时间段
                    submitVoteAsyncList(selectedTimeSlotList);
                    updateEventStatus(eventId);
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    private void showVotingDialog2() {
        // 获取 SharedPreferences 中的 "AppPreferences"
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);

        // 获取 "FREE_TIME_RANGES" 中保存的数据，假设它是一个 JSON 字符串集合
        Set<String> jsonRanges = sharedPreferences.getStringSet("FREE_TIME_RANGES", new HashSet<>());

        // 将 JSON 字符串解析回 LongRange 列表，并生成时间段的字符串数组
        List<String> availableTimeSlotsList = new ArrayList<>();
        for (String jsonString : jsonRanges) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                long start = jsonArray.getLong(0);
                long end = jsonArray.getLong(1);
                String formattedTimeSlot = formatTimestamp(start) + " - " + formatTimestamp(end);
                availableTimeSlotsList.add(formattedTimeSlot);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 将 availableTimeSlotsList 按照开始时间排序
        Collections.sort(availableTimeSlotsList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                try {
                    // 从时间段中解析开始时间
                    long start1 = parseTimestamp(o1.split(" - ")[0]);
                    long start2 = parseTimestamp(o2.split(" - ")[0]);
                    return Long.compare(start1, start2);
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        // 将列表转换为字符串数组
        String[] availableTimeSlots = availableTimeSlotsList.toArray(new String[0]);

        // 创建并显示对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Vote for the time slot")
                .setItems(availableTimeSlots, (dialog, which) -> {
                    // 处理用户选择的时间段
                    String selectedTimeSlot = availableTimeSlots[which];
                    // 异步提交用户选择的时间段
                    submitVoteAsync(selectedTimeSlot);
                    updateEventStatus(eventId);
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // 格式化时间戳的方法，假设格式为 "yyyy-MM-dd HH:mm"
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
    // 解析时间戳的方法
    private long parseTimestamp(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = sdf.parse(timestamp);  // 将字符串解析为 Date 对象
            return date != null ? date.getTime() : 0;  // 获取对应的时间戳（毫秒）
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;  // 如果解析失败，返回 0
        }
    }


    private void submitVoteAsync(String selectedTimeSlot) {
        Log.e("submitVoteAsync", "11111111111 " + selectedTimeSlot);

        new Thread(() -> {
            GroupEventParticipantDAO participantDAO = new GroupEventParticipantDAO();

            // 获取当前用户ID（从你的会话管理或用户身份验证获取）
            int userId = getCurrentUserId();

            // 检查是否所有参与者都已提交了时间段
            boolean allTimeSlotsSubmitted = participantDAO.canVote(eventId);
            // 如果所有参与者都已提交时间段，则可以提交投票
            if (allTimeSlotsSubmitted) {
                // 提交投票到数据库
                participantDAO.submitVoteForTimeSlot(userId, selectedTimeSlot, eventId);
                updateEventStatus(eventId);
                // 在主线程更新UI，显示投票成功的消息
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(GroupEventDetailsActivity.this, "Vote submitted for " + selectedTimeSlot, Toast.LENGTH_SHORT).show();
                });
            } else {
                // 如果未所有参与者都提交时间段，提示用户
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(GroupEventDetailsActivity.this, "Please wait for all participants to submit their time slots.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    private void submitVoteAsyncList(List selectedTimeSlotList) {
        Log.e("submitVoteAsync", "11111111111 " + selectedTimeSlotList);

        new Thread(() -> {
            GroupEventParticipantDAO participantDAO = new GroupEventParticipantDAO();

            // 获取当前用户ID（从你的会话管理或用户身份验证获取）
            int userId = getCurrentUserId();

            // 检查是否所有参与者都已提交了时间段
            boolean allTimeSlotsSubmitted = participantDAO.canVote(eventId);
            //allTimeSlotsSubmitted= true;
            // 如果所有参与者都已提交时间段，则可以提交投票
            if (allTimeSlotsSubmitted) {
                // 提交投票到数据库
                JSONArray jsonArray = new JSONArray();
                selectedTimeSlotList.forEach(item->{
                    jsonArray.put(item);
                });

                participantDAO.submitVoteForTimeSlotList(userId, jsonArray.toString(), eventId);

                // 在主线程更新UI，显示投票成功的消息
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(GroupEventDetailsActivity.this, "Vote submitted for " + selectedTimeSlotList, Toast.LENGTH_SHORT).show();
                });
            } else {
                // 如果未所有参与者都提交时间段，提示用户
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(GroupEventDetailsActivity.this, "Please wait for all participants to submit their time slots.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    //求出空闲时间方法
    private int getCurrentUserId() {
        String accountName = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .getString("currentUserId", null);
        Log.e("CreateGroupEventActivity", "currentUserId" + accountName);
        if (accountName == null) {
            throw new IllegalStateException("Account name is not set in SharedPreferences");
        }

        final int[] userId = new int[1];  // 用于存储用户ID

        // 使用Thread来进行异步操作
        Thread thread = new Thread(() -> {
            UserDao dao = new UserDao();
            User user = dao.findUser(accountName);
            if (user != null) {
                userId[0] = user.getId();  // 获取userId并存储
            } else {
                runOnUiThread(() -> Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show());
                userId[0] = 0;  // 如果没有找到用户，返回0
            }
        });

        thread.start();  // 启动线程

        try {
            thread.join();  // 等待线程执行完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return userId[0];  // 返回获取到的userId
    }
    public void onResume() {
        super.onResume();
        fillWeeklyViewPager();  // Fill the view pager
        fillTimeLatter();
    }

    /**
     * 异步任务，用于检查和更新事件的状态
     */
    private void updateEventStatus(final int eventId) {
        // 使用 ExecutorService 提交异步任务，避免在主线程中执行耗时操作
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                // 在后台线程中执行数据库查询操作
                GroupEvent event = groupEventDAO.getGroupEventById(eventId);

                // 执行更新事件状态的操作
                if (event != null) {
                    executorService.submit(new UpdateEventStatusRunnable(event));
                } else {
                    // 处理 event 为 null 的情况，例如记录错误或通知用户
                    Log.e("updateEventStatus", "Event not found for ID: " + eventId);
                }
            }
        });
    }


    private class UpdateEventStatusRunnable implements Runnable {
        private GroupEvent event;

        public UpdateEventStatusRunnable(GroupEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            try {
                if(!event.getStatus().equals("COMPLETED")){
                    boolean allPreferredTimeSelected = groupEventParticipantDAO.canVote(event.getId());
                    if (allPreferredTimeSelected && event.getStatus().equals("ONGOING")) {
                        // 检查是否所有参与者都完成了投票
                        boolean allVoted = groupEventParticipantDAO.allParticipantsVoted(event.getId());
                        if (allVoted) {
                            // 更新事件状态为 COMPLETED
                            groupEventDAO.updateEventStatus(event.getId(), "COMPLETED");
                            event.setStatus("COMPLETED");
                            //TODO 获得投票最多的日期
                            //TODO 确立最终时间
                            // 获取投票最多的时间段
                            List<GroupEventParticipant> participants = groupEventParticipantDAO.getParticipantsByEventId(event.getId());
                            //String mostVotedTimeSlot = groupEventDAO.getMostVotedTimeSlot(participants);
                            String mostVotedTimeSlot = groupEventDAO.getMostVotedTimeSlotByList(participants);

                            // 设定最终时间段
                            event.setFinalStartDate(mostVotedTimeSlot.split(" - ")[0].trim());
                            event.setFinalEndDate(mostVotedTimeSlot.split(" - ")[1].trim());

                            // 为每个参与者创建个人事件
                            String[] timeSlot = mostVotedTimeSlot.split(" - ");
                            String startTime = timeSlot[0].trim();
                            String endTime = timeSlot[1].trim();
                            EventDAO eventDAO=new EventDAO();

                            long startTimeMillis = convertTimeStringToMillis(startTime);
                            long endTimeMillis = convertTimeStringToMillis(endTime);
                            String title= event.getTitle();
                            String des= event.getDescription();
                            Event event1=new Event(startTimeMillis,endTimeMillis);
                            event1.setTitle(title);
                            event1.setDescription(des);

                            for (GroupEventParticipant participant : participants) {
                                // 创建个人事件并保存到数据库
                                int userID=participant.getUserId();
                                event1.setUserId(userID);
                                eventDAO.addEvent(event1);
                            }
                        }
                    }
                    // 如果未更新，则保持当前状态
                    updateUI(event.getStatus());
                }
                // 检查是否所有参与者都提交了期望时间段

            } catch (Exception e) {
                Log.e("GroupEventDetailsActivity", "Error updating event status for event ID: " + event.getId(), e);
                updateUI(event.getStatus());
            }
        }

        private void updateUI(String status) {
            // 需要在主线程中更新 UI
            runOnUiThread(() -> {
                // 更新UI代码，比如显示状态
                Toast.makeText(GroupEventDetailsActivity.this, "Event status updated: " + status, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 确保线程池资源得到释放
        executorService.shutdown();
    }

    private long convertTimeStringToMillis(String timeString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date date = dateFormat.parse(timeString);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            Log.e("GroupEventDetailsActivity", "Error parsing time string: " + timeString, e);
            return 0;
        }
    }

}
