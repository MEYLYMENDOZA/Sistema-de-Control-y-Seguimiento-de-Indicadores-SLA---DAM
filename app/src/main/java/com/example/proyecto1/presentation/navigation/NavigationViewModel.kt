package com.example.proyecto1.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.SlaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    slaRepository: SlaRepository
) : ViewModel() {

    // Exposes a boolean StateFlow that is true if there are items in the repository,
    // and false otherwise. The UI (the navigation drawer) will observe this.
    val isGestionDeDatosEnabled: StateFlow<Boolean> = slaRepository.slaItems
        .map { it.isNotEmpty() } // Map the list of items to a boolean
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Start collecting when the UI is visible
            initialValue = false // Initially disabled
        )
}
