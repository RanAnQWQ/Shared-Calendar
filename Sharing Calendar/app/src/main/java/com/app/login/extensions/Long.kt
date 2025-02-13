package com.app.login.extensions


/**
 * @Title: Long
 * @Description: Long的扩展函数(用一句话描述该文件做什么)
 * @Author: cnctema
 * @CreateDate: 2020/6/8 12:34
 */
const val DAY_SECONDS = 24*60*60
fun Long.plusDayMilli(index:Int): Long = this + DAY_SECONDS * index * 1000L