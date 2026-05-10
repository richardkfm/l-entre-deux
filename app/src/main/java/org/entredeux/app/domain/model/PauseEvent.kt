package org.entredeux.app.domain.model

data class PauseEvent(
    val id: Long = 0,
    val timestamp: Long,
    val packageName: String,
    val intentionKey: String,
    val budgetMinutes: Int?,
    val outcome: PauseOutcome,
)
