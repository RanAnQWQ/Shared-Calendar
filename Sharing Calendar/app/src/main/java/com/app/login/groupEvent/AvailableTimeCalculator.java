package com.app.login.groupEvent;

import com.app.login.TimeSlot;
import com.app.login.entity.Event;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class AvailableTimeCalculator {

    // 假设一天的时间范围是从00:00到23:59
    private static final int START_OF_DAY = 0;  // 00:00
    private static final int END_OF_DAY = 24;   // 24:00

    public static List<TimeSlot> getAvailableTimeSlotsForUser(List<Event> events, DateTime startDate, DateTime endDate) {
        List<TimeSlot> availableTimeSlots = new ArrayList<>();

        // 假设用户的空闲时间是从事件的开始时间到结束时间的空隙
        // 1. 创建一天的空闲时间段：从00:00到24:00
        int currentStart = START_OF_DAY;

        // 2. 遍历用户的个人事件，找到每个事件的时间段，排除在外
        for (Event event : events) {
            DateTime eventStart = new DateTime(event.getStartTimeMilli());
            DateTime eventEnd = new DateTime(event.getEndTimeMilli());

            // 如果事件发生在我们查询的时间范围内，才需要处理
            if (eventStart.isBefore(endDate) && eventEnd.isAfter(startDate)) {
                // 找到事件前的空闲时间段
                if (eventStart.getHourOfDay() > currentStart) {
                    availableTimeSlots.add(new TimeSlot(currentStart, eventStart.getHourOfDay()));
                }
                // 更新当前时间到事件结束后
                currentStart = eventEnd.getHourOfDay();
            }
        }

        // 3. 添加最后的空闲时间段
        if (currentStart < END_OF_DAY) {
            availableTimeSlots.add(new TimeSlot(currentStart, END_OF_DAY));
        }

        return availableTimeSlots;
    }
}
