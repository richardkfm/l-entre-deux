package org.entredeux.app.ui.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.prefs.AppSelectionRepository
import org.entredeux.app.domain.model.SelectedApp
import org.entredeux.app.domain.usecase.toggleAppSelection

data class SelectableApp(val app: SelectedApp, val isSelected: Boolean)

data class AppSelectionUiState(
    val apps: List<SelectableApp> = emptyList(),
    val isLoading: Boolean = true,
    val query: String = "",
)

class AppSelectionViewModel(
    private val installedAppsRepository: InstalledAppsRepository,
    private val appSelectionRepository: AppSelectionRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _allApps = MutableStateFlow<List<SelectedApp>>(emptyList())

    val uiState: StateFlow<AppSelectionUiState>
        get() = _uiState

    private val _uiState = MutableStateFlow(AppSelectionUiState())

    init {
        viewModelScope.launch {
            val installed = withContext(Dispatchers.IO) {
                installedAppsRepository.getInstalledApps()
            }
            _allApps.value = installed

            combine(
                _allApps,
                appSelectionRepository.selectedPackageNames,
                _query,
            ) { allApps, selected, query ->
                val filtered = if (query.isBlank()) allApps
                else allApps.filter { it.label.contains(query, ignoreCase = true) }
                AppSelectionUiState(
                    apps = filtered.map { app ->
                        SelectableApp(app, app.packageName in selected)
                    },
                    isLoading = false,
                    query = query,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onQueryChange(query: String) {
        _query.update { query }
    }

    fun onToggle(packageName: String) {
        viewModelScope.launch {
            val currentSet = appSelectionRepository.selectedPackageNames.first()
            appSelectionRepository.setSelectedPackages(
                toggleAppSelection(currentSet, packageName),
            )
        }
    }

    companion object {
        fun factory(
            installedAppsRepository: InstalledAppsRepository,
            appSelectionRepository: AppSelectionRepository,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AppSelectionViewModel(installedAppsRepository, appSelectionRepository) as T
        }
    }
}
