package com.example.jieqi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jieqi.data.content.DailyContentFactory
import com.example.jieqi.domain.CheckInCalculator
import com.example.jieqi.domain.TermDateUtils
import com.example.jieqi.ui.LocalAppContainer
import com.example.jieqi.ui.LocalSnackbarHostState
import com.example.jieqi.ui.components.GradientCard
import com.example.jieqi.ui.components.JieQiCard
import com.example.jieqi.ui.components.SectionHeader
import com.example.jieqi.ui.components.StatCell
import com.example.jieqi.ui.components.ThinProgressBar
import com.example.jieqi.ui.theme.SeasonSpring
import com.example.jieqi.ui.theme.SeasonSummer
import com.example.jieqi.ui.theme.SeasonAutumn
import com.example.jieqi.ui.theme.SeasonWinter
import com.example.jieqi.util.ShareUtils
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 打卡页：把“文化学习”变成看得见的坚持。
 *
 * 包含：连续打卡天数、月度日历、二十四节气点亮进度、
 * 打卡历史（可补填心情、可撤销），以及分享打卡卡片。
 */
@Composable
fun CheckInScreen(onRequireLogin: () -> Unit = {}) {
    val container = LocalAppContainer.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val today = remember { LocalDate.now() }
    val occurrence = remember(today) { TermDateUtils.currentOccurrence(today) }
    val accent = occurrence.term.color

    val stats by container.checkInRepository.stats.collectAsStateWithLifecycle(
        initialValue = com.example.jieqi.domain.CheckInStats()
    )
    val records by container.checkInRepository.records.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    val checkedInToday by remember(today) { container.checkInRepository.observeCheckedIn(today) }
        .collectAsStateWithLifecycle(initialValue = false)

    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    val loggedIn by container.userRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)

    val checkedDates = remember(records) {
        records.mapNotNull { TermDateUtils.fromDbDate(it.date) }.toSet()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(text = "节气打卡", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "每天打卡记录一次与节气的相遇，坚持得越久等级越高",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---------- 打卡主卡 ----------
        item {
            GradientCard(color = accent, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "今日${occurrence.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = occurrence.term.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatCell(value = "${stats.currentStreak}", label = "连续打卡", accent = Color.White)
                        StatCell(value = "${stats.total}", label = "累计天数", accent = Color.White)
                        StatCell(value = "${stats.longestStreak}", label = "最长纪录", accent = Color.White)
                    }
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (checkedInToday) {
                                    scope.launch {
                                        snackbar.showSnackbar("今天已经打卡啦，明天再来")
                                    }
                                } else if (loggedIn) {
                                    showNoteDialog = true
                                } else {
                                    onRequireLogin()
                                }
                            }
                    ) {
                        Text(
                            text = if (checkedInToday) "今日已打卡 ✓" else "立即打卡",
                            style = MaterialTheme.typography.titleSmall,
                            color = accent,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 14.dp)
                        )
                    }
                    if (checkedInToday) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val record = records.firstOrNull { it.date == today.toString() }
                                    ShareUtils.shareText(
                                        context,
                                        DailyContentFactory.shareCheckInText(
                                            today,
                                            occurrence,
                                            stats.currentStreak,
                                            record?.note
                                        )
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Share,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(
                                    text = "分享今日打卡",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---------- 传习等级 ----------
        item {
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "传习等级：${stats.level}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "已点亮 ${stats.visitedTerms}/24 个节气",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    ThinProgressBar(progress = stats.visitedTerms / 24f, color = accent)
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items((1..24).toList()) { index ->
                            val visited = records.any { it.termIndex == index }
                            val term = com.example.jieqi.data.content.SolarTermContent.byIndex(index)
                            Surface(
                                shape = CircleShape,
                                color = if (visited) term?.color ?: accent else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = term?.name?.take(1) ?: "?",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (visited) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ---------- 本月日历 ----------
        item {
            SectionHeader(
                title = "${today.year} 年 ${today.monthValue} 月打卡日历",
                subtitle = "本月已打卡 ${stats.thisMonth} 天",
                accent = accent
            )
        }
        item {
            MonthCalendar(
                year = today.year,
                month = today.monthValue,
                today = today,
                checkedDates = checkedDates,
                accent = accent,
                onDayClick = { date ->
                    if (date.isAfter(today)) {
                        scope.launch { snackbar.showSnackbar("还没到那一天呢") }
                    } else if (checkedDates.contains(date)) {
                        scope.launch {
                            container.checkInRepository.cancel(date)
                            snackbar.showSnackbar("已取消 ${CheckInCalculator.labelOf(date)} 的打卡")
                        }
                    } else {
                        scope.launch {
                            val ok = container.checkInRepository.checkIn(date)
                            snackbar.showSnackbar(
                                if (ok) "已补打卡 ${CheckInCalculator.labelOf(date)}" else "该日期已有记录"
                            )
                        }
                    }
                }
            )
        }

        // ---------- 打卡记录 ----------
        item {
            SectionHeader(
                title = "打卡记录",
                subtitle = if (records.isEmpty()) "还没有记录" else "共 ${records.size} 条，点击可删除",
                accent = accent
            )
        }
        if (records.isEmpty()) {
            item {
                com.example.jieqi.ui.components.EmptyState(
                    text = "从今天开始记录吧",
                    hint = "每天花 10 秒打卡，一年后就是一份完整的节气手账"
                )
            }
        } else {
            items(records, key = { it.date }) { record ->
                val date = TermDateUtils.fromDbDate(record.date)
                JieQiCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        date?.let { d ->
                            scope.launch {
                                container.checkInRepository.cancel(d)
                                snackbar.showSnackbar("已删除 ${record.date} 的打卡")
                            }
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = com.example.jieqi.data.content.SolarTermContent
                                .byName(record.termName)?.color?.copy(alpha = 0.14f)
                                ?: MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = record.termName.take(1),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = com.example.jieqi.data.content.SolarTermContent
                                        .byName(record.termName)?.color ?: accent
                                )
                            }
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.date,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "所处节气：${record.termName}" +
                                    (record.note?.let { "　·　$it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "删除",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // ---------- 打卡备注弹窗 ----------
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("记录今日心情（可跳过）") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { if (it.length <= 60) noteText = it },
                    placeholder = { Text("例如：今天吃了春饼，很香") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showNoteDialog = false
                    scope.launch {
                        val ok = container.checkInRepository.checkIn(today, noteText)
                        noteText = ""
                        snackbar.showSnackbar(if (ok) "打卡成功，连续 ${stats.currentStreak + 1} 天" else "今天已经打过卡啦")
                    }
                }) { Text("确认打卡") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNoteDialog = false
                    scope.launch {
                        val ok = container.checkInRepository.checkIn(today)
                        noteText = ""
                        snackbar.showSnackbar(if (ok) "打卡成功，连续 ${stats.currentStreak + 1} 天" else "今天已经打过卡啦")
                    }
                }) { Text("跳过") }
            }
        )
    }
}

/**
 * 月度打卡日历：点击某天可以补打卡 / 取消打卡。
 */
@Composable
private fun MonthCalendar(
    year: Int,
    month: Int,
    today: LocalDate,
    checkedDates: Set<LocalDate>,
    accent: Color,
    onDayClick: (LocalDate) -> Unit
) {
    val cells = remember(year, month) { CheckInCalculator.monthGrid(year, month) }
    JieQiCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                CheckInCalculator.WEEK_LABELS.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                cells.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        week.forEach { date ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (date != null) {
                                    val checked = checkedDates.contains(date)
                                    val isToday = date == today
                                    val termOccurrence = remember(date) { TermDateUtils.termOn(date) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.92f)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    checked -> accent
                                                    termOccurrence != null -> accent.copy(alpha = 0.16f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { onDayClick(date) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${date.dayOfMonth}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = when {
                                                checked -> Color.White
                                                isToday -> accent
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                            fontWeight = if (isToday || checked) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                LegendDot(color = accent, text = "已打卡")
                LegendDot(color = accent.copy(alpha = 0.16f), text = "节气日")
                LegendDot(color = Color.Transparent, text = "未打卡", outlined = true)
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, text: String, outlined: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (outlined) {
                        Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    } else Modifier
                )
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
