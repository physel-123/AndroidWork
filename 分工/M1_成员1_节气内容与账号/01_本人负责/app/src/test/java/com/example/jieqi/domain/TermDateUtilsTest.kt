package com.example.jieqi.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * 节气日期工具单元测试。
 *
 * 这组测试是为了防止一类真实发生过的 bug：
 * 「去年 + 今年 + 明年」三组节气拼接后如果忘记排序，
 * 当前节气会被误判为次年 1 月的大寒（因为它在拼接结果里排在最后）。
 */
class TermDateUtilsTest {

    @Test
    fun `当前节气是最后一个不晚于今天的节气`() {
        // 2026-09-19：白露在 9/7 前后，秋分在 9/23 前后 → 当前应为白露
        val current = TermDateUtils.currentOccurrence(LocalDate.of(2026, 9, 19))
        assertEquals("白露", current.name)
        assertTrue(!current.date.isAfter(LocalDate.of(2026, 9, 19)))
    }

    @Test
    fun `下一个节气是第一个晚于当前时刻的节气`() {
        val date = LocalDate.of(2026, 9, 19)
        val next = TermDateUtils.nextOccurrence(date, LocalTime.of(20, 0))
        assertEquals("秋分", next.name)
        assertTrue(next.dateTime.isAfter(date.atTime(20, 0)))
    }

    @Test
    fun `当前节气不晚于该日且早于下一节气`() {
        // 遍历 2026 全年（每 7 天抽样一次），使用精确时刻版本判定
        var date = LocalDate.of(2026, 1, 1)
        while (date.year == 2026) {
            val now = date.atTime(LocalTime.NOON)
            val current = TermDateUtils.currentOccurrence(now)
            val next = TermDateUtils.nextOccurrence(date, LocalTime.NOON)
            assertTrue(
                "$date：当前 ${current.name} 应严格早于下一节气 ${next.name}",
                current.dateTime.isBefore(next.dateTime)
            )
            assertTrue(
                "$date：当前节气 ${current.name} 不应晚于当前时刻",
                !current.dateTime.isAfter(now)
            )
            date = date.plusDays(7)
        }
    }

    @Test
    fun `节气当天在精确时刻之前仍属上一个节气`() {
        // 2026 年惊蛰在 3 月 5 日，当天 00:30 时惊蛰尚未到来
        val occurrence = TermDateUtils.occurrencesOfYear(2026).first { it.name == "惊蛰" }
        val before = occurrence.dateTime.minusMinutes(30)
        val after = occurrence.dateTime.plusMinutes(30)

        assertEquals("惊蛰", TermDateUtils.currentOccurrence(after).name)
        assertEquals("雨水", TermDateUtils.currentOccurrence(before).name)
        // 按整天归档的版本则直接把当天算作惊蛰（用于打卡归属）
        assertEquals("惊蛰", TermDateUtils.currentOccurrence(occurrence.date).name)
    }

    @Test
    fun `一月的节气跨年判定正确`() {
        // 2026-01-10 处于小寒区间（小寒 1/5 前后、大寒 1/20 前后）
        val date = LocalDate.of(2026, 1, 10)
        assertEquals("小寒", TermDateUtils.currentOccurrence(date).name)
        assertEquals("大寒", TermDateUtils.nextOccurrence(date, LocalTime.NOON).name)
    }

    @Test
    fun `十二月底的下一个节气落在次年一月`() {
        val date = LocalDate.of(2026, 12, 25)
        assertEquals("冬至", TermDateUtils.currentOccurrence(date).name)
        val next = TermDateUtils.nextOccurrence(date, LocalTime.NOON)
        assertEquals("小寒", next.name)
        assertEquals(2027, next.date.year)
    }

    @Test
    fun `节气当天判定为节气日`() {
        val occurrence = TermDateUtils.occurrencesOfYear(2026).first { it.name == "立春" }
        val found = TermDateUtils.termOn(occurrence.date)
        assertEquals("立春", found?.name)
        // 非节气日应返回 null
        assertEquals(null, TermDateUtils.termOn(occurrence.date.plusDays(1)))
    }

    @Test
    fun `倒计时文案格式正确`() {
        val date = LocalDate.of(2026, 9, 19)
        val text = TermDateUtils.countdownText(date.atTime(20, 0))
        assertTrue("倒计时文案异常：$text", text.startsWith("还有"))
        // 距秋分约 3 天，应包含“天”
        assertTrue("倒计时文案异常：$text", text.contains("天"))
    }

    @Test
    fun `一年内节气数量为24且按日期排序后序号循环连续`() {
        val list = TermDateUtils.yearTable(2026)
        assertEquals(24, list.size)
        // occurrencesOfYear 按节气序号 1..24 返回（立春起、大寒止），
        // 而按“同一公历年内的日期”排序则是：小寒(1/5)、大寒(1/20)、立春(2/4) …… 冬至(12/22)。
        // 两种顺序都有用途，这里把不变量固定下来。
        assertEquals(1, list.first().index)
        assertEquals(24, list.last().index)
        assertEquals((1..24).toSet(), list.map { it.index }.toSet())

        val byDate = list.sortedBy { it.dateTime }
        assertEquals((23..24).toList() + (1..22).toList(), byDate.map { it.index })
        // 小寒、大寒落在当年 1 月，立春至冬至落在 2—12 月
        assertEquals(1, byDate.first().date.monthValue)
        assertTrue("立春至冬至应都在 2026 年", list.filter { it.index <= 22 }.all { it.date.year == 2026 })
    }
}
