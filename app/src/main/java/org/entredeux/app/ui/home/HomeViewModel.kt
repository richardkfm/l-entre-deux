package org.entredeux.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.prefs.AppSelectionRepository
import org.entredeux.app.data.shortcuts.ShortcutRepository
import org.entredeux.app.domain.model.SelectedApp

enum class ShortcutResult { SUCCESS, UNSUPPORTED }

// Which hand-holding step the Home screen should surface. Derived from
// whether the one-time guide is still pending and whether any app is chosen.
enum class CoachStep { NONE, ADD_APP, PIN }

data class HomeUiState(
    val selectedApps: List<SelectedApp> = emptyList(),
    val shortcutResult: ShortcutResult? = null,
    val coachStep: CoachStep = CoachStep.NONE,
)

class HomeViewModel(
    private val installedAppsRepository: InstalledAppsRepository,
    private val appSelectionRepository: AppSelectionRepository,
    private val shortcutRepository: ShortcutRepository,
) : ViewModel() {

    private val _shortcutResult = MutableStateFlow<ShortcutResult?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        appSelectionRepository.selectedPackageNames.map { resolveLabels(it) },
        _shortcutResult,
        appSelectionRepository.homeCoachCompleted,
    ) { apps, result, coachDone ->
        val step = when {
            coachDone -> CoachStep.NONE
            apps.isEmpty() -> CoachStep.ADD_APP
            else -> CoachStep.PIN
        }
        HomeUiState(selectedApps = apps, shortcutResult = result, coachStep = step)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private suspend fun resolveLabels(packageNames: Set<String>): List<SelectedApp> =
        withContext(Dispatchers.IO) {
            packageNames.mapNotNull { pkg ->
                val label = installedAppsRepository.getAppLabel(pkg) ?: return@mapNotNull null
                SelectedApp(packageName = pkg, label = label)
            }.sortedBy { it.label.lowercase() }
        }

    fun requestPinShortcut(app: SelectedApp) {
        val ok = shortcutRepository.requestPinShortcut(app.packageName, app.label)
        _shortcutResult.value = if (ok) ShortcutResult.SUCCESS else ShortcutResult.UNSUPPORTED
    }

    fun clearShortcutResult() {
        _shortcutResult.value = null
    }

    fun dismissCoach() {
        viewModelScope.launch { appSelectionRepository.setHomeCoachCompleted() }
    }

    companion object {
        fun factory(
            installedAppsRepository: InstalledAppsRepository,
            appSelectionRepository: AppSelectionRepository,
            shortcutRepository: ShortcutRepository,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(installedAppsRepository, appSelectionRepository, shortcutRepository) as T
        }
    }
}
