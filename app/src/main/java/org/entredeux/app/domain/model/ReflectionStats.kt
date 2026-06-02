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
)
