package org.entredeux.app.domain.usecase

import org.entredeux.app.domain.model.AppPauseCount
import org.entredeux.app.domain.model.Intention
import org.entredeux.app.domain.model.IntentionCount
import org.entredeux.app.domain.model.PauseEvent
import org.entredeux.app.domain.model.PauseOutcome
import org.entredeux.app.domain.model.ReflectionStats
import org.entredeux.app.domain.model.TimeOfDay
import org.entredeux.app.domain.model.TimeOfDayCount
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

fun getReflectionStats(events: List<PauseEvent>): ReflectionStats? {
    if (events.isEmpty()) return null

    val perApp = events.groupBy { it.packageName }
        .map { (pkg, evts) -> AppPauseCount(pkg, evts.size) }
        .sortedByDescending { it.count }

    val intentionMix = Intention.entries.map { intention ->
        IntentionCount(intention, events.count { it.intentionKey == intention.stableKey })
    }

    val zone = ZoneId.systemDefault()
    val todCounts = IntArray(4)
    events.forEach { event ->
        val hour = ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.timestamp), zone).hour
        todCounts[hourToIndex(hour)]++
    }

    return ReflectionStats(
        totalPauses = events.size,
        backedOutCount = events.count { it.outcome == PauseOutcome.BACKED_OUT },
        perApp = perApp,
        intentionMix = intentionMix,
        timeOfDay = listOf(
            TimeOfDayCount(TimeOfDay.MORNING, todCounts[0]),
            TimeOfDayCount(TimeOfDay.AFTERNOON, todCounts[1]),
            TimeOfDayCount(TimeOfDay.EVENING, todCounts[2]),
            TimeOfDayCount(TimeOfDay.NIGHT, todCounts[3]),
        ),
        withBudgetCount = events.count { it.budgetMinutes != null },
    )
}

private fun hourToIndex(hour: Int) = when (hour) {
    in 6..11 -> 0
    in 12..17 -> 1
    in 18..22 -> 2
    else -> 3
}
