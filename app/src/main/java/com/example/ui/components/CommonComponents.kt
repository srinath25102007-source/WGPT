package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SavedLocationEntity
import com.example.data.model.SectorMode
import com.example.data.model.SupportedLanguage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherGptTopBar(
  currentLocation: SavedLocationEntity,
  savedLocations: List<SavedLocationEntity>,
  onLocationSelected: (SavedLocationEntity) -> Unit,
  onSearchQuery: (String) -> Unit,
  selectedLanguage: SupportedLanguage,
  onLanguageSelected: (SupportedLanguage) -> Unit,
  isDemoMode: Boolean,
  onToggleDemoMode: () -> Unit,
  isSpeaking: Boolean,
  onStopSpeaking: () -> Unit
) {
  var showLocationDialog by remember { mutableStateOf(false) }
  var showLanguageDialog by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }

  Surface(
    color = SleekBackground,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
      // Sleek Interface Top Row: Location Header on Left, Notification & Tool Icons on Right
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Location Info (Sleek Interface pattern)
        Column(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { showLocationDialog = true }
            .testTag("location_selector_banner")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.LocationOn,
              contentDescription = "Location",
              tint = SleekBluePrimary,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "${currentLocation.name}, ${currentLocation.state}",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = SleekTextPrimary,
                fontSize = 17.sp,
                letterSpacing = (-0.2).sp
              )
            )
            Icon(
              imageVector = Icons.Filled.ArrowDropDown,
              contentDescription = "Select Station",
              tint = SleekTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
          Text(
            text = "IMD Hyper-local Station • ${currentLocation.category}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = SleekTextSecondary,
              fontSize = 11.sp
            ),
            modifier = Modifier.padding(start = 24.dp)
          )
        }

        // Actions: Speaking mute, Language chip, Demo toggle, Notifications
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (isSpeaking) {
            IconButton(
              onClick = onStopSpeaking,
              modifier = Modifier.size(36.dp).testTag("stop_speaking_button")
            ) {
              Icon(
                imageVector = Icons.Filled.VolumeUp,
                contentDescription = "Mute Voice",
                tint = SleekAlertRed,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          // Language Selector Chip
          Surface(
            color = SleekCardBg,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .clickable { showLanguageDialog = true }
              .testTag("language_selector_chip")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Outlined.Translate, contentDescription = null, tint = SleekTextSecondary, modifier = Modifier.size(14.dp))
              Text(
                text = selectedLanguage.nativeName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
              )
            }
          }

          // Demo Mode Toggle Button
          IconButton(
            onClick = onToggleDemoMode,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(if (isDemoMode) SleekBlueContainer else SleekCardBg)
              .testTag("demo_mode_toggle")
          ) {
            Icon(
              imageVector = if (isDemoMode) Icons.Filled.Science else Icons.Outlined.Science,
              contentDescription = "Toggle Demo Fixtures",
              tint = if (isDemoMode) SleekBluePrimary else SleekTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }

          // Sleek Notification Button with Red Dot Badge
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(SleekCardBorder)
              .clickable { showLocationDialog = true },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.Notifications,
              contentDescription = "Notifications",
              tint = SleekTextSecondary,
              modifier = Modifier.size(20.dp)
            )
            // Sleek Red Dot
            Box(
              modifier = Modifier
                .size(9.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 4.dp)
                .clip(CircleShape)
                .background(SleekAlertRed)
                .border(1.5.dp, SleekBackground, CircleShape)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
    }
  }

  // Location Selection Dialog
  if (showLocationDialog) {
    AlertDialog(
      onDismissRequest = { showLocationDialog = false },
      title = { Text("Select Meteorological Station / City", fontWeight = FontWeight.Bold, color = SleekTextPrimary) },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Indian city / district...", color = SleekTextSecondary) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = SleekBluePrimary) },
            trailingIcon = {
              if (searchQuery.isNotBlank()) {
                IconButton(onClick = {
                  onSearchQuery(searchQuery)
                  showLocationDialog = false
                  searchQuery = ""
                }) {
                  Icon(Icons.Filled.Check, contentDescription = "Select", tint = SleekBluePrimary)
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = SleekBluePrimary,
              unfocusedBorderColor = SleekCardBorder,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = SleekCardBg
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
          )

          Text(
            text = "Key Meteorological Stations",
            style = MaterialTheme.typography.labelMedium.copy(color = SleekTextSecondary, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 6.dp)
          )

          savedLocations.forEach { loc ->
            Surface(
              color = if (loc.name == currentLocation.name) SleekBlueContainer else Color.Transparent,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  onLocationSelected(loc)
                  showLocationDialog = false
                }
            ) {
              Row(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Filled.Place,
                  contentDescription = null,
                  tint = if (loc.name == currentLocation.name) SleekBluePrimary else SleekTextSecondary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "${loc.name}, ${loc.state}",
                    fontWeight = if (loc.name == currentLocation.name) FontWeight.Bold else FontWeight.Medium,
                    color = if (loc.name == currentLocation.name) SleekDeepInk else SleekTextPrimary
                  )
                  Text(text = loc.category, fontSize = 11.sp, color = SleekTextSecondary)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showLocationDialog = false }) {
          Text("Cancel", color = SleekBluePrimary)
        }
      }
    )
  }

  // Language Selection Dialog
  if (showLanguageDialog) {
    AlertDialog(
      onDismissRequest = { showLanguageDialog = false },
      title = { Text("Choose Language / மொழி / भाषा", fontWeight = FontWeight.Bold, color = SleekTextPrimary) },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          SupportedLanguage.values().forEach { lang ->
            Surface(
              color = if (lang == selectedLanguage) SleekBlueContainer else Color.Transparent,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  onLanguageSelected(lang)
                  showLanguageDialog = false
                }
            ) {
              Row(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(
                    text = lang.nativeName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (lang == selectedLanguage) SleekDeepInk else SleekTextPrimary
                  )
                  Text(text = lang.displayName, fontSize = 12.sp, color = SleekTextSecondary)
                }
                if (lang == selectedLanguage) {
                  Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SleekBluePrimary)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showLanguageDialog = false }) {
          Text("Done", color = SleekBluePrimary)
        }
      }
    )
  }
}

