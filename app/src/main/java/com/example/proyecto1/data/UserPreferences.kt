package com.example.proyecto1.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// Top-level property to ensure a single instance of DataStore throughout the app.
val Context.dataStore by preferencesDataStore(name = "user_prefs")
