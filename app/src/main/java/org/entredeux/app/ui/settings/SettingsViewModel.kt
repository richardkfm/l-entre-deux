package org.entredeux.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.entredeux.app.data.local.PauseEventRepository

class SettingsViewModel(
    private val pauseEventRepository: PauseEventRepository,
) : ViewModel() {

    fun wipeSessionLog() {
        viewModelScope.launch { pauseEventRepository.deleteAll() }
    }

    companion object {
        fun factory(
            pauseEventRepository: PauseEventRepository,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(pauseEventRepository) as T
        }
    }
}
