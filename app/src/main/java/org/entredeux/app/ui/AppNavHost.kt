package org.entredeux.app.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.entredeux.app.R
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.local.PauseEventRepository
import org.entredeux.app.data.prefs.AppSelectionRepository
import org.entredeux.app.data.prefs.SettingsRepository
import org.entredeux.app.data.shortcuts.ShortcutRepository
import org.entredeux.app.ui.home.HomeScreen
import org.entredeux.app.ui.home.HomeViewModel
import org.entredeux.app.ui.onboarding.OnboardingScreen
import org.entredeux.app.ui.onboarding.OnboardingViewModel
import org.entredeux.app.ui.reflection.ReflectionScreen
import org.entredeux.app.ui.reflection.ReflectionViewModel
import org.entredeux.app.ui.selection.AppSelectionScreen
import org.entredeux.app.ui.selection.AppSelectionViewModel
import org.entredeux.app.ui.settings.SettingsScreen
import org.entredeux.app.ui.settings.SettingsViewModel

private val topLevelRoutes = setOf("home", "reflection", "settings")

// Navigation Compose defaults to a 700 ms crossfade, long enough to see two
// screens stacked on top of each other. A short fade keeps moves calm.
private const val NAV_FADE_MS = 220

@Composable
fun AppNavHost(
    startDestination: String,
    installedAppsRepository: InstalledAppsRepository,
    appSelectionRepository: AppSelectionRepository,
    pauseEventRepository: PauseEventRepository,
    shortcutRepository: ShortcutRepository,
    settingsRepository: SettingsRepository,
    onOpenPause: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_home)) },
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                    NavigationBarItem(
                        icon = { Icon(painterResource(R.drawable.ic_nav_reflection), contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_reflection)) },
                        selected = currentRoute == "reflection",
                        onClick = {
                            navController.navigate("reflection") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_settings)) },
                        selected = currentRoute == "settings",
                        onClick = {
                            navController.navigate("settings") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            // Consuming the insets stops each screen's own Scaffold from
            // padding for the status bar a second time (edge-to-edge).
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            enterTransition = { fadeIn(tween(NAV_FADE_MS)) },
            exitTransition = { fadeOut(tween(NAV_FADE_MS)) },
            popEnterTransition = { fadeIn(tween(NAV_FADE_MS)) },
            popExitTransition = { fadeOut(tween(NAV_FADE_MS)) },
        ) {
            composable("onboarding") {
                val vm: OnboardingViewModel = viewModel(
                    factory = OnboardingViewModel.factory(appSelectionRepository),
                )
                OnboardingScreen(
                    viewModel = vm,
                    onDone = {
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    },
                )
            }

            composable("home") {
                val vm: HomeViewModel = viewModel(
                    factory = HomeViewModel.factory(
                        installedAppsRepository,
                        appSelectionRepository,
                        shortcutRepository,
                    ),
                )
                HomeScreen(
                    viewModel = vm,
                    onNavigateToSelection = { navController.navigate("selection") },
                    onNavigateToPause = onOpenPause,
                )
            }

            composable("selection") {
                val vm: AppSelectionViewModel = viewModel(
                    factory = AppSelectionViewModel.factory(installedAppsRepository, appSelectionRepository),
                )
                AppSelectionScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                )
            }

            composable("reflection") {
                val vm: ReflectionViewModel = viewModel(
                    factory = ReflectionViewModel.factory(pauseEventRepository, installedAppsRepository),
                )
                ReflectionScreen(viewModel = vm)
            }

            composable("settings") {
                val vm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.factory(pauseEventRepository, settingsRepository),
                )
                SettingsScreen(
                    viewModel = vm,
                    onNavigateToSelection = { navController.navigate("selection") },
                )
            }
        }
    }
}
