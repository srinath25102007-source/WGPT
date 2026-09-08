package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClimateTrendPoint
import com.example.ui.theme.*

@Composable
fun ClimateScreen(
  climateTrends: List<ClimateTrendPoint>,
  locationName: String
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(SleekBackground)
      .testTag("climate_screen_container"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header Banner
    item {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Climate Intelligence & Trends",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SleekDeepInk, fontSize = 16.sp)
              )
              Text(
                text = "Historical Analysis for $locationName (2000–2025)",
                style = MaterialTheme.typography.bodySmall.copy(color = SleekTextSecondary, fontSize = 12.sp)
              )
            }
            Surface(
              color = SleekBlueContainer,
              shape = CircleShape,
              modifier = Modifier.size(38.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ShowChart, contentDescription = null, tint = SleekBluePrimary, modifier = Modifier.size(20.dp))
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Long-term temperature anomalies and monsoon precipitation shifts based on IMD 30-year climatological normal reference data.",
            style = MaterialTheme.typography.bodySmall.copy(color = SleekTextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
          )
        }
      }
    }

    // Key Climate Metrics Row
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricHighlightCard(
          title = "25-Yr Warming",
          value = "+0.84°C",
          subtitle = "Above Baseline",
          icon = Icons.Filled.DeviceThermostat,
          color = WarningOrange,
          modifier = Modifier.weight(1f)
        )
        MetricHighlightCard(
          title = "Monsoon Deficit",
          value = "+4.2%",
          subtitle = "Variance/Year",
          icon = Icons.Filled.WaterDrop,
          color = SleekBluePrimary,
          modifier = Modifier.weight(1f)
        )
        MetricHighlightCard(
          title = "Extreme Events",
          value = "4 Major",
          subtitle = "Floods & Cyclones",
          icon = Icons.Filled.CrisisAlert,
          color = SleekAlertRed,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Interactive Temperature Anomaly Curve Chart
    item {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Annual Mean Temperature Anomaly (°C)",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SleekDeepInk)
            )
            Text(
              text = "2000–2025 Trend",
              style = MaterialTheme.typography.bodySmall.copy(color = SleekBluePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Native Compose Canvas Line Chart
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(160.dp)
              .padding(vertical = 8.dp)
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height

              // Draw baseline 0.0 line
              val zeroY = h * 0.75f
              drawLine(
                color = SleekCardBorder,
                start = Offset(0f, zeroY),
                end = Offset(w, zeroY),
                strokeWidth = 1.5f
              )

              if (climateTrends.isNotEmpty()) {
                val stepX = w / (climateTrends.size - 1)
                val path = Path()

                climateTrends.forEachIndexed { i, pt ->
                  val normalized = (pt.anomalyTemp / 1.3).coerceIn(0.0, 1.0).toFloat()
                  val x = i * stepX
                  val y = zeroY - normalized * (h * 0.65f)

                  if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

                  if (pt.isExtremeYear) {
                    drawCircle(color = SleekAlertRed, radius = 4.dp.toPx(), center = Offset(x, y))
                  }
                }

                drawPath(
                  path = path,
                  color = SleekBluePrimary,
                  style = Stroke(width = 3.dp.toPx())
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "2000", fontSize = 10.sp, color = SleekTextSecondary)
            Text(text = "2008", fontSize = 10.sp, color = SleekTextSecondary)
            Text(text = "2016", fontSize = 10.sp, color = SleekTextSecondary)
            Text(text = "2025", fontSize = 10.sp, color = SleekTextSecondary)
          }
        }
      }
    }

    // Historical Decadal Observations Table
    item {
      Text(
        text = "Decadal Climatological Milestones",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SleekDeepInk)
      )
    }

    val milestonePoints = climateTrends.filter { it.year in listOf(2000, 2005, 2010, 2015, 2020, 2023, 2025) }
    items(milestonePoints) { point ->
      Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              color = if (point.isExtremeYear) SleekAlertRed.copy(alpha = 0.15f) else SleekBlueContainer,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "${point.year}",
                fontWeight = FontWeight.Bold,
                color = if (point.isExtremeYear) SleekAlertRed else SleekDeepInk,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Annual Rainfall: ${point.rainfallMm.toInt()} mm",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = SleekTextPrimary)
              )
              if (point.isExtremeYear) {
                Text(
                  text = "Extreme Meteorological Event Year",
                  style = MaterialTheme.typography.bodySmall.copy(color = SleekAlertRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
              }
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "+${point.anomalyTemp}°C",
              fontWeight = FontWeight.Bold,
              color = if (point.anomalyTemp > 0.8) WarningOrange else SleekDeepInk,
              fontSize = 13.sp
            )
            Text(text = "Mean ${point.avgTemperature}°C", fontSize = 10.sp, color = SleekTextSecondary)
          }
        }
      }
    }

    // Scientific Disclaimer Card
    item {
      Surface(
        color = SleekCardBg,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Filled.Info, contentDescription = null, tint = SleekBluePrimary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Scientific Clarification: Daily weather predictions describe short-term atmospheric states, whereas climate trends represent verified statistical patterns computed over 20-30 year observation intervals.",
            style = MaterialTheme.typography.bodySmall.copy(color = SleekTextPrimary, fontSize = 11.sp, lineHeight = 16.sp)
          )
        }
      }
    }
  }
}

@Composable
fun MetricHighlightCard(
  title: String,
  value: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    color = Color.White,
    shape = RoundedCornerShape(20.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(SleekCardBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = value, fontWeight = FontWeight.Bold, color = SleekDeepInk, fontSize = 15.sp)
      Text(text = title, fontSize = 10.sp, color = SleekTextSecondary, maxLines = 1)
      Text(text = subtitle, fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
  }
}
