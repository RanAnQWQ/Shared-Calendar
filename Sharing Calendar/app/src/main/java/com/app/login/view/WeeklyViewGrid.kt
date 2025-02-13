/*
 * Copyright 2017 SimpleMobileTools
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.app.login.view;
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.app.login.R
import com.app.login.extensions.getConfig

/**
 * @ClassName: WeeklyViewGrid
 * @Description: 自定义周视图网格(类的作用描述)
 * @Author: cnctema
 * @CreateDate: 2020/6/6 15:02
 */
class WeeklyViewGrid(context:Context,attrs:AttributeSet,defStyle:Int):View(context,attrs,defStyle) {
    private val ROWS_CNT = 24
    private val COLS_CNT = context.getConfig().dayInCalendar
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context,attrs: AttributeSet):this(context,attrs,0)

    init {
        paint.color = ContextCompat.getColor(context, R.color.divider)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rowHeight = resources.getDimension(R.dimen.weekly_view_row_height)
        for(i in 0 until ROWS_CNT){
            val y = rowHeight*i.toFloat()
            canvas.drawLine(0f,y,width.toFloat(),y,paint)
        }

        val rowWidth = width/COLS_CNT.toFloat()
        for(i in 0 until COLS_CNT){
            val x = rowWidth*i.toFloat()
            canvas.drawLine(x,0f,x,height.toFloat(),paint)
        }
    }
}
