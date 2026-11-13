package com.example.jieqi.data.remote

import com.google.gson.annotations.SerializedName

/** 后端服务器地址。 */
object ApiConfig {
    const val BASE_URL = "http://49.232.151.242:8001/"
}

/** 认证返回（注册/登录共用）。 */
data class AuthResponse(
    val token: String = "",
    val id: Long = 0,
    val username: String = "",
    val nickname: String = "",
    val bio: String = "",
    @SerializedName("avatarColor") val avatarColor: Int = 0,
    @SerializedName("isDemo") val isDemo: Boolean = false
)

/** 今日运势返回。 */
data class FortuneResponse(
    val date: String = "",
    val zodiac: String = "",
    @SerializedName("overall_stars") val overallStars: Int = 0,
    val summary: String = "",
    val dimensions: Map<String, FortuneDimension> = emptyMap(),
    @SerializedName("lucky_color") val luckyColor: String = "",
    @SerializedName("lucky_number") val luckyNumber: String = "",
    val suitable: String = "",
    val avoid: String = "",
    val disclaimer: String = ""
)

data class FortuneDimension(
    val stars: Int = 0,
    val text: String = ""
)

/** 抽签返回。 */
data class SignResponse(
    val type: String = "",
    val date: String = "",
    val name: String = "",
    val level: Int = 0,
    val poem: String = "",
    val interpret: String = "",
    val disclaimer: String = ""
)

/** 塔罗返回。 */
data class TarotResponse(
    val type: String = "",
    val date: String = "",
    val cards: List<TarotCard> = emptyList(),
    @SerializedName("overall_level") val overallLevel: Int = 0,
    val summary: String = "",
    val disclaimer: String = ""
)

data class TarotCard(
    val position: String = "",
    val name: String = "",
    val orientation: String = "",
    val meaning: String = ""
)

/** 手相/面相识别返回。 */
data class VisionResponse(
    val features: Map<String, String> = emptyMap(),
    val items: List<VisionItem> = emptyList(),
    val recognized: Boolean = true,
    val message: String = "",
    val closing: String = "",
    val disclaimer: String = ""
)

data class VisionItem(
    val dimension: String = "",
    val feature: String = "",
    val interpret: String = ""
)

// ── 社交（打卡广场 / 评论 / 点赞）──

/** 打卡广场动态（后端返回）。 */
data class PostDto(
    val id: Long = 0,
    val author: String = "",
    @SerializedName("authorName") val authorName: String = "",
    @SerializedName("authorColor") val authorColor: Int = 0,
    @SerializedName("termIndex") val termIndex: Int = 0,
    @SerializedName("termName") val termName: String = "",
    val content: String = "",
    @SerializedName("imageAsset") val imageAsset: String? = null,
    val mood: String? = null,
    @SerializedName("likeCount") val likeCount: Int = 0,
    @SerializedName("commentCount") val commentCount: Int = 0,
    val createdAt: Long = 0,
    @SerializedName("likedByMe") val likedByMe: Boolean = false,
    @SerializedName("isMine") val isMine: Boolean = false
)

/** 评论（后端返回，与前端 CommentItem 对齐）。 */
data class CommentDto(
    val id: Long = 0,
    @SerializedName("targetType") val targetType: String = "",
    @SerializedName("targetId") val targetId: Long = 0,
    @SerializedName("parentId") val parentId: Long = 0,
    val author: String = "",
    @SerializedName("authorName") val authorName: String = "",
    @SerializedName("authorColor") val authorColor: Int = 0,
    val content: String = "",
    @SerializedName("likeCount") val likeCount: Int = 0,
    val createdAt: Long = 0,
    @SerializedName("isMine") val isMine: Boolean = false
)

/** 点赞结果。 */
data class LikeResult(
    val liked: Boolean = false,
    @SerializedName("likeCount") val likeCount: Int = 0
)

/** 发动态请求体。 */
data class PostCreateRequest(
    val username: String,
    val content: String,
    @SerializedName("termIndex") val termIndex: Int = 0,
    @SerializedName("termName") val termName: String = "",
    val mood: String? = null,
    @SerializedName("imageAsset") val imageAsset: String? = null
)

/** 发评论请求体。 */
data class CommentCreateRequest(
    val username: String,
    @SerializedName("targetType") val targetType: String,
    @SerializedName("targetId") val targetId: Long,
    val content: String,
    @SerializedName("parentId") val parentId: Long = 0
)

/** 民俗投稿（用户补充，后端存储）。 */
data class FolkCustomDto(
    val id: Long = 0,
    val title: String = "",
    @SerializedName("termName") val termName: String? = null,
    val province: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val contributor: String = "",
    @SerializedName("contributorName") val contributorName: String = "",
    @SerializedName("createdAt") val createdAt: Long = 0
)

/** 投稿请求体。 */
data class FolkCustomCreateRequest(
    val username: String,
    val title: String,
    @SerializedName("termName") val termName: String? = null,
    val province: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val description: String
)
