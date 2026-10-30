package com.example.calculator

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val expression by viewModel.expression.collectAsState()
    val result by viewModel.result.collectAsState()
    val previewResult by viewModel.previewResult.collectAsState()
    val fractionResult by viewModel.fractionResult.collectAsState()
    val angleUnit by viewModel.angleUnit.collectAsState()
    val isInverse by viewModel.isInverseMode.collectAsState()
    val isHyperbolic by viewModel.isHyperbolicMode.collectAsState()
    val memoryVal by viewModel.memoryValue.collectAsState()
    val history by viewModel.history.collectAsState()

    var showHistorySheet by remember { mutableStateOf(false) }
    var keypadTab by remember { mutableIntStateOf(0) } // 0: Standard + Sci, 1: Advanced Trig/Hyperbolics

    fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Slate950,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Angle Unit Toggle Pill
                FilterChip(
                    selected = true,
                    onClick = {
                        triggerHaptic()
                        viewModel.toggleAngleUnit()
                    },
                    label = {
                        Text(
                            text = angleUnit.shortLabel,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CyanNeonContainer,
                        labelColor = CyanNeon
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = CyanNeon.copy(alpha = 0.5f),
                        enabled = true,
                        selected = true
                    ),
                    modifier = Modifier.testTag("angle_unit_toggle")
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (memoryVal != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberContainer,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = "M",
                                color = AmberWarm,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Keypad Mode Switcher Tab
                    TabRow(
                        selectedTabIndex = keypadTab,
                        containerColor = Slate900,
                        contentColor = CyanNeon,
                        modifier = Modifier
                            .width(160.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        Tab(
                            selected = keypadTab == 0,
                            onClick = {
                                triggerHaptic()
                                keypadTab = 0
                            },
                            text = { Text("Basic", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = keypadTab == 1,
                            onClick = {
                                triggerHaptic()
                                keypadTab = 1
                            },
                            text = { Text("Trig/Hyp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    IconButton(
                        onClick = {
                            triggerHaptic()
                            showHistorySheet = true
                        },
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Calculation History",
                            tint = Slate200
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Display Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Slate900, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                // Expression Scrollable Display
                val scrollState = rememberScrollState()
                LaunchedEffect(expression) {
                    scrollState.scrollTo(scrollState.maxValue)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = if (expression.isEmpty()) "0" else expression,
                        fontSize = if (expression.length > 18) 28.sp else 36.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (expression.isEmpty()) Slate600 else Slate100,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("expression_display")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Result or Preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (fractionResult != null) {
                        Text(
                            text = "≈ $fractionResult",
                            color = Slate400,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Text(
                        text = when {
                            result.isNotEmpty() -> "= $result"
                            previewResult.isNotEmpty() -> "= $previewResult"
                            else -> ""
                        },
                        fontSize = if (result.isNotEmpty()) 32.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isNotEmpty()) CyanNeon else Slate400,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("result_display")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Memory and Modifier Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalcSecondaryButton(
                    label = "2nd",
                    active = isInverse,
                    onClick = {
                        triggerHaptic()
                        viewModel.toggleInverse()
                    },
                    modifier = Modifier.weight(1f).testTag("inv_button")
                )
                CalcSecondaryButton(
                    label = "hyp",
                    active = isHyperbolic,
                    onClick = {
                        triggerHaptic()
                        viewModel.toggleHyperbolic()
                    },
                    modifier = Modifier.weight(1f).testTag("hyp_button")
                )
                CalcSecondaryButton(
                    label = "MC",
                    enabled = memoryVal != null,
                    onClick = {
                        triggerHaptic()
                        viewModel.memoryClear()
                    },
                    modifier = Modifier.weight(1f).testTag("mc_button")
                )
                CalcSecondaryButton(
                    label = "MR",
                    enabled = memoryVal != null,
                    onClick = {
                        triggerHaptic()
                        viewModel.memoryRecall()
                    },
                    modifier = Modifier.weight(1f).testTag("mr_button")
                )
                CalcSecondaryButton(
                    label = "M+",
                    onClick = {
                        triggerHaptic()
                        viewModel.memoryAdd()
                    },
                    modifier = Modifier.weight(1f).testTag("mplus_button")
                )
                CalcSecondaryButton(
                    label = "M-",
                    onClick = {
                        triggerHaptic()
                        viewModel.memorySub()
                    },
                    modifier = Modifier.weight(1f).testTag("msub_button")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Keypad Area
            AnimatedContent(
                targetState = keypadTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "keypad_content"
            ) { tab ->
                if (tab == 0) {
                    StandardScientificKeypad(
                        isInverse = isInverse,
                        isHyperbolic = isHyperbolic,
                        onInput = {
                            triggerHaptic()
                            viewModel.onInput(it)
                        },
                        onFunction = {
                            triggerHaptic()
                            viewModel.onFunctionInput(it)
                        },
                        onClear = {
                            triggerHaptic()
                            viewModel.onClear()
                        },
                        onDelete = {
                            triggerHaptic()
                            viewModel.onDelete()
                        },
                        onEvaluate = {
                            triggerHaptic()
                            viewModel.onEvaluate()
                        }
                    )
                } else {
                    AdvancedTrigKeypad(
                        isInverse = isInverse,
                        isHyperbolic = isHyperbolic,
                        onInput = {
                            triggerHaptic()
                            viewModel.onInput(it)
                        },
                        onFunction = {
                            triggerHaptic()
                            viewModel.onFunctionInput(it)
                        },
                        onClear = {
                            triggerHaptic()
                            viewModel.onClear()
                        },
                        onDelete = {
                            triggerHaptic()
                            viewModel.onDelete()
                        },
                        onEvaluate = {
                            triggerHaptic()
                            viewModel.onEvaluate()
                        }
                    )
                }
            }
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = Slate900,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calculation History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    if (history.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                triggerHaptic()
                                viewModel.clearHistory()
                            }
                        ) {
                            Text("Clear All", color = RosePink)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = Slate600,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No history yet", color = Slate400, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history, key = { it.id }) { item ->
                            Card(
                                onClick = {
                                    triggerHaptic()
                                    viewModel.selectHistoryItem(item)
                                    showHistorySheet = false
                                },
                                colors = CardDefaults.cardColors(containerColor = Slate850),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = item.expression,
                                        color = Slate400,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "= ${item.result}",
                                            color = CyanNeon,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = item.angleUnit.shortLabel,
                                            color = VioletElectric,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StandardScientificKeypad(
    isInverse: Boolean,
    isHyperbolic: Boolean,
    onInput: (String) -> Unit,
    onFunction: (String) -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onEvaluate: () -> Unit
) {
    // 5 Columns Grid for high-density scientific productivity
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: Scientific functions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val sinLabel = when {
                isHyperbolic && isInverse -> "asinh"
                isHyperbolic -> "sinh"
                isInverse -> "asin"
                else -> "sin"
            }
            CalcButton(label = sinLabel, type = ButtonType.SCIENTIFIC, onClick = { onFunction(sinLabel) }, modifier = Modifier.weight(1f))

            val cosLabel = when {
                isHyperbolic && isInverse -> "acosh"
                isHyperbolic -> "cosh"
                isInverse -> "acos"
                else -> "cos"
            }
            CalcButton(label = cosLabel, type = ButtonType.SCIENTIFIC, onClick = { onFunction(cosLabel) }, modifier = Modifier.weight(1f))

            val tanLabel = when {
                isHyperbolic && isInverse -> "atanh"
                isHyperbolic -> "tanh"
                isInverse -> "atan"
                else -> "tan"
            }
            CalcButton(label = tanLabel, type = ButtonType.SCIENTIFIC, onClick = { onFunction(tanLabel) }, modifier = Modifier.weight(1f))

            val logLabel = if (isInverse) "10^" else "log"
            CalcButton(label = logLabel, type = ButtonType.SCIENTIFIC, onClick = { if (isInverse) onInput("10^") else onFunction("log") }, modifier = Modifier.weight(1f))

            val lnLabel = if (isInverse) "e^" else "ln"
            CalcButton(label = lnLabel, type = ButtonType.SCIENTIFIC, onClick = { if (isInverse) onInput("e^") else onFunction("ln") }, modifier = Modifier.weight(1f))
        }

        // Row 2: Powers, Roots, Pi, e, Clear
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val sqLabel = if (isInverse) "√" else "x²"
            CalcButton(label = sqLabel, type = ButtonType.SCIENTIFIC, onClick = { if (isInverse) onFunction("sqrt") else onInput("^2") }, modifier = Modifier.weight(1f))

            val powLabel = if (isInverse) "∛" else "x^y"
            CalcButton(label = powLabel, type = ButtonType.SCIENTIFIC, onClick = { if (isInverse) onFunction("cbrt") else onInput("^") }, modifier = Modifier.weight(1f))

            CalcButton(label = "π", type = ButtonType.SCIENTIFIC, onClick = { onInput("π") }, modifier = Modifier.weight(1f))
            CalcButton(label = "e", type = ButtonType.SCIENTIFIC, onClick = { onInput("e") }, modifier = Modifier.weight(1f))
            CalcButton(label = "AC", type = ButtonType.ACTION, onClick = onClear, modifier = Modifier.weight(1f))
        }

        // Row 3: Brackets, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton(label = "(", type = ButtonType.OPERATOR, onClick = { onInput("(") }, modifier = Modifier.weight(1f))
            CalcButton(label = ")", type = ButtonType.OPERATOR, onClick = { onInput(")") }, modifier = Modifier.weight(1f))
            CalcButton(label = "n!", type = ButtonType.SCIENTIFIC, onClick = { onInput("!") }, modifier = Modifier.weight(1f))
            CalcButton(label = "%", type = ButtonType.OPERATOR, onClick = { onInput("%") }, modifier = Modifier.weight(1f))
            CalcButton(label = "÷", type = ButtonType.OPERATOR, onClick = { onInput("÷") }, modifier = Modifier.weight(1f))
        }

        // Row 4: 7 8 9 × DEL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton(label = "7", type = ButtonType.NUMBER, onClick = { onInput("7") }, modifier = Modifier.weight(1f))
            CalcButton(label = "8", type = ButtonType.NUMBER, onClick = { onInput("8") }, modifier = Modifier.weight(1f))
            CalcButton(label = "9", type = ButtonType.NUMBER, onClick = { onInput("9") }, modifier = Modifier.weight(1f))
            CalcButton(label = "×", type = ButtonType.OPERATOR, onClick = { onInput("×") }, modifier = Modifier.weight(1f))
            CalcButton(label = "⌫", type = ButtonType.ACTION, onClick = onDelete, modifier = Modifier.weight(1f))
        }

        // Row 5: 4 5 6 -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton(label = "4", type = ButtonType.NUMBER, onClick = { onInput("4") }, modifier = Modifier.weight(1f))
            CalcButton(label = "5", type = ButtonType.NUMBER, onClick = { onInput("5") }, modifier = Modifier.weight(1f))
            CalcButton(label = "6", type = ButtonType.NUMBER, onClick = { onInput("6") }, modifier = Modifier.weight(1f))
            CalcButton(label = "−", type = ButtonType.OPERATOR, onClick = { onInput("−") }, modifier = Modifier.weight(1f))
            CalcButton(label = "1/x", type = ButtonType.SCIENTIFIC, onClick = { onInput("^(-1)") }, modifier = Modifier.weight(1f))
        }

        // Row 6: 1 2 3 +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton(label = "1", type = ButtonType.NUMBER, onClick = { onInput("1") }, modifier = Modifier.weight(1f))
            CalcButton(label = "2", type = ButtonType.NUMBER, onClick = { onInput("2") }, modifier = Modifier.weight(1f))
            CalcButton(label = "3", type = ButtonType.NUMBER, onClick = { onInput("3") }, modifier = Modifier.weight(1f))
            CalcButton(label = "+", type = ButtonType.OPERATOR, onClick = { onInput("+") }, modifier = Modifier.weight(1f))
            CalcButton(label = "|x|", type = ButtonType.SCIENTIFIC, onClick = { onFunction("abs") }, modifier = Modifier.weight(1f))
        }

        // Row 7: 0 . ± =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton(label = "0", type = ButtonType.NUMBER, onClick = { onInput("0") }, modifier = Modifier.weight(1f))
            CalcButton(label = ".", type = ButtonType.NUMBER, onClick = { onInput(".") }, modifier = Modifier.weight(1f))
            CalcButton(label = "±", type = ButtonType.NUMBER, onClick = { onInput("(-1)*") }, modifier = Modifier.weight(1f))
            CalcButton(label = "=", type = ButtonType.EQUAL, onClick = onEvaluate, modifier = Modifier.weight(2f))
        }
    }
}

@Composable
private fun AdvancedTrigKeypad(
    isInverse: Boolean,
    isHyperbolic: Boolean,
    onInput: (String) -> Unit,
    onFunction: (String) -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onEvaluate: () -> Unit
) {
    // Advanced Dedicated Trigonometry & Hyperbolic Keypad
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: Direct Reciprocal Trig Functions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val secLabel = when {
                isHyperbolic && isInverse -> "asech"
                isHyperbolic -> "sech"
                isInverse -> "asec"
                else -> "sec"
            }
            val cscLabel = when {
                isHyperbolic && isInverse -> "acsch"
                isHyperbolic -> "csch"
                isInverse -> "acsc"
                else -> "csc"
            }
            val cotLabel = when {
                isHyperbolic && isInverse -> "acoth"
                isHyperbolic -> "coth"
                isInverse -> "acot"
                else -> "cot"
            }
            CalcButton(label = secLabel, type = ButtonType.SCIENTIFIC, onClick = { onFunction(secLabel) }, modifier = Modifier.weight(1f))
            CalcButton(label = cscLabel, type = ButtonType.SCIENTIFIC, onClick = { onFunction(cscLabel) }, modifier = Modifier.weight(1f))
            CalcButton(label = cotLabel, type = ButtonType.SCIENTIFIC, onClick = { onFunction(cotLabel) }, modifier = Modifier.weight(1f))
            CalcButton(label = "log2", type = ButtonType.SCIENTIFIC, onClick = { onFunction("log2") }, modifier = Modifier.weight(1f))
        }

        // Row 2: Constants & Special Math Functions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton(label = "φ (phi)", type = ButtonType.SCIENTIFIC, onClick = { onInput("φ") }, modifier = Modifier.weight(1f))
            CalcButton(label = "c (light)", type = ButtonType.SCIENTIFIC, onClick = { onInput("c") }, modifier = Modifier.weight(1f))
            CalcButton(label = "exp", type = ButtonType.SCIENTIFIC, onClick = { onFunction("exp") }, modifier = Modifier.weight(1f))
            CalcButton(label = "AC", type = ButtonType.ACTION, onClick = onClear, modifier = Modifier.weight(1f))
        }

        // Row 3: Rounding / Floor / Ceil
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton(label = "floor", type = ButtonType.SCIENTIFIC, onClick = { onFunction("floor") }, modifier = Modifier.weight(1f))
            CalcButton(label = "ceil", type = ButtonType.SCIENTIFIC, onClick = { onFunction("ceil") }, modifier = Modifier.weight(1f))
            CalcButton(label = "round", type = ButtonType.SCIENTIFIC, onClick = { onFunction("round") }, modifier = Modifier.weight(1f))
            CalcButton(label = "⌫", type = ButtonType.ACTION, onClick = onDelete, modifier = Modifier.weight(1f))
        }

        // Row 4: Numbers row with shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton(label = "7", type = ButtonType.NUMBER, onClick = { onInput("7") }, modifier = Modifier.weight(1f))
            CalcButton(label = "8", type = ButtonType.NUMBER, onClick = { onInput("8") }, modifier = Modifier.weight(1f))
            CalcButton(label = "9", type = ButtonType.NUMBER, onClick = { onInput("9") }, modifier = Modifier.weight(1f))
            CalcButton(label = "÷", type = ButtonType.OPERATOR, onClick = { onInput("÷") }, modifier = Modifier.weight(1f))
        }

        // Row 5: 4 5 6 ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton(label = "4", type = ButtonType.NUMBER, onClick = { onInput("4") }, modifier = Modifier.weight(1f))
            CalcButton(label = "5", type = ButtonType.NUMBER, onClick = { onInput("5") }, modifier = Modifier.weight(1f))
            CalcButton(label = "6", type = ButtonType.NUMBER, onClick = { onInput("6") }, modifier = Modifier.weight(1f))
            CalcButton(label = "×", type = ButtonType.OPERATOR, onClick = { onInput("×") }, modifier = Modifier.weight(1f))
        }

        // Row 6: 1 2 3 -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton(label = "1", type = ButtonType.NUMBER, onClick = { onInput("1") }, modifier = Modifier.weight(1f))
            CalcButton(label = "2", type = ButtonType.NUMBER, onClick = { onInput("2") }, modifier = Modifier.weight(1f))
            CalcButton(label = "3", type = ButtonType.NUMBER, onClick = { onInput("3") }, modifier = Modifier.weight(1f))
            CalcButton(label = "−", type = ButtonType.OPERATOR, onClick = { onInput("−") }, modifier = Modifier.weight(1f))
        }

        // Row 7: 0 . + =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcButton(label = "0", type = ButtonType.NUMBER, onClick = { onInput("0") }, modifier = Modifier.weight(1f))
            CalcButton(label = ".", type = ButtonType.NUMBER, onClick = { onInput(".") }, modifier = Modifier.weight(1f))
            CalcButton(label = "+", type = ButtonType.OPERATOR, onClick = { onInput("+") }, modifier = Modifier.weight(1f))
            CalcButton(label = "=", type = ButtonType.EQUAL, onClick = onEvaluate, modifier = Modifier.weight(1f))
        }
    }
}

