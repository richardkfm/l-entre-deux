package org.entredeux.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import org.entredeux.app.R
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.local.BudgetNotificationScheduler
import org.entredeux.app.data.local.PauseEventRepository
import org.entredeux.app.data.prefs.AppSelectionRepository
import org.entredeux.app.data.shortcuts.ShortcutRepository
import org.entredeux.app.ui.home.HomeScreen
import org.entredeux.app.ui.home.HomeViewModel
import org.entredeux.app.ui.onboarding.OnboardingScreen
import org.entredeux.app.ui.onboarding.OnboardingViewModel
import org.entredeux.app.ui.pause.PauseScreen
import org.entredeux.app.ui.pause.PauseViewModel
import org.entredeux.app.ui.reflection.ReflectionScreen
import org.entredeux.app.ui.reflection.ReflectionViewModel
import org.entredeux.app.ui.selection.AppSelectionScreen
import org.entredeux.app.ui.selection.AppSelectionViewModel
import org.entredeux.app.ui.settings.SettingsScreen
import org.entredeux.app.ui.settings.SettingsViewModel

data class ShortcutRequest(val packageName: String, val id: Long = System.currentTimeMillis())

private val topLevelRoutes = setOf("home", "reflection", "settings")

@Composable
fun AppNavHost(
    startDestination: String,
    shortcutRequest: ShortcutRequest?,
    onShortcutHandled: () -> Unit,
    installedAppsRepository: InstalledAppsRepository,
    appSelectionRepository: AppSelectionRepository,
    pauseEventRepository: PauseEventRepository,
    budgetScheduler: BudgetNotificationScheduler,
    shortcutRepository: ShortcutRepository,
    appScope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(shortcutRequest) {
        val pkg = shortcutRequest?.packageName ?: return@LaunchedEffect
        navController.navigate("pause/$pkg") {
            launchSingleTop = true
        }
        onShortcutHandled()
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
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
                        icon = { Icon(Icons.Filled.Info, contentDescription = null) },
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
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
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
            modifier = Modifier.padding(innerPadding),
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
                    onNavigateToPause = { pkg -> navController.navigate("pause/$pkg") },
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

            composable("pause/{packageName}") { backStackEntry ->
                val packageName = backStackEntry.arguments?.getString("packageName") ?: return@composable
                val vm: PauseViewModel = viewModel(
                    key = packageName,
                    factory = PauseViewModel.factory(
                        installedAppsRepository,
                        pauseEventRepository,
                        budgetScheduler,
                        appSelectionRepository,
                        appScope,
                        packageName,
                    ),
                )
                val context = LocalContext.current
                PauseScreen(
                    viewModel = vm,
                    onProceed = {
                        val intent = installedAppsRepository.getLaunchIntent(packageName)
                        intent?.let { context.startActivity(it) }
                        navController.popBackStack()
                    },
                    onBackOut = { navController.popBackStack() },
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
                    factory = SettingsViewModel.factory(appSelectionRepository, pauseEventRepository),
                )
                SettingsScreen(
                    viewModel = vm,
                    onNavigateToSelection = { navController.navigate("selection") },
                )
            }
        }
    }
}
