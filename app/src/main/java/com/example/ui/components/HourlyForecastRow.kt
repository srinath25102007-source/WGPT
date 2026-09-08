package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyForecastItem
import com.example.ui.theme.*
import kotlin.math.roundToInt

/**
 * A horizontal scrolling row component using LazyRow to display hourly
 * temperature and weather icon predictions for the next 24 hours.
 */
@Composable
fun HourlyForecastRow(
  hourlyForecast: List<HourlyForecastItem>,
  modifier: Modifier = Modifier,
  title: String = "HOURLY OUTLOOK",
  subtitle: String = "Next 24 Hours • IMD Synoptic Model",
  onHourSelected: ((HourlyForecastItem) -> Unit)? = null
) {
  val displayHours = remember(hourlyForecast) {
    hourlyForecast.take(24)
  }

  var selectedIndex by remember { mutableIntStateOf(0) }
  val listState = rememberLazyListState()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("hourly_forecast_section")
  ) {
    // Header Bar with 24h temperature range summary
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = SleekTextSecondary,
            fontSize = 11.sp,
            letterSpacing = 1.sp
          )
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(
            color = SleekTextSecondary,
            fontSize = 11.sp
          )
        )
      }

      if (displayHours.isNotEmpty()) {
        val maxTemp = displayHours.maxOfOrNull { it.temperature }?.roundToInt() ?: 0
        val minTemp = displayHours.minOfOrNull { it.temperature }?.roundToInt() ?: 0
        Surface(
          color = SleekCardBg,
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, SleekCardBorder)
        ) {
          Text(
            text = "24h  H:${maxTemp}°  L:${minTemp}°",
            style = MaterialTheme.typography.labelSmall.copy(
              color = SleekDeepInk,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }

    if (displayHours.isEmpty()) {
      // Empty state placeholder
      Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = SleekBluePrimary
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Loading 24-hour synoptic forecast...",
            style = MaterialTheme.typography.bodySmall.copy(color = SleekTextSecondary)
          )
        }
      }
    } else {
      // Horizontal Scrolling LazyRow for the 24 hours
      LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hourly_forecast_lazy_row")
      ) {
        itemsIndexed(displayHours) { index, item ->
          val isSelected = index == selectedIndex
          val isNow = index == 0

          HourlyCardItem(
            item = item,
            index = index,
            isNow = isNow,
            isSelected = isSelected,
            onClick = {
              selectedIndex = index
              onHourSelected?.invoke(item)
            }
          )
        }
      }

      // Selected Hour Detail Chip strip (smoothly reveals selected hour's metrics)
      val currentSelection = displayHours.getOrNull(selectedIndex)
      if (currentSelection != null) {
        AnimatedVisibility(
          visible = true,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Surface(
            color = SleekBlueContainer.copy(alpha = 0.45f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SleekCardBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp)
              .testTag("hourly_detail_strip")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = getPredictionWeatherIcon(currentSelection.weatherCode),
                  contentDescription = null,
                  tint = SleekBluePrimary,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "${if (selectedIndex == 0) "Now" else currentSelection.time}: ${currentSelection.conditionText}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SleekDeepInk,
                    fontSize = 11.sp
                  )
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = SleekBluePrimary, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(2.dp))
                  Text(
                    text = "${currentSelection.precipitationProbability}%",
                    style = MaterialTheme.typography.labelSmall.copy(color = SleekDeepInk, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                  )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Filled.Air, contentDescription = null, tint = SleekTextSecondary, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(2.dp))
                  Text(
                    text = "${currentSelection.windSpeed.roundToInt()} km/h",
                    style = MaterialTheme.typography.labelSmall.copy(color = SleekTextSecondary, fontSize = 10.sp)
                  )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Filled.Grain, contentDescription = null, tint = SleekTextSecondary, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(2.dp))
                  Text(
                    text = "${currentSelection.humidity}%",
                    style = MaterialTheme.typography.labelSmall.copy(color = SleekTextSecondary, fontSize = 10.sp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Individual Hourly Prediction Card in the LazyRow.
 */
@Composable
fun HourlyCardItem(
  item: HourlyForecastItem,
  index: Int,
  isNow: Boolean,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val (cardBg, borderColor, primaryTextColor) = if (isSelected) {
    Triple(SleekBlueContainer, SleekBluePrimary, SleekDeepInk)
  } else {
    Triple(Color.White, SleekCardBorder, SleekTextPrimary)
  }

  Surface(
    color = cardBg,
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
    shadowElevation = if (isSelected) 1.dp else 0.dp,
    modifier = modifier
      .width(82.dp)
      .height(154.dp)
      .clip(RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .testTag("hourly_card_$index")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(vertical = 10.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // 1. Time Display Pill
      if (isNow) {
        Surface(
          color = if (isSelected) SleekBluePrimary else SleekBlueContainer,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.height(20.dp)
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
          ) {
            Text(
              text = "NOW",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) Color.White else SleekDeepInk,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
              )
            )
          }
        }
      } else {
        Text(
          text = formatHourDisplay(item.time),
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isSelected) SleekDeepInk else SleekTextSecondary,
            fontSize = 11.sp
          ),
          maxLines = 1
        )
      }

      // 2. Weather Icon Prediction
      val weatherIcon = getPredictionWeatherIcon(item.weatherCode)
      val iconTint = when {
        isSelected -> SleekDeepInk
        item.weatherCode in listOf(95, 96, 99) -> SleekAlertRed
        item.weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> SleekBluePrimary
        item.weatherCode in listOf(0, 1) -> SunYellow
        else -> SleekBluePrimary
      }

      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(if (isSelected) Color.White.copy(alpha = 0.6f) else SleekCardBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = weatherIcon,
          contentDescription = item.conditionText,
          tint = iconTint,
          modifier = Modifier
            .size(24.dp)
            .testTag("hourly_icon_$index")
        )
      }

      // 3. Hourly Temperature
      Text(
        text = "${item.temperature.roundToInt()}°",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = primaryTextColor,
          fontSize = 16.sp,
          letterSpacing = (-0.5).sp
        ),
        modifier = Modifier.testTag("hourly_temp_$index")
      )

      // 4. Precipitation Chance or Humidity Badge
      if (item.precipitationProbability > 0) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.padding(bottom = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.WaterDrop,
            contentDescription = null,
            tint = SleekBluePrimary,
            modifier = Modifier.size(11.dp)
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(
            text = "${item.precipitationProbability}%",
            style = MaterialTheme.typography.labelSmall.copy(
              color = SleekBluePrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            )
          )
        }
      } else {
        Text(
          text = "${item.humidity}% Hum",
          style = MaterialTheme.typography.labelSmall.copy(
            color = SleekTextSecondary,
            fontWeight = FontWeight.Normal,
            fontSize = 9.sp
          ),
          maxLines = 1
        )
      }
    }
  }
}

