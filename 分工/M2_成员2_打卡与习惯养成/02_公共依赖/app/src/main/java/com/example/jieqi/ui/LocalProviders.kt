package com.example.jieqi.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.jieqi.data.AppContainer

/**
 * 全局依赖容器（通过 CompositionLocal 提供，避免在 UI 层到处传递 Context）。
 *
 * 使用 staticCompositionLocalOf：容器在应用生命周期内不会变化，
 * 读取时不需要额外的重组跟踪，性能更友好。
 */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer 未初始化，请确认已在 MainActivity 中提供")
}

/**
 * 全局提示（Snackbar）入口：由根组件注入，便于任意页面弹出提示。
 */
val LocalSnackbarHostState = staticCompositionLocalOf { androidx.compose.material3.SnackbarHostState() }
