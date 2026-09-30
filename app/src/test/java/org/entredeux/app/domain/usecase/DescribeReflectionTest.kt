package org.entredeux.app.domain.usecase

import org.entredeux.app.domain.model.AppPauseCount
import org.entredeux.app.domain.model.Intention
import org.entredeux.app.domain.model.IntentionCount
import org.entredeux.app.domain.model.ReflectionStats
import org.entredeux.app.domain.model.TimeOfDay
import org.entredeux.app.domain.model.TimeOfDayCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DescribeReflectionTest {

    @Test
    fun names_a_clear_busiest_period_and_app() {
        val summary = describeReflection(stats(periods = listOf(1, 0, 4, 2), apps = listOf("a" to 5, "b" to 2)))
        assertEquals(TimeOfDay.EVENING, summary.busiestPeriod)
        assertEquals("a", summary.mostOftenApp)
    }

    @Test
    fun a_tie_names_nothing() {
        val summary = describeReflection(stats(periods = listOf(3, 0, 3, 0), apps = listOf("a" to 2, "b" to 2)))
        assertNull(summary.busiestPeriod)
        assertNull(summary.mostOftenApp)
    }

    @Test
    fun counts_carry_over() {
        val summary = describeReflection(stats(periods = listOf(1, 1, 1, 0), apps = listOf("a" to 3), backedOut = 2))
        assertEquals(3, summary.totalPauses)
        assertEquals(2, summary.notNowCount)
    }

    private fun stats(periods: List<Int>, apps: List<Pair<String, Int>>, backedOut: Int = 0) = ReflectionStats(
        totalPauses = periods.sum(),
        backedOutCount = backedOut,
        perApp = apps.map { AppPauseCount(it.first, it.second) },
        intentionMix = Intention.entries.map { IntentionCount(it, 0) },
        timeOfDay = TimeOfDay.entries.mapIndexed { i, period -> TimeOfDayCount(period, periods[i]) },
        hourly = List(24) { 0 },
    )
}
