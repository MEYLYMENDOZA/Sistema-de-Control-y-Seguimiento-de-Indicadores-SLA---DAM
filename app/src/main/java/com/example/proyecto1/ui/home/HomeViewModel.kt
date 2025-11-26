package com.example.proyecto1.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.ui.gestion.GestionDatosViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val roles: List<String> = emptyList(),
    val slaTypes: List<String> = emptyList(),
    val selectedRole: String = "Todos los roles",
    val selectedSlaType: String = "Todos los tipos",
    val compliancePercentage: Float = 0f,
    val averageDays: Float = 0f,
    val totalCases: Int = 0,
    val compliantCases: Int = 0,
    val nonCompliantCases: Int = 0
)

class HomeViewModel(gestionDatosViewModel: GestionDatosViewModel) : ViewModel() {

    private val _selectedRole = MutableStateFlow("Todos los roles")
    private val _selectedSlaType = MutableStateFlow("Todos los tipos")

    val uiState: StateFlow<HomeUiState> = combine(
        gestionDatosViewModel.uiState,
        _selectedRole,
        _selectedSlaType
    ) { gestionState, role, slaType ->
        val records = gestionState.records

        val filteredRecords = records
            .filter { role == "Todos los roles" || it.rol == role }
            .filter { slaType == "Todos los tipos" || it.tipoSla == slaType }

        val total = filteredRecords.size
        val compliant = filteredRecords.count { it.cumple }
        val nonCompliant = total - compliant
        val percentage = if (total > 0) (compliant.toFloat() / total) * 100f else 0f
        val avgDays = if (total > 0) filteredRecords.sumOf { it.diasSla }.toFloat() / total else 0f

        HomeUiState(
            roles = listOf("Todos los roles") + records.map { it.rol }.distinct(),
            slaTypes = listOf("Todos los tipos") + records.map { it.tipoSla }.distinct(),
            selectedRole = role,
            selectedSlaType = slaType,
            totalCases = total,
            compliantCases = compliant,
            nonCompliantCases = nonCompliant,
            compliancePercentage = percentage,
            averageDays = avgDays
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onRoleSelected(role: String) {
        _selectedRole.value = role
    }

    fun onSlaTypeSelected(slaType: String) {
        _selectedSlaType.value = slaType
    }
}
