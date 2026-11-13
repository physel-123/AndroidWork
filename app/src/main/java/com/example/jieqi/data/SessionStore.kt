package com.example.jieqi.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 登录态存储：保存当前登录用户信息。
 *
 * 迁移后同时保存两部分：
 * - [currentUserId]：本地 Room 中的用户镜像 id（供打卡广场等本地社交功能使用）
 * - [currentToken]：后端 JWT token（供后续账号相关网络请求使用）
 *
 * 与 [SettingsStore] 分开一个 DataStore 文件，职责更清晰。
 */
private val Context.sessionStore: DataStore<Preferences> by androidx.datastore.preferences.preferencesDataStore(
    name = "jieqi_session"
)

class SessionStore(private val context: Context) {

    private val userIdKey = longPreferencesKey("current_user_id")
    private val tokenKey = stringPreferencesKey("auth_token")
    private val usernameKey = stringPreferencesKey("auth_username")

    /** 当前登录用户 id，0 表示未登录。 */
    val currentUserId: Flow<Long> = context.sessionStore.data.map { it[userIdKey] ?: 0L }

    /** 后端 JWT token，空串表示未登录。 */
    val currentToken: Flow<String> = context.sessionStore.data.map { it[tokenKey] ?: "" }

    /** 当前登录用户名，空串表示未登录。 */
    val currentUsername: Flow<String> = context.sessionStore.data.map { it[usernameKey] ?: "" }

    suspend fun currentUserIdOnce(): Long = currentUserId.first()

    suspend fun currentTokenOnce(): String = currentToken.first()

    suspend fun login(userId: Long, token: String = "", username: String = "") {
        context.sessionStore.edit {
            it[userIdKey] = userId
            if (token.isNotEmpty()) it[tokenKey] = token
            if (username.isNotEmpty()) it[usernameKey] = username
        }
    }

    suspend fun logout() {
        context.sessionStore.edit {
            it[userIdKey] = 0L
            it[tokenKey] = ""
            it[usernameKey] = ""
        }
    }
}
