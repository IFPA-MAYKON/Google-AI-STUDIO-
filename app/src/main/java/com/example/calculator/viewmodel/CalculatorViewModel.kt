package com.example.calculator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.converter.ConversionUnit
import com.example.calculator.converter.UnitCategory
import com.example.calculator.converter.UnitConverterData
import com.example.calculator.data.CalculationEntity
import com.example.calculator.data.CalculatorDatabase
import com.example.calculator.engine.AngleMode
import com.example.calculator.engine.CalculatorEngine
import com.example.calculator.engine.EvalResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class CalcMode {
    STANDARD,
    SCIENTIFIC,
    CONVERTER
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = CalculatorEngine()
    private val db = CalculatorDatabase.getInstance(application)
    private val dao = db.calculationDao()

    val historyList: StateFlow<List<CalculationEntity>> = dao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _livePreview = MutableStateFlow("")
    val livePreview: StateFlow<String> = _livePreview.asStateFlow()

    private val _calcMode = MutableStateFlow(CalcMode.STANDARD)
    val calcMode: StateFlow<CalcMode> = _calcMode.asStateFlow()

    private val _angleMode = MutableStateFlow(AngleMode.DEG)
    val angleMode: StateFlow<AngleMode> = _angleMode.asStateFlow()

    private val _isSecondFunction = MutableStateFlow(false)
    val isSecondFunction: StateFlow<Boolean> = _isSecondFunction.asStateFlow()

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

    private val _lastAnswer = MutableStateFlow(0.0)
    val lastAnswer: StateFlow<Double> = _lastAnswer.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Converter States
    private val _converterCategory = MutableStateFlow(UnitCategory.LENGTH)
    val converterCategory: StateFlow<UnitCategory> = _converterCategory.asStateFlow()

    private val _converterFromUnit = MutableStateFlow(UnitConverterData.categories[UnitCategory.LENGTH]!![1]) // Metro
    val converterFromUnit: StateFlow<ConversionUnit> = _converterFromUnit.asStateFlow()

    private val _converterToUnit = MutableStateFlow(UnitConverterData.categories[UnitCategory.LENGTH]!![0]) // Quilômetro
    val converterToUnit: StateFlow<ConversionUnit> = _converterToUnit.asStateFlow()

    private val _converterInputValue = MutableStateFlow("1")
    val converterInputValue: StateFlow<String> = _converterInputValue.asStateFlow()

    private val _converterResultValue = MutableStateFlow("0.001")
    val converterResultValue: StateFlow<String> = _converterResultValue.asStateFlow()

    init {
        updateConverter()
    }

    fun setCalcMode(mode: CalcMode) {
        _calcMode.value = mode
    }

    fun toggleAngleMode() {
        _angleMode.value = if (_angleMode.value == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
        updateLivePreview(_expression.value)
    }

    fun toggleSecondFunction() {
        _isSecondFunction.value = !_isSecondFunction.value
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun onInput(char: String) {
        val current = _expression.value
        val updated = current + char
        _expression.value = updated
        updateLivePreview(updated)
    }

    fun onFunction(func: String) {
        val current = _expression.value
        val updated = "$current$func("
        _expression.value = updated
        updateLivePreview(updated)
    }

    fun onConstant(constant: String) {
        val current = _expression.value
        val updated = "$current$constant"
        _expression.value = updated
        updateLivePreview(updated)
    }

    fun onBackspace() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            // Check if removing a function name like "sin(", "asin(", "log(", "sqrt("
            val funcSuffixes = listOf(
                "sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "sinh(", "cosh(", "tanh(",
                "sin(", "cos(", "tan(", "log₁₀(", "log₂(", "log(", "ln(",
                "sqrt(", "cbrt(", "abs("
            )
            val matched = funcSuffixes.firstOrNull { current.endsWith(it) }
            val updated = if (matched != null) {
                current.dropLast(matched.length)
            } else {
                current.dropLast(1)
            }
            _expression.value = updated
            updateLivePreview(updated)
        }
    }

    fun onClear() {
        _expression.value = ""
        _livePreview.value = ""
    }

    fun onToggleSign() {
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "-"
            return
        }
        // If whole expression is a number, toggle sign
        val num = current.toDoubleOrNull()
        if (num != null) {
            val toggled = if (current.startsWith("-")) current.substring(1) else "-$current"
            _expression.value = toggled
            updateLivePreview(toggled)
        } else {
            // Otherwise append minus or wrap
            onInput("-")
        }
    }

    fun onPercentage() {
        val current = _expression.value
        if (current.isNotEmpty() && !current.endsWith("%")) {
            val updated = "$current%"
            _expression.value = updated
            updateLivePreview(updated)
        }
    }

    fun onRand() {
        val r = Random.nextDouble(0.0, 1.0)
        val formatted = String.format(java.util.Locale.US, "%.4f", r)
        onInput(formatted)
    }

    fun onInverse() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            val updated = "1/($current)"
            _expression.value = updated
            updateLivePreview(updated)
        }
    }

    fun onSquare() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            val updated = "($current)^2"
            _expression.value = updated
            updateLivePreview(updated)
        }
    }

    fun onCube() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            val updated = "($current)^3"
            _expression.value = updated
            updateLivePreview(updated)
        }
    }

    fun onPower() {
        onInput("^")
    }

    fun onFactorial() {
        val current = _expression.value
        if (current.isNotEmpty() && !current.endsWith("!")) {
            val updated = "$current!"
            _expression.value = updated
            updateLivePreview(updated)
        }
    }

    fun onEquals() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) return

        when (val eval = engine.evaluate(expr, _angleMode.value, _lastAnswer.value)) {
            is EvalResult.Success -> {
                val formatted = eval.formatted
                _lastAnswer.value = eval.value
                val modeLabel = if (_calcMode.value == CalcMode.SCIENTIFIC) "CIENTÍFICA" else "PADRÃO"

                // Save to local Room history
                viewModelScope.launch {
                    dao.insertCalculation(
                        CalculationEntity(
                            expression = expr,
                            result = formatted,
                            numericResult = eval.value,
                            mode = modeLabel
                        )
                    )
                }

                _expression.value = formatted
                _livePreview.value = ""
            }
            is EvalResult.Error -> {
                _statusMessage.value = eval.message
            }
        }
    }

    private fun updateLivePreview(expr: String) {
        if (expr.isBlank()) {
            _livePreview.value = ""
            return
        }
        // Only evaluate if there's at least an operator or function
        val hasOperation = expr.any { it in "+-×÷*/%^!" } ||
                listOf("sin", "cos", "tan", "log", "ln", "sqrt", "cbrt").any { expr.contains(it) }

        if (!hasOperation) {
            _livePreview.value = ""
            return
        }

        when (val eval = engine.evaluate(expr, _angleMode.value, _lastAnswer.value)) {
            is EvalResult.Success -> {
                _livePreview.value = "= ${eval.formatted}"
            }
            is EvalResult.Error -> {
                // In typing mode, incomplete expressions are normal, don't scream error in preview
                _livePreview.value = ""
            }
        }
    }

    // Memory Functions
    fun onMemoryClear() {
        _memoryValue.value = 0.0
        _statusMessage.value = "Memória limpa (MC)"
    }

    fun onMemoryRecall() {
        val mem = CalculatorEngine.formatNumber(_memoryValue.value)
        onInput(mem)
        _statusMessage.value = "Memória recuperada: $mem"
    }

    fun onMemoryAdd() {
        evalCurrentNumeric()?.let { num ->
            _memoryValue.value += num
            _statusMessage.value = "Adicionado à memória (M+)"
        }
    }

    fun onMemorySubtract() {
        evalCurrentNumeric()?.let { num ->
            _memoryValue.value -= num
            _statusMessage.value = "Subtraído da memória (M-)"
        }
    }

    fun onMemoryStore() {
        evalCurrentNumeric()?.let { num ->
            _memoryValue.value = num
            _statusMessage.value = "Armazenado na memória (MS)"
        }
    }

    private fun evalCurrentNumeric(): Double? {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) return null
        return when (val eval = engine.evaluate(expr, _angleMode.value, _lastAnswer.value)) {
            is EvalResult.Success -> eval.value
            is EvalResult.Error -> null
        }
    }

    // History controls
    fun deleteHistoryItem(item: CalculationEntity) {
        viewModelScope.launch {
            dao.deleteCalculation(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dao.clearAllHistory()
        }
    }

    fun useHistoryExpression(expr: String) {
        _expression.value = expr
        updateLivePreview(expr)
    }

    fun useHistoryResult(res: String) {
        _expression.value = _expression.value + res
        updateLivePreview(_expression.value)
    }

    // Converter methods
    fun selectConverterCategory(cat: UnitCategory) {
        _converterCategory.value = cat
        val units = UnitConverterData.categories[cat] ?: return
        _converterFromUnit.value = units[0]
        _converterToUnit.value = if (units.size > 1) units[1] else units[0]
        updateConverter()
    }

    fun selectFromUnit(unit: ConversionUnit) {
        _converterFromUnit.value = unit
        updateConverter()
    }

    fun selectToUnit(unit: ConversionUnit) {
        _converterToUnit.value = unit
        updateConverter()
    }

    fun onConverterInputChange(input: String) {
        _converterInputValue.value = input
        updateConverter()
    }

    fun swapConverterUnits() {
        val temp = _converterFromUnit.value
        _converterFromUnit.value = _converterToUnit.value
        _converterToUnit.value = temp
        updateConverter()
    }

    private fun updateConverter() {
        val raw = _converterInputValue.value.replace(',', '.')
        val d = raw.toDoubleOrNull() ?: 0.0
        val converted = UnitConverterData.convert(
            d,
            _converterFromUnit.value,
            _converterToUnit.value,
            _converterCategory.value
        )
        _converterResultValue.value = CalculatorEngine.formatNumber(converted)
    }
}
