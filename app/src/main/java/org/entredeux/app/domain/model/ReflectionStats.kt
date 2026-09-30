package org.entredeux.app.domain.model

data class AppPauseCount(val packageName: String, val count: Int)

data class IntentionCount(val intention: Intention, val count: Int)

enum class TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }

data class TimeOfDayCount(val period: TimeOfDay, val count: Int)

data class ReflectionStats(
    val totalPauses: Int,
    val backedOutCount: Int,
    val perApp: List<AppPauseCount>,
    val intentionMix: List<IntentionCount>,
    val timeOfDay: List<TimeOfDayCount>,
    // Pauses per local hour, index 0..23, for the day dial.
    val hourly: List<Int>,
)

// What the carnet says in words. Each observation is only made when it is
// clear: a tie for the busiest period or app says nothing rather than
// picking one arbitrarily.
data class ReflectionSummary(
    val totalPauses: Int,
    val busiestPeriod: TimeOfDay?,
    val mostOftenApp: String?,
    val notNowCount: Int,
)
