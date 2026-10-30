package com.example.math

import kotlin.math.*

object AdvancedTrig {

    // Standard & Reciprocal Trigonometric Functions
    fun sin(x: Double, angleUnit: AngleUnit): Double {
        val rad = angleUnit.toRadians(x)
        // Clean up near-zero values like sin(180°)
        val res = kotlin.math.sin(rad)
        return if (abs(res) < 1e-15) 0.0 else res
    }

    fun cos(x: Double, angleUnit: AngleUnit): Double {
        val rad = angleUnit.toRadians(x)
        val res = kotlin.math.cos(rad)
        return if (abs(res) < 1e-15) 0.0 else res
    }

    fun tan(x: Double, angleUnit: AngleUnit): Double {
        val rad = angleUnit.toRadians(x)
        val cosVal = kotlin.math.cos(rad)
        if (abs(cosVal) < 1e-15) {
            throw ArithmeticException("Tangent undefined at asymptote")
        }
        val res = kotlin.math.tan(rad)
        return if (abs(res) < 1e-15) 0.0 else res
    }

    fun cot(x: Double, angleUnit: AngleUnit): Double {
        val t = tan(x, angleUnit)
        if (abs(t) < 1e-15) throw ArithmeticException("Cotangent undefined (division by zero)")
        return 1.0 / t
    }

    fun sec(x: Double, angleUnit: AngleUnit): Double {
        val c = cos(x, angleUnit)
        if (abs(c) < 1e-15) throw ArithmeticException("Secant undefined (division by zero)")
        return 1.0 / c
    }

    fun csc(x: Double, angleUnit: AngleUnit): Double {
        val s = sin(x, angleUnit)
        if (abs(s) < 1e-15) throw ArithmeticException("Cosecant undefined (division by zero)")
        return 1.0 / s
    }

    // Inverse Trigonometric Functions
    fun asin(x: Double, angleUnit: AngleUnit): Double {
        if (x < -1.0 || x > 1.0) throw ArithmeticException("Domain error: asin argument must be in [-1, 1]")
        return angleUnit.fromRadians(kotlin.math.asin(x))
    }

    fun acos(x: Double, angleUnit: AngleUnit): Double {
        if (x < -1.0 || x > 1.0) throw ArithmeticException("Domain error: acos argument must be in [-1, 1]")
        return angleUnit.fromRadians(kotlin.math.acos(x))
    }

    fun atan(x: Double, angleUnit: AngleUnit): Double {
        return angleUnit.fromRadians(kotlin.math.atan(x))
    }

    fun acot(x: Double, angleUnit: AngleUnit): Double {
        if (abs(x) < 1e-15) return angleUnit.fromRadians(PI / 2.0)
        return angleUnit.fromRadians(kotlin.math.atan(1.0 / x))
    }

    fun asec(x: Double, angleUnit: AngleUnit): Double {
        if (abs(x) < 1.0) throw ArithmeticException("Domain error: |asec(x)| requires |x| >= 1")
        return angleUnit.fromRadians(kotlin.math.acos(1.0 / x))
    }

    fun acsc(x: Double, angleUnit: AngleUnit): Double {
        if (abs(x) < 1.0) throw ArithmeticException("Domain error: |acsc(x)| requires |x| >= 1")
        return angleUnit.fromRadians(kotlin.math.asin(1.0 / x))
    }

    // Hyperbolic Functions (always in hyperbolic radians)
    fun sinh(x: Double): Double = kotlin.math.sinh(x)
    fun cosh(x: Double): Double = kotlin.math.cosh(x)
    fun tanh(x: Double): Double = kotlin.math.tanh(x)

    fun coth(x: Double): Double {
        if (abs(x) < 1e-15) throw ArithmeticException("Domain error: coth(0) undefined")
        return 1.0 / kotlin.math.tanh(x)
    }

    fun sech(x: Double): Double = 1.0 / kotlin.math.cosh(x)

    fun csch(x: Double): Double {
        if (abs(x) < 1e-15) throw ArithmeticException("Domain error: csch(0) undefined")
        return 1.0 / kotlin.math.sinh(x)
    }

    // Inverse Hyperbolic Functions
    fun asinh(x: Double): Double = ln(x + sqrt(x * x + 1.0))

    fun acosh(x: Double): Double {
        if (x < 1.0) throw ArithmeticException("Domain error: acosh(x) requires x >= 1")
        return ln(x + sqrt(x * x - 1.0))
    }

    fun atanh(x: Double): Double {
        if (abs(x) >= 1.0) throw ArithmeticException("Domain error: atanh(x) requires |x| < 1")
        return 0.5 * ln((1.0 + x) / (1.0 - x))
    }

    fun acoth(x: Double): Double {
        if (abs(x) <= 1.0) throw ArithmeticException("Domain error: acoth(x) requires |x| > 1")
        return 0.5 * ln((x + 1.0) / (x - 1.0))
    }

    fun asech(x: Double): Double {
        if (x <= 0.0 || x > 1.0) throw ArithmeticException("Domain error: asech(x) requires 0 < x <= 1")
        return ln((1.0 + sqrt(1.0 - x * x)) / x)
    }

    fun acsch(x: Double): Double {
        if (abs(x) < 1e-15) throw ArithmeticException("Domain error: acsch(0) undefined")
        return ln(1.0 / x + sqrt(1.0 / (x * x) + 1.0))
    }

    // Combinatorics and Gamma
    fun factorial(n: Double): Double {
        if (n < 0) throw ArithmeticException("Factorial undefined for negative numbers")
        if (n > 170.0) return Double.POSITIVE_INFINITY
        if (floor(n) == n) {
            val intN = n.toLong()
            var result = 1.0
            for (i in 2..intN) {
                result *= i
            }
            return result
        }
        // Lanczos Gamma approximation for non-integer inputs: n! = Gamma(n + 1)
        return gamma(n + 1.0)
    }

    // Lanczos Gamma Approximation
    fun gamma(z: Double): Double {
        if (z < 0.5) {
            return PI / (kotlin.math.sin(PI * z) * gamma(1.0 - z))
        }
        val p = doubleArrayOf(
            676.5203681218851, -1259.1392167224028,
            771.32342877765313, -176.61502916214059,
            12.507343278686905, -0.13857109583115912,
            9.9843695780195716e-6, 1.5056327351493116e-7
        )
        val g = 7.0
        var x = 0.99999999999980993
        val zm1 = z - 1.0
        for (i in p.indices) {
            x += p[i] / (zm1 + (i + 1))
        }
        val t = zm1 + g + 0.5
        return sqrt(2.0 * PI) * t.pow(zm1 + 0.5) * exp(-t) * x
    }

    fun nPr(n: Double, r: Double): Double {
        if (n < 0 || r < 0 || r > n) throw ArithmeticException("Invalid nPr arguments")
        return factorial(n) / factorial(n - r)
    }

    fun nCr(n: Double, r: Double): Double {
        if (n < 0 || r < 0 || r > n) throw ArithmeticException("Invalid nCr arguments")
        return factorial(n) / (factorial(r) * factorial(n - r))
    }
}
