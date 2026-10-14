package com.example.calculator.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.engine.AngleMode

enum class ButtonType {
    NUMBER,
    OPERATOR,
    ACTION,
    EQUALS,
    SCIENTIFIC,
    MEMORY
}

@Composable
fun CalcButton(
    text: String,
    type: ButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    fontSize: Int = 22,
    isSelected: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    val containerColor = when (type) {
        ButtonType.NUMBER -> MaterialTheme.colorScheme.surfaceVariant
        ButtonType.OPERATOR -> MaterialTheme.colorScheme.primaryContainer
        ButtonType.ACTION -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
        ButtonType.EQUALS -> MaterialTheme.colorScheme.primary
        ButtonType.SCIENTIFIC -> if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondaryContainer
        ButtonType.MEMORY -> MaterialTheme.colorScheme.surfaceContainer
    }

    val contentColor = when (type) {
        ButtonType.NUMBER -> MaterialTheme.colorScheme.onSurfaceVariant
        ButtonType.OPERATOR -> MaterialTheme.colorScheme.onPrimaryContainer
        ButtonType.ACTION -> MaterialTheme.colorScheme.onErrorContainer
        ButtonType.EQUALS -> MaterialTheme.colorScheme.onPrimary
        ButtonType.SCIENTIFIC -> if (isSelected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSecondaryContainer
        ButtonType.MEMORY -> MaterialTheme.colorScheme.onSurface
    }

    FilledTonalButton(
        onClick = {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } catch (e: Exception) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onClick()
        },
        modifier = modifier
            .height(height)
            .padding(3.dp)
            .testTag("btn_${text.replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = if (type == ButtonType.NUMBER) FontWeight.Normal else FontWeight.Bold
        )
    }
}

@Composable
fun StandardKeypad(
    onInput: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onToggleSign: () -> Unit,
    onPercentage: () -> Unit,
    onEquals: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1: AC, ( ), %, ÷
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton("AC", ButtonType.ACTION, onClear, Modifier.weight(1f))
            CalcButton("( )", ButtonType.OPERATOR, { onInput("(") }, Modifier.weight(1f))
            CalcButton("%", ButtonType.OPERATOR, onPercentage, Modifier.weight(1f))
            CalcButton("÷", ButtonType.OPERATOR, { onInput("÷") }, Modifier.weight(1f))
        }

        // Row 2: 7, 8, 9, ×
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton("7", ButtonType.NUMBER, { onInput("7") }, Modifier.weight(1f))
            CalcButton("8", ButtonType.NUMBER, { onInput("8") }, Modifier.weight(1f))
            CalcButton("9", ButtonType.NUMBER, { onInput("9") }, Modifier.weight(1f))
            CalcButton("×", ButtonType.OPERATOR, { onInput("×") }, Modifier.weight(1f))
        }

        // Row 3: 4, 5, 6, −
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton("4", ButtonType.NUMBER, { onInput("4") }, Modifier.weight(1f))
            CalcButton("5", ButtonType.NUMBER, { onInput("5") }, Modifier.weight(1f))
            CalcButton("6", ButtonType.NUMBER, { onInput("6") }, Modifier.weight(1f))
            CalcButton("−", ButtonType.OPERATOR, { onInput("−") }, Modifier.weight(1f))
        }

        // Row 4: 1, 2, 3, +
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton("1", ButtonType.NUMBER, { onInput("1") }, Modifier.weight(1f))
            CalcButton("2", ButtonType.NUMBER, { onInput("2") }, Modifier.weight(1f))
            CalcButton("3", ButtonType.NUMBER, { onInput("3") }, Modifier.weight(1f))
            CalcButton("+", ButtonType.OPERATOR, { onInput("+") }, Modifier.weight(1f))
        }

        // Row 5: ±, 0, ., =
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton("±", ButtonType.NUMBER, onToggleSign, Modifier.weight(1f))
            CalcButton("0", ButtonType.NUMBER, { onInput("0") }, Modifier.weight(1f))
            CalcButton(".", ButtonType.NUMBER, { onInput(".") }, Modifier.weight(1f))
            CalcButton("=", ButtonType.EQUALS, onEquals, Modifier.weight(1f))
        }
    }
}

