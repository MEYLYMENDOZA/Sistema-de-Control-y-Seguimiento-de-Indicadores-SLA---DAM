package com.example.proyecto1.ui.prediction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.ui.gestion.GestionDatosViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.apache.commons.math3.stat.regression.SimpleRegression
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PredictionUiState(
    val projectedSla: Float = 0f,
    val slope: Double = 0.0,
    val intercept: Double = 0.0,
    val isTrendNegative: Boolean = false,
    val lastUpdated: String = "",
    val recordCount: Int = 0,
    val isPredictionReady: Boolean = false,
    val showWarning: Boolean = false,
    val availablePeriods: List<String> = emptyList(),
    val selectedPeriod: String = "Seleccionar período"
)

class PredictionViewModel(private val gestionDatosViewModel: GestionDatosViewModel) : ViewModel() {

    private val warningThreshold = 75.0f
    private val _selectedPeriod = MutableStateFlow("Seleccionar período")
    private val _recalculationTrigger = MutableStateFlow(0)

    val uiState: StateFlow<PredictionUiState> = combine(
        gestionDatosViewModel.uiState, _selectedPeriod, _recalculationTrigger
    ) { gestionState, selectedPeriod, _ ->
        val records = gestionState.records
        if (records.size < 2) {
            return@combine PredictionUiState(recordCount = records.size)
        }

        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val displayMonthFormat = SimpleDateFormat("MMMM 'de' yyyy", Locale("es", "ES"))

        val allMonthlyCompliance = records
            .mapNotNull { record ->
                try {
                    inputFormat.parse(record.fechaIngreso)?.let { monthFormat.format(it) to record.cumple }
                } catch (e: Exception) { null }
            }
            .groupBy { it.first }
            .map { (month, entries) ->
                val total = entries.size
                val compliant = entries.count { it.second }
                val percentage = if (total > 0) (compliant.toFloat() / total) * 100f else 0f
                Triple(month, percentage, monthFormat.parse(month))
            }
            .sortedBy { it.third }

        val availablePeriods = allMonthlyCompliance.map { displayMonthFormat.format(it.third!!) }

        val dataForRegression = if (selectedPeriod == "Seleccionar período" || !availablePeriods.contains(selectedPeriod)) {
            allMonthlyCompliance
        } else {
            val selectedIndex = availablePeriods.indexOf(selectedPeriod)
            allMonthlyCompliance.take(selectedIndex + 1)
        }

        if (dataForRegression.size < 2) {
            return@combine PredictionUiState(recordCount = records.size, availablePeriods = listOf("Seleccionar período") + availablePeriods, selectedPeriod = selectedPeriod)
        }

        val regression = SimpleRegression()
        dataForRegression.forEachIndexed { index, data ->
            regression.addData(index.toDouble(), data.second.toDouble())
        }

        val slope = regression.slope
        val intercept = regression.intercept
        val nextMonthIndex = dataForRegression.size
        val projectedSla = (slope * nextMonthIndex + intercept).toFloat().coerceIn(0f, 100f)

        val lastUpdatedFormat = SimpleDateFormat("dd 'de' MMMM, HH:mm:ss", Locale("es", "ES"))

        PredictionUiState(
            projectedSla = projectedSla,
            slope = slope,
            intercept = intercept,
            isTrendNegative = slope < 0,
            lastUpdated = lastUpdatedFormat.format(Date()),
            recordCount = records.size,
            isPredictionReady = true,
            showWarning = projectedSla < warningThreshold,
            availablePeriods = listOf("Seleccionar período") + availablePeriods,
            selectedPeriod = selectedPeriod
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PredictionUiState()
    )

    fun onPeriodSelected(period: String) {
        _selectedPeriod.value = period
    }

    // CORRECCIÓN: Renombrada para claridad. Refresca el cálculo para el período actual.
    fun forceRecalculation() {
        _recalculationTrigger.value++
    }

    // CORRECCIÓN: Nueva función para el botón "Actualizar Datos". Restablece la selección.
    fun resetPeriodAndUpdate() {
        _selectedPeriod.value = "Seleccionar período"
    }
}
