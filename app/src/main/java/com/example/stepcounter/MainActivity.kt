package com.example.stepcounter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stepcounter.ui.StepsUiState
import com.example.stepcounter.ui.StepsViewModel
import com.example.stepcounter.ui.ThemeMode
import com.example.stepcounter.ui.achievements.AchievementsScreen
import com.example.stepcounter.ui.goals.GoalsScreen
import com.example.stepcounter.ui.home.HomeScreen
import com.example.stepcounter.ui.permission.PermissionScreen
import com.example.stepcounter.ui.profile.ProfileScreen
import com.example.stepcounter.ui.settings.FileEditorScreen
import com.example.stepcounter.ui.settings.SettingsScreen
import com.example.stepcounter.ui.stats.StatsScreen
import com.example.stepcounter.ui.theme.StepCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: StepsViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()

            val darkTheme = when (state.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // Keep system-bar icons legible for the active theme.
            val view = LocalView.current
            SideEffect {
                val window = (view.context as Activity).window
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }

            StepCounterTheme(darkTheme = darkTheme) {
                StepCounterApp(viewModel = viewModel, state = state)
            }
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("Главная", Icons.Filled.Home),
    Stats("Статистика", Icons.Filled.BarChart),
    Goals("Цели", Icons.Filled.TrackChanges),
    Awards("Награды", Icons.Filled.EmojiEvents),
    Profile("Профиль", Icons.Filled.Person)
}

@Composable
private fun StepCounterApp(viewModel: StepsViewModel, state: StepsUiState) {
    val context = LocalContext.current

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequestedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        permissionRequestedOnce = true
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, permissionGranted) {
        val observer = LifecycleEventObserver { _, event ->
            when {
                event == Lifecycle.Event.ON_START && permissionGranted -> viewModel.start()
                event == Lifecycle.Event.ON_STOP -> viewModel.stop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!permissionGranted) {
        val activity = context as? Activity
        val permanentlyDenied = permissionRequestedOnce && activity != null &&
            !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION)
        PermissionScreen(
            permanentlyDenied = permanentlyDenied,
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            },
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
        return
    }

    var showSettings by remember { mutableStateOf(false) }
    var showEditor by remember { mutableStateOf(false) }

    if (showEditor) {
        FileEditorScreen(
            filePath = viewModel.statsFilePath,
            loadContent = viewModel::readRawFile,
            onSave = viewModel::saveRawFile,
            onBack = { showEditor = false }
        )
        return
    }

    if (showSettings) {
        SettingsScreen(
            themeMode = state.themeMode,
            unitsMetric = state.unitsMetric,
            strideCm = state.strideCm,
            onThemeMode = viewModel::setThemeMode,
            onUnitsMetric = viewModel::setUnitsMetric,
            onStrideCm = viewModel::setStrideCm,
            onReset = viewModel::resetHistory,
            onOpenEditor = { showEditor = true },
            onBack = { showSettings = false }
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = Tab.entries

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        when (tabs[selectedTab]) {
            Tab.Home -> HomeScreen(state = state, modifier = screenModifier)
            Tab.Stats -> StatsScreen(state = state, modifier = screenModifier)
            Tab.Goals -> GoalsScreen(
                state = state,
                onStepGoal = viewModel::setStepGoal,
                onActiveGoal = viewModel::setActiveGoal,
                onCalorieGoal = viewModel::setCalorieGoal,
                modifier = screenModifier
            )
            Tab.Awards -> AchievementsScreen(state = state, modifier = screenModifier)
            Tab.Profile -> ProfileScreen(
                state = state,
                onNameChange = viewModel::setUserName,
                onOpenSettings = { showSettings = true },
                modifier = screenModifier
            )
        }
    }
}
