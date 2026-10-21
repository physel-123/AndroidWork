package com.example.jieqi.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jieqi.data.ProfileSettings
import com.example.jieqi.data.ReminderSettings
import com.example.jieqi.data.ThemeMode
import com.example.jieqi.data.content.SolarTermContent
import com.example.jieqi.ui.LocalAppContainer
import com.example.jieqi.ui.LocalSnackbarHostState
import com.example.jieqi.ui.components.JieQiCard
import com.example.jieqi.ui.components.SectionHeader
import com.example.jieqi.ui.components.SelectableChip
import com.example.jieqi.ui.components.StatCell
import com.example.jieqi.util.UserAvatar
import kotlinx.coroutines.launch

/**
 * “我的”页面：个人文化足迹 + 提醒/主题/数据管理设置。
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenFavorites: () -> Unit,
    onOpenTerm: (Int) -> Unit,
    onOpenCredits: () -> Unit = {},
    onRequireLogin: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val stats by container.checkInRepository.stats.collectAsStateWithLifecycle(
        initialValue = com.example.jieqi.domain.CheckInStats()
    )
    val favorites by container.favoriteRepository.all.collectAsStateWithLifecycle(initialValue = emptyList())
    val reminder by container.settings.reminder.collectAsStateWithLifecycle(initialValue = ReminderSettings())
    val profile by container.settings.profile.collectAsStateWithLifecycle(initialValue = ProfileSettings())
    val customCount by container.folkCustomRepository.userContributionCount
        .collectAsStateWithLifecycle(initialValue = 0)

    var editingNickname by remember { mutableStateOf(false) }
    var nicknameDraft by remember { mutableStateOf(profile.nickname) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    val currentUser by container.userRepository.currentUser.collectAsStateWithLifecycle(initialValue = null)
    val loggedIn by container.userRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val myPostCount by remember(currentUser?.id) {
        currentUser?.let { container.socialRepository.postCountOfAuthor(it.id) }
            ?: kotlinx.coroutines.flow.flowOf(0)
    }.collectAsStateWithLifecycle(initialValue = 0)
    val likedCount by remember(currentUser?.id) {
        currentUser?.let { container.socialRepository.likesGiven(it.id) }
            ?: kotlinx.coroutines.flow.flowOf(0)
    }.collectAsStateWithLifecycle(initialValue = 0)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------- 个人卡片 ----------
        item {
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentUser != null) {
                            UserAvatar(
                                nickname = currentUser!!.nickname,
                                colorIndex = currentUser!!.avatarColor,
                                size = 56.dp
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "游客",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.size(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.nickname ?: "未登录",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(Modifier.size(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "修改昵称",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            if (currentUser == null) {
                                                onRequireLogin()
                                            } else {
                                                nicknameDraft = currentUser!!.nickname
                                                editingNickname = true
                                            }
                                        },
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = currentUser?.bio
                                    ?: "登录后可以打卡、评论、发布动态",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Text(
                                text = "传习等级：${stats.level}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatCell("${stats.total}", "累计打卡")
                        StatCell("${myPostCount}", "我的动态")
                        StatCell("${likedCount}", "送出点赞")
                        StatCell("${favorites.size}", "收藏")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatCell("${stats.currentStreak}", "连续天数")
                        StatCell("${stats.visitedTerms}", "点亮节气")
                        StatCell("${stats.longestStreak}", "最长纪录")
                        StatCell("${stats.thisMonth}", "本月打卡")
                    }
                }
            }
        }

        // ---------- 账号操作 ----------
        item {
            if (loggedIn) {
                SettingRow(
                    icon = Icons.Filled.Logout,
                    title = "退出登录",
                    subtitle = "退出后仍可浏览，但无法打卡与互动",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = {
                        scope.launch {
                            container.userRepository.logout()
                            snackbar.showSnackbar("已退出登录")
                        }
                    }
                )
            } else {
                SettingRow(
                    icon = Icons.Filled.Login,
                    title = "登录 / 注册",
                    subtitle = "登录后即可打卡、评论、发布动态",
                    onClick = onRequireLogin
                )
            }
        }

        // ---------- 我的收藏 ----------
        item {
            SettingRow(
                icon = Icons.Filled.Bookmarks,
                title = "我的收藏",
                subtitle = "已收藏 ${favorites.size} 条内容",
                onClick = onOpenFavorites
            )
        }

        // ---------- 提醒设置 ----------
        item { SectionHeader(title = "学习提醒", subtitle = "每天固定时间推送当日节气知识") }
        item {
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "每日节气提醒", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = if (reminder.enabled) "每天 ${reminder.timeText} 提醒" else "已关闭",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = reminder.enabled,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    container.settings.setReminderEnabled(checked)
                                    val updated = reminder.copy(enabled = checked)
                                    com.example.jieqi.notify.ReminderScheduler.apply(context, updated)
                                    snackbar.showSnackbar(if (checked) "已开启每日提醒" else "已关闭每日提醒")
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = reminder.enabled) { showTimePicker = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = "提醒时间",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = reminder.timeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---------- 主题 ----------
        item { SectionHeader(title = "外观") }
        item {
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (profile.themeMode) {
                                ThemeMode.SYSTEM -> Icons.Filled.DarkMode
                                ThemeMode.LIGHT -> Icons.Filled.LightMode
                                ThemeMode.DARK -> Icons.Filled.DarkMode
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = "主题模式",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            SelectableChip(
                                text = mode.label,
                                selected = profile.themeMode == mode,
                                accent = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    scope.launch { container.settings.setThemeMode(mode) }
                                }
                            )
                        }
                    }
                }
            }
        }

        // ---------- 数据管理 ----------
        item { SectionHeader(title = "数据管理", subtitle = "所有记录都保存在本机，不上传服务器") }
        item {
            SettingRow(
                icon = Icons.Filled.FileDownload,
                title = "导出打卡记录",
                subtitle = "复制为 CSV 文本，可粘贴到 Excel",
                onClick = {
                    scope.launch {
                        val csv = container.checkInRepository.exportCsv()
                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                        clipboard?.setPrimaryClip(ClipData.newPlainText("节气打卡记录", csv))
                        snackbar.showSnackbar("已复制 ${csv.lines().size - 1} 条记录到剪贴板")
                    }
                }
            )
        }
        item {
            SettingRow(
                icon = Icons.Filled.Refresh,
                title = "清空打卡数据",
                subtitle = "会同时重置等级与点亮进度",
                tint = MaterialTheme.colorScheme.error,
                onClick = {
                    scope.launch {
                        container.checkInRepository.clearAll()
                        container.folkCustomRepository.clearUserContributions()
                        snackbar.showSnackbar("已清空本机打卡数据")
                    }
                }
            )
        }

        // ---------- 文化小知识 ----------
        item { SectionHeader(title = "二十四节气小知识") }
        item {
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "二十四节气是中国古人通过观察太阳周年运动、认知时令与物候变化规律所形成的知识体系，2016 年被列入联合国教科文组织人类非物质文化遗产代表作名录。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val counts = remember { SolarTermContent.countBySeason() }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        counts.entries.sortedBy { it.key.ordinal }.forEach { (season, count) ->
                            SelectableChip(
                                text = "${season.label}季 $count",
                                selected = false,
                                accent = seasonColor(season),
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }

        // ---------- 关于 ----------
        item {
            SettingRow(
                icon = Icons.Filled.Info,
                title = "关于本项目",
                subtitle = "移动互联网应用开发技术 · 结课项目",
                onClick = { showAbout = true }
            )
        }
        item {
            SettingRow(
                icon = Icons.Filled.Image,
                title = "图片来源与授权",
                subtitle = "全部配画为 CC0 公共领域藏品，可自由使用",
                onClick = onOpenCredits
            )
        }

        // ---------- 快捷入口：随便看一个节气 ----------
        item { SectionHeader(title = "随便逛逛") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 5, 10, 15, 20, 23).forEach { index ->
                    val term = SolarTermContent.byIndex(index)
                    if (term != null) {
                        SelectableChip(
                            text = term.name,
                            selected = false,
                            accent = term.color,
                            onClick = { onOpenTerm(index) }
                        )
                    }
                }
            }
        }
    }

    // ---------- 昵称编辑 ----------
    if (editingNickname) {
        AlertDialog(
            onDismissRequest = { editingNickname = false },
            title = { Text("修改昵称") },
            text = {
                OutlinedTextField(
                    value = nicknameDraft,
                    onValueChange = { if (it.length <= 12) nicknameDraft = it },
                    label = { Text("昵称（12 字以内）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        container.settings.setNickname(nicknameDraft.ifBlank { "节气文化传习人" })
                        editingNickname = false
                        snackbar.showSnackbar("昵称已更新")
                    }
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { editingNickname = false }) { Text("取消") } }
        )
    }

    // ---------- 提醒时间选择 ----------
    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = reminder.hour,
            initialMinute = reminder.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择每日提醒时间") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        container.settings.setReminderTime(timeState.hour, timeState.minute)
                        com.example.jieqi.notify.ReminderScheduler.schedule(
                            context,
                            timeState.hour,
                            timeState.minute
                        )
                        showTimePicker = false
                        snackbar.showSnackbar("已设置为每天 %02d:%02d 提醒".format(timeState.hour, timeState.minute))
                    }
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } }
        )
    }

    // ---------- 关于弹窗 ----------
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("关于节气民俗") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("版本：1.0.0", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "课程：移动互联网应用开发技术\n项目：传统民俗节气文化宣传",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "技术：Kotlin + Jetpack Compose + Room + DataStore，单 Activity 架构，全部内容离线内置。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "节气时刻由 VSOP87 天文算法实时计算，无需联网即可获得准确的节气日期。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("知道了") } }
        )
    }
}

/** 通用设置行。 */
@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    JieQiCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
