package org.entredeux.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.entredeux.app.domain.model.PauseEvent
import org.entredeux.app.domain.model.PauseOutcome
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PauseEventRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: PauseEventRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        repo = PauseEventRepository(db.pauseEventDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun inserting_event_appears_in_recent_events() = runTest {
        repo.record(pauseEvent(outcome = PauseOutcome.PROCEEDED))
        val events = repo.recentEvents().first()
        assertEquals(1, events.size)
        assertEquals(PauseOutcome.PROCEEDED, events[0].outcome)
    }

    @Test
    fun events_returned_newest_first() = runTest {
        repo.record(pauseEvent(timestamp = 1_000L))
        repo.record(pauseEvent(timestamp = 2_000L))
        val events = repo.recentEvents().first()
        assertEquals(2_000L, events[0].timestamp)
        assertEquals(1_000L, events[1].timestamp)
    }

    @Test
    fun deleteAll_clears_all_events() = runTest {
        repo.record(pauseEvent())
        repo.record(pauseEvent())
        repo.deleteAll()
        assertTrue(repo.recentEvents().first().isEmpty())
    }

    @Test
    fun fields_round_trip_correctly() = runTest {
        val event = PauseEvent(
            timestamp = 42_000L,
            packageName = "com.example.app",
            intentionKey = "brief_check",
            budgetMinutes = 5,
            outcome = PauseOutcome.PROCEEDED,
        )
        repo.record(event)
        val stored = repo.recentEvents().first().single()
        assertEquals(event.packageName, stored.packageName)
        assertEquals(event.intentionKey, stored.intentionKey)
        assertEquals(event.budgetMinutes, stored.budgetMinutes)
        assertEquals(event.outcome, stored.outcome)
    }

    private fun pauseEvent(
        timestamp: Long = System.currentTimeMillis(),
        outcome: PauseOutcome = PauseOutcome.PROCEEDED,
    ) = PauseEvent(
        timestamp = timestamp,
        packageName = "com.example.test",
        intentionKey = "specific_task",
        budgetMinutes = null,
        outcome = outcome,
    )
}
