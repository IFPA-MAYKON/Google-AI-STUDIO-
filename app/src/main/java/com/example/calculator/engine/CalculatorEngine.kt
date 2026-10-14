package com.example.calculator.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

enum class AngleMode {
    DEG, RAD
}

sealed class EvalResult {
    data class Success(val value: Double, val formatted: String) : EvalResult()
    data class Error(val message: String) : EvalResult()
}

class CalculatorEngine {

    companion object {
        const val PI_VALUE = Math.PI
        const val E_VALUE = Math.E
        const val PHI_VALUE = 1.6180339887498948482
        private const val MAX_FACTORIAL = 170

        fun formatNumber(value: Double): String {
            if (value.isNaN()) return "Indefinido"
            if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

            // Fix tiny floating point residue (e.g. 0.1 + 0.2 = 0.30000000000000004 or sin(pi) = 1.22e-16)
            var cleanVal = value
            if (abs(cleanVal) < 1e-12) {
                cleanVal = 0.0
            }

            // If it's effectively an integer
            val rounded = round(cleanVal)
            if (abs(cleanVal - rounded) < 1e-11 && abs(cleanVal) < 1e14) {
                return rounded.toLong().toString()
            }

            // Scientific notation for very large or tiny numbers
            if (abs(cleanVal) >= 1e12 || (abs(cleanVal) > 0 && abs(cleanVal) < 1e-6)) {
                val symbols = DecimalFormatSymbols(Locale.US)
                val df = DecimalFormat("0.######E0", symbols)
                return df.format(cleanVal)
            }

            // High precision standard formatting up to 10 decimal places, trimming trailing zeros
            val symbols = DecimalFormatSymbols(Locale.US)
            val df = DecimalFormat("#,##0.##########", symbols)
            df.isGroupingUsed = false
            val formatted = df.format(cleanVal)
            return formatted
        }

        fun factorial(n: Double): Double {
            if (n < 0 || n != floor(n)) {
                throw ArithmeticException("Fatorial inválido para números negativos ou decimais")
            }
            val intN = n.toInt()
            if (intN > MAX_FACTORIAL) {
                throw ArithmeticException("Resultado excede o limite")
            }
            var res = 1.0
            for (i in 2..intN) {
                res *= i
            }
            return res
        }
    }

    /**
     * Evaluates a mathematical expression string given the current angle mode and last answer (Ans).
     */
    fun evaluate(expression: String, angleMode: AngleMode = AngleMode.DEG, ans: Double = 0.0): EvalResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) {
            return EvalResult.Success(0.0, "0")
        }

        return try {
            val sanitized = sanitize(trimmed, ans)
            val tokens = tokenize(sanitized)
            val parser = ExpressionParser(tokens, angleMode)
            val result = parser.parseExpression()

            if (result.isNaN()) {
                EvalResult.Error("Resultado indefinido")
            } else if (result.isInfinite()) {
                EvalResult.Error("Divisão por zero")
            } else {
                EvalResult.Success(result, formatNumber(result))
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Erro de cálculo")
        } catch (e: Exception) {
            EvalResult.Error("Expressão inválida")
        }
    }

    /**
     * Replaces visual symbols (×, ÷, π, √, etc.) with normalized parser tokens.
     */
    private fun sanitize(expr: String, ans: Double): String {
        var s = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "PI")
            .replace("e", "EULER")
            .replace("φ", "PHI")
            .replace("Ans", "($ans)")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("log₁₀", "log10")
            .replace("log₂", "log2")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")

        // Handle implicit multiplication:
        // e.g. "5(2)" -> "5*(2)", ")(" -> ")*(", "2PI" -> "2*PI", ")sqrt" -> ")*sqrt"
        s = s.replace(Regex("(\\d)\\("), "$1*(")
        s = s.replace(Regex("\\)(\\d)"), ")*$1")
        s = s.replace(Regex("\\)\\("), ")*(")
        s = s.replace(Regex("(\\d)(PI|EULER|PHI|sqrt|cbrt|sin|cos|tan|asin|acos|atan|sinh|cosh|tanh|ln|log)"), "$1*$2")
        s = s.replace(Regex("\\)(PI|EULER|PHI|sqrt|cbrt|sin|cos|tan|asin|acos|atan|sinh|cosh|tanh|ln|log)"), ")*$1")

        return s
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val c = expr[i]
            when {
                c.isWhitespace() -> {
                    i++
                }
                c in "+-*/^%(),!" -> {
                    tokens.add(c.toString())
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < len && (expr[i].isLetter() || expr[i].isDigit())) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                else -> {
                    i++
                }
            }
        }
        return tokens
    }
}

/**
 * Recursive descent parser with operator precedence.
 */
