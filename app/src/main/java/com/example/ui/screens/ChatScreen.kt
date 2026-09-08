package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ChatMessageEntity
import com.example.data.model.SupportedLanguage
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
  messages: List<ChatMessageEntity>,
  onSendMessage: (String) -> Unit,
  onStartVoiceInput: () -> Unit,
  isListening: Boolean,
  isAiThinking: Boolean,
  onSpeakMessage: (String) -> Unit,
  onClearChat: () -> Unit,
  selectedLanguage: SupportedLanguage
) {
  var inputQuery by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  val suggestedQueries = listOf(
    "Will it rain today in Mumbai?",
    "Can I spray pesticide on my crop tomorrow?",
    "Is it safe for fishermen to go to sea tomorrow?",
    "What are today's official IMD warnings?",
    "நாளைக்கு மழை வருமா?",
    "क्या कल बारिश होगी?"
  )

  // Auto-scroll to bottom when new messages arrive
  LaunchedEffect(messages.size, isAiThinking) {
    if (messages.isNotEmpty()) {
      coroutineScope.launch {
        listState.animateScrollToItem(messages.size - 1)
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SleekBackground)
      .testTag("chat_screen_container")
  ) {
    // Header with Clear Action & Language indicator
    Surface(
      color = SleekBackground,
      border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(SleekBluePrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "WeatherGPT Grounded LLM",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = SleekDeepInk,
              fontSize = 15.sp,
              letterSpacing = (-0.2).sp
            )
          )
        }

        IconButton(onClick = onClearChat, modifier = Modifier.size(32.dp).testTag("clear_chat_button")) {
          Icon(Icons.Outlined.DeleteSweep, contentDescription = "Clear Conversation", tint = SleekTextSecondary)
        }
      }
    }

    // Suggested Queries Bar
    LazyRow(
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(suggestedQueries) { query ->
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
          modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSendMessage(query) }
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
          ) {
            Text(query, fontSize = 11.sp, color = SleekTextPrimary, fontWeight = FontWeight.Medium, maxLines = 1)
          }
        }
      }
    }

    // Message List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 12.dp),
      contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(messages) { msg ->
        ChatMessageBubble(
          message = msg,
          onSpeak = { onSpeakMessage(msg.message) }
        )
      }

      if (isAiThinking) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SleekBluePrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
              color = SleekBlueContainer,
              shape = RoundedCornerShape(18.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SleekBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Analyzing IMD observations & radar...",
                  style = MaterialTheme.typography.bodySmall.copy(color = SleekDeepInk, fontWeight = FontWeight.Medium)
                )
              }
            }
          }
        }
      }
    }

    // Bottom Input Bar
    Surface(
      color = Color.White,
      border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 70.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Voice Mic Button with pulsing animation
        val infiniteTransition = rememberInfiniteTransition()
        val micScale by infiniteTransition.animateFloat(
          initialValue = 1f,
          targetValue = if (isListening) 1.25f else 1f,
          animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
          )
        )

        IconButton(
          onClick = onStartVoiceInput,
          modifier = Modifier
            .scale(micScale)
            .size(42.dp)
            .background(
              color = if (isListening) SleekAlertRed else SleekCardBg,
              shape = CircleShape
            )
            .testTag("voice_mic_button")
        ) {
          Icon(
            imageVector = if (isListening) Icons.Filled.Mic else Icons.Filled.MicNone,
            contentDescription = "Voice Input",
            tint = if (isListening) Color.White else SleekBluePrimary,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Query Text Field
        OutlinedTextField(
          value = inputQuery,
          onValueChange = { inputQuery = it },
          placeholder = {
            Text(
              text = if (isListening) "Listening to voice..." else "Ask weather, rain, crops...",
              fontSize = 13.sp,
              color = SleekTextSecondary
            )
          },
          maxLines = 3,
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SleekBluePrimary,
            unfocusedBorderColor = SleekCardBorder,
            focusedContainerColor = SleekCardBg,
            unfocusedContainerColor = SleekCardBg
          ),
          modifier = Modifier
            .weight(1f)
            .testTag("chat_input_textfield")
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Send Button
        IconButton(
          onClick = {
            if (inputQuery.isNotBlank()) {
              onSendMessage(inputQuery)
              inputQuery = ""
            }
          },
          enabled = inputQuery.isNotBlank() && !isAiThinking,
          modifier = Modifier
            .size(42.dp)
            .background(
              color = if (inputQuery.isNotBlank()) SleekBluePrimary else SleekCardBorder,
              shape = CircleShape
            )
            .testTag("send_chat_button")
        ) {
          Icon(
            imageVector = Icons.Filled.Send,
            contentDescription = "Send",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun ChatMessageBubble(
  message: ChatMessageEntity,
  onSpeak: () -> Unit
) {
  val isUser = message.sender == "user"

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Top
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(SleekBluePrimary),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Column(
      horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
      modifier = Modifier.widthIn(max = 300.dp)
    ) {
      Surface(
        color = if (isUser) SleekBluePrimary else Color.White,
        shape = RoundedCornerShape(
          topStart = 20.dp,
          topEnd = 20.dp,
          bottomStart = if (isUser) 20.dp else 4.dp,
          bottomEnd = if (isUser) 4.dp else 20.dp
        ),
        border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder) else null,
        shadowElevation = 0.dp
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = message.message,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = if (isUser) Color.White else SleekTextPrimary,
              fontSize = 14.sp,
              lineHeight = 20.sp
            )
          )

          // If WeatherGPT message, show TTS audio speak button
          if (!isUser) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (message.sourceAttribution != null) {
                Surface(
                  color = SleekCardBg,
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = message.sourceAttribution,
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = SleekTextSecondary,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              IconButton(onClick = onSpeak, modifier = Modifier.size(28.dp)) {
                Icon(
                  imageVector = Icons.Filled.VolumeUp,
                  contentDescription = "Read Aloud",
                  tint = SleekBluePrimary,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
