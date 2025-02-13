package com.cnctema.easecalendar_weekly_view.extensions

import android.graphics.Color

/**
 * @Title: Int
 * @Description: Int类型的扩展函数(用一句话描述该文件做什么)
 * @Author: cnctema
 * @CreateDate: 2020/6/1 11:24
 */

//根据背景确定对比色字体
fun Int.getContrastColor(): Int {
    val y = (299 * Color.red(this) + 587 * Color.green(this) + 114 * Color.blue(this)) / 1000
    return if (y >= 125 && this != Color.BLACK) Color.BLACK else Color.WHITE
}

//设置透明度
fun Int.adjustAlpha(factor: Float): Int {
    val alpha = Math.round(Color.alpha(this) * factor)
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    return Color.argb(alpha, red, green, blue)
}