package com.example.jieqi.data

import android.content.Context
import com.example.jieqi.data.db.JieQiDatabase
import com.example.jieqi.notify.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 轻量级依赖容器（手写单例，避免为课程项目引入 Hilt 等框架带来的额外复杂度）。
 *
 * 全局只保留一个数据库实例与各仓库实例，供 ViewModel 与通知模块共享。
 */
class AppContainer(val context: Context) {

    /** 应用级协程作用域：用于播种数据、调度提醒等“发射后不管”的任务。 */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: JieQiDatabase by lazy { JieQiDatabase.get(context) }

    val settings: SettingsStore by lazy { SettingsStore(context) }

    val session: SessionStore by lazy { SessionStore(context) }

    val checkInRepository: CheckInRepository by lazy { CheckInRepository(database) }

    val favoriteRepository: FavoriteRepository by lazy { FavoriteRepository(database) }

    val folkCustomRepository: FolkCustomRepository by lazy { FolkCustomRepository(database) }

    val userRepository: UserRepository by lazy { UserRepository(database, session) }

    val socialRepository: SocialRepository by lazy { SocialRepository(database, session) }

    /** 启动时的初始化：播种内置内容 + 演示账号与动态 + 恢复每日提醒。 */
    fun warmUp() {
        appScope.launch {
            runCatching { folkCustomRepository.seedIfEmpty() }
            runCatching { socialRepository.seedDemoContentIfNeeded() }
            runCatching {
                val reminder = settings.reminder.first()
                ReminderScheduler.apply(context, reminder)
            }
        }
    }
}
