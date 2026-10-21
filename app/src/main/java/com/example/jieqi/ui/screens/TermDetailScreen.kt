package com.example.jieqi.ui.screens

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jieqi.data.content.DailyContentFactory
import com.example.jieqi.data.content.ArtworkLibrary
import com.example.jieqi.data.content.SolarTermContent
import com.example.jieqi.data.db.FolkCustomEntity
import com.example.jieqi.domain.TermDateUtils
import com.example.jieqi.ui.LocalAppContainer
import com.example.jieqi.ui.LocalSnackbarHostState
import com.example.jieqi.ui.components.ArtworkFullScreen
import com.example.jieqi.ui.components.ArtworkGallery
import com.example.jieqi.ui.components.CommentSection
import com.example.jieqi.ui.components.EmptyState
import com.example.jieqi.ui.components.GradientCard
import com.example.jieqi.ui.components.InfoChip
import com.example.jieqi.ui.components.JieQiCard
import com.example.jieqi.ui.components.PhenologyRow
import com.example.jieqi.ui.components.SectionHeader
import com.example.jieqi.ui.theme.TermTitleStyle
import com.example.jieqi.util.ShareUtils
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 节气详情页：一个节气的完整文化档案。
 *
 * 内容结构：精确时刻 → 由来释义 → 三候 → 传统习俗 → 时令食俗 → 顺时养生
 * → 节气诗话 → 各地民俗（联动民俗地图数据）。
 */
@Composable
fun TermDetailScreen(
    termIndex: Int,
    onBack: () -> Unit,
    onOpenFolkMap: () -> Unit,
    onRequireLogin: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val term = remember(termIndex) { SolarTermContent.byIndex(termIndex) }
    val year = remember { LocalDate.now().year }
    val occurrence = remember(termIndex, year) {
        TermDateUtils.occurrencesOfYear(year).first { it.index == termIndex }
    }

    val favoriteKey = remember(term) { term?.let { "term:${it.name}" } ?: "" }
    val isFavorite by container.favoriteRepository.isFavorite(favoriteKey)
        .collectAsStateWithLifecycle(initialValue = false)

    val folkCustoms by container.folkCustomRepository.all.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    val relatedCustoms = remember(folkCustoms, term) {
        folkCustoms.filter { it.termName == null || it.termName == term?.name }
    }
    var zoomArtwork by remember { mutableStateOf<com.example.jieqi.data.content.Artwork?>(null) }

    if (term == null) {
        EmptyState(text = "未找到该节气")
        return
    }

    val accent = term.color
    val artworks = remember(termIndex) { ArtworkLibrary.of(termIndex) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------- 顶部操作栏 ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    scope.launch {
                        val added = container.favoriteRepository.toggleTerm(term)
                        snackbar.showSnackbar(if (added) "已收藏《${term.name}》" else "已取消收藏")
                    }
                }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (isFavorite) accent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = {
                    ShareUtils.shareText(context, DailyContentFactory.shareTermText(occurrence))
                }) {
                    Icon(Icons.Filled.Share, contentDescription = "分享")
                }
            }
        }

        // ---------- 主视觉 ----------
        item {
            GradientCard(color = accent, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "${term.season.label}季 · 第 ${term.index} 个节气",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = term.name,
                        style = TermTitleStyle,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = term.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = TermDateUtils.formatDate(occurrence.date),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = TermDateUtils.formatDateTime(occurrence.dateTime),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "太阳到达黄经 ${term.longitude}°　传统日期 ${term.dateRange}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // ---------- 节气画卷 ----------
        if (artworks.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "节气画卷",
                    subtitle = "古人笔下的${term.name} · 共 ${artworks.size} 幅",
                    accent = accent
                )
            }
            item {
                ArtworkGallery(artworks = artworks, onZoom = { zoomArtwork = it })
            }
        }

        // ---------- 由来释义 ----------
        item { SectionHeader(title = "由来与含义", accent = accent) }
        item {
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = term.meaning,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // ---------- 三候 ----------
        item { SectionHeader(title = "三候物候", subtitle = "古人五日为一候，观察自然变化", accent = accent) }
        item {
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                PhenologyRow(items = term.phenology, accent = accent)
            }
        }

        // ---------- 传统习俗 ----------
        item { SectionHeader(title = "传统习俗", subtitle = "共 ${term.customs.size} 条", accent = accent) }
        items(term.customs.size) { i ->
            val custom = term.customs[i]
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.Top) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accent.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "${i + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Text(text = custom, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // ---------- 时令食俗 ----------
        item { SectionHeader(title = "时令食俗", accent = accent) }
        item {
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(term.foods) { food ->
                        InfoChip(
                            text = food,
                            containerColor = accent.copy(alpha = 0.14f),
                            contentColor = accent
                        )
                    }
                }
            }
        }

        // ---------- 养生 ----------
        item { SectionHeader(title = "顺时养生", accent = accent) }
        item {
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = term.health, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // ---------- 诗话 ----------
        item { SectionHeader(title = "节气诗话", accent = accent) }
        item {
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = term.poem,
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = FontStyle.Italic,
                        color = accent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "—— ${term.poemFrom}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ---------- 各地民俗 ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "各地民俗",
                    subtitle = "共 ${relatedCustoms.size} 条 · 点击查看地图分布",
                    accent = accent,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "地图 →",
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    modifier = Modifier.clickable { onOpenFolkMap() }
                )
            }
        }
        if (relatedCustoms.isEmpty()) {
            item { EmptyState(text = "暂无该节气的民俗条目") }
        } else {
            items(relatedCustoms, key = { it.id }) { custom ->
                FolkCustomRow(custom = custom, accent = accent, onClick = onOpenFolkMap)
            }
        }

        // ---------- 互动区 ----------
        item {
            SectionHeader(
                title = "节气讨论",
                subtitle = "说说你那里的${term.name}",
                accent = accent
            )
        }
        item {
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                CommentSection(
                    type = com.example.jieqi.data.TargetType.TERM,
                    targetId = termIndex.toLong(),
                    title = "评论",
                    onRequireLogin = onRequireLogin
                )
            }
        }
    }

    // ---------- 全屏看图 ----------
    zoomArtwork?.let { artwork ->
        ArtworkFullScreen(artwork = artwork, onDismiss = { zoomArtwork = null })
    }
}

/** 民俗条目行（详情页与地图页共用）。 */
@Composable
fun FolkCustomRow(
    custom: FolkCustomEntity,
    accent: Color,
    onClick: () -> Unit
) {
    JieQiCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = custom.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.size(8.dp))
                if (!custom.isBuiltIn) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "我投稿的",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${custom.province} · ${custom.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = accent
                )
                custom.termName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = custom.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
