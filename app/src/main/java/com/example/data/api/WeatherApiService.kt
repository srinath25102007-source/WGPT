package com.example.data.api

import com.example.data.model.OpenMeteoResponse
import com.example.data.model.RainViewerResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface OpenMeteoApi {
  @GET("v1/forecast")
  suspend fun getForecast(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m",
    @Query("hourly") hourly: String = "temperature_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m,relative_humidity_2m",
    @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max,uv_index_max,wind_speed_10m_max,sunrise,sunset",
    @Query("timezone") timezone: String = "auto"
  ): OpenMeteoResponse

  @GET("v1/archive")
  suspend fun getHistoricalArchive(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("start_date") startDate: String,
    @Query("end_date") endDate: String,
    @Query("daily") daily: String = "temperature_2m_mean,precipitation_sum",
    @Query("timezone") timezone: String = "auto"
  ): OpenMeteoResponse
}

interface RainViewerApi {
  @GET("public/weather-maps.json")
  suspend fun getWeatherMaps(): RainViewerResponse
}

object ApiClient {
  private val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BASIC
    })
    .build()

  val openMeteo: OpenMeteoApi by lazy {
    Retrofit.Builder()
      .baseUrl("https://api.open-meteo.com/")
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(OpenMeteoApi::class.java)
  }

  val rainViewer: RainViewerApi by lazy {
    Retrofit.Builder()
      .baseUrl("https://api.rainviewer.com/")
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(RainViewerApi::class.java)
  }

  val moshiInstance: Moshi get() = moshi
}
