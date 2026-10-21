package com.example.jieqi.data.content

import com.example.jieqi.domain.TermDateUtils
import com.example.jieqi.domain.TermOccurrence
import java.time.LocalDate

/**
 * 每日内容工厂：把节气知识组合成“每日一句 / 每日一俗 / 每日一食”等卡片内容。
 *
 * 采用“当天日期 + 节气序号”做轮换下标，保证同一天多次进入 App 内容稳定，
 * 不同日期看到的习俗与食俗又会自然轮换，形成每日更新的感觉。
 */
object DailyContentFactory {

    /** 每日一句：节气释义中的第一句。 */
    fun dailySentence(occurrence: TermOccurrence): String =
        occurrence.term.summary

    /** 每日一俗：从习俗列表中轮换取一条。 */
    fun dailyCustom(occurrence: TermOccurrence, date: LocalDate = LocalDate.now()): String {
        val list = occurrence.term.customs
        if (list.isEmpty()) return ""
        return list[(date.dayOfYear + occurrence.index) % list.size]
    }

    /** 每日一食：从食俗列表中轮换取一条。 */
    fun dailyFood(occurrence: TermOccurrence, date: LocalDate = LocalDate.now()): String {
        val list = occurrence.term.foods
        if (list.isEmpty()) return ""
        return list[(date.dayOfYear + occurrence.index) % list.size]
    }

    /** 今日养生提示。 */
    fun healthTip(occurrence: TermOccurrence): String = occurrence.term.health

    /** 首页顶部问候语，按时间段变化。 */
    fun greeting(hour: Int): String = when (hour) {
        in 5..8 -> "晨光正好，宜读一节"
        in 9..11 -> "日上三竿，顺时而行"
        in 12..13 -> "午间小憩，静心养神"
        in 14..17 -> "午后时光，且听风吟"
        in 18..22 -> "夜色初上，温故知新"
        else -> "夜深气沉，早卧养阳"
    }

    /**
     * 打卡分享文案：用于系统分享给好友/群。
     */
    fun shareCheckInText(
        date: LocalDate,
        occurrence: TermOccurrence,
        streak: Int,
        note: String?
    ): String = buildString {
        appendLine("【节气民俗 · 打卡记】")
        appendLine("${TermDateUtils.formatDate(date)}　今日${occurrence.name}")
        appendLine("「${occurrence.term.summary}」")
        appendLine("今日习俗：${dailyCustom(occurrence, date)}")
        appendLine("时令食俗：${dailyFood(occurrence, date)}")
        if (!note.isNullOrBlank()) appendLine("我的心情：$note")
        if (streak > 0) appendLine("已连续打卡 $streak 天，跟我一起跟着节气过日子。")
        append("#二十四节气 #传统文化 #节气民俗")
    }

    /** 单个节气的分享文案。 */
    fun shareTermText(occurrence: TermOccurrence): String = buildString {
        appendLine("【二十四节气 · ${occurrence.name}】")
        appendLine("时间：${TermDateUtils.formatDateTime(occurrence.dateTime)}")
        appendLine("释义：${occurrence.term.meaning}")
        appendLine("三候：${occurrence.term.phenology.joinToString(" / ")}")
        appendLine("习俗：${occurrence.term.customs.take(2).joinToString("；")}")
        appendLine("食俗：${occurrence.term.foods.joinToString("、")}")
        appendLine("${occurrence.term.poem}——${occurrence.term.poemFrom}")
        append("#二十四节气 #${occurrence.name}")
    }

    /** 民俗条目分享文案。 */
    fun shareCustomText(title: String, region: String, termName: String?, description: String): String =
        buildString {
            appendLine("【地方民俗 · $title】")
            appendLine("流传地区：$region")
            if (!termName.isNullOrBlank()) appendLine("相关节气：$termName")
            appendLine(description)
            append("#地方民俗 #传统文化")
        }
}
