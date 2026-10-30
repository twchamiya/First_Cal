package com.example.graph

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.math.AngleUnit
import com.example.math.ExpressionEvaluator
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldBright
import com.example.ui.theme.RosePink
import com.example.ui.theme.VioletElectric
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class GraphViewModel : ViewModel() {

    private val evaluator = ExpressionEvaluator(AngleUnit.RADIANS) // Standard for mathematical graphing

    private val availableColors = listOf(
        CyanNeon,
        EmeraldBright,
        VioletElectric,
        RosePink,
        BlueAccent
    )

    private val _functions = MutableStateFlow<List<GraphFunction>>(
        listOf(
            GraphFunction(equation = "sin(x)", color = CyanNeon, isVisible = true),
            GraphFunction(equation = "0.5*x^2 - 3", color = EmeraldBright, isVisible = true)
        )
    )
    val functions: StateFlow<List<GraphFunction>> = _functions.asStateFlow()

    // Viewport bounds
    private val _xMin = MutableStateFlow(-10.0)
    val xMin: StateFlow<Double> = _xMin.asStateFlow()

    private val _xMax = MutableStateFlow(10.0)
    val xMax: StateFlow<Double> = _xMax.asStateFlow()

    private val _yMin = MutableStateFlow(-8.0)
    val yMin: StateFlow<Double> = _yMin.asStateFlow()

    private val _yMax = MutableStateFlow(8.0)
    val yMax: StateFlow<Double> = _yMax.asStateFlow()

    // Trace cursor
    private val _traceX = MutableStateFlow<Double?>(null)
    val traceX: StateFlow<Double?> = _traceX.asStateFlow()

    // Table of values state
    private val _tablePoints = MutableStateFlow<List<ValuePoint>>(emptyList())
    val tablePoints: StateFlow<List<ValuePoint>> = _tablePoints.asStateFlow()

    private val _selectedFunctionForTable = MutableStateFlow<GraphFunction?>(null)
    val selectedFunctionForTable: StateFlow<GraphFunction?> = _selectedFunctionForTable.asStateFlow()

    init {
        validateAllFunctions()
    }

    fun addFunction(equation: String = "") {
        val colorIndex = _functions.value.size % availableColors.size
        val newFunc = GraphFunction(
            equation = equation,
            color = availableColors[colorIndex],
            isVisible = true
        )
        _functions.update { it + newFunc }
        validateAllFunctions()
    }

    fun updateFunctionEquation(id: String, newEquation: String) {
        _functions.update { list ->
            list.map { fn ->
                if (fn.id == id) {
                    val (valid, error) = validateEquation(newEquation)
                    fn.copy(equation = newEquation, isValid = valid, errorMessage = error)
                } else fn
            }
        }
    }

    fun toggleFunctionVisibility(id: String) {
        _functions.update { list ->
            list.map { fn ->
                if (fn.id == id) fn.copy(isVisible = !fn.isVisible) else fn
            }
        }
    }

    fun removeFunction(id: String) {
        _functions.update { it.filterNot { fn -> fn.id == id } }
    }

    fun setTraceX(x: Double?) {
        _traceX.value = x
    }

    fun pan(dxNorm: Float, dyNorm: Float) {
        val xSpan = _xMax.value - _xMin.value
        val ySpan = _yMax.value - _yMin.value
        val dxWorld = dxNorm * xSpan
        val dyWorld = dyNorm * ySpan

        _xMin.update { it - dxWorld }
        _xMax.update { it - dxWorld }
        _yMin.update { it + dyWorld }
        _yMax.update { it + dyWorld }
    }

    fun zoom(zoomFactor: Float, centerNormX: Float = 0.5f, centerNormY: Float = 0.5f) {
        if (zoomFactor <= 0f) return
        val xSpan = _xMax.value - _xMin.value
        val ySpan = _yMax.value - _yMin.value

        val newXSpan = xSpan / zoomFactor
        val newYSpan = ySpan / zoomFactor

        if (newXSpan < 1e-4 || newXSpan > 1e6 || newYSpan < 1e-4 || newYSpan > 1e6) return

        val centerX = _xMin.value + xSpan * centerNormX
        val centerY = _yMin.value + ySpan * (1f - centerNormY)

        _xMin.value = centerX - newXSpan * centerNormX
        _xMax.value = centerX + newXSpan * (1f - centerNormX)
        _yMin.value = centerY - newYSpan * (1f - centerNormY)
        _yMax.value = centerY + newYSpan * centerNormY
    }

    fun resetViewport() {
        _xMin.value = -10.0
        _xMax.value = 10.0
        _yMin.value = -8.0
        _yMax.value = 8.0
        _traceX.value = null
    }

    fun applyPresetTrig() {
        _xMin.value = -2 * PI
        _xMax.value = 2 * PI
        _yMin.value = -3.0
        _yMax.value = 3.0
        _traceX.value = null
    }

    fun applyTemplate(name: String) {
        val eq = when (name) {
            "Sine Wave" -> "sin(x)"
            "Cosine Wave" -> "cos(x)"
            "Tangent" -> "tan(x)"
            "Parabola" -> "x^2 - 4"
            "Damped Oscillation" -> "exp(-0.3*x)*sin(4*x)"
            "Gaussian Bell" -> "exp(-x^2)"
            "Sigmoid" -> "1 / (1 + exp(-x))"
            "Cubic Wave" -> "x^3 - 3*x"
            else -> "sin(x)"
        }
        addFunction(eq)
    }

    fun evaluateAt(equation: String, x: Double): Double? {
        return try {
            val y = evaluator.evaluate(equation, x)
            if (y.isNaN() || y.isInfinite()) null else y
        } catch (_: Exception) {
            null
        }
    }

    fun generateTable(fn: GraphFunction, startX: Double = -5.0, endX: Double = 5.0, step: Double = 0.5) {
        _selectedFunctionForTable.value = fn
        val points = mutableListOf<ValuePoint>()
        var curX = startX
        while (curX <= endX + 1e-6) {
            val y = evaluateAt(fn.equation, curX)
            points.add(ValuePoint(x = curX, y = y, isDefined = y != null))
            curX += step
        }
        _tablePoints.value = points
    }

    private fun validateAllFunctions() {
        _functions.update { list ->
            list.map { fn ->
                val (valid, error) = validateEquation(fn.equation)
                fn.copy(isValid = valid, errorMessage = error)
            }
        }
    }

    private fun validateEquation(equation: String): Pair<Boolean, String?> {
        if (equation.isBlank()) return Pair(false, "Equation cannot be empty")
        return try {
            evaluator.evaluate(equation, 1.0)
            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, e.message ?: "Invalid syntax")
        }
    }
}
