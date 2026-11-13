package com.example.jieqi.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.jieqi.data.remote.FortuneResponse
import com.example.jieqi.data.remote.SignResponse
import com.example.jieqi.data.remote.TarotResponse
import com.example.jieqi.data.remote.VisionResponse
import com.example.jieqi.ui.LocalAppContainer
import com.example.jieqi.ui.components.JieQiCard
import com.example.jieqi.ui.components.SectionHeader
import kotlinx.coroutines.launch

/**
 * 「神秘文化」页面：今日运势、抽签、塔罗、手相识读。
 *
 * 数据全部来自后端 FastAPI 接口（运势/占卜为规则引擎，手相为多模态识别）。
 */
@Composable
fun MysticScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()

    // 将各功能的状态提升到此处，避免 LazyColumn 复用 item 时状态被重置
    val fortuneState = rememberFortuneState(container)
    val signState = rememberSignState(container)
    val tarotState = rememberTarotState(container)
    val palmState = rememberPalmState(container)
    val faceState = rememberFaceState(container)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "header") {
            Text(
                text = "神秘文化",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "传统民俗趣味体验 · 内容仅供娱乐参考",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 今日运势
        item(key = "fortune") { FortuneSection(fortuneState) }

        // 抽签
        item(key = "sign") { SignSection(signState) }

        // 塔罗
        item(key = "tarot") { TarotSection(tarotState) }

        // 手相
        item(key = "palm") { PalmSection(palmState) }

        // 面相
        item(key = "face") { FaceSection(faceState) }
    }
}

/* ─────────────────────────── 今日运势 ─────────────────────────── */

private val ZODIACS = listOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")

private class FortuneState(val container: com.example.jieqi.data.AppContainer) {
    var zodiac by mutableStateOf("龙")
    var loading by mutableStateOf(false)
    var result by mutableStateOf<FortuneResponse?>(null)
    var error by mutableStateOf<String?>(null)
}

@Composable
private fun rememberFortuneState(container: com.example.jieqi.data.AppContainer): FortuneState {
    return remember { FortuneState(container) }
}

@Composable
private fun FortuneSection(state: FortuneState) {
    val scope = rememberCoroutineScope()

    SectionHeader(title = "今日运势", subtitle = "选一个生肖，看看今天的运程")

    JieQiCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // 生肖选择：横向 6 个、竖向 2 行
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ZODIACS.chunked(6).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { z ->
                            val selected = z == state.zodiac
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { state.zodiac = z },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = z,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        state.loading = true
                        state.error = null
                        state.result = null
                        state.container.mysticRepository.getFortune(state.zodiac).fold(
                            onSuccess = { state.result = it },
                            onFailure = { state.error = it.message }
                        )
                        state.loading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.loading
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("查看今日运势")
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            state.result?.let { r ->
                FortuneResult(r)
            }
        }
    }
}

