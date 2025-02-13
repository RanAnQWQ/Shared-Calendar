package com.app.login.personalEvent;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.app.login.entity.Event;
import com.app.login.dao.EventDAO;
import com.app.login.util.FormatterUtils;
import org.joda.time.DateTime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEventViewModel extends AndroidViewModel {
    private final EventDAO database;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    //private MutableLiveData<Event> currEventLiveData = new MutableLiveData<>(); // LiveData用于观察
    public boolean isNewEvent = true; // 是否是新日程
    public Event currEvent;
    public AddEventViewModel(@NonNull Application app, EventDAO database) {
        super(app);
        this.database = database;
    }
    public interface Callback<T> {
        void onResult(T result);
    }
    public interface DateTimeCallback {
        void onDateTimeReset(DateTime stDateTime, DateTime edDateTime, boolean isReseted);
    }

    // 初始化日程数据
    public void initializeCurrEvent(long eventId, long weekTimestamp, Callback<Event> callback) {
        executorService.execute(() -> {
            Event event = getEventFromDatabase(eventId);
            if (event != null) {
                currEvent = event;
                isNewEvent = false;
            } else {
                event = getNewEvent(weekTimestamp);
                currEvent = event;
                isNewEvent = true;
            }
            callback.onResult(event); // 使用 LiveData 获取当前事件
        });
    }

    // 获取日程数据
    private Event getEventFromDatabase(long eventId) {
        return database.getEventFromDatabase(eventId);
    }

    // 创建新的日程
    public Event getNewEvent(long weekTimestamp) {
        if (weekTimestamp == -1L) {
            return new Event();
        } else {
            DateTime startTime = FormatterUtils.getDateTimeFromTS(weekTimestamp);
            return new Event(
                    startTime.getMillis(),
                    startTime.plusMinutes(30).getMillis()
            );
        }
    }

    // 重新计算开始或结束日期
    public void resetDate(int year, int monthOfYear, int dayOfMonth, int sd, DateTimeCallback callback) {
        if (currEvent == null) return; // 确保 currEvent 不为 null

        DateTime stDateTime = new DateTime(currEvent.getStartTimeMilli());
        DateTime edDateTime = new DateTime(currEvent.getEndTimeMilli());
        boolean isRested = false;

        switch (sd) {
            case 0:
                stDateTime = new DateTime(year, monthOfYear, dayOfMonth,
                        stDateTime.getHourOfDay(),
                        stDateTime.getMinuteOfHour(),
                        stDateTime.getSecondOfMinute());
                currEvent.startTimeMilli = stDateTime.getMillis();
                if (stDateTime.getMillis() > edDateTime.getMillis()) {
                    edDateTime = stDateTime.plusMinutes(30);
                    currEvent.endTimeMilli = edDateTime.getMillis();
                    isRested = true;
                }
                break;
            default:
                edDateTime = new DateTime(year, monthOfYear, dayOfMonth,
                        edDateTime.getHourOfDay(),
                        edDateTime.getMinuteOfHour(),
                        edDateTime.getSecondOfMinute());
                currEvent.endTimeMilli = edDateTime.getMillis();
                if (stDateTime.getMillis() > edDateTime.getMillis()) {
                    stDateTime = edDateTime.minusMinutes(30);
                    currEvent.startTimeMilli = stDateTime.getMillis();
                    isRested = true;
                }
                break;
        }
        // 调用回调
        callback.onDateTimeReset(stDateTime, edDateTime, isRested);
    }


    // 重新计算开始或结束时间
    public void resetTime(int hourOfDay, int minute, int sd, DateTimeCallback callback) {
        DateTime stDateTime = new DateTime(currEvent.startTimeMilli);
        DateTime edDateTime = new DateTime(currEvent.endTimeMilli);
        boolean isRested = false;

        switch (sd) {
            case 0:
                stDateTime = new DateTime(stDateTime.getYear(), stDateTime.getMonthOfYear(), stDateTime.getDayOfMonth(),
                        hourOfDay, minute, stDateTime.getSecondOfMinute());
                currEvent.startTimeMilli = stDateTime.getMillis();
                if (stDateTime.getMillis() > edDateTime.getMillis()) {
                    edDateTime = stDateTime.plusMinutes(30);
                    currEvent.endTimeMilli = edDateTime.getMillis();
                    isRested = true;
                }
                break;
            default:
                edDateTime = new DateTime(edDateTime.getYear(), edDateTime.getMonthOfYear(), edDateTime.getDayOfMonth(),
                        hourOfDay, minute, stDateTime.getSecondOfMinute());
                currEvent.endTimeMilli = edDateTime.getMillis();
                if (stDateTime.getMillis() > edDateTime.getMillis()) {
                    stDateTime = edDateTime.minusMinutes(30);
                    currEvent.startTimeMilli = stDateTime.getMillis();
                    isRested = true;
                }
                break;
        }

        callback.onDateTimeReset(stDateTime, edDateTime, isRested);
    }


}
