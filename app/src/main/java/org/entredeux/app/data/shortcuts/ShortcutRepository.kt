package org.entredeux.app.data.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
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
            shortcutIcon(context.packageManager.getApplicationIcon(packageName))
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

    // Launchers shape adaptive icons themselves and give plain bitmaps the
    // legacy treatment (shrunk onto a plate), which made the pinned icon
    // look like a copy. Handing over the unmasked layers as an adaptive
    // bitmap lets the launcher mask it exactly like the original.
    private fun shortcutIcon(drawable: Drawable): Icon {
        if (drawable !is AdaptiveIconDrawable) return Icon.createWithBitmap(drawable.toBitmap())
        val size = (ADAPTIVE_ICON_DP * context.resources.displayMetrics.density).toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        listOfNotNull(drawable.background, drawable.foreground).forEach { layer ->
            layer.setBounds(0, 0, size, size)
            layer.draw(canvas)
        }
        return Icon.createWithAdaptiveBitmap(bitmap)
    }

    companion object {
        private const val ADAPTIVE_ICON_DP = 108

        const val ACTION_PAUSE_LAUNCH = "org.entredeux.app.action.PAUSE_LAUNCH"
        const val EXTRA_PACKAGE_NAME = "org.entredeux.app.extra.PACKAGE_NAME"
    }
}
