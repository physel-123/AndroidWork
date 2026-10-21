package com.example.jieqi.domain

import com.example.jieqi.data.content.SolarTerm
import com.example.jieqi.data.content.SolarTermContent
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 一次具体的节气发生（某年某节气 + 其精确时刻）。
 *
 * @param term 节气内容对象
 * @param date 节气所在公历日期（东八区）
 * @param dateTime 节气精确时刻（东八区），如 2026-02-04 04:01
 */
data class TermOccurrence(
    val term: SolarTerm,
    val date: LocalDate,
    val dateTime: LocalDateTime
) {
    val name: String get() = term.name
    val index: Int get() = term.index
}

/**
 * 节气相关的日期工具与业务计算。
 *
 * 首页的“当前节气 / 距离下一节气”等逻辑都集中在这里，
 * 计算量较大（一次要解 24 个天文方程），因此按年份做内存缓存。
 */
object TermDateUtils {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")

    private val cache = mutableMapOf<Int, List<TermOccurrence>>()

    /** 某一公历年内 24 个节气（按时间先后，从立春到大寒）。 */
    @Synchronized
    fun occurrencesOfYear(year: Int): List<TermOccurrence> = cache.getOrPut(year) {
        (1..24).map { index ->
            val term = SolarTermContent.byIndex(index) ?: error("节气 $index 不存在")
            TermOccurrence(
                term = term,
                date = SolarTermCalculator.termDate(year, index),
                dateTime = SolarTermCalculator.termDateTime(year, index)
            )
        }
    }

    /**
     * 把「去年 + 今年 + 明年」三年的节气拼成一条**按时间排序**的时间线。
     *
     * 必须重新排序：单个年份的 24 个节气是“立春→大寒”的顺序，
     * 而小寒、大寒落在该公历年的 1 月，因此直接拼接后 year+1 的 1 月节气
     * 会排在末尾，导致“当前节气”被误判为大寒（这是本项目实际踩到过的 bug）。
     */
    private fun timelineAround(year: Int): List<TermOccurrence> =
        (occurrencesOfYear(year - 1) + occurrencesOfYear(year) + occurrencesOfYear(year + 1))
            .sortedBy { it.dateTime }

    /**
     * 以某一天（按整天）为基准的“当前节气”。
     *
     * 适用于“打卡归属节气”这类按日期归档的场景：
     * 节气当天无论几点，都算作进入了新节气。
     */
    fun currentOccurrence(date: LocalDate): TermOccurrence {
        val timeline = timelineAround(date.year)
        return timeline.lastOrNull { !it.date.isAfter(date) } ?: timeline.first()
    }

    /**
     * 以某一时刻为基准的“当前节气”。
     *
     * 首页展示必须用这个版本：节气当天，在**精确时刻到达之前**，
     * 当前节气仍是上一个节气（否则会出现“当前节气 == 下一个节气”的矛盾）。
     */
    fun currentOccurrence(dateTime: LocalDateTime): TermOccurrence {
        val timeline = timelineAround(dateTime.year)
        return timeline.lastOrNull { !it.dateTime.isAfter(dateTime) } ?: timeline.first()
    }

    /** 下一个节气（含精确时刻与倒计时）。 */
    fun nextOccurrence(date: LocalDate, now: LocalTime = LocalTime.now()): TermOccurrence {
        val timeline = timelineAround(date.year)
        val nowDateTime = LocalDateTime.of(date, now)
        return timeline.firstOrNull { it.dateTime.isAfter(nowDateTime) } ?: timeline.last()
    }

    /** 距离下一个节气还有多少天（按日期差，首页展示用）。 */
    fun daysToNext(date: LocalDate): Long =
        Duration.between(date.atStartOfDay(), nextOccurrence(date).date.atStartOfDay()).toDays()

    /** 距离下一个节气的精细倒计时文案，例如“还有 3 天 05 小时”。 */
    fun countdownText(now: LocalDateTime = LocalDateTime.now()): String {
        val next = nextOccurrence(now.toLocalDate(), now.toLocalTime())
        val duration = Duration.between(now, next.dateTime)
        if (duration.isNegative) return "就在今天"
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        return when {
            days > 0 -> "还有 $days 天 $hours 小时"
            hours > 0 -> "还有 $hours 小时 $minutes 分钟"
            else -> "还有 $minutes 分钟"
        }
    }

    /** 判断某天是否是节气日（用于日历高亮）。 */
    fun termOn(date: LocalDate): TermOccurrence? =
        occurrencesOfYear(date.year).firstOrNull { it.date == date }

    /** 全年的节气日期表，节气页“节气时间表”使用。 */
    fun yearTable(year: Int): List<TermOccurrence> = occurrencesOfYear(year)

    /** 节气日期的文案，如“2026年2月4日”。 */
    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    /** “2月4日 04:01”形式的时刻文案。 */
    fun formatDateTime(dateTime: LocalDateTime): String =
        "${dateTime.monthValue}月${dateTime.dayOfMonth}日 %02d:%02d".format(dateTime.hour, dateTime.minute)

    /** 把 LocalDate 转成数据库使用的 yyyy-MM-dd。 */
    fun toDbDate(date: LocalDate): String = date.toString()

    /** 由 yyyy-MM-dd 解析回 LocalDate，失败返回 null。 */
    fun fromDbDate(text: String): LocalDate? = runCatching { LocalDate.parse(text) }.getOrNull()

    /** 物候描述：根据月份给出三候里的第几候（每候 5 天）。 */
    fun phenologyOf(occurrence: TermOccurrence, date: LocalDate): String? {
        val days = Duration.between(occurrence.date.atStartOfDay(), date.atStartOfDay()).toDays()
        val stage = (days / 5).toInt().coerceIn(0, 2)
        return occurrence.term.phenology.getOrNull(stage)
    }
}

