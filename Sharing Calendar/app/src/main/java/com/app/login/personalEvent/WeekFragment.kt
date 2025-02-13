package com.app.login.personalEvent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.login.Config
import com.app.login.DAY_SECONDS
import com.app.login.EVENT_ID
import com.app.login.Hight_ALPHA
import com.app.login.LOW_ALPHA
import com.app.login.R
import com.app.login.WEEK_START_TIMESTAMP
import com.app.login.dao.EventDAO
import com.app.login.dao.UserDao
import com.app.login.entity.Event
import com.app.login.extensions.getConfig
import com.app.login.extensions.plusDayMilli
import com.app.login.util.FormatterUtils
import com.app.login.util.MyColorUtils
import com.app.login.view.MyScrollView
import com.cnctema.easecalendar_weekly_view.extensions.adjustAlpha
import com.cnctema.easecalendar_weekly_view.extensions.getContrastColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class WeekFragment : Fragment() {
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private lateinit var config: Config
    private lateinit var dataSource: EventDAO
    private lateinit var userDAO: UserDao
    private var mListener: WeekScrollListener? = null
    private var dayInCalendar = 7
    private var mWeekTimestamp = 0L
    private var currEventsListHashCode = 0
    private var selectedGrid : View? = null
    private lateinit var weekEventsScrollview: MyScrollView
    private lateinit var weekDaylettersHolder: LinearLayout
    private lateinit var weekColumn0: ViewGroup
    private lateinit var weekColumn1: ViewGroup
    private lateinit var weekColumn2: ViewGroup
    private lateinit var weekColumn3: ViewGroup
    private lateinit var weekColumn4: ViewGroup
    private lateinit var weekColumn5: ViewGroup
    private lateinit var weekColumn6: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (isAdded) {
            config = requireContext().getConfig()
        }

        userDAO=UserDao()
        dataSource = EventDAO() // Initialize your EventDAO instance
        dayInCalendar = config.dayInCalendar
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_week, container, false)
        Log.e("onCreateView","1111111111111111111111111")

        weekEventsScrollview = view.findViewById(R.id.week_events_scrollview)
        weekDaylettersHolder = view.findViewById(R.id.week_dayletters_holder)
        weekColumn0 = view.findViewById(R.id.week_column_0)
        weekColumn1 = view.findViewById(R.id.week_column_1)
        weekColumn2 = view.findViewById(R.id.week_column_2)
        weekColumn3 = view.findViewById(R.id.week_column_3)
        weekColumn4 = view.findViewById(R.id.week_column_4)
        weekColumn5 = view.findViewById(R.id.week_column_5)
        weekColumn6 = view.findViewById(R.id.week_column_6)

        mWeekTimestamp = requireArguments().getLong(WEEK_START_TIMESTAMP)

        weekEventsScrollview.setOnScrollviewListener(object : MyScrollView.ScrollViewListener {
            override fun onScrollChanged(scrollView: MyScrollView, x: Int, y: Int, oldx: Int, oldy: Int) {
                mListener?.scrollTo(y)
            }
        })

        weekEventsScrollview.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                updateScrollY(PersonalEventFragment.mWeekScrollY)
                weekEventsScrollview.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        hideEventsColumns(dayInCalendar)
        setupDayLabels()
        initGrid()
        return view
    }

    override fun onResume() {
        super.onResume()
        Log.e("onResume","1111111111111111111111111")
        updateEventsView()
    }

    private fun updateEventsView() {
        Log.e("updateEventsView","1111111111111111111111111")

        getCurrEventsList(dayInCalendar) { eventsList ->
            if (eventsList.isEmpty()) {
                Log.e("updateEventsView", "The events list is empty.")
                return@getCurrEventsList // Early exit if the list is empty
            }
            val newHashCode: Int = eventsList.hashCode()
            if (newHashCode == currEventsListHashCode) {
                return@getCurrEventsList
            } else {
                Log.e("updateEventSquare","66666666666666666666666666")
                currEventsListHashCode = newHashCode
                val eventViewHashMap = setEventViewHashMap(eventsList)
                if (eventViewHashMap.isEmpty()) {
                    Log.e("EventViewCheck", "The event view hash map is empty.")
                } else {
                    Log.e("EventViewCheck", "The event view hash map is not empty. Size: ${eventViewHashMap.size}")
                }
                clearViewGroup()
                updateEventSquare(eventViewHashMap)
            }
        }
    }



    private fun getCurrentUserId(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("currentUserId", null)
    }

    private fun getCurrEventsList(dayInCalendar: Int, callback: (List<Event>) -> Unit) {
        Thread {
            val userAccount = getCurrentUserId() // 获取当前用户账号
            val user = userDAO.findUser(userAccount) // 根据 userAccount 获取 User 对象
            val userId = user?.id ?: -1 // 获取用户 ID，默认为-1
            //val userId = 11 // Replace with actual user ID logic
            val currDay = FormatterUtils.getDateTimeFromTS(mWeekTimestamp)
            val startDateTime = currDay
            val endDateTime = currDay.plusDays(dayInCalendar).plusMillis(-1)
            Log.e("dataSource",""+startDateTime.toString()+" "+endDateTime.toString())
            val events = dataSource.getEventsByTimeRange(userId,startDateTime,endDateTime) // Get events for the current user

            // 使用 Handler 切换回主线程
            val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
            mainHandler.post {
                callback(events) // 处理事件列表，例如更新 UI
            }
        }.start()
    }

    //设置网格
    private fun initGrid(){
        (0 until dayInCalendar).map { getColumnWithId(it) }
            .forEachIndexed{ index, viewGroup ->
                val gestureDetector = getViewGestureDetector(viewGroup, index)
                viewGroup.setOnTouchListener{view,motionEvent->
                    gestureDetector.onTouchEvent(motionEvent)
                    true
                }
            }
    }
    // 设置事件矩形的哈希表
    private fun setEventViewHashMap(events: List<Event>): java.util.LinkedHashMap<String, java.util.ArrayList<EventWeeklyView>> {
        val eventViewHashMap =
            java.util.LinkedHashMap<String, java.util.ArrayList<EventWeeklyView>>()  //事件方块HashMap

        val edgeStDateTime = FormatterUtils.getDateTimeFromTS(mWeekTimestamp)   //当前边界开始时间DateTime
        val edgeStMilli = edgeStDateTime.millis                             //当前开始时间边界
        val edgeEndMilli = edgeStDateTime.plusDays(dayInCalendar).millis    //当前结束时间边界

        for (event in events) {
            val evStMilli = if(event.startTimeMilli>=edgeStMilli) event.startTimeMilli else edgeStMilli //有效绘制时间起点
            val evEdMilli = if(event.endTimeMilli<=edgeEndMilli) event.endTimeMilli else edgeEndMilli   //有效绘制时间结尾
            val stDateTime = DateTime(evStMilli, DateTimeZone.getDefault()) //事件开始时间
            val edDateTime = DateTime(evEdMilli, DateTimeZone.getDefault()) //事件结束时间
            val crossDay = edDateTime.dayOfYear - stDateTime.dayOfYear  //将事件拆分为跨天数子事件，跨天数子事件占用绘制天数
            val crossEdgeDay = stDateTime.dayOfYear-edgeStDateTime.dayOfYear    //距离边界开始时间的天数(可作为列标志)

            for(i in 0..crossDay) { //对于每一个事件拆分后的跨天数子事件，进行列分段
                //计算当前子事件开始时间
                val currViewStMilli =
                    if(i == 0)
                        evStMilli
                    else
                        edgeStMilli.plusDayMilli(crossEdgeDay+i) //距离边界开始时间的毫秒数

                val sDateTime = DateTime(currViewStMilli, DateTimeZone.getDefault())    //子事件开始时间的DateTime
                val dayCode = sDateTime.toString(FormatterUtils.DATE_PATTERN)   //计算键
                if (!eventViewHashMap.containsKey(dayCode)) {   //若哈希表里不存在键，创建一个
                    eventViewHashMap[dayCode] = java.util.ArrayList()
                }

                //计算当前子事件结束时间
                val currViewEdMilli =
                    if(crossDay == 0 || i==crossDay)
                        evEdMilli
                    else
                        edgeStMilli.plusDayMilli(i+crossEdgeDay+1)-1  //距离边界结束时间的毫秒数
                Log.e("setEventViewHashMap","idddddddddd "+event.id)
                //设置EventWeeklyView
                val eventWeekly = EventWeeklyView(
                    id = event.id,
                    title = event.title,
                    type = 0,
                    stTime = currViewStMilli,
                    edTime = currViewEdMilli
                ) //设置事件方块属性
                eventViewHashMap[dayCode]?.add(eventWeekly)
            }
        }

        return eventViewHashMap
    }

    private fun updateEventSquare(eventViewHashMap: LinkedHashMap<String, ArrayList<EventWeeklyView>>) {
        Log.e("updateEventSquare", "square")

        for (colIndex in 0 until dayInCalendar) {
            val currDateTime =FormatterUtils.getDateTimeFromTS(mWeekTimestamp+colIndex* DAY_SECONDS)   //当前时间DateTime
            Log.e("updateEventSquare", "current day time:"+currDateTime)
            Log.e("mWeekTimestamp", "Generated DAY_SECONDS: "+mWeekTimestamp+" "+ DAY_SECONDS)
            val dayCode = currDateTime.toString(FormatterUtils.DATE_PATTERN)

            Log.e("updateEventSquare", "Generated dayCode: $dayCode")

            Log.e("updateEventSquare", "Keys in eventViewHashMap: ${eventViewHashMap.keys.joinToString(", ")}")

            val evList = eventViewHashMap[dayCode]
            Log.e("updateEventSquare", "date: $dayCode, list: ${evList?.size ?: 0}")

            if (evList == null || evList.isEmpty()) {
                Log.e("updateEventSquare", "no event")
                continue
            }

            val mLayout = getColumnWithId(colIndex)

            for (evView in evList) {
                Log.e("drawEventSquare","12222222222222222222211")
                Log.e("EVENT_ID","idddddddddd "+evView.id)
                drawEventSquare(evView, mLayout) {
                    Intent(context, AddNewEventActivity::class.java).apply {
                        putExtra(EVENT_ID, evView.id)
                        startActivity(this)
                    }
                }
            }
        }
    }


    private fun drawEventSquare(evView: EventWeeklyView, mLayout: ViewGroup, myOnClick: (evView: View) -> Unit) {
        Log.e("drawEventSquare", "33333333333333333333333333333333")
        // Check if the fragment is added
        val miniHeight = requireContext().resources.getDimension(R.dimen.weekly_view_minimal_event_height).toInt() // 事件方块最小高度
        val oneMinHeight = resources.getDimension(R.dimen.weekly_view_one_minute_height) // 事件一分钟对应的方块高度
        val density = Math.round(resources.displayMetrics.density) // 获取屏幕密度

        val stDateTime = DateTime(evView.stTime, DateTimeZone.getDefault()) // 开始时间 DateTime
        val edDateTime = DateTime(evView.edTime, DateTimeZone.getDefault()) // 结束时间 DateTime
        val stMinutes = stDateTime.minuteOfDay // 事件开始时的分钟数
        val stMinuteHeight = stMinutes * oneMinHeight // 事件方块开始高度
        val durationHeight = ((edDateTime.minuteOfDay - stMinutes) * oneMinHeight).toInt() // 持续时间

        val colIndex = evView.colIndex // 事件方块列标号
        val colNum = evView.colNum // 事件方块宽度分子
        val colDenom = evView.colDenom // 事件方块宽度分母
        if (!isAdded) return
        // 使用 requireContext() 获取有效的上下文
        (LayoutInflater.from(context)
            .inflate(R.layout.item_week_event_square, null, false) as TextView).apply {
            var mBackgroundColor = setEvColorByType(requireContext(), evView.type) // TODO: 设置成多组自定义颜色
            var mTextColor = mBackgroundColor.getContrastColor() // 根据背景确定对比色字体
            if (evView.edTime < System.currentTimeMillis()) { // 此事件已过当前时间
                mBackgroundColor = mBackgroundColor.adjustAlpha(LOW_ALPHA) // 设置透明度
                mTextColor = mTextColor.adjustAlpha(Hight_ALPHA)
            }

            background = ColorDrawable(mBackgroundColor)
            setTextColor(mTextColor)
            text = "${evView.title}\n\n${evView.location}"

            mLayout.addView(this) // 添加事件方块
            y = stMinuteHeight + density
            // 设置矩形框View属性
            (layoutParams as RelativeLayout.LayoutParams).apply {
                width = mLayout.width - 1
                width /= colDenom // 根据重叠个数设置宽度
                if (colDenom > 1) {
                    x = width * colIndex.toFloat() // 根据重叠次序设置位置
                    width *= colNum
                    if (colIndex != 0) {
                        x += density // 左移，左边位移留空
                        if (colIndex + 1 != colDenom) { // 事件不是最后一个或第一个
                            width -= density // 减少宽度
                        }
                    }
                    width -= density // 减少宽度
                }

                minHeight = if (durationHeight > miniHeight) (durationHeight - density) else miniHeight
                maxHeight = minHeight
            }

            // 设置点击监听
            setOnClickListener {
                myOnClick(it)
            }
        }
    }


    private fun getColumnWithId(index: Int): ViewGroup {
        return when (index) {
            0 -> weekColumn0
            1 -> weekColumn1
            2 -> weekColumn2
            3 -> weekColumn3
            4 -> weekColumn4
            5 -> weekColumn5
            6 -> weekColumn6
            else -> weekColumn0
        }
    }
    //根据事件type类型赋值颜色
    private fun setEvColorByType(context: Context, type:Int):Int{
        return ContextCompat.getColor(context,
            MyColorUtils.eventViewColorList[type% MyColorUtils.eventViewColorList.size]
        )
    }

    private fun setupDayLabels() {
        var curDay = FormatterUtils.getDateTimeFromTS(mWeekTimestamp)
        for (i in 1..dayInCalendar) {
            val dayView = layoutInflater.inflate(R.layout.item_weekly_view_dayletter_textview, null, false) as TextView
            val dayLetter = FormatterUtils.getDayLetter(requireContext(), curDay.dayOfWeek)
            dayView.apply {
                text = "${dayLetter}\n${curDay.dayOfMonth}"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                setPadding(0, 20, 0, 0)
                if (curDay.dayOfYear == DateTime(System.currentTimeMillis()).dayOfYear) {
                    this.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                }
            }

            // 使用 view?.findViewById 进行安全调用
            weekDaylettersHolder.addView(dayView,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ))

            curDay = curDay.plusDays(1)
        }
    }




    private fun hideEventsColumns(dayInCalendar: Int) {
        for (i in dayInCalendar until 7) {
            getColumnWithId(i).visibility = View.GONE
        }
    }
    private fun getViewGestureDetector(view: ViewGroup, index: Int): GestureDetector {
        return GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                selectedGrid?.animation?.cancel()
                selectedGrid?.visibility = View.GONE

                val rowHeight = resources.getDimension(R.dimen.weekly_view_row_height)
                val hour = (e.y/rowHeight).toInt()

                //选择的网格方块
                selectedGrid = (LayoutInflater.from(context)
                    .inflate(R.layout.item_week_grid,null,false) as ImageView).apply {
                    view.addView(this)
                    layoutParams.width = view.width //宽
                    layoutParams.height = rowHeight.toInt() //高
                    y = hour * rowHeight

                    //设置点击后的动作
                    setOnClickListener{
                        val timestamp = mWeekTimestamp + index * 24*60*60 + hour*60*60
                        Log.e("getViewGestureDetector", "timestamp:$timestamp")
                        Intent(context, AddNewEventActivity::class.java).apply {
                            putExtra(WEEK_START_TIMESTAMP, timestamp)
                            startActivity(this)
                        }
                    }
                    //设置动画
                    animate().setStartDelay(500L).alpha(0f).withEndAction{
                        visibility = View.GONE
                    }
                }
                return super.onSingleTapUp(e)
            }
        })
    }
    private fun clearViewGroup() {
        weekColumn0.removeAllViews()
        weekColumn1.removeAllViews()
        weekColumn2.removeAllViews()
        weekColumn3.removeAllViews()
        weekColumn4.removeAllViews()
        weekColumn5.removeAllViews()
        weekColumn6.removeAllViews()
    }

    fun updateScrollY(y: Int) {
        weekEventsScrollview.scrollTo(0, y)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WeekScrollListener) {
            mListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface WeekScrollListener {
        fun scrollTo(y: Int)
    }
    //设置监听
    fun setListener(listener: WeekScrollListener) {
        mListener = listener
    }

}