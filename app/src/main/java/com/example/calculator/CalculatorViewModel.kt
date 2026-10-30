package com.example.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.math.AdvancedTrig
import com.example.math.AngleUnit
import com.example.math.ExpressionEvaluator
import com.example.math.MathFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel : ViewModel() {

    private val evaluator = ExpressionEvaluator(AngleUnit.DEGREES)

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _previewResult = MutableStateFlow("")
    val previewResult: StateFlow<String> = _previewResult.asStateFlow()

    private val _fractionResult = MutableStateFlow<String?>(null)
    val fractionResult: StateFlow<String?> = _fractionResult.asStateFlow()

    private val _angleUnit = MutableStateFlow(AngleUnit.DEGREES)
    val angleUnit: StateFlow<AngleUnit> = _angleUnit.asStateFlow()

    private val _isInverseMode = MutableStateFlow(false)
    val isInverseMode: StateFlow<Boolean> = _isInverseMode.asStateFlow()

    private val _isHyperbolicMode = MutableStateFlow(false)
    val isHyperbolicMode: StateFlow<Boolean> = _isHyperbolicMode.asStateFlow()

    private val _memoryValue = MutableStateFlow<Double?>(null)
    val memoryValue: StateFlow<Double?> = _memoryValue.asStateFlow()

    private val _history = MutableStateFlow<List<CalculationItem>>(emptyList())
    val history: StateFlow<List<CalculationItem>> = _history.asStateFlow()

    private var justEvaluated = false

    fun toggleAngleUnit() {
        val next = when (_angleUnit.value) {
            AngleUnit.DEGREES -> AngleUnit.RADIANS
            AngleUnit.RADIANS -> AngleUnit.GRADIANS
            AngleUnit.GRADIANS -> AngleUnit.DEGREES
        }
        _angleUnit.value = next
        evaluator.angleUnit = next
        updatePreview()
    }

    fun setAngleUnit(unit: AngleUnit) {
        _angleUnit.value = unit
        evaluator.angleUnit = unit
        updatePreview()
    }

    fun toggleInverse() {
        _isInverseMode.update { !it }
    }

    fun toggleHyperbolic() {
        _isHyperbolicMode.update { !it }
    }

    fun onInput(token: String) {
        if (justEvaluated) {
            // If the user inputs an operator right after evaluation, continue with the previous result
            if (isOperator(token)) {
                _expression.value = _result.value + token
            } else {
                _expression.value = token
            }
            justEvaluated = false
            _result.value = ""
            _fractionResult.value = null
        } else {
            _expression.update { it + token }
        }
        updatePreview()
    }

    fun onFunctionInput(funcName: String) {
        val toInsert = "$funcName("
        if (justEvaluated) {
            _expression.value = toInsert
            justEvaluated = false
            _result.value = ""
        } else {
            _expression.update { it + toInsert }
        }
        updatePreview()
    }

    fun onDelete() {
        if (justEvaluated) {
            _result.value = ""
            justEvaluated = false
            return
        }
        _expression.update {
            if (it.isNotEmpty()) it.dropLast(1) else ""
        }
        updatePreview()
    }

    fun onClear() {
        _expression.value = ""
        _previewResult.value = ""
        _result.value = ""
        _fractionResult.value = null
        justEvaluated = false
    }

    fun onEvaluate() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) return

        try {
            val eval = evaluator.evaluate(expr)
            val formatted = MathFormatter.format(eval)
            val fraction = MathFormatter.toFractionOrNull(eval)

            _result.value = formatted
            _fractionResult.value = fraction
            _previewResult.value = ""
            justEvaluated = true

            // Add to history
            val item = CalculationItem(
                expression = expr,
                result = formatted,
                fraction = fraction,
                angleUnit = _angleUnit.value
            )
            _history.update { listOf(item) + it.take(49) }
        } catch (e: Exception) {
            _result.value = "Error: ${e.message ?: "Invalid Expression"}"
            _previewResult.value = ""
        }
    }

    private fun updatePreview() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) {
            _previewResult.value = ""
            return
        }
        try {
            val eval = evaluator.evaluate(expr)
            _previewResult.value = MathFormatter.format(eval)
        } catch (_: Exception) {
            _previewResult.value = ""
        }
    }

    private fun isOperator(token: String): Boolean {
        return token in listOf("+", "-", "×", "÷", "^", "%", "*", "/")
    }

    // Memory operations
    fun memoryClear() {
        _memoryValue.value = null
    }

    fun memoryRecall() {
        _memoryValue.value?.let { mem ->
            val formatted = MathFormatter.format(mem)
            onInput(formatted)
        }
    }

    fun memoryStore() {
        val currentVal = getCurrentNumericValue()
        if (currentVal != null) {
            _memoryValue.value = currentVal
        }
    }

    fun memoryAdd() {
        val currentVal = getCurrentNumericValue() ?: return
        val currentMem = _memoryValue.value ?: 0.0
        _memoryValue.value = currentMem + currentVal
    }

    fun memorySub() {
        val currentVal = getCurrentNumericValue() ?: return
        val currentMem = _memoryValue.value ?: 0.0
        _memoryValue.value = currentMem - currentVal
    }

    private fun getCurrentNumericValue(): Double? {
        val candidate = _result.value.ifBlank { _previewResult.value }.replace(",", "")
        return candidate.toDoubleOrNull() ?: try {
            evaluator.evaluate(_expression.value)
        } catch (_: Exception) {
            null
        }
    }

    fun selectHistoryItem(item: CalculationItem) {
        _expression.value = item.expression
        _result.value = item.result
        _fractionResult.value = item.fraction
        _angleUnit.value = item.angleUnit
        evaluator.angleUnit = item.angleUnit
        justEvaluated = true
    }

    fun clearHistory() {
        _history.value = emptyList()
    }
}
