package org.entredeux.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.entredeux.app.domain.usecase.toggleAppSelection

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_selection")

class AppSelectionRepository(private val context: Context) {

    private val selectedPackagesKey = stringSetPreferencesKey("selected_packages")
    private val onboardingDoneKey = booleanPreferencesKey("onboarding_done")
    private val homeCoachDoneKey = booleanPreferencesKey("home_coach_done")

    val selectedPackageNames: Flow<Set<String>> = context.dataStore.data
        .map { it[selectedPackagesKey] ?: emptySet() }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { it[onboardingDoneKey] ?: false }

    val homeCoachCompleted: Flow<Boolean> = context.dataStore.data
        .map { it[homeCoachDoneKey] ?: false }

    // Read and write inside one edit so quick successive taps can't
    // overwrite each other with a stale copy of the selection.
    suspend fun toggle(packageName: String) {
        context.dataStore.edit {
            it[selectedPackagesKey] = toggleAppSelection(it[selectedPackagesKey] ?: emptySet(), packageName)
        }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { it[onboardingDoneKey] = true }
    }

    suspend fun setHomeCoachCompleted() {
        context.dataStore.edit { it[homeCoachDoneKey] = true }
    }
}
