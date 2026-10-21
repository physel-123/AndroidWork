package com.example.jieqi.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 每日提醒设置。 */
data class ReminderSettings(
    val enabled: Boolean = true,
    val hour: Int = 8,
    val minute: Int = 0
) {
    /** “08:00” 形式的展示文本。 */
    val timeText: String get() = "%02d:%02d".format(hour, minute)
}

/** 主题设置。 */
enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色")
}

/** 用户昵称等个人化信息。 */
data class ProfileSettings(
    val nickname: String = "节气文化传习人",
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jieqi_settings")

/**
 * 基于 DataStore 的设置存储：提醒开关/时间、昵称、主题模式。
 */
class SettingsStore(private val context: Context) {

    private object Keys {
        val reminderEnabled = booleanPreferencesKey("reminder_enabled")
        val reminderHour = intPreferencesKey("reminder_hour")
        val reminderMinute = intPreferencesKey("reminder_minute")
        val nickname = stringPreferencesKey("nickname")
        val themeMode = stringPreferencesKey("theme_mode")
    }

    val reminder: Flow<ReminderSettings> = context.dataStore.data.map { prefs ->
        ReminderSettings(
            enabled = prefs[Keys.reminderEnabled] ?: true,
            hour = prefs[Keys.reminderHour] ?: 8,
            minute = prefs[Keys.reminderMinute] ?: 0
        )
    }

    val profile: Flow<ProfileSettings> = context.dataStore.data.map { prefs ->
        ProfileSettings(
            nickname = prefs[Keys.nickname] ?: "节气文化传习人",
            themeMode = runCatching {
                ThemeMode.valueOf(prefs[Keys.themeMode] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM)
        )
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.reminderEnabled] = enabled }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.reminderHour] = hour
            it[Keys.reminderMinute] = minute
        }
    }

    suspend fun setNickname(name: String) {
        context.dataStore.edit { it[Keys.nickname] = name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.themeMode] = mode.name }
    }
}
