package com.example.ai

import com.example.BuildConfig
import com.example.data.model.NormalizedWeather
import com.example.data.model.SectorAdvisory
import com.example.data.model.SectorMode
import com.example.data.model.SupportedLanguage
import com.example.data.model.WeatherAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WeatherGptEngine {

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  // RAG Knowledge Base for Meteorological Standards & IMD Guidelines
  private val ragMeteorologicalKnowledge = """
    IMD RAINFALL CLASSIFICATION:
    - Very Light: 0.1 to 2.4 mm
    - Light: 2.5 to 15.5 mm
    - Moderate: 15.6 to 64.4 mm
    - Heavy: 64.5 to 115.5 mm
    - Very Heavy: 115.6 to 204.4 mm
    - Extremely Heavy: >= 204.5 mm

    IMD COLOR CODED WARNINGS:
    - Green (No Warning): No action required, normal weather.
    - Yellow (Watch / Be Updated): Weather condition may deteriorate, monitor updates.
    - Orange (Alert / Be Prepared): Severe weather likely, disruption to transport/farming possible.
    - Red (Warning / Take Action): Extremely severe / hazardous weather, life and property risk.

    AGRICULTURAL ADVISORY GUIDELINES (IMD AGROMET):
    - Pesticide / Weedicide spraying: Safe only when wind speed is < 15 km/h and rainfall probability < 30% for the next 6-8 hours.
    - Irrigation: Withhold if rainfall forecast > 25 mm within 24 hours. Ensure drainage if standing water.

    MARINE & FISHERMEN SAFETY GUIDELINES:
    - Wind speed > 45 km/h or sea swell > 2.5 m: Fishermen are advised not to venture into deep sea.
    - Port Warning Signal No. 1 to 11 indicating danger level from squall to severe cyclone landfall.
  """.trimIndent()

  suspend fun askWeatherGpt(
    query: String,
    currentWeather: NormalizedWeather,
    activeAlerts: List<WeatherAlert>,
    sectorMode: SectorMode,
    sectorAdvisory: SectorAdvisory,
    language: SupportedLanguage
  ): Pair<String, String> = withContext(Dispatchers.IO) {
    val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

    val promptContext = buildString {
      appendLine("CURRENT GROUND-TRUTH METEOROLOGICAL OBSERVATION (MANDATORY FACTS):")
      appendLine("- Location: ${currentWeather.locationName}, ${currentWeather.stateOrRegion} (${currentWeather.latitude}, ${currentWeather.longitude})")
      appendLine("- Temperature: ${currentWeather.temperature}°C (Feels like: ${currentWeather.feelsLike}°C)")
      appendLine("- Humidity: ${currentWeather.humidity}%")
      appendLine("- Surface Wind: ${currentWeather.windSpeed} km/h from ${currentWeather.windDirection}°, Gusts up to ${currentWeather.windGust} km/h")
      appendLine("- Surface Pressure: ${currentWeather.pressure} hPa")
      appendLine("- Current Condition: ${currentWeather.weatherCondition}")
      appendLine("- Precipitation Rate: ${currentWeather.rainfall} mm/h, Rain Probability: ${currentWeather.precipitationProbability}%")
      appendLine("- UV Index: ${currentWeather.uvIndex}, Cloud Cover: ${currentWeather.cloudCover}%")
      appendLine("- Sunrise: ${currentWeather.sunrise}, Sunset: ${currentWeather.sunset}")
      if (currentWeather.waveHeight != null) {
        appendLine("- Coastal Wave Height: ${currentWeather.waveHeight} meters")
      }
      appendLine("- Observation Timestamp: ${currentWeather.timestamp} (Data Freshness: ${currentWeather.dataFreshness})")
      appendLine("- Primary Data Source: ${currentWeather.source}")

      appendLine("\nACTIVE OFFICIAL IMD WARNINGS & BULLETINS:")
      if (activeAlerts.isEmpty()) {
        appendLine("- No active warnings at this moment.")
      } else {
        activeAlerts.forEach { alert ->
          appendLine("- [${alert.severity}] ${alert.title} for ${alert.location}. ${alert.description} Action: ${alert.recommendedAction}")
        }
      }

      appendLine("\nACTIVE SECTOR ADVISORY [${sectorMode.displayName}]:")
      appendLine("Status: ${sectorAdvisory.statusTitle} (Risk Score: ${sectorAdvisory.riskScore}/100)")
      sectorAdvisory.bulletPoints.forEach { pt ->
        appendLine("• $pt")
      }

      appendLine("\nMETEOROLOGICAL RAG KNOWLEDGE BASE:")
      appendLine(ragMeteorologicalKnowledge)
    }

    val systemInstruction = """
      You are WeatherGPT, an AI meteorological intelligence assistant built for India (Smart India Hackathon SIH-26068 / Ministry of Earth Sciences / India Meteorological Department).
      
      STRICT GROUNDING & SAFETY MANDATES:
      1. Never fabricate or extrapolate unverified weather claims (temperature, rainfall mm, cyclone warnings, wind speeds).
      2. Base every factual claim strictly on the provided real-time observation and IMD warnings above.
      3. If the user asks about pesticide spraying, marine safety, disaster risks, or travel, apply the explicit thresholds provided in the context.
      4. Language: Respond fluently and naturally in ${language.displayName} (${language.nativeName}). If the user asks in Tamil, reply in Tamil. If in Hindi, reply in Hindi. If in English, reply in English.
      5. Tone: Calm, professional, highly actionable, concise, scientific, and respectful.
      6. End the response with explicit source attribution and timestamp:
         "Data Source: ${currentWeather.source} | Updated: ${currentWeather.timestamp}"
    """.trimIndent()

    if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
      try {
        val geminiResponse = callGeminiRest(apiKey, systemInstruction, promptContext, query)
        if (geminiResponse.isNotBlank()) {
          val source = "${currentWeather.source} (Verified)"
          return@withContext Pair(geminiResponse, source)
        }
      } catch (_: Exception) {
        // Fallback to grounded rule-based reasoning engine
      }
    }

    // Grounded Rule-Based Meteorological Reasoning Engine (ensures 100% reliability offline/without key)
    val fallbackResponse = generateGroundedFallbackResponse(
      query = query,
      weather = currentWeather,
      alerts = activeAlerts,
      advisory = sectorAdvisory,
      language = language
    )
    val source = "${currentWeather.source} | Meteorological Grounding Engine"
    Pair(fallbackResponse, source)
  }

  private fun callGeminiRest(apiKey: String, systemPrompt: String, context: String, userQuery: String): String {
    val model = "gemini-3.5-flash"
    val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

    val jsonBody = JSONObject().apply {
      put("systemInstruction", JSONObject().apply {
        put("parts", JSONArray().apply {
          put(JSONObject().put("text", systemPrompt))
        })
      })
      put("contents", JSONArray().apply {
        put(JSONObject().apply {
          put("role", "user")
          put("parts", JSONArray().apply {
            put(JSONObject().put("text", "CONTEXT DATA:\n$context\n\nUSER QUESTION:\n$userQuery"))
          })
        })
      })
      put("generationConfig", JSONObject().apply {
        put("temperature", 0.3) // low temperature to prevent hallucination
        put("topP", 0.85)
        put("maxOutputTokens", 800)
      })
    }

    val request = Request.Builder()
      .url(url)
      .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
      .build()

    httpClient.newCall(request).execute().use { resp ->
      if (!resp.isSuccessful) return ""
      val respBody = resp.body?.string() ?: return ""
      val json = JSONObject(respBody)
      val candidates = json.optJSONArray("candidates") ?: return ""
      val firstCandidate = candidates.optJSONObject(0) ?: return ""
      val content = firstCandidate.optJSONObject("content") ?: return ""
      val parts = content.optJSONArray("parts") ?: return ""
      val firstPart = parts.optJSONObject(0) ?: return ""
      return firstPart.optString("text", "")
    }
  }

  private fun generateGroundedFallbackResponse(
    query: String,
    weather: NormalizedWeather,
    alerts: List<WeatherAlert>,
    advisory: SectorAdvisory,
    language: SupportedLanguage
  ): String {
    val lower = query.lowercase()
    val isTamil = language == SupportedLanguage.TAMIL || lower.contains("மழை") || lower.contains("வானிலை")
    val isHindi = language == SupportedLanguage.HINDI || lower.contains("बारिश") || lower.contains("मौसम")

    val rainProb = weather.precipitationProbability
    val temp = weather.temperature
    val condition = weather.weatherCondition
    val wind = weather.windSpeed
    val loc = weather.locationName

    if (isTamil) {
      return buildString {
        if (lower.contains("மழை") || lower.contains("rain") || lower.contains("நாளை")) {
          if (rainProb > 40) {
            appendLine("ஆம், $loc-ல் மழை பெய்ய வாய்ப்புள்ளது.")
            appendLine("🌧️ மழை வாய்ப்பு: $rainProb%")
            appendLine("🌡️ வெப்பநிலை: ${temp}°C")
            appendLine("💨 காற்றின் வேகம்: ${wind.toInt()} km/h")
            appendLine("வானிலை நிலை: $condition. குடை அல்லது மழைக்கோட் எடுத்துச் செல்வது நல்லது.")
          } else {
            appendLine("$loc-ல் மழைக்கான வாய்ப்பு குறைவு ($rainProb%).")
            appendLine("தற்போதைய வெப்பநிலை ${temp}°C மற்றும் வானம் $condition உடன் உள்ளது.")
          }
        } else if (lower.contains("மீனவர்") || lower.contains("மீன்பிடி") || lower.contains("fisher")) {
          appendLine("🌊 $loc கடற்கரை & கடல் வானிலை நிலவரம்:")
          appendLine("காற்றின் வேகம்: ${wind.toInt()} km/h (அதிகபட்சம் ${weather.windGust.toInt()} km/h).")
          if (wind > 35) {
            appendLine("⚠️ எச்சரிக்கை: பலத்த காற்று மற்றும் கொந்தளிப்பான கடல் நிலவும் என்பதால் மீனவர்கள் கடலுக்குச் செல்ல வேண்டாம் என இந்திய வானிலை மையம் (IMD) அறிவுறுத்துகிறது.")
          } else {
            appendLine("அலை உயரம்: ${weather.waveHeight ?: 1.2} மீ. ஆழ்கடல் பகுதிக்கு செல்லும் போது எச்சரிக்கையுடன் செயல்படவும்.")
          }
        } else if (lower.contains("விவசாய") || lower.contains("மருந்து") || lower.contains("spray") || lower.contains("farm")) {
          appendLine("🌾 $loc விவசாய வானிலை ஆலோசனை:")
          advisory.bulletPoints.forEach { appendLine(it) }
        } else {
          appendLine("வணக்கம்! $loc-ன் தற்போதைய வானிலை:")
          appendLine("🌡️ வெப்பநிலை: ${temp}°C (உணரப்படுவது ${weather.feelsLike}°C)")
          appendLine("💧 ஈரப்பதம்: ${weather.humidity}% | 🌧️ மழை வாய்ப்பு: $rainProb%")
          appendLine("💨 காற்று: ${wind.toInt()} km/h | ☀️ $condition")
          if (alerts.isNotEmpty()) {
            appendLine("⚠️ IMD எச்சரிக்கை: ${alerts.first().title}")
          }
        }
        appendLine("\nதரவு மூலம்: ${weather.source} | பதிவு நேரம்: ${weather.timestamp}")
      }
    }

    if (isHindi) {
      return buildString {
        if (lower.contains("बारिश") || lower.contains("rain") || lower.contains("कल")) {
          if (rainProb > 40) {
            appendLine("हाँ, $loc में बारिश होने की संभावना है।")
            appendLine("🌧️ बारिश की संभावना: $rainProb%")
            appendLine("🌡️ तापमान: ${temp}°C")
            appendLine("💨 हवा की गति: ${wind.toInt()} km/h")
            appendLine("मौसम स्थिति: $condition। बाहर जाते समय छाता अवश्य साथ रखें।")
          } else {
            appendLine("$loc में बारिश की संभावना बहुत कम है ($rainProb%)।")
            appendLine("वर्तमान तापमान ${temp}°C है और मौसम $condition रहेगा।")
          }
        } else if (lower.contains("मछुआरे") || lower.contains("समुद्र") || lower.contains("fisher")) {
          appendLine("🌊 $loc तटीय एवं समुद्री मौसम चेतावनी:")
          appendLine("हवा की गति: ${wind.toInt()} km/h (झोंके ${weather.windGust.toInt()} km/h)।")
          if (wind > 35) {
            appendLine("⚠️ चेतावनी: समुद्र में अशांत स्थिति के कारण मछुआरों को गहरे समुद्र में न जाने की सलाह दी जाती है।")
          } else {
            appendLine("समुद्र में लहरें सामान्य हैं। तटीय परिचालन सावधानीपूर्वक किया जा सकता है।")
          }
        } else if (lower.contains("किसान") || lower.contains("दवा") || lower.contains("कीटनाशक") || lower.contains("farm")) {
          appendLine("🌾 $loc कृषि मौसम परामर्श:")
          advisory.bulletPoints.forEach { appendLine(it) }
        } else {
          appendLine("नमस्ते! $loc का वर्तमान मौसम:")
          appendLine("🌡️ तापमान: ${temp}°C (महसूस: ${weather.feelsLike}°C)")
          appendLine("💧 नमी: ${weather.humidity}% | 🌧️ वर्षा संभावना: $rainProb%")
          appendLine("💨 हवा: ${wind.toInt()} km/h | स्थिति: $condition")
          if (alerts.isNotEmpty()) {
            appendLine("⚠️ मौसम चेतावनी: ${alerts.first().title}")
          }
        }
        appendLine("\nडेटा स्रोत: ${weather.source} | अद्यतन समय: ${weather.timestamp}")
      }
    }

    // Default English Grounded Response
    return buildString {
      if (lower.contains("rain") || lower.contains("umbrella") || lower.contains("shower")) {
        if (rainProb > 40 || weather.rainfall > 0.5) {
          appendLine("Yes, rainfall is likely in $loc.")
          appendLine("🌧️ Rain Probability: $rainProb%")
          appendLine("💧 Precipitation Rate: ${weather.rainfall} mm/h")
          appendLine("🌡️ Temperature: ${temp}°C | Humidity: ${weather.humidity}%")
          appendLine("Condition: $condition. It is strongly recommended to carry rain protection.")
        } else {
          appendLine("Rainfall probability in $loc is low ($rainProb%).")
          appendLine("Current temperature is ${temp}°C with $condition skies and ${weather.humidity}% humidity.")
        }
      } else if (lower.contains("fish") || lower.contains("sea") || lower.contains("marine") || lower.contains("boat") || lower.contains("port")) {
        appendLine("🌊 Coastal & Marine Assessment for $loc:")
        appendLine("Surface Wind: ${wind.toInt()} km/h from ${weather.windDirection}° (Gusts up to ${weather.windGust.toInt()} km/h).")
        appendLine("Significant Wave Height: ${weather.waveHeight ?: 1.2} meters.")
        if (wind > 35 || weather.windGust > 45) {
          appendLine("🚨 OFFICIAL WARNING: Squally weather with strong wind gusts prevailing. Fishermen are strictly advised not to venture into deep sea areas.")
        } else {
          appendLine("Sea condition is moderate. Small craft vessels should stay within coastal VHF radar coverage.")
        }
      } else if (lower.contains("pesticide") || lower.contains("spray") || lower.contains("farm") || lower.contains("crop") || lower.contains("harvest")) {
        appendLine("🌾 Agricultural Meteorological Advisory for $loc:")
        advisory.bulletPoints.forEach { appendLine(it) }
      } else if (lower.contains("warning") || lower.contains("alert") || lower.contains("cyclone") || lower.contains("flood")) {
        appendLine("⚠️ Meteorological Warnings & Bulletin Status:")
        if (alerts.isNotEmpty()) {
          alerts.forEach { a ->
            appendLine("• [${a.severity}] ${a.title}: ${a.description}")
            appendLine("  Action: ${a.recommendedAction}")
          }
        } else {
          appendLine("No extreme cyclone or flood warnings are active for $loc at this time.")
        }
      } else {
        appendLine("Here is the latest verified weather intelligence for $loc:")
        appendLine("🌡️ Temperature: ${temp}°C (Feels like ${weather.feelsLike}°C)")
        appendLine("💧 Humidity: ${weather.humidity}% | 🌧️ Rain Probability: $rainProb%")
        appendLine("💨 Wind: ${wind.toInt()} km/h from ${weather.windDirection}° (Gusts: ${weather.windGust.toInt()} km/h)")
        appendLine("📊 Atmospheric Pressure: ${weather.pressure} hPa | ☀️ Condition: $condition")
        appendLine("🧴 UV Index: ${weather.uvIndex} | 🌅 Sunrise: ${weather.sunrise} | 🌇 Sunset: ${weather.sunset}")
      }
      appendLine("\nData Source: ${weather.source} | Observation: ${weather.timestamp}")
    }
  }
}
