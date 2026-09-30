package org.entredeux.app.domain.usecase

const val GRACE_WINDOW_MS = 2 * 60 * 1000L

// Reopening an app right after choosing to open it through the pause is
// the same visit, not a new reach, so it shouldn't be asked about again.
fun isWithinGraceWindow(
    lastProceededAt: Long?,
    now: Long,
    windowMillis: Long = GRACE_WINDOW_MS,
): Boolean = lastProceededAt != null && now - lastProceededAt in 0 until windowMillis
