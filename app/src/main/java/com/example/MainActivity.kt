package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
import com.example.ui.WeatherGptViewModel
import com.example.ui.components.WeatherGptTopBar
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class NavigationTab(val label: String, val icon: ImageVector, val testTag: String) {
  HOME("Home", Icons.Filled.Home, "nav_home"),
  CHAT("Chat AI", Icons.Filled.ChatBubble, "nav_chat"),
  MAP("Radar Map", Icons.Filled.Radar, "nav_map"),
  ALERTS("Alerts", Icons.Filled.Warning, "nav_alerts"),
  CLIMATE("Climate", Icons.Filled.ShowChart, "nav_climate")
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      WeatherGptTheme {
        WeatherGptApp()
      }
    }
  }
}

@Composable
fun WeatherGptApp(viewModel: WeatherGptViewModel = viewModel()) {
  var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
  val context = LocalContext.current

  // State flows from ViewModel
  val currentWeather by viewModel.currentWeather.collectAsStateWithLifecycle()
  val hourlyForecast by viewModel.hourlyForecast.collectAsStateWithLifecycle()
  val dailyForecast by viewModel.dailyForecast.collectAsStateWithLifecycle()
  val radarData by viewModel.radarData.collectAsStateWithLifecycle()
  val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
  val sectorMode by viewModel.sectorMode.collectAsStateWithLifecycle()
  val sectorAdvisory by viewModel.sectorAdvisory.collectAsStateWithLifecycle()
  val climateTrends by viewModel.climateTrends.collectAsStateWithLifecycle()
  val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
  val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
  val isDemoMode by viewModel.isDemoMode.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
  val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()

  val isListening by viewModel.voiceManager.isListening.collectAsStateWithLifecycle()
  val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsStateWithLifecycle()

  // Audio permission request launcher
  val audioPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      viewModel.startVoiceInput()
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      WeatherGptTopBar(
        currentLocation = selectedLocation,
        savedLocations = viewModel.savedLocations,
        onLocationSelected = { viewModel.selectLocation(it) },
        onSearchQuery = { viewModel.searchLocation(it) },
        selectedLanguage = selectedLanguage,
        onLanguageSelected = { viewModel.setLanguage(it) },
        isDemoMode = isDemoMode,
        onToggleDemoMode = { viewModel.toggleDemoMode() },
        isSpeaking = isSpeaking,
        onStopSpeaking = { viewModel.voiceManager.stopSpeaking() }
      )
    },
    bottomBar = {
      Surface(
        color = SleekBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        NavigationBar(
          containerColor = SleekBackground,
          tonalElevation = 0.dp,
          modifier = Modifier.testTag("bottom_navigation_bar")
        ) {
          NavigationTab.values().forEach { tab ->
            val isSelected = tab == selectedTab
            NavigationBarItem(
              selected = isSelected,
              onClick = { selectedTab = tab },
              icon = {
                if (tab == NavigationTab.ALERTS && activeAlerts.any { it.severity == com.example.data.model.AlertSeverity.EXTREME || it.severity == com.example.data.model.AlertSeverity.SEVERE }) {
                  BadgedBox(
                    badge = {
                      Badge(
                        containerColor = SleekAlertRed,
                        contentColor = Color.White
                      ) {
                        Text("${activeAlerts.size}", fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                      }
                    }
                  ) {
                    Icon(tab.icon, contentDescription = tab.label)
                  }
                } else {
                  Icon(tab.icon, contentDescription = tab.label)
                }
              },
              label = {
                Text(
                  text = tab.label,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SleekDeepInk,
                selectedTextColor = SleekDeepInk,
                indicatorColor = SleekBlueContainer,
                unselectedIconColor = SleekTextSecondary,
                unselectedTextColor = SleekTextSecondary
              ),
              modifier = Modifier.testTag(tab.testTag)
            )
          }
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
        NavigationTab.HOME -> {
          HomeScreen(
            weather = currentWeather,
            hourlyForecast = hourlyForecast,
            dailyForecast = dailyForecast,
            sectorMode = sectorMode,
            sectorAdvisory = sectorAdvisory,
            onModeSelected = { viewModel.setSectorMode(it) },
            onNavigateToChatWithQuery = { query ->
              selectedTab = NavigationTab.CHAT
              viewModel.sendChatMessage(query)
            },
            onNavigateToMap = { selectedTab = NavigationTab.MAP },
            onNavigateToAlerts = { selectedTab = NavigationTab.ALERTS },
            onNavigateToClimate = { selectedTab = NavigationTab.CLIMATE },
            onRefresh = { viewModel.loadWeatherData() },
            isLoading = isLoading
          )
        }

        NavigationTab.CHAT -> {
          ChatScreen(
            messages = chatMessages,
            onSendMessage = { viewModel.sendChatMessage(it) },
            onStartVoiceInput = {
              val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
              if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                viewModel.startVoiceInput()
              } else {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
              }
            },
            isListening = isListening,
            isAiThinking = isAiThinking,
            onSpeakMessage = { text ->
              viewModel.voiceManager.speak(text, selectedLanguage)
            },
            onClearChat = { viewModel.clearChat() },
            selectedLanguage = selectedLanguage
          )
        }

        NavigationTab.MAP -> {
          MapScreen(
            radarData = radarData,
            currentLocation = selectedLocation,
            currentWeather = currentWeather,
            onRecenter = { viewModel.loadWeatherData() },
            onLocationSelected = { viewModel.selectLocation(it) }
          )
        }

        NavigationTab.ALERTS -> {
          AlertsScreen(
            alerts = activeAlerts,
            onTestNotification = {}
          )
        }

        NavigationTab.CLIMATE -> {
          ClimateScreen(
            climateTrends = climateTrends,
            locationName = selectedLocation.name
          )
        }
      }
    }
  }
}