/**
 * Maps WMO weather code to the corresponding Material 3 Icon.
 */
fun getPredictionWeatherIcon(weatherCode: Int): ImageVector {
  return when (weatherCode) {
    0 -> Icons.Filled.WbSunny
    1 -> Icons.Filled.WbSunny
    2 -> Icons.Filled.WbCloudy
    3 -> Icons.Filled.Cloud
    45, 48 -> Icons.Filled.Deblur
    51, 53, 55 -> Icons.Filled.Grain
    61, 63, 65 -> Icons.Filled.WaterDrop
    71, 73, 75 -> Icons.Filled.AcUnit
    80, 81, 82 -> Icons.Filled.WaterDrop
    95 -> Icons.Filled.Thunderstorm
    96, 99 -> Icons.Filled.CrisisAlert
    else -> Icons.Filled.WbSunny
  }
}

/**
 * Formats time string e.g. "14:00" to readable hour display e.g. "14:00" or "2 PM".
 */
private fun formatHourDisplay(timeStr: String): String {
  val clean = timeStr.trim()
  val hourPart = clean.substringBefore(":").toIntOrNull()
  return if (hourPart != null) {
    when {
      hourPart == 0 -> "12 AM"
      hourPart < 12 -> "${hourPart} AM"
      hourPart == 12 -> "12 PM"
      else -> "${hourPart - 12} PM"
    }
  } else {
    clean
  }
}
