package com.app.login
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import com.app.login.extensions.getSharedPrefs
/**
 * @Title: Context
 * @Description: context的扩展函数(用一句话描述该文件做什么)
 * @Author: cnctema
 * @CreateDate: 2020/5/24 20:35
 */

// 个性化设置
const val START_WORKTIME_AT = "start_worktime_at"
const val BACKGROUND_COLOR = "background_color"
const val PRIMARY_COLOR = "primary_color"
const val DAY_IN_CALENDAR = "day_in_Calendar"
const val SNOOZE_MINUTE ="snooze_minute"

//账户
const val MY_NAME : String = "my_name"

class Config(val context: Context){
    companion object{
        fun newInstance(context: Context) = Config(context)
    }
    protected val prefs: SharedPreferences = context.getSharedPrefs()

    var startWorkTimeAt: Int  //默认工作开始时间
        get() = prefs.getInt(START_WORKTIME_AT, 7)
        set(startWorkTimeAt) = prefs.edit().putInt(START_WORKTIME_AT, startWorkTimeAt).apply()
    var dayInCalendar:Int   //默认视图显示天数
        get() = prefs.getInt(DAY_IN_CALENDAR,7)
        set(dayInCalendar) = prefs.edit().putInt(DAY_IN_CALENDAR,dayInCalendar).apply()
    var snoozeMinute : Int  //默认等待时间
        get() = prefs.getInt(SNOOZE_MINUTE,10)
        set(snoozeMinute) = prefs.edit().putInt(SNOOZE_MINUTE,snoozeMinute).apply()

    var backgroundColor: Int    // 默认背景颜色
        get() = prefs.getInt(BACKGROUND_COLOR, ContextCompat.getColor(context, R.color.default_background_color))
        set(backgroundColor) = prefs.edit().putInt(BACKGROUND_COLOR, backgroundColor).apply()
    var primaryColor: Int       //默认主题色
        get() = prefs.getInt(PRIMARY_COLOR, ContextCompat.getColor(context, R.color.colorPrimary))
        set(primaryColor) = prefs.edit().putInt(PRIMARY_COLOR, primaryColor).apply()


}