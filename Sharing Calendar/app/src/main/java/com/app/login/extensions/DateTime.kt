@file:JvmName("DateTimeExtensions")
package com.app.login.extensions

import org.joda.time.DateTime


fun seconds(dateTime: DateTime): Long = dateTime.millis / 1000L
