package com.example.math

import kotlin.math.PI

enum class AngleUnit(val label: String, val shortLabel: String) {
    DEGREES("Degrees", "DEG"),
    RADIANS("Radians", "RAD"),
    GRADIANS("Gradians", "GRAD");

    /**
     * Converts an angle in this unit to radians for Kotlin's standard trigonometric functions.
     */
    fun toRadians(angle: Double): Double {
        return when (this) {
            DEGREES -> angle * (PI / 180.0)
            RADIANS -> angle
            GRADIANS -> angle * (PI / 200.0)
        }
    }

    /**
     * Converts an angle from radians (as returned by asin, acos, atan, etc.) to this unit.
     */
    fun fromRadians(radians: Double): Double {
        return when (this) {
            DEGREES -> radians * (180.0 / PI)
            RADIANS -> radians
            GRADIANS -> radians * (200.0 / PI)
        }
    }
}
