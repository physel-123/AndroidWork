package com.example.jieqi.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.jieqi.ui.theme.termColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 把 assets 下的相对路径转成 Coil 可加载的 URI。
 *
 * 统一走 Coil 的好处：静态图、GIF 动图、视频首帧都由同一个组件处理，
 * 后续想换成网络图或视频也不需要改动界面代码。
 */
fun assetImageUrl(path: String): String =
    if (path.startsWith("file://") || path.startsWith("http")) path
    else "file:///android_asset/$path"

/**
 * 节气配图。
 *
 * @param path assets 相对路径，如 `images/term01_37145.jpg`
 */
@Composable
fun AssetImage(
    path: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(assetImageUrl(path))
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

/**
 * 用户头像：用昵称首字 + 节气主题色圆形底，避免依赖头像图片资源。
 *
 * @param colorIndex 0..23，对应 24 节气主题色
 */
@Composable
fun UserAvatar(
    nickname: String,
    colorIndex: Int,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val color = termColor(colorIndex)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nickname.take(1),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp
        )
    }
}

/** 相对时间文案：刚刚 / x 分钟前 / x 小时前 / 昨天 / x 天前 / 具体日期。 */
fun relativeTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val diff = now - timestamp
    if (diff < 0) return "刚刚"
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        diff < minute -> "刚刚"
        diff < hour -> "${diff / minute} 分钟前"
        diff < day -> "${diff / hour} 小时前"
        diff < 2 * day -> "昨天"
        diff < 7 * day -> "${diff / day} 天前"
        else -> SimpleDateFormat("M月d日", Locale.CHINA).format(Date(timestamp))
    }
}

/** 发布时间文案：今天 14:30 / 昨天 / 3月5日。 */
fun postTimeText(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    val today = Calendar.getInstance().apply { timeInMillis = now }
    val sameYear = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
    val sameDay = sameYear && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    val timeText = SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(timestamp))
    return when {
        sameDay -> "今天 $timeText"
        else -> SimpleDateFormat(if (sameYear) "M月d日 HH:mm" else "yyyy年M月d日", Locale.CHINA)
            .format(Date(timestamp))
    }
}

/**
 * 从 assets 读取一个 Drawable（供需要 Drawable 的场景使用）。
 */
fun loadAssetDrawable(context: Context, path: String): Drawable? = runCatching {
    val stream = context.assets.open(path)
    Drawable.createFromStream(stream, null)
}.getOrNull()
