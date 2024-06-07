package com.axiel7.anihyou.ui.screens.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.axiel7.anihyou.common.firstBlocking
import com.axiel7.anihyou.data.model.DeepLink
import com.axiel7.anihyou.ui.common.BottomDestination.Companion.isBottomDestination
import com.axiel7.anihyou.ui.common.BottomDestination.Companion.toBottomDestinationIndex
import com.axiel7.anihyou.ui.common.Theme
import com.axiel7.anihyou.ui.common.navigation.NavActionManager
import com.axiel7.anihyou.ui.screens.home.HomeTab
import com.axiel7.anihyou.ui.screens.main.composables.MainBottomNavBar
import com.axiel7.anihyou.ui.screens.main.composables.MainNavigationRail
import com.axiel7.anihyou.ui.screens.profile.UserDetails
import com.axiel7.anihyou.ui.theme.AniHyouTheme
import com.axiel7.anihyou.ui.theme.dark_scrim
import com.axiel7.anihyou.ui.theme.light_scrim
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val deepLink = findDeepLink()

        //get necessary preferences while on splashscreen
        val initialIsLoggedIn = viewModel.isLoggedIn.firstBlocking()
        val initialTheme = viewModel.theme.firstBlocking()
        val initialUseBlackColors = viewModel.useBlackColors.firstBlocking()
        val initialAppColor = viewModel.appColor.firstBlocking()
        val initialAppColorMode = viewModel.appColorMode.firstBlocking()
        val startTab = viewModel.startTab.firstBlocking()
        val homeTab = viewModel.homeTab.firstBlocking() ?: HomeTab.DISCOVER
        val lastTabOpened = intent.action?.toBottomDestinationIndex() ?: startTab

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val theme by viewModel.theme.collectAsStateWithLifecycle(initialTheme)
            val isDark = if (theme == Theme.FOLLOW_SYSTEM) isSystemInDarkTheme()
            else theme == Theme.DARK
            val useBlackColors by viewModel.useBlackColors.collectAsStateWithLifecycle(
                initialValue = initialUseBlackColors
            )
            val appColor by viewModel.appColor.collectAsStateWithLifecycle(initialAppColor)
            val appColorMode by viewModel.appColorMode.collectAsStateWithLifecycle(
                initialValue = initialAppColorMode
            )
            val currentUserColor by viewModel.currentUserColor.collectAsStateWithLifecycle(null)
            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle(initialIsLoggedIn)

            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { isDark },
                    navigationBarStyle = SystemBarStyle.auto(
                        light_scrim.toArgb(),
                        dark_scrim.toArgb(),
                    ) { isDark },
                )
                onDispose {}
            }

            AniHyouTheme(
                darkTheme = isDark,
                blackColors = useBlackColors,
                appColor = appColor,
                appColorMode = appColorMode,
                currentUserColor = currentUserColor,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView(
                        windowSizeClass = windowSizeClass,
                        isLoggedIn = isLoggedIn,
                        lastTabOpened = lastTabOpened,
                        currentUserColor = currentUserColor,
                        event = viewModel,
                        homeTab = homeTab,
                        deepLink = deepLink,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.onIntentDataReceived(intent.data)
    }

    private fun findDeepLink(): DeepLink? {
        return when {
            // Widget intent
            intent.action == "media_details" -> {
                DeepLink(
                    type = DeepLink.Type.ANIME,// does not mather ANIME or MANGA
                    id = intent.getIntExtra("media_id", 0).toString()
                )
            }
            // Search shortcut
            intent.action == "search" -> {
                DeepLink(
                    type = DeepLink.Type.SEARCH,
                    id = "search"
                )
            }
            // Login intent or anilist link
            intent.data != null -> {
                viewModel.onIntentDataReceived(intent.data)
                // Manually handle deep links because the uri pattern in the compose navigation
                // matches this -> https://anilist.co/manga/41514/
                // but not this -> https://anilist.co/manga/41514/Otoyomegatari/
                //TODO: find a better solution :)
                val anilistSchemeIndex = intent.dataString?.indexOf("anilist.co")
                if (anilistSchemeIndex != null && anilistSchemeIndex != -1) {
                    val linkSplit = intent.dataString!!.substring(anilistSchemeIndex).split('/')
                    DeepLink(
                        type = DeepLink.Type.valueOf(linkSplit[1].uppercase()),
                        id = linkSplit[2]
                    )
                } else null
            }

            else -> null
        }
    }
}

@Composable
fun MainView(
    windowSizeClass: WindowSizeClass,
    isLoggedIn: Boolean,
    lastTabOpened: Int,
    currentUserColor: Color?,
    event: MainEvent?,
    homeTab: HomeTab,
    deepLink: DeepLink?,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isBottomDestination by remember {
        derivedStateOf { navBackStackEntry?.isBottomDestination() == true }
    }
    val navActionManager = NavActionManager.rememberNavActionManager(navController)
    val isCompactScreen = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    LaunchedEffect(navBackStackEntry) {
        if (currentUserColor != null
            && navBackStackEntry?.destination?.hierarchy?.any {
                it.hasRoute(UserDetails::class)
            } == true
        ) {
            event?.restoreAppColor()
        }
    }

    Scaffold(
        bottomBar = {
            if (isCompactScreen) {
                MainBottomNavBar(
                    navController = navController,
                    navBackStackEntry = navBackStackEntry,
                    navActionManager = navActionManager,
                    isVisible = isBottomDestination,
                    onItemSelected = { event?.saveLastTab(it) }
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal)
    ) { padding ->
        if (isCompactScreen) {
            MainNavigation(
                navController = navController,
                navActionManager = navActionManager,
                isCompactScreen = true,
                isLoggedIn = isLoggedIn,
                lastTabOpened = lastTabOpened,
                deepLink = deepLink,
                homeTab = homeTab,
                padding = padding,
            )
        } else {
            Row(
                modifier = Modifier.padding(padding)
            ) {
                MainNavigationRail(
                    navController = navController,
                    navBackStackEntry = navBackStackEntry,
                    onItemSelected = { event?.saveLastTab(it) },
                    modifier = Modifier.safeDrawingPadding(),
                )
                MainNavigation(
                    navController = navController,
                    navActionManager = navActionManager,
                    isCompactScreen = false,
                    isLoggedIn = isLoggedIn,
                    lastTabOpened = lastTabOpened,
                    deepLink = deepLink,
                    homeTab = homeTab,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun MainPreview() {
    AniHyouTheme {
        MainView(
            windowSizeClass = WindowSizeClass.calculateFromSize(
                DpSize(width = 1280.dp, height = 1920.dp)
            ),
            isLoggedIn = false,
            lastTabOpened = 0,
            currentUserColor = null,
            event = null,
            homeTab = HomeTab.DISCOVER,
            deepLink = null
        )
    }
}