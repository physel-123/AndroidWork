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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jieqi.data.content.DailyContentFactory
import com.example.jieqi.data.content.SolarTermContent
import com.example.jieqi.data.db.FolkCustomEntity
import com.example.jieqi.domain.TermDateUtils
import com.example.jieqi.ui.LocalAppContainer
import com.example.jieqi.ui.LocalSnackbarHostState
import com.example.jieqi.ui.components.EmptyState
import com.example.jieqi.ui.components.FolkMapCanvas
import com.example.jieqi.ui.components.JieQiCard
import com.example.jieqi.ui.components.MapPoint
import com.example.jieqi.ui.components.SectionHeader
import com.example.jieqi.ui.components.SelectableChip
import com.example.jieqi.ui.theme.Cinnabar
import com.example.jieqi.util.ShareUtils
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 地域民俗地图页。
 *
 * 核心思路：把“养在深闺人未识”的地方民俗按经纬度投射到地图上，
 * 用户既能看到全国分布，也能按省份/节气筛选，还能投稿自己家乡的民俗。
 */
@Composable
fun FolkMapScreen(onRequireLogin: () -> Unit = {}) {
    val container = LocalAppContainer.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val customs by container.folkCustomRepository.all.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    val today = remember { LocalDate.now() }
    val currentTerm = remember(today) { TermDateUtils.currentOccurrence(today) }

    var keyword by remember { mutableStateOf("") }
    var provinceFilter by remember { mutableStateOf<String?>(null) }
    var termFilter by remember { mutableStateOf<String?>(null) }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val provinces = remember(customs) { customs.map { it.province }.distinct().sorted() }

    val filtered = remember(customs, keyword, provinceFilter, termFilter) {
        customs.filter { custom ->
            (provinceFilter == null || custom.province == provinceFilter) &&
                (termFilter == null || custom.termName == termFilter) &&
                (keyword.isBlank() || custom.title.contains(keyword) ||
                    custom.city.contains(keyword) || custom.description.contains(keyword))
        }
    }

    val mapPoints = remember(filtered) {
        filtered.map {
            MapPoint(
                id = it.id,
                title = it.title,
                region = "${it.province} ${it.city}",
                longitude = it.longitude,
                latitude = it.latitude,
                termName = it.termName
            )
        }
    }

    val selected = remember(selectedId, customs) { customs.firstOrNull { it.id == selectedId } }

    // 参与筛选的节气列表（在 composable 作用域内计算，不能放进 LazyRow 的 scope 里）
    val termNames = remember(customs) {
        customs.mapNotNull { it.termName }.distinct()
            .sortedBy { SolarTermContent.byName(it)?.index ?: 99 }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---------- 标题 ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "民俗地图", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "共收录 ${customs.size} 条地方民俗，覆盖 ${provinces.size} 个省级地区",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { showAddDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            text = "投稿",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // ---------- 地图 ----------
        item {
            JieQiCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    FolkMapCanvas(
                        points = mapPoints,
                        selectedId = selectedId,
                        onPointClick = { point ->
                            selectedId = if (selectedId == point.id) null else point.id
                        },
                        highlightProvince = provinceFilter,
                        onProvinceClick = { province ->
                            provinceFilter = if (provinceFilter == province) null else province
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Cinnabar)
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = if (provinceFilter != null) {
                                    "已选中 $provinceFilter（再点一次取消）"
                                } else {
                                    "点击圆点看民俗，点击省份可筛选"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "筛出 ${mapPoints.size} 条",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---------- 选中详情 ----------
        selected?.let { custom ->
            item {
                val accent = custom.termName
                    ?.let { SolarTermContent.byName(it)?.color }
                    ?: MaterialTheme.colorScheme.primary
                JieQiCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = custom.title,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "分享",
                                style = MaterialTheme.typography.labelLarge,
                                color = accent,
                                modifier = Modifier.clickable {
                                    ShareUtils.shareText(
                                        context,
                                        DailyContentFactory.shareCustomText(
                                            custom.title,
                                            "${custom.province} ${custom.city}",
                                            custom.termName,
                                            custom.description
                                        )
                                    )
                                }
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${custom.province} · ${custom.city}" +
                                (custom.termName?.let { "　|　$it" } ?: "　|　全年民俗"),
                            style = MaterialTheme.typography.bodySmall,
                            color = accent
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = custom.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!custom.isBuiltIn) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "删除这条投稿",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.clickable {
                                    scope.launch {
                                        container.folkCustomRepository.delete(custom)
                                        selectedId = null
                                        snackbar.showSnackbar("已删除投稿")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // ---------- 搜索 ----------
        item {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("搜索民俗、地区或关键词") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            )
        }

        // ---------- 省份筛选 ----------
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    SelectableChip(
                        text = "全部省份",
                        selected = provinceFilter == null,
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = { provinceFilter = null }
                    )
                }
                items(provinces) { province ->
                    SelectableChip(
                        text = province,
                        selected = provinceFilter == province,
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = {
                            provinceFilter = if (provinceFilter == province) null else province
                        }
                    )
                }
            }
        }

        // ---------- 节气筛选 ----------
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    SelectableChip(
                        text = "全部节气",
                        selected = termFilter == null,
                        accent = currentTerm.term.color,
                        onClick = { termFilter = null }
                    )
                }
                item {
                    SelectableChip(
                        text = "当前·${currentTerm.name}",
                        selected = termFilter == currentTerm.name,
                        accent = currentTerm.term.color,
                        onClick = {
                            termFilter = if (termFilter == currentTerm.name) null else currentTerm.name
                        }
                    )
                }
                items(termNames) { termName ->
                    SelectableChip(
                        text = termName,
                        selected = termFilter == termName,
                        accent = SolarTermContent.byName(termName)?.color
                            ?: MaterialTheme.colorScheme.primary,
                        onClick = {
                            termFilter = if (termFilter == termName) null else termName
                        }
                    )
                }
            }
        }

        // ---------- 列表 ----------
        item {
            SectionHeader(
                title = "民俗条目",
                subtitle = "按省份与城市排列",
                accent = MaterialTheme.colorScheme.primary
            )
        }
        if (filtered.isEmpty()) {
            item {
                EmptyState(
                    text = "没有符合条件的民俗",
                    hint = "试试清除筛选条件，或者投稿你家乡的民俗"
                )
            }
        } else {
            items(filtered, key = { it.id }) { custom ->
                FolkCustomRow(
                    custom = custom,
                    accent = custom.termName
                        ?.let { SolarTermContent.byName(it)?.color }
                        ?: MaterialTheme.colorScheme.primary,
                    onClick = { selectedId = custom.id }
                )
            }
        }
    }

    // ---------- 投稿弹窗 ----------
    if (showAddDialog) {
        AddCustomDialog(
            defaultTerm = currentTerm.name,
            onDismiss = { showAddDialog = false },
            onSubmit = { entity ->
                showAddDialog = false
                scope.launch {
                    container.folkCustomRepository.addUserCustom(entity)
                    snackbar.showSnackbar("投稿成功，感谢你为家乡民俗留下一笔记录")
                }
            }
        )
    }
}

