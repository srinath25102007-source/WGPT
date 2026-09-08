# WeatherGPT Project Audit (SIH 26068 - MoES / IMD)

## 1. Current Architecture
- **Platform/Environment**: Android Application (Kotlin 2.2.10, Jetpack Compose, AGP 9.1.1, Android SDK 36).
- **Existing Files**: Basic single-activity template (`MainActivity.kt` with sample `Greeting`), default Android green icons, basic Room dependencies declared in catalog, Retrofit/Moshi configured, Firebase AI / Secrets gradle plugin configured.
- **Data Layer**: Empty; no weather services, no database models, no API contracts.
- **AI/LLM Layer**: Not implemented yet; `.env.example` has placeholder `GEMINI_API_KEY`.
- **UI/UX**: Default placeholder "Hello Android" screen with no bottom navigation, no weather cards, and no map visualization.

## 2. Existing Problems
- Lack of real meteorological data integration (IMD, ECMWF/GFS global models, RainViewer radar, MOSDAC satellite interfaces).
- No conversational engine or tool-grounding pipeline for climate queries.
- No multilingual support (Indian regional languages like Tamil, Hindi, Telugu, Kannada, etc.).
- Missing multi-sector intelligence for Farmers, Fishermen/Marine workers, Disaster Management, Aviation, and Climate Researchers.
- No local database caching, saved locations, or offline-first alert storage.

## 3. Missing Integrations
- **IMD & Authoritative Meteorological Layer**: District rainfall, severe weather bulletins, coastal/fishermen alerts, nowcasts.
- **Global High-Resolution Forecast Engine**: Hourly 24h & 7-day forecast, precipitation probability, wind gusts, humidity, pressure, UV index, sunrise/sunset.
- **Real-Time Radar & Satellite Engine**: RainViewer radar tile integration, satellite infrared/cloud observation layer, precipitation intensity scale.
- **Conversational AI Agent (WeatherGPT)**: Gemini API client with system instructions enforcing zero-hallucination meteorological grounding, intent detection, location extraction, and tool execution.
- **Speech-to-Text & Text-to-Speech (STT / TTS)**: Regional voice queries and spoken advisory output.
- **Room Database**: Tables for `chat_sessions`, `chat_messages`, `saved_locations`, `weather_cache`, and `alerts`.
- **Demo Mode**: Explicitly toggled demonstration fixtures for reliable live stage presentations when external network connectivity is restricted.

## 4. Proposed Architecture
```
┌────────────────────────────────────────────────────────────────────────┐
│                   WeatherGPT Jetpack Compose UI                        │
│ ┌──────────────┬──────────────┬──────────────┬──────────────┬────────┐ │
│ │  Chat Screen │ Dashboard UI │  Live Radar  │ Alert Center │Climate │ │
│ │   (STT/TTS)  │(7-Day/Hourly)│   & GIS Map  │ (IMD Hazard) │Trends  │ │
│ └──────┬───────┴──────┬───────┴──────┬───────┴──────┬───────┴────┬───┘ │
└────────┼──────────────┼──────────────┼──────────────┼────────────┼─────┘
         │              │              │              │            │
┌────────▼──────────────▼──────────────▼──────────────▼────────────▼─────┐
│                       WeatherGPT ViewModel                             │
│       (StateFlow, Multi-Sector Modes, Multilingual & Audio)            │
└────────┬─────────────────────────────┬─────────────────────────────────┘
         │                             │
┌────────▼─────────────────────────┐ ┌─▼─────────────────────────────────┐
│ WeatherGPT Grounded AI Engine    │ │ Meteorological Repository         │
│ - Gemini 3.5 / Flash LLM Client  │ │ - IMD Weather & Bulletin Service  │
│ - Zero-Hallucination Tool Calling│ │ - Open-Meteo High-Res Forecast    │
│ - Climate RAG Knowledge Engine   │ │ - RainViewer Real-Time Radar API  │
│ - Sector Advisory Rule Engines   │ │ - Climate Historical Trends Archive│
└──────────────────────────────────┘ └─┬─────────────────────────────────┘
                                       │
┌──────────────────────────────────────▼─────────────────────────────────┐
│               Room Local Database & Cache Architecture                 │
│   (ChatHistoryDao, SavedLocationsDao, WeatherCacheDao, AlertsDao)      │
└────────────────────────────────────────────────────────────────────────┘
```

## 5. Files That Will Be Changed
- `app/build.gradle.kts` (Enable Coil, Location services, fix applicationId)
- `app/src/main/AndroidManifest.xml` (Permissions for Internet, Location, Audio Record, Notifications)
- `app/src/main/java/com/example/MainActivity.kt` (Full WeatherGPT application shell with bottom navigation and dialogs)
- `app/src/main/java/com/example/ui/theme/Color.kt` (Vibrant weather palette: sky blue, cyan, electric blue, alert amber, storm red)
- `app/src/main/java/com/example/ui/theme/Theme.kt` (Material Design 3 clean weather theme)
- `/.env.example` (Uncomment `GEMINI_API_KEY`)

## 6. Files That Will Be Created
- `app/src/main/java/com/example/data/model/WeatherModels.kt`: Normalized meteorological models (WeatherData, HourlyForecast, DailyForecast, WeatherAlert, ClimateTrend, SectorAdvisory).
- `app/src/main/java/com/example/data/db/WeatherDatabase.kt`: Room database and entities (`ChatMessageEntity`, `SavedLocationEntity`, `WeatherCacheEntity`, `WeatherAlertEntity`).
- `app/src/main/java/com/example/data/api/WeatherApiService.kt`: Retrofit interfaces for Open-Meteo, RainViewer Radar API, and IMD/MOSDAC schemas.
- `app/src/main/java/com/example/data/repository/WeatherRepository.kt`: Unified data repository handling caching, offline fallback, and demo mode.
- `app/src/main/java/com/example/ai/WeatherGptEngine.kt`: Grounded LLM reasoning engine with Gemini REST API, tool calling, and RAG knowledge retrieval.
- `app/src/main/java/com/example/voice/VoiceManager.kt`: Native SpeechRecognizer and TextToSpeech integration for regional languages.
- `app/src/main/java/com/example/ui/WeatherGptViewModel.kt`: Centralized state management for chat, dashboard, map, alerts, sector modes, and language.
- `app/src/main/java/com/example/ui/screens/HomeScreen.kt`: Hero dashboard with dynamic weather reaction, quick actions, hourly & 7-day cards.
- `app/src/main/java/com/example/ui/screens/ChatScreen.kt`: Conversational weather interface with weather cards, tool citations, voice button, and suggested prompts.
- `app/src/main/java/com/example/ui/screens/MapScreen.kt`: Interactive GIS radar & satellite map with layer selector, intensity scale, and playback controls.
- `app/src/main/java/com/example/ui/screens/AlertsScreen.kt`: Hazard alert center (Extreme, Severe, Moderate, Info) with IMD bulletins and action recommendations.
- `app/src/main/java/com/example/ui/screens/ClimateScreen.kt`: Historical climate trend analytics, decadal temperature anomalies, and rainfall comparison.
- `app/src/main/java/com/example/ui/components/CommonComponents.kt`: Shared UI elements (Sector mode selector, Language dialog, Weather status chips, Audio pulse).
- Detailed documentation files (`README.md`, `ARCHITECTURE.md`, `API_INTEGRATION.md`, `LLM_SETUP.md`, `RAG_SETUP.md`, `MOSDAC_SETUP.md`, `DEMO_SCRIPT.md`, `TROUBLESHOOTING.md`).
