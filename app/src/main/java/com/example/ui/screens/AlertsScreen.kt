package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.WeatherAlert
import com.example.ui.theme.*

@Composable
fun AlertsScreen(
  alerts: List<WeatherAlert>,
  onTestNotification: () -> Unit
) {
  var selectedSeverityFilter by remember { mutableStateOf<AlertSeverity?>(null) }
  var notificationsEnabled by remember { mutableStateOf(true) }

  val filteredAlerts = if (selectedSeverityFilter == null) {
    alerts
  } else {
    alerts.filter { it.severity == selectedSeverityFilter }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(SleekBackground)
      .testTag("alerts_screen_container"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header Banner
    item {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = SleekDeepInk,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(SleekAlertRed.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "IMD Early Warning System",
                  style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
                Text(
                  text = "Ministry of Earth Sciences Bulletins",
                  style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                )
              }
            }

            // Push Notification Switch
            Switch(
              checked = notificationsEnabled,
              onCheckedChange = { notificationsEnabled = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SleekBluePrimary
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Automated real-time notifications for cyclones, extreme rainfall (>64.5 mm), thunderstorms, and coastal fishermen advisories.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, lineHeight = 17.sp)
          )
        }
      }
    }

    // Severity Filter Chips
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          FilterChip(
            selected = selectedSeverityFilter == null,
            onClick = { selectedSeverityFilter = null },
            label = { Text("All (${alerts.size})", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
            shape = RoundedCornerShape(16.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SleekBluePrimary,
              selectedLabelColor = Color.White,
              containerColor = Color.White,
              labelColor = SleekTextPrimary
            ),
            border = FilterChipDefaults.filterChipBorder(
              borderColor = SleekCardBorder,
              selectedBorderColor = SleekBluePrimary,
              enabled = true,
              selected = selectedSeverityFilter == null
            )
          )
        }
        item {
          FilterChip(
            selected = selectedSeverityFilter == AlertSeverity.EXTREME,
            onClick = { selectedSeverityFilter = AlertSeverity.EXTREME },
            label = { Text("Red (Extreme)", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
            shape = RoundedCornerShape(16.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SleekAlertRed,
              selectedLabelColor = Color.White,
              containerColor = Color.White,
              labelColor = SleekTextPrimary
            )
          )
        }
        item {
          FilterChip(
            selected = selectedSeverityFilter == AlertSeverity.SEVERE,
            onClick = { selectedSeverityFilter = AlertSeverity.SEVERE },
            label = { Text("Orange (Severe)", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
            shape = RoundedCornerShape(16.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = WarningOrange,
              selectedLabelColor = Color.White,
              containerColor = Color.White,
              labelColor = SleekTextPrimary
            )
          )
        }
        item {
          FilterChip(
            selected = selectedSeverityFilter == AlertSeverity.MODERATE,
            onClick = { selectedSeverityFilter = AlertSeverity.MODERATE },
            label = { Text("Yellow (Watch)", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
            shape = RoundedCornerShape(16.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = WarningYellow,
              selectedLabelColor = SleekDeepInk,
              containerColor = Color.White,
              labelColor = SleekTextPrimary
            )
          )
        }
      }
    }

    // List of Active Alerts
    items(filteredAlerts) { alert ->
      val (accentColor, icon) = when (alert.severity) {
        AlertSeverity.EXTREME -> Pair(SleekAlertRed, Icons.Filled.CrisisAlert)
        AlertSeverity.SEVERE -> Pair(WarningOrange, Icons.Filled.Warning)
        AlertSeverity.MODERATE -> Pair(WarningYellow, Icons.Filled.Info)
        AlertSeverity.INFO -> Pair(SleekBluePrimary, Icons.Filled.CheckCircle)
      }

      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
        shadowElevation = 0.dp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("alert_card_${alert.id}")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Severity Pill & Location
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              color = accentColor.copy(alpha = 0.15f),
              shape = RoundedCornerShape(12.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = alert.severity.name,
                  color = accentColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              }
            }

            Text(
              text = alert.phenomenon,
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = SleekDeepInk,
                fontSize = 11.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Alert Title
          Text(
            text = alert.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = SleekDeepInk,
              fontSize = 16.sp
            )
          )

          Spacer(modifier = Modifier.height(4.dp))

          // Location & Validity Time
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = SleekTextSecondary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = alert.location,
              style = MaterialTheme.typography.bodySmall.copy(color = SleekTextPrimary, fontWeight = FontWeight.Medium)
            )
          }

          Spacer(modifier = Modifier.height(2.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Schedule, contentDescription = null, tint = SleekTextSecondary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${alert.issuedTime} • ${alert.validUntil}",
              style = MaterialTheme.typography.bodySmall.copy(color = SleekTextSecondary, fontSize = 11.sp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Description
          Text(
            text = alert.description,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = SleekTextPrimary,
              fontSize = 13.sp,
              lineHeight = 19.sp
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Recommended Action Box
          Surface(
            color = SleekCardBg,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "RECOMMENDED ACTION:",
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = alert.recommendedAction,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = SleekTextPrimary,
                  fontWeight = FontWeight.Medium,
                  fontSize = 12.sp
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Official Attribution
          Text(
            text = "Official Source: ${alert.source}",
            style = MaterialTheme.typography.bodySmall.copy(color = SleekTextSecondary, fontSize = 10.sp)
          )
        }
      }
    }

    // Emergency Helpline Numbers Card
    item {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "National Disaster Helplines (Emergency)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SleekDeepInk)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            HelplineBadge(name = "National Emergency", number = "112")
            HelplineBadge(name = "District DEOC", number = "1077")
            HelplineBadge(name = "Coast Guard Sea", number = "1554")
          }
        }
      }
    }
  }
}

@Composable
fun HelplineBadge(name: String, number: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = name, fontSize = 10.sp, color = SleekTextSecondary)
    Surface(
      color = SleekBlueContainer,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.padding(top = 4.dp)
    ) {
      Text(
        text = number,
        color = SleekDeepInk,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
      )
    }
  }
}
