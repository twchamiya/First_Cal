package com.example

import com.example.math.AdvancedTrig
import com.example.math.AngleUnit
import com.example.math.ExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class ExampleUnitTest {

    @Test
    fun testBasicArithmeticAndPrecedence() {
        val evaluator = ExpressionEvaluator(AngleUnit.DEGREES)
        assertEquals(14.0, evaluator.evaluate("2 + 3 * 4"), 1e-9)
        assertEquals(20.0, evaluator.evaluate("(2 + 3) * 4"), 1e-9)
        assertEquals(8.0, evaluator.evaluate("2^3"), 1e-9)
        assertEquals(512.0, evaluator.evaluate("2^3^2"), 1e-9)
        assertEquals(120.0, evaluator.evaluate("5!"), 1e-9)
    }

    @Test
    fun testImplicitMultiplicationAndConstants() {
        val evaluator = ExpressionEvaluator(AngleUnit.DEGREES)
        assertEquals(15.0, evaluator.evaluate("3(4 + 1)"), 1e-9)
        assertEquals(2 * PI, evaluator.evaluate("2π"), 1e-9)
        assertEquals(10.0, evaluator.evaluate("2(5)"), 1e-9)
    }

    @Test
    fun testTrigonometricOperationsInDegrees() {
        val evaluator = ExpressionEvaluator(AngleUnit.DEGREES)
        assertEquals(0.5, evaluator.evaluate("sin(30)"), 1e-9)
        assertEquals(0.5, evaluator.evaluate("cos(60)"), 1e-9)
        assertEquals(1.0, evaluator.evaluate("tan(45)"), 1e-9)
        assertEquals(30.0, evaluator.evaluate("asin(0.5)"), 1e-9)
        assertEquals(45.0, evaluator.evaluate("atan(1)"), 1e-9)
        assertEquals(1.0, evaluator.evaluate("sec(0)"), 1e-9)
        assertEquals(1.0, evaluator.evaluate("csc(90)"), 1e-9)
        assertEquals(1.0, evaluator.evaluate("cot(45)"), 1e-9)
    }

    @Test
    fun testHyperbolicFunctions() {
        val evaluator = ExpressionEvaluator(AngleUnit.RADIANS)
        assertEquals(0.0, evaluator.evaluate("sinh(0)"), 1e-9)
        assertEquals(1.0, evaluator.evaluate("cosh(0)"), 1e-9)
        assertEquals(0.0, evaluator.evaluate("tanh(0)"), 1e-9)
        assertEquals(0.0, evaluator.evaluate("asinh(0)"), 1e-9)
    }

    @Test
    fun testGraphVariableEvaluation() {
        val evaluator = ExpressionEvaluator(AngleUnit.RADIANS)
        assertEquals(5.0, evaluator.evaluate("x^2 - 4", xValue = 3.0), 1e-9)
        assertEquals(0.0, evaluator.evaluate("sin(x)", xValue = 0.0), 1e-9)
        assertEquals(1.0, evaluator.evaluate("sin(x)", xValue = PI / 2.0), 1e-9)
    }
}
