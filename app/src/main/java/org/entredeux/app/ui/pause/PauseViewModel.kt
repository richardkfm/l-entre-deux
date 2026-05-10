package org.entredeux.app.ui.pause

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.domain.model.Intention

data class PauseUiState(
    val appLabel: String = "",
    val appFound: Boolean = true,
    val selectedIntention: Intention? = null,
    val selectedBudgetMinutes: Int? = null,
    val budgetChosen: Boolean = false,
)

class PauseViewModel(
    private val installedAppsRepository: InstalledAppsRepository,
    val packageName: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PauseUiState())
    val uiState: StateFlow<PauseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val label = installedAppsRepository.getAppLabel(packageName)
            _uiState.update {
                it.copy(
                    appLabel = label ?: packageName,
                    appFound = label != null,
                )
            }
        }
    }

    fun selectIntention(intention: Intention) {
        _uiState.update { it.copy(selectedIntention = intention) }
    }

    fun selectBudget(minutes: Int?) {
        _uiState.update { it.copy(selectedBudgetMinutes = minutes, budgetChosen = true) }
    }

    companion object {
        fun factory(
            installedAppsRepository: InstalledAppsRepository,
            packageName: String,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PauseViewModel(installedAppsRepository, packageName) as T
        }
    }
}
