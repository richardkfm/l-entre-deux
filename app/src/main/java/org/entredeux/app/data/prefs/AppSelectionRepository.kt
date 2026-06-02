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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_selection")

class AppSelectionRepository(private val context: Context) {

    private val selectedPackagesKey = stringSetPreferencesKey("selected_packages")
    private val onboardingDoneKey = booleanPreferencesKey("onboarding_done")

    val selectedPackageNames: Flow<Set<String>> = context.dataStore.data
        .map { it[selectedPackagesKey] ?: emptySet() }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { it[onboardingDoneKey] ?: false }

    suspend fun setSelectedPackages(packages: Set<String>) {
        context.dataStore.edit { it[selectedPackagesKey] = packages }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { it[onboardingDoneKey] = true }
    }
}
