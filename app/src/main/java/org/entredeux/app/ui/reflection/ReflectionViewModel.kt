package org.entredeux.app.ui.reflection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.local.PauseEventRepository
import org.entredeux.app.domain.model.ReflectionStats
import org.entredeux.app.domain.usecase.getReflectionStats

sealed interface ReflectionUiState {
    object Loading : ReflectionUiState
    object Empty : ReflectionUiState
    data class Ready(
        val stats: ReflectionStats,
        val appLabels: Map<String, String>,
    ) : ReflectionUiState
}

class ReflectionViewModel(
    pauseEventRepository: PauseEventRepository,
    private val installedAppsRepository: InstalledAppsRepository,
) : ViewModel() {

    val uiState: StateFlow<ReflectionUiState> = pauseEventRepository.allEvents()
        .map { events ->
            val stats = getReflectionStats(events) ?: return@map ReflectionUiState.Empty
            val appLabels = stats.perApp.associate { appCount ->
                appCount.packageName to
                    (installedAppsRepository.getAppLabel(appCount.packageName) ?: appCount.packageName)
            }
            ReflectionUiState.Ready(stats, appLabels)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReflectionUiState.Loading)

    companion object {
        fun factory(
            pauseEventRepository: PauseEventRepository,
            installedAppsRepository: InstalledAppsRepository,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReflectionViewModel(pauseEventRepository, installedAppsRepository) as T
        }
    }
}
