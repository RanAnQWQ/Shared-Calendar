package com.app.login.groupEvent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;

import androidx.appcompat.app.AppCompatActivity;

import com.app.login.R;
import com.app.login.dao.GroupEventParticipantDAO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class InputPreferredTimeActivity extends AppCompatActivity {
    private int eventId,userId;
    private String startDate,endDate;

    private TextView tvSelectedTimeRange;
    private Calendar startCalendar, endCalendar;
    private SimpleDateFormat dateFormat, timeFormat;
    private ListView lvDateTime;
    private ArrayAdapter<String> adapter;
    private List<String> dateTimeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_preferred_time);

        eventId = getIntent().getIntExtra("eventId", -1);
        Log.e("InputPreferredTimeActivity", "eventId: " + eventId);
        userId=getIntent().getIntExtra("userId",-1);
        startDate=getIntent().getStringExtra("startDate");
        endDate=getIntent().getStringExtra("endDate");
        Button btnSubmit = findViewById(R.id.btn_submit);
        Button btnChooseTime = findViewById(R.id.btn_choose_time);
        Button btnChooseTimeEnd = findViewById(R.id.btn_choose_time_end);

        // 初始化视图
        lvDateTime = findViewById(R.id.lvDateTime);
        adapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,dateTimeList);
        lvDateTime.setAdapter(adapter);

        // 初始化日期和时间格式
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // 初始化日历对象
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        // 设置按钮点击事件
        btnChooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker(true);
            }
        });
        // 设置按钮点击事件
        btnChooseTimeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker(false);
            }
        });


        btnSubmit.setOnClickListener(v -> {
            String preferredTimeSlots = getSelectedTimeSlots();
            String perferredDateTimeList = getperferredDateTime();
            // 异步提交数据库更新
            new Thread(() -> {
                GroupEventParticipantDAO participantDAO = new GroupEventParticipantDAO();
                participantDAO.updatePreferredTimeSlots(userId, eventId,preferredTimeSlots,perferredDateTimeList);
                runOnUiThread(() -> {
                    // 操作完成后的 UI 更新（如果需要）
                    Log.i("InputPreferredTimeActivity", "Preferred time slots updated successfully.");
                    finish();
                });
            }).start();
        });


    }

    public String getperferredDateTime(){
        JSONArray perferredDateTimeJson = new JSONArray();
        dateTimeList.forEach(
                item ->{
                    perferredDateTimeJson.put(item);
                }
        );
        return  perferredDateTimeJson.toString();
    }

    public String getSelectedTimeSlots() {
        JSONArray selectedTimeSlotsArray = new JSONArray();

        // 获取复选框控件
        CheckBox checkBoxMorning = findViewById(R.id.checkbox_morning);
        CheckBox checkBoxNoon = findViewById(R.id.checkbox_noon);
        CheckBox checkBoxAfternoon = findViewById(R.id.checkbox_afternoon);
        CheckBox checkBoxEvening = findViewById(R.id.checkbox_evening);
        CheckBox checkBoxNight = findViewById(R.id.checkbox_night);

        // 检查每个复选框的选中状态，并将选中的时间段添加到结果中
        if (checkBoxMorning.isChecked()) {
            selectedTimeSlotsArray.put("morning");
        }
        if (checkBoxNoon.isChecked()) {
            selectedTimeSlotsArray.put("noon");
        }
        if (checkBoxAfternoon.isChecked()) {
            selectedTimeSlotsArray.put("afternoon");
        }
        if (checkBoxEvening.isChecked()) {
            selectedTimeSlotsArray.put("evening");
        }
        if (checkBoxNight.isChecked()) {
            selectedTimeSlotsArray.put("night");
        }

        // 返回 JSON 格式的字符串
        return selectedTimeSlotsArray.toString();
    }

    // 显示日期时间选择器
    private void showDateTimePicker(final boolean isStartTime) {
        final Calendar currentCalendar = isStartTime ? startCalendar : endCalendar;

        // 创建日期选择器
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        currentCalendar.set(Calendar.YEAR, year);
                        currentCalendar.set(Calendar.MONTH, month);
                        currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // 显示时间选择器
                        new TimePickerDialog(InputPreferredTimeActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                currentCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                currentCalendar.set(Calendar.MINUTE, minute);
                                // 如果是选择结束时间，才调用 updateTimeRange 方法
                                if (!isStartTime) {
                                    updateTimeRange();
                                }

                            }
                        }, currentCalendar.get(Calendar.HOUR_OF_DAY), currentCalendar.get(Calendar.MINUTE), true).show();
                    }
                },
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // 设置日期范围（2024-11-17)
        // 截取下日期 startDate
        String[] startDateList = startDate.split("-");
        String[] endDateList = endDate.split("-");
        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.YEAR, Integer.parseInt(startDateList[0]));
        minDate.set(Calendar.MONTH, Integer.parseInt(startDateList[1])-1); // 12月
        minDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(startDateList[2])); // 1日

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(Calendar.YEAR, Integer.parseInt(endDateList[0]));
        maxDate.set(Calendar.MONTH, Integer.parseInt(endDateList[1])-1);
        maxDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(endDateList[2]));

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        // 显示日期选择器
        datePickerDialog.show();
    }

    // 更新显示的时间段
    private void updateTimeRange() {
        String startTime = dateFormat.format(startCalendar.getTime()) + " " + timeFormat.format(startCalendar.getTime());
        String endTime = dateFormat.format(endCalendar.getTime()) + " " + timeFormat.format(endCalendar.getTime());

        // 判断开始时间是否小于结束时间
        if (startCalendar.before(endCalendar)) {
            dateTimeList.add(startTime + " - " + endTime);
            adapter.notifyDataSetChanged();

            // 重置时间，供下一次使用
            resetTime();
        } else {
            // 如果开始时间不小于结束时间，显示错误提示
            Toast.makeText(this, "开始时间必须小于结束时间", Toast.LENGTH_SHORT).show();
        }
    }

    // 重置时间，供下一次使用
    private void resetTime() {
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
    }
}