@Composable
fun SectorModeSelector(
  selectedMode: SectorMode,
  onModeSelected: (SectorMode) -> Unit
) {
  LazyRow(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(SectorMode.values()) { mode ->
      val isSelected = mode == selectedMode
      val icon: ImageVector = when (mode) {
        SectorMode.PERSONAL -> Icons.Filled.Person
        SectorMode.FARMER -> Icons.Filled.Agriculture
        SectorMode.FISHER_MARINE -> Icons.Filled.Sailing
        SectorMode.DISASTER -> Icons.Filled.Warning
        SectorMode.AVIATION -> Icons.Filled.Flight
        SectorMode.INDUSTRIAL -> Icons.Filled.Factory
        SectorMode.RESEARCHER -> Icons.Filled.Analytics
      }

      Surface(
        color = if (isSelected) SleekBluePrimary else SleekCardBg,
        shape = RoundedCornerShape(20.dp),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder) else null,
        shadowElevation = if (isSelected) 1.dp else 0.dp,
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .clickable { onModeSelected(mode) }
          .testTag("sector_mode_${mode.name}")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = icon,
            contentDescription = mode.displayName,
            tint = if (isSelected) Color.White else SleekBluePrimary,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = mode.displayName,
            style = MaterialTheme.typography.labelMedium.copy(
              color = if (isSelected) Color.White else SleekTextPrimary,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 12.sp
            )
          )
        }
      }
    }
  }
}

@Composable
fun WeatherStatCard(
  title: String,
  value: String,
  subtitle: String? = null,
  icon: ImageVector,
  iconColor: Color = SleekBluePrimary,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = Color.White,
    border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
    shadowElevation = 0.dp,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodySmall.copy(color = SleekTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium),
          maxLines = 1
        )
        Box(
          modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(SleekCardBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = SleekDeepInk,
          fontSize = 17.sp
        )
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(
            color = SleekTextSecondary,
            fontSize = 10.sp
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}
