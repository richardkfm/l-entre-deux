package org.entredeux.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import org.entredeux.app.domain.model.Look
import org.entredeux.app.ui.AppNavHost
import org.entredeux.app.ui.theme.EntreDeuxTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A shortcut pinned before 1.1.0 lands here: hand it to the pause
        // and get out of the way, as if the shortcut had pointed there.
        if (savedInstanceState == null && forwardLegacyShortcut(intent)) {
            finish()
            return
        }
        enableEdgeToEdge()
        val app = application as EntreDeuxApplication
        app.shortcutRepository.migratePinnedShortcuts()
        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModel.factory(app.appSelectionRepository, app.settingsRepository),
            )
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val loaded = uiState as? MainUiState.Loaded

            // Capture start destination once so NavHost is not recreated when
            // onboarding completes.
            var startDestination by remember { mutableStateOf<String?>(null) }
            if (startDestination == null && loaded != null) {
                startDestination = if (loaded.needsOnboarding) "onboarding" else "home"
            }

            EntreDeuxTheme(look = loaded?.look ?: Look.PAPIER) {
                val dest = startDestination
                if (dest != null) {
                    AppNavHost(
                        startDestination = dest,
                        installedAppsRepository = app.installedAppsRepository,
                        appSelectionRepository = app.appSelectionRepository,
                        pauseEventRepository = app.pauseEventRepository,
                        shortcutRepository = app.shortcutRepository,
                        settingsRepository = app.settingsRepository,
                        onOpenPause = { pkg -> startActivity(PauseActivity.intent(this, pkg)) },
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
        forwardLegacyShortcut(intent)
    }

    private fun forwardLegacyShortcut(intent: Intent?): Boolean {
        if (intent?.action != ShortcutRepository.ACTION_PAUSE_LAUNCH) return false
        val pkg = intent.getStringExtra(ShortcutRepository.EXTRA_PACKAGE_NAME) ?: return false
        startActivity(PauseActivity.intent(this, pkg))
        return true
    }
}
