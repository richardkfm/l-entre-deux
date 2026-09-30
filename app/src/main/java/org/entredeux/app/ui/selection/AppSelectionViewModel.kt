package org.entredeux.app.ui.selection

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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

data class SelectableApp(val app: SelectedApp, val isSelected: Boolean)

data class AppSelectionUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    // Without a query the list is grouped; the groups are fixed when the
    // screen opens, so a row never jumps away from under the finger.
    val chosen: List<SelectableApp> = emptyList(),
    val often: List<SelectableApp> = emptyList(),
    val others: List<SelectableApp> = emptyList(),
    val results: List<SelectableApp> = emptyList(),
)

class AppSelectionViewModel(
    private val installedAppsRepository: InstalledAppsRepository,
    private val appSelectionRepository: AppSelectionRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(AppSelectionUiState())
    val uiState: StateFlow<AppSelectionUiState> = _uiState

    init {
        viewModelScope.launch {
            val installed = withContext(Dispatchers.IO) { installedAppsRepository.getInstalledApps() }
            val chosenAtOpen = appSelectionRepository.selectedPackageNames.first()
            combine(appSelectionRepository.selectedPackageNames, _query) { selected, query ->
                fun List<SelectedApp>.rows() = map { SelectableApp(it, it.packageName in selected) }
                val chosen = installed.filter { it.packageName in chosenAtOpen }
                val rest = installed.filterNot { it.packageName in chosenAtOpen }
                AppSelectionUiState(
                    isLoading = false,
                    query = query,
                    chosen = chosen.rows(),
                    often = rest.filter { it.often }.rows(),
                    others = rest.filterNot { it.often }.rows(),
                    results = if (query.isBlank()) {
                        emptyList()
                    } else {
                        installed.filter { it.label.contains(query, ignoreCase = true) }.rows()
                    },
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onQueryChange(query: String) {
        _query.update { query }
    }

    fun onToggle(packageName: String) {
        viewModelScope.launch { appSelectionRepository.toggle(packageName) }
    }

    suspend fun icon(packageName: String): ImageBitmap? =
        withContext(Dispatchers.IO) { installedAppsRepository.getAppIcon(packageName)?.asImageBitmap() }

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
