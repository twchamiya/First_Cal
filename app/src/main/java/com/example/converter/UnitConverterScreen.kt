package com.example.converter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    viewModel: UnitConverterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val category by viewModel.selectedCategory.collectAsState()
    val fromUnit by viewModel.fromUnit.collectAsState()
    val toUnit by viewModel.toUnit.collectAsState()
    val inputValue by viewModel.inputValue.collectAsState()
    val outputValue by viewModel.outputValue.collectAsState()
    val formulaInfo by viewModel.formulaInfo.collectAsState()

    var showFromMenu by remember { mutableStateOf(false) }
    var showToMenu by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Slate950,
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Engineering Unit Converter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate850
                    ) {
                        Text(
                            text = category.displayName,
                            color = CyanNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Category Selector Chips
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UnitCategory.values().forEach { cat ->
                        FilterChip(
                            selected = cat == category,
                            onClick = { viewModel.selectCategory(cat) },
                            label = { Text(cat.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeonContainer,
                                selectedLabelColor = CyanNeon,
                                containerColor = Slate900,
                                labelColor = Slate300
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (cat == category) CyanNeon else Slate700,
                                enabled = true,
                                selected = cat == category
                            )
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // From Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("FROM", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                            // Unit Dropdown
                            Box {
                                OutlinedButton(
                                    onClick = { showFromMenu = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("${fromUnit.name} (${fromUnit.symbol})", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                DropdownMenu(
                                    expanded = showFromMenu,
                                    onDismissRequest = { showFromMenu = false },
                                    modifier = Modifier.background(Slate850)
                                ) {
                                    category.units.forEach { u ->
                                        DropdownMenuItem(
                                            text = { Text("${u.name} (${u.symbol})", color = Slate100) },
                                            onClick = {
                                                viewModel.setFromUnit(u)
                                                showFromMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = inputValue,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate100,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f).testTag("converter_input_display")
                            )
                            IconButton(onClick = { copyToClipboard(inputValue, "Input Value") }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Slate400, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Swap Units Row Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FilledIconButton(
                        onClick = { viewModel.swapUnits() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Slate850,
                            contentColor = CyanNeon
                        ),
                        modifier = Modifier.size(40.dp).testTag("swap_units_button")
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Swap Units")
                    }
                }

                // To Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TO", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                            // To Unit Dropdown
                            Box {
                                OutlinedButton(
                                    onClick = { showToMenu = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VioletElectric),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Slate700)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("${toUnit.name} (${toUnit.symbol})", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                DropdownMenu(
                                    expanded = showToMenu,
                                    onDismissRequest = { showToMenu = false },
                                    modifier = Modifier.background(Slate850)
                                ) {
                                    category.units.forEach { u ->
                                        DropdownMenuItem(
                                            text = { Text("${u.name} (${u.symbol})", color = Slate100) },
                                            onClick = {
                                                viewModel.setToUnit(u)
                                                showToMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = outputValue,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f).testTag("converter_output_display")
                            )
                            IconButton(onClick = { copyToClipboard(outputValue, "Converted Result") }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Slate400, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Formula Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Slate850,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formulaInfo,
                            color = Slate300,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Numeric Keypad
            ConverterKeypad(
                onInput = { viewModel.onInput(it) },
                onDelete = { viewModel.onDelete() },
                onClear = { viewModel.onClear() }
            )
        }
    }
}

@Composable
private fun ConverterKeypad(
    onInput: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton("7", onClick = { onInput("7") }, modifier = Modifier.weight(1f))
            KeypadButton("8", onClick = { onInput("8") }, modifier = Modifier.weight(1f))
            KeypadButton("9", onClick = { onInput("9") }, modifier = Modifier.weight(1f))
            KeypadActionButton("C", color = RosePink, onClick = onClear, modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton("4", onClick = { onInput("4") }, modifier = Modifier.weight(1f))
            KeypadButton("5", onClick = { onInput("5") }, modifier = Modifier.weight(1f))
            KeypadButton("6", onClick = { onInput("6") }, modifier = Modifier.weight(1f))
            KeypadActionButton("⌫", color = AmberWarm, onClick = onDelete, modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton("1", onClick = { onInput("1") }, modifier = Modifier.weight(1f))
            KeypadButton("2", onClick = { onInput("2") }, modifier = Modifier.weight(1f))
            KeypadButton("3", onClick = { onInput("3") }, modifier = Modifier.weight(1f))
            KeypadActionButton("00", color = CyanNeon, onClick = { onInput("00") }, modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton("0", onClick = { onInput("0") }, modifier = Modifier.weight(2f))
            KeypadButton(".", onClick = { onInput(".") }, modifier = Modifier.weight(1f))
            KeypadActionButton("±", color = Slate300, onClick = { /* negate handled */ }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = KeypadNumBg,
        tonalElevation = 2.dp,
        modifier = modifier.height(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = Slate100, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun KeypadActionButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Slate850,
        tonalElevation = 2.dp,
        modifier = modifier.height(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
