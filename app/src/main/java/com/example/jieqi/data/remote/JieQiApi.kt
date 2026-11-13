package com.example.jieqi.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/** 后端 FastAPI 接口定义。 */
interface JieQiApi {

    // ── 账号认证 ──
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    // 登录走 OAuth2PasswordRequestForm（form 表单）
    @FormUrlEncoded
    @POST("api/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): AuthResponse

    // ── 运势 / 占卜 ──
    @GET("api/fortune")
    suspend fun fortune(@Query("zodiac") zodiac: String): FortuneResponse

    @GET("api/divination/sign")
    suspend fun sign(@Query("username") username: String = "guest"): SignResponse

    @GET("api/divination/sign/today")
    suspend fun todaySign(@Query("username") username: String = "guest"): SignResponse?

    @GET("api/divination/tarot")
    suspend fun tarot(
        @Query("username") username: String = "guest",
        @Query("count") count: Int = 3
    ): TarotResponse

    @GET("api/divination/tarot/today")
    suspend fun todayTarot(@Query("username") username: String = "guest"): TarotResponse?

    // ── 手相 / 面相 ──
    @Multipart
    @POST("api/palm/analyze")
    suspend fun analyzePalm(@Part file: MultipartBody.Part): VisionResponse

    @Multipart
    @POST("api/face/analyze")
    suspend fun analyzeFace(@Part file: MultipartBody.Part): VisionResponse

    // ── 社交（打卡广场 / 评论 / 点赞 / 民俗投稿）──
    @GET("api/social/posts")
    suspend fun socialPosts(@Query("username") username: String = ""): List<PostDto>

    @POST("api/social/posts")
    suspend fun createPost(@Body body: PostCreateRequest): PostDto

    @DELETE("api/social/posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: Long, @Query("username") username: String): Map<String, Boolean>

    @GET("api/social/comments")
    suspend fun socialComments(
        @Query("targetType") targetType: String,
        @Query("targetId") targetId: Long,
        @Query("username") username: String = ""
    ): List<CommentDto>

    @GET("api/social/comments/count")
    suspend fun commentCount(
        @Query("targetType") targetType: String,
        @Query("targetId") targetId: Long
    ): Int

    @POST("api/social/comments")
    suspend fun createComment(@Body body: CommentCreateRequest): CommentDto

    @DELETE("api/social/comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") commentId: Long, @Query("username") username: String): Map<String, Boolean>

    @GET("api/social/like")
    suspend fun getLike(
        @Query("targetType") targetType: String,
        @Query("targetId") targetId: Long,
        @Query("username") username: String
    ): LikeResult

    @POST("api/social/like")
    suspend fun toggleLike(
        @Query("targetType") targetType: String,
        @Query("targetId") targetId: Long,
        @Query("username") username: String
    ): LikeResult

    @GET("api/social/member_count")
    suspend fun memberCount(): Int

    // ── 民俗投稿 ──
    @GET("api/social/folk_customs")
    suspend fun folkCustoms(): List<FolkCustomDto>

    @POST("api/social/folk_customs")
    suspend fun createFolkCustom(@Body body: FolkCustomCreateRequest): FolkCustomDto

    @DELETE("api/social/folk_customs/{customId}")
    suspend fun deleteFolkCustom(@Path("customId") customId: Long, @Query("username") username: String): Map<String, Boolean>
}

/** 注册请求体。 */
data class RegisterRequest(
    val username: String,
    val password: String,
    val nickname: String = ""
)
