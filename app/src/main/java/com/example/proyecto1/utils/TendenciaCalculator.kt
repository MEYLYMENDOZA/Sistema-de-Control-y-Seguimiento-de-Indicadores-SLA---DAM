package com.example.proyecto1.utils

import android.util.Log
import com.example.proyecto1.data.remote.dto.*

/**
 * Calculadora de Tendencia SLA con Regresión Lineal
 * US-12: Arquitectura Simplificada
 *
 * RESPONSABILIDAD: Calcular tendencia y proyección LOCALMENTE en la app
 * - Recibe datos crudos del backend
 * - Calcula regresión lineal: y = mx + b
 * - Calcula proyección para el siguiente mes
 * - Determina estado de tendencia (MEJORANDO/EMPEORANDO/ESTABLE)
 */
class TendenciaCalculator {

    companion object {
        private const val TAG = "TendenciaCalculator"
        private const val UMBRAL_PENDIENTE = 0.5 // Umbral para determinar si la tendencia es significativa
    }

    /**
     * Calcula la tendencia completa a partir de datos mensuales crudos
     *
     * @param datosMensuales Lista de datos mensuales con porcentaje de cumplimiento
     * @return TendenciaCalculadaLocal con todos los cálculos o null si no hay suficientes datos
     */
    fun calcularTendencia(datosMensuales: List<DatoMensualCrudoDto>): TendenciaCalculadaLocal? {

        // Validar datos mínimos (necesitamos al menos 3 puntos para regresión lineal)
        if (datosMensuales.size < 3) {
            Log.w(TAG, "⚠️ Datos insuficientes para calcular tendencia (mínimo 3 meses, recibidos: ${datosMensuales.size})")
            return null
        }

        Log.d(TAG, "📈 Calculando regresión lineal con ${datosMensuales.size} puntos de datos...")

        // Extraer valores para cálculo
        val n = datosMensuales.size
        val x = (1..n).map { it.toDouble() } // Índices: 1, 2, 3, ..., n
        val y = datosMensuales.map { it.porcentajeCumplimiento } // Porcentajes de cumplimiento

        // Calcular regresión lineal: y = mx + b
        val regresion = calcularRegresionLineal(x, y)
        val pendiente = regresion.first
        val intercepto = regresion.second

        Log.d(TAG, "📊 Pendiente (m): $pendiente")
        Log.d(TAG, "📊 Intercepto (b): $intercepto")

        // Determinar estado de tendencia
        val estado = determinarEstadoTendencia(pendiente)
        Log.d(TAG, "📊 Estado de tendencia: $estado")

        // Crear puntos históricos
        val historico = datosMensuales.mapIndexed { index, dato ->
            PuntoHistoricoDto(
                mes = dato.mesNombre,
                valor = dato.porcentajeCumplimiento,
                orden = index + 1,
                totalCasos = dato.totalCasos,
                cumplidos = dato.cumplidos,
                noCumplidos = dato.noCumplidos
            )
        }

        // Crear línea de tendencia (valores calculados con y = mx + b)
        val lineaTendencia = x.mapIndexed { index, xi ->
            val valorTendencia = (pendiente * xi + intercepto).coerceIn(0.0, 100.0)
            PuntoTendenciaDto(
                mes = datosMensuales[index].mesNombre,
                valor = valorTendencia,
                orden = index + 1
            )
        }

        // Calcular proyección para el siguiente mes (mes n+1)
        val xProyeccion = n + 1.0
        val proyeccion = (pendiente * xProyeccion + intercepto).coerceIn(0.0, 100.0)

        Log.d(TAG, "✅ Proyección para mes ${n + 1}: ${"%.2f".format(proyeccion)}%")
        Log.d(TAG, "✅ Ecuación de tendencia: y = ${"%.4f".format(pendiente)}x + ${"%.2f".format(intercepto)}")

        return TendenciaCalculadaLocal(
            historico = historico,
            lineaTendencia = lineaTendencia,
            proyeccion = proyeccion,
            pendiente = pendiente,
            intercepto = intercepto,
            estadoTendencia = estado
        )
    }

    /**
     * Calcula regresión lineal usando método de mínimos cuadrados
     * Fórmulas:
     *   m = (n * Σ(xy) - Σ(x) * Σ(y)) / (n * Σ(x²) - (Σ(x))²)
     *   b = (Σ(y) - m * Σ(x)) / n
     *
     * @param x Lista de valores independientes (meses)
     * @param y Lista de valores dependientes (porcentajes)
     * @return Par (pendiente, intercepto)
     */
    private fun calcularRegresionLineal(x: List<Double>, y: List<Double>): Pair<Double, Double> {
        val n = x.size.toDouble()

        val sumX = x.sum()
        val sumY = y.sum()
        val sumXY = x.zip(y).sumOf { (xi, yi) -> xi * yi }
        val sumX2 = x.sumOf { it * it }

        // Calcular pendiente (m)
        val pendiente = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)

        // Calcular intercepto (b)
        val intercepto = (sumY - pendiente * sumX) / n

        return Pair(pendiente, intercepto)
    }

    /**
     * Determina el estado de la tendencia basándose en la pendiente
     *
     * @param pendiente Pendiente de la recta de regresión
     * @return Estado de tendencia
     */
    private fun determinarEstadoTendencia(pendiente: Double): EstadoTendencia {
        return when {
            pendiente > UMBRAL_PENDIENTE -> {
                Log.d(TAG, "📈 Tendencia MEJORANDO (pendiente: ${"%.4f".format(pendiente)} > $UMBRAL_PENDIENTE)")
                EstadoTendencia.MEJORANDO
            }
            pendiente < -UMBRAL_PENDIENTE -> {
                Log.d(TAG, "📉 Tendencia EMPEORANDO (pendiente: ${"%.4f".format(pendiente)} < -$UMBRAL_PENDIENTE)")
                EstadoTendencia.EMPEORANDO
            }
            else -> {
                Log.d(TAG, "➡️ Tendencia ESTABLE (pendiente: ${"%.4f".format(pendiente)} entre -$UMBRAL_PENDIENTE y $UMBRAL_PENDIENTE)")
                EstadoTendencia.ESTABLE
            }
        }
    }

    /**
     * Calcula coeficiente de determinación R² para medir bondad de ajuste
     * R² cercano a 1 indica buen ajuste, cercano a 0 indica mal ajuste
     *
     * @param yReales Valores reales
     * @param yPredichos Valores predichos por el modelo
     * @return Valor R² entre 0 y 1
     */
    fun calcularR2(yReales: List<Double>, yPredichos: List<Double>): Double {
        if (yReales.size != yPredichos.size || yReales.isEmpty()) return 0.0

        val promedio = yReales.average()

        val ssTotal = yReales.sumOf { (it - promedio) * (it - promedio) }
        val ssResidual = yReales.zip(yPredichos).sumOf { (real, predicho) ->
            (real - predicho) * (real - predicho)
        }

        return if (ssTotal == 0.0) 0.0 else 1 - (ssResidual / ssTotal)
    }
}

