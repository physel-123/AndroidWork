package com.example.jieqi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 节气打卡记录：每天最多一条，用日期字符串（yyyy-MM-dd）作主键天然去重。
 *
 * @param date 打卡日期，格式 yyyy-MM-dd
 * @param termIndex 打卡当天所处的节气序号（1..24），用于统计“跟着节气过日子”的完成度
 * @param termName 节气名，冗余存储便于展示
 * @param note 一句话心情/记录，可为空
 * @param createdAt 打卡时间戳（毫秒）
 */
@Entity(tableName = "check_in")
data class CheckInEntity(
    @PrimaryKey val date: String,
    val termIndex: Int,
    val termName: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 收藏：可以收藏节气，也可以收藏民俗条目。
 *
 * @param key 唯一键，节气为 "term:立春"，民俗为 "custom:12"
 * @param type 类型：term / custom
 * @param title 标题
 * @param subtitle 副标题（节气摘要或民俗所属地区）
 * @param refId 关联的业务 id（节气序号或民俗主键）
 * @param createdAt 收藏时间戳（毫秒）
 */
@Entity(tableName = "favorite")
data class FavoriteEntity(
    @PrimaryKey val key: String,
    val type: String,
    val title: String,
    val subtitle: String,
    val refId: Int,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 地方民俗：支持地域民俗地图展示，也支持用户自己补充“我家的民俗”。
 *
 * @param id 自增主键；内置数据 id 为 0，由 Room 自动分配
 * @param title 民俗名称，如“立春打春牛”
 * @param termName 关联节气名，可为空（表示全年民俗）
 * @param province 省份，如“浙江”
 * @param city 城市/地区，如“衢州”
 * @param latitude 纬度（用于地图打点）
 * @param longitude 经度（用于地图打点）
 * @param description 民俗介绍
 * @param isBuiltIn 是否内置数据（false 表示用户投稿）
 * @param createdAt 创建时间戳（毫秒）
 */
@Entity(tableName = "folk_custom")
data class FolkCustomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val termName: String?,
    val province: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val isBuiltIn: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
