package org.entredeux.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.entredeux.app.data.prefs.AppSelectionRepository
import org.entredeux.app.data.prefs.SettingsRepository
import org.entredeux.app.domain.model.Look

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Loaded(val needsOnboarding: Boolean, val look: Look) : MainUiState
}

class MainViewModel(
    appSelectionRepository: AppSelectionRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    // Both are read before the first screen composes, so a Material user
    // never sees a frame of the Papier look (or the other way round).
    val uiState: StateFlow<MainUiState> = combine(
        appSelectionRepository.onboardingCompleted,
        settingsRepository.look,
    ) { done, look -> MainUiState.Loaded(needsOnboarding = !done, look = look) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState.Loading)

    companion object {
        fun factory(
            appSelectionRepository: AppSelectionRepository,
            settingsRepository: SettingsRepository,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MainViewModel(appSelectionRepository, settingsRepository) as T
        }
    }
}
