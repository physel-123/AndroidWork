package com.example.jieqi.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 用户表（本地账号体系）。
 *
 * 说明：本项目为纯离线课程项目，账号保存在本机 Room 中，
 * 密码以「随机盐 + SHA-256」形式存储，不保存明文。
 *
 * @param id 自增主键
 * @param username 登录名（唯一）
 * @param nickname 昵称
 * @param avatarColor 头像底色（用主题色索引，避免引入图片资源）
 * @param passwordHash 密码摘要
 * @param salt 随机盐
 * @param bio 个性签名
 * @param isDemo 是否内置演示用户（演示用户用于展示多人互动效果）
 * @param createdAt 注册时间戳
 */
@Entity(
    tableName = "user",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val nickname: String,
    val avatarColor: Int = 0,
    val passwordHash: String,
    val salt: String,
    val bio: String = "",
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 打卡动态（打卡广场）。
 *
 * @param id 自增主键
 * @param authorId 作者用户 id
 * @param authorName 作者昵称（冗余，便于列表直接展示）
 * @param authorColor 作者头像色索引
 * @param termIndex 发布时所处节气序号 1..24
 * @param termName 节气名（冗余）
 * @param content 正文
 * @param imageAsset 配图（assets/images 下的文件名，可为空）
 * @param mood 心情标签
 * @param likeCount 点赞数（冗余计数，便于排序展示）
 * @param commentCount 评论数（冗余计数）
 * @param isMine 是否本人发布（演示数据为 false）
 * @param createdAt 发布时间戳
 */
@Entity(tableName = "post", indices = [Index("createdAt")])
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: Long,
    val authorName: String,
    val authorColor: Int = 0,
    val termIndex: Int,
    val termName: String,
    val content: String,
    val imageAsset: String? = null,
    val mood: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 评论表：同时服务于「节气详情」「民俗条目」「打卡动态」三个场景。
 *
 * @param targetType 评论对象类型：term / folk / post
 * @param targetId 评论对象 id（term 用节气序号，folk/post 用主键）
 * @param parentId 父评论 id，0 表示顶级评论（用于二级回复）
 */
@Entity(tableName = "comment", indices = [Index(value = ["targetType", "targetId"])])
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetType: String,
    val targetId: Long,
    val parentId: Long = 0,
    val authorId: Long,
    val authorName: String,
    val authorColor: Int = 0,
    val content: String,
    val likeCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 点赞表：用「对象类型 + 对象 id + 用户 id」做唯一键，天然防重复点赞。
 */
@Entity(
    tableName = "like_record",
    primaryKeys = ["targetType", "targetId", "userId"],
    indices = [Index(value = ["targetType", "targetId"])]
)
data class LikeEntity(
    val targetType: String,
    val targetId: Long,
    val userId: Long,
    val createdAt: Long = System.currentTimeMillis()
)
