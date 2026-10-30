package com.example.graph

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class GraphFunction(
    val id: String = UUID.randomUUID().toString(),
    val equation: String,
    val color: Color,
    val isVisible: Boolean = true,
    val isValid: Boolean = true,
    val errorMessage: String? = null
)

data class ValuePoint(
    val x: Double,
    val y: Double?,
    val isDefined: Boolean
)
