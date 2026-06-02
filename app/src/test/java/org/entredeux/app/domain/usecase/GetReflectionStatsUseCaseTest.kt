package org.entredeux.app.domain.usecase

import org.entredeux.app.domain.model.Intention
import org.entredeux.app.domain.model.PauseEvent
import org.entredeux.app.domain.model.PauseOutcome
import org.entredeux.app.domain.model.TimeOfDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GetReflectionStatsUseCaseTest {

    @Test
    fun empty_list_returns_null() {
        assertNull(getReflectionStats(emptyList()))
    }

    @Test
    fun total_pause_count_matches_event_count() {
        val events = listOf(
            pause(pkg = "a", outcome = PauseOutcome.PROCEEDED),
            pause(pkg = "b", outcome = PauseOutcome.PROCEEDED),
            pause(pkg = "a", outcome = PauseOutcome.BACKED_OUT),
        )
        assertEquals(3, getReflectionStats(events)!!.totalPauses)
    }

    @Test
    fun backed_out_count_is_correct() {
        val events = listOf(
            pause(outcome = PauseOutcome.PROCEEDED),
            pause(outcome = PauseOutcome.BACKED_OUT),
            pause(outcome = PauseOutcome.BACKED_OUT),
        )
        assertEquals(2, getReflectionStats(events)!!.backedOutCount)
    }

    @Test
    fun per_app_sorted_by_count_descending() {
        val events = listOf(
            pause(pkg = "com.b"),
            pause(pkg = "com.a"),
            pause(pkg = "com.a"),
        )
        val perApp = getReflectionStats(events)!!.perApp
        assertEquals("com.a", perApp[0].packageName)
        assertEquals(2, perApp[0].count)
        assertEquals("com.b", perApp[1].packageName)
        assertEquals(1, perApp[1].count)
    }

    @Test
    fun intention_mix_counts_all_entries() {
        val events = listOf(
            pause(intention = Intention.SPECIFIC_TASK),
            pause(intention = Intention.SPECIFIC_TASK),
            pause(intention = Intention.AUTOPILOT),
        )
        val mix = getReflectionStats(events)!!.intentionMix.associate { it.intention to it.count }
        assertEquals(2, mix[Intention.SPECIFIC_TASK])
        assertEquals(0, mix[Intention.BRIEF_CHECK])
        assertEquals(1, mix[Intention.AUTOPILOT])
    }

    @Test
    fun time_of_day_buckets_morning_and_afternoon() {
        val zone = ZoneId.systemDefault()
        val nineAm = LocalDate.now().atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
        val twoPm = LocalDate.now().atTime(14, 0).atZone(zone).toInstant().toEpochMilli()
        val events = listOf(
            pause(ts = nineAm),
            pause(ts = nineAm),
            pause(ts = twoPm),
        )
        val tod = getReflectionStats(events)!!.timeOfDay.associate { it.period to it.count }
        assertEquals(2, tod[TimeOfDay.MORNING])
        assertEquals(1, tod[TimeOfDay.AFTERNOON])
        assertEquals(0, tod[TimeOfDay.EVENING])
        assertEquals(0, tod[TimeOfDay.NIGHT])
    }

    private fun pause(
        pkg: String = "com.example",
        outcome: PauseOutcome = PauseOutcome.PROCEEDED,
        intention: Intention = Intention.BRIEF_CHECK,
        ts: Long = System.currentTimeMillis(),
    ) = PauseEvent(
        timestamp = ts,
        packageName = pkg,
        intentionKey = intention.stableKey,
        outcome = outcome,
    )
}
