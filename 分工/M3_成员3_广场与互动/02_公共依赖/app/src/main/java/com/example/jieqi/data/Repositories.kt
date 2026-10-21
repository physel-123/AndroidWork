package com.example.jieqi.data

import android.content.Context
import com.example.jieqi.data.content.SolarTerm
import com.example.jieqi.data.content.SolarTermContent
import com.example.jieqi.data.db.CheckInEntity
import com.example.jieqi.data.db.FavoriteEntity
import com.example.jieqi.data.db.JieQiDatabase
import com.example.jieqi.domain.CheckInCalculator
import com.example.jieqi.domain.CheckInStats
import com.example.jieqi.domain.TermDateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * 打卡数据仓库：把 Room 数据流转换成界面直接可用的模型。
 */
class CheckInRepository(private val db: JieQiDatabase) {

    private val dao = db.checkInDao()

    /** 全部打卡日期（升序）。 */
    val dates: Flow<List<LocalDate>> = dao.observeAll().map { list ->
        list.mapNotNull { TermDateUtils.fromDbDate(it.date) }
    }

    /** 全部打卡记录（按日期倒序，含备注）。 */
    val records: Flow<List<CheckInEntity>> = dao.observeAll()

    /** 已点亮（打卡过）的节气序号。 */
    val visitedTermIndexes: Flow<Set<Int>> = dao.observeVisitedTermIndexes().map { it.toSet() }

    /**
     * 实时统计：打卡天数、连续天数、等级、已点亮节气数。
     */
    val stats: Flow<CheckInStats> = combine(dates, visitedTermIndexes) { dateList, terms ->
        val set = dateList.toSet()
        // 以“历史上最后一次打卡的日期”为统计基准，避免跨天时统计口径漂移
        val base = set.maxOrNull() ?: LocalDate.now()
        CheckInCalculator.stats(set, terms, base)
    }

    /** 今天是否已打卡。 */
    fun observeCheckedIn(date: LocalDate = LocalDate.now()): Flow<Boolean> =
        dao.observeAll().map { list -> list.any { it.date == TermDateUtils.toDbDate(date) } }

    /**
     * 今日打卡；返回 true 表示打卡成功，false 表示今天已经打过。
     */
    suspend fun checkIn(date: LocalDate = LocalDate.now(), note: String? = null): Boolean {
        val key = TermDateUtils.toDbDate(date)
        if (dao.findByDate(key) != null) return false
        val occurrence = TermDateUtils.currentOccurrence(date)
        dao.insert(
            CheckInEntity(
                date = key,
                termIndex = occurrence.index,
                termName = occurrence.name,
                note = note?.takeIf { it.isNotBlank() }
            )
        )
        return true
    }

    /** 取消打卡（用户误操作时使用）。 */
    suspend fun cancel(date: LocalDate) {
        val key = TermDateUtils.toDbDate(date)
        dao.findByDate(key)?.let { dao.delete(it) }
    }

    suspend fun clearAll() = dao.clear()

    /** 导出为简易 CSV 文本（“我的”页面数据导出功能）。 */
    suspend fun exportCsv(): String {
        val all = dao.getAll()
        val header = "日期,节气,备注"
        return (listOf(header) + all.map { "${it.date},${it.termName},${it.note ?: ""}" })
            .joinToString("\n")
    }
}

/**
 * 收藏仓库：节气与民俗共用一张表。
 */
class FavoriteRepository(db: JieQiDatabase) {

    private val dao = db.favoriteDao()

    val all: Flow<List<FavoriteEntity>> = dao.observeAll()

    val count: Flow<Int> = dao.observeCount()

    fun isFavorite(key: String): Flow<Boolean> = dao.observeExists(key)

    suspend fun toggleTerm(term: SolarTerm): Boolean {
        val key = termKey(term)
        return if (dao.exists(key)) {
            dao.deleteByKey(key)
            false
        } else {
            dao.insert(
                FavoriteEntity(
                    key = key,
                    type = "term",
                    title = term.name,
                    subtitle = term.summary,
                    refId = term.index
                )
            )
            true
        }
    }

    suspend fun toggleCustom(id: Long, title: String, subtitle: String): Boolean {
        val key = "custom:$id"
        return if (dao.exists(key)) {
            dao.deleteByKey(key)
            false
        } else {
            dao.insert(
                FavoriteEntity(
                    key = key,
                    type = "custom",
                    title = title,
                    subtitle = subtitle,
                    refId = id.toInt()
                )
            )
            true
        }
    }

    suspend fun remove(key: String) = dao.deleteByKey(key)

    suspend fun clearAll() = dao.clear()

    companion object {
        fun termKey(term: SolarTerm): String = "term:${term.name}"

        /** 收藏列表 → 节气对象（用于收藏页跳转）。 */
        fun termOf(favorite: FavoriteEntity): SolarTerm? =
            if (favorite.type == "term") SolarTermContent.byName(favorite.title) else null
    }
}
