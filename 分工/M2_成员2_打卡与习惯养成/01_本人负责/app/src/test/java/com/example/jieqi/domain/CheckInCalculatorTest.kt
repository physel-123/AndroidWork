package com.example.jieqi.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * 打卡统计逻辑单元测试：连续天数、最长纪录、等级与日历排版。
 */
class CheckInCalculatorTest {

    private val today = LocalDate.of(2026, 3, 10)

    @Test
    fun `今天已打卡时连续天数从今天起算`() {
        val dates = setOf(
            today,
            today.minusDays(1),
            today.minusDays(2)
        )
        assertEquals(3, CheckInCalculator.currentStreak(dates, today))
    }

    @Test
    fun `今天未打卡但昨天打过时连续天数不清零`() {
        val dates = setOf(today.minusDays(1), today.minusDays(2))
        assertEquals(2, CheckInCalculator.currentStreak(dates, today))
    }

    @Test
    fun `中断两天以上时连续天数归零`() {
        val dates = setOf(today.minusDays(2), today.minusDays(3))
        assertEquals(0, CheckInCalculator.currentStreak(dates, today))
    }

    @Test
    fun `空集合连续天数为零`() {
        assertEquals(0, CheckInCalculator.currentStreak(emptySet(), today))
        assertEquals(0, CheckInCalculator.longestStreak(emptySet()))
    }

    @Test
    fun `最长连续天数取历史最大值`() {
        val dates = setOf(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 2),
            LocalDate.of(2026, 1, 3),
            LocalDate.of(2026, 1, 4),
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 2)
        )
        assertEquals(4, CheckInCalculator.longestStreak(dates))
    }

    @Test
    fun `等级随累计天数提升`() {
        assertEquals("初识节气", CheckInCalculator.levelOf(0))
        assertEquals("初识节气", CheckInCalculator.levelOf(2))
        assertEquals("节气新手", CheckInCalculator.levelOf(3))
        assertEquals("节气学徒", CheckInCalculator.levelOf(7))
        assertEquals("民俗达人", CheckInCalculator.levelOf(20))
        assertEquals("非遗传习人", CheckInCalculator.levelOf(60))
        assertEquals("二十四节气大家", CheckInCalculator.levelOf(365))
    }

    @Test
    fun `统计结果包含本月与已点亮节气数`() {
        val dates = setOf(today, today.minusDays(1), LocalDate.of(2026, 2, 20))
        val stats = CheckInCalculator.stats(dates, setOf(1, 2), today)
        assertEquals(3, stats.total)
        assertEquals(2, stats.thisMonth)
        assertEquals(2, stats.visitedTerms)
        assertEquals(2, stats.currentStreak)
    }

    @Test
    fun `月历格子数量为 7 的整数倍且包含当月全部日期`() {
        val cells = CheckInCalculator.monthGrid(2026, 3)
        assertEquals(0, cells.size % 7)
        val days = cells.filterNotNull()
        assertEquals(31, days.size)
        assertEquals(LocalDate.of(2026, 3, 1), days.first())
        assertEquals(LocalDate.of(2026, 3, 31), days.last())
        assertTrue(CheckInCalculator.isWeekend(LocalDate.of(2026, 3, 7)))
    }
}
