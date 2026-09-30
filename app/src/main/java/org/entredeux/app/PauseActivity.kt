package org.entredeux.app

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.entredeux.app.data.shortcuts.ShortcutRepository
import org.entredeux.app.data.shortcuts.ShortcutRepository.Companion.EXTRA_PACKAGE_NAME
import org.entredeux.app.domain.model.Look
import org.entredeux.app.domain.model.PauseTint
import org.entredeux.app.domain.usecase.isWithinGraceWindow
import org.entredeux.app.domain.usecase.pauseTintForHour
import org.entredeux.app.ui.pause.PauseScreen
import org.entredeux.app.ui.pause.PauseViewModel
import org.entredeux.app.ui.theme.EntreDeuxTheme
import org.entredeux.app.ui.theme.isDarkPause
import java.time.LocalTime

private data class PauseRequest(
    val packageName: String,
    val demo: Boolean,
    // A fresh id per launch resets the shuffled order and the epigraph even
    // when the same app is paused twice in a row.
    val id: Long = SystemClock.elapsedRealtimeNanos(),
)

private data class Appearance(val look: Look, val tint: PauseTint?)

// The pause lives in its own small activity, in its own task that never
// shows in Recents. Pinned shortcuts open it directly, so the first frame
// after tapping an icon is the pause itself, not the app's Home, and
// nothing of l'entre-deux is left behind once the other app opens.
class PauseActivity : ComponentActivity() {

    private var request by mutableStateOf<PauseRequest?>(null)
    private var appearance by mutableStateOf<Appearance?>(null)
    private var decision: Job? = null

    private val app get() = application as EntreDeuxApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Recreated (rotation, a dark-mode switch, process death): the same
        // pause with the same order and epigraph, and no second grace check.
        val restored = savedInstanceState?.let(::restoreRequest)
        val next = restored ?: requestFrom(intent) ?: return finish()
        setContent {
            val current = appearance ?: return@setContent
            EntreDeuxTheme(look = current.look, tint = current.tint) {
                // The pause's own background goes down as soon as the look is
                // known, before the grace check, so the screen settles on its
                // final colour at once.
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    val pause = request ?: return@Box
                    key(pause.id) {
                        val viewModel: PauseViewModel = viewModel(
                            key = "${pause.packageName}#${pause.id}",
                            factory = PauseViewModel.factory(
                                app.installedAppsRepository,
                                app.pauseEventRepository,
                                app.appScope,
                                pause.packageName,
                                pause.demo,
                            ),
                        )
                        PauseScreen(
                            viewModel = viewModel,
                            onProceed = { if (pause.demo) close() else openTarget(pause.packageName) },
                            onBackOut = { close() },
                        )
                    }
                }
            }
        }
        decide(next, checkGrace = restored == null)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestFrom(intent)?.let { decide(it, checkGrace = true) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        request?.let {
            outState.putString(STATE_PACKAGE, it.packageName)
            outState.putBoolean(STATE_DEMO, it.demo)
            outState.putLong(STATE_ID, it.id)
        }
    }

    // A newer launch always wins: the previous decision is cancelled, so a
    // slow check for an earlier app can't replace the pause just asked for.
    private fun decide(next: PauseRequest, checkGrace: Boolean) {
        decision?.cancel()
        if (checkGrace) request = null
        decision = lifecycleScope.launch {
            val look = app.settingsRepository.look.first()
            val tint = if (look == Look.PAPIER && app.settingsRepository.tintByTime.first()) {
                pauseTintForHour(LocalTime.now().hour)
            } else {
                null
            }
            val systemDark = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            if (isDarkPause(look, systemDark, tint)) {
                // Light icons on the evening and night tints, even when the
                // system itself is in light mode.
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                    navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                )
            } else {
                enableEdgeToEdge()
            }
            appearance = Appearance(look, tint)
            val skip = checkGrace &&
                !next.demo &&
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

    private fun restoreRequest(state: Bundle): PauseRequest? {
        val pkg = state.getString(STATE_PACKAGE) ?: return null
        return PauseRequest(pkg, state.getBoolean(STATE_DEMO), state.getLong(STATE_ID))
    }

    private fun requestFrom(intent: Intent?): PauseRequest? {
        val pkg = intent?.getStringExtra(EXTRA_PACKAGE_NAME) ?: return null
        return PauseRequest(pkg, demo = intent.getBooleanExtra(EXTRA_DEMO, false))
    }

    companion object {
        private const val EXTRA_DEMO = "org.entredeux.app.extra.DEMO"
        private const val STATE_PACKAGE = "pause_package"
        private const val STATE_DEMO = "pause_demo"
        private const val STATE_ID = "pause_id"

        fun intent(context: Context, packageName: String, demo: Boolean = false): Intent =
            Intent(context, PauseActivity::class.java)
                .setAction(ShortcutRepository.ACTION_PAUSE_LAUNCH)
                .putExtra(EXTRA_PACKAGE_NAME, packageName)
                .putExtra(EXTRA_DEMO, demo)
    }
}
