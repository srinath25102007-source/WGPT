package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

data class ImdRadarStation(
  val id: String,
  val name: String,
  val stationCode: String,
  val latitude: Double,
  val longitude: Double,
  val rangeKm: Int = 250,
  val radarType: String = "S-Band Dual-Pol DWR",
  val status: String = "Operational - Live Scanning",
  val reflectivityDbz: Int,
  val rainfallRateMmH: Double,
  val temperature: Double,
  val condition: String,
  val alertLevel: String, // "NORMAL", "ADVISORY", "WARNING", "SEVERE"
  val echoTopKm: Double
) {
  val latLng: LatLng get() = LatLng(latitude, longitude)
}

data class RadarEchoZone(
  val center: LatLng,
  val radiusMeters: Double,
  val reflectivityDbz: Int,
  val intensityLabel: String,
  val fillColor: Color,
  val strokeColor: Color
)

object ImdStationsData {
  val stations = listOf(
    ImdRadarStation(
      id = "dwr_chennai",
      name = "Chennai DWR",
      stationCode = "DWR-CHN",
      latitude = 13.0827,
      longitude = 80.2707,
      rangeKm = 250,
      radarType = "S-Band Dual-Pol DWR",
      reflectivityDbz = 48,
      rainfallRateMmH = 18.5,
      temperature = 31.0,
      condition = "Heavy Rain / Thunderstorm",
      alertLevel = "WARNING",
      echoTopKm = 12.4
    ),
    ImdRadarStation(
      id = "dwr_mumbai",
      name = "Mumbai Colaba DWR",
      stationCode = "DWR-BOM",
      latitude = 18.9067,
      longitude = 72.8147,
      rangeKm = 250,
      radarType = "C-Band Polarimetric",
      reflectivityDbz = 38,
      rainfallRateMmH = 8.2,
      temperature = 29.5,
      condition = "Moderate Showers",
      alertLevel = "ADVISORY",
      echoTopKm = 9.8
    ),
    ImdRadarStation(
      id = "dwr_delhi",
      name = "Delhi Palam DWR",
      stationCode = "DWR-DEL",
      latitude = 28.5843,
      longitude = 77.0863,
      rangeKm = 250,
      radarType = "S-Band Dual-Pol",
      reflectivityDbz = 22,
      rainfallRateMmH = 1.0,
      temperature = 33.2,
      condition = "Partly Cloudy / Haze",
      alertLevel = "NORMAL",
      echoTopKm = 5.2
    ),
    ImdRadarStation(
      id = "dwr_kolkata",
      name = "Kolkata New Town DWR",
      stationCode = "DWR-CCU",
      latitude = 22.5726,
      longitude = 88.3639,
      rangeKm = 250,
      radarType = "S-Band Dual-Pol",
      reflectivityDbz = 52,
      rainfallRateMmH = 26.0,
      temperature = 30.1,
      condition = "Severe Thunderstorm",
      alertLevel = "SEVERE",
      echoTopKm = 14.1
    ),
    ImdRadarStation(
      id = "dwr_bengaluru",
      name = "Bengaluru DWR",
      stationCode = "DWR-BLR",
      latitude = 12.9716,
      longitude = 77.5946,
      rangeKm = 250,
      radarType = "C-Band Doppler",
      reflectivityDbz = 32,
      rainfallRateMmH = 4.5,
      temperature = 26.0,
      condition = "Light Rain / Overcast",
      alertLevel = "NORMAL",
      echoTopKm = 7.5
    ),
    ImdRadarStation(
      id = "dwr_hyderabad",
      name = "Hyderabad Begumpet DWR",
      stationCode = "DWR-HYD",
      latitude = 17.4531,
      longitude = 78.4677,
      rangeKm = 250,
      radarType = "S-Band Dual-Pol",
      reflectivityDbz = 28,
      rainfallRateMmH = 2.8,
      temperature = 29.8,
      condition = "Passing Clouds",
      alertLevel = "NORMAL",
      echoTopKm = 6.0
    ),
    ImdRadarStation(
      id = "dwr_kochi",
      name = "Kochi Naval Base DWR",
      stationCode = "DWR-COK",
      latitude = 9.9312,
      longitude = 76.2673,
      rangeKm = 250,
      radarType = "C-Band Dual-Pol",
      reflectivityDbz = 44,
      rainfallRateMmH = 14.2,
      temperature = 28.2,
      condition = "Monsoon Squall",
      alertLevel = "WARNING",
      echoTopKm = 11.2
    ),
    ImdRadarStation(
      id = "dwr_vizag",
      name = "Visakhapatnam DWR",
      stationCode = "DWR-VTZ",
      latitude = 17.6868,
      longitude = 83.2185,
      rangeKm = 500,
      radarType = "S-Band Cyclone Detection",
      reflectivityDbz = 41,
      rainfallRateMmH = 11.0,
      temperature = 30.5,
      condition = "Coastal Rain Bands",
      alertLevel = "ADVISORY",
      echoTopKm = 10.5
    ),
    ImdRadarStation(
      id = "dwr_bhubaneswar",
      name = "Bhubaneswar DWR",
      stationCode = "DWR-BBI",
      latitude = 20.2961,
      longitude = 85.8245,
      rangeKm = 500,
      radarType = "S-Band Dual-Pol",
      reflectivityDbz = 49,
      rainfallRateMmH = 21.0,
      temperature = 29.2,
      condition = "Deep Depression Inflow",
      alertLevel = "WARNING",
      echoTopKm = 13.0
    ),
    ImdRadarStation(
      id = "dwr_ahmedabad",
      name = "Ahmedabad DWR",
      stationCode = "DWR-AMD",
      latitude = 23.0225,
      longitude = 72.5714,
      rangeKm = 250,
      radarType = "C-Band Doppler",
      reflectivityDbz = 18,
      rainfallRateMmH = 0.5,
      temperature = 35.1,
      condition = "Warm & Dry",
      alertLevel = "NORMAL",
      echoTopKm = 4.0
    ),
    ImdRadarStation(
      id = "dwr_thiruvananthapuram",
      name = "Thiruvananthapuram DWR",
      stationCode = "DWR-TRV",
      latitude = 8.5241,
      longitude = 76.9366,
      rangeKm = 250,
      radarType = "S-Band Polarimetric",
      reflectivityDbz = 42,
      rainfallRateMmH = 12.8,
      temperature = 28.6,
      condition = "Coastal Showers",
      alertLevel = "ADVISORY",
      echoTopKm = 10.2
    ),
    ImdRadarStation(
      id = "dwr_patna",
      name = "Patna DWR",
      stationCode = "DWR-PAT",
      latitude = 25.5941,
      longitude = 85.1376,
      rangeKm = 250,
      radarType = "S-Band Doppler",
      reflectivityDbz = 35,
      rainfallRateMmH = 6.4,
      temperature = 31.8,
      condition = "Scattered Rain",
      alertLevel = "NORMAL",
      echoTopKm = 8.5
    )
  )

