package org.entredeux.app.data.shortcuts

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import androidx.core.graphics.drawable.toBitmap
import org.entredeux.app.PauseActivity
import org.entredeux.app.R

class ShortcutRepository(private val context: Context) {

    fun requestPinShortcut(packageName: String, label: String): Boolean {
        val sm = context.getSystemService(ShortcutManager::class.java) ?: return false
        if (!sm.isRequestPinShortcutSupported) return false

        val shortcutIntent = PauseActivity.intent(context, packageName)

        val appIcon: Drawable? = try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
        val icon = appIcon?.let(::shortcutIcon) ?: Icon.createWithResource(context, R.mipmap.ic_launcher)

        val info = ShortcutInfo.Builder(context, ID_PREFIX + packageName)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(icon)
            .setIntent(shortcutIntent)
            .build()

        sm.requestPinShortcut(info, null)
        return true
    }

    // Which of our shortcuts the launcher currently shows. Read on resume, so
    // the Apps list reflects what's really on the home screen.
    fun pinnedPackages(): Set<String> {
        val sm = context.getSystemService(ShortcutManager::class.java) ?: return emptySet()
        return sm.pinnedShortcuts
            .filter { it.isEnabled && it.id.startsWith(ID_PREFIX) }
            .map { it.id.removePrefix(ID_PREFIX) }
            .toSet()
    }

    // Shortcuts pinned before 1.1.0 open MainActivity, which then had to
    // start the whole app before the pause. Pinned shortcuts can be updated
    // in place, so point them at PauseActivity; only the intent changes.
    fun migratePinnedShortcuts() {
        val sm = context.getSystemService(ShortcutManager::class.java) ?: return
        val updates = sm.pinnedShortcuts
            .filter { it.id.startsWith(ID_PREFIX) }
            .map { pinned ->
                ShortcutInfo.Builder(context, pinned.id)
                    .setIntent(PauseActivity.intent(context, pinned.id.removePrefix(ID_PREFIX)))
                    .build()
            }
        if (updates.isEmpty()) return
        try {
            sm.updateShortcuts(updates)
        } catch (_: IllegalStateException) {
            // Rate-limited; the legacy route through MainActivity still works.
        }
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
        private const val ID_PREFIX = "pause_"

        const val ACTION_PAUSE_LAUNCH = "org.entredeux.app.action.PAUSE_LAUNCH"
        const val EXTRA_PACKAGE_NAME = "org.entredeux.app.extra.PACKAGE_NAME"
    }
}
