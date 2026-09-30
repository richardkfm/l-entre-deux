package org.entredeux.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PauseEventDao {

    @Insert
    suspend fun insert(event: PauseEventEntity)

    @Query("SELECT * FROM pause_events ORDER BY timestamp DESC LIMIT :limit")
    fun recentEvents(limit: Int = 200): Flow<List<PauseEventEntity>>

    @Query("SELECT * FROM pause_events ORDER BY timestamp DESC")
    fun allEvents(): Flow<List<PauseEventEntity>>

    @Query("SELECT MAX(timestamp) FROM pause_events WHERE packageName = :packageName AND outcome = 'PROCEEDED'")
    suspend fun lastProceededAt(packageName: String): Long?

    @Query("DELETE FROM pause_events")
    suspend fun deleteAll()
}
