package com.app.login

import android.util.Log
import android.util.Range
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class ExampleTest {

    //@Test
    fun testTimestaps() {
        val perferredDateTimeList = listOf(
            "2024-12-10 19:00 - 2024-12-10 21:00",
            "2024-12-12 12:00 - 2024-12-12 18:00"
        )

        print(convertPerferredDateTimeListToTimestamps(perferredDateTimeList))
    }


    //@Test
    fun testExclude(){
        val sourceTimestampsList = listOf<LongRange>(
            1733720400000..1733738400000,//2024-12-09 13:00:00 -2024-12-09 18:00:00
            1733738400000..1733752800000,//2024-12-09 18:00:00 -2024-12-09 22:00:00
            1733824800000..1733839200000, //2024-12-10 18:00:00 - 2024-12-10 22:00:00
            1733997600000..1734012000000,//2024-12-12 18:00:00- 2024-12-12 22:00:00
        )
        val excludedTimestampsList = listOf<LongRange>(
            1733828400000..1733835600000,//["2024-12-10 19:00 - 2024-12-10 21:00", ]
            1733976000000..1733997600000,//"2024-12-12 12:00 - 2024-12-12 18:00"
        )
        //最后结果应该是
        //2024-12-09 13:00:00 -2024-12-09 18:00:00
        //2024-12-09 18:00:00 -2024-12-09 22:00:00
        //2024-12-10 18:00:00 - 2024-12-10 19:00:00
        //2024-12-10 21:00:00 - 2024-12-10 22:00:00
        //2024-12-12 18:00:00- 2024-12-12 22:00:00
        val timestamps = excludedTimestamps(sourceTimestampsList, excludedTimestampsList)
        print(timestamps)
    }

    /**
     * 在 sourceTimestampsList 中排除 excludedTimestampsList 中的时间段，并重新分割
     *
     * @param sourceTimestampsList 源时间段列表
     * @param excludedTimestampsList 需要排除的时间段列表
     * @return 排除后的时间段列表
     */
    fun excludedTimestamps(
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
     * 将 perferredDateTimeList 中的时间字符串转换为时间戳
     *
     * @param perferredDateTimeList 时间列表
     * @return
     */
    private fun convertPerferredDateTimeListToTimestamps(perferredDateTimeList: List<String>): List<LongRange> {
        // 定义时间格式
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        // 存储结果的列表
        val timestampList = mutableListOf<LongRange>()

        // 遍历 perferredDateTimeList
        perferredDateTimeList.forEach { timeSlot ->
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
}