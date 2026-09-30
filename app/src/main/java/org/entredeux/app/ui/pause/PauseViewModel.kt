package org.entredeux.app.ui.pause

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.local.PauseEventRepository
import org.entredeux.app.domain.model.Intention
import org.entredeux.app.domain.model.PauseEvent
import org.entredeux.app.domain.model.PauseOutcome

data class PauseUiState(
    val appLabel: String = "",
    val appIcon: ImageBitmap? = null,
)

class PauseViewModel(
    private val installedAppsRepository: InstalledAppsRepository,
    private val pauseEventRepository: PauseEventRepository,
    private val appScope: CoroutineScope,
    val packageName: String,
    // A rehearsal during setup: shown exactly like a real pause, never logged.
    private val demo: Boolean = false,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PauseUiState())
    val uiState: StateFlow<PauseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val label = installedAppsRepository.getAppLabel(packageName)
            val icon = installedAppsRepository.getAppIcon(packageName)?.asImageBitmap()
            _uiState.update { it.copy(appLabel = label ?: packageName, appIcon = icon) }
        }
    }

    // Naming the intention is the act of proceeding — one tap opens the app.
    fun proceed(intention: Intention) = record(intention.stableKey, PauseOutcome.PROCEEDED)

    fun backOut() = record(BACKED_OUT_INTENTION, PauseOutcome.BACKED_OUT)

    private fun record(intentionKey: String, outcome: PauseOutcome) {
        if (demo) return
        appScope.launch {
            pauseEventRepository.record(
                PauseEvent(
                    timestamp = System.currentTimeMillis(),
                    packageName = packageName,
                    intentionKey = intentionKey,
                    outcome = outcome,
                ),
            )
        }
    }

    companion object {
        // A back-out has no "why I'm opening it" intention. We store an empty
        // key (rather than changing the schema): the reflection intention-mix
        // matches on stable keys, so empty simply isn't counted there, while
        // the back-out still counts toward the per-app and back-out totals.
        private const val BACKED_OUT_INTENTION = ""

        fun factory(
            installedAppsRepository: InstalledAppsRepository,
            pauseEventRepository: PauseEventRepository,
            appScope: CoroutineScope,
            packageName: String,
            demo: Boolean = false,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PauseViewModel(
                    installedAppsRepository,
                    pauseEventRepository,
                    appScope,
                    packageName,
                    demo,
                ) as T
        }
    }
}
