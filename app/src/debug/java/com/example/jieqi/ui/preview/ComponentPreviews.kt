package com.example.jieqi.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jieqi.ui.components.EmptyState
import com.example.jieqi.ui.components.FolkMapCanvas
import com.example.jieqi.ui.components.GradientCard
import com.example.jieqi.ui.components.InfoChip
import com.example.jieqi.ui.components.JieQiCard
import com.example.jieqi.ui.components.MapPoint
import com.example.jieqi.ui.components.PhenologyRow
import com.example.jieqi.ui.components.SealBadge
import com.example.jieqi.ui.components.SectionHeader
import com.example.jieqi.ui.components.SelectableChip
import com.example.jieqi.ui.components.StatCell
import com.example.jieqi.ui.components.TermWheel
import com.example.jieqi.ui.components.ThinProgressBar
import com.example.jieqi.ui.theme.JieQiTheme
import com.example.jieqi.ui.theme.termColor
import com.example.jieqi.util.UserAvatar

/**
 * 组件预览（仅 debug 源集，不参与 release 打包）。
 *
 * 说明：页面级 Composable（HomeScreen、TermListScreen 等）依赖
 * [com.example.jieqi.ui.LocalAppContainer]，其默认实现会抛异常，
 * 且容器构造时会打开 Room 与 DataStore，
 * 这两者在 Compose Preview 的 layoutlib 渲染环境里无法运行，
 * 因此这里只预览不依赖容器的纯组件。
 */

private const val SEAL_INDEX = 18 // 立冬，TermColors 下标

@Preview(name = "01 节气罗盘", showBackground = true, widthDp = 360, heightDp = 400)
@Composable
private fun TermWheelPreview() {
    JieQiTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TermWheel(
                currentIndex = SEAL_INDEX + 1,
                onTermClick = {},
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Preview(name = "02 卡片与标签", showBackground = true, widthDp = 360, heightDp = 660)
@Composable
private fun CardsPreview() {
    JieQiTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionHeader(title = "今日节气", subtitle = "立冬 · 11月7日")

                GradientCard(color = termColor(SEAL_INDEX)) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SealBadge("19", Color.White)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "立冬",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "11月7日 — 11月21日",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Text(
                            text = "水始冰，地始冻，万物收藏。",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }

                JieQiCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SectionHeader(title = "三候", subtitle = "立冬物候")
                        PhenologyRow(
                            items = listOf("一候 水始冰", "二候 地始冻", "三候 雉入大水为蜃"),
                            accent = termColor(SEAL_INDEX)
                        )
                        ThinProgressBar(progress = 0.78f, color = termColor(SEAL_INDEX))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatCell(value = "19", label = "已过节气", accent = termColor(SEAL_INDEX))
                            StatCell(value = "42", label = "累计打卡", accent = termColor(SEAL_INDEX))
                            StatCell(value = "6", label = "连续天数", accent = termColor(SEAL_INDEX))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoChip("吃饺子")
                            InfoChip("补冬")
                            InfoChip("贺冬")
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectableChip("全部", true, termColor(SEAL_INDEX), onClick = {})
                    SelectableChip("春", false, termColor(SEAL_INDEX), onClick = {})
                    SelectableChip("夏", false, termColor(SEAL_INDEX), onClick = {})
                    SelectableChip("秋", false, termColor(SEAL_INDEX), onClick = {})
                }
            }
        }
    }
}

@Preview(name = "03 地域民俗地图", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun FolkMapPreview() {
    JieQiTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(12.dp)) {
                SectionHeader(title = "地域民俗", subtitle = "按纬度分布的节气习俗")
                FolkMapCanvas(
                    points = previewPoints,
                    selectedId = 2L,
                    onPointClick = {},
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(name = "04 头像与空状态", showBackground = true, widthDp = 360, heightDp = 260)
@Composable
private fun SmallPartsPreview() {
    JieQiTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatar(nickname = "孟树豪", colorIndex = SEAL_INDEX, size = 48.dp)
                    UserAvatar(nickname = "任春满", colorIndex = 4, size = 36.dp)
                    UserAvatar(nickname = "王佳乐", colorIndex = 11, size = 36.dp)
                    UserAvatar(nickname = "高旨骐", colorIndex = 21, size = 36.dp)
                }
                EmptyState(text = "还没有打卡记录", hint = "完成一次节令打卡后，这里会出现你的足迹")
            }
        }
    }
}

private val previewPoints = listOf(
    MapPoint(id = 1L, title = "冬至吃饺子", region = "北京", longitude = 116.4, latitude = 39.9, termName = "冬至"),
    MapPoint(id = 2L, title = "立春咬春", region = "山东", longitude = 117.0, latitude = 36.6, termName = "立春"),
    MapPoint(id = 3L, title = "清明踏青", region = "江苏", longitude = 118.8, latitude = 32.0, termName = "清明"),
    MapPoint(id = 4L, title = "夏至吃面", region = "陕西", longitude = 108.9, latitude = 34.3, termName = "夏至"),
    MapPoint(id = 5L, title = "秋分祭月", region = "浙江", longitude = 120.2, latitude = 30.3, termName = "秋分"),
    MapPoint(id = 6L, title = "霜降登高", region = "四川", longitude = 104.1, latitude = 30.6, termName = "霜降"),
    MapPoint(id = 7L, title = "立冬补冬", region = "广东", longitude = 113.3, latitude = 23.1, termName = "立冬")
)
