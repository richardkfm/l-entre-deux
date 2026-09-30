package org.entredeux.app.domain.usecase

import org.entredeux.app.domain.model.PauseTint

fun pauseTintForHour(hour: Int): PauseTint = when (hour) {
    in 6..17 -> PauseTint.JOUR
    in 18..21 -> PauseTint.HEURE_BLEUE
    else -> PauseTint.NUIT
}
