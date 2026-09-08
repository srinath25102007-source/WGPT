package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.HourlyForecastRow
import com.example.ui.components.SectorModeSelector
import com.example.ui.components.WeatherStatCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
  weather: NormalizedWeather?,
  hourlyForecast: List<HourlyForecastItem>,
  dailyForecast: List<DailyForecastItem>,
  sectorMode: SectorMode,
  sectorAdvisory: SectorAdvisory?,
  onModeSelected: (SectorMode) -> Unit,
  onNavigateToChatWithQuery: (String) -> Unit,
  onNavigateToMap: () -> Unit,
  onNavigateToAlerts: () -> Unit,
  onNavigateToClimate: () -> Unit,
  onRefresh: () -> Unit,
  isLoading: Boolean
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(SleekBackground)
      .testTag("home_screen_container"),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // Sector Mode Selector
    item {
      SectorModeSelector(
        selectedMode = sectorMode,
        onModeSelected = onModeSelected
      )
    }

    // Sleek Interface Hero Weather Card (from HTML: rounded-[32px], gradient from #D3E4FF to #E1E2EC, text #001C38)
    item {
      if (weather != null) {
        val isRain = weather.weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99)
        val isThunder = weather.weatherCode in listOf(95, 96, 99)

        Surface(
          shape = RoundedCornerShape(32.dp),
          color = Color.Transparent,
          shadowElevation = 1.dp,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("hero_weather_card")
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.linearGradient(
                  colors = listOf(SleekBlueContainer, SleekCardBorder)
                )
              )
              .padding(20.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top
            ) {
              Column(modifier = Modifier.weight(1f)) {
                // Big Temperature: 60sp font light in SleekDeepInk
                Text(
                  text = "${weather.temperature.toInt()}°",
                  style = MaterialTheme.typography.displayMedium.copy(
                    color = SleekDeepInk,
                    fontWeight = FontWeight.Light,
                    fontSize = 62.sp,
                    letterSpacing = (-1.5).sp
                  )
                )

                // Condition: uppercase tracking-wider
                Text(
                  text = weather.weatherCondition.uppercase(),
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = SleekDeepInk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 1.2.sp
                  )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Wind & Humidity Row
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Air,
                      contentDescription = null,
                      tint = SleekTextSecondary,
                      modifier = Modifier.size(16.dp)
                    )
                    Text(
                      text = "${weather.windSpeed.toInt()}km/h",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      color = SleekTextPrimary
                    )
                  }

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Filled.WaterDrop,
                      contentDescription = null,
                      tint = SleekTextSecondary,
                      modifier = Modifier.size(16.dp)
                    )
                    Text(
                      text = "${weather.humidity}%",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      color = SleekTextPrimary
                    )
                  }

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Compress,
                      contentDescription = null,
                      tint = SleekTextSecondary,
                      modifier = Modifier.size(16.dp)
                    )
                    Text(
                      text = "${weather.pressure.toInt()}hPa",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      color = SleekTextPrimary
                    )
                  }
                }
              }

              // Weather Icon Box: semi-transparent backdrop with glowing sun/cloud accent
              Box(
                modifier = Modifier
                  .size(80.dp)
                  .clip(RoundedCornerShape(20.dp))
                  .background(Color.White.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
              ) {
                val conditionIcon: ImageVector = when {
                  isThunder -> Icons.Filled.Thunderstorm
                  isRain -> Icons.Filled.WaterDrop
                  weather.weatherCode in listOf(1, 2, 3) -> Icons.Filled.Cloud
                  else -> Icons.Filled.WbSunny
                }

                Icon(
                  imageVector = conditionIcon,
                  contentDescription = weather.weatherCondition,
                  tint = if (conditionIcon == Icons.Filled.WbSunny) SunYellow else SleekDeepInk,
                  modifier = Modifier.size(52.dp)
                )
              }
            }
          }
        }
      } else {
        // Skeleton loader
        Surface(
          shape = RoundedCornerShape(32.dp),
          color = SleekCardBg,
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp)
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(color = SleekBluePrimary)
          }
        }
      }
    }

    // Sleek Interface Live Radar Preview Card (HTML: min-h-[160px] rounded-[32px] bg-[#E1E2EC] border-2 border-white)
    item {
      Surface(
        shape = RoundedCornerShape(32.dp),
        color = SleekCardBorder,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
        shadowElevation = 1.dp,
        modifier = Modifier
          .fillMaxWidth()
          .height(160.dp)
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clip(RoundedCornerShape(32.dp))
          .clickable { onNavigateToMap() }
          .testTag("home_radar_preview_card")
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          // Subtle GIS grid / echo canvas
          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Radar background radar circles
            for (r in 1..4) {
              drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                center = Offset(w / 2f, h / 2f),
                radius = r * 32f,
                style = Stroke(width = 1.5f)
              )
            }

            // Radar echo blob
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(SleekBluePrimary.copy(alpha = 0.7f), SleekBlueContainer.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(w * 0.6f, h * 0.45f),
                radius = 55f
              ),
              center = Offset(w * 0.6f, h * 0.45f),
              radius = 55f
            )
          }

          // Top Left: RADAR LIVE Badge
          Surface(
            color = SleekBluePrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(SleekAlertRed)
              )
              Text(
                text = "RADAR LIVE",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
              )
            }
          }

          // Bottom Right: Precipitation Trace Badge
          Surface(
            color = Color.White.copy(alpha = 0.92f),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp,
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(7.dp)
                  .clip(CircleShape)
                  .background(SleekBluePrimary)
              )
              Text(
                text = "Precipitation Trace (Tap for GIS Map)",
                color = SleekTextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // Sleek Interface LLM Climate Insight Card (HTML: bg-[#F0F0F7] p-4 rounded-[28px] border border-[#E1E2EC])
    item {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = SleekCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clickable {
            onNavigateToChatWithQuery("Analyze the latest climate and meteorological insights for my location.")
          }
          .testTag("llm_climate_insight_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Header Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.Psychology,
                contentDescription = null,
                tint = SleekTextSecondary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "LLM CLIMATE INSIGHT",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = SleekTextSecondary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  letterSpacing = 1.sp
                )
              )
            }

            Text(
              text = "ANALYZING TRENDS",
              style = MaterialTheme.typography.labelSmall.copy(
                color = SleekBluePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Body with Avatar & Grounded Insight
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(SleekBluePrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }

            val insightQuote = if (sectorAdvisory != null) {
              "${sectorAdvisory.statusTitle}. ${sectorAdvisory.bulletPoints.firstOrNull() ?: "IMD observation models show stable seasonal atmospheric circulation."}"
            } else {
              "Monsoon onset expected within climatological normal. Coastal humidity trends suggest heavy precipitation post-14:00."
            }

            Text(
              text = "\"$insightQuote\"",
              style = MaterialTheme.typography.bodySmall.copy(
                color = SleekTextPrimary,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 17.sp
              ),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // Sleek Interface 24-Hour Hourly Forecast
    item {
      HourlyForecastRow(
        hourlyForecast = hourlyForecast,
        modifier = Modifier.padding(vertical = 4.dp)
      )
    }

    // Quick Weather Actions Grid
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
          text = "QUICK ACTIONS",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = SleekTextSecondary,
            fontSize = 11.sp,
            letterSpacing = 1.sp
          ),
          modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          QuickActionPill(
            icon = Icons.Filled.WaterDrop,
            label = "Will it rain?",
            onClick = { onNavigateToChatWithQuery("Will it rain today or tomorrow?") },
            modifier = Modifier.weight(1f)
          )
          QuickActionPill(
            icon = Icons.Filled.Agriculture,
            label = "Farm Spraying",
            onClick = { onNavigateToChatWithQuery("Can I spray pesticide on my crop tomorrow?") },
            modifier = Modifier.weight(1f)
          )
          QuickActionPill(
            icon = Icons.Filled.Sailing,
            label = "Marine Safety",
            onClick = { onNavigateToChatWithQuery("Is it safe for fishermen to go to sea tomorrow?") },
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          QuickActionPill(
            icon = Icons.Filled.Radar,
            label = "Live Radar",
            onClick = onNavigateToMap,
            modifier = Modifier.weight(1f)
          )
          QuickActionPill(
            icon = Icons.Filled.NotificationImportant,
            label = "IMD Alerts",
            onClick = onNavigateToAlerts,
            modifier = Modifier.weight(1f)
          )
          QuickActionPill(
            icon = Icons.Filled.Insights,
            label = "Climate Trend",
            onClick = onNavigateToClimate,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Atmospheric Parameters Grid
    item {
      if (weather != null) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
          Text(
            text = "ATMOSPHERIC METRICS",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = SleekTextSecondary,
              fontSize = 11.sp,
              letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
          )

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeatherStatCard(
              title = "Wind & Gusts",
              value = "${weather.windSpeed.toInt()} km/h",
              subtitle = "Gusts: ${weather.windGust.toInt()} km/h",
              icon = Icons.Filled.Air,
              modifier = Modifier.weight(1f)
            )
            WeatherStatCard(
              title = "Humidity & Dew",
              value = "${weather.humidity}%",
              subtitle = "Pressure: ${weather.pressure.toInt()} hPa",
              icon = Icons.Filled.Water,
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeatherStatCard(
              title = "UV Radiation",
              value = "${weather.uvIndex}",
              subtitle = if (weather.uvIndex > 6.0) "High Risk" else "Moderate",
              icon = Icons.Filled.WbSunny,
              iconColor = if (weather.uvIndex > 6.0) WarningOrange else SunYellow,
              modifier = Modifier.weight(1f)
            )
            WeatherStatCard(
              title = "Sun Schedule",
              value = weather.sunrise,
              subtitle = "Sunset: ${weather.sunset}",
              icon = Icons.Filled.WbTwilight,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // 7-Day Forecast Section
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
          text = "7-DAY METEOROLOGICAL OUTLOOK",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = SleekTextSecondary,
            fontSize = 11.sp,
            letterSpacing = 1.sp
          ),
          modifier = Modifier.padding(bottom = 8.dp)
        )

        Surface(
          shape = RoundedCornerShape(24.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            dailyForecast.forEachIndexed { index, daily ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = daily.dayOfWeek,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextPrimary
                  ),
                  modifier = Modifier.width(68.dp)
                )

                // Rain probability pill
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.width(60.dp)
                ) {
                  Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = if (daily.precipitationProbability > 40) SleekBluePrimary else SleekCardBorder,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Text(
                    text = "${daily.precipitationProbability}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = if (daily.precipitationProbability > 40) SleekBluePrimary else SleekTextSecondary,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Medium
                    )
                  )
                }

                Text(
                  text = daily.conditionText,
                  style = MaterialTheme.typography.bodySmall.copy(color = SleekTextSecondary, fontSize = 12.sp),
                  modifier = Modifier.weight(1f),
                  maxLines = 1
                )

                // High / Low temperatures
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "${daily.maxTemp.toInt()}°",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = SleekDeepInk
                    )
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "${daily.minTemp.toInt()}°",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = SleekTextSecondary
                    )
                  )
                }
              }
              if (index < dailyForecast.size - 1) {
                HorizontalDivider(color = SleekCardBorder.copy(alpha = 0.7f), thickness = 0.8.dp)
              }
            }
          }
        }
      }
    }

    // Source Attribution Footer
    item {
      if (weather != null) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Data Source: ${weather.source}",
            style = MaterialTheme.typography.bodySmall.copy(color = SleekTextSecondary, fontSize = 11.sp)
          )
          Text(
            text = "Observation synced: ${weather.timestamp}",
            style = MaterialTheme.typography.bodySmall.copy(color = SleekTextSecondary.copy(alpha = 0.7f), fontSize = 10.sp)
          )
        }
      }
    }
  }
}

@Composable
fun SleekHourlyCell(item: HourlyForecastItem, isActive: Boolean) {
  Surface(
    color = if (isActive) SleekBlueContainer else Color.White,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) SleekBluePrimary else SleekCardBorder),
    modifier = Modifier.width(74.dp)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = item.time.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
          color = if (isActive) SleekDeepInk else SleekTextSecondary,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold
        )
      )
      Spacer(modifier = Modifier.height(6.dp))

      val icon = when {
        item.weatherCode in listOf(95, 96, 99) -> Icons.Filled.Thunderstorm
        item.weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> Icons.Filled.WaterDrop
        item.weatherCode in listOf(1, 2, 3) -> Icons.Filled.Cloud
        else -> Icons.Filled.WbSunny
      }
      val tint = if (isActive) SleekDeepInk else SleekBluePrimary

      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(22.dp)
      )

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "${item.temperature.toInt()}°",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          color = if (isActive) SleekDeepInk else SleekTextPrimary,
          fontSize = 14.sp
        )
      )
    }
  }
}

@Composable
fun QuickActionPill(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = Color.White,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = SleekBluePrimary, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(color = SleekTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
        maxLines = 1
      )
    }
  }
}
