package com.example.calculator

import com.example.math.AngleUnit

data class CalculationItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val expression: String,
    val result: String,
    val fraction: String? = null,
    val angleUnit: AngleUnit,
    val timestamp: Long = System.currentTimeMillis()
)
