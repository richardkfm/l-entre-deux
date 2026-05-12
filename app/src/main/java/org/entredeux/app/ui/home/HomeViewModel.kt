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
import kotlinx.coroutines.withContext
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.prefs.AppSelectionRepository
import org.entredeux.app.data.shortcuts.ShortcutRepository
import org.entredeux.app.domain.model.SelectedApp

enum class ShortcutResult { SUCCESS, UNSUPPORTED }

data class HomeUiState(
    val selectedApps: List<SelectedApp> = emptyList(),
    val shortcutResult: ShortcutResult? = null,
)

class HomeViewModel(
    private val installedAppsRepository: InstalledAppsRepository,
    appSelectionRepository: AppSelectionRepository,
    private val shortcutRepository: ShortcutRepository,
) : ViewModel() {

    private val _shortcutResult = MutableStateFlow<ShortcutResult?>(null)

    val uiState: StateFlow<HomeUiState> = appSelectionRepository.selectedPackageNames
        .map { names -> resolveLabels(names) }
        .combine(_shortcutResult) { apps, result ->
            HomeUiState(selectedApps = apps, shortcutResult = result)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

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
