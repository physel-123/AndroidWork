package com.example.jieqi.domain

import com.example.jieqi.data.content.SolarTermContent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 打卡统计结果。
 *
 * @param total 累计打卡天数
 * @param currentStreak 当前连续打卡天数（今天或昨天有打卡则延续）
 * @param longestStreak 历史最长连续打卡天数
 * @param thisMonth 本月打卡天数
 * @param visitedTerms 已“点亮”的节气数量
 * @param level 文化传习等级
 */
data class CheckInStats(
    val total: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val thisMonth: Int = 0,
    val visitedTerms: Int = 0,
    val level: String = "初识节气"
)

/**
 * 打卡相关业务计算。
 *
 * 连续天数、等级等纯逻辑放在这里，便于单元测试（见 test 目录）。
 */
object CheckInCalculator {

    /** 文化传习等级门槛：累计打卡天数 → 等级名称。 */
    private val LEVELS = listOf(
        0 to "初识节气",
        3 to "节气新手",
        7 to "节气学徒",
        15 to "民俗达人",
        30 to "节气守护者",
        60 to "非遗传习人",
        100 to "二十四节气大家"
    )

    fun levelOf(total: Int): String =
        LEVELS.lastOrNull { total >= it.first }?.second ?: LEVELS.first().second

    /**
     * 计算当前连续打卡天数。
     *
     * 规则：从今天开始往前数；若今天还没打卡，则允许从昨天开始数
     * （这样“昨天打过、今天还没打”时连续天数不会被清零，符合用户直觉）。
     */
    fun currentStreak(dates: Set<LocalDate>, today: LocalDate): Int {
        if (dates.isEmpty()) return 0
        var cursor = if (dates.contains(today)) today else today.minusDays(1)
        if (!dates.contains(cursor)) return 0
        var streak = 0
        while (dates.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    /** 计算历史最长连续打卡天数。 */
    fun longestStreak(dates: Set<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        val sorted = dates.sorted()
        var best = 1
        var run = 1
        for (i in 1 until sorted.size) {
            run = if (sorted[i] == sorted[i - 1].plusDays(1)) run + 1 else 1
            if (run > best) best = run
        }
        return best
    }

    /** 汇总统计。 */
    fun stats(
        dates: Set<LocalDate>,
        visitedTerms: Set<Int>,
        today: LocalDate = LocalDate.now()
    ): CheckInStats {
        val monthCount = dates.count { it.year == today.year && it.monthValue == today.monthValue }
        return CheckInStats(
            total = dates.size,
            currentStreak = currentStreak(dates, today),
            longestStreak = longestStreak(dates),
            thisMonth = monthCount,
            visitedTerms = visitedTerms.size,
            level = levelOf(dates.size)
        )
    }

    /** 某个月用于展示的日期格子（含前置空白，便于按周排版）。 */
    fun monthGrid(year: Int, month: Int): List<LocalDate?> {
        val first = LocalDate.of(year, month, 1)
        val length = first.lengthOfMonth()
        val leading = (first.dayOfWeek.value) % 7 // 周日为一周起点时前置空格数
        val cells = mutableListOf<LocalDate?>()
        repeat(leading) { cells += null }
        for (day in 1..length) cells += LocalDate.of(year, month, day)
        while (cells.size % 7 != 0) cells += null
        return cells
    }

    /** 星期标题。 */
    val WEEK_LABELS = listOf("日", "一", "二", "三", "四", "五", "六")

    private val noteFormatter = DateTimeFormatter.ofPattern("M月d日")

    fun labelOf(date: LocalDate): String = date.format(noteFormatter)

    /** 判断某一天是否属于周末（展示时用弱化颜色）。 */
    fun isWeekend(date: LocalDate): Boolean =
        date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    /** 节气是否已“点亮”（打卡过该节气区间）。 */
    fun visitedTermIndexes(dates: Set<LocalDate>): Set<Int> =
        dates.mapNotNull { date ->
            runCatching { TermDateUtils.currentOccurrence(date).index }.getOrNull()
        }.toSet()

    /** 未点亮的节气，用于“点亮二十四节气”进度展示。 */
    fun lockedTerms(visited: Set<Int>): List<Int> =
        (1..24).filterNot { visited.contains(it) }

    /** 某节气对应的内容名称，供界面展示。 */
    fun termNameOf(index: Int): String =
        SolarTermContent.byIndex(index)?.name ?: "第 $index 个节气"
}
