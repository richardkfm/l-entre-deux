package org.entredeux.app.ui.pause

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.local.BudgetNotificationScheduler
import org.entredeux.app.data.local.PauseEventRepository
import org.entredeux.app.data.prefs.AppSelectionRepository
import org.entredeux.app.domain.model.Intention
import org.entredeux.app.domain.model.PauseEvent
import org.entredeux.app.domain.model.PauseOutcome

data class PauseUiState(
    val appLabel: String = "",
    val appFound: Boolean = true,
    val selectedIntention: Intention? = null,
    val selectedBudgetMinutes: Int? = null,
    val budgetChosen: Boolean = false,
)

class PauseViewModel(
    private val installedAppsRepository: InstalledAppsRepository,
    private val pauseEventRepository: PauseEventRepository,
    private val budgetScheduler: BudgetNotificationScheduler,
    private val appSelectionRepository: AppSelectionRepository,
    private val appScope: CoroutineScope,
    val packageName: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PauseUiState())
    val uiState: StateFlow<PauseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val label = installedAppsRepository.getAppLabel(packageName)
            val defaultBudget = appSelectionRepository.defaultBudgetMinutes.first()
            _uiState.update {
                it.copy(
                    appLabel = label ?: packageName,
                    appFound = label != null,
                    selectedBudgetMinutes = defaultBudget,
                    budgetChosen = defaultBudget != null,
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

    fun onProceed() {
        val state = _uiState.value
        val intention = state.selectedIntention ?: return
        appScope.launch {
            pauseEventRepository.record(
                PauseEvent(
                    timestamp = System.currentTimeMillis(),
                    packageName = packageName,
                    intentionKey = intention.stableKey,
                    budgetMinutes = state.selectedBudgetMinutes,
                    outcome = PauseOutcome.PROCEEDED,
                ),
            )
        }
        state.selectedBudgetMinutes?.let { budgetScheduler.schedule(packageName, it) }
    }

    fun onBackOut() {
        val state = _uiState.value
        val intention = state.selectedIntention ?: return
        appScope.launch {
            pauseEventRepository.record(
                PauseEvent(
                    timestamp = System.currentTimeMillis(),
                    packageName = packageName,
                    intentionKey = intention.stableKey,
                    budgetMinutes = state.selectedBudgetMinutes,
                    outcome = PauseOutcome.BACKED_OUT,
                ),
            )
        }
    }

    companion object {
        fun factory(
            installedAppsRepository: InstalledAppsRepository,
            pauseEventRepository: PauseEventRepository,
            budgetScheduler: BudgetNotificationScheduler,
            appSelectionRepository: AppSelectionRepository,
            appScope: CoroutineScope,
            packageName: String,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PauseViewModel(
                    installedAppsRepository,
                    pauseEventRepository,
                    budgetScheduler,
                    appSelectionRepository,
                    appScope,
                    packageName,
                ) as T
        }
    }
}
