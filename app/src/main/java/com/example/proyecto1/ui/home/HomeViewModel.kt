package com.example.proyecto1.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.ui.gestion.GestionDatosViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class RoleCompliance(val role: String, val compliantCount: Int, val nonCompliantCount: Int)
data class MonthlyCompliance(val yearMonth: String, val percentage: Float)
data class SlaTypeCompliance(val slaType: String, val compliantCount: Int, val nonCompliantCount: Int)

data class HomeUiState(
    val roles: List<String> = emptyList(),
    val slaTypes: List<String> = emptyList(),
    val periods: List<String> = emptyList(),
    val selectedRole: String = "Todos los roles",
    val selectedSlaType: String = "Todos los tipos",
    val selectedPeriod: String = "Todo el periodo",
    val compliancePercentage: Float = 0f,
    val averageDays: Float = 0f,
    val totalCases: Int = 0,
    val compliantCases: Int = 0,
    val nonCompliantCases: Int = 0,
    val complianceByRole: List<RoleCompliance> = emptyList(),
    val monthlyCompliance: List<MonthlyCompliance> = emptyList(),
    val complianceBySlaType: List<SlaTypeCompliance> = emptyList()
)

class HomeViewModel(gestionDatosViewModel: GestionDatosViewModel) : ViewModel() {

    private val _selectedRole = MutableStateFlow("Todos los roles")
    private val _selectedSlaType = MutableStateFlow("Todos los tipos")
    private val _selectedPeriod = MutableStateFlow("Todo el periodo")

    private val periodOptions = listOf("Todo el periodo", "Últimos 7 días", "Últimos 30 días", "Últimos 90 días", "Últimos 6 meses", "Último año")

    val uiState: StateFlow<HomeUiState> = combine(
        gestionDatosViewModel.uiState, _selectedRole, _selectedSlaType, _selectedPeriod
    ) { gestionState, role, slaType, period ->
        val records = gestionState.records
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        val filteredRecords = records
            .filter { role == "Todos los roles" || it.rol == role }
            .filter { slaType == "Todos los tipos" || it.tipoSla == slaType }
            .filter { record ->
                if (period == "Todo el periodo") true else {
                    try {
                        val recordDate = inputFormat.parse(record.fechaIngreso) ?: return@filter false
                        val limitDate = Calendar.getInstance().apply {
                            when (period) {
                                "Últimos 7 días" -> add(Calendar.DAY_OF_YEAR, -7)
                                "Últimos 30 días" -> add(Calendar.DAY_OF_YEAR, -30)
                                "Últimos 90 días" -> add(Calendar.DAY_OF_YEAR, -90)
                                "Últimos 6 meses" -> add(Calendar.MONTH, -6)
                                "Último año" -> add(Calendar.YEAR, -1)
                            }
                        }.time
                        recordDate.after(limitDate)
                    } catch (e: Exception) { false }
                }
            }

        val total = filteredRecords.size
        val compliant = filteredRecords.count { it.cumple }
        val nonCompliant = total - compliant
        val percentage = if (total > 0) (compliant.toFloat() / total) * 100f else 0f
        val avgDays = if (total > 0) filteredRecords.sumOf { it.diasSla }.toFloat() / total else 0f

        val complianceByRoleData = filteredRecords.groupBy { it.rol }.map { RoleCompliance(it.key, it.value.count { r -> r.cumple }, it.value.count { r -> !r.cumple }) }
        
        val complianceBySlaTypeData = filteredRecords.groupBy { it.tipoSla }.map { SlaTypeCompliance(it.key, it.value.count { r -> r.cumple }, it.value.count { r -> !r.cumple }) }

        val monthlyComplianceData = filteredRecords
            .mapNotNull { record -> try { inputFormat.parse(record.fechaIngreso)?.let { outputFormat.format(it) to record.cumple } } catch (e: Exception) { null } }
            .groupBy { it.first }
            .map { (month, entries) ->
                val monthTotal = entries.size
                val monthCompliant = entries.count { it.second }
                val monthPercentage = if (monthTotal > 0) (monthCompliant.toFloat() / monthTotal) * 100f else 0f
                MonthlyCompliance(month, monthPercentage)
            }.sortedBy { it.yearMonth }

        HomeUiState(
            roles = listOf("Todos los roles") + records.map { it.rol }.distinct(),
            slaTypes = listOf("Todos los tipos") + records.map { it.tipoSla }.distinct(),
            periods = periodOptions,
            selectedRole = role,
            selectedSlaType = slaType,
            selectedPeriod = period,
            totalCases = total,
            compliantCases = compliant,
            nonCompliantCases = nonCompliant,
            compliancePercentage = percentage,
            averageDays = avgDays,
            complianceByRole = complianceByRoleData,
            monthlyCompliance = monthlyComplianceData,
            complianceBySlaType = complianceBySlaTypeData
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(periods = periodOptions)
    )

    fun onRoleSelected(role: String) { _selectedRole.value = role }
    fun onSlaTypeSelected(slaType: String) { _selectedSlaType.value = slaType }
    fun onPeriodSelected(period: String) { _selectedPeriod.value = period }
}
