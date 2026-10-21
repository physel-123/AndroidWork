package com.example.jieqi.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 路由表：集中管理所有页面路径。
 */
object JieQiRoutes {

    const val HOME = "home"
    const val TERMS = "terms"
    const val FEED = "feed"
    const val CHECK_IN = "check_in"
    const val MAP = "map"
    const val PROFILE = "profile"
    const val FAVORITES = "favorites"
    const val CREDITS = "credits"

    const val ARG_TERM_INDEX = "termIndex"
    const val TERM_DETAIL = "term/{$ARG_TERM_INDEX}"

    fun termDetail(index: Int): String = "term/$index"

    /** 只有一级页面显示底部导航栏。 */
    fun shouldShowBottomBar(route: String?): Boolean = route in setOf(
        HOME, TERMS, FEED, CHECK_IN, MAP, PROFILE
    )
}

/** 底部导航项定义。 */
private data class BottomItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomItems = listOf(
    BottomItem(JieQiRoutes.HOME, "首页", Icons.Filled.Home, Icons.Outlined.Home),
    BottomItem(JieQiRoutes.TERMS, "节气", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    BottomItem(JieQiRoutes.FEED, "广场", Icons.Filled.Forum, Icons.Outlined.Forum),
    BottomItem(JieQiRoutes.CHECK_IN, "打卡", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    BottomItem(JieQiRoutes.MAP, "民俗地图", Icons.Filled.Map, Icons.Outlined.Map),
    BottomItem(JieQiRoutes.PROFILE, "我的", Icons.Filled.Person, Icons.Outlined.Person)
)

/**
 * 底部导航栏。
 */
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        bottomItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}
