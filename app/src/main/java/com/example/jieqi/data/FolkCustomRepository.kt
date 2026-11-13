package com.example.jieqi.data

import com.example.jieqi.data.content.FolkCustomContent
import com.example.jieqi.data.db.FolkCustomEntity
import com.example.jieqi.data.db.JieQiDatabase
import com.example.jieqi.data.remote.ApiClient
import com.example.jieqi.data.remote.FolkCustomCreateRequest
import com.example.jieqi.data.remote.FolkCustomDto
import com.example.jieqi.data.remote.JieQiApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * 地方民俗仓库：
 * - 内置民俗数据（34 条）仍存本地内容库，离线可看；
 * - 用户投稿「我家的民俗」已迁移到后端（PostgreSQL），前端从后端拉取并合并展示。
 */
class FolkCustomRepository(
    private val db: JieQiDatabase,
    private val session: SessionStore,
    private val api: JieQiApi = ApiClient.api
) {

    private val dao = db.folkCustomDao()

    /** 后端用户投稿缓存。 */
    private val _contributions = MutableStateFlow<List<FolkCustomEntity>>(emptyList())
    private val contributions: Flow<List<FolkCustomEntity>> = _contributions.asStateFlow()

    /** 全部民俗：内置（本地）+ 用户投稿（后端）。 */
    val all: Flow<List<FolkCustomEntity>> = combine(
        dao.observeAll(), contributions
    ) { builtin, contributed -> builtin + contributed }

    /** 用户投稿数量。 */
    val userContributionCount: Flow<Int> = contributions.map { it.size }

    private suspend fun me(): String = session.currentUsername.first().takeIf { it.isNotEmpty() } ?: ""

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

    /** 拉取后端用户投稿并刷新缓存。 */
    suspend fun refreshContributions() {
        val result = safeCall { api.folkCustoms() }
        result.getOrNull()?.let { list ->
            _contributions.value = list.map { it.toEntity() }
        }
    }

    /** 投稿「我家的民俗」，走后端。 */
    suspend fun addUserCustom(custom: FolkCustomEntity): Long {
        val username = me()
        if (username.isEmpty()) return -1
        val result = safeCall {
            api.createFolkCustom(
                FolkCustomCreateRequest(
                    username = username,
                    title = custom.title,
                    termName = custom.termName,
                    province = custom.province,
                    city = custom.city,
                    latitude = custom.latitude,
                    longitude = custom.longitude,
                    description = custom.description
                )
            )
        }
        if (result.isSuccess) refreshContributions()
        return result.getOrNull()?.id ?: -1
    }

    /** 删除投稿（仅本人）。 */
    suspend fun delete(custom: FolkCustomEntity) {
        if (custom.isBuiltIn) return  // 内置数据不允许删除
        val username = me()
        if (username.isEmpty()) return
        // 通过贡献者信息判断；后端按 username 校验
        safeCall { api.deleteFolkCustom(custom.id, username) }
        refreshContributions()
    }

    suspend fun count(): Int = dao.count() + _contributions.value.size

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

    private fun FolkCustomDto.toEntity(): FolkCustomEntity = FolkCustomEntity(
        id = id,
        title = title,
        termName = termName,
        province = province,
        city = city,
        latitude = latitude,
        longitude = longitude,
        description = description,
        isBuiltIn = false,
        createdAt = createdAt
    )

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(block())
            } catch (e: HttpException) {
                Result.failure(Exception("服务返回错误（${e.code()}）"))
            } catch (e: IOException) {
                Result.failure(Exception("网络连接失败"))
            } catch (e: Exception) {
                Result.failure(Exception(e.message ?: "请求失败"))
            }
        }

    companion object {
        /** 全部内置省份，供筛选下拉使用。 */
        val allProvinces: List<String> get() = FolkCustomContent.provinces
    }
}
