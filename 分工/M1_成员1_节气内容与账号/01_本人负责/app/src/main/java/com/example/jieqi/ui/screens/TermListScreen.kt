package com.example.jieqi.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jieqi.data.content.Season
import com.example.jieqi.data.content.SolarTerm
import com.example.jieqi.data.content.SolarTermContent
import com.example.jieqi.domain.TermDateUtils
import com.example.jieqi.ui.LocalAppContainer
import com.example.jieqi.ui.components.EmptyState
import com.example.jieqi.ui.components.JieQiCard
import com.example.jieqi.ui.components.SealBadge
import com.example.jieqi.ui.components.SelectableChip
import com.example.jieqi.ui.components.TermWheel
import java.time.LocalDate

/**
 * 节气列表页。
 *
 * 顶部是“节气罗盘”可视化，下面支持按季节筛选、关键词搜索，
 * [favoritesOnly] 为 true 时切换为收藏列表模式（复用同一套卡片）。
 */
@Composable
fun TermListScreen(
    onOpenTerm: (Int) -> Unit,
    favoritesOnly: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    val container = LocalAppContainer.current
    val today = remember { LocalDate.now() }
    val currentIndex = remember { TermDateUtils.currentOccurrence(today).index }

    var keyword by remember { mutableStateOf("") }
    var season by remember { mutableStateOf<Season?>(null) }
    var wheelExpanded by remember { mutableStateOf(true) }

    val favorites by container.favoriteRepository.all.collectAsStateWithLifecycle(initialValue = emptyList())

    val favoriteTerms: List<SolarTerm> = remember(favorites) {
        favorites.mapNotNull { SolarTermContent.byName(it.title) }
    }

    val displayTerms: List<SolarTerm> = remember(keyword, season, favoritesOnly, favoriteTerms) {
        val base = if (favoritesOnly) {
            favoriteTerms.filter { term ->
                keyword.isBlank() || term.name.contains(keyword) || term.keywords.any { it.contains(keyword) }
            }
        } else {
            SolarTermContent.search(keyword)
        }
        season?.let { s -> base.filter { it.season == s } } ?: base
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---------- 标题栏 ----------
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (favoritesOnly) "我的收藏" else "二十四节气",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = if (favoritesOnly) {
                            "共收藏 ${favoriteTerms.size} 个节气"
                        } else {
                            "太阳每走 15° 为一个节气，一年二十四节气"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!favoritesOnly) {
                    Text(
                        text = if (wheelExpanded) "收起罗盘" else "展开罗盘",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { wheelExpanded = !wheelExpanded }
                    )
                }
            }
        }

        // ---------- 节气罗盘 ----------
        if (!favoritesOnly) {
            item {
                AnimatedVisibility(visible = wheelExpanded) {
                    JieQiCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TermWheel(
                                currentIndex = currentIndex,
                                onTermClick = onOpenTerm,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "点击任意节气查看详情，当前高亮为“${SolarTermContent.byIndex(currentIndex)?.name}”",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ---------- 搜索与筛选 ----------
        item {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("搜索节气、习俗或关键词（如：饺子、踏青）") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (keyword.isNotEmpty()) {
                        IconButton(onClick = { keyword = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "清空")
                        }
                    }
                }
            )
        }
        if (!favoritesOnly) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        SelectableChip(
                            text = "全部",
                            selected = season == null,
                            accent = MaterialTheme.colorScheme.primary,
                            onClick = { season = null }
                        )
                    }
                    items(Season.entries.toList()) { s ->
                        SelectableChip(
                            text = "${s.label}季",
                            selected = season == s,
                            accent = seasonColor(s),
                            onClick = { season = if (season == s) null else s }
                        )
                    }
                }
            }
        }

        // ---------- 结果统计 ----------
        item {
            Text(
                text = if (displayTerms.isEmpty()) "" else "共 ${displayTerms.size} 个节气",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---------- 卡片列表 ----------
        if (displayTerms.isEmpty()) {
            item {
                EmptyState(
                    text = if (favoritesOnly) "还没有收藏任何节气" else "没有找到相关节气",
                    hint = if (favoritesOnly) "在节气详情页点击收藏，方便日后回看" else "换个关键词试试，例如“清明”“汤圆”"
                )
            }
        } else {
            items(displayTerms, key = { it.index }) { term ->
                TermCard(
                    term = term,
                    highlighted = !favoritesOnly && term.index == currentIndex,
                    onClick = { onOpenTerm(term.index) }
                )
            }
        }
    }
}

/** 季节主题色。 */
fun seasonColor(season: Season) = when (season) {
    Season.SPRING -> com.example.jieqi.ui.theme.SeasonSpring
    Season.SUMMER -> com.example.jieqi.ui.theme.SeasonSummer
    Season.AUTUMN -> com.example.jieqi.ui.theme.SeasonAutumn
    Season.WINTER -> com.example.jieqi.ui.theme.SeasonWinter
}

/**
 * 节气卡片：印章徽标 + 名称 + 摘要 + 关键词标签。
 */
@Composable
fun TermCard(
    term: SolarTerm,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val accent = term.color
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (highlighted) accent.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SealBadge(
                    text = term.name.take(1),
                    color = accent,
                    size = if (highlighted) 48 else 42
                )
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = term.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (highlighted) {
                            Spacer(Modifier.size(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accent)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "当前",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }
                        }
                    }
                    Text(
                        text = "${term.season.label}季 · 公历 ${term.dateRange} · 黄经 ${term.longitude}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = term.summary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                term.keywords.take(3).forEach { kw ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accent.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = kw,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
