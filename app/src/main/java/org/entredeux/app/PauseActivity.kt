package org.entredeux.app

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.entredeux.app.data.shortcuts.ShortcutRepository
import org.entredeux.app.data.shortcuts.ShortcutRepository.Companion.EXTRA_PACKAGE_NAME
import org.entredeux.app.domain.model.Look
import org.entredeux.app.domain.usecase.isWithinGraceWindow
import org.entredeux.app.ui.pause.PauseScreen
import org.entredeux.app.ui.pause.PauseViewModel
import org.entredeux.app.ui.theme.EntreDeuxTheme

private data class PauseRequest(
    val packageName: String,
    val demo: Boolean,
    // A fresh id per launch resets the shuffled order and the epigraph even
    // when the same app is paused twice in a row.
    val id: Long = SystemClock.elapsedRealtimeNanos(),
)

// The pause lives in its own small activity, in its own task that never
// shows in Recents. Pinned shortcuts open it directly, so the first frame
// after tapping an icon is the pause itself, not the app's Home, and
// nothing of l'entre-deux is left behind once the other app opens.
class PauseActivity : ComponentActivity() {

    private var request by mutableStateOf<PauseRequest?>(null)
    private var look by mutableStateOf(Look.PAPIER)

    private val app get() = application as EntreDeuxApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val first = requestFrom(intent) ?: return finish()
        setContent {
            val current = request ?: return@setContent
            EntreDeuxTheme(look = look) {
                key(current.id) {
                    val viewModel: PauseViewModel = viewModel(
                        key = "${current.packageName}#${current.id}",
                        factory = PauseViewModel.factory(
                            app.installedAppsRepository,
                            app.pauseEventRepository,
                            app.appScope,
                            current.packageName,
                            current.demo,
                        ),
                    )
                    PauseScreen(
                        viewModel = viewModel,
                        onProceed = { if (current.demo) close() else openTarget(current.packageName) },
                        onBackOut = { close() },
                    )
                }
            }
        }
        decide(first)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestFrom(intent)?.let { decide(it) }
    }

    // Until this settles the window background (the same paper as the
    // pause) is all that shows, so there is no flash of anything else.
    private fun decide(next: PauseRequest) {
        lifecycleScope.launch {
            look = app.settingsRepository.look.first()
            val skip = !next.demo &&
                app.settingsRepository.graceWindow.first() &&
                isWithinGraceWindow(app.pauseEventRepository.lastProceededAt(next.packageName), System.currentTimeMillis())
            if (skip) openTarget(next.packageName) else request = next
        }
    }

    private fun openTarget(packageName: String) {
        app.installedAppsRepository.getLaunchIntent(packageName)?.let { launch ->
            val fade = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(launch, fade.toBundle())
        }
        finishAndRemoveTask()
    }

    private fun close() {
        finishAndRemoveTask()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, android.R.anim.fade_out)
        }
    }

    private fun requestFrom(intent: Intent?): PauseRequest? {
        val pkg = intent?.getStringExtra(EXTRA_PACKAGE_NAME) ?: return null
        return PauseRequest(pkg, demo = intent.getBooleanExtra(EXTRA_DEMO, false))
    }

    companion object {
        private const val EXTRA_DEMO = "org.entredeux.app.extra.DEMO"

        fun intent(context: Context, packageName: String, demo: Boolean = false): Intent =
            Intent(context, PauseActivity::class.java)
                .setAction(ShortcutRepository.ACTION_PAUSE_LAUNCH)
                .putExtra(EXTRA_PACKAGE_NAME, packageName)
                .putExtra(EXTRA_DEMO, demo)
    }
}
