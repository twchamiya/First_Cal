package com.example.trig

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.AdvancedTrig
import com.example.math.AngleUnit
import com.example.math.MathFormatter
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrigLabScreen(
    viewModel: TrigLabViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Unit Circle, 1: Triangle Solver, 2: Identities

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
                        text = "Trigonometry Studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Slate900,
                    contentColor = CyanNeon,
                    divider = { Divider(color = Slate800) }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Unit Circle", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Adjust, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Triangle Solver", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.ChangeHistory, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Identities", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) }
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
            when (selectedTab) {
                0 -> UnitCircleTab(viewModel)
                1 -> TriangleSolverTab(viewModel)
                2 -> TrigIdentitiesTab()
            }
        }
    }
}

@Composable
private fun UnitCircleTab(viewModel: TrigLabViewModel) {
    val angleDeg by viewModel.angleDegrees.collectAsState()
    val rad = angleDeg * PI / 180.0
    val sinVal = sin(rad)
    val cosVal = cos(rad)
    val tanVal = if (abs(cosVal) < 1e-12) null else tan(rad)
    val exactValues = viewModel.getExactRadicalTrig(angleDeg)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Interactive Unit Circle Canvas
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Slate900)
                .testTag("unit_circle_box"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touch = change.position
                            val dx = touch.x - center.x
                            val dy = center.y - touch.y // inverted Y
                            var angle = atan2(dy, dx) * 180.0 / PI
                            if (angle < 0) angle += 360.0
                            viewModel.setAngleDegrees(angle)
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width * 0.38f

                // Axes
                drawLine(
                    color = Slate700,
                    start = Offset(16.dp.toPx(), center.y),
                    end = Offset(size.width - 16.dp.toPx(), center.y),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawLine(
                    color = Slate700,
                    start = Offset(center.x, 16.dp.toPx()),
                    end = Offset(center.x, size.height - 16.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Circle
                drawCircle(
                    color = Slate600,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                val pointX = center.x + radius * cosVal.toFloat()
                val pointY = center.y - radius * sinVal.toFloat()

                // Cosine projection (horizontal cyan)
                drawLine(
                    color = CyanNeon,
                    start = center,
                    end = Offset(pointX, center.y),
                    strokeWidth = 3.dp.toPx()
                )

                // Sine projection (vertical emerald)
                drawLine(
                    color = EmeraldBright,
                    start = Offset(pointX, center.y),
                    end = Offset(pointX, pointY),
                    strokeWidth = 3.dp.toPx()
                )

                // Radius arm
                drawLine(
                    color = Slate100,
                    start = center,
                    end = Offset(pointX, pointY),
                    strokeWidth = 2.dp.toPx()
                )

                // Circle point marker
                drawCircle(
                    color = VioletElectric,
                    radius = 7.dp.toPx(),
                    center = Offset(pointX, pointY)
                )
                drawCircle(
                    color = Slate950,
                    radius = 3.dp.toPx(),
                    center = Offset(pointX, pointY)
                )
            }
        }

        // Angle Slider & Quick Angle Presets
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Angle: ${String.format("%.1f", angleDeg)}°",
                    color = Slate100,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${String.format("%.3f", rad)} rad",
                    color = CyanNeon,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }

            Slider(
                value = angleDeg.toFloat(),
                onValueChange = { viewModel.setAngleDegrees(it.toDouble()) },
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(
                    thumbColor = CyanNeon,
                    activeTrackColor = CyanNeon,
                    inactiveTrackColor = Slate800
                ),
                modifier = Modifier.fillMaxWidth().testTag("angle_slider")
            )

            // Preset Angle Chips
            val chipScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(chipScrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0.0, 30.0, 45.0, 60.0, 90.0, 120.0, 135.0, 180.0, 270.0, 360.0).forEach { deg ->
                    FilterChip(
                        selected = abs(angleDeg - deg) < 0.5,
                        onClick = { viewModel.setAngleDegrees(deg) },
                        label = { Text("${deg.toInt()}°", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletContainer,
                            selectedLabelColor = VioletElectric,
                            containerColor = Slate900,
                            labelColor = Slate300
                        )
                    )
                }
            }
        }

        // Exact Radicals Card (if available)
        if (exactValues != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VioletContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VioletElectric, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Exact Algebraic Values", color = VioletElectric, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(exactValues, color = Slate100, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    }
                }
            }
        }

        // Trigonometric Values 6-Grid Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Function Evaluations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate300
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    TrigValueCell(name = "sin(θ)", value = sinVal, color = EmeraldBright, modifier = Modifier.weight(1f))
                    TrigValueCell(name = "cos(θ)", value = cosVal, color = CyanNeon, modifier = Modifier.weight(1f))
                    TrigValueCell(name = "tan(θ)", value = tanVal, color = VioletElectric, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    val secVal = if (abs(cosVal) < 1e-12) null else 1.0 / cosVal
                    val cscVal = if (abs(sinVal) < 1e-12) null else 1.0 / sinVal
                    val cotVal = if (abs(sinVal) < 1e-12) null else cosVal / sinVal

                    TrigValueCell(name = "csc(θ)", value = cscVal, color = Slate200, modifier = Modifier.weight(1f))
                    TrigValueCell(name = "sec(θ)", value = secVal, color = Slate200, modifier = Modifier.weight(1f))
                    TrigValueCell(name = "cot(θ)", value = cotVal, color = Slate200, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TrigValueCell(
    name: String,
    value: Double?,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(name, color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value?.let { MathFormatter.format(it) } ?: "Undefined",
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TriangleSolverTab(viewModel: TrigLabViewModel) {
    val a by viewModel.sideAInput.collectAsState()
    val b by viewModel.sideBInput.collectAsState()
    val c by viewModel.sideCInput.collectAsState()
    val angA by viewModel.angleAInput.collectAsState()
    val angB by viewModel.angleBInput.collectAsState()
    val angC by viewModel.angleCInput.collectAsState()
    val solution by viewModel.triangleSolution.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Triangle Parameter Inputs",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate200
                )
                Text(
                    text = "Provide any 3 values (e.g. 3 sides SSS, 2 sides & included angle SAS, or 2 angles & 1 side ASA)",
                    color = Slate400,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sides
                Text("Sides", color = CyanNeon, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = a,
                        onValueChange = {
                            viewModel.sideAInput.value = it
                            viewModel.solveTriangle()
                        },
                        label = { Text("Side a") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = b,
                        onValueChange = {
                            viewModel.sideBInput.value = it
                            viewModel.solveTriangle()
                        },
                        label = { Text("Side b") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = c,
                        onValueChange = {
                            viewModel.sideCInput.value = it
                            viewModel.solveTriangle()
                        },
                        label = { Text("Side c") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Angles
                Text("Angles (° Degrees)", color = VioletElectric, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = angA,
                        onValueChange = {
                            viewModel.angleAInput.value = it
                            viewModel.solveTriangle()
                        },
                        label = { Text("Angle A") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = angB,
                        onValueChange = {
                            viewModel.angleBInput.value = it
                            viewModel.solveTriangle()
                        },
                        label = { Text("Angle B") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = angC,
                        onValueChange = {
                            viewModel.angleCInput.value = it
                            viewModel.solveTriangle()
                        },
                        label = { Text("Angle C") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Solution Output Card
        if (solution != null) {
            val sol = solution!!
            if (sol.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = KeypadActionBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = sol.error,
                        color = RosePink,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 13.sp
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Triangle Solution",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldBright
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Slate850
                            ) {
                                Text(
                                    text = sol.method,
                                    color = CyanNeon,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            TriangleSolutionMetric("Side a", String.format("%.3f", sol.sideA), Modifier.weight(1f))
                            TriangleSolutionMetric("Side b", String.format("%.3f", sol.sideB), Modifier.weight(1f))
                            TriangleSolutionMetric("Side c", String.format("%.3f", sol.sideC), Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TriangleSolutionMetric("Angle A", String.format("%.2f°", sol.angleA), Modifier.weight(1f))
                            TriangleSolutionMetric("Angle B", String.format("%.2f°", sol.angleB), Modifier.weight(1f))
                            TriangleSolutionMetric("Angle C", String.format("%.2f°", sol.angleC), Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TriangleSolutionMetric("Area", String.format("%.3f", sol.area), Modifier.weight(1f))
                            TriangleSolutionMetric("Perimeter", String.format("%.3f", sol.perimeter), Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TriangleSolutionMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(4.dp)) {
        Text(label, color = Slate400, fontSize = 11.sp)
        Text(value, color = Slate100, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun TrigIdentitiesTab() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IdentitySection(
            title = "Pythagorean Identities",
            formulas = listOf(
                "sin²(θ) + cos²(θ) = 1",
                "1 + tan²(θ) = sec²(θ)",
                "1 + cot²(θ) = csc²(θ)"
            )
        )
        IdentitySection(
            title = "Reciprocal & Quotient",
            formulas = listOf(
                "tan(θ) = sin(θ) / cos(θ)",
                "cot(θ) = cos(θ) / sin(θ) = 1 / tan(θ)",
                "sec(θ) = 1 / cos(θ)",
                "csc(θ) = 1 / sin(θ)"
            )
        )
        IdentitySection(
            title = "Double-Angle Formulas",
            formulas = listOf(
                "sin(2θ) = 2 · sin(θ) · cos(θ)",
                "cos(2θ) = cos²(θ) - sin²(θ) = 2cos²(θ) - 1",
                "tan(2θ) = (2 · tan(θ)) / (1 - tan²(θ))"
            )
        )
        IdentitySection(
            title = "Hyperbolic Relationships",
            formulas = listOf(
                "cosh²(x) - sinh²(x) = 1",
                "tanh(x) = sinh(x) / cosh(x)",
                "1 - tanh²(x) = sech²(x)",
                "sinh(2x) = 2 · sinh(x) · cosh(x)"
            )
        )
    }
}

@Composable
private fun IdentitySection(title: String, formulas: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = CyanNeon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            formulas.forEach { formula ->
                Text(
                    text = "• $formula",
                    color = Slate200,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
