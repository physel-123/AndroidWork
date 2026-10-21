package com.example.jieqi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jieqi.data.content.DailyContentFactory
import com.example.jieqi.data.content.ArtworkLibrary
import com.example.jieqi.domain.TermDateUtils
import com.example.jieqi.ui.LocalAppContainer
import com.example.jieqi.ui.LocalSnackbarHostState
import com.example.jieqi.ui.components.ArtworkFullScreen
import com.example.jieqi.ui.components.ArtworkGallery
import com.example.jieqi.ui.components.GradientCard
import com.example.jieqi.ui.components.InfoChip
import com.example.jieqi.ui.components.PhenologyRow
import com.example.jieqi.ui.components.SectionHeader
import com.example.jieqi.ui.components.StatCell
import com.example.jieqi.ui.components.ThinProgressBar
import com.example.jieqi.ui.theme.TermTitleStyle
import com.example.jieqi.util.ShareUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 首页：每日节气推荐 + 一键打卡 + 今日民俗小贴士。
 *
 * 设计目标是打开 App 三秒内就能获得“今天是什么节气、该做什么”的信息，
 * 并顺手完成打卡，形成每日触达的闭环。
 */
@Composable
fun HomeScreen(
    onOpenTerm: (Int) -> Unit,
    onOpenCheckIn: () -> Unit,
    onOpenMap: () -> Unit,
    onRequireLogin: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val today = remember { LocalDate.now() }
    // 首页按“精确时刻”判定当前节气：节气当天在时刻到达之前仍显示上一个节气，
    // 这样“当前节气”与“下一个节气”不会出现同一个节气
    val occurrence = remember { TermDateUtils.currentOccurrence(LocalDateTime.now()) }
    val next = remember { TermDateUtils.nextOccurrence(today, LocalTime.now()) }
    val accent = occurrence.term.color

    var justCheckedIn by remember { mutableStateOf(false) }

    val stats by container.checkInRepository.stats.collectAsStateWithLifecycle(
        initialValue = com.example.jieqi.domain.CheckInStats()
    )
    val favoriteCount by container.favoriteRepository.count.collectAsStateWithLifecycle(initialValue = 0)
    val customCount by container.folkCustomRepository.all.collectAsStateWithLifecycle(initialValue = emptyList())

    val checkedInTodayFlow = remember(today) { container.checkInRepository.observeCheckedIn(today) }
    val checkedInToday by checkedInTodayFlow.collectAsStateWithLifecycle(initialValue = false)
    val isCheckedIn = checkedInToday || justCheckedIn
    val loggedIn by container.userRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    var zoomArtwork by remember { mutableStateOf<com.example.jieqi.data.content.Artwork?>(null) }

    /** 打卡：未登录先去登录。 */
    fun doCheckIn(note: String? = null) {
        if (!loggedIn) {
            onRequireLogin()
            return
        }
        scope.launch {
            val ok = container.checkInRepository.checkIn(today, note)
            justCheckedIn = ok
            snackbar.showSnackbar(
                if (ok) "打卡成功，已连续 ${stats.currentStreak + 1} 天" else "今天已经打过卡啦"
            )
        }
    }

    val customToday = remember(today) { DailyContentFactory.dailyCustom(occurrence, today) }
    val foodToday = remember(today) { DailyContentFactory.dailyFood(occurrence, today) }
    val greeting = remember { DailyContentFactory.greeting(LocalTime.now().hour) }
    val artworks = remember(occurrence.index) { ArtworkLibrary.of(occurrence.index) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------- 顶部问候 ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = TermDateUtils.formatDate(today),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accent.copy(alpha = 0.12f),
                    modifier = Modifier.clickable {
                        ShareUtils.shareText(
                            context,
                            DailyContentFactory.shareTermText(next)
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "分享",
                            tint = accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = "今日分享",
                            style = MaterialTheme.typography.labelLarge,
                            color = accent
                        )
                    }
                }
            }
        }

        // ---------- 当前节气主卡 ----------
        item {
            GradientCard(color = accent, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = occurrence.term.shortLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = occurrence.name,
                                style = TermTitleStyle,
                                color = Color.White
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = occurrence.term.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        }
                        Text(
                            text = "${occurrence.index}/24",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "下一个节气 · ${next.name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = TermDateUtils.formatDateTime(next.dateTime),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = TermDateUtils.countdownText(),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTerm(occurrence.index) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "查看${occurrence.name}详情",
                                style = MaterialTheme.typography.labelLarge,
                                color = accent
                            )
                            Spacer(Modifier.size(6.dp))
                            Icon(
                                imageVector = Icons.Filled.Forward,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // ---------- 节气配画 ----------
        if (artworks.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "节气画卷",
                    subtitle = "古人笔下的${occurrence.name}",
                    accent = accent
                )
            }
            item {
                ArtworkGallery(
                    artworks = artworks,
                    onZoom = { zoomArtwork = it }
                )
            }
        }

        // ---------- 打卡卡 ----------
        item {
            GradientCard(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "跟着节气过日子",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = if (isCheckedIn) "今天已打卡，明天继续" else "今天还没有打卡哦",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        StatCell(
                            value = "${stats.currentStreak}",
                            label = "连续天数",
                            accent = Color.White
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { doCheckIn() }
                        ) {
                            Text(
                                text = if (isCheckedIn) "今日已打卡" else "立即打卡",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenCheckIn() }
                        ) {
                            Text(
                                text = "打卡日历",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // ---------- 今日民俗 ----------
        item {
            SectionHeader(
                title = "今日一俗",
                subtitle = "节气里的老规矩，一天学一条",
                accent = accent
            )
        }

        item {
            com.example.jieqi.ui.components.JieQiCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        com.example.jieqi.ui.components.SealBadge(
                            text = occurrence.name.take(1),
                            color = accent
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = customToday,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(occurrence.term.foods) { food ->
                            InfoChip(
                                text = "时令 · $food",
                                containerColor = accent.copy(alpha = 0.12f),
                                contentColor = accent
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "今日宜食：$foodToday",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ---------- 三候 ----------
        item {
            SectionHeader(
                title = "${occurrence.name}三候",
                subtitle = "古人五日为一候，记录物候变化",
                accent = accent
            )
        }
        item {
            com.example.jieqi.ui.components.JieQiCard(modifier = Modifier.fillMaxWidth()) {
                PhenologyRow(items = occurrence.term.phenology, accent = accent)
            }
        }

        // ---------- 养生 + 诗句 ----------
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                com.example.jieqi.ui.components.JieQiCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Text(
                            text = "顺时养生",
                            style = MaterialTheme.typography.titleSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = occurrence.term.health,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                com.example.jieqi.ui.components.JieQiCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Text(
                            text = "节气诗话",
                            style = MaterialTheme.typography.titleSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = occurrence.term.poem,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = occurrence.term.poemFrom,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---------- 进度与数据 ----------
        item {
            SectionHeader(title = "我的节气足迹", accent = accent)
        }
        item {
            com.example.jieqi.ui.components.JieQiCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatCell(value = "${stats.total}", label = "累计打卡", accent = accent)
                        StatCell(value = "${stats.visitedTerms}/24", label = "点亮节气", accent = accent)
                        StatCell(value = "$favoriteCount", label = "收藏", accent = accent)
                        StatCell(value = "${customCount.size}", label = "民俗条目", accent = accent)
                    }
                    Spacer(Modifier.height(14.dp))
                    ThinProgressBar(progress = stats.visitedTerms / 24f, color = accent)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "传习等级：${stats.level}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "查看民俗地图 →",
                            style = MaterialTheme.typography.bodySmall,
                            color = accent,
                            modifier = Modifier.clickable { onOpenMap() }
                        )
                    }
                }
            }
        }
    }

    // ---------- 全屏看图 ----------
    zoomArtwork?.let { artwork ->
        ArtworkFullScreen(artwork = artwork, onDismiss = { zoomArtwork = null })
    }
}

