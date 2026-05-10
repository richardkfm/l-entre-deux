package org.entredeux.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.prefs.AppSelectionRepository
import org.entredeux.app.domain.model.SelectedApp

data class HomeUiState(
    val selectedApps: List<SelectedApp> = emptyList(),
)

class HomeViewModel(
    private val installedAppsRepository: InstalledAppsRepository,
    appSelectionRepository: AppSelectionRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = appSelectionRepository.selectedPackageNames
        .map { packageNames -> resolveLabels(packageNames) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private suspend fun resolveLabels(packageNames: Set<String>): HomeUiState {
        val apps = withContext(Dispatchers.IO) {
            packageNames.mapNotNull { pkg ->
                val label = installedAppsRepository.getAppLabel(pkg) ?: return@mapNotNull null
                SelectedApp(packageName = pkg, label = label)
            }.sortedBy { it.label.lowercase() }
        }
        return HomeUiState(selectedApps = apps)
    }

    companion object {
        fun factory(
            installedAppsRepository: InstalledAppsRepository,
            appSelectionRepository: AppSelectionRepository,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(installedAppsRepository, appSelectionRepository) as T
        }
    }
}
