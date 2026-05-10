package org.entredeux.app.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import org.entredeux.app.domain.model.SelectedApp

class InstalledAppsRepository(private val context: Context) {

    fun getInstalledApps(): List<SelectedApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val pm = context.packageManager
        @Suppress("DEPRECATION")
        return pm.queryIntentActivities(intent, 0)
            .map { it.activityInfo }
            .filter { it.packageName != context.packageName }
            .map { info ->
                SelectedApp(
                    packageName = info.packageName,
                    label = info.loadLabel(pm).toString(),
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun getAppLabel(packageName: String): String? {
        return try {
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun getLaunchIntent(packageName: String): Intent? =
        context.packageManager.getLaunchIntentForPackage(packageName)
}