@Composable
private fun FortuneResult(r: FortuneResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "综合运势",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Row {
                repeat(5) { i ->
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (i < r.overallStars) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Text(r.summary, style = MaterialTheme.typography.bodyMedium)

        // 四维度
        r.dimensions.forEach { (dim, d) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dim,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.width(40.dp)
                )
                Row(modifier = Modifier.width(110.dp)) {
                    repeat(5) { i ->
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (i < d.stars) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = d.text,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoTag("幸运色 ${r.luckyColor}")
            InfoTag("幸运数字 ${r.luckyNumber}")
        }
        Text("宜：${r.suitable}", style = MaterialTheme.typography.bodySmall)
        Text("忌：${r.avoid}", style = MaterialTheme.typography.bodySmall)
        Text(
            text = r.disclaimer,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

/* ─────────────────────────── 抽签 ─────────────────────────── */

private class SignState(val container: com.example.jieqi.data.AppContainer) {
    var loading by mutableStateOf(false)
    var result by mutableStateOf<SignResponse?>(null)
    var error by mutableStateOf<String?>(null)
    var initialized by mutableStateOf(false)
}

@Composable
private fun rememberSignState(container: com.example.jieqi.data.AppContainer): SignState {
    return remember { SignState(container) }
}

@Composable
private fun SignSection(state: SignState) {
    val scope = rememberCoroutineScope()

    // 进入页面时自动加载当天已抽的签
    LaunchedEffect(state.initialized) {
        if (!state.initialized) {
            state.container.mysticRepository.todaySign().fold(
                onSuccess = {
                    if (it != null) state.result = it
                },
                onFailure = { /* 静默失败，首次加载不弹错 */ }
            )
            state.initialized = true
        }
    }

    SectionHeader(title = "灵签占卜", subtitle = "心诚则灵，抽取今日签文")

    JieQiCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        state.loading = true
                        state.error = null
                        state.result = null
                        state.container.mysticRepository.drawSign().fold(
                            onSuccess = { state.result = it },
                            onFailure = { state.error = it.message }
                        )
                        state.loading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.loading
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (state.result == null) "抽取一支签" else "再抽一支（覆盖）")
                }
            }
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            state.result?.let { s ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "今日签文 · ${s.date}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = s.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Row {
                            repeat(5) { i ->
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = if (i < s.level) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = s.poem,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(s.interpret, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = s.disclaimer,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/* ─────────────────────────── 塔罗 ─────────────────────────── */

private class TarotState(val container: com.example.jieqi.data.AppContainer) {
    var loading by mutableStateOf(false)
    var result by mutableStateOf<TarotResponse?>(null)
    var error by mutableStateOf<String?>(null)
    var initialized by mutableStateOf(false)
}

@Composable
private fun rememberTarotState(container: com.example.jieqi.data.AppContainer): TarotState {
    return remember { TarotState(container) }
}

@Composable
private fun TarotSection(state: TarotState) {
    val scope = rememberCoroutineScope()

    // 进入页面时自动加载当天已翻的牌
    LaunchedEffect(state.initialized) {
        if (!state.initialized) {
            state.container.mysticRepository.todayTarot().fold(
                onSuccess = {
                    if (it != null) state.result = it
                },
                onFailure = { /* 静默失败 */ }
            )
            state.initialized = true
        }
    }

    SectionHeader(title = "塔罗牌阵", subtitle = "抽取三张牌，观过去·现在·未来")

    JieQiCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        state.loading = true
                        state.error = null
                        state.result = null
                        state.container.mysticRepository.drawTarot(count = 3).fold(
                            onSuccess = { state.result = it },
                            onFailure = { state.error = it.message }
                        )
                        state.loading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.loading
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (state.result == null) "翻开三张牌" else "重新洗牌（覆盖）")
                }
            }
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            state.result?.let { t ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "今日牌阵 · ${t.date}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    t.cards.forEach { card ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = card.name.take(1),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${card.position} · ${card.name}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = card.orientation,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(card.meaning, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Text(t.summary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = t.disclaimer,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/* ─────────────────────────── 手相 ─────────────────────────── */

private class PalmState(val container: com.example.jieqi.data.AppContainer) {
    var imageUri by mutableStateOf<Uri?>(null)
    var loading by mutableStateOf(false)
    var result by mutableStateOf<VisionResponse?>(null)
    var error by mutableStateOf<String?>(null)
}

@Composable
private fun rememberPalmState(container: com.example.jieqi.data.AppContainer): PalmState {
    return remember { PalmState(container) }
}

@Composable
private fun PalmSection(state: PalmState) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) state.imageUri = uri }

    SectionHeader(title = "手相识读", subtitle = "上传手掌照片，AI 识读掌纹特征")

    JieQiCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // 图片预览
            state.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "手掌照片",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("选择照片")
                }
                Button(
                    onClick = {
                        val uri = state.imageUri
                        if (uri == null) {
                            state.error = "请先选择一张手掌照片"
                        } else {
                            scope.launch {
                                state.loading = true
                                state.error = null
                                state.result = null
                                val bytes = runCatching {
                                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                }.getOrNull()
                                if (bytes == null) {
                                    state.error = "读取图片失败，请重试"
                                } else {
                                    state.container.mysticRepository.analyzePalm(bytes).fold(
                                        onSuccess = { state.result = it },
                                        onFailure = { state.error = it.message }
                                    )
                                }
                                state.loading = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !state.loading
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("开始识读")
                    }
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            state.result?.let { r ->
                if (!r.recognized) {
                    Text(
                        text = r.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        r.items.forEach { item ->
                            Column {
                                Row {
                                    Text(
                                        text = item.dimension,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(64.dp)
                                    )
                                    Text(
                                        text = item.feature,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(item.interpret, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text(
                            text = r.closing,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = r.disclaimer,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/* ─────────────────────────── 面相 ─────────────────────────── */

private class FaceState(val container: com.example.jieqi.data.AppContainer) {
    var imageUri by mutableStateOf<Uri?>(null)
    var loading by mutableStateOf(false)
    var result by mutableStateOf<VisionResponse?>(null)
    var error by mutableStateOf<String?>(null)
}

@Composable
private fun rememberFaceState(container: com.example.jieqi.data.AppContainer): FaceState {
    return remember { FaceState(container) }
}

@Composable
private fun FaceSection(state: FaceState) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) state.imageUri = uri }

    SectionHeader(title = "面相解读", subtitle = "上传面部照片，AI 识读五官特征")

    JieQiCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // 图片预览
            state.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "面部照片",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("选择照片")
                }
                Button(
                    onClick = {
                        val uri = state.imageUri
                        if (uri == null) {
                            state.error = "请先选择一张面部照片"
                        } else {
                            scope.launch {
                                state.loading = true
                                state.error = null
                                state.result = null
                                val bytes = runCatching {
                                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                }.getOrNull()
                                if (bytes == null) {
                                    state.error = "读取图片失败，请重试"
                                } else {
                                    state.container.mysticRepository.analyzeFace(bytes).fold(
                                        onSuccess = { state.result = it },
                                        onFailure = { state.error = it.message }
                                    )
                                }
                                state.loading = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !state.loading
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("开始识读")
                    }
                }
            }

            // 隐私提示
            Text(
                text = "隐私说明：照片仅用于本次面相分析，在取得你的授权后上传，分析完成后不会留存。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            state.result?.let { r ->
                if (!r.recognized) {
                    Text(
                        text = r.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        r.items.forEach { item ->
                            Column {
                                Row {
                                    Text(
                                        text = item.dimension,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(64.dp)
                                    )
                                    Text(
                                        text = item.feature,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(item.interpret, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text(
                            text = r.closing,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = r.disclaimer,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
