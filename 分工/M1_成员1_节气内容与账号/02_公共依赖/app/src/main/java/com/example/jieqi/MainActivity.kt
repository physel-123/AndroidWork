package com.example.jieqi

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jieqi.data.AppContainer
import com.example.jieqi.data.ProfileSettings
import com.example.jieqi.data.ThemeMode
import com.example.jieqi.ui.LocalAppContainer
import com.example.jieqi.ui.LocalSnackbarHostState
import com.example.jieqi.ui.navigation.BottomNavBar
import com.example.jieqi.ui.navigation.JieQiRoutes
import com.example.jieqi.ui.screens.CheckInScreen
import com.example.jieqi.ui.screens.FeedScreen
import com.example.jieqi.ui.screens.FolkMapScreen
import com.example.jieqi.ui.screens.HomeScreen
import com.example.jieqi.ui.screens.ImageCreditsScreen
import com.example.jieqi.ui.screens.LoginScreen
import com.example.jieqi.ui.screens.ProfileScreen
import com.example.jieqi.ui.screens.TermDetailScreen
import com.example.jieqi.ui.screens.TermListScreen
import com.example.jieqi.ui.theme.JieQiTheme

/**
 * 应用唯一的 Activity。
 *
 * 结构：单 Activity + Compose 导航。
 * 首次启动进入登录页，可「先逛逛」以游客身份浏览；
 * 打卡、评论、发布动态等操作会再次要求登录。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val container = (application as JieQiApp).container
        container.warmUp()

        setContent {
            JieQiAppRoot(container)
        }
    }
}

@Composable
private fun JieQiAppRoot(container: AppContainer) {
    // Android 13+ 申请通知权限，用于每日节气提醒
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* 用户拒绝也不影响其它功能 */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val profile by container.settings.profile.collectAsStateWithLifecycle(
        initialValue = ProfileSettings()
    )

    val darkTheme = when (profile.themeMode) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    JieQiTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(
            LocalAppContainer provides container,
            LocalSnackbarHostState provides snackbarHostState
        ) {
            AppContent(container)
        }
    }
}

@Composable
private fun AppContent(container: AppContainer) {
    val navController = rememberNavController()
    val snackbarHostState = LocalSnackbarHostState.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: JieQiRoutes.HOME

    // 登录状态：未登录时先展示登录页，但允许「先逛逛」以游客身份进入
    val loggedIn by container.userRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    var skippedLogin by remember { mutableStateOf(false) }

    if (!loggedIn && !skippedLogin) {
        LoginScreen(
            onLoggedIn = { skippedLogin = false },
            onSkip = { skippedLogin = true }
        )
        return
    }

    /** 游客触发需登录的操作时，回到登录页。 */
    fun requireLogin() {
        skippedLogin = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (JieQiRoutes.shouldShowBottomBar(currentRoute)) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(JieQiRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = JieQiRoutes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(JieQiRoutes.HOME) {
                HomeScreen(
                    onOpenTerm = { index -> navController.navigate(JieQiRoutes.termDetail(index)) },
                    onOpenCheckIn = { navController.navigate(JieQiRoutes.CHECK_IN) },
                    onOpenMap = { navController.navigate(JieQiRoutes.MAP) },
                    onRequireLogin = { requireLogin() }
                )
            }
            composable(JieQiRoutes.TERMS) {
                TermListScreen(
                    onOpenTerm = { index -> navController.navigate(JieQiRoutes.termDetail(index)) }
                )
            }
            composable(JieQiRoutes.FEED) {
                FeedScreen(onRequireLogin = { requireLogin() })
            }
            composable(JieQiRoutes.CHECK_IN) {
                CheckInScreen(onRequireLogin = { requireLogin() })
            }
            composable(JieQiRoutes.MAP) {
                FolkMapScreen(onRequireLogin = { requireLogin() })
            }
            composable(JieQiRoutes.PROFILE) {
                ProfileScreen(
                    onOpenFavorites = { navController.navigate(JieQiRoutes.FAVORITES) },
                    onOpenTerm = { index -> navController.navigate(JieQiRoutes.termDetail(index)) },
                    onOpenCredits = { navController.navigate(JieQiRoutes.CREDITS) },
                    onRequireLogin = { requireLogin() }
                )
            }
            composable(JieQiRoutes.TERM_DETAIL) { entry ->
                val index = entry.arguments?.getString(JieQiRoutes.ARG_TERM_INDEX)?.toIntOrNull() ?: 1
                TermDetailScreen(
                    termIndex = index,
                    onBack = { navController.popBackStack() },
                    onOpenFolkMap = { navController.navigate(JieQiRoutes.MAP) },
                    onRequireLogin = { requireLogin() }
                )
            }
            composable(JieQiRoutes.FAVORITES) {
                TermListScreen(
                    favoritesOnly = true,
                    onOpenTerm = { index -> navController.navigate(JieQiRoutes.termDetail(index)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(JieQiRoutes.CREDITS) {
                ImageCreditsScreen(onBack = { navController.popBackStack() })
            }
        }


    }
}
