package org.entredeux.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.entredeux.app.data.prefs.AppSelectionRepository

class OnboardingViewModel(private val repo: AppSelectionRepository) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch { repo.setOnboardingCompleted() }
    }

    companion object {
        fun factory(repo: AppSelectionRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                OnboardingViewModel(repo) as T
        }
    }
}
