package com.example.proyecto1.presentation.prediccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.domain.repository.PrediccionRepository
import com.example.proyecto1.data.remote.FirestoreSeeder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PrediccionViewModel : ViewModel() {

    private val repo = PrediccionRepository()

    // opcional: Firestore por defecto
    private val db = FirebaseFirestore.getInstance()

    private val _prediccion = MutableStateFlow<Double?>(null)
    val prediccion: StateFlow<Double?> get() = _prediccion

    private val _slope = MutableStateFlow<Double?>(null)
    val slope: StateFlow<Double?> get() = _slope

    private val _intercept = MutableStateFlow<Double?>(null)
    val intercept: StateFlow<Double?> get() = _intercept

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    fun cargarYPredecir() {
        viewModelScope.launch {
            try {
                FirestoreSeeder.seedIfEmpty(db)
            } catch (_: Exception) {
                // ignorar problemas del seeder en producción
            }
            try {
                val (p, m, b) = repo.calcularPrediccion()
                _prediccion.value = p
                _slope.value = m
                _intercept.value = b
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
