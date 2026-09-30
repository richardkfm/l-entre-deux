package org.entredeux.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.entredeux.app.R

// Spectral (Production Type, Paris; SIL OFL 1.1), subset to Latin. It is
// the app's voice: titles, the pause question and answers, epigraphs.
// Body and label text stay in the system sans for legibility at small sizes.
val Spectral = FontFamily(
    Font(R.font.spectral_light, FontWeight.Light),
    Font(R.font.spectral_regular, FontWeight.Normal),
    Font(R.font.spectral_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.spectral_medium, FontWeight.Medium),
)

private val base = Typography()

val PapierTypography = base.copy(
    displayLarge = base.displayLarge.copy(fontFamily = Spectral, fontWeight = FontWeight.Light),
    displayMedium = base.displayMedium.copy(fontFamily = Spectral, fontWeight = FontWeight.Light),
    displaySmall = base.displaySmall.copy(fontFamily = Spectral, fontWeight = FontWeight.Light),
    headlineLarge = base.headlineLarge.copy(fontFamily = Spectral, fontWeight = FontWeight.Light),
    headlineMedium = base.headlineMedium.copy(fontFamily = Spectral, fontWeight = FontWeight.Light),
    headlineSmall = base.headlineSmall.copy(fontFamily = Spectral, fontWeight = FontWeight.Normal),
    titleLarge = base.titleLarge.copy(fontFamily = Spectral, fontWeight = FontWeight.Normal),
)
