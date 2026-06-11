package com.frollot.mobile.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implémentation Android de LanguagePreferences utilisant DataStore.
 * 
 * Conforme à l'ADR-001 - DÉCISION 2 : Persistance locale via DataStore.
 */
private val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "language_preferences"
)

private val LANGUAGE_KEY = stringPreferencesKey("preferred_language")

class AndroidLanguagePreferences(
    private val context: Context
) : LanguagePreferences {
    
    override suspend fun getLanguage(): String? {
        return context.languageDataStore.data
            .map { preferences -> preferences[LANGUAGE_KEY] }
            .first()
    }
    
    override suspend fun setLanguage(languageCode: String) {
        context.languageDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }
    
    override suspend fun clear() {
        context.languageDataStore.edit { preferences ->
            preferences.remove(LANGUAGE_KEY)
        }
    }
}

/**
 * Factory function pour créer LanguagePreferences sur Android.
 * 
 * Nécessite un Context, donc doit être appelée depuis un composable.
 */
@Composable
actual fun createLanguagePreferences(): LanguagePreferences {
    val context = LocalContext.current
    return remember(context) {
        AndroidLanguagePreferences(context)
    }
}

