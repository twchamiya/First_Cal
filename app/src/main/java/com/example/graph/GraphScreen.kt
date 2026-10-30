package com.example.graph

import android.graphics.Paint
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.MathFormatter
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    viewModel: GraphViewModel,
    modifier: Modifier = Modifier
) {
    val functions by viewModel.functions.collectAsState()
    val xMin by viewModel.xMin.collectAsState()
    val xMax by viewModel.xMax.collectAsState()
    val yMin by viewModel.yMin.collectAsState()
    val yMax by viewModel.yMax.collectAsState()
    val traceX by viewModel.traceX.collectAsState()
    val tablePoints by viewModel.tablePoints.collectAsState()
    val selectedFnForTable by viewModel.selectedFunctionForTable.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newEquationText by remember { mutableStateOf("") }
    var showTableSheet by remember { mutableStateOf(false) }
    var showFunctionsList by remember { mutableStateOf(false) }

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
                    Column {
                        Text(
                            text = "2D Function Grapher",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(
                            text = "X: [${String.format("%.1f", xMin)}, ${String.format("%.1f", xMax)}]  Y: [${String.format("%.1f", yMin)}, ${String.format("%.1f", yMax)}]",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.resetViewport() },
                            modifier = Modifier.testTag("reset_graph_button")
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Center Graph", tint = CyanNeon)
                        }
                        IconButton(
                            onClick = { showFunctionsList = !showFunctionsList },
                            modifier = Modifier.testTag("toggle_functions_list")
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = CyanNeon) {
                                        Text("${functions.count { it.isVisible }}", color = Slate950)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Layers, contentDescription = "Manage Functions", tint = Slate100)
                            }
                        }
                    }
                }

                // Preset Curves Horizontal Row
                val presetScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(presetScrollState)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { viewModel.applyPresetTrig() },
                        label = { Text("Trig [-2π, 2π]", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = { viewModel.applyTemplate("Sine Wave") },
                        label = { Text("+ sin(x)", fontSize = 12.sp) }
                    )
                    AssistChip(
                        onClick = { viewModel.applyTemplate("Tangent") },
                        label = { Text("+ tan(x)", fontSize = 12.sp) }
                    )
                    AssistChip(
                        onClick = { viewModel.applyTemplate("Damped Oscillation") },
                        label = { Text("+ Damped Wave", fontSize = 12.sp) }
                    )
                    AssistChip(
                        onClick = { viewModel.applyTemplate("Gaussian Bell") },
                        label = { Text("+ Bell Curve", fontSize = 12.sp) }
                    )
                    AssistChip(
                        onClick = { viewModel.applyTemplate("Sigmoid") },
                        label = { Text("+ Sigmoid", fontSize = 12.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Graph Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("graph_canvas")
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            if (zoom != 1f) {
                                val normX = centroid.x / size.width
                                val normY = centroid.y / size.height
                                viewModel.zoom(zoom, normX, normY)
                            }
                            if (pan != Offset.Zero) {
                                val normDx = pan.x / size.width
                                val normDy = pan.y / size.height
                                viewModel.pan(normDx, normDy)
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val xWorld = xMin + (offset.x / size.width) * (xMax - xMin)
                                viewModel.setTraceX(xWorld)
                            },
                            onDoubleTap = {
                                viewModel.resetViewport()
                            }
                        )
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val xSpan = xMax - xMin
                val ySpan = yMax - yMin

                fun worldToScreenX(x: Double): Float =
                    ((x - xMin) / xSpan * canvasWidth).toFloat()

                fun worldToScreenY(y: Double): Float =
                    ((1.0 - (y - yMin) / ySpan) * canvasHeight).toFloat()

                // Calculate nice grid steps
                val roughStepX = xSpan / 8.0
                val gridStepX = calculateNiceStep(roughStepX)

                val roughStepY = ySpan / 8.0
                val gridStepY = calculateNiceStep(roughStepY)

                val textPaint = Paint().apply {
                    color = android.graphics.Color.argb(180, 148, 163, 184)
                    textSize = 30f
                    isAntiAlias = true
                }

                // Draw vertical grid lines & labels
                var curX = floor(xMin / gridStepX) * gridStepX
                while (curX <= xMax) {
                    val screenX = worldToScreenX(curX)
                    drawLine(
                        color = Slate800,
                        start = Offset(screenX, 0f),
                        end = Offset(screenX, canvasHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                    if (abs(curX) > 1e-6) {
                        val text = formatGridNumber(curX)
                        val originY = worldToScreenY(0.0).coerceIn(40f, canvasHeight - 20f)
                        drawContext.canvas.nativeCanvas.drawText(
                            text,
                            screenX + 8f,
                            originY - 8f,
                            textPaint
                        )
                    }
                    curX += gridStepX
                }

                // Draw horizontal grid lines & labels
                var curY = floor(yMin / gridStepY) * gridStepY
                while (curY <= yMax) {
                    val screenY = worldToScreenY(curY)
                    drawLine(
                        color = Slate800,
                        start = Offset(0f, screenY),
                        end = Offset(canvasWidth, screenY),
                        strokeWidth = 1.dp.toPx()
                    )
                    if (abs(curY) > 1e-6) {
                        val text = formatGridNumber(curY)
                        val originX = worldToScreenX(0.0).coerceIn(10f, canvasWidth - 80f)
                        drawContext.canvas.nativeCanvas.drawText(
                            text,
                            originX + 8f,
                            screenY - 8f,
                            textPaint
                        )
                    }
                    curY += gridStepY
                }

                // Draw primary Axes (X = 0 and Y = 0)
                val axisX = worldToScreenX(0.0)
                if (axisX in 0f..canvasWidth) {
                    drawLine(
                        color = Slate600,
                        start = Offset(axisX, 0f),
                        end = Offset(axisX, canvasHeight),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                val axisY = worldToScreenY(0.0)
                if (axisY in 0f..canvasHeight) {
                    drawLine(
                        color = Slate600,
                        start = Offset(0f, axisY),
                        end = Offset(canvasWidth, axisY),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Plot Functions
                val samples = 350
                functions.filter { it.isVisible && it.isValid }.forEach { fn ->
                    val path = Path()
                    var isDrawing = false
                    var lastScreenY = 0f

                    for (i in 0..samples) {
                        val screenX = (i.toFloat() / samples) * canvasWidth
                        val worldX = xMin + (screenX / canvasWidth) * xSpan
                        val worldY = viewModel.evaluateAt(fn.equation, worldX)

                        if (worldY != null && !worldY.isNaN() && !worldY.isInfinite()) {
                            val screenY = worldToScreenY(worldY)

                            // Detect asymptotic jump / discontinuity
                            if (isDrawing && abs(screenY - lastScreenY) > canvasHeight * 0.75f) {
                                isDrawing = false
                            }

                            if (!isDrawing) {
                                path.moveTo(screenX, screenY)
                                isDrawing = true
                            } else {
                                path.lineTo(screenX, screenY)
                            }
                            lastScreenY = screenY
                        } else {
                            isDrawing = false
                        }
                    }

                    drawPath(
                        path = path,
                        color = fn.color,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Draw Trace Line and Points if active
                traceX?.let { tx ->
                    val traceScreenX = worldToScreenX(tx)
                    if (traceScreenX in 0f..canvasWidth) {
                        drawLine(
                            color = Slate400.copy(alpha = 0.6f),
                            start = Offset(traceScreenX, 0f),
                            end = Offset(traceScreenX, canvasHeight),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        functions.filter { it.isVisible && it.isValid }.forEach { fn ->
                            val ty = viewModel.evaluateAt(fn.equation, tx)
                            if (ty != null && !ty.isNaN() && !ty.isInfinite()) {
                                val traceScreenY = worldToScreenY(ty)
                                if (traceScreenY in 0f..canvasHeight) {
                                    drawCircle(
                                        color = fn.color,
                                        radius = 6.dp.toPx(),
                                        center = Offset(traceScreenX, traceScreenY)
                                    )
                                    drawCircle(
                                        color = Slate950,
                                        radius = 3.dp.toPx(),
                                        center = Offset(traceScreenX, traceScreenY)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating Trace Tooltip HUD
            traceX?.let { tx ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "x = ${String.format("%.3f", tx)}",
                                color = Slate200,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.setTraceX(null) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close Trace", tint = Slate400, modifier = Modifier.size(16.dp))
                            }
                        }
                        functions.filter { it.isVisible && it.isValid }.forEach { fn ->
                            val ty = viewModel.evaluateAt(fn.equation, tx)
                            val yStr = ty?.let { MathFormatter.format(it) } ?: "undef"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(fn.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "y = $yStr",
                                    color = fn.color,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Floating Controls: Zoom In, Zoom Out, Add Function
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { viewModel.zoom(1.3f) },
                    containerColor = Slate850,
                    contentColor = CyanNeon
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }
                SmallFloatingActionButton(
                    onClick = { viewModel.zoom(0.77f) },
                    containerColor = Slate850,
                    contentColor = CyanNeon
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = CyanNeon,
                    contentColor = Slate950,
                    modifier = Modifier.testTag("add_function_fab")
                ) {
                    Icon(Icons.Default.AddChart, contentDescription = "Add Function")
                }
            }

            // Slide-over Functions List Drawer / Panel
            AnimatedVisibility(
                visible = showFunctionsList,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Plotted Functions (${functions.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            IconButton(onClick = { showFunctionsList = false }) {
                                Icon(Icons.Default.ExpandMore, contentDescription = "Hide", tint = Slate300)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(functions, key = { it.id }) { fn ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Slate850),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(fn.color)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "f(x) = ${fn.equation}",
                                                color = Slate100,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 14.sp
                                            )
                                            if (!fn.isValid) {
                                                Text(
                                                    text = fn.errorMessage ?: "Invalid",
                                                    color = RosePink,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        // Table Button
                                        IconButton(
                                            onClick = {
                                                viewModel.generateTable(fn)
                                                showTableSheet = true
                                            }
                                        ) {
                                            Icon(Icons.Default.TableRows, contentDescription = "View Table", tint = Slate400)
                                        }

                                        // Visibility Eye
                                        IconButton(
                                            onClick = { viewModel.toggleFunctionVisibility(fn.id) }
                                        ) {
                                            Icon(
                                                imageVector = if (fn.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle Visibility",
                                                tint = if (fn.isVisible) fn.color else Slate600
                                            )
                                        }

                                        // Delete Button
                                        IconButton(
                                            onClick = { viewModel.removeFunction(fn.id) }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RosePink)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeonContainer, contentColor = CyanNeon)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Another Function", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Add Function Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newEquationText = ""
            },
            containerColor = Slate900,
            title = { Text("Plot New Function", color = Slate100, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter function f(x):", color = Slate400, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newEquationText,
                        onValueChange = { newEquationText = it },
                        placeholder = { Text("e.g. 2*cos(3*x)", color = Slate600) },
                        prefix = { Text("f(x) = ", color = CyanNeon, fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("function_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEquationText.isNotBlank()) {
                            viewModel.addFunction(newEquationText.trim())
                            newEquationText = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate950)
                ) {
                    Text("Plot", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }

    // Value Table Bottom Sheet
    if (showTableSheet && selectedFnForTable != null) {
        ModalBottomSheet(
            onDismissRequest = { showTableSheet = false },
            containerColor = Slate900
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Values for f(x) = ${selectedFnForTable?.equation}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = selectedFnForTable?.color ?: CyanNeon
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate850, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("x", fontWeight = FontWeight.Bold, color = Slate300, fontFamily = FontFamily.Monospace)
                    Text("f(x)", fontWeight = FontWeight.Bold, color = Slate300, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(tablePoints) { pt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = String.format("%.2f", pt.x),
                                color = Slate200,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = pt.y?.let { MathFormatter.format(it) } ?: "Undefined",
                                color = if (pt.isDefined) CyanNeon else RosePink,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Divider(color = Slate800, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

private fun calculateNiceStep(roughStep: Double): Double {
    val exponent = floor(log10(roughStep))
    val fraction = roughStep / 10.0.pow(exponent)
    val niceFraction = when {
        fraction <= 1.5 -> 1.0
        fraction <= 3.5 -> 2.0
        fraction <= 7.5 -> 5.0
        else -> 10.0
    }
    return niceFraction * 10.0.pow(exponent)
}

private fun formatGridNumber(v: Double): String {
    val absV = abs(v)
    return when {
        absV >= 1000 || (absV < 0.01 && absV > 0) -> String.format("%.1e", v)
        v == v.roundToLong().toDouble() -> v.roundToLong().toString()
        else -> String.format("%.2f", v)
    }
}
