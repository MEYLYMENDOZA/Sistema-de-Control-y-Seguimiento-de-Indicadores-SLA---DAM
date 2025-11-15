package com.example.proyecto1.domain.math

class LinearRegression(
    private val x: DoubleArray,
    private val y: DoubleArray
) {
    var slope: Double = 0.0
        private set
    var intercept: Double = 0.0
        private set

    init {
        computeRegression()
    }

    private fun computeRegression() {
        val n = x.size
        val avgX = x.average()
        val avgY = y.average()

        var num = 0.0
        var den = 0.0

        for (i in 0 until n) {
            val dx = x[i] - avgX
            val dy = y[i] - avgY
            num += dx * dy
            den += dx * dx
        }

        slope = if (den != 0.0) num / den else 0.0
        intercept = avgY - slope * avgX
    }

    fun predict(nextX: Double): Double = slope * nextX + intercept
}
