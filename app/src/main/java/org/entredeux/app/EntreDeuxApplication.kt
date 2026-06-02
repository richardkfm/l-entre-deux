package org.entredeux.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.local.AppDatabase
import org.entredeux.app.data.local.PauseEventRepository
import org.entredeux.app.data.prefs.AppSelectionRepository
import org.entredeux.app.data.shortcuts.ShortcutRepository

class EntreDeuxApplication : Application() {

    // Outlives all ViewModels; used for fire-and-forget I/O that must not
    // be cancelled when the initiating screen is popped (e.g. pause logging).
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var installedAppsRepository: InstalledAppsRepository
        private set
    lateinit var appSelectionRepository: AppSelectionRepository
        private set
    lateinit var pauseEventRepository: PauseEventRepository
        private set
    lateinit var shortcutRepository: ShortcutRepository
        private set

    override fun onCreate() {
        super.onCreate()
        installedAppsRepository = InstalledAppsRepository(this)
        appSelectionRepository = AppSelectionRepository(this)
        pauseEventRepository = PauseEventRepository(AppDatabase.getInstance(this).pauseEventDao())
        shortcutRepository = ShortcutRepository(this)
    }
}
