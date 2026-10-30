package com.example.math

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

object MathFormatter {

    private val symbols = DecimalFormatSymbols(Locale.US)
    private val standardFormat = DecimalFormat("#,##0.##########", symbols)
    private val sciFormat = DecimalFormat("0.######E0", symbols)

    fun format(value: Double): String {
        if (value.isNaN()) return "Undefined"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

        val absVal = abs(value)
        if (absVal < 1e-15) return "0"

        // Check if value is very close to an integer
        val rounded = value.roundToLong()
        if (abs(value - rounded) < 1e-11 && absVal < 1e14) {
            return standardFormat.format(rounded)
        }

        // Use scientific notation for extremely large or tiny numbers
        if (absVal >= 1e11 || absVal <= 1e-6) {
            val formatted = sciFormat.format(value)
            return formatted.replace("E", " × 10^")
        }

        return standardFormat.format(value)
    }

    /**
     * Attempts to find a close simple fractional representation (e.g. 0.33333333 -> 1/3, 0.75 -> 3/4)
     */
    fun toFractionOrNull(value: Double, maxDenominator: Long = 1000): String? {
        if (value.isNaN() || value.isInfinite()) return null
        val absVal = abs(value)
        if (absVal < 1e-9) return "0"

        var bestNumerator = 1L
        var bestDenominator = 1L
        var bestError = Double.MAX_VALUE

        for (d in 1L..maxDenominator) {
            val n = (absVal * d).roundToLong()
            val error = abs(absVal - n.toDouble() / d.toDouble())
            if (error < bestError) {
                bestError = error
                bestNumerator = n
                bestDenominator = d
                if (error < 1e-8) break
            }
        }

        if (bestError < 1e-6 && bestDenominator > 1) {
            val sign = if (value < 0) "-" else ""
            return "$sign$bestNumerator/$bestDenominator"
        }
        return null
    }
}