/**
 * 民俗投稿弹窗：让用户补充“我家的民俗”，体现 UGC 与文化共建。
 */
@Composable
private fun AddCustomDialog(
    defaultTerm: String,
    onDismiss: () -> Unit,
    onSubmit: (FolkCustomEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var termName by remember { mutableStateOf(defaultTerm) }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    /** 简易省份中心坐标表：投稿时按省份自动定位到地图上。 */
    val provinceCenters = remember {
        mapOf(
            "北京" to (116.41 to 39.90), "天津" to (117.20 to 39.13), "上海" to (121.47 to 31.23),
            "重庆" to (106.55 to 29.56), "河北" to (114.50 to 38.04), "山西" to (112.55 to 37.87),
            "辽宁" to (123.43 to 41.80), "吉林" to (125.32 to 43.90), "黑龙江" to (126.53 to 45.80),
            "江苏" to (118.78 to 32.06), "浙江" to (120.15 to 30.27), "安徽" to (117.28 to 31.86),
            "福建" to (119.30 to 26.08), "江西" to (115.89 to 28.68), "山东" to (117.00 to 36.65),
            "河南" to (113.65 to 34.76), "湖北" to (114.31 to 30.52), "湖南" to (112.98 to 28.19),
            "广东" to (113.26 to 23.13), "广西" to (108.32 to 22.82), "海南" to (110.35 to 20.02),
            "四川" to (104.07 to 30.57), "贵州" to (106.71 to 26.57), "云南" to (102.71 to 25.05),
            "西藏" to (91.11 to 29.97), "陕西" to (108.94 to 34.34), "甘肃" to (103.82 to 36.06),
            "青海" to (101.78 to 36.62), "宁夏" to (106.27 to 38.47), "新疆" to (87.62 to 43.79),
            "内蒙古" to (111.65 to 40.82), "香港" to (114.17 to 22.32), "澳门" to (113.55 to 22.20),
            "台湾" to (121.52 to 25.03)
        )
    }

    fun centerOf(provinceName: String): Pair<Double, Double> {
        val key = provinceCenters.keys.firstOrNull {
            provinceName.contains(it) || it.contains(provinceName)
        }
        return provinceCenters[key] ?: (108.94 to 34.34)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("投稿我家的民俗") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("民俗名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = province,
                        onValueChange = { province = it },
                        label = { Text("省份") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("城市") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = termName,
                    onValueChange = { termName = it },
                    label = { Text("相关节气（如：立春）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("民俗介绍") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    title.isBlank() -> error = "请填写民俗名称"
                    province.isBlank() -> error = "请填写省份"
                    city.isBlank() -> error = "请填写城市"
                    description.isBlank() -> error = "请简单介绍一下这条民俗"
                    else -> {
                        val (lon, lat) = centerOf(province)
                        onSubmit(
                            FolkCustomEntity(
                                title = title.trim(),
                                termName = termName.trim().ifBlank { null },
                                province = province.trim(),
                                city = city.trim(),
                                latitude = lat,
                                longitude = lon,
                                description = description.trim(),
                                isBuiltIn = false
                            )
                        )
                    }
                }
            }) { Text("提交") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
