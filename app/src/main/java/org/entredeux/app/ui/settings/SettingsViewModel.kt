package org.entredeux.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.entredeux.app.data.local.PauseEventRepository
import org.entredeux.app.data.prefs.AppSelectionRepository

class SettingsViewModel(
    private val appSelectionRepository: AppSelectionRepository,
    private val pauseEventRepository: PauseEventRepository,
) : ViewModel() {

    val defaultBudgetMinutes: StateFlow<Int?> = appSelectionRepository.defaultBudgetMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setDefaultBudget(minutes: Int?) {
        viewModelScope.launch { appSelectionRepository.setDefaultBudgetMinutes(minutes) }
    }

    fun wipeSessionLog() {
        viewModelScope.launch { pauseEventRepository.deleteAll() }
    }

    companion object {
        fun factory(
            appSelectionRepository: AppSelectionRepository,
            pauseEventRepository: PauseEventRepository,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(appSelectionRepository, pauseEventRepository) as T
        }
    }
}
