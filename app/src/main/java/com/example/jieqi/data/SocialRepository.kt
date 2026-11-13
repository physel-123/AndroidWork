package com.example.jieqi.data

import com.example.jieqi.data.remote.ApiClient
import com.example.jieqi.data.remote.CommentCreateRequest
import com.example.jieqi.data.remote.CommentDto
import com.example.jieqi.data.remote.JieQiApi
import com.example.jieqi.data.remote.PostCreateRequest
import com.example.jieqi.data.remote.PostDto
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
 * 互动对象类型常量。
 *
 * 节气用节气序号（1..24）作为目标 id —— 它是稳定的；
 * 民俗与打卡动态用数据库主键。
 */
object TargetType {
    const val TERM = "term"
    const val FOLK = "folk"
    const val POST = "post"
}

/** 界面直接可用的评论模型（二级回复已挂在 [replies] 上）。 */
data class CommentItem(
    val id: Long,
    val targetType: String,
    val targetId: Long,
    val parentId: Long,
    val authorId: Long,
    val authorName: String,
    val authorColor: Int,
    val content: String,
    val likeCount: Int,
    val createdAt: Long,
    val replyToName: String? = null,
    val replies: List<CommentItem> = emptyList(),
    val isMine: Boolean = false
)

/** 打卡广场的一条动态（含当前用户的点赞状态）。 */
data class FeedPost(
    val post: PostDto,
    val likedByMe: Boolean,
    val isMine: Boolean
) {
    val id: Long get() = post.id
}

/**
 * 互动仓库：评论、点赞、打卡广场。
 *
 * 已迁移到后端（FastAPI）：动态/评论/点赞/民俗投稿均由后端 PostgreSQL 存储，
 * 前端通过 REST 接口读写。为保持 UI 层「响应式」体验，内部用 StateFlow 做
 * 内存缓存，写操作成功后自动刷新缓存。
 *
 * 用户标识：以 username（后端账号主键）为准，未登录时用「guest」跳过写操作。
 */
