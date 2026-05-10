package org.entredeux.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.entredeux.app.data.prefs.AppSelectionRepository

sealed interface MainUiState {
    data object Loading : MainUiState
    data object NeedsOnboarding : MainUiState
    data object Ready : MainUiState
}

class MainViewModel(repo: AppSelectionRepository) : ViewModel() {

    val uiState: StateFlow<MainUiState> = repo.onboardingCompleted
        .map { done -> if (done) MainUiState.Ready else MainUiState.NeedsOnboarding }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState.Loading)

    companion object {
        fun factory(repo: AppSelectionRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MainViewModel(repo) as T
        }
    }
}
