package com.app.login.personalEvent;

import static com.app.login.ConstantsKt.EVENT_ID;
import static com.app.login.ConstantsKt.WEEK_START_TIMESTAMP;
import static com.app.login.util.FormatterUtils.DAYCODE_PATTERN;
import static com.app.login.util.FormatterUtils.TIME_24_PATTERN;
import android.content.Context;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.app.login.R;
import com.app.login.dao.EventDAO;
import com.app.login.dao.UserDao;
import com.app.login.entity.Event;
import com.app.login.entity.User;
import com.app.login.util.FormatterUtils;
import org.joda.time.DateTime;

import java.util.concurrent.Executors;

public class AddNewEventActivity extends AppCompatActivity {
    private EventDAO eventDAO;
    private long eventId;
    private Event currentEvent;
    private AddEventViewModel mViewModel;
    private EditText edTitle, edDes;
    private TextView tvStartDate, tvEndDate,tvStartTime,tvEndTime;
    private LinearLayout dateStHolder, dateEndHolder;
    private Switch swIsPersonal; // 新增的 Switch 变量
    private Event currEvent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_event);
        // 假设 EventDAO 是你与 MySQL 通信的类
        EventDAO dataSource = new EventDAO(); // 这里可以传入其他参数，如 DatabaseHelper

        mViewModel = new ViewModelProvider(this, new AddEventViewModelFactory(getApplication(), dataSource))
                .get(AddEventViewModel.class);
        // 使用 findViewById 方法获取视图
        edTitle = findViewById(R.id.ed_title);
        edDes = findViewById(R.id.ed_des);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
        tvStartTime=findViewById(R.id.tv_start_time);
        tvEndTime=findViewById(R.id.tv_end_time);
        dateStHolder = findViewById(R.id.date_st_holder); // 确保这个 ID 是正确的
        dateEndHolder = findViewById(R.id.date_end_holder); // 确保这个 ID 是正确的
        swIsPersonal = findViewById(R.id.sw_is_personal); // 获取 Switch 视图
        Intent intent = getIntent();
        if (intent == null) return;
        eventDAO = new EventDAO();
        eventId = intent.getIntExtra(EVENT_ID, -1);
        Log.e("addNewEventActivity","55555555555555555555555555"+eventId);
        if (eventId != -1) {
            Log.e("loadEvent","55555555555555555555555555"+eventId);
            loadEvent(eventId);
        }
        long weekTimestamp = intent.getLongExtra(WEEK_START_TIMESTAMP, -1L);
        Log.e("weekTimestamp","weekTimestamp:"+weekTimestamp);
        mViewModel.initializeCurrEvent(eventId, weekTimestamp, event -> {
            runOnUiThread(() -> {
                DateTime startDateTime = new DateTime(System.currentTimeMillis());
                DateTime endDateTime = new DateTime(startDateTime);
                edTitle.setText("");
                edDes.setText("");
                // 事件标记
                swIsPersonal.setSelected(false);
                // 时间
                tvStartDate.setText(startDateTime.toString(DAYCODE_PATTERN));
                tvEndDate.setText(endDateTime.toString(DAYCODE_PATTERN));
                tvStartTime.setText(startDateTime.toString(TIME_24_PATTERN));
                tvEndTime.setText(endDateTime.toString(TIME_24_PATTERN));
            });
        });
        // 设置日期按钮赋值监听
        dateStHolder.setOnClickListener(v -> {
            hideKeyboard();
            dateStHolder.setClickable(false);
            showDatePicker(new DateTime(mViewModel.currEvent.startTimeMilli), 0);
            dateStHolder.setClickable(true); // 这里要在 showDatePicker 之后恢复点击状态
        });


        dateEndHolder.setOnClickListener(v -> {
            hideKeyboard();
            dateEndHolder.setClickable(false);
            showDatePicker(new DateTime(mViewModel.currEvent.endTimeMilli), 1);
            dateEndHolder.setClickable(true); // 这里要在 showDatePicker 之后恢复点击状态
        });
        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> {
            saveEvent(); // 保存事件
        });

    }

    private void loadEvent(long eventId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentEvent = eventDAO.getEventFromDatabase(eventId);
            Log.e("currentEvent","55555555555555555555555555"+currentEvent.getTitle());
            runOnUiThread(() -> {
                if (currentEvent != null) {
                    edTitle.setText(currentEvent.getTitle());
                    edDes.setText(currentEvent.getDescription());
                    tvStartTime.setText(FormatterUtils.getTimeString_24(new DateTime(currentEvent.getStartTimeMilli())));
                    tvEndTime.setText(FormatterUtils.getTimeString_24(new DateTime(currentEvent.getEndTimeMilli())));
                    // 使用 DATE_PATTERN 格式化日期
                    tvStartDate.setText(new DateTime(currentEvent.getStartTimeMilli()).toString(FormatterUtils.DATE_PATTERN));
                    tvEndDate.setText(new DateTime(currentEvent.getEndTimeMilli()).toString(FormatterUtils.DATE_PATTERN));
                    swIsPersonal.setChecked(currentEvent.isEventIsPersonal()); // 设定 Switch 状态
                } else {
                    Toast.makeText(this, "无法加载事件", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showDatePicker(final DateTime dateTime, final int sdType) {
        DatePickerDialog picker = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    mViewModel.resetDate(year, monthOfYear + 1, dayOfMonth, sdType, new AddEventViewModel.DateTimeCallback() {
                        @Override
                        public void onDateTimeReset(DateTime stDateTime, DateTime edDateTime, boolean isRested) {
                            // 确保主线程更新
                            runOnUiThread(() -> {
                                TextView tvStartDate = findViewById(R.id.tv_start_date);
                                TextView tvEndDate = findViewById(R.id.tv_end_date);
                                tvStartDate.setText(stDateTime.toString(DAYCODE_PATTERN));
                                tvEndDate.setText(edDateTime.toString(DAYCODE_PATTERN));
                                mViewModel.currEvent.startTimeMilli = stDateTime.getMillis();
                                mViewModel.currEvent.endTimeMilli = edDateTime.getMillis();
                                DateTime sendDateTime = (sdType == 0) ? stDateTime : edDateTime;
                                showTimePicker(sendDateTime, sdType);
                            });
                        }
                    });
                    Log.e("date picker", "111111111111111111");
                },
                dateTime.getYear(),
                dateTime.getMonthOfYear() - 1,
                dateTime.getDayOfMonth());

        picker.show();
        picker.setOnCancelListener(dialog -> {});
    }

    private void showTimePicker(final DateTime dateTime, final int sdType) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    mViewModel.resetTime(hourOfDay, minute, sdType, new AddEventViewModel.DateTimeCallback() {
                        @Override
                        public void onDateTimeReset(DateTime stDateTime, DateTime edDateTime, boolean isRested) {
                            // 确保主线程更新
                            runOnUiThread(() -> {
                                TextView tvStartTime = findViewById(R.id.tv_start_time);
                                TextView tvEndTime = findViewById(R.id.tv_end_time);

                                tvStartTime.setText(FormatterUtils.getTimeString_24(stDateTime));
                                tvEndTime.setText(FormatterUtils.getTimeString_24(edDateTime));

                                    TextView tvStartDate = findViewById(R.id.tv_start_date);
                                    TextView tvEndDate = findViewById(R.id.tv_end_date);
                                    Log.e("mViewModel.currEvent.startTimeMilli",FormatterUtils.getTimeString_24(stDateTime));
                                    Log.e("mViewModel.currEvent.endTimeMilli",FormatterUtils.getTimeString_24(edDateTime));

                                    tvStartDate.setText(stDateTime.toString(DAYCODE_PATTERN));
                                    tvEndDate.setText(edDateTime.toString(DAYCODE_PATTERN));



                            });
                        }
                    });
                    Log.e("time picker", "222222222222222222222");
                },
                dateTime.getHourOfDay(),
                dateTime.getMinuteOfHour(),
                true);

        timePickerDialog.show();
    }



    private void saveEvent() {
        Log.e("save event","333333333333333333333333333333");
        mViewModel.currEvent.setTitle(edTitle.getText().toString());
        mViewModel.currEvent.setDescription(edDes.getText().toString());
        mViewModel.currEvent.setEventIsPersonal(swIsPersonal.isChecked()); // 保存 Switch 状态

        // 从 SharedPreferences 获取当前用户的 accountName
        String accountName = getCurrentUserId();

        Executors.newSingleThreadExecutor().execute(() -> {
            // 使用 findUser 方法获取 userId
            UserDao dao=new UserDao();
            User user = dao.findUser(accountName);
            if (user != null) {
                Log.e("find userID","333333333333333333333333333333");
                mViewModel.currEvent.setUserId(user.getId()); // 假设 Event 类有 setUserId 方法
            } else {
                runOnUiThread(() -> Toast.makeText(this, "userID not exist", Toast.LENGTH_SHORT).show());
                return; // 如果用户不存在，终止后续操作
            }

            if (eventId == -1L) {
                Log.e("save event!!!!!!!!!","333333333333333333333333333333");
                eventDAO.addEvent(mViewModel.currEvent);
            } else {
                eventDAO.updateEvent(mViewModel.currEvent);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "event save successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addevent, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) { // 确保这个 ID 与菜单中的 ID 一致
            saveEvent();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

}
