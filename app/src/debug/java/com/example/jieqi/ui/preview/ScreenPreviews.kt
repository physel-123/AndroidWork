package com.example.jieqi.ui.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jieqi.data.content.SolarTerm
import com.example.jieqi.data.content.SolarTermContent
import com.example.jieqi.data.db.FolkCustomEntity
import com.example.jieqi.domain.CheckInStats
import com.example.jieqi.domain.TermDateUtils
import com.example.jieqi.domain.TermOccurrence
import com.example.jieqi.ui.screens.HomeContent
import com.example.jieqi.ui.screens.LoginContent
import com.example.jieqi.ui.screens.TermDetailContent
import com.example.jieqi.ui.screens.TermListContent
import com.example.jieqi.ui.theme.JieQiTheme

/**
 * 页面级预览（仅 debug 源集）。
 *
 * 四个页面已拆成「外壳 Screen + 无状态 Content」：
 * Screen 负责从 AppContainer 读状态，Content 只接收数据与回调，
 * 因此预览只需构造数据，不必启动数据库与网络。
 *
 * 这些预览对应的正是第 4 章需要的「Android Studio 界面设计视图」截图。
 */

// ---------- 预览数据 ----------

private const val PREVIEW_TERM_INDEX = 19 // 立冬

private val previewTerms: List<TermOccurrence> = TermDateUtils.occurrencesOfYear(2026)
private val previewTerm: SolarTerm = SolarTermContent.byIndex(PREVIEW_TERM_INDEX)!!
private val previewCurrent: TermOccurrence = previewTerms.first { it.index == PREVIEW_TERM_INDEX }
private val previewNext: TermOccurrence = previewTerms.first { it.index == PREVIEW_TERM_INDEX + 1 }

private val previewCustoms = listOf(
    FolkCustomEntity(
        id = 1L,
        title = "立冬吃饺子",
        termName = "立冬",
        province = "北京",
        city = "东城区",
        latitude = 39.93,
        longitude = 116.42,
        description = "老北京讲究“立冬补冬，补嘴空”，饺子取“交子之时”的谐音，图个热乎。"
    ),
    FolkCustomEntity(
        id = 2L,
        title = "腌菜迎冬",
        termName = "立冬",
        province = "山东",
        city = "潍坊",
        latitude = 36.71,
        longitude = 119.16,
        description = "立冬前后把萝卜、白菜入缸腌制，是一冬的家常滋味。"
    )
)

// ---------- 页面预览 ----------

@Preview(name = "01 首页", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun HomeScreenPreview() {
    JieQiTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeContent(
                occurrence = previewCurrent,
                next = previewNext,
                greeting = "早上好",
                stats = CheckInStats(
                    total = 42,
                    currentStreak = 6,
                    longestStreak = 9,
                    thisMonth = 8,
                    visitedTerms = 19,
                    level = "节气行家"
                ),
                favoriteCount = 5,
                customCount = 3,
                isCheckedIn = false
            )
        }
    }
}

@Preview(name = "02 二十四节气", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun TermListScreenPreview() {
    JieQiTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TermListContent(
                favoriteNames = listOf("立春", "清明"),
                onOpenTerm = {}
            )
        }
    }
}

@Preview(name = "03 节气详情", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun TermDetailScreenPreview() {
    JieQiTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TermDetailContent(
                term = previewTerm,
                occurrence = previewCurrent,
                isFavorite = true,
                relatedCustoms = previewCustoms,
                commentSlot = {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "评论区在预览中以占位形式呈现",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

@Preview(name = "04 登录与注册", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun LoginScreenPreview() {
    JieQiTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LoginContent()
        }
    }
}
