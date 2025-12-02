package com.example.proyecto1.presentation.session

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val appContext = getApplication<Application>().applicationContext
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    suspend fun isUserLoggedIn(): Boolean = appContext.dataStore.data
        .map { it[IS_LOGGED_IN] ?: false }
        .first()

    fun saveSession() {
        viewModelScope.launch {
            appContext.dataStore.edit { preferences ->
                preferences[IS_LOGGED_IN] = true
            }
        }
    }

    fun clearSession() {
        viewModelScope.launch {
            appContext.dataStore.edit { preferences ->
                preferences[IS_LOGGED_IN] = false
            }
        }
    }
}
