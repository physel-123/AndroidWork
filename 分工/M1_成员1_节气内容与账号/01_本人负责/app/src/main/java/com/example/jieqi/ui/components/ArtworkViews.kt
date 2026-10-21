package com.example.jieqi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.jieqi.data.content.Artwork
import com.example.jieqi.util.AssetImage
import com.example.jieqi.util.assetImageUrl

/**
 * 配画展示区。
 *
 * 中国古画多为立轴/扇面/长卷，直接按卡片裁切会破坏构图，
 * 因此这里统一用「宣纸底色 + Fit 居中」的方式完整展示画面，
 * 底部另附一行小字署名（作品名 · 作者 · 年代 · 地区）。
 *
 * @param artworks 该节气的配画列表
 * @param ratio 展示区宽高比，1.4 左右比正方形更接近画卷观感
 * @param onZoom 点击放大回调（可空）
 */
@Composable
fun ArtworkGallery(
    artworks: List<Artwork>,
    modifier: Modifier = Modifier,
    ratio: Float = 1.35f,
    onZoom: ((Artwork) -> Unit)? = null
) {
    if (artworks.isEmpty()) return
    val main = artworks.first()

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio)
                .clip(RoundedCornerShape(18.dp))
                .background(PaperColor)
                .then(if (onZoom != null) Modifier.clickable { onZoom(main) } else Modifier)
        ) {
            AssetImage(
                path = main.file,
                contentDescription = main.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // 右下角放大提示
            if (onZoom != null) {
                Surface(
                    shape = RoundedCornerShape(topStart = 12.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.ZoomIn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "点击看大图",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = main.creditLine,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 第二幅（如果有）
        if (artworks.size > 1) {
            Spacer(Modifier.height(10.dp))
            val second = artworks[1]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PaperColor)
                    .then(if (onZoom != null) Modifier.clickable { onZoom(second) } else Modifier)
            ) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxSize()
                        .background(PaperColor)
                ) {
                    AssetImage(
                        path = second.file,
                        contentDescription = second.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = second.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOf(second.artist, second.date, second.culture)
                            .filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = second.medium,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 节气卡片上的小图（列表用），按 Cover 裁切，尺寸固定避免列表跳动。
 */
@Composable
fun ArtworkThumb(
    artwork: Artwork,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 88.dp,
    height: androidx.compose.ui.unit.Dp = 88.dp
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(14.dp))
            .background(PaperColor)
    ) {
        AssetImage(
            path = artwork.file,
            contentDescription = artwork.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * 全屏看图。
 *
 * 用深色底衬托画面，点击任意位置或右上角按钮关闭。
 */
@Composable
fun ArtworkFullScreen(
    artwork: Artwork,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
            .clickable(onClick = onDismiss)
    ) {
        // 顶部信息
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
        ) {
            Text(
                text = artwork.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = listOf(artwork.artist, artwork.date, artwork.culture)
                    .filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = artwork.medium,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.55f)
            )
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White.copy(alpha = 0.16f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clickable(onClick = onDismiss)
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "关闭",
                tint = Color.White,
                modifier = Modifier.padding(8.dp)
            )
        }

        AssetImage(
            path = artwork.file,
            contentDescription = artwork.title,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 96.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "馆藏：大都会艺术博物馆（公共领域 CC0）\n${artwork.sourceUrl}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
        )
    }
}

/** 宣纸底色：比纯白柔和，衬古画更协调。 */
val PaperColor = Color(0xFFF3ECDD)

/** 顶部渐变遮罩，让白色文字压在图片上也能看清。 */
fun topScrim(): Brush = Brush.verticalGradient(
    colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent)
)
