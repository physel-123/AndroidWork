package com.example.jieqi

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 冒烟测试（仪器化）：在真机 / 模拟器上验证核心界面能正常渲染与切换。
 *
 * 运行方式：连接设备后执行
 *   gradlew.bat :app:connectedDebugAndroidTest
 *
 * 说明：首页展示的节气取决于运行日期，因此这里只断言与日期无关的固定文案。
 */
@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 首页展示打卡与今日民俗入口() {
        composeRule.onNodeWithText("跟着节气过日子").assertIsDisplayed()
        composeRule.onNodeWithText("今日一俗").assertIsDisplayed()
        composeRule.onNodeWithText("我的节气足迹").assertIsDisplayed()
    }

    @Test
    fun 底部导航可切换到节气页与打卡页() {
        // 切到节气页：标题与罗盘说明为固定文案
        composeRule.onNodeWithText("节气").performClick()
        composeRule.onNodeWithText("二十四节气").assertIsDisplayed()

        // 切到打卡页
        composeRule.onNodeWithText("打卡").performClick()
        composeRule.onNodeWithText("节气打卡").assertIsDisplayed()
        composeRule.onNodeWithText("传习等级", substring = true).assertIsDisplayed()
    }

    @Test
    fun 一键打卡后首页显示已打卡() {
        composeRule.onNodeWithText("立即打卡").performClick()
        // 弹出心情备注对话框（可跳过）
        composeRule.onNodeWithText("跳过").performClick()
        composeRule.onNodeWithText("今日已打卡").assertIsDisplayed()
    }
}
