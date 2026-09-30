package org.entredeux.app.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsWithinGraceWindowTest {

    private val now = 1_000_000_000L

    @Test
    fun never_proceeded_is_outside() {
        assertFalse(isWithinGraceWindow(lastProceededAt = null, now = now))
    }

    @Test
    fun just_proceeded_is_inside() {
        assertTrue(isWithinGraceWindow(lastProceededAt = now - 30_000, now = now))
    }

    @Test
    fun window_end_is_exclusive() {
        assertTrue(isWithinGraceWindow(lastProceededAt = now - GRACE_WINDOW_MS + 1, now = now))
        assertFalse(isWithinGraceWindow(lastProceededAt = now - GRACE_WINDOW_MS, now = now))
    }

    @Test
    fun a_timestamp_in_the_future_is_outside() {
        // A clock change can put the last pause "ahead" of now; don't let
        // that silently skip every pause.
        assertFalse(isWithinGraceWindow(lastProceededAt = now + 5_000, now = now))
    }
}