@Composable
fun ScientificKeypad(
    isSecondFunction: Boolean,
    angleMode: AngleMode,
    onToggleSecond: () -> Unit,
    onToggleAngle: () -> Unit,
    onInput: (String) -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onToggleSign: () -> Unit,
    onPercentage: () -> Unit,
    onSquare: () -> Unit,
    onCube: () -> Unit,
    onPower: () -> Unit,
    onInverse: () -> Unit,
    onFactorial: () -> Unit,
    onRand: () -> Unit,
    onEquals: () -> Unit,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Memory Toolbar: MC, MR, M+, M-, MS
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton("MC", ButtonType.MEMORY, onMemoryClear, Modifier.weight(1f), height = 40.dp, fontSize = 13)
            CalcButton("MR", ButtonType.MEMORY, onMemoryRecall, Modifier.weight(1f), height = 40.dp, fontSize = 13)
            CalcButton("M+", ButtonType.MEMORY, onMemoryAdd, Modifier.weight(1f), height = 40.dp, fontSize = 13)
            CalcButton("M−", ButtonType.MEMORY, onMemorySubtract, Modifier.weight(1f), height = 40.dp, fontSize = 13)
            CalcButton("MS", ButtonType.MEMORY, onMemoryStore, Modifier.weight(1f), height = 40.dp, fontSize = 13)
        }

        // Scientific Row 1: 2nd, RAD/DEG, sin/asin, cos/acos, tan/atan
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton("2nd", ButtonType.SCIENTIFIC, onToggleSecond, Modifier.weight(1f), height = 46.dp, fontSize = 14, isSelected = isSecondFunction)
            CalcButton(if (angleMode == AngleMode.DEG) "DEG" else "RAD", ButtonType.SCIENTIFIC, onToggleAngle, Modifier.weight(1f), height = 46.dp, fontSize = 14)
            CalcButton(if (isSecondFunction) "sin⁻¹" else "sin", ButtonType.SCIENTIFIC, { onFunction(if (isSecondFunction) "asin" else "sin") }, Modifier.weight(1f), height = 46.dp, fontSize = 14)
            CalcButton(if (isSecondFunction) "cos⁻¹" else "cos", ButtonType.SCIENTIFIC, { onFunction(if (isSecondFunction) "acos" else "cos") }, Modifier.weight(1f), height = 46.dp, fontSize = 14)
            CalcButton(if (isSecondFunction) "tan⁻¹" else "tan", ButtonType.SCIENTIFIC, { onFunction(if (isSecondFunction) "atan" else "tan") }, Modifier.weight(1f), height = 46.dp, fontSize = 14)
        }

        // Scientific Row 2: ln/eˣ, log/10ˣ, xʸ, √/x², !/∛
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton(if (isSecondFunction) "eˣ" else "ln", ButtonType.SCIENTIFIC, { if (isSecondFunction) onFunction("exp") else onFunction("ln") }, Modifier.weight(1f), height = 46.dp, fontSize = 14)
            CalcButton(if (isSecondFunction) "10ˣ" else "log", ButtonType.SCIENTIFIC, { if (isSecondFunction) onInput("10^") else onFunction("log") }, Modifier.weight(1f), height = 46.dp, fontSize = 14)
            CalcButton("xʸ", ButtonType.SCIENTIFIC, onPower, Modifier.weight(1f), height = 46.dp, fontSize = 14)
            CalcButton(if (isSecondFunction) "x²" else "√", ButtonType.SCIENTIFIC, { if (isSecondFunction) onSquare() else onFunction("sqrt") }, Modifier.weight(1f), height = 46.dp, fontSize = 14)
            CalcButton(if (isSecondFunction) "∛" else "x!", ButtonType.SCIENTIFIC, { if (isSecondFunction) onFunction("cbrt") else onFactorial() }, Modifier.weight(1f), height = 46.dp, fontSize = 14)
        }

        // Scientific Row 3: 1/x, π, e, (, )
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton("1/x", ButtonType.SCIENTIFIC, onInverse, Modifier.weight(1f), height = 46.dp, fontSize = 14)
            CalcButton("π", ButtonType.SCIENTIFIC, { onConstant("π") }, Modifier.weight(1f), height = 46.dp, fontSize = 16)
            CalcButton("e", ButtonType.SCIENTIFIC, { onConstant("e") }, Modifier.weight(1f), height = 46.dp, fontSize = 16)
            CalcButton("(", ButtonType.SCIENTIFIC, { onInput("(") }, Modifier.weight(1f), height = 46.dp, fontSize = 16)
            CalcButton(")", ButtonType.SCIENTIFIC, { onInput(")") }, Modifier.weight(1f), height = 46.dp, fontSize = 16)
        }

        // Numbers & Arithmetic grid: 5 columns
        // Row 4: 7, 8, 9, ÷, DEL
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton("7", ButtonType.NUMBER, { onInput("7") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("8", ButtonType.NUMBER, { onInput("8") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("9", ButtonType.NUMBER, { onInput("9") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("÷", ButtonType.OPERATOR, { onInput("÷") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("DEL", ButtonType.ACTION, onBackspace, Modifier.weight(1f), height = 50.dp, fontSize = 14)
        }

        // Row 5: 4, 5, 6, ×, AC
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton("4", ButtonType.NUMBER, { onInput("4") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("5", ButtonType.NUMBER, { onInput("5") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("6", ButtonType.NUMBER, { onInput("6") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("×", ButtonType.OPERATOR, { onInput("×") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("AC", ButtonType.ACTION, onClear, Modifier.weight(1f), height = 50.dp, fontSize = 14)
        }

        // Row 6: 1, 2, 3, −, Ans
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton("1", ButtonType.NUMBER, { onInput("1") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("2", ButtonType.NUMBER, { onInput("2") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("3", ButtonType.NUMBER, { onInput("3") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("−", ButtonType.OPERATOR, { onInput("−") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("Ans", ButtonType.SCIENTIFIC, { onInput("Ans") }, Modifier.weight(1f), height = 50.dp, fontSize = 14)
        }

        // Row 7: ±, 0, ., +, =
        Row(modifier = Modifier.fillMaxWidth()) {
            CalcButton("±", ButtonType.NUMBER, onToggleSign, Modifier.weight(1f), height = 50.dp, fontSize = 18)
            CalcButton("0", ButtonType.NUMBER, { onInput("0") }, Modifier.weight(1f), height = 50.dp)
            CalcButton(".", ButtonType.NUMBER, { onInput(".") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("+", ButtonType.OPERATOR, { onInput("+") }, Modifier.weight(1f), height = 50.dp)
            CalcButton("=", ButtonType.EQUALS, onEquals, Modifier.weight(1f), height = 50.dp)
        }
    }
}
