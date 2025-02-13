@file:JvmName("ContextExtensions")

package com.app.login.extensions

import android.content.Context
import com.app.login.Config

fun Context.getSharedPrefs() = getSharedPreferences("Prefs", Context.MODE_PRIVATE)

fun Context.getConfig(): Config = Config.newInstance(this)


fun Context.secondsInWeek(): Int = 7 * 24 * 60 * 60
