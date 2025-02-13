package com.app.login.groupEvent;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.login.R;
import com.app.login.dao.GroupEventDAO;
import com.app.login.dao.GroupEventParticipantDAO;
import com.app.login.dao.UserDao;
import com.app.login.entity.GroupEvent;
import com.app.login.entity.GroupEventParticipant;
import com.app.login.entity.User;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.List;


public class CreateGroupEventActivity extends AppCompatActivity {
    private static final String TAG = "CreateGroupEventActivity";
    private Button createEventButton;
    private List<User> selectedFriends;
    private GroupEventFriendAdapter friendAdapter;
    private RecyclerView friendsRecyclerView;
    private LinearLayout dateStHolder, dateEndHolder;
    private TextView tvStartDate, tvEndDate;
    private String tentativeStartDate, tentativeEndDate;
    // 添加标题和描述字段
    private EditText titleEditText;
    private EditText descriptionEditText;
    private TextView titleTextView;
    private TextView descriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_event); // 确保布局已经加载

        // 获取传递的朋友列表
        selectedFriends = (List<User>) getIntent().getSerializableExtra("selected_friends");

        // 初始化视图元素
        titleTextView = findViewById(R.id.tv_st_title); // 假设布局中的 TextView 用于显示标题
        tvStartDate = findViewById(R.id.tv_start_date); // 用于显示开始日期
        tvEndDate = findViewById(R.id.tv_end_date); // 用于显示结束日期
        dateStHolder = findViewById(R.id.date_st_holder); // 开始日期选择器
        dateEndHolder = findViewById(R.id.date_end_holder); // 结束日期选择器

        DateTime currentDate = new DateTime();
        String currentDateString = currentDate.toString("yyyy-MM-dd");
        tvStartDate.setText(currentDateString); // 显示当前日期
        tvEndDate.setText(currentDateString);   // 默认结束日期也设置为当前日期
        // 设置日期选择器的点击事件
        dateStHolder.setOnClickListener(v -> {
            hideKeyboard();
            showDatePicker(new DateTime(), 0); // 显示日期选择器用于选择开始日期
        });

        dateEndHolder.setOnClickListener(v -> {
            hideKeyboard();
            showDatePicker(new DateTime(), 1); // 显示日期选择器用于选择结束日期
        });
        // 初始化 RecyclerView
        friendsRecyclerView = findViewById(R.id.friends_recycler_view); // 在 XML 布局中定义的 RecyclerView
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // 设置 LinearLayoutManager
        // 设置适配器
        friendAdapter = new GroupEventFriendAdapter(this, selectedFriends);
        friendsRecyclerView.setAdapter(friendAdapter);

        createEventButton = findViewById(R.id.create_event_button);
        createEventButton.setOnClickListener(v -> {
            // 获取标题和描述文本
            String title = ((EditText) findViewById(R.id.ed_title)).getText().toString();
            String description = ((EditText) findViewById(R.id.ed_des)).getText().toString();
            int creatorId = getCurrentUserId();
            Log.e("CreateGrou...ntActivity22222222222",""+creatorId);

            // 确保开始和结束日期已选择
            if (tentativeStartDate != null && tentativeEndDate != null) {

                createGroupEvent(title, description, creatorId, tentativeStartDate, tentativeEndDate, selectedFriends);
            } else {
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
            }
        });


    }

    // 显示日期选择器
    private void showDatePicker(final DateTime dateTime, final int sdType) {
        DatePickerDialog picker = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    // 根据选择的日期更新开始或结束日期
                    DateTime selectedDate = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0); // 选择的日期
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = sdf.format(selectedDate.toDate());

                    if (sdType == 0) {
                        tentativeStartDate = formattedDate; // 设置开始日期
                        tvStartDate.setText(formattedDate); // 显示选择的开始日期
                    } else {
                        tentativeEndDate = formattedDate; // 设置结束日期
                        tvEndDate.setText(formattedDate); // 显示选择的结束日期
                    }
                },
                dateTime.getYear(),
                dateTime.getMonthOfYear() - 1,
                dateTime.getDayOfMonth());

        picker.show();
        picker.setOnCancelListener(dialog -> {});
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // 创建群体事件并返回结果
    private void createGroupEvent(String title, String description, int creatorId, String startDate, String endDate, List<User> selectedParticipants) {
        new Thread(() -> {
            // 创建群体事件并存储到数据库
            GroupEventDAO groupEventDAO = new GroupEventDAO();
            GroupEvent groupEvent = new GroupEvent();
            groupEvent.setTitle(title);
            groupEvent.setDescription(description);
            groupEvent.setCreatorId(creatorId);
            groupEvent.setTentativeStartDate(startDate);  // 设置开始日期
            groupEvent.setTentativeEndDate(endDate);      // 设置结束日期
            groupEvent.setStatus("PENDING");     // 设置状态为 PENDING

            // Log the data being inserted
            Log.d(TAG, "Creating group event with details:");
            Log.d(TAG, "Title: " + title);
            Log.d(TAG, "Description: " + description);
            Log.d(TAG, "Creator ID: " + creatorId);
            Log.d(TAG, "Start Date: " + startDate);
            Log.d(TAG, "End Date: " + endDate);
            Log.d(TAG, "Status: " + groupEvent.getStatus());

            // 保存事件并获取事件ID
            groupEventDAO.createGroupEvent(groupEvent);

            // 获取创建的群体事件ID
            int createdEventId = groupEvent.getId();
            Log.d(TAG, "Group event created with ID: " + createdEventId);

            // 为每个选中的参与者添加记录到 GroupEventParticipant 表中
            GroupEventParticipantDAO participantDAO = new GroupEventParticipantDAO();

            // 插入创建者作为参与者
            GroupEventParticipant creatorParticipant = new GroupEventParticipant();
            creatorParticipant.setGroupEventId(createdEventId);
            creatorParticipant.setUserId(creatorId);  // 设置创建者的 ID
            creatorParticipant.setPreferredTimeSlots("[]");  // 默认没有选择期望时间段
            creatorParticipant.setIsTimeSlotSubmitted(false);  // 默认没有提交时间段
            creatorParticipant.setVotedTimeSlot(null);  // 默认没有投票时间段

            // Log the creator participant data
            Log.d(TAG, "Creating participant for creator with ID: " + creatorId);
            Log.d(TAG, "Preferred Time Slots: " + creatorParticipant.getPreferredTimeSlots());
            Log.d(TAG, "Time Slot Submitted: " + creatorParticipant.getIsTimeSlotSubmitted());

            participantDAO.createParticipant(creatorParticipant);  // 保存创建者作为参与者

            // 插入其他选择的朋友作为参与者
            for (User user : selectedParticipants) {
                GroupEventParticipant participant = new GroupEventParticipant();
                participant.setGroupEventId(createdEventId);
                participant.setUserId(user.getId());
                participant.setPreferredTimeSlots("[]");  // 默认没有选择期望时间段
                participant.setIsTimeSlotSubmitted(false);  // 默认没有提交时间段
                participant.setVotedTimeSlot(null);  // 默认没有投票时间段

                // Log the participant data
                Log.d(TAG, "Creating participant for user with ID: " + user.getId());
                Log.d(TAG, "Preferred Time Slots: " + participant.getPreferredTimeSlots());
                Log.d(TAG, "Time Slot Submitted: " + participant.getIsTimeSlotSubmitted());

                participantDAO.createParticipant(participant);  // 保存参与者到数据库
            }

            // 回到主线程，通知群体事件创建完成
            runOnUiThread(() -> {
                Log.d(TAG, "Group event creation completed, notifying UI.");
                Intent resultIntent = new Intent();
                resultIntent.putExtra("group_event_created", true);  // 返回群体事件创建的结果
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }).start();  // 启动子线程
    }

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

}
