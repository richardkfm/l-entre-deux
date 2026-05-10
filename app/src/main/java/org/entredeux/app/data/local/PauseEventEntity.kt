package org.entredeux.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.entredeux.app.domain.model.PauseEvent
import org.entredeux.app.domain.model.PauseOutcome

@Entity(tableName = "pause_events")
data class PauseEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val packageName: String,
    val intentionKey: String,
    val budgetMinutes: Int?,
    val outcome: String,
) {
    fun toDomain() = PauseEvent(
        id = id,
        timestamp = timestamp,
        packageName = packageName,
        intentionKey = intentionKey,
        budgetMinutes = budgetMinutes,
        outcome = PauseOutcome.valueOf(outcome),
    )
}

fun PauseEvent.toEntity() = PauseEventEntity(
    id = id,
    timestamp = timestamp,
    packageName = packageName,
    intentionKey = intentionKey,
    budgetMinutes = budgetMinutes,
    outcome = outcome.name,
)
