package com.example.jieqi.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 登录态存储：只保存当前登录用户的 id，会话在应用重启后保持。
 *
 * 与 [SettingsStore] 分开一个 DataStore 文件，职责更清晰。
 */
private val Context.sessionStore: DataStore<Preferences> by androidx.datastore.preferences.preferencesDataStore(
    name = "jieqi_session"
)

class SessionStore(private val context: Context) {

    private val key = longPreferencesKey("current_user_id")

    /** 当前登录用户 id，0 表示未登录。 */
    val currentUserId: Flow<Long> = context.sessionStore.data.map { it[key] ?: 0L }

    suspend fun currentUserIdOnce(): Long = currentUserId.first()

    suspend fun login(userId: Long) {
        context.sessionStore.edit { it[key] = userId }
    }

    suspend fun logout() {
        context.sessionStore.edit { it[key] = 0L }
    }
}
