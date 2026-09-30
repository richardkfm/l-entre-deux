package org.entredeux.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import org.entredeux.app.domain.model.Look

private val MaterialLight = lightColorScheme()
private val MaterialDark = darkColorScheme()

// Screens that draw differently per look (the pause) read this.
val LocalLook = staticCompositionLocalOf { Look.PAPIER }

@Composable
fun EntreDeuxTheme(
    look: Look = Look.PAPIER,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when (look) {
        Look.PAPIER -> if (darkTheme) NuitColors else PapierColors
        Look.MATERIAL -> when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> MaterialDark
            else -> MaterialLight
        }
    }
    val typography = if (look == Look.PAPIER) PapierTypography else Typography()
    CompositionLocalProvider(LocalLook provides look) {
        MaterialTheme(colorScheme = colorScheme, typography = typography, content = content)
    }
}
