package com.example

import com.example.calculator.engine.AngleMode
import com.example.calculator.engine.CalculatorEngine
import com.example.calculator.engine.EvalResult
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    private val engine = CalculatorEngine()

    @Test
    fun testBasicArithmetic() {
        val res = engine.evaluate("2 + 2")
        assertTrue(res is EvalResult.Success)
        assertEquals("4", (res as EvalResult.Success).formatted)

        val res2 = engine.evaluate("10 - 3 * 2")
        assertTrue(res2 is EvalResult.Success)
        assertEquals("4", (res2 as EvalResult.Success).formatted)
    }

    @Test
    fun testDivisionAndPrecedence() {
        val res = engine.evaluate("20 ÷ 4 + 5 × 2")
        assertTrue(res is EvalResult.Success)
        assertEquals("15", (res as EvalResult.Success).formatted)
    }

    @Test
    fun testParenthesesAndImplicitMultiplication() {
        val res = engine.evaluate("3(4 + 2)")
        assertTrue(res is EvalResult.Success)
        assertEquals("18", (res as EvalResult.Success).formatted)
    }

    @Test
    fun testPowerAndSqrt() {
        val res = engine.evaluate("2^3 + √(16)")
        assertTrue(res is EvalResult.Success)
        assertEquals("12", (res as EvalResult.Success).formatted)
    }

    @Test
    fun testFactorial() {
        val res = engine.evaluate("5!")
        assertTrue(res is EvalResult.Success)
        assertEquals("120", (res as EvalResult.Success).formatted)
    }

    @Test
    fun testTrigonometryDegreesAndRadians() {
        val degRes = engine.evaluate("sin(90)", AngleMode.DEG)
        assertTrue(degRes is EvalResult.Success)
        assertEquals("1", (degRes as EvalResult.Success).formatted)

        val cosRes = engine.evaluate("cos(0)", AngleMode.DEG)
        assertTrue(cosRes is EvalResult.Success)
        assertEquals("1", (cosRes as EvalResult.Success).formatted)
    }

    @Test
    fun testDivisionByZero() {
        val res = engine.evaluate("10 ÷ 0")
        assertTrue(res is EvalResult.Error)
    }
}
