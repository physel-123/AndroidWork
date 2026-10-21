package com.example.jieqi.data

import com.example.jieqi.data.content.FolkCustomContent
import com.example.jieqi.data.db.FolkCustomEntity
import com.example.jieqi.data.db.JieQiDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 地方民俗仓库：负责内置数据播种、按地域/节气查询，以及用户投稿的“我家的民俗”。
 */
class FolkCustomRepository(private val db: JieQiDatabase) {

    private val dao = db.folkCustomDao()

    val all: Flow<List<FolkCustomEntity>> = dao.observeAll()

    /** 用户投稿数量。 */
    val userContributionCount: Flow<Int> = dao.observeUserContributionCount()

    /** 按节气筛选：包含该节气专属民俗 + 全年通用民俗。 */
    fun byTerm(termName: String): Flow<List<FolkCustomEntity>> =
        all.map { list -> list.filter { it.termName == null || it.termName == termName } }

    /** 按省份筛选。 */
    fun byProvince(province: String?): Flow<List<FolkCustomEntity>> =
        all.map { list -> if (province == null) list else list.filter { it.province == province } }

    /** 民俗名 / 地区 / 简介模糊搜索。 */
    fun search(keyword: String): Flow<List<FolkCustomEntity>> = all.map { list ->
        val q = keyword.trim()
        if (q.isEmpty()) list
        else list.filter {
            it.title.contains(q) || it.city.contains(q) || it.province.contains(q) ||
                (it.termName?.contains(q) == true) || it.description.contains(q)
        }
    }

    suspend fun addUserCustom(custom: FolkCustomEntity): Long =
        dao.insert(custom.copy(isBuiltIn = false))

    suspend fun update(custom: FolkCustomEntity) = dao.update(custom)

    suspend fun delete(custom: FolkCustomEntity) {
        // 内置数据不允许删除，避免内容库被清空
        if (!custom.isBuiltIn) dao.delete(custom)
    }

    suspend fun count(): Int = dao.count()

    /**
     * 首次启动时播种内置民俗数据；已有数据则跳过。
     *
     * @return 是否执行了播种
     */
    suspend fun seedIfEmpty(): Boolean {
        if (dao.count() > 0) return false
        dao.insertAll(FolkCustomContent.all)
        return true
    }

    suspend fun clearUserContributions() = dao.clearUserContributions()

    companion object {
        /** 全部内置省份，供筛选下拉使用。 */
        val allProvinces: List<String> get() = FolkCustomContent.provinces
    }
}
