package com.example.jieqi.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 视觉设计令牌（Design Tokens）。
 *
 * 配色取自传统色：朱砂、黛青、竹青、藤黄、藕荷、米白……
 * 24 节气各自有主题色，用于卡片、环形图与详情页渐变，形成“一节气一色”的辨识度。
 */

// ---------- 基础色 ----------
val RiceWhite = Color(0xFFF7F2E8)      // 宣纸米白
val InkBlack = Color(0xFF2B2018)       // 墨色
val Cinnabar = Color(0xFF8F3A2E)       // 朱砂
val Amber = Color(0xFFD9932F)          // 琥珀
val BambooGreen = Color(0xFF3E7C6A)    // 竹青
val IndigoBlue = Color(0xFF2F4A6D)     // 靛青
val LotusPink = Color(0xFFC97B84)      // 藕荷
val PlumRed = Color(0xFF9E3B4E)        // 梅红
val DarkInk = Color(0xFF17130F)        // 夜墨（深色模式背景）

// ---------- 季节主题色 ----------
val SeasonSpring = Color(0xFF3E7C6A)   // 春 · 青
val SeasonSummer = Color(0xFFC0562F)   // 夏 · 赤
val SeasonAutumn = Color(0xFFD9932F)   // 秋 · 金
val SeasonWinter = Color(0xFF2F4A6D)   // 冬 · 靛

/** 24 节气主题色，顺序与 [com.example.jieqi.data.content.SolarTermContent] 中的节气一一对应。 */
val TermColors: List<Color> = listOf(
    Color(0xFF6E9C4B), // 立春
    Color(0xFF4E9A8A), // 雨水
    Color(0xFF7BA23F), // 惊蛰
    Color(0xFF3E8E70), // 春分
    Color(0xFF5AA36B), // 清明
    Color(0xFF9BBF6A), // 谷雨
    Color(0xFFD98B3A), // 立夏
    Color(0xFFCFAE3C), // 小满
    Color(0xFFB9A23F), // 芒种
    Color(0xFFC2703A), // 夏至
    Color(0xFFD9694A), // 小暑
    Color(0xFFC4492F), // 大暑
    Color(0xFFC98A3C), // 立秋
    Color(0xFFB0763C), // 处暑
    Color(0xFFCBA96B), // 白露
    Color(0xFFB9925A), // 秋分
    Color(0xFFA98C5E), // 寒露
    Color(0xFF8F7A55), // 霜降
    Color(0xFF7E6E63), // 立冬
    Color(0xFF6B7A8F), // 小雪
    Color(0xFF4E6E8E), // 大雪
    Color(0xFF3E5C7A), // 冬至
    Color(0xFF4A5A6B), // 小寒
    Color(0xFF40506B)  // 大寒
)

/** 取节气主题色；越界时回退到品牌主色。 */
fun termColor(index: Int): Color = TermColors.getOrElse(index) { Cinnabar }

