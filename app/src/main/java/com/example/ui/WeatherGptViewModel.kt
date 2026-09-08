package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.WeatherGptEngine
import com.example.data.db.ChatMessageEntity
import com.example.data.db.SavedLocationEntity
import com.example.data.model.*
import com.example.data.repository.WeatherRepository
import com.example.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherGptViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = WeatherRepository(application)
  private val gptEngine = WeatherGptEngine()
  val voiceManager = VoiceManager(application)

  private val _currentWeather = MutableStateFlow<NormalizedWeather?>(null)
  val currentWeather: StateFlow<NormalizedWeather?> = _currentWeather.asStateFlow()

  private val _hourlyForecast = MutableStateFlow<List<HourlyForecastItem>>(emptyList())
  val hourlyForecast: StateFlow<List<HourlyForecastItem>> = _hourlyForecast.asStateFlow()

  private val _dailyForecast = MutableStateFlow<List<DailyForecastItem>>(emptyList())
  val dailyForecast: StateFlow<List<DailyForecastItem>> = _dailyForecast.asStateFlow()

  private val _radarData = MutableStateFlow<RadarData?>(null)
  val radarData: StateFlow<RadarData?> = _radarData.asStateFlow()

  private val _activeAlerts = MutableStateFlow<List<WeatherAlert>>(emptyList())
  val activeAlerts: StateFlow<List<WeatherAlert>> = _activeAlerts.asStateFlow()

  private val _sectorMode = MutableStateFlow(SectorMode.PERSONAL)
  val sectorMode: StateFlow<SectorMode> = _sectorMode.asStateFlow()

  private val _sectorAdvisory = MutableStateFlow<SectorAdvisory?>(null)
  val sectorAdvisory: StateFlow<SectorAdvisory?> = _sectorAdvisory.asStateFlow()

  private val _climateTrends = MutableStateFlow<List<ClimateTrendPoint>>(emptyList())
  val climateTrends: StateFlow<List<ClimateTrendPoint>> = _climateTrends.asStateFlow()

  private val _selectedLanguage = MutableStateFlow(SupportedLanguage.ENGLISH)
  val selectedLanguage: StateFlow<SupportedLanguage> = _selectedLanguage.asStateFlow()

  private val _isDemoMode = MutableStateFlow(false)
  val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _isAiThinking = MutableStateFlow(false)
  val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _selectedLocation = MutableStateFlow(repository.currentLocation)
  val selectedLocation: StateFlow<SavedLocationEntity> = _selectedLocation.asStateFlow()

  val savedLocations = repository.defaultLocations

  private val _chatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
  val chatMessages: StateFlow<List<ChatMessageEntity>> = _chatMessages.asStateFlow()

  init {
    loadWeatherData()
    loadChatMessages()
  }

  fun loadWeatherData() {
    viewModelScope.launch {
      _isLoading.value = true
      _errorMessage.value = null
      try {
        val loc = _selectedLocation.value
        val weather = repository.getNormalizedWeather(loc.latitude, loc.longitude, loc.name, loc.state)
        _currentWeather.value = weather

        val hourly = repository.getHourlyForecast(loc.latitude, loc.longitude)
        _hourlyForecast.value = hourly

        val daily = repository.getDailyForecast(loc.latitude, loc.longitude)
        _dailyForecast.value = daily

        val alerts = repository.getActiveAlerts(loc.state)
        _activeAlerts.value = alerts

        val advisory = repository.generateSectorAdvisory(_sectorMode.value, weather)
        _sectorAdvisory.value = advisory

        val radar = repository.getRadarData()
        _radarData.value = radar

        val trends = repository.getClimateTrends(loc.name)
        _climateTrends.value = trends
      } catch (e: Exception) {
        _errorMessage.value = "Weather sync error: ${e.localizedMessage ?: "Network issue"}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun setSectorMode(mode: SectorMode) {
    _sectorMode.value = mode
    _currentWeather.value?.let { weather ->
      _sectorAdvisory.value = repository.generateSectorAdvisory(mode, weather)
    }
  }

  fun setLanguage(language: SupportedLanguage) {
    _selectedLanguage.value = language
  }

  fun toggleDemoMode() {
    val newMode = !_isDemoMode.value
    _isDemoMode.value = newMode
    repository.isDemoMode = newMode
    loadWeatherData()
  }

  fun selectLocation(location: SavedLocationEntity) {
    _selectedLocation.value = location
    repository.currentLocation = location
    loadWeatherData()
  }

  fun searchLocation(query: String) {
    val found = repository.defaultLocations.firstOrNull {
      it.name.contains(query, ignoreCase = true) || it.state.contains(query, ignoreCase = true)
    }
    if (found != null) {
      selectLocation(found)
    } else {
      // Create ad-hoc location
      val newLoc = SavedLocationEntity(
        name = query.replaceFirstChar { it.uppercase() },
        state = "India",
        latitude = 13.0827,
        longitude = 80.2707,
        category = "Custom"
      )
      selectLocation(newLoc)
    }
  }

  private fun loadChatMessages() {
    viewModelScope.launch {
      repository.getChatMessages("default_session").collect { msgs ->
        if (msgs.isEmpty()) {
          // Initialize with friendly welcome greeting
          val welcome = ChatMessageEntity(
            sender = "weathergpt",
            message = "Hello! I am WeatherGPT, your grounded AI weather intelligence assistant powered by IMD datasets, Doppler radar, and high-resolution models.\n\nAsk me anything like:\n• \"Will it rain today in Chennai?\"\n• \"Can fishermen go to sea tomorrow?\"\n• \"Is it safe to spray pesticides tomorrow?\"\n• \"Show me active IMD warnings.\"\n• Or ask in Tamil: \"நாளைக்கு மழை வருமா?\"",
            sourceAttribution = "India Meteorological Dept / WeatherGPT",
            timestamp = System.currentTimeMillis()
          )
          _chatMessages.value = listOf(welcome)
        } else {
          _chatMessages.value = msgs
        }
      }
    }
  }

  fun sendChatMessage(query: String) {
    if (query.isBlank()) return

    viewModelScope.launch {
      val userMsg = ChatMessageEntity(
        sender = "user",
        message = query,
        timestamp = System.currentTimeMillis()
      )
      repository.saveChatMessage(userMsg)

      _isAiThinking.value = true
      val weather = _currentWeather.value ?: repository.getNormalizedWeather(
        _selectedLocation.value.latitude,
        _selectedLocation.value.longitude,
        _selectedLocation.value.name,
        _selectedLocation.value.state
      )
      val alerts = _activeAlerts.value
      val advisory = _sectorAdvisory.value ?: repository.generateSectorAdvisory(_sectorMode.value, weather)

      val (response, source) = gptEngine.askWeatherGpt(
        query = query,
        currentWeather = weather,
        activeAlerts = alerts,
        sectorMode = _sectorMode.value,
        sectorAdvisory = advisory,
        language = _selectedLanguage.value
      )

      val botMsg = ChatMessageEntity(
        sender = "weathergpt",
        message = response,
        sourceAttribution = source,
        timestamp = System.currentTimeMillis()
      )
      repository.saveChatMessage(botMsg)
      _isAiThinking.value = false

      // Optional auto voice read
      voiceManager.speak(response, _selectedLanguage.value)
    }
  }

  fun startVoiceInput() {
    voiceManager.startListening(_selectedLanguage.value) { result ->
      sendChatMessage(result)
    }
  }

  fun clearChat() {
    viewModelScope.launch {
      repository.clearChatHistory("default_session")
      loadChatMessages()
    }
  }

  override fun onCleared() {
    super.onCleared()
    voiceManager.destroy()
  }
}
