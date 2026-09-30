package org.entredeux.app.ui.reflection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.local.PauseEventRepository
import org.entredeux.app.domain.model.ReflectionStats
import org.entredeux.app.domain.model.ReflectionSummary
import org.entredeux.app.domain.usecase.describeReflection
import org.entredeux.app.domain.usecase.getReflectionStats
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class ReflectionPeriod { WEEK, ALL }

sealed interface ReflectionUiState {
    val period: ReflectionPeriod

    data class Loading(override val period: ReflectionPeriod) : ReflectionUiState

    // hasHistory: something exists outside the chosen period, so the empty
    // state can point to "All time" instead of saying there is nothing.
    data class Empty(override val period: ReflectionPeriod, val hasHistory: Boolean) : ReflectionUiState

    data class Ready(
        override val period: ReflectionPeriod,
        val from: LocalDate,
        val to: LocalDate,
        val stats: ReflectionStats,
        val summary: ReflectionSummary,
        val appLabels: Map<String, String>,
    ) : ReflectionUiState
}

class ReflectionViewModel(
    pauseEventRepository: PauseEventRepository,
    private val installedAppsRepository: InstalledAppsRepository,
) : ViewModel() {

    private val period = MutableStateFlow(ReflectionPeriod.WEEK)

    val uiState: StateFlow<ReflectionUiState> = combine(pauseEventRepository.allEvents(), period) { events, chosen ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        // The last seven days including today, from midnight.
        val weekStart = today.minusDays(6)
        val since = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
        val inPeriod = if (chosen == ReflectionPeriod.WEEK) events.filter { it.timestamp >= since } else events
        val stats = getReflectionStats(inPeriod, zone)
            ?: return@combine ReflectionUiState.Empty(chosen, hasHistory = events.isNotEmpty())
        val from = if (chosen == ReflectionPeriod.WEEK) {
            weekStart
        } else {
            Instant.ofEpochMilli(inPeriod.minOf { it.timestamp }).atZone(zone).toLocalDate()
        }
        val appLabels = stats.perApp.associate { appCount ->
            appCount.packageName to
                (installedAppsRepository.getAppLabel(appCount.packageName) ?: appCount.packageName)
        }
        ReflectionUiState.Ready(chosen, from, today, stats, describeReflection(stats), appLabels)
    }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReflectionUiState.Loading(ReflectionPeriod.WEEK))

    fun choose(next: ReflectionPeriod) {
        period.value = next
    }

    companion object {
        fun factory(
            pauseEventRepository: PauseEventRepository,
            installedAppsRepository: InstalledAppsRepository,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReflectionViewModel(pauseEventRepository, installedAppsRepository) as T
        }
    }
}
