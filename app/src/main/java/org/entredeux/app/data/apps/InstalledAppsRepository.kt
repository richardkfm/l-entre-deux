package org.entredeux.app.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import org.entredeux.app.domain.model.SelectedApp

class InstalledAppsRepository(private val context: Context) {

    private val iconCache = LruCache<String, Bitmap>(ICON_CACHE_SIZE)

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
                    often = info.applicationInfo.category in OFTEN_CATEGORIES,
                )
            }
            .distinctBy { it.packageName }
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

    // Icons are drawn at one size everywhere (40 dp rows, the 40 dp pause
    // mark), so one cached bitmap per app is enough.
    fun getAppIcon(packageName: String): Bitmap? {
        iconCache.get(packageName)?.let { return it }
        val drawable: Drawable? = try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
        val bitmap = drawable?.toBitmap(ICON_PX, ICON_PX) ?: return null
        iconCache.put(packageName, bitmap)
        return bitmap
    }

    fun getLaunchIntent(packageName: String): Intent? =
        context.packageManager.getLaunchIntentForPackage(packageName)

    private companion object {
        const val ICON_PX = 128
        const val ICON_CACHE_SIZE = 200
        val OFTEN_CATEGORIES = setOf(
            ApplicationInfo.CATEGORY_SOCIAL,
            ApplicationInfo.CATEGORY_VIDEO,
            ApplicationInfo.CATEGORY_NEWS,
            ApplicationInfo.CATEGORY_GAME,
        )
    }
}
