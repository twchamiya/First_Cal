package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.CalculatorScreen
import com.example.calculator.CalculatorViewModel
import com.example.converter.UnitConverterScreen
import com.example.converter.UnitConverterViewModel
import com.example.graph.GraphScreen
import com.example.graph.GraphViewModel
import com.example.trig.TrigLabScreen
import com.example.trig.TrigLabViewModel
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

sealed class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    object Calculator : NavItem("Calculator", Icons.Filled.Calculate, Icons.Outlined.Calculate, "nav_calculator")
    object Grapher : NavItem("Grapher", Icons.Filled.ShowChart, Icons.Outlined.ShowChart, "nav_grapher")
    object Converter : NavItem("Converter", Icons.Filled.CompareArrows, Icons.Outlined.CompareArrows, "nav_converter")
    object TrigStudio : NavItem("Trig Studio", Icons.Filled.Timeline, Icons.Outlined.Timeline, "nav_trig_studio")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    var currentItem by remember { mutableStateOf<NavItem>(NavItem.Calculator) }

    val calculatorViewModel: CalculatorViewModel = viewModel()
    val graphViewModel: GraphViewModel = viewModel()
    val unitConverterViewModel: UnitConverterViewModel = viewModel()
    val trigLabViewModel: TrigLabViewModel = viewModel()

    // Handle back button on secondary screens
    if (currentItem != NavItem.Calculator) {
        BackHandler {
            currentItem = NavItem.Calculator
        }
    }

    val navItems = listOf(
        NavItem.Calculator,
        NavItem.Grapher,
        NavItem.Converter,
        NavItem.TrigStudio
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Slate950,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                tonalElevation = 6.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                navItems.forEach { item ->
                    val selected = currentItem == item
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentItem = item },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate950,
                            selectedTextColor = CyanNeon,
                            indicatorColor = CyanNeon,
                            unselectedIconColor = Slate850,
                            unselectedTextColor = Slate850
                        ),
                        modifier = Modifier.testTag(item.testTag)
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
            when (currentItem) {
                is NavItem.Calculator -> CalculatorScreen(calculatorViewModel)
                is NavItem.Grapher -> GraphScreen(graphViewModel)
                is NavItem.Converter -> UnitConverterScreen(unitConverterViewModel)
                is NavItem.TrigStudio -> TrigLabScreen(trigLabViewModel)
            }
        }
    }
}