  /**
   * Generates dynamic Doppler Radar reflectivity echo circles around a given location
   * based on precipitation probability and intensity.
   */
  fun generateRadarEchoes(
    centerLat: Double,
    centerLng: Double,
    precipProb: Int,
    rainfallRate: Double,
    frameOffsetIndex: Int = 0
  ): List<RadarEchoZone> {
    val zones = mutableListOf<RadarEchoZone>()
    val baseRadius = 18000.0 // 18 km
    val driftLat = (frameOffsetIndex % 4) * 0.012
    val driftLng = (frameOffsetIndex % 4) * 0.015

    val effectivePrecip = if (precipProb > 0) precipProb else 35
    val effectiveRain = if (rainfallRate > 0.0) rainfallRate else 4.5

    // Outer light rain boundary (15-30 dBZ)
    zones.add(
      RadarEchoZone(
        center = LatLng(centerLat + 0.02 + driftLat, centerLng + 0.03 + driftLng),
        radiusMeters = baseRadius * 2.8,
        reflectivityDbz = 25,
        intensityLabel = "15-30 dBZ (Light Rain)",
        fillColor = Color(0x384CAF50),
        strokeColor = Color(0x804CAF50)
      )
    )

    // Moderate rain zone (30-40 dBZ)
    if (effectivePrecip >= 25 || effectiveRain >= 2.0) {
      zones.add(
        RadarEchoZone(
          center = LatLng(centerLat + 0.015 + driftLat, centerLng + 0.02 + driftLng),
          radiusMeters = baseRadius * 1.8,
          reflectivityDbz = 35,
          intensityLabel = "30-40 dBZ (Moderate Rain)",
          fillColor = Color(0x4DFFEB3B),
          strokeColor = Color(0x99FFC107)
        )
      )
    }

    // Heavy convective core (40-50 dBZ)
    if (effectivePrecip >= 45 || effectiveRain >= 7.0) {
      zones.add(
        RadarEchoZone(
          center = LatLng(centerLat + 0.005 + driftLat, centerLng + 0.01 + driftLng),
          radiusMeters = baseRadius * 1.1,
          reflectivityDbz = 45,
          intensityLabel = "40-50 dBZ (Heavy Rain)",
          fillColor = Color(0x66FF9800),
          strokeColor = Color(0xB3FF5722)
        )
      )
    }

    // Extreme convective cell / thunderstorm core (50+ dBZ)
    if (effectivePrecip >= 70 || effectiveRain >= 15.0) {
      zones.add(
        RadarEchoZone(
          center = LatLng(centerLat + driftLat, centerLng + driftLng),
          radiusMeters = baseRadius * 0.55,
          reflectivityDbz = 55,
          intensityLabel = "50+ dBZ (Severe Thunderstorm / Squall)",
          fillColor = Color(0x80D32F2F),
          strokeColor = Color(0xE6B71C1C)
        )
      )
    }

    // Secondary coastal or offshore cell
    zones.add(
      RadarEchoZone(
        center = LatLng(centerLat - 0.06 + driftLat, centerLng + 0.09 + driftLng),
        radiusMeters = baseRadius * 1.4,
        reflectivityDbz = 38,
        intensityLabel = "Offshore Convective Cell",
        fillColor = Color(0x4D00BCD4),
        strokeColor = Color(0x990097A7)
      )
    )

    return zones
  }
}
