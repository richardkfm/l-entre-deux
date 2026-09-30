package org.entredeux.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.entredeux.app.data.local.PauseEventRepository
import org.entredeux.app.data.prefs.SettingsRepository
import org.entredeux.app.domain.model.Look

data class SettingsUiState(
    val look: Look = Look.PAPIER,
    val graceWindow: Boolean = true,
    val tintByTime: Boolean = true,
)

class SettingsViewModel(
    private val pauseEventRepository: PauseEventRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.look,
        settingsRepository.graceWindow,
        settingsRepository.tintByTime,
    ) { look, grace, tint -> SettingsUiState(look, grace, tint) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setLook(look: Look) {
        viewModelScope.launch { settingsRepository.setLook(look) }
    }

    fun setGraceWindow(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setGraceWindow(enabled) }
    }

    fun setTintByTime(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setTintByTime(enabled) }
    }

    fun wipeSessionLog() {
        viewModelScope.launch { pauseEventRepository.deleteAll() }
    }

    companion object {
        fun factory(
            pauseEventRepository: PauseEventRepository,
            settingsRepository: SettingsRepository,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(pauseEventRepository, settingsRepository) as T
        }
    }
}
