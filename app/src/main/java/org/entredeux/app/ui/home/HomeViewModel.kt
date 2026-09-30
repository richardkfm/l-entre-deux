package org.entredeux.app.ui.home

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.prefs.AppSelectionRepository
import org.entredeux.app.data.shortcuts.ShortcutRepository
import org.entredeux.app.domain.model.SelectedApp

data class AppRow(val app: SelectedApp, val icon: ImageBitmap?, val pinned: Boolean)

sealed interface HomeMessage {
    data class Pinned(val label: String) : HomeMessage
    data object Unsupported : HomeMessage
}

data class HomeUiState(
    // True until the saved selection has been read, so the list never
    // flashes its empty state at people who already chose apps.
    val isLoading: Boolean = true,
    val rows: List<AppRow> = emptyList(),
    val message: HomeMessage? = null,
) {
    val unpinned get() = rows.filterNot { it.pinned }
    val pinned get() = rows.filter { it.pinned }
}

class HomeViewModel(
    private val installedAppsRepository: InstalledAppsRepository,
    appSelectionRepository: AppSelectionRepository,
    private val shortcutRepository: ShortcutRepository,
) : ViewModel() {

    private val pinned = MutableStateFlow<Set<String>>(emptySet())
    private val message = MutableStateFlow<HomeMessage?>(null)
    private var awaitingPin: SelectedApp? = null

    private val apps = appSelectionRepository.selectedPackageNames.map { packages ->
        withContext(Dispatchers.IO) {
            packages.mapNotNull { pkg ->
                val label = installedAppsRepository.getAppLabel(pkg) ?: return@mapNotNull null
                SelectedApp(packageName = pkg, label = label) to
                    installedAppsRepository.getAppIcon(pkg)?.asImageBitmap()
            }.sortedBy { it.first.label.lowercase() }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(apps, pinned, message) { list, pinnedSet, msg ->
        HomeUiState(
            isLoading = false,
            rows = list.map { (app, icon) -> AppRow(app, icon, app.packageName in pinnedSet) },
            message = msg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    // Called on every resume: the launcher's "Add to home screen" dialog is
    // its own window, so coming back from it is when a pin shows up.
    fun refreshPinned() {
        viewModelScope.launch {
            val now = withContext(Dispatchers.IO) { shortcutRepository.pinnedPackages() }
            pinned.value = now
            val waiting = awaitingPin
            if (waiting != null && waiting.packageName in now) {
                awaitingPin = null
                message.value = HomeMessage.Pinned(waiting.label)
            }
        }
    }

    fun requestPinShortcut(app: SelectedApp) {
        if (!shortcutRepository.requestPinShortcut(app.packageName, app.label)) {
            message.value = HomeMessage.Unsupported
            return
        }
        awaitingPin = app
        // Launchers that add the icon without asking never pause us.
        viewModelScope.launch {
            delay(AUTO_PIN_CHECK_MS)
            refreshPinned()
        }
    }

    fun clearMessage() {
        message.value = null
    }

    companion object {
        private const val AUTO_PIN_CHECK_MS = 800L

        fun factory(
            installedAppsRepository: InstalledAppsRepository,
            appSelectionRepository: AppSelectionRepository,
            shortcutRepository: ShortcutRepository,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(installedAppsRepository, appSelectionRepository, shortcutRepository) as T
        }
    }
}
