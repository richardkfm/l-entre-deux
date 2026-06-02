package org.entredeux.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.entredeux.app.data.shortcuts.ShortcutRepository
import org.entredeux.app.ui.AppNavHost
import org.entredeux.app.ui.ShortcutRequest
import org.entredeux.app.ui.theme.EntreDeuxTheme

class MainActivity : ComponentActivity() {

    private val shortcutRequest = mutableStateOf<ShortcutRequest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extractShortcut(intent)
        val app = application as EntreDeuxApplication
        setContent {
            EntreDeuxTheme {
                val mainViewModel: MainViewModel = viewModel(
                    factory = MainViewModel.factory(app.appSelectionRepository),
                )
                val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

                // Capture start destination once so NavHost is not recreated when
                // onboarding completes and MainUiState transitions to Ready.
                var startDestination by remember { mutableStateOf<String?>(null) }
                if (startDestination == null && uiState != MainUiState.Loading) {
                    startDestination = when (uiState) {
                        MainUiState.NeedsOnboarding -> "onboarding"
                        else -> "home"
                    }
                }

                val dest = startDestination
                if (dest != null) {
                    AppNavHost(
                        startDestination = dest,
                        shortcutRequest = shortcutRequest.value,
                        onShortcutHandled = { shortcutRequest.value = null },
                        installedAppsRepository = app.installedAppsRepository,
                        appSelectionRepository = app.appSelectionRepository,
                        pauseEventRepository = app.pauseEventRepository,
                        shortcutRepository = app.shortcutRepository,
                        appScope = app.appScope,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(Modifier.fillMaxSize())
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractShortcut(intent)
    }

    private fun extractShortcut(intent: Intent?) {
        if (intent?.action == ShortcutRepository.ACTION_PAUSE_LAUNCH) {
            val pkg = intent.getStringExtra(ShortcutRepository.EXTRA_PACKAGE_NAME)
            if (pkg != null) {
                shortcutRequest.value = ShortcutRequest(pkg)
            }
        }
    }
}
