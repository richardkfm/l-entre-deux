package org.entredeux.app.domain.usecase

import org.entredeux.app.domain.model.ReflectionStats
import org.entredeux.app.domain.model.ReflectionSummary

fun describeReflection(stats: ReflectionStats): ReflectionSummary {
    val periodTop = stats.timeOfDay.maxOf { it.count }
    val periods = stats.timeOfDay.filter { it.count == periodTop && it.count > 0 }
    val appTop = stats.perApp.maxOfOrNull { it.count } ?: 0
    val apps = stats.perApp.filter { it.count == appTop && it.count > 0 }
    return ReflectionSummary(
        totalPauses = stats.totalPauses,
        busiestPeriod = periods.singleOrNull()?.period,
        mostOftenApp = apps.singleOrNull()?.packageName,
        notNowCount = stats.backedOutCount,
    )
}
