package com.example.jieqi.notify

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.jieqi.MainActivity
import com.example.jieqi.R
import com.example.jieqi.data.SettingsStore
import com.example.jieqi.data.content.DailyContentFactory
import com.example.jieqi.data.db.JieQiDatabase
import com.example.jieqi.domain.TermDateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

/**
 * 每日节气提醒的闹钟接收器。
 *
 * 触发后读取“今天是否已打卡”，生成一条包含当前节气、今日习俗、
 * 时令食俗的通知，点击可直达首页。
 */
class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        showDailyNotification(context)
    }

    private fun showDailyNotification(context: Context) {
        // Android 13+ 未授权通知权限时静默退出
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val today = LocalDate.now()
        val occurrence = TermDateUtils.currentOccurrence(today)
        val custom = DailyContentFactory.dailyCustom(occurrence, today)
        val food = DailyContentFactory.dailyFood(occurrence, today)

        val checkedIn = runCatching {
            runBlocking {
                JieQiDatabase.get(context).checkInDao().findByDate(today.toString()) != null
            }
        }.getOrDefault(false)

        val title = "今日${occurrence.name}｜${occurrence.term.summary}"
        val content = buildString {
            append("习俗：").append(custom)
            append("\n食俗：").append(food)
            if (!checkedIn) append("\n别忘了完成今日节气打卡～")
        }

        ReminderScheduler.ensureChannel(context)

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_term)
            .setContentTitle(title)
            .setContentText(custom)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        runCatching {
            NotificationManagerCompat.from(context)
                .notify(ReminderScheduler.notificationIdFor(today), notification)
        }
    }
}

/**
 * 开机 / 应用更新后恢复每日提醒。
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        ReminderScheduler.ensureChannel(context)
        runCatching {
            val settings = runBlocking { SettingsStore(context).reminder.first() }
            ReminderScheduler.apply(context, settings)
        }
    }
}
