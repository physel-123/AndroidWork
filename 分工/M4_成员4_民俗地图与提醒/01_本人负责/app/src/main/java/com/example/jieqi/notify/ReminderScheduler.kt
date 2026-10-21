package com.example.jieqi.notify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.example.jieqi.data.ReminderSettings
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 每日节气提醒调度器。
 *
 * 采用 [AlarmManager] 的 `setInexactRepeating` 方式设定每天固定时刻提醒：
 * 这种方式在低电耗模式下无需精确闹钟权限，符合平台对省电的要求，
 * 也足够满足“每日推送节气知识”的场景。
 */
object ReminderScheduler {

    const val CHANNEL_ID = "daily_term_reminder"
    const val CHANNEL_NAME = "每日节气提醒"
    const val EXTRA_FROM_ALARM = "from_alarm"

    private const val REQUEST_CODE = 20240

    /** 创建通知渠道（Android 8.0+），重复调用安全。 */
    fun ensureChannel(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "每天推送当日节气知识与民俗小贴士"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * 应用设置：开启则安排每日提醒，关闭则取消。
     */
    fun apply(context: Context, settings: ReminderSettings) {
        if (settings.enabled) {
            schedule(context, settings.hour, settings.minute)
        } else {
            cancel(context)
        }
    }

    /** 安排每天 [hour]:[minute] 的提醒；若今天该时刻已过，则从明天开始。 */
    fun schedule(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        ensureChannel(context)

        val now = LocalDateTime.now()
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)

        val triggerAt = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pendingIntent = alarmPendingIntent(context)

        // 每天重复 24 小时；首次触发时间由 triggerAt 决定
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    /** 取消提醒。 */
    fun cancel(context: Context) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        alarmManager.cancel(alarmPendingIntent(context))
    }

    private fun alarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = "com.example.jieqi.action.DAILY_REMINDER"
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
    }

    /** 稳定的通知 id（同一天复用，避免重复堆积）。 */
    fun notificationIdFor(date: java.time.LocalDate): Int =
        (date.toEpochDay() % Int.MAX_VALUE).toInt()
}
