package com.example.jieqi.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jieqi.data.content.SolarTermContent
import com.example.jieqi.domain.TermDateUtils
import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * 节气罗盘：把二十四节气按太阳黄经顺序排列成一个圆环。
 *
 * 这是本项目的“颜值担当”组件，也是节气数据可视化的核心：
 * 圆环上一圈正好是一年，当前节气高亮显示并置于 12 点方向，
 * 点击任意节气即可进入详情页。
 *
 * @param currentIndex 当前节气序号（1..24）
 * @param onTermClick 点击节气回调
 */
@Composable
fun TermWheel(
    currentIndex: Int,
    onTermClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    year: Int = LocalDate.now().year
) {
    val textMeasurer = rememberTextMeasurer()
    val accent = com.example.jieqi.ui.theme.termColor(currentIndex - 1)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val ringColor = MaterialTheme.colorScheme.surfaceVariant

    // 记录每个节气在画布上的落点，用于点击命中判断
    val hitPoints = remember { mutableMapOf<Int, Offset>() }
    // 用 remember 保存环半径，避免每次重组把值重置为 0 导致点击失效
    val radiusState = remember { floatArrayOf(0f) }

    val nameStyle = remember {
        TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
    }
    val dateStyle = remember {
        TextStyle(fontSize = 9.sp, textAlign = TextAlign.Center)
    }
    val centerNameStyle = remember {
        TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
    val centerHintStyle = remember {
        TextStyle(fontSize = 11.sp, textAlign = TextAlign.Center)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(currentIndex) {
                    detectTapGestures { tap ->
                        val lastRadius = radiusState[0]
                        if (lastRadius <= 0f) return@detectTapGestures
                        val nearest = hitPoints.minByOrNull { (_, point) ->
                            hypot((point.x - tap.x).toDouble(), (point.y - tap.y).toDouble())
                        } ?: return@detectTapGestures
                        val distance = hypot(
                            (nearest.value.x - tap.x).toDouble(),
                            (nearest.value.y - tap.y).toDouble()
                        )
                        val threshold = lastRadius * 0.10
                        if (distance <= threshold) onTermClick(nearest.key)
                    }
                }
        ) {
            val size = size.minDimension
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = size * 0.38f
            radiusState[0] = radius
            hitPoints.clear()

            // 装饰底环：轻微渐变让罗盘更有质感
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.10f), Color.Transparent),
                    center = center,
                    radius = radius * 1.45f
                ),
                radius = radius * 1.42f,
                center = center
            )
            // 节气环
            drawCircle(
                color = ringColor,
                radius = radius,
                center = center,
                style = Stroke(width = size * 0.012f)
            )
            drawCircle(
                color = onSurface.copy(alpha = 0.10f),
                radius = radius * 0.72f,
                center = center,
                style = Stroke(width = size * 0.004f)
            )

            // 当前节气的弧段高亮（15° 一段）
            val currentAngle = -90f + (currentIndex - 1) * 15f - 7.5f
            drawArc(
                color = accent,
                startAngle = currentAngle,
                sweepAngle = 15f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = size * 0.024f)
            )

            // 24 个节气刻度与文字
            for (i in 1..24) {
                val angleDeg = -90.0 + (i - 1) * 15.0
                val rad = Math.toRadians(angleDeg)
                val cosA = cos(rad).toFloat()
                val sinA = sin(rad).toFloat()
                val isCurrent = i == currentIndex

                val point = Offset(
                    center.x + radius * cosA,
                    center.y + radius * sinA
                )
                hitPoints[i] = point

                // 刻度点
                drawCircle(
                    color = if (isCurrent) accent else onSurface.copy(alpha = 0.35f),
                    radius = if (isCurrent) size * 0.018f else size * 0.010f,
                    center = point
                )

                val term = SolarTermContent.byIndex(i) ?: continue
                val labelDistance = radius * 1.20f
                val labelCenter = Offset(
                    center.x + labelDistance * cosA,
                    center.y + labelDistance * sinA
                )

                val nameLayout = textMeasurer.measure(term.name, nameStyle)
                if (isCurrent) {
                    // 高亮项的胶囊底
                    drawRoundRect(
                        color = accent.copy(alpha = 0.16f),
                        topLeft = Offset(
                            labelCenter.x - nameLayout.size.width / 2f - 6f,
                            labelCenter.y - nameLayout.size.height / 2f - 4f
                        ),
                        size = Size(
                            nameLayout.size.width + 12f,
                            nameLayout.size.height + 8f
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                }
                drawText(
                    textLayoutResult = nameLayout,
                    topLeft = Offset(
                        labelCenter.x - nameLayout.size.width / 2f,
                        labelCenter.y - nameLayout.size.height / 2f
                    ),
                    color = if (isCurrent) accent else onSurface
                )

                // 当前节气再加一行日期，信息量更足
                if (isCurrent) {
                    val dateText = TermDateUtils.occurrencesOfYear(year)
                        .firstOrNull { it.index == i }
                        ?.let { "${it.date.monthValue}/${it.date.dayOfMonth}" } ?: ""
                    if (dateText.isNotEmpty()) {
                        val dateLayout = textMeasurer.measure(dateText, dateStyle)
                        drawText(
                            textLayoutResult = dateLayout,
                            topLeft = Offset(
                                labelCenter.x - dateLayout.size.width / 2f,
                                labelCenter.y + nameLayout.size.height / 2f + 2f
                            ),
                            color = onSurfaceVariant
                        )
                    }
                }
            }

            // 圆心：当前节气大字
            val term = SolarTermContent.byIndex(currentIndex)
            if (term != null) {
                val centerLayout = textMeasurer.measure(term.name, centerNameStyle)
                drawText(
                    textLayoutResult = centerLayout,
                    topLeft = Offset(
                        center.x - centerLayout.size.width / 2f,
                        center.y - centerLayout.size.height / 2f - size * 0.03f
                    ),
                    color = onSurface
                )
                val hint = textMeasurer.measure("${term.season.label} · 黄经 ${term.longitude}°", centerHintStyle)
                drawText(
                    textLayoutResult = hint,
                    topLeft = Offset(
                        center.x - hint.size.width / 2f,
                        center.y + centerLayout.size.height / 2f + size * 0.01f
                    ),
                    color = onSurfaceVariant
                )
            }
        }
    }
}
