package org.entredeux.app.data.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Icon
import androidx.core.graphics.drawable.toBitmap
import org.entredeux.app.R

class ShortcutRepository(private val context: Context) {

    fun isSupported(): Boolean =
        context.getSystemService(ShortcutManager::class.java)
            ?.isRequestPinShortcutSupported == true

    fun requestPinShortcut(packageName: String, label: String): Boolean {
        val sm = context.getSystemService(ShortcutManager::class.java) ?: return false
        if (!sm.isRequestPinShortcutSupported) return false

        val shortcutIntent = Intent(ACTION_PAUSE_LAUNCH).apply {
            setClassName(context.packageName, "${context.packageName}.MainActivity")
            putExtra(EXTRA_PACKAGE_NAME, packageName)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val icon = try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            Icon.createWithBitmap(drawable.toBitmap())
        } catch (_: PackageManager.NameNotFoundException) {
            Icon.createWithResource(context, R.mipmap.ic_launcher)
        }

        val info = ShortcutInfo.Builder(context, "pause_$packageName")
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(icon)
            .setIntent(shortcutIntent)
            .build()

        sm.requestPinShortcut(info, null)
        return true
    }

    companion object {
        const val ACTION_PAUSE_LAUNCH = "org.entredeux.app.action.PAUSE_LAUNCH"
        const val EXTRA_PACKAGE_NAME = "org.entredeux.app.extra.PACKAGE_NAME"
    }
}
