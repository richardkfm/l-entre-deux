package org.entredeux.app.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.entredeux.app.domain.model.PauseEvent

class PauseEventRepository(private val dao: PauseEventDao) {

    suspend fun record(event: PauseEvent) {
        dao.insert(event.toEntity())
    }

    fun recentEvents(limit: Int = 200): Flow<List<PauseEvent>> =
        dao.recentEvents(limit).map { list -> list.map { it.toDomain() } }

    fun allEvents(): Flow<List<PauseEvent>> =
        dao.allEvents().map { list -> list.map { it.toDomain() } }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}