enum class ButtonType {
    NUMBER, OPERATOR, SCIENTIFIC, ACTION, EQUAL
}

@Composable
private fun CalcButton(
    label: String,
    type: ButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when (type) {
        ButtonType.NUMBER -> KeypadNumBg
        ButtonType.OPERATOR -> KeypadOpBg
        ButtonType.SCIENTIFIC -> KeypadSciBg
        ButtonType.ACTION -> KeypadActionBg
        ButtonType.EQUAL -> KeypadEqualBg
    }
    val fg = when (type) {
        ButtonType.NUMBER -> KeypadNumText
        ButtonType.OPERATOR -> KeypadOpText
        ButtonType.SCIENTIFIC -> KeypadSciText
        ButtonType.ACTION -> KeypadActionText
        ButtonType.EQUAL -> KeypadEqualText
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bg,
        tonalElevation = 2.dp,
        modifier = modifier
            .height(52.dp)
            .testTag("btn_$label")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = fg,
                fontSize = if (label.length > 4) 12.sp else 16.sp,
                fontWeight = if (type == ButtonType.EQUAL || type == ButtonType.NUMBER) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CalcSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = if (active) VioletContainer else Slate850,
        tonalElevation = 1.dp,
        modifier = modifier.height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (active) VioletElectric else if (enabled) Slate300 else Slate600,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

