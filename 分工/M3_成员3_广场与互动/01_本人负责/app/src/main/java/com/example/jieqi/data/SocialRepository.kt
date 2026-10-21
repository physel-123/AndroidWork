package com.example.jieqi.data

import android.util.Base64
import com.example.jieqi.data.db.CommentEntity
import com.example.jieqi.data.db.JieQiDatabase
import com.example.jieqi.data.db.LikeEntity
import com.example.jieqi.data.db.PostEntity
import com.example.jieqi.data.db.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.security.MessageDigest

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
    val post: PostEntity,
    val likedByMe: Boolean,
    val isMine: Boolean
) {
    val id: Long get() = post.id
}

/**
 * 互动仓库：评论、点赞、打卡广场。
 *
 * 点赞数与评论数在业务表上做冗余计数（写操作时同步维护），
 * 列表渲染就不需要每行再查一次 count。
 */
class SocialRepository(
    private val db: JieQiDatabase,
    private val session: SessionStore
) {

    private val postDao = db.postDao()
    private val commentDao = db.commentDao()
    private val likeDao = db.likeDao()
    private val userDao = db.userDao()

    // ------------------------------------------------------------------
    // 打卡广场
    // ------------------------------------------------------------------

    /** 全部动态，附带「我是否点过赞」「是否我发布的」。 */
    val feed: Flow<List<FeedPost>> = session.currentUserId.flatMapLatest { userId ->
        val likedFlow: Flow<List<Long>> =
            if (userId <= 0L) flowOf(emptyList())
            else likeDao.observeLikedIds(TargetType.POST, userId)

        combine(postDao.observeAll(), likedFlow) { posts, likedIds ->
            val likedSet = likedIds.toSet()
            posts.map { post ->
                FeedPost(
                    post = post,
                    likedByMe = likedSet.contains(post.id),
                    isMine = post.authorId == userId
                )
            }
        }
    }

    /** 某个节气下的动态。 */
    fun postsOfTerm(termIndex: Int): Flow<List<PostEntity>> = postDao.observeByTerm(termIndex)

    /** 我发布的动态。 */
    fun myPosts(userId: Long): Flow<List<PostEntity>> = postDao.observeByAuthor(userId)

    fun observePost(id: Long): Flow<PostEntity?> = postDao.observeById(id)

    fun postCountOfAuthor(userId: Long): Flow<Int> = postDao.observeCountByAuthor(userId)

    /**
     * 发布打卡动态，返回新动态 id；内容为空或未登录时返回 -1。
     */
    suspend fun publish(
        content: String,
        termIndex: Int,
        termName: String,
        mood: String?,
        imageAsset: String?,
        author: UserEntity
    ): Long {
        if (content.isBlank()) return -1
        return postDao.insert(
            PostEntity(
                authorId = author.id,
                authorName = author.nickname,
                authorColor = author.avatarColor,
                termIndex = termIndex,
                termName = termName,
                content = content.trim(),
                imageAsset = imageAsset,
                mood = mood
            )
        )
    }

    /** 删除动态（连带清理它的评论与点赞记录）。 */
    suspend fun deletePost(post: PostEntity) {
        commentDao.clearTarget(TargetType.POST, post.id)
        likeDao.clearTarget(TargetType.POST, post.id)
        postDao.deleteById(post.id)
    }

    // ------------------------------------------------------------------
    // 评论
    // ------------------------------------------------------------------

    /** 观察某目标的评论，二级回复挂到父评论上。 */
    fun comments(type: String, targetId: Long): Flow<List<CommentItem>> =
        combine(
            commentDao.observeForTarget(type, targetId),
            session.currentUserId
        ) { list, userId ->
            val items = list.map { it.toItem(userId) }
            val repliesByParent = items.filter { it.parentId != 0L }.groupBy { it.parentId }
            val nameById = items.associate { it.id to it.authorName }
            items.filter { it.parentId == 0L }.map { parent ->
                parent.copy(
                    replies = repliesByParent[parent.id].orEmpty().map { reply ->
                        reply.copy(replyToName = nameById[reply.parentId])
                    }
                )
            }
        }

    fun commentCount(type: String, targetId: Long): Flow<Int> =
        commentDao.observeCount(type, targetId)

    /** 发表评论；[parentId] 不为 0 时表示回复某条评论。 */
    suspend fun comment(
        type: String,
        targetId: Long,
        content: String,
        parentId: Long,
        author: UserEntity
    ): Boolean {
        if (content.isBlank()) return false
        commentDao.insert(
            CommentEntity(
                targetType = type,
                targetId = targetId,
                parentId = parentId,
                authorId = author.id,
                authorName = author.nickname,
                authorColor = author.avatarColor,
                content = content.trim()
            )
        )
        if (type == TargetType.POST) postDao.addCommentCount(targetId, 1)
        return true
    }

    suspend fun deleteComment(commentId: Long, type: String, targetId: Long) {
        commentDao.deleteById(commentId)
        if (type == TargetType.POST) postDao.addCommentCount(targetId, -1)
    }

    // ------------------------------------------------------------------
    // 点赞
    // ------------------------------------------------------------------

    /** 当前用户是否点赞了该目标（未登录恒为 false）。 */
    fun liked(type: String, targetId: Long): Flow<Boolean> =
        session.currentUserId.flatMapLatest { userId ->
            if (userId <= 0L) flowOf(false) else likeDao.observeLiked(type, targetId, userId)
        }

    fun likeCount(type: String, targetId: Long): Flow<Int> = likeDao.observeCountFlow(type, targetId)

    /**
     * 点赞 / 取消点赞。
     *
     * @return 操作后是否处于「已点赞」状态；未登录返回 false
     */
    suspend fun toggleLike(type: String, targetId: Long): Boolean {
        val userId = session.currentUserIdOnce()
        if (userId <= 0L) return false
        val alreadyLiked = likeDao.observeLiked(type, targetId, userId).first()
        return if (alreadyLiked) {
            likeDao.delete(type, targetId, userId)
            if (type == TargetType.POST) postDao.addLikeCount(targetId, -1)
            false
        } else {
            likeDao.insert(LikeEntity(type, targetId, userId))
            if (type == TargetType.POST) postDao.addLikeCount(targetId, 1)
            true
        }
    }

    /** 我给出的点赞总数（个人主页统计）。 */
    fun likesGiven(userId: Long): Flow<Int> = likeDao.observeGivenCount(userId)

    /** 社区成员数。 */
    val memberCount: Flow<Int> = userDao.observeCount()

    // ------------------------------------------------------------------
    // 演示数据
    // ------------------------------------------------------------------

    /**
     * 首次启动时播种演示账号与示例动态。
     *
     * 目的：让「打卡广场」开箱就有内容，课堂上可以演示多人互动效果。
     * 演示账号密码统一为 123456，可在登录页一键登录切换身份。
     */
    suspend fun seedDemoContentIfNeeded() {
        if (userDao.demoCount() > 0) return

        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000
        val hour = 60L * 60 * 1000

        val ids = mutableListOf<Long>()
        UserRepository.DEMO_ACCOUNTS.forEachIndexed { index, account ->
            ids += userDao.insert(
                UserEntity(
                    username = account.username,
                    nickname = account.nickname,
                    avatarColor = account.avatarColor,
                    passwordHash = demoHash(UserRepository.DEMO_PASSWORD),
                    salt = DEMO_SALT,
                    bio = account.bio,
                    isDemo = true,
                    createdAt = now - (10 - index) * day
                )
            )
        }
        if (ids.size < 4 || ids.any { it <= 0L }) return

        suspend fun authorOf(index: Int): UserEntity? {
            val id = ids.getOrNull(index) ?: return null
            return userDao.findById(id)
        }

        // 参数顺序：作者下标、节气序号、节气名、心情、正文、评论数、距今小时
        val seeds = listOf(
            PostSeed(
                1, 15, "白露", "清爽",
                "白露第一天，去田里看了稻子，稻穗已经开始低头了。" +
                    "老话说「白露白迷迷，秋分稻秀齐」，真准。",
                4, 26
            ),
            PostSeed(
                2, 15, "白露", "馋了",
                "按老规矩收了清露，又煮了一壶白露茶，配着龙眼吃。" +
                    "福建朋友说他们那儿白露必吃龙眼，试了一下确实甜。",
                4, 20
            ),
            PostSeed(
                3, 16, "秋分", "充实",
                "秋分要竖蛋，试了七次终于立住一个！还去采了秋菜煮秋汤，" +
                    "「秋汤灌脏，洗涤肝肠」，图个吉利。",
                2, 8
            ),
            PostSeed(
                0, 16, "秋分", "惬意",
                "秋分昼夜平分，今天在西湖边一直走到天黑。" +
                    "桂花的味道一阵一阵的，节气真的是能闻出来的。",
                2, 5
            ),
            PostSeed(
                1, 17, "寒露", "惦记",
                "寒露脚不露，今天开始给爸妈寄厚袜子了。顺便泡了菊花酒，" +
                    "等冬至的时候正好能喝。",
                1, 2
            )
        )

        // 点赞情况：为每条动态指定哪些演示用户点了赞（下标），
        // 点赞数由真实记录算出，避免「计数与记录不一致」导致点赞后数字跳变。
        val likePlan = listOf(
            listOf(0, 2, 3),        // 12 赞的等效展示：3 人点赞
            listOf(0, 1, 3),        // 白露茶那条
            listOf(0, 1, 2, 3),     // 秋分竖蛋
            listOf(0, 2),           // 西湖那条
            listOf(3)               // 寒露寄袜子
        )

        seeds.forEachIndexed { index, seed ->
            val author = authorOf(seed.authorIdx) ?: return@forEachIndexed
            val postId = postDao.insert(
                PostEntity(
                    authorId = author.id,
                    authorName = author.nickname,
                    authorColor = author.avatarColor,
                    termIndex = seed.termIndex,
                    termName = seed.termName,
                    content = seed.content,
                    mood = seed.mood,
                    likeCount = likePlan.getOrElse(index) { emptyList() }.size,
                    commentCount = seed.commentCount,
                    createdAt = now - seed.hoursAgo * hour
                )
            )
            // 插入真实点赞记录，保证计数与记录一致
            likePlan.getOrElse(index) { emptyList() }.forEach { userIdx ->
                val liker = authorOf(userIdx) ?: return@forEach
                likeDao.insert(
                    LikeEntity(
                        targetType = TargetType.POST,
                        targetId = postId,
                        userId = liker.id,
                        createdAt = now - seed.hoursAgo * hour + 60_000
                    )
                )
            }
        }

        // 给最新的两条动态补评论，形成可演示的互动效果
        val posts = postDao.recent(2)
        val commentSeeds = listOf(
            CommentSeed(0, "白露茶配龙眼，学到了，明天就试", 24),
            CommentSeed(2, "我们这边白露是吃红薯，也很香", 22),
            CommentSeed(1, "竖蛋成功也太厉害了，我一次都没立住", 7),
            CommentSeed(3, "秋汤是什么味道呀？想试试", 6)
        )
        posts.forEach { post ->
            commentSeeds.forEach { seed ->
                val author = authorOf(seed.authorIdx) ?: return@forEach
                commentDao.insert(
                    CommentEntity(
                        targetType = TargetType.POST,
                        targetId = post.id,
                        authorId = author.id,
                        authorName = author.nickname,
                        authorColor = author.avatarColor,
                        content = seed.content,
                        likeCount = (0..6).random(),
                        createdAt = now - seed.hoursAgo * hour
                    )
                )
            }
        }

        // 白露节气详情下也放一条评论，进入详情页即可看到互动
        authorOf(3)?.let { author ->
            commentDao.insert(
                CommentEntity(
                    targetType = TargetType.TERM,
                    targetId = 15,
                    authorId = author.id,
                    authorName = author.nickname,
                    authorColor = author.avatarColor,
                    content = "讲白露一定让学生背「露从今夜白」，配上天气变化特别好懂。",
                    likeCount = 5,
                    createdAt = now - 12 * hour
                )
            )
        }
    }

    /** 清空打卡广场的所有动态与点赞（不影响账号）。 */
    suspend fun clearAllSocial() {
        postDao.clear()
        likeDao.clear()
    }

    private fun CommentEntity.toItem(currentUserId: Long) = CommentItem(
        id = id,
        targetType = targetType,
        targetId = targetId,
        parentId = parentId,
        authorId = authorId,
        authorName = authorName,
        authorColor = authorColor,
        content = content,
        likeCount = likeCount,
        createdAt = createdAt,
        isMine = authorId == currentUserId
    )

    private companion object {
        const val DEMO_SALT = "jieqi-demo-salt"

        fun demoHash(password: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(DEMO_SALT.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(
                digest.digest(password.toByteArray(Charsets.UTF_8)),
                Base64.NO_WRAP
            )
        }
    }
}

private data class PostSeed(
    val authorIdx: Int,
    val termIndex: Int,
    val termName: String,
    val mood: String,
    val content: String,
    val commentCount: Int,
    val hoursAgo: Long
)

private data class CommentSeed(
    val authorIdx: Int,
    val content: String,
    val hoursAgo: Long
)
