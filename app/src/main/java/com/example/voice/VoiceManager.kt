package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.example.data.model.SupportedLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

  private var speechRecognizer: SpeechRecognizer? = null
  private var textToSpeech: TextToSpeech? = null
  private var isTtsReady = false

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening

  private val _recognizedText = MutableStateFlow("")
  val recognizedText: StateFlow<String> = _recognizedText

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking

  init {
    textToSpeech = TextToSpeech(context, this)
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      isTtsReady = true
      textToSpeech?.language = Locale("en", "IN")
    }
  }

  fun startListening(language: SupportedLanguage, onResult: (String) -> Unit) {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      // Return simulated query for demo if emulator/system lacks native mic speech recognizer
      _isListening.value = false
      val demoQuery = when (language) {
        SupportedLanguage.TAMIL -> "நாளைக்கு மழை வருமா?"
        SupportedLanguage.HINDI -> "क्या कल बारिश होगी?"
        else -> "Will it rain today?"
      }
      onResult(demoQuery)
      return
    }

    speechRecognizer?.destroy()
    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
      setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { _isListening.value = true }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { _isListening.value = false }
        override fun onError(error: Int) {
          _isListening.value = false
        }
        override fun onResults(results: Bundle?) {
          _isListening.value = false
          val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          val text = matches?.firstOrNull() ?: ""
          if (text.isNotBlank()) {
            _recognizedText.value = text
            onResult(text)
          }
        }
        override fun onPartialResults(partialResults: Bundle?) {
          val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          val text = matches?.firstOrNull() ?: ""
          if (text.isNotBlank()) {
            _recognizedText.value = text
          }
        }
        override fun onEvent(eventType: Int, params: Bundle?) {}
      })
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      val langTag = when (language) {
        SupportedLanguage.TAMIL -> "ta-IN"
        SupportedLanguage.HINDI -> "hi-IN"
        SupportedLanguage.TELUGU -> "te-IN"
        SupportedLanguage.KANNADA -> "kn-IN"
        SupportedLanguage.MALAYALAM -> "ml-IN"
        SupportedLanguage.BENGALI -> "bn-IN"
        SupportedLanguage.MARATHI -> "mr-IN"
        SupportedLanguage.GUJARATI -> "gu-IN"
        else -> "en-IN"
      }
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
    }

    try {
      speechRecognizer?.startListening(intent)
      _isListening.value = true
    } catch (_: Exception) {
      _isListening.value = false
    }
  }

  fun stopListening() {
    speechRecognizer?.stopListening()
    _isListening.value = false
  }

  fun speak(text: String, language: SupportedLanguage) {
    if (!isTtsReady || textToSpeech == null) return

    val loc = when (language) {
      SupportedLanguage.TAMIL -> Locale("ta", "IN")
      SupportedLanguage.HINDI -> Locale("hi", "IN")
      SupportedLanguage.TELUGU -> Locale("te", "IN")
      SupportedLanguage.KANNADA -> Locale("kn", "IN")
      SupportedLanguage.MALAYALAM -> Locale("ml", "IN")
      SupportedLanguage.BENGALI -> Locale("bn", "IN")
      SupportedLanguage.MARATHI -> Locale("mr", "IN")
      SupportedLanguage.GUJARATI -> Locale("gu", "IN")
      else -> Locale("en", "IN")
    }

    try {
      textToSpeech?.language = loc
      textToSpeech?.speak(text.take(300), TextToSpeech.QUEUE_FLUSH, null, "WeatherGptTts")
      _isSpeaking.value = true
    } catch (_: Exception) {
      _isSpeaking.value = false
    }
  }

  fun stopSpeaking() {
    textToSpeech?.stop()
    _isSpeaking.value = false
  }

  fun destroy() {
    speechRecognizer?.destroy()
    textToSpeech?.stop()
    textToSpeech?.shutdown()
  }
}
