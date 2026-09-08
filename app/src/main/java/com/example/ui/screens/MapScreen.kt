package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SavedLocationEntity
import com.example.data.model.*
import com.example.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class MapLayerMode(val title: String, val category: String, val icon: ImageVector) {
  RADAR("Precipitation Radar", "LIVE", Icons.Filled.Radar),
  SATELLITE("INSAT Satellite", "LIVE", Icons.Filled.Cloud),
  PRECIPITATION("Rain Intensity", "FORECAST", Icons.Filled.WaterDrop),
  TEMPERATURE("Temp Heatmap", "FORECAST", Icons.Filled.Thermostat),
  WIND("Wind Flow", "FORECAST", Icons.Filled.Air),
  MARINE("Sea Swell & Waves", "FORECAST", Icons.Filled.Waves)
}

enum class MapEngineMode(val label: String) {
  GOOGLE_MAPS("Google Maps SDK"),
  IMD_SYNOPTIC("IMD Doppler Radar GIS")
}

@Composable
fun MapScreen(
  radarData: RadarData?,
  currentLocation: SavedLocationEntity,
  currentWeather: NormalizedWeather? = null,
  onRecenter: () -> Unit = {},
  onLocationSelected: ((SavedLocationEntity) -> Unit)? = null
) {
  var selectedLayer by remember { mutableStateOf(MapLayerMode.RADAR) }
  var selectedEngine by remember { mutableStateOf(MapEngineMode.GOOGLE_MAPS) }
  var selectedMapType by remember { mutableStateOf(MapType.NORMAL) }
  var isPlaying by remember { mutableStateOf(true) }
  var currentFrameIndex by remember { mutableIntStateOf(3) } // Live frame default
  var showLegend by remember { mutableStateOf(true) }
  var selectedStation by remember { mutableStateOf<ImdRadarStation?>(null) }
  var showWeatherCard by remember { mutableStateOf(true) }

  val coroutineScope = rememberCoroutineScope()
  val userLatLng = remember(currentLocation.latitude, currentLocation.longitude) {
    LatLng(currentLocation.latitude, currentLocation.longitude)
  }

  // Camera state for Google Maps SDK
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(userLatLng, 9.5f)
  }

  // Animate camera when current location changes
  LaunchedEffect(currentLocation) {
    cameraPositionState.animate(
      update = CameraUpdateFactory.newLatLngZoom(userLatLng, 9.5f),
      durationMs = 900
    )
  }

  // Frame labels for radar timeline
  val timelineFrames = remember {
    listOf(
      "-60m" to "14:00 (T-60)",
      "-45m" to "14:15 (T-45)",
      "-30m" to "14:30 (T-30)",
      "-15m" to "14:45 (T-15)",
      "LIVE" to "15:00 (LIVE NOW)",
      "+15m" to "15:15 (Nowcast +15)",
      "+30m" to "15:30 (Nowcast +30)",
      "+45m" to "15:45 (Nowcast +45)"
    )
  }

  // Animation ticker for radar frames
  LaunchedEffect(isPlaying) {
    while (isPlaying) {
      delay(1400)
      currentFrameIndex = (currentFrameIndex + 1) % timelineFrames.size
    }
  }

  // Dynamic radar echoes around the user's location
  val precipProb = currentWeather?.precipitationProbability ?: 45
  val rainRate = currentWeather?.rainfall ?: 6.2
  val userRadarEchoes = remember(userLatLng, precipProb, rainRate, currentFrameIndex) {
    ImdStationsData.generateRadarEchoes(
      centerLat = userLatLng.latitude,
      centerLng = userLatLng.longitude,
      precipProb = precipProb,
      rainfallRate = rainRate,
      frameOffsetIndex = currentFrameIndex
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(SleekBackground)
      .testTag("map_screen_container")
  ) {
    // -------------------------------------------------------------------------
    // 1. MAP VIEW (Google Maps SDK or IMD Synoptic Canvas)
    // -------------------------------------------------------------------------
    if (selectedEngine == MapEngineMode.GOOGLE_MAPS) {
      GoogleMap(
        modifier = Modifier
          .fillMaxSize()
          .testTag("google_map_view"),
        cameraPositionState = cameraPositionState,
        properties = remember(selectedMapType) {
          MapProperties(
            mapType = selectedMapType,
            isMyLocationEnabled = false,
            isTrafficEnabled = false
          )
        },
        uiSettings = remember {
          MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true
          )
        }
      ) {
        // --- OVERLAY 1: User Location Accuracy & Radar Catchment Ring ---
        Circle(
          center = userLatLng,
          radius = 7000.0,
          fillColor = Color(0x280061A4),
          strokeColor = Color(0xFF0061A4),
          strokeWidth = 3f,
          zIndex = 2f
        )

        // --- OVERLAY 2: User Location Marker ---
        val userTemp = currentWeather?.temperature?.roundToInt() ?: 31
        val userCondition = currentWeather?.weatherCondition ?: "Observation"
        Marker(
          state = rememberMarkerState(position = userLatLng),
          title = "${currentLocation.name} ($userTemp°C)",
          snippet = "$userCondition • Rain: ${currentWeather?.rainfall ?: 4.2} mm/h • IMD Station",
          icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
          zIndex = 10f,
          onClick = {
            showWeatherCard = true
            false
          }
        )

        // --- OVERLAY 3: Dynamic Precipitation Radar Overlays ---
        if (selectedLayer == MapLayerMode.RADAR || selectedLayer == MapLayerMode.PRECIPITATION) {
          userRadarEchoes.forEach { echoZone ->
            Circle(
              center = echoZone.center,
              radius = echoZone.radiusMeters,
              fillColor = echoZone.fillColor,
              strokeColor = echoZone.strokeColor,
              strokeWidth = 2.5f,
              zIndex = 3f
            )
          }
        }

        // --- OVERLAY 4: INSAT Cloud Satellite Bands ---
        if (selectedLayer == MapLayerMode.SATELLITE) {
          for (i in 0..3) {
            val satCenter = LatLng(userLatLng.latitude + (i * 0.12 - 0.15), userLatLng.longitude + (i * 0.16 - 0.12))
            Circle(
              center = satCenter,
              radius = 45000.0,
              fillColor = Color(0x38FFFFFF),
              strokeColor = Color(0x66B0BEC5),
              strokeWidth = 2f,
              zIndex = 3f
            )
          }
        }

        // --- OVERLAY 5: Temperature Heatmap Thermal Rings ---
        if (selectedLayer == MapLayerMode.TEMPERATURE) {
          Circle(
            center = userLatLng,
            radius = 35000.0,
            fillColor = Color(0x35FF7043),
            strokeColor = Color(0x80FF5722),
            strokeWidth = 2f,
            zIndex = 3f
          )
          Circle(
            center = LatLng(userLatLng.latitude + 0.15, userLatLng.longitude - 0.12),
            radius = 28000.0,
            fillColor = Color(0x30FFCA28),
            strokeColor = Color(0x80FFB300),
            strokeWidth = 2f,
            zIndex = 3f
          )
        }

        // --- OVERLAY 6: Marine Swell Wave Contours ---
        if (selectedLayer == MapLayerMode.MARINE) {
          for (r in 1..3) {
            Circle(
              center = LatLng(userLatLng.latitude, userLatLng.longitude + 0.25),
              radius = r * 22000.0,
              fillColor = Color(0x1500E5FF),
              strokeColor = Color(0x9900B0FF),
              strokeWidth = 2.5f,
              zIndex = 3f
            )
          }
        }

        // --- OVERLAY 7: IMD Doppler Weather Radar (DWR) Station Markers across India ---
        ImdStationsData.stations.forEach { station ->
          val markerHue = when (station.alertLevel) {
            "SEVERE" -> BitmapDescriptorFactory.HUE_RED
            "WARNING" -> BitmapDescriptorFactory.HUE_ORANGE
            "ADVISORY" -> BitmapDescriptorFactory.HUE_YELLOW
            else -> BitmapDescriptorFactory.HUE_GREEN
          }

          Marker(
            state = rememberMarkerState(position = station.latLng),
            title = "${station.name} (${station.reflectivityDbz} dBZ)",
            snippet = "${station.condition} • Rain: ${station.rainfallRateMmH} mm/h • ${station.alertLevel}",
            icon = BitmapDescriptorFactory.defaultMarker(markerHue),
            zIndex = 5f,
            onClick = {
              selectedStation = station
              false
            }
          )

          // Radar Surveillance Range Ring (250 km or 500 km)
          if (selectedStation?.id == station.id || station.name.contains(currentLocation.name, ignoreCase = true)) {
            Circle(
              center = station.latLng,
              radius = station.rangeKm * 1000.0,
              strokeColor = Color(0x800061A4),
              strokeWidth = 2f,
              fillColor = Color(0x0C0061A4),
              zIndex = 1f
            )
          }
        }
      }
    } else {
      // -----------------------------------------------------------------------
      // IMD Synoptic Doppler Radar GIS Canvas
      // -----------------------------------------------------------------------
      var canvasScale by remember { mutableFloatStateOf(1f) }
      var canvasOffset by remember { mutableStateOf(Offset.Zero) }

      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .testTag("imd_synoptic_gis_canvas")
          .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
              canvasScale = (canvasScale * zoom).coerceIn(0.7f, 4.0f)
              canvasOffset = Offset(canvasOffset.x + pan.x, canvasOffset.y + pan.y)
            }
          }
      ) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f + canvasOffset.x, h / 2f + canvasOffset.y)

        // Ocean Background
        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF10253B), Color(0xFF091421))
          )
        )

        // Geographic Graticule Grid
        val gridSpacing = 85f * canvasScale
        for (x in -6..6) {
          val gx = center.x + x * gridSpacing
          drawLine(
            color = Color.White.copy(alpha = 0.07f),
            start = Offset(gx, 0f),
            end = Offset(gx, h),
            strokeWidth = 1f
          )
        }
        for (y in -6..6) {
          val gy = center.y + y * gridSpacing
          drawLine(
            color = Color.White.copy(alpha = 0.07f),
            start = Offset(0f, gy),
            end = Offset(w, gy),
            strokeWidth = 1f
          )
        }

        // India Coastline Outline Polygon
        val indiaPath = Path().apply {
          moveTo(center.x - 120f * canvasScale, center.y - 220f * canvasScale)
          lineTo(center.x + 80f * canvasScale, center.y - 200f * canvasScale)
          lineTo(center.x + 180f * canvasScale, center.y - 120f * canvasScale)
          lineTo(center.x + 120f * canvasScale, center.y - 60f * canvasScale)
          lineTo(center.x + 100f * canvasScale, center.y + 20f * canvasScale)
          lineTo(center.x + 60f * canvasScale, center.y + 110f * canvasScale)
          lineTo(center.x + 50f * canvasScale, center.y + 170f * canvasScale)
          lineTo(center.x + 20f * canvasScale, center.y + 250f * canvasScale)
          lineTo(center.x - 20f * canvasScale, center.y + 210f * canvasScale)
          lineTo(center.x - 50f * canvasScale, center.y + 130f * canvasScale)
          lineTo(center.x - 70f * canvasScale, center.y + 50f * canvasScale)
          lineTo(center.x - 110f * canvasScale, center.y - 10f * canvasScale)
          lineTo(center.x - 150f * canvasScale, center.y - 40f * canvasScale)
          lineTo(center.x - 140f * canvasScale, center.y - 150f * canvasScale)
          close()
        }

        drawPath(path = indiaPath, color = Color(0xFF1B3854))
        drawPath(path = indiaPath, color = Color(0xFF4FA3D1).copy(alpha = 0.6f), style = Stroke(width = 2f * canvasScale))

        // Dynamic Radar Echoes on Canvas
        val pulse = (currentFrameIndex * 12f) % 40f
        val userPos = Offset(center.x + 50f * canvasScale, center.y + 170f * canvasScale)

        // Doppler Radar Echo Cells
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFFD32F2F).copy(alpha = 0.8f), Color(0xFFFF9800).copy(alpha = 0.65f), Color(0xFF4CAF50).copy(alpha = 0.45f), Color.Transparent),
            center = Offset(userPos.x + 15f * canvasScale, userPos.y - 10f * canvasScale),
            radius = (90f + pulse) * canvasScale
          ),
          center = Offset(userPos.x + 15f * canvasScale, userPos.y - 10f * canvasScale),
          radius = (90f + pulse) * canvasScale
        )

        // User Location Pin
        drawCircle(color = SleekAlertRed, center = userPos, radius = 8f * canvasScale)
        drawCircle(color = Color.White, center = userPos, radius = 4f * canvasScale)
      }
    }

    // -------------------------------------------------------------------------
    // 2. TOP FLOATING CONTROL BAR: Station Info, Weather Pill & Engine Selector
    // -------------------------------------------------------------------------
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 14.dp, vertical = 6.dp)
        .align(Alignment.TopCenter)
    ) {
      Surface(
        color = Color.White.copy(alpha = 0.94f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SleekCardBorder),
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Station & Weather Condition summary
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (isPlaying) Color(0xFF00C853) else SleekTextSecondary)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${currentLocation.name}, ${currentLocation.state}",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = SleekDeepInk,
                  fontSize = 13.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            val temp = currentWeather?.temperature?.roundToInt() ?: 31
            val cond = currentWeather?.weatherCondition ?: "Partly Cloudy"
            val rain = currentWeather?.rainfall ?: 4.2
            Text(
              text = "$temp°C • $cond • ${rain}mm/h Rain",
              style = MaterialTheme.typography.bodySmall.copy(
                color = SleekBluePrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          // Engine Switcher Pill (Google Maps SDK vs IMD Radar GIS)
          Surface(
            color = SleekBlueContainer,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SleekBluePrimary.copy(alpha = 0.3f)),
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                selectedEngine = if (selectedEngine == MapEngineMode.GOOGLE_MAPS) {
                  MapEngineMode.IMD_SYNOPTIC
                } else {
                  MapEngineMode.GOOGLE_MAPS
                }
              }
              .testTag("toggle_map_engine_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (selectedEngine == MapEngineMode.GOOGLE_MAPS) Icons.Filled.Map else Icons.Filled.Radar,
                contentDescription = null,
                tint = SleekDeepInk,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (selectedEngine == MapEngineMode.GOOGLE_MAPS) "Google Maps" else "IMD GIS",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = SleekDeepInk,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Layer Selection Horizontal LazyRow
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 2.dp)
      ) {
        items(MapLayerMode.values()) { layer ->
          val isSelected = layer == selectedLayer
          Surface(
            color = if (isSelected) SleekBluePrimary else Color.White.copy(alpha = 0.92f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (isSelected) SleekBluePrimary else SleekCardBorder),
            shadowElevation = if (isSelected) 2.dp else 1.dp,
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .clickable { selectedLayer = layer }
              .testTag("layer_mode_${layer.name}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = layer.icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else SleekBluePrimary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = layer.title,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSelected) Color.White else SleekTextPrimary,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 11.sp
                )
              )
            }
          }
        }
      }
    }

    // -------------------------------------------------------------------------
    // 3. RIGHT FLOATING ACTION BUTTONS (Recenter, Zoom, Map Type, Legend)
    // -------------------------------------------------------------------------
    Column(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Recenter on User Location
      FloatingActionButton(
        onClick = {
          coroutineScope.launch {
            cameraPositionState.animate(
              update = CameraUpdateFactory.newLatLngZoom(userLatLng, 10.5f),
              durationMs = 800
            )
          }
          onRecenter()
        },
        containerColor = SleekBluePrimary,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .size(44.dp)
          .testTag("recenter_map_button")
      ) {
        Icon(Icons.Filled.MyLocation, contentDescription = "Center on Current Location", modifier = Modifier.size(20.dp))
      }

      // Zoom In
      SmallFloatingActionButton(
        onClick = {
          coroutineScope.launch {
            cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 300)
          }
        },
        containerColor = Color.White,
        contentColor = SleekDeepInk,
        shape = CircleShape,
        modifier = Modifier.size(38.dp)
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
      }

      // Zoom Out
      SmallFloatingActionButton(
        onClick = {
          coroutineScope.launch {
            cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 300)
          }
        },
        containerColor = Color.White,
        contentColor = SleekDeepInk,
        shape = CircleShape,
        modifier = Modifier.size(38.dp)
      ) {
        Icon(Icons.Filled.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
      }

      // Map Type Cycle (Standard -> Satellite -> Terrain -> Hybrid)
      SmallFloatingActionButton(
        onClick = {
          selectedMapType = when (selectedMapType) {
            MapType.NORMAL -> MapType.SATELLITE
            MapType.SATELLITE -> MapType.TERRAIN
            MapType.TERRAIN -> MapType.HYBRID
            else -> MapType.NORMAL
          }
        },
        containerColor = Color.White,
        contentColor = SleekBluePrimary,
        shape = CircleShape,
        modifier = Modifier
          .size(38.dp)
          .testTag("cycle_map_type_button")
      ) {
        Icon(Icons.Filled.Layers, contentDescription = "Cycle Map Type", modifier = Modifier.size(18.dp))
      }

      // Legend Toggle Button
      SmallFloatingActionButton(
        onClick = { showLegend = !showLegend },
        containerColor = Color.White,
        contentColor = SleekTextSecondary,
        shape = CircleShape,
        modifier = Modifier.size(38.dp)
      ) {
        Icon(Icons.Filled.Info, contentDescription = "Toggle Radar Legend", modifier = Modifier.size(18.dp))
      }
    }

    // -------------------------------------------------------------------------
    // 4. RADAR REFLECTIVITY & RAIN INTENSITY LEGEND
    // -------------------------------------------------------------------------
    if (showLegend && (selectedLayer == MapLayerMode.RADAR || selectedLayer == MapLayerMode.PRECIPITATION)) {
      Surface(
        color = Color.White.copy(alpha = 0.94f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SleekCardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(start = 14.dp, bottom = 124.dp)
          .testTag("radar_reflectivity_legend")
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          Text(
            text = "IMD Radar Reflectivity (dBZ)",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = SleekDeepInk,
              fontSize = 9.5.sp
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            MapLegendItem(color = Color(0xFF4CAF50), label = "15 Light")
            MapLegendItem(color = Color(0xFFFFEB3B), label = "30 Mod")
            MapLegendItem(color = Color(0xFFFF9800), label = "45 Hvy")
            MapLegendItem(color = Color(0xFFD32F2F), label = "55+ Ext")
          }
        }
      }
    }

    // -------------------------------------------------------------------------
    // 5. BOTTOM RADAR PLAYBACK TIMELINE CONTROLLER
    // -------------------------------------------------------------------------
    Surface(
      color = Color.White.copy(alpha = 0.96f),
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      border = BorderStroke(1.dp, SleekCardBorder),
      shadowElevation = 6.dp,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .testTag("radar_timeline_controller")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        // Player Status Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { isPlaying = !isPlaying },
              modifier = Modifier
                .size(36.dp)
                .background(SleekBlueContainer, CircleShape)
                .testTag("radar_play_pause_button")
            ) {
              Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause Radar" else "Play Radar",
                tint = SleekDeepInk,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            val currentFrameLabel = timelineFrames.getOrNull(currentFrameIndex)?.second ?: "LIVE NOW"
            Column {
              Text(
                text = currentFrameLabel,
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = SleekDeepInk,
                  fontSize = 12.sp
                )
              )
              Text(
                text = "IMD Radar Network • 10-Min Scan Interval",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = SleekTextSecondary,
                  fontSize = 10.sp
                )
              )
            }
          }

          Surface(
            color = if (isPlaying) SleekBlueContainer else SleekCardBg,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, SleekCardBorder)
          ) {
            Text(
              text = if (currentFrameIndex == 4) "LIVE ECHO" else "NOWCAST",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = SleekDeepInk,
                fontSize = 10.sp
              ),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Frame Scrubber Slider
        Slider(
          value = currentFrameIndex.toFloat(),
          onValueChange = {
            currentFrameIndex = it.toInt().coerceIn(0, timelineFrames.size - 1)
            isPlaying = false
          },
          valueRange = 0f..(timelineFrames.size - 1).toFloat(),
          steps = timelineFrames.size - 2,
          colors = SliderDefaults.colors(
            thumbColor = SleekBluePrimary,
            activeTrackColor = SleekBluePrimary,
            inactiveTrackColor = SleekCardBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .testTag("radar_frame_slider")
        )

        // Timestamp tick labels below slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          timelineFrames.forEachIndexed { idx, pair ->
            Text(
              text = pair.first,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = if (idx == currentFrameIndex) FontWeight.Bold else FontWeight.Normal,
                color = if (idx == currentFrameIndex) SleekBluePrimary else SleekTextSecondary
              )
            )
          }
        }
      }
    }

    // -------------------------------------------------------------------------
    // 6. POPUP SHEET: IMD Doppler Radar Station Inspector
    // -------------------------------------------------------------------------
    AnimatedVisibility(
      visible = selectedStation != null,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(start = 14.dp, end = 14.dp, bottom = 90.dp)
    ) {
      val station = selectedStation
      if (station != null) {
        Surface(
          color = Color.White,
          shape = RoundedCornerShape(20.dp),
          border = BorderStroke(1.5.dp, SleekBluePrimary),
          shadowElevation = 6.dp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("imd_station_detail_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = SleekDeepInk
                    )
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    color = SleekBlueContainer,
                    shape = RoundedCornerShape(6.dp)
                  ) {
                    Text(
                      text = station.stationCode,
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = SleekBluePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp
                      ),
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                  text = "${station.radarType} • Range: ${station.rangeKm} km",
                  style = MaterialTheme.typography.bodySmall.copy(color = SleekTextSecondary, fontSize = 11.sp)
                )
              }

              IconButton(onClick = { selectedStation = null }) {
                Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = SleekTextSecondary)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Grid
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              StationMetricItem("Reflectivity", "${station.reflectivityDbz} dBZ", SleekBluePrimary)
              StationMetricItem("Rainfall Rate", "${station.rainfallRateMmH} mm/h", SleekBluePrimary)
              StationMetricItem("Echo Top", "${station.echoTopKm} km", SleekDeepInk)
              val alertColor = when (station.alertLevel) {
                "SEVERE" -> SleekAlertRed
                "WARNING" -> WarningOrange
                "ADVISORY" -> WarningYellow
                else -> WarningGreen
              }
              StationMetricItem("Alert Level", station.alertLevel, alertColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Center camera on station
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  coroutineScope.launch {
                    cameraPositionState.animate(
                      CameraUpdateFactory.newLatLngZoom(station.latLng, 11f),
                      700
                    )
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SleekBluePrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Filled.ZoomIn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Focus Radar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }

              if (onLocationSelected != null) {
                OutlinedButton(
                  onClick = {
                    onLocationSelected(
                      SavedLocationEntity(
                        name = station.name.substringBefore(" DWR"),
                        state = "India",
                        latitude = station.latitude,
                        longitude = station.longitude,
                        isCurrentLocation = false,
                        category = "IMD Radar"
                      )
                    )
                    selectedStation = null
                  },
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, SleekBluePrimary),
                  modifier = Modifier.weight(1f)
                ) {
                  Text("Select Station", fontSize = 12.sp, color = SleekBluePrimary, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MapLegendItem(color: Color, label: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier
        .size(width = 24.dp, height = 7.dp)
        .background(color, shape = RoundedCornerShape(2.dp))
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 8.sp,
        color = SleekTextSecondary,
        fontWeight = FontWeight.Medium
      )
    )
  }
}

@Composable
private fun StationMetricItem(label: String, value: String, valueColor: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        color = SleekTextSecondary
      )
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Bold,
        color = valueColor,
        fontSize = 12.sp
      )
    )
  }
}