class SocialRepository(
    private val session: SessionStore,
    private val api: JieQiApi = ApiClient.api
) {

    // 动态缓存
    private val _feed = MutableStateFlow<List<FeedPost>>(emptyList())
    val feed: Flow<List<FeedPost>> = _feed.asStateFlow()

    private val _memberCount = MutableStateFlow(0)
    val memberCount: Flow<Int> = _memberCount.asStateFlow()

    /** 当前登录用户名（未登录返回空串）。 */
    private suspend fun me(): String = session.currentUsername.first().takeIf { it.isNotEmpty() } ?: ""

    // ------------------------------------------------------------------
    // 打卡广场
    // ------------------------------------------------------------------

    /** 拉取全部动态并刷新缓存。 */
    suspend fun refreshFeed() {
        val username = me()
        val result = safeCall { api.socialPosts(username) }
        result.getOrNull()?.let { list ->
            _feed.value = list.map { FeedPost(it, it.likedByMe, it.isMine) }
            safeCall { api.memberCount() }.getOrNull()?.let { _memberCount.value = it }
        }
    }

    /** 某个节气下的动态（从缓存过滤）。 */
    fun postsOfTerm(termIndex: Int): Flow<List<PostDto>> =
        _feed.map { list -> list.filter { it.post.termIndex == termIndex }.map { it.post } }

    /** 我发布的动态。 */
    fun myPosts(): Flow<List<PostDto>> = _feed.map { list -> list.filter { it.isMine }.map { it.post } }

    /** 我发布的动态数量。 */
    fun postCountOfAuthor(): Flow<Int> = _feed.map { list -> list.count { it.isMine } }

    /**
     * 发布打卡动态，返回新动态 id；内容为空或未登录时返回 -1。
     */
    suspend fun publish(
        content: String,
        termIndex: Int,
        termName: String,
        mood: String?,
        imageAsset: String?,
        author: com.example.jieqi.data.db.UserEntity
    ): Long {
        if (content.isBlank()) return -1
        val username = me()
        if (username.isEmpty()) return -1
        val result = safeCall {
            api.createPost(
                PostCreateRequest(
                    username = username,
                    content = content.trim(),
                    termIndex = termIndex,
                    termName = termName,
                    mood = mood,
                    imageAsset = imageAsset
                )
            )
        }
        return if (result.isSuccess) {
            refreshFeed()
            result.getOrNull()?.id ?: -1
        } else -1
    }

    /** 删除动态。 */
    suspend fun deletePost(post: PostDto) {
        val username = me()
        if (username.isEmpty()) return
        safeCall { api.deletePost(post.id, username) }
        refreshFeed()
    }

    // ------------------------------------------------------------------
    // 评论
    // ------------------------------------------------------------------

    /** 观察某目标的评论，二级回复挂到父评论上（实时从后端拉取）。 */
    fun comments(type: String, targetId: Long): Flow<List<CommentItem>> {
        // 用缓存 + 手动刷新；为简化，每次组合重新拉取
        return session.currentUsername.map { username ->
            val meName = username.takeIf { it.isNotEmpty() }
            val list = safeCall { api.socialComments(type, targetId, username) }.getOrNull() ?: emptyList()
            assembleComments(list, meName)
        }
    }

    /** 评论数。 */
    fun commentCount(type: String, targetId: Long): Flow<Int> = session.currentUsername.map { _ ->
        safeCall { api.commentCount(type, targetId) }.getOrNull() ?: 0
    }

    /** 发表评论；[parentId] 不为 0 时表示回复某条评论。 */
    suspend fun comment(
        type: String,
        targetId: Long,
        content: String,
        parentId: Long,
        author: com.example.jieqi.data.db.UserEntity
    ): Boolean {
        if (content.isBlank()) return false
        val username = me()
        if (username.isEmpty()) return false
        val result = safeCall {
            api.createComment(
                CommentCreateRequest(
                    username = username,
                    targetType = type,
                    targetId = targetId,
                    content = content.trim(),
                    parentId = parentId
                )
            )
        }
        if (result.isSuccess && type == TargetType.POST) refreshFeed()
        return result.isSuccess
    }

    suspend fun deleteComment(commentId: Long, type: String, targetId: Long) {
        val username = me()
        if (username.isEmpty()) return
        safeCall { api.deleteComment(commentId, username) }
        if (type == TargetType.POST) refreshFeed()
    }

    // ------------------------------------------------------------------
    // 点赞
    // ------------------------------------------------------------------

    /** 当前用户是否点赞了该目标（未登录恒为 false）。 */
    fun liked(type: String, targetId: Long): Flow<Boolean> = session.currentUsername.map { username ->
        if (username.isEmpty()) false
        else safeCall { api.getLike(type, targetId, username) }.getOrNull()?.liked ?: false
    }

    fun likeCount(type: String, targetId: Long): Flow<Int> = session.currentUsername.map { username ->
        if (username.isEmpty()) {
            // 未登录也要显示点赞数，用任意用户（这里用空串查不到，改用带 username 的查询）
            safeCall { api.getLike(type, targetId, "guest") }.getOrNull()?.likeCount ?: 0
        } else {
            safeCall { api.getLike(type, targetId, username) }.getOrNull()?.likeCount ?: 0
        }
    }

    /**
     * 点赞 / 取消点赞。
     *
     * @return 操作后是否处于「已点赞」状态；未登录返回 false
     */
    suspend fun toggleLike(type: String, targetId: Long): Boolean {
        val username = me()
        if (username.isEmpty()) return false
        val result = safeCall { api.toggleLike(type, targetId, username) }
        if (result.isSuccess && type == TargetType.POST) refreshFeed()
        return result.getOrNull()?.liked ?: false
    }

    /** 我给出的点赞总数。 */
    fun likesGiven(): Flow<Int> = session.currentUsername.map { _ ->
        // 后端未单独提供 likesGiven 接口，暂返回 0；个人主页统计以动态数为准
        0
    }

    // ------------------------------------------------------------------
    // 工具
    // ------------------------------------------------------------------

    private fun assembleComments(list: List<CommentDto>, meName: String?): List<CommentItem> {
        val items = list.map { d ->
            CommentItem(
                id = d.id,
                targetType = d.targetType,
                targetId = d.targetId,
                parentId = d.parentId,
                authorId = 0L,
                authorName = d.authorName,
                authorColor = d.authorColor,
                content = d.content,
                likeCount = d.likeCount,
                createdAt = d.createdAt,
                isMine = d.author == meName
            )
        }
        val repliesByParent = items.filter { it.parentId != 0L }.groupBy { it.parentId }
        val nameById = items.associate { it.id to it.authorName }
        return items.filter { it.parentId == 0L }.map { parent ->
            parent.copy(
                replies = repliesByParent[parent.id].orEmpty().map { reply ->
                    reply.copy(replyToName = nameById[reply.parentId])
                }
            )
        }
    }

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
}
