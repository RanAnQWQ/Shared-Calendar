package com.app.login.personalEvent

/**
 * @ClassName: EventWeeklyView
 * @Description: 周视图事件事件方块类(类的作用描述)
 * @Author: cnctema
 * @CreateDate: 2020/6/2 23:52
 */
data class EventWeeklyView(
    //源事件信息
    val id:Int,            //id
    val title:String,       //标题
    val location:String="", //地点
    val type:Int=0,          //类型

    //时间
    val stTime:Long,        //开始时间
    val edTime:Long,        //结束时间

    //周视图排列
    var colIndex:Int = 0,   //排列序号
    var colNum:Int = 1,     //宽度平均分子
    var colDenom:Int = 1,    //宽度平均分分母

    //标记
    var sign:Boolean=false
){
    //判断事件方块间是否有重叠
    fun isTouch(otherStTime:Long,otherEdTime:Long):Boolean = stTime<otherEdTime && edTime>otherStTime
}
