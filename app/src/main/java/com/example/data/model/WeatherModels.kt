package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Normalized Weather Data Schema complying with Section 12 of the SIH WeatherGPT specification.
 */
data class NormalizedWeather(
  val locationName: String,
  val stateOrRegion: String,
  val latitude: Double,
  val longitude: Double,
  val timestamp: String,
  val source: String, // e.g., "IMD / Open-Meteo ECMWF"
  val observationTime: String,
  val dataFreshness: String, // "Live", "Recent (10m ago)", "Forecast"
  val temperature: Double,
  val feelsLike: Double,
  val humidity: Int,
  val pressure: Double,
  val windSpeed: Double, // km/h
  val windDirection: Int, // degrees
  val windGust: Double, // km/h
  val rainfall: Double, // mm in last hour
  val precipitationProbability: Int, // %
  val cloudCover: Int, // %
  val visibility: Double, // km
  val weatherCode: Int, // WMO code
  val weatherCondition: String, // "Partly Cloudy", "Thunderstorm", etc.
  val uvIndex: Double,
  val sunrise: String,
  val sunset: String,
  val waveHeight: Double? = null, // for coastal / marine
  val waveDirection: Int? = null,
  val soilMoisture: Double? = null // for farmers
)

data class HourlyForecastItem(
  val time: String, // "14:00"
  val fullTime: String,
  val temperature: Double,
  val precipitationProbability: Int,
  val rainfall: Double,
  val weatherCode: Int,
  val conditionText: String,
  val windSpeed: Double,
  val humidity: Int
)

data class DailyForecastItem(
  val date: String, // "2026-09-06"
  val dayOfWeek: String, // "Mon", "Tue"
  val maxTemp: Double,
  val minTemp: Double,
  val precipitationProbability: Int,
  val precipitationSum: Double,
  val weatherCode: Int,
  val conditionText: String,
  val uvIndexMax: Double,
  val windSpeedMax: Double
)

enum class AlertSeverity {
  EXTREME,   // IMD Red - Take Action
  SEVERE,    // IMD Orange - Be Prepared
  MODERATE,  // IMD Yellow - Be Updated
  INFO       // IMD Green - Normal Information
}

data class WeatherAlert(
  val id: String,
  val title: String,
  val severity: AlertSeverity,
  val location: String,
  val issuedTime: String,
  val validUntil: String,
  val source: String, // "India Meteorological Department (IMD)"
  val description: String,
  val recommendedAction: String,
  val phenomenon: String // "Heavy Rainfall", "Cyclone Warning", "Thunderstorm", "Heatwave", "Squally Weather"
)

enum class SectorMode(val displayName: String, val iconName: String) {
  PERSONAL("Personal", "Person"),
  FARMER("Farmer (Agri)", "Agriculture"),
  FISHER_MARINE("Fisher & Marine", "Sailing"),
  DISASTER("Disaster Watch", "Warning"),
  AVIATION("Aviation", "Flight"),
  INDUSTRIAL("Industrial", "Factory"),
  RESEARCHER("Climate Research", "Analytics")
}

data class SectorAdvisory(
  val mode: SectorMode,
  val statusTitle: String,
  val statusColor: String, // "GREEN", "YELLOW", "ORANGE", "RED"
  val bulletPoints: List<String>,
  val riskScore: Int // 0-100
)

data class ClimateTrendPoint(
  val year: Int,
  val avgTemperature: Double,
  val rainfallMm: Double,
  val anomalyTemp: Double,
  val isExtremeYear: Boolean
)

enum class SupportedLanguage(val code: String, val displayName: String, val nativeName: String) {
  ENGLISH("en", "English", "English"),
  TAMIL("ta", "Tamil", "தமிழ்"),
  HINDI("hi", "Hindi", "हिन्दी"),
  TELUGU("te", "Telugu", "తెలుగు"),
  KANNADA("kn", "Kannada", "ಕನ್ನಡ"),
  MALAYALAM("ml", "Malayalam", "മലയാളം"),
  BENGALI("bn", "Bengali", "বাংলা"),
  MARATHI("mr", "Marathi", "मराठी"),
  GUJARATI("gu", "Gujarati", "ગુજરાતી")
}

// Radar Layer Models
data class RadarFrame(
  val time: Long,
  val path: String,
  val formattedTime: String
)

data class RadarData(
  val host: String,
  val frames: List<RadarFrame>,
  val satelliteFrames: List<RadarFrame>,
  val source: String = "RainViewer Doppler Radar"
)

// API DTOs for Open-Meteo
@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
  val latitude: Double?,
  val longitude: Double?,
  val timezone: String?,
  val current: OpenMeteoCurrent?,
  val hourly: OpenMeteoHourly?,
  val daily: OpenMeteoDaily?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
  val time: String?,
  @Json(name = "temperature_2m") val temperature2m: Double?,
  @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int?,
  @Json(name = "apparent_temperature") val apparentTemperature: Double?,
  val precipitation: Double?,
  val rain: Double?,
  @Json(name = "weather_code") val weatherCode: Int?,
  @Json(name = "surface_pressure") val surfacePressure: Double?,
  @Json(name = "wind_speed_10m") val windSpeed10m: Double?,
  @Json(name = "wind_direction_10m") val windDirection10m: Int?,
  @Json(name = "wind_gusts_10m") val windGusts10m: Double?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourly(
  val time: List<String>?,
  @Json(name = "temperature_2m") val temperature2m: List<Double>?,
  @Json(name = "precipitation_probability") val precipitationProbability: List<Int>?,
  val precipitation: List<Double>?,
  @Json(name = "weather_code") val weatherCode: List<Int>?,
  @Json(name = "wind_speed_10m") val windSpeed10m: List<Double>?,
  @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Int>?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
  val time: List<String>?,
  @Json(name = "weather_code") val weatherCode: List<Int>?,
  @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>?,
  @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>?,
  @Json(name = "precipitation_sum") val precipitationSum: List<Double>?,
  @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>?,
  @Json(name = "uv_index_max") val uvIndexMax: List<Double>?,
  @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double>?,
  val sunrise: List<String>?,
  val sunset: List<String>?
)

// RainViewer maps JSON
@JsonClass(generateAdapter = true)
data class RainViewerResponse(
  val version: String?,
  val generated: Long?,
  val host: String?,
  val radar: RainViewerRadar?,
  val satellite: RainViewerSatellite?
)

@JsonClass(generateAdapter = true)
data class RainViewerRadar(
  val past: List<RainViewerFrame>?,
  val nowcast: List<RainViewerFrame>?
)

@JsonClass(generateAdapter = true)
data class RainViewerSatellite(
  val infrared: List<RainViewerFrame>?
)

@JsonClass(generateAdapter = true)
data class RainViewerFrame(
  val time: Long?,
  val path: String?
)