private class ExpressionParser(
    private val tokens: List<String>,
    private val angleMode: AngleMode
) {
    private var pos = 0

    private fun peek(): String? = if (pos < tokens.size) tokens[pos] else null
    private fun consume(): String = tokens[pos++]

    fun parseExpression(): Double {
        val result = parseAddition()
        if (pos < tokens.size) {
            throw IllegalArgumentException("Sintaxe inesperada próximo de '${tokens[pos]}'")
        }
        return result
    }

    // Addition & Subtraction
    private fun parseAddition(): Double {
        var left = parseMultiplication()
        while (peek() == "+" || peek() == "-") {
            val op = consume()
            val right = parseMultiplication()
            left = if (op == "+") left + right else left - right
        }
        return left
    }

    // Multiplication & Division & Percentage
    private fun parseMultiplication(): Double {
        var left = parsePower()
        while (peek() == "*" || peek() == "/" || peek() == "%") {
            val op = consume()
            val right = parsePower()
            left = when (op) {
                "*" -> left * right
                "/" -> {
                    if (abs(right) < 1e-15) throw ArithmeticException("Divisão por zero")
                    left / right
                }
                "%" -> {
                    // Modulo or percentage calculation
                    left * (right / 100.0)
                }
                else -> left
            }
        }
        return left
    }

    // Power (exponentiation ^)
    private fun parsePower(): Double {
        var left = parsePostfix()
        if (peek() == "^") {
            consume()
            val right = parsePower() // right-associative
            left = left.pow(right)
        }
        return left
    }

    // Postfix operators: factorial (!) and percentage postfix (%)
    private fun parsePostfix(): Double {
        var left = parseUnary()
        while (peek() == "!" || peek() == "%") {
            val op = consume()
            left = if (op == "!") {
                CalculatorEngine.factorial(left)
            } else {
                left / 100.0
            }
        }
        return left
    }

    // Unary plus and minus
    private fun parseUnary(): Double {
        if (peek() == "+") {
            consume()
            return parseUnary()
        }
        if (peek() == "-") {
            consume()
            return -parseUnary()
        }
        return parsePrimary()
    }

    // Primary: numbers, constants, functions, parenthesized expressions
    private fun parsePrimary(): Double {
        val token = peek() ?: throw IllegalArgumentException("Fim inesperado da expressão")

        // Parentheses
        if (token == "(") {
            consume()
            val value = parseAddition()
            if (peek() == ")") {
                consume()
            }
            return value
        }

        // Numbers
        token.toDoubleOrNull()?.let {
            consume()
            return it
        }

        // Named Constants
        when (token) {
            "PI" -> {
                consume()
                return CalculatorEngine.PI_VALUE
            }
            "EULER" -> {
                consume()
                return CalculatorEngine.E_VALUE
            }
            "PHI" -> {
                consume()
                return CalculatorEngine.PHI_VALUE
            }
        }

        // Functions
        consume()
        // Function argument can be in parentheses e.g. sin(45) or primary e.g. sin 45
        val arg = if (peek() == "(") {
            consume()
            val v = parseAddition()
            if (peek() == ")") consume()
            v
        } else {
            parseUnary()
        }

        return when (token.lowercase()) {
            "sin" -> {
                val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                val sinVal = sin(rad)
                if (abs(sinVal) < 1e-14) 0.0 else sinVal
            }
            "cos" -> {
                val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                val cosVal = cos(rad)
                if (abs(cosVal) < 1e-14) 0.0 else cosVal
            }
            "tan" -> {
                val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                val cosVal = cos(rad)
                if (abs(cosVal) < 1e-14) throw ArithmeticException("Tangente indefinida")
                val tanVal = tan(rad)
                if (abs(tanVal) < 1e-14) 0.0 else tanVal
            }
            "asin" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domínio inválido para asin")
                val rad = asin(arg)
                if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
            }
            "acos" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domínio inválido para acos")
                val rad = acos(arg)
                if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
            }
            "atan" -> {
                val rad = atan(arg)
                if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
            }
            "sinh" -> sinh(arg)
            "cosh" -> cosh(arg)
            "tanh" -> tanh(arg)
            "ln" -> {
                if (arg <= 0) throw ArithmeticException("Logaritmo de número não positivo")
                ln(arg)
            }
            "log", "log10" -> {
                if (arg <= 0) throw ArithmeticException("Logaritmo de número não positivo")
                log10(arg)
            }
            "log2" -> {
                if (arg <= 0) throw ArithmeticException("Logaritmo de número não positivo")
                log2(arg)
            }
            "sqrt" -> {
                if (arg < 0) throw ArithmeticException("Raiz quadrada de número negativo")
                sqrt(arg)
            }
            "cbrt" -> cbrt(arg)
            "abs" -> abs(arg)
            "exp" -> exp(arg)
            else -> throw IllegalArgumentException("Função desconhecida: $token")
        }
    }
}
