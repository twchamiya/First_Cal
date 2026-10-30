package com.example.converter

import androidx.lifecycle.ViewModel
import com.example.math.MathFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UnitConverterViewModel : ViewModel() {

    private val _selectedCategory = MutableStateFlow(UnitCategory.LENGTH)
    val selectedCategory: StateFlow<UnitCategory> = _selectedCategory.asStateFlow()

    private val _fromUnit = MutableStateFlow(UnitCategory.LENGTH.units[1]) // km
    val fromUnit: StateFlow<ConversionUnit> = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow(UnitCategory.LENGTH.units[7])   // mi
    val toUnit: StateFlow<ConversionUnit> = _toUnit.asStateFlow()

    private val _inputValue = MutableStateFlow("1")
    val inputValue: StateFlow<String> = _inputValue.asStateFlow()

    private val _outputValue = MutableStateFlow("")
    val outputValue: StateFlow<String> = _outputValue.asStateFlow()

    private val _formulaInfo = MutableStateFlow("")
    val formulaInfo: StateFlow<String> = _formulaInfo.asStateFlow()

    init {
        recalculate()
    }

    fun selectCategory(category: UnitCategory) {
        _selectedCategory.value = category
        _fromUnit.value = category.units[0]
        _toUnit.value = category.units.getOrElse(1) { category.units[0] }
        recalculate()
    }

    fun setFromUnit(unit: ConversionUnit) {
        _fromUnit.value = unit
        recalculate()
    }

    fun setToUnit(unit: ConversionUnit) {
        _toUnit.value = unit
        recalculate()
    }

    fun swapUnits() {
        val temp = _fromUnit.value
        _fromUnit.value = _toUnit.value
        _toUnit.value = temp
        recalculate()
    }

    fun onInput(char: String) {
        if (_inputValue.value == "0" && char != ".") {
            _inputValue.value = char
        } else if (char == "." && _inputValue.value.contains(".")) {
            return
        } else {
            _inputValue.update { it + char }
        }
        recalculate()
    }

    fun onDelete() {
        _inputValue.update {
            if (it.length > 1) it.dropLast(1) else "0"
        }
        recalculate()
    }

    fun onClear() {
        _inputValue.value = "0"
        recalculate()
    }

    private fun recalculate() {
        val inVal = _inputValue.value.toDoubleOrNull() ?: 0.0
        val category = _selectedCategory.value
        val from = _fromUnit.value
        val to = _toUnit.value

        val outVal = if (category == UnitCategory.TEMPERATURE) {
            convertTemperature(inVal, from.symbol, to.symbol)
        } else {
            // General linear conversion
            val baseVal = inVal * from.toBaseFactor
            baseVal / to.toBaseFactor
        }

        _outputValue.value = MathFormatter.format(outVal)

        // Generate formula preview string e.g. "1 km = 0.62137 mi"
        val sampleOut = if (category == UnitCategory.TEMPERATURE) {
            convertTemperature(1.0, from.symbol, to.symbol)
        } else {
            (1.0 * from.toBaseFactor) / to.toBaseFactor
        }
        _formulaInfo.value = "1 ${from.symbol} = ${MathFormatter.format(sampleOut)} ${to.symbol}"
    }

    private fun convertTemperature(value: Double, fromSymbol: String, toSymbol: String): Double {
        // Convert to Kelvin first
        val kelvin = when (fromSymbol) {
            "°C" -> value + 273.15
            "°F" -> (value - 32.0) * (5.0 / 9.0) + 273.15
            "K" -> value
            "°R" -> value * (5.0 / 9.0)
            else -> value
        }
        // Convert from Kelvin to target
        return when (toSymbol) {
            "°C" -> kelvin - 273.15
            "°F" -> (kelvin - 273.15) * (9.0 / 5.0) + 32.0
            "K" -> kelvin
            "°R" -> kelvin * (9.0 / 5.0)
            else -> kelvin
        }
    }
}
