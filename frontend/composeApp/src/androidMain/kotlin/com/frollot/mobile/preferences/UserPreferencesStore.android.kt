package com.frollot.mobile.preferences

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implémentation Android de UserPreferencesStore utilisant DataStore.
 * 
 * Les préférences sont stockées de manière persistante dans DataStore.
 */
private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

class AndroidUserPreferencesStore(
    private val context: Context
) : UserPreferencesStore {
    
    override suspend fun getDarkMode(): Boolean? {
        return context.userPreferencesDataStore.data
            .map { preferences -> preferences[DARK_MODE_KEY] }
            .first()
    }
    
    override suspend fun setDarkMode(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
    
    override suspend fun clear() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences.remove(DARK_MODE_KEY)
        }
    }
}

/**
 * Factory function pour créer UserPreferencesStore sur Android.
 * 
 * Nécessite un Context, donc doit être appelée depuis un composable.
 */
@Composable
actual fun createUserPreferencesStore(): UserPreferencesStore {
    val context = LocalContext.current
    return remember(context) {
        AndroidUserPreferencesStore(context)
    }
}

