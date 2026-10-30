package com.example.math

import kotlin.math.*

class ExpressionEvaluator(
    var angleUnit: AngleUnit = AngleUnit.DEGREES
) {
    companion object {
        const val PI_VAL = kotlin.math.PI
        const val E_VAL = kotlin.math.E
        const val PHI_VAL = 1.618033988749895
        const val C_VAL = 299792458.0 // m/s
    }

    /**
     * Evaluates expression with an optional variable 'x' (useful for graphing f(x)).
     */
    fun evaluate(expression: String, xValue: Double = 0.0): Double {
        val sanitized = sanitize(expression)
        if (sanitized.isBlank()) return 0.0
        val parser = Parser(sanitized, angleUnit, xValue)
        return parser.parse()
    }

    /**
     * Sanitizes raw user input into standard tokens.
     */
    fun sanitize(raw: String): String {
        return raw
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("φ", "phi")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("cot⁻¹", "acot")
            .replace("sec⁻¹", "asec")
            .replace("csc⁻¹", "acsc")
            .replace("sinh⁻¹", "asinh")
            .replace("cosh⁻¹", "acosh")
            .replace("tanh⁻¹", "atanh")
            .replace("coth⁻¹", "acoth")
            .replace("sech⁻¹", "asech")
            .replace("csch⁻¹", "acsch")
            .trim()
    }

    private class Parser(
        private val src: String,
        private val angleUnit: AngleUnit,
        private val xVal: Double
    ) {
        private var pos = 0
        private val len = src.length

        fun parse(): Double {
            val result = parseExpression()
            skipWhitespace()
            if (pos < len) {
                throw IllegalArgumentException("Unexpected character at end: '${src[pos]}'")
            }
            return result
        }

        private fun skipWhitespace() {
            while (pos < len && src[pos].isWhitespace()) pos++
        }

        private fun peek(): Char = if (pos < len) src[pos] else '\u0000'

        private fun parseExpression(): Double {
            var left = parseTerm()
            while (true) {
                skipWhitespace()
                val c = peek()
                if (c == '+' || c == '-') {
                    pos++
                    val right = parseTerm()
                    left = if (c == '+') left + right else left - right
                } else {
                    break
                }
            }
            return left
        }

        private fun parseTerm(): Double {
            var left = parseFactorWithPostfix()
            while (true) {
                skipWhitespace()
                val c = peek()
                if (c == '*' || c == '/' || c == '%') {
                    pos++
                    val right = parseFactorWithPostfix()
                    left = when (c) {
                        '*' -> left * right
                        '/' -> {
                            if (abs(right) < 1e-15) throw ArithmeticException("Division by zero")
                            left / right
                        }
                        '%' -> left % right
                        else -> left
                    }
                } else if (isImplicitMultiplicationStart(c)) {
                    // Implicit multiplication, e.g. 2x, 2(3), (2)(3), 2sin(x)
                    val right = parseFactorWithPostfix()
                    left *= right
                } else {
                    break
                }
            }
            return left
        }

        private fun isImplicitMultiplicationStart(c: Char): Boolean {
            return c == '(' || c == 'x' || c == 'X' || c.isLetter()
        }

        private fun parseFactorWithPostfix(): Double {
            var base = parsePower()
            while (true) {
                skipWhitespace()
                if (peek() == '!') {
                    pos++
                    base = AdvancedTrig.factorial(base)
                } else {
                    break
                }
            }
            return base
        }

        private fun parsePower(): Double {
            val left = parseUnary()
            skipWhitespace()
            if (peek() == '^') {
                pos++
                // Exponentiation is right-associative: a^b^c = a^(b^c)
                val right = parseFactorWithPostfix()
                return left.pow(right)
            }
            return left
        }

        private fun parseUnary(): Double {
            skipWhitespace()
            val c = peek()
            if (c == '+') {
                pos++
                return parseUnary()
            }
            if (c == '-') {
                pos++
                return -parseUnary()
            }
            return parsePrimary()
        }

        private fun parsePrimary(): Double {
            skipWhitespace()
            val c = peek()

            if (c == '(') {
                pos++
                val value = parseExpression()
                skipWhitespace()
                if (peek() == ')') {
                    pos++
                } else {
                    // Tolerant parser auto-closes missing trailing parenthesis
                }
                return value
            }

            if (c == '|') {
                pos++
                val value = parseExpression()
                skipWhitespace()
                if (peek() == '|') pos++
                return abs(value)
            }

            if (c.isDigit() || c == '.') {
                return parseNumber()
            }

            if (c.isLetter()) {
                val ident = parseIdentifier()
                return resolveIdentifierOrFunction(ident)
            }

            if (c == '\u0000') {
                return 0.0
            }

            throw IllegalArgumentException("Unexpected character: '$c'")
        }

        private fun parseNumber(): Double {
            val start = pos
            while (pos < len && (src[pos].isDigit() || src[pos] == '.')) {
                pos++
            }
            // Handle scientific notation e.g. 1.2e5 or 1.2e-3
            if (pos < len && (src[pos] == 'e' || src[pos] == 'E')) {
                val nextPos = pos + 1
                if (nextPos < len && (src[nextPos] == '+' || src[nextPos] == '-' || src[nextPos].isDigit())) {
                    pos = nextPos
                    if (src[pos] == '+' || src[pos] == '-') pos++
                    while (pos < len && src[pos].isDigit()) pos++
                }
            }
            val numStr = src.substring(start, pos)
            return numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
        }

        private fun parseIdentifier(): String {
            val start = pos
            while (pos < len && (src[pos].isLetter() || src[pos].isDigit())) {
                pos++
            }
            return src.substring(start, pos).lowercase()
        }

        private fun resolveIdentifierOrFunction(name: String): Double {
            // Check variable
            if (name == "x") return xVal
            // Check constants
            when (name) {
                "pi" -> return PI_VAL
                "e" -> return E_VAL
                "phi" -> return PHI_VAL
                "c" -> return C_VAL
            }

            // Function call with argument e.g. sin(...) or sin 30
            skipWhitespace()
            val arg: Double
            if (peek() == '(') {
                pos++
                arg = parseExpression()
                skipWhitespace()
                if (peek() == ')') pos++
            } else {
                arg = parseFactorWithPostfix()
            }

            return when (name) {
                "sin" -> AdvancedTrig.sin(arg, angleUnit)
                "cos" -> AdvancedTrig.cos(arg, angleUnit)
                "tan" -> AdvancedTrig.tan(arg, angleUnit)
                "cot" -> AdvancedTrig.cot(arg, angleUnit)
                "sec" -> AdvancedTrig.sec(arg, angleUnit)
                "csc" -> AdvancedTrig.csc(arg, angleUnit)

                "asin" -> AdvancedTrig.asin(arg, angleUnit)
                "acos" -> AdvancedTrig.acos(arg, angleUnit)
                "atan" -> AdvancedTrig.atan(arg, angleUnit)
                "acot" -> AdvancedTrig.acot(arg, angleUnit)
                "asec" -> AdvancedTrig.asec(arg, angleUnit)
                "acsc" -> AdvancedTrig.acsc(arg, angleUnit)

                "sinh" -> AdvancedTrig.sinh(arg)
                "cosh" -> AdvancedTrig.cosh(arg)
                "tanh" -> AdvancedTrig.tanh(arg)
                "coth" -> AdvancedTrig.coth(arg)
                "sech" -> AdvancedTrig.sech(arg)
                "csch" -> AdvancedTrig.csch(arg)

                "asinh" -> AdvancedTrig.asinh(arg)
                "acosh" -> AdvancedTrig.acosh(arg)
                "atanh" -> AdvancedTrig.atanh(arg)
                "acoth" -> AdvancedTrig.acoth(arg)
                "asech" -> AdvancedTrig.asech(arg)
                "acsch" -> AdvancedTrig.acsch(arg)

                "sqrt" -> {
                    if (arg < 0.0) throw ArithmeticException("Domain error: sqrt of negative")
                    sqrt(arg)
                }
                "cbrt" -> cbrt(arg)
                "ln" -> {
                    if (arg <= 0.0) throw ArithmeticException("Domain error: ln(x) requires x > 0")
                    ln(arg)
                }
                "log" -> {
                    if (arg <= 0.0) throw ArithmeticException("Domain error: log10(x) requires x > 0")
                    log10(arg)
                }
                "log2" -> {
                    if (arg <= 0.0) throw ArithmeticException("Domain error: log2(x) requires x > 0")
                    log2(arg)
                }
                "abs" -> abs(arg)
                "exp" -> exp(arg)
                "floor" -> floor(arg)
                "ceil" -> ceil(arg)
                "round" -> round(arg)
                "fact" -> AdvancedTrig.factorial(arg)
                else -> throw IllegalArgumentException("Unknown function: $name")
            }
        }
    }
}
