package org.entredeux.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.entredeux.app.domain.model.Look

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val lookKey = stringPreferencesKey("look")
    private val graceWindowKey = booleanPreferencesKey("grace_window")

    val look: Flow<Look> = context.settingsStore.data
        .map { prefs -> Look.entries.firstOrNull { it.name == prefs[lookKey] } ?: Look.PAPIER }

    val graceWindow: Flow<Boolean> = context.settingsStore.data
        .map { it[graceWindowKey] ?: true }

    suspend fun setLook(look: Look) {
        context.settingsStore.edit { it[lookKey] = look.name }
    }

    suspend fun setGraceWindow(enabled: Boolean) {
        context.settingsStore.edit { it[graceWindowKey] = enabled }
    }
}
