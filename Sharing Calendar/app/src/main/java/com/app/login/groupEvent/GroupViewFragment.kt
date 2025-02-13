package com.app.login.groupEvent
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.login.R
import com.app.login.WEEK_START_TIMESTAMP
import com.app.login.dao.EventDAO
import com.app.login.dao.GroupEventParticipantDAO
import com.app.login.util.FormatterUtils
import com.app.login.view.MyScrollView
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class GroupViewFragment : Fragment() {
    private var mListener: WeekScrollListener? = null
    private lateinit var eventDAO: EventDAO
    private lateinit var participantDAO: GroupEventParticipantDAO
    private var dayInCalendar = 7
    private var mWeekTimestamp = 0L
    private var groupEventId: Int = 0
    private var groupEventStartTimestamp: Long = 0L
    private var groupEventEndTimestamp: Long = 0L
    private lateinit var weekEventsScrollview: MyScrollView
    private lateinit var weekDaylettersHolder: LinearLayout
    private lateinit var weekColumn0: ViewGroup
    private lateinit var weekColumn1: ViewGroup
    private lateinit var weekColumn2: ViewGroup
    private lateinit var weekColumn3: ViewGroup
    private lateinit var weekColumn4: ViewGroup
    private lateinit var weekColumn5: ViewGroup
    private lateinit var weekColumn6: ViewGroup
    private var selectedGrid : View? = null
    private val weekColumns = arrayOfNulls<ViewGroup>(7)

    companion object {
        private const val DAY_SECONDS = 24 * 60 * 60 * 1000 // 一天的毫秒数
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        eventDAO = EventDAO()
        participantDAO = GroupEventParticipantDAO()

        arguments?.let {
            groupEventId = it.getLong("GROUP_EVENT_ID", 0L).toInt()
            groupEventStartTimestamp = it.getLong("GROUP_EVENT_START_TIMESTAMP", 0L)
            groupEventEndTimestamp = it.getLong("GROUP_EVENT_END_TIMESTAMP", 0L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_week, container, false)
        weekEventsScrollview = view.findViewById(R.id.week_events_scrollview)
        weekDaylettersHolder = view.findViewById(R.id.week_dayletters_holder)
        weekColumns[0] = view.findViewById(R.id.week_column_0)
        weekColumns[1] = view.findViewById(R.id.week_column_1)
        weekColumns[2] = view.findViewById(R.id.week_column_2)
        weekColumns[3] = view.findViewById(R.id.week_column_3)
        weekColumns[4] = view.findViewById(R.id.week_column_4)
        weekColumns[5] = view.findViewById(R.id.week_column_5)
        weekColumns[6] = view.findViewById(R.id.week_column_6)
        mWeekTimestamp = requireArguments().getLong(WEEK_START_TIMESTAMP)
        weekEventsScrollview.setOnScrollviewListener(object : MyScrollView.ScrollViewListener {
            override fun onScrollChanged(scrollView: MyScrollView, x: Int, y: Int, oldx: Int, oldy: Int) {
                mListener?.scrollTo(y)
            }
        })

        weekEventsScrollview.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                updateScrollY(GroupEventDetailsActivity.mWeekScrollY)
                weekEventsScrollview.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        setupDayLabels()
        //initGrid()
        updateEventsView()

        return view
    }

    private fun updateEventsView() {
        Thread {
            // 获取群体事件参与者
            val participants = participantDAO.getParticipantsByEventId(groupEventId)
            if (participants.isEmpty()) {
                Log.e("GroupViewFragment", "No participants for this group event.")
                return@Thread
            }

            // 获取所有参与者的事件时间段
            val allEvents = participants.flatMap { participant ->
                eventDAO.getEventsByTimeRange(
                    participant.userId,
                    DateTime(groupEventStartTimestamp * 1000, DateTimeZone.getDefault()),
                    DateTime(groupEventEndTimestamp * 1000, DateTimeZone.getDefault())
                )
            }

            Log.d("GroupViewFragment", "Total events fetched: ${allEvents.size}")

            // 打印所有的事件时间段
            allEvents.forEach { event ->
                Log.d(
                    "GroupViewFragment",
                    "Event: id=${event.id}, start=${event.startTimeMilli}, end=${event.endTimeMilli}"
                )
            }

            // 计算时间范围内的所有冲突时间段
            val occupiedTimeRanges = allEvents.map { event ->
                event.startTimeMilli..event.endTimeMilli
            }

            Log.d("GroupViewFragment", "Occupied time ranges count: ${occupiedTimeRanges.size}")

            // 获取参与者的期望时间段
            val preferredTimeSlotsList = participantDAO.getPreferredTimeSlotsByEventId(groupEventId)
            val perferredDateTimeList = participantDAO.getPreferredTimeSlotsListByEventId(groupEventId)

            Log.d("GroupViewFragment", "Preferred time slots list: $preferredTimeSlotsList")

            // 判断所有参与者的期望时间段是否为空
            val allPreferredTimeSlotsEmpty = preferredTimeSlotsList.all { jsonStr ->
                // 尝试解析 JSON 字符串
                val jsonArray = try {
                    JSONArray(jsonStr) // 解析为 JSON 数组
                } catch (e: Exception) {
                    Log.e("GroupViewFragment", "Error parsing JSON: $e")
                    null // 如果解析失败，返回 null
                }
                // 判断 JSON 数组是否为空
                jsonArray == null || jsonArray.length() == 0
            }

            // 如果所有参与者的期望时间段都为空，则跳过交集计算，直接进行共同空闲时间段的计算
            val freeTimeRanges = if (allPreferredTimeSlotsEmpty) {
                // 计算共同空闲时间段，不考虑期望时间段的交集
                Log.d("GroupViewFragment", "All participants have no preferred time slots, calculating free time ranges directly.")
                findFreeTimeRanges(
                    groupEventStartTimestamp * 1000,
                    groupEventEndTimestamp * 1000,
                    occupiedTimeRanges
                )
            } else {
                // 计算期望时间段的交集
                val commonTimeSlots = preferredTimeSlotsList
                    .mapNotNull {
                        val jsonArray = try {
                            JSONArray(it) // 解析为 JSON 数组
                        } catch (e: Exception) {
                            Log.e("GroupViewFragment", "Error parsing JSON: $e")
                            null // 如果解析失败，返回 null
                        }
                        // 转换为 Set<String>，忽略空的时间段
                        if (jsonArray != null && jsonArray.length() > 0) {
                            (0 until jsonArray.length()).map { jsonArray.getString(it) }.toSet()
                        } else {
                            null
                        }
                    }
                    .reduceOrNull { acc, slots -> acc.intersect(slots) }
                    ?: emptySet() // 如果最终为空则返回空集合

                Log.d("GroupViewFragment", "Common preferred time slots: $commonTimeSlots")

                // 如果交集为空，直接返回
                if (commonTimeSlots.isEmpty()) {
                    Log.e("GroupViewFragment", "No common preferred time slots found.")
                    return@Thread
                }

                // 转换交集为具体时间范围
                var preferredTimeRanges = mapPreferredTimeSlotsToTimeRanges(
                    commonTimeSlots.toList(), // 将交集转换为列表传入
                    groupEventStartTimestamp * 1000,
                    groupEventEndTimestamp * 1000
                )

                // perferredDateTimeList转为时间戳



                val excludedTimestamps = convertPreferredDateTimeListToTimestamps(perferredDateTimeList
                )
                // 在preferredTimeRanges排除掉excludedTimestamps的时间戳
                preferredTimeRanges =  excludedTimestamps(preferredTimeRanges,excludedTimestamps)


                Log.d("GroupViewFragment", "Preferred time ranges: ${formatTimesRange(preferredTimeRanges)}")

                // 计算共同空闲时间段
                findFreeTimeRanges(
                    groupEventStartTimestamp * 1000,
                    groupEventEndTimestamp * 1000,
                    occupiedTimeRanges
                ).flatMap { range ->
                    preferredTimeRanges.mapNotNull { preferredRange ->
                        // 手动计算 LongRange 的交集
                        val start = maxOf(range.start, preferredRange.start)
                        val end = minOf(range.endInclusive, preferredRange.endInclusive)
                        if (start <= end) start..end else null // 只有当交集有效时才返回
                    }
                }
            }

            // 打印过滤后的空闲时间段
            freeTimeRanges.forEachIndexed { index, range ->
                Log.d("GroupViewFragment", "Filtered free range $index: $range")
            }

            activity?.runOnUiThread {
                val sharedPreferences = activity?.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                val editor = sharedPreferences?.edit()

                // 清空旧的数据
                editor?.remove("FREE_TIME_RANGES")

                // 按照时间段的开始时间排序
                val sortedFreeTimeRanges = freeTimeRanges.sortedBy { it.start }

                // 序列化 sortedFreeTimeRanges 为 JSON 字符串
                val jsonRanges = sortedFreeTimeRanges.map { range ->
                    JSONArray().apply { put(range.start); put(range.endInclusive) }.toString()
                }

                // 插入新的值
                editor?.putStringSet("FREE_TIME_RANGES", jsonRanges.toSet())

                // 提交修改
                editor?.apply()

                // 更新视图
                clearViewGroup()
                drawFreeTimeRanges(sortedFreeTimeRanges)
            }
        }.start()
    }
    /**
     * 在 sourceTimestampsList 中排除 excludedTimestampsList 中的时间段，并重新分割
     *
     * @param sourceTimestampsList 源时间段列表
     * @param excludedTimestampsList 需要排除的时间段列表
     * @return 排除后的时间段列表
     */
   private fun excludedTimestamps(
        sourceTimestampsList: List<LongRange>,
        excludedTimestampsList: List<LongRange>
    ): List<LongRange> {
        val result = mutableListOf<LongRange>()

        // 遍历源时间段列表
        for (sourceRange in sourceTimestampsList) {
            var remainingRange = sourceRange

            // 遍历需要排除的时间段列表
            for (excludedRange in excludedTimestampsList) {
                if (remainingRange.endInclusive < excludedRange.start || remainingRange.start > excludedRange.endInclusive) {
                    // 如果没有重叠，继续处理下一个 excludedRange
                    continue
                }

                // 计算重叠部分
                val overlapStart = maxOf(remainingRange.start, excludedRange.start)
                val overlapEnd = minOf(remainingRange.endInclusive, excludedRange.endInclusive)

                // 排除重叠部分
                if (remainingRange.start < overlapStart) {
                    // 添加重叠前的部分
                    result.add(remainingRange.start .. overlapStart)
                }
                if (remainingRange.endInclusive > overlapEnd) {
                    // 更新 remainingRange 为重叠后的部分
                    remainingRange = overlapEnd ..remainingRange.endInclusive
                } else {
                    // 如果 remainingRange 完全被排除，跳出循环
                    remainingRange = LongRange.EMPTY
                    break
                }
            }

            // 如果 remainingRange 还有剩余，添加到结果中
            if (remainingRange != LongRange.EMPTY) {
                result.add(remainingRange)
            }
        }


        return result
    }

    /**
     * 将 preferredTimeSlots 转换为具体的时间范围
     */
    private fun mapPreferredTimeSlotsToTimeRanges(
        preferredTimeSlotsList: List<String>,
        startTimestamp: Long,
        endTimestamp: Long
    ): List<LongRange> {
        val timeSlotToRange = mapOf(
            "morning" to 6 * 60..11 * 60,   // 06:00 - 11:00
            "noon" to 11 * 60..13 * 60,    // 11:00 - 13:00
            "afternoon" to 13 * 60..18 * 60, // 13:00 - 18:00
            "evening" to 18 * 60..22 * 60,  // 18:00 - 22:00
            "night" to 22 * 60..24 * 60 + 6 // 22:00 - 06:00 (next day)
        )

        val result = mutableListOf<LongRange>()
        val startDate = DateTime(startTimestamp, DateTimeZone.getDefault())
        val endDate = DateTime(endTimestamp, DateTimeZone.getDefault())

        // 遍历日期范围
        var currentDate = startDate
        while (currentDate.isBefore(endDate)) {
            val dayStart = currentDate.withTimeAtStartOfDay().millis
            val dayEnd = dayStart + 24 * 60 * 60 * 1000 // 下一天的开始时间

            preferredTimeSlotsList.forEach { preferredSlotsJson ->
                val preferredSlots = parsePreferredSlots(preferredSlotsJson)
                preferredSlots.forEach { slot ->
                    val rangeMinutes = timeSlotToRange[slot]
                    if (rangeMinutes != null) {
                        val rangeStart = dayStart + rangeMinutes.first * 60 * 1000
                        val rangeEnd = dayStart + rangeMinutes.last * 60 * 1000
                        result.add(rangeStart..rangeEnd)
                    }
                }
            }

            currentDate = currentDate.plusDays(1)
        }

        return result
    }
    /**
     * 将 preferredDateTimeList 中的时间字符串转换为时间戳
     *
     * @param perferredDateTimeList 时间列表
     * @return
     */
    private fun convertPreferredDateTimeListToTimestamps(preferredDateTimeList: List<String>): List<LongRange> {
        // 定义时间格式
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        // 存储结果的列表
        val timestampList = mutableListOf<LongRange>()

        // 遍历 perferredDateTimeList
        preferredDateTimeList.forEach { timeSlot ->
            // 分割时间字符串
            val parts = timeSlot.split(" - ")
            if (parts.size == 2) {
                // 解析开始时间和结束时间
                val startTime = dateFormat.parse(parts[0])?.time
                val endTime = dateFormat.parse(parts[1])?.time

                // 检查解析结果是否有效
                if (startTime != null && endTime != null) {
                    timestampList.add(startTime..endTime)
                } else {
                    Log.e("GroupViewFragment", "Failed to parse time slot: $timeSlot")
                }
            } else {
                Log.e("GroupViewFragment", "Invalid time slot format: $timeSlot")
            }
        }

        return timestampList
    }

    /**
     * 将时间字符串（如 "2025-01-10 19:00"）转换为时间戳
     */
    private fun parseTimeStringToTimestamp(timeString: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC") // 设置时区为 UTC
        return dateFormat.parse(timeString)?.time ?: throw IllegalArgumentException("Invalid time string: $timeString")
    }
    /**
     * 解析 JSON 格式的 preferred slots
     */
    private fun parsePreferredSlots(preferredSlotsJson: String): List<String> {
        // 判断String类型中是否为数组类型
        val isArray = preferredSlotsJson.startsWith("[") && preferredSlotsJson.endsWith("]")
        if (!isArray){
            return preferredSlotsJson.split(",")
        }
        // 确保返回的 list 是字符串类型，例如 ["noon", "afternoon"]
        val jsonArray = JSONArray(preferredSlotsJson)
        return List(jsonArray.length()) { jsonArray.getString(it) }
    }


    /**
     * 扩展函数检查两个时间范围是否重叠
     */
    private fun LongRange.overlaps(other: LongRange): Boolean {
        return this.start <= other.endInclusive && other.start <= this.endInclusive
    }




    private fun findFreeTimeRanges(
        startTimestamp: Long,
        endTimestamp: Long,
        occupiedTimeRanges: List<LongRange>
    ): List<LongRange> {
        Log.d("GroupViewFragment", "Finding free time ranges...")
        Log.d("GroupViewFragment", "Start timestamp: ${formatTimestamp(startTimestamp)}, End timestamp: ${formatTimestamp(endTimestamp)}")

        // Step 1: 排序并合并重叠的时间段
        val mergedRanges = mutableListOf<LongRange>()
        val sortedRanges = occupiedTimeRanges.sortedBy { it.start }
        sortedRanges.forEach { range ->
            if (mergedRanges.isEmpty() || mergedRanges.last().endInclusive < range.start - 1) {
                // 当前时间段与最后一个已合并时间段不重叠
                mergedRanges.add(range)
            } else {
                // 合并重叠时间段
                val lastRange = mergedRanges.removeAt(mergedRanges.size - 1)
                mergedRanges.add(lastRange.start..maxOf(lastRange.endInclusive, range.endInclusive))
            }
        }
        Log.d("GroupViewFragment", "Merged occupied ranges: $mergedRanges")

        // Step 2: 找出空闲时间段
        val freeTimeRanges = mutableListOf<LongRange>()
        var currentStart = startTimestamp

        for (range in mergedRanges) {
            if (range.start > currentStart) {
                freeTimeRanges.add(currentStart until range.start)
            }
            currentStart = maxOf(currentStart, range.endInclusive + 1)
        }

        // 添加最后一段空闲时间
        if (currentStart < endTimestamp) {
            freeTimeRanges.add(currentStart until endTimestamp)
        }

        // Convert to ArrayList<LongArray>
        val serializableRanges = ArrayList<LongArray>()
        freeTimeRanges.forEach { range ->
            serializableRanges.add(longArrayOf(range.start, range.endInclusive))
        }
        return freeTimeRanges
    }


    private fun drawFreeTimeRanges(freeTimeRanges: List<LongRange>) {
        Log.d("GroupViewFragment", "Drawing free time ranges...")
        // 获取当前周的开始时间戳（假设 weekTimestamp 已经给定）
        val weekStartDateTime = DateTime(mWeekTimestamp * 1000L, DateTimeZone.getDefault())
        Log.d("GroupViewFragment", "Week start: ${formatTimestamp(weekStartDateTime.millis)}")

        // 遍历一周的每一天
        for (colIndex in 0..6) {
            // 计算当前列的日期
            val currDateTime = weekStartDateTime.plusDays(colIndex)
            Log.d("GroupViewFragment", "currDateTime for column $colIndex: $currDateTime")

            val dayStart = currDateTime.millis
            val dayEnd = currDateTime.plusDays(1).minusMillis(1).millis
            Log.d("GroupViewFragment", "Day Start: ${formatTimestamp(dayStart)}, Day End: ${formatTimestamp(dayEnd)}")

            // 找到当天的所有空闲时间范围
            val dailyFreeRanges = freeTimeRanges.filter { range ->
                range.first < dayEnd && range.last >= dayStart
            }.map { range ->
                maxOf(range.first, dayStart)..minOf(range.last, dayEnd)
            }

            Log.d("GroupViewFragment", "Column $colIndex: Free ranges for the day: ${formatTimesRange(dailyFreeRanges)}")

            // 获取该列的视图，并绘制空闲时间方块
            val column = weekColumns[colIndex] ?: continue
            for (freeRange in dailyFreeRanges) {
                drawFreeTimeSquare(freeRange, column)
            }
        }
    }



    private fun drawFreeTimeSquare(freeRange: LongRange, mLayout: ViewGroup) {
        val oneMinHeight = resources.getDimension(R.dimen.weekly_view_one_minute_height)

        val stDateTime = DateTime(freeRange.first, DateTimeZone.getDefault())
        val edDateTime = DateTime(freeRange.last, DateTimeZone.getDefault())

        val stMinutes = stDateTime.minuteOfDay
        val edMinutes = edDateTime.minuteOfDay

        // 修复类型不匹配问题
        val stMinuteHeight = (stMinutes * oneMinHeight)
        val durationHeight = ((edMinutes - stMinutes) * oneMinHeight).toInt()

        (LayoutInflater.from(context)
            .inflate(R.layout.item_week_event_square, null, false) as TextView).apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.free_time_color))
            text = "Free"

            mLayout.addView(this)
            (layoutParams as RelativeLayout.LayoutParams).apply {
                width = mLayout.width - 1
                y = stMinuteHeight
                height = durationHeight
            }
        }
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

    private fun clearViewGroup() {
        weekColumns.forEach { column ->
            column?.removeAllViews()
        }
    }
    // 将时间戳转换为日期时间字符串的方法
    private fun formatTimestamp(timestamp: Long): String {
        return DateTime(timestamp, DateTimeZone.getDefault()).toString("yyyy-MM-dd HH:mm:ss")
    }
    // 假设 formatTimesRange 用于格式化时间范围
    private fun formatTimesRange(ranges: List<LongRange>): String {
        return ranges.joinToString(", ") { range ->
            val startTime = DateTime(range.start, DateTimeZone.getDefault())
            val endTime = DateTime(range.endInclusive, DateTimeZone.getDefault())

            // 格式化为年月日 时:分 格式
            val formattedStart = startTime.toString("yyyy-MM-dd HH:mm")
            val formattedEnd = endTime.toString("yyyy-MM-dd HH:mm")

            "$formattedStart - $formattedEnd"
        }
    }
    //滑动监听类
    interface WeekScrollListener{
        fun scrollTo(y:Int)
    }

    //设置监听
    fun setListener(listener: WeekScrollListener) {
        mListener = listener
    }

    //页面上下滑动
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
    override fun onResume() {
        super.onResume()
        updateEventsView()
    }

}
