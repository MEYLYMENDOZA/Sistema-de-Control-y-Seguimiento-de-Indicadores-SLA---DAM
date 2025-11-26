package com.example.proyecto1.ui.prediction

import androidx.lifecycle.ViewModel
import com.example.proyecto1.ui.gestion.SlaRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.apache.commons.math3.stat.regression.SimpleRegression

data class PredictionUiState(
    val predictionEnabled: Boolean = false,
    val predictionSlope: Double = 0.0,
    val predictionIntercept: Double = 0.0,
    val nextSlaPrediction: Double = 0.0
)

class PredictionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PredictionUiState())
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()

    fun updateSlaRecords(records: List<SlaRecord>) {
        if (records.size < 2) {
            _uiState.update { it.copy(predictionEnabled = false) }
            return
        }

        val regression = SimpleRegression()
        records.forEachIndexed { index, record ->
            regression.addData(index.toDouble(), record.diasSla.toDouble())
        }

        val slope = regression.slope
        val intercept = regression.intercept
        val nextIndex = records.size.toDouble()
        val nextPrediction = slope * nextIndex + intercept

        _uiState.update {
            it.copy(
                predictionEnabled = true,
                predictionSlope = slope,
                predictionIntercept = intercept,
                nextSlaPrediction = nextPrediction
            )
        }
    }
}