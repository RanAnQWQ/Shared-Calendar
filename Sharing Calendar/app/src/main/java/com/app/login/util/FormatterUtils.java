package com.app.login.util;

import android.content.Context;
import com.app.login.R;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @Title: Constants
 * @Description: 格式转换工具类
 * @Author: cnctema
 * @CreateDate: 2020/5/24 21:45
 */
public class FormatterUtils {
    public static final String DAYCODE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_12_PATTERN = "HH:mm a";
    public static final String TIME_24_PATTERN = "HH:mm";
    public static final String DATE_TIME_PATTERN = "yyyy年M月d日 HH:mm";
    public static final String DATE_TIME_PATTERN_2 = "M月d日 HH:mm";
    public static final String DATE_PATTERN = "yyyy-MM-dd"; // 只显示日期

    // 日期转换工具方法
    public static DateTime getDateTimeFromTS(long ts) {
        return new DateTime(ts * 1000L, DateTimeZone.getDefault());
    }

    // 使用手动翻译的月份名称，因为DateFormat和Joda在多语言支持上有问题
    public static String getMonthName(Context context, int id) {
        return context.getResources().getStringArray(R.array.months)[id - 1];
    }

    // 时间转化为24小时格式字符串
    public static String getTimeString_24(DateTime dateTime) {
        String hourStr = String.valueOf(dateTime.getHourOfDay() % 24);
        return hourStr.length() == 2 ?
                hourStr + ":" + dateTime.toString("mm") :
                "0" + hourStr + ":" + dateTime.toString("mm");
    }

    // 获取当前周视图列的星期数
    public static String getDayLetter(Context context, int id) {
        return context.getResources().getStringArray(R.array.dayLetter)[id - 1];
    }
    public static String formatDayLabel(DateTime dateTime) {
        // You can format the date in different ways. Here, I'll use the day of the week and the month/day format.
        return dateTime.toString("MM/dd"); // For example: "Monday, 11/17"
    }
}
