package com.example.calculator.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.ui.components.*
import com.example.calculator.viewmodel.CalcMode
import com.example.calculator.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsStateWithLifecycle()
    val livePreview by viewModel.livePreview.collectAsStateWithLifecycle()
    val calcMode by viewModel.calcMode.collectAsStateWithLifecycle()
    val angleMode by viewModel.angleMode.collectAsStateWithLifecycle()
    val isSecondFunction by viewModel.isSecondFunction.collectAsStateWithLifecycle()
    val memoryValue by viewModel.memoryValue.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    // Converter States
    val converterCategory by viewModel.converterCategory.collectAsStateWithLifecycle()
    val converterFromUnit by viewModel.converterFromUnit.collectAsStateWithLifecycle()
    val converterToUnit by viewModel.converterToUnit.collectAsStateWithLifecycle()
    val converterInputValue by viewModel.converterInputValue.collectAsStateWithLifecycle()
    val converterResultValue by viewModel.converterResultValue.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showHistorySheet by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Show status messages in Snackbar
    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearStatusMessage()
        }
    }

    // Handle back button: if in converter or history, go back to calculator
    BackHandler(enabled = showHistorySheet || calcMode != CalcMode.STANDARD) {
        if (showHistorySheet) {
            showHistorySheet = false
        } else {
            viewModel.setCalcMode(CalcMode.STANDARD)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calculadora Omni",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("appbar_history_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (historyList.isNotEmpty()) {
                                    Badge { Text(historyList.size.coerceAtMost(99).toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Histórico de Cálculos"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented mode switcher tabs
            PrimaryTabRow(
                selectedTabIndex = calcMode.ordinal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Tab(
                    selected = calcMode == CalcMode.STANDARD,
                    onClick = { viewModel.setCalcMode(CalcMode.STANDARD) },
                    text = { Text("Padrão", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_standard")
                )
                Tab(
                    selected = calcMode == CalcMode.SCIENTIFIC,
                    onClick = { viewModel.setCalcMode(CalcMode.SCIENTIFIC) },
                    text = { Text("Científica", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_scientific")
                )
                Tab(
                    selected = calcMode == CalcMode.CONVERTER,
                    onClick = { viewModel.setCalcMode(CalcMode.CONVERTER) },
                    text = { Text("Conversor", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_converter")
                )
            }

            AnimatedContent(
                targetState = calcMode,
                label = "ModeTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetMode ->
                when (targetMode) {
                    CalcMode.STANDARD -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            CalculatorDisplay(
                                expression = expression,
                                livePreview = livePreview,
                                calcMode = CalcMode.STANDARD,
                                angleMode = angleMode,
                                hasMemory = memoryValue != 0.0,
                                onToggleAngleMode = { viewModel.toggleAngleMode() },
                                onOpenHistory = { showHistorySheet = true },
                                onBackspace = { viewModel.onBackspace() },
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            StandardKeypad(
                                onInput = { viewModel.onInput(it) },
                                onClear = { viewModel.onClear() },
                                onBackspace = { viewModel.onBackspace() },
                                onToggleSign = { viewModel.onToggleSign() },
                                onPercentage = { viewModel.onPercentage() },
                                onEquals = { viewModel.onEquals() }
                            )
                        }
                    }

                    CalcMode.SCIENTIFIC -> {
                        if (isLandscape) {
                            // Landscape layout: Display & left scientific functions, right standard keys
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                CalculatorDisplay(
                                    expression = expression,
                                    livePreview = livePreview,
                                    calcMode = CalcMode.SCIENTIFIC,
                                    angleMode = angleMode,
                                    hasMemory = memoryValue != 0.0,
                                    onToggleAngleMode = { viewModel.toggleAngleMode() },
                                    onOpenHistory = { showHistorySheet = true },
                                    onBackspace = { viewModel.onBackspace() },
                                    modifier = Modifier.weight(1f)
                                )

                                ScientificKeypad(
                                    isSecondFunction = isSecondFunction,
                                    angleMode = angleMode,
                                    onToggleSecond = { viewModel.toggleSecondFunction() },
                                    onToggleAngle = { viewModel.toggleAngleMode() },
                                    onInput = { viewModel.onInput(it) },
                                    onFunction = { viewModel.onFunction(it) },
                                    onConstant = { viewModel.onConstant(it) },
                                    onClear = { viewModel.onClear() },
                                    onBackspace = { viewModel.onBackspace() },
                                    onToggleSign = { viewModel.onToggleSign() },
                                    onPercentage = { viewModel.onPercentage() },
                                    onSquare = { viewModel.onSquare() },
                                    onCube = { viewModel.onCube() },
                                    onPower = { viewModel.onPower() },
                                    onInverse = { viewModel.onInverse() },
                                    onFactorial = { viewModel.onFactorial() },
                                    onRand = { viewModel.onRand() },
                                    onEquals = { viewModel.onEquals() },
                                    onMemoryClear = { viewModel.onMemoryClear() },
                                    onMemoryRecall = { viewModel.onMemoryRecall() },
                                    onMemoryAdd = { viewModel.onMemoryAdd() },
                                    onMemorySubtract = { viewModel.onMemorySubtract() },
                                    onMemoryStore = { viewModel.onMemoryStore() },
                                    modifier = Modifier.weight(1.3f)
                                )
                            }
                        } else {
                            // Portrait layout
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 6.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                CalculatorDisplay(
                                    expression = expression,
                                    livePreview = livePreview,
                                    calcMode = CalcMode.SCIENTIFIC,
                                    angleMode = angleMode,
                                    hasMemory = memoryValue != 0.0,
                                    onToggleAngleMode = { viewModel.toggleAngleMode() },
                                    onOpenHistory = { showHistorySheet = true },
                                    onBackspace = { viewModel.onBackspace() },
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                ScientificKeypad(
                                    isSecondFunction = isSecondFunction,
                                    angleMode = angleMode,
                                    onToggleSecond = { viewModel.toggleSecondFunction() },
                                    onToggleAngle = { viewModel.toggleAngleMode() },
                                    onInput = { viewModel.onInput(it) },
                                    onFunction = { viewModel.onFunction(it) },
                                    onConstant = { viewModel.onConstant(it) },
                                    onClear = { viewModel.onClear() },
                                    onBackspace = { viewModel.onBackspace() },
                                    onToggleSign = { viewModel.onToggleSign() },
                                    onPercentage = { viewModel.onPercentage() },
                                    onSquare = { viewModel.onSquare() },
                                    onCube = { viewModel.onCube() },
                                    onPower = { viewModel.onPower() },
                                    onInverse = { viewModel.onInverse() },
                                    onFactorial = { viewModel.onFactorial() },
                                    onRand = { viewModel.onRand() },
                                    onEquals = { viewModel.onEquals() },
                                    onMemoryClear = { viewModel.onMemoryClear() },
                                    onMemoryRecall = { viewModel.onMemoryRecall() },
                                    onMemoryAdd = { viewModel.onMemoryAdd() },
                                    onMemorySubtract = { viewModel.onMemorySubtract() },
                                    onMemoryStore = { viewModel.onMemoryStore() }
                                )
                            }
                        }
                    }

                    CalcMode.CONVERTER -> {
                        UnitConverterScreen(
                            currentCategory = converterCategory,
                            fromUnit = converterFromUnit,
                            toUnit = converterToUnit,
                            inputValue = converterInputValue,
                            resultValue = converterResultValue,
                            onCategorySelected = { viewModel.selectConverterCategory(it) },
                            onFromUnitSelected = { viewModel.selectFromUnit(it) },
                            onToUnitSelected = { viewModel.selectToUnit(it) },
                            onInputChange = { viewModel.onConverterInputChange(it) },
                            onSwapUnits = { viewModel.swapConverterUnits() }
                        )
                    }
                }
            }
        }
    }

    if (showHistorySheet) {
        HistoryBottomSheet(
            historyList = historyList,
            onDismiss = { showHistorySheet = false },
            onUseExpression = { expr ->
                viewModel.useHistoryExpression(expr)
                showHistorySheet = false
            },
            onUseResult = { res ->
                viewModel.useHistoryResult(res)
                showHistorySheet = false
            },
            onDeleteItem = { item -> viewModel.deleteHistoryItem(item) },
            onClearAll = { viewModel.clearAllHistory() }
        )
    }
}
