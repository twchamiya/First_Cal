package com.example.trig

import androidx.lifecycle.ViewModel
import com.example.math.AdvancedTrig
import com.example.math.AngleUnit
import com.example.math.MathFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

data class TriangleSolution(
    val sideA: Double,
    val sideB: Double,
    val sideC: Double,
    val angleA: Double, // in degrees
    val angleB: Double,
    val angleC: Double,
    val area: Double,
    val perimeter: Double,
    val method: String,
    val error: String? = null
)

class TrigLabViewModel : ViewModel() {

    // Unit Circle state
    private val _angleDegrees = MutableStateFlow(45.0)
    val angleDegrees: StateFlow<Double> = _angleDegrees.asStateFlow()

    // Triangle Solver inputs
    val sideAInput = MutableStateFlow("3")
    val sideBInput = MutableStateFlow("4")
    val sideCInput = MutableStateFlow("5")
    val angleAInput = MutableStateFlow("")
    val angleBInput = MutableStateFlow("")
    val angleCInput = MutableStateFlow("")

    private val _triangleSolution = MutableStateFlow<TriangleSolution?>(null)
    val triangleSolution: StateFlow<TriangleSolution?> = _triangleSolution.asStateFlow()

    init {
        solveTriangle()
    }

    fun setAngleDegrees(degrees: Double) {
        val normalized = (degrees % 360.0 + 360.0) % 360.0
        _angleDegrees.value = normalized
    }

    fun getExactRadicalTrig(deg: Double): String? {
        val d = (deg.roundToInt() % 360 + 360) % 360
        return when (d) {
            0 -> "sin: 0, cos: 1, tan: 0"
            30 -> "sin: 1/2, cos: √3/2, tan: √3/3"
            45 -> "sin: √2/2, cos: √2/2, tan: 1"
            60 -> "sin: √3/2, cos: 1/2, tan: √3"
            90 -> "sin: 1, cos: 0, tan: undefined"
            120 -> "sin: √3/2, cos: -1/2, tan: -√3"
            135 -> "sin: √2/2, cos: -√2/2, tan: -1"
            150 -> "sin: 1/2, cos: -√3/2, tan: -√3/3"
            180 -> "sin: 0, cos: -1, tan: 0"
            210 -> "sin: -1/2, cos: -√3/2, tan: √3/3"
            225 -> "sin: -√2/2, cos: -√2/2, tan: 1"
            240 -> "sin: -√3/2, cos: -1/2, tan: √3"
            270 -> "sin: -1, cos: 0, tan: undefined"
            300 -> "sin: -√3/2, cos: 1/2, tan: -√3"
            315 -> "sin: -√2/2, cos: √2/2, tan: -1"
            330 -> "sin: -1/2, cos: √3/2, tan: -√3/3"
            else -> null
        }
    }

    fun solveTriangle() {
        val a = sideAInput.value.toDoubleOrNull()
        val b = sideBInput.value.toDoubleOrNull()
        val c = sideCInput.value.toDoubleOrNull()
        val A = angleAInput.value.toDoubleOrNull()
        val B = angleBInput.value.toDoubleOrNull()
        val C = angleCInput.value.toDoubleOrNull()

        try {
            // Case 1: Three sides (SSS)
            if (a != null && b != null && c != null) {
                if (a + b <= c || a + c <= b || b + c <= a) {
                    _triangleSolution.value = TriangleSolution(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "", "Triangle Inequality violated: sum of any two sides must exceed the third")
                    return
                }
                val cosA = (b * b + c * c - a * a) / (2.0 * b * c)
                val cosB = (a * a + c * c - b * b) / (2.0 * a * c)
                val angA = acos(cosA.coerceIn(-1.0, 1.0)) * 180.0 / PI
                val angB = acos(cosB.coerceIn(-1.0, 1.0)) * 180.0 / PI
                val angC = 180.0 - angA - angB
                val s = (a + b + c) / 2.0
                val area = sqrt(s * (s - a) * (s - b) * (s - c))

                _triangleSolution.value = TriangleSolution(
                    sideA = a, sideB = b, sideC = c,
                    angleA = angA, angleB = angB, angleC = angC,
                    area = area, perimeter = a + b + c,
                    method = "Law of Cosines (SSS)"
                )
                return
            }

            // Case 2: SAS (Two sides and included angle)
            if (a != null && b != null && C != null) {
                val radC = C * PI / 180.0
                val cCalc = sqrt(a * a + b * b - 2.0 * a * b * cos(radC))
                val cosA = (b * b + cCalc * cCalc - a * a) / (2.0 * b * cCalc)
                val angA = acos(cosA.coerceIn(-1.0, 1.0)) * 180.0 / PI
                val angB = 180.0 - angA - C
                val area = 0.5 * a * b * sin(radC)

                _triangleSolution.value = TriangleSolution(
                    sideA = a, sideB = b, sideC = cCalc,
                    angleA = angA, angleB = angB, angleC = C,
                    area = area, perimeter = a + b + cCalc,
                    method = "Law of Cosines (SAS)"
                )
                return
            }

            // Case 3: ASA or AAS (Two angles and one side)
            if (A != null && B != null && (a != null || c != null)) {
                val angC = 180.0 - A - B
                if (angC <= 0) {
                    _triangleSolution.value = TriangleSolution(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "", "Sum of angles must be < 180°")
                    return
                }
                val radA = A * PI / 180.0
                val radB = B * PI / 180.0
                val radC = angC * PI / 180.0

                val (sA, sB, sC) = if (a != null) {
                    val sB = a * sin(radB) / sin(radA)
                    val sC = a * sin(radC) / sin(radA)
                    Triple(a, sB, sC)
                } else {
                    val sA = c!! * sin(radA) / sin(radC)
                    val sB = c * sin(radB) / sin(radC)
                    Triple(sA, sB, c)
                }
                val area = 0.5 * sA * sB * sin(radC)

                _triangleSolution.value = TriangleSolution(
                    sideA = sA, sideB = sB, sideC = sC,
                    angleA = A, angleB = B, angleC = angC,
                    area = area, perimeter = sA + sB + sC,
                    method = "Law of Sines (ASA/AAS)"
                )
                return
            }

            _triangleSolution.value = null
        } catch (e: Exception) {
            _triangleSolution.value = TriangleSolution(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "", e.message)
        }
    }
}
