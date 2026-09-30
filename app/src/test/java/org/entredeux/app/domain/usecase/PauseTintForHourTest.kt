package org.entredeux.app.domain.usecase

import org.entredeux.app.domain.model.PauseTint
import org.junit.Assert.assertEquals
import org.junit.Test

class PauseTintForHourTest {

    @Test
    fun day_runs_from_six_to_six() {
        assertEquals(PauseTint.JOUR, pauseTintForHour(6))
        assertEquals(PauseTint.JOUR, pauseTintForHour(17))
    }

    @Test
    fun evening_is_the_blue_hour() {
        assertEquals(PauseTint.HEURE_BLEUE, pauseTintForHour(18))
        assertEquals(PauseTint.HEURE_BLEUE, pauseTintForHour(21))
    }

    @Test
    fun late_and_early_hours_are_night() {
        assertEquals(PauseTint.NUIT, pauseTintForHour(22))
        assertEquals(PauseTint.NUIT, pauseTintForHour(0))
        assertEquals(PauseTint.NUIT, pauseTintForHour(5))
    }
}
