package org.entredeux.app

import android.app.Application
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.prefs.AppSelectionRepository

class EntreDeuxApplication : Application() {

    lateinit var installedAppsRepository: InstalledAppsRepository
        private set
    lateinit var appSelectionRepository: AppSelectionRepository
        private set

    override fun onCreate() {
        super.onCreate()
        installedAppsRepository = InstalledAppsRepository(this)
        appSelectionRepository = AppSelectionRepository(this)
    }
}
