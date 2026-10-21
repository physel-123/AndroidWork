package com.example.jieqi.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.jieqi.ui.theme.Cinnabar
import kotlin.math.hypot

/**
 * 地图上的一个数据点。
 */
data class MapPoint(
    val id: Long,
    val title: String,
    val region: String,
    val longitude: Double,
    val latitude: Double,
    val termName: String?
)

/**
 * 地域民俗地图。
 *
 * 坐标系统一：**底图轮廓与数据点都使用真实经纬度**，再经同一个线性投影换算到画布，
 * 因此数据点必然落在正确的省份内。
 *
 * （早期版本底图用的是手工编的“归一化轮廓”，与经纬度投影不是同一套坐标系，
 *   导致所有点整体偏向东南、看起来“没有重合”，现已修正。）
 *
 * @param points 要展示的民俗数据点
 * @param selectedId 当前选中的数据点 id
 * @param onPointClick 点击数据点回调
 * @param onProvinceClick 点击省份回调（可空）
 */
@Composable
fun FolkMapCanvas(
    points: List<MapPoint>,
    selectedId: Long?,
    onPointClick: (MapPoint) -> Unit,
    modifier: Modifier = Modifier,
    highlightProvince: String? = null,
    onProvinceClick: ((String) -> Unit)? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary

    // 记录每个数据点的画布坐标，供点击命中判断
    val hitPoints = remember { mutableMapOf<Long, Offset>() }
    // 记录每个省份的重心，供点击省份判断（用数组避免重组时被重置）
    val provinceHits = remember { mutableListOf<Triple<String, Offset, Float>>() }

    val provinceLabelStyle = remember { TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Medium) }
    val pointLabelStyle = remember {
        TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(MAP_ASPECT)
            .pointerInput(points, selectedId) {
                detectTapGestures { tap ->
                    // 优先命中数据点，其次命中省份
                    val nearestPoint = hitPoints.minByOrNull { (_, p) ->
                        hypot((p.x - tap.x).toDouble(), (p.y - tap.y).toDouble())
                    }
                    if (nearestPoint != null) {
                        val d = hypot(
                            (nearestPoint.value.x - tap.x).toDouble(),
                            (nearestPoint.value.y - tap.y).toDouble()
                        )
                        if (d <= size.width * 0.045f) {
                            points.firstOrNull { it.id == nearestPoint.key }
                                ?.let { onPointClick(it) }
                            return@detectTapGestures
                        }
                    }
                    if (onProvinceClick != null) {
                        provinceHits.minByOrNull { (_, p, _) ->
                            hypot((p.x - tap.x).toDouble(), (p.y - tap.y).toDouble())
                        }?.let { (name, center, radius) ->
                            if (hypot(
                                    (center.x - tap.x).toDouble(),
                                    (center.y - tap.y).toDouble()
                                ) <= radius
                            ) {
                                onProvinceClick(name)
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(MAP_ASPECT)) {
            val w = size.width
            val h = size.height
            val inset = w * 0.03f
            val usableW = w - inset * 2
            val usableH = h - inset * 2

            /** 经纬度 → 画布坐标（与底图轮廓共用）。 */
            fun project(lon: Double, lat: Double): Offset {
                val x = ((lon - LON_MIN) / (LON_MAX - LON_MIN)).coerceIn(0.0, 1.0)
                val y = ((LAT_MAX - lat) / (LAT_MAX - LAT_MIN)).coerceIn(0.0, 1.0)
                return Offset(inset + (x * usableW).toFloat(), inset + (y * usableH).toFloat())
            }

            // ---------- 省份轮廓 ----------
            provinceHits.clear()
            ProvinceOutlines.shapes.forEach { (name, coords) ->
                val path = Path()
                var sumX = 0f
                var sumY = 0f
                var minX = Float.MAX_VALUE
                var maxX = -Float.MAX_VALUE
                coords.forEachIndexed { index, (lon, lat) ->
                    val o = project(lon, lat)
                    if (index == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y)
                    sumX += o.x
                    sumY += o.y
                    if (o.x < minX) minX = o.x
                    if (o.x > maxX) maxX = o.x
                }
                path.close()

                val highlighted = highlightProvince == name
                drawPath(
                    path = path,
                    color = if (highlighted) primary.copy(alpha = 0.22f)
                    else surfaceVariant.copy(alpha = 0.8f)
                )
                drawPath(
                    path = path,
                    color = if (highlighted) primary else outline.copy(alpha = 0.5f),
                    style = Stroke(width = if (highlighted) 2f else 1.1f)
                )

                // 省份名：标在多边形重心
                val center = Offset(sumX / coords.size, sumY / coords.size)
                val layout = textMeasurer.measure(name, provinceLabelStyle)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        center.x - layout.size.width / 2f,
                        center.y - layout.size.height / 2f
                    ),
                    color = if (highlighted) primary else onSurface.copy(alpha = 0.32f)
                )
                // 命中半径用外接宽度的一半做粗略估计
                provinceHits += Triple(name, center, ((maxX - minX) / 2f).coerceAtLeast(w * 0.05f))
            }

            // ---------- 数据点 ----------
            hitPoints.clear()
            val placedLabels = mutableListOf<androidx.compose.ui.geometry.Rect>()

            points.forEach { point ->
                val offset = project(point.longitude, point.latitude)
                hitPoints[point.id] = offset

                val selected = point.id == selectedId
                val dotColor = if (selected) primary else onSurface.copy(alpha = 0.6f)
                val radius = if (selected) w * 0.022f else w * 0.012f

                // 外圈光晕 + 实心点
                drawCircle(
                    color = dotColor.copy(alpha = if (selected) 0.3f else 0.16f),
                    radius = radius * 2.0f,
                    center = offset
                )
                drawCircle(color = dotColor, radius = radius, center = offset)

                // 标签：只给选中点标注，并做避让，避免东部密集区互相重叠
                if (selected) {
                    val layout: TextLayoutResult = textMeasurer.measure(point.title, pointLabelStyle)
                    val labelW = layout.size.width.toFloat()
                    val labelH = layout.size.height.toFloat()
                    val gap = radius + 6f

                    // 依次尝试：右上 → 左上 → 右下 → 左下 → 正上 → 正下
                    val candidates = listOf(
                        Offset(offset.x + gap, offset.y - labelH - gap),
                        Offset(offset.x - labelW - gap, offset.y - labelH - gap),
                        Offset(offset.x + gap, offset.y + gap),
                        Offset(offset.x - labelW - gap, offset.y + gap),
                        Offset(offset.x - labelW / 2f, offset.y - labelH - gap * 2),
                        Offset(offset.x - labelW / 2f, offset.y + gap * 2)
                    )
                    val boxW = labelW + 12f
                    val boxH = labelH + 6f
                    val chosen = candidates.firstOrNull { pos ->
                        val rect = androidx.compose.ui.geometry.Rect(
                            pos.x - 6f, pos.y - 3f, pos.x - 6f + boxW, pos.y - 3f + boxH
                        )
                        rect.left >= 0f && rect.top >= 0f && rect.right <= w && rect.bottom <= h &&
                            placedLabels.none { it.overlaps(rect) }
                    } ?: candidates.first()

                    val rect = androidx.compose.ui.geometry.Rect(
                        chosen.x - 6f, chosen.y - 3f, chosen.x - 6f + boxW, chosen.y - 3f + boxH
                    )
                    placedLabels += rect

                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.94f),
                        topLeft = Offset(rect.left, rect.top),
                        size = Size(rect.width, rect.height),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(chosen.x, chosen.y),
                        color = primary
                    )
                }
            }
        }
    }
}

/** 投影范围：中国陆地大致范围。 */
internal const val LON_MIN = 73.0
internal const val LON_MAX = 135.6
internal const val LAT_MIN = 17.5
internal const val LAT_MAX = 54.0

/**
 * 地图宽高比。
 *
 * 等距圆柱投影直接按经纬度线性映射，会让中国版图显得“又高又瘦”
 * （因为经度 1° 的实际距离要乘以 cos(纬度)）。
 * 按中纬度 35° 校正：宽高比 = (经度跨度 / 纬度跨度) × cos(35°) ≈ 1.4。
 */
internal const val MAP_ASPECT = 1.4f
