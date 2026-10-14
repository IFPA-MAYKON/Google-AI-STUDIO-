package com.example.calculator.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.engine.AngleMode
import com.example.calculator.viewmodel.CalcMode

@Composable
fun CalculatorDisplay(
    expression: String,
    livePreview: String,
    calcMode: CalcMode,
    angleMode: AngleMode,
    hasMemory: Boolean,
    onToggleAngleMode: () -> Unit,
    onOpenHistory: () -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    // Auto-scroll expression to the end when typing
    LaunchedEffect(expression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("calculator_display_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Top status badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // DEG / RAD Toggle Button
                    FilterChip(
                        selected = angleMode == AngleMode.DEG,
                        onClick = onToggleAngleMode,
                        label = {
                            Text(
                                text = if (angleMode == AngleMode.DEG) "DEG" else "RAD",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("angle_mode_chip")
                    )

                    // Memory Indicator Badge
                    AnimatedVisibility(
                        visible = hasMemory,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "M",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy expression/result
                    IconButton(
                        onClick = {
                            val textToCopy = livePreview.removePrefix("= ").trim().ifEmpty { expression }
                            if (textToCopy.isNotEmpty()) {
                                clipboard.setText(AnnotatedString(textToCopy))
                                Toast.makeText(context, "Copiado: $textToCopy", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("copy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar resultado",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Open History button
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("open_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Abrir Histórico",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Backspace button
                    IconButton(
                        onClick = onBackspace,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("backspace_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Apagar último caractere",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main formula / expression
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = expression.ifEmpty { "0" },
                    fontSize = if (expression.length > 14) 32.sp else 42.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.End,
                    color = if (expression.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.testTag("main_expression_text")
                )
            }

            // Live evaluation preview
            AnimatedVisibility(
                visible = livePreview.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = livePreview,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("live_preview_text")
                )
            }
        }
    }
}
