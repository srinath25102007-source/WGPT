package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SectorMode
import com.example.data.repository.WeatherRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WeatherGPT", appName)
  }

  @Test
  fun `verify weather repository and sector advisory generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = WeatherRepository(context)
    val weather = repo.getDemoWeather(13.0827, 80.2707, "Chennai", "Tamil Nadu")

    assertNotNull(weather)
    assertEquals("Chennai", weather.locationName)
    assertEquals(31.4, weather.temperature, 0.1)

    // Verify Farmer advisory calculation
    val farmAdvisory = repo.generateSectorAdvisory(SectorMode.FARMER, weather)
    assertNotNull(farmAdvisory)
    assertTrue(farmAdvisory.bulletPoints.isNotEmpty())

    // Verify Marine advisory calculation
    val marineAdvisory = repo.generateSectorAdvisory(SectorMode.FISHER_MARINE, weather)
    assertNotNull(marineAdvisory)
    assertTrue(marineAdvisory.bulletPoints.isNotEmpty())
  }

  @Test
  fun `verify 24-hour hourly forecast generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = WeatherRepository(context)
    repo.isDemoMode = true

    kotlinx.coroutines.runBlocking {
      val hourly = repo.getHourlyForecast(13.0827, 80.2707)
      assertNotNull(hourly)
      assertEquals(24, hourly.size)
      // Verify first hour has valid fields
      val firstHour = hourly.first()
      assertNotNull(firstHour.time)
      assertTrue(firstHour.temperature > 0.0)
      assertNotNull(firstHour.conditionText)
    }
  }

  @Test
  fun `verify IMD radar stations and dynamic precipitation echoes`() {
    val stations = com.example.data.model.ImdStationsData.stations
    assertTrue(stations.isNotEmpty())
    val chennaiStation = stations.firstOrNull { it.stationCode == "DWR-CHN" }
    assertNotNull(chennaiStation)
    assertTrue(chennaiStation.reflectivityDbz > 0)
    assertTrue(chennaiStation.rainfallRateMmH > 0.0)

    val echoes = com.example.data.model.ImdStationsData.generateRadarEchoes(
      centerLat = 13.0827,
      centerLng = 80.2707,
      precipProb = 60,
      rainfallRate = 12.0,
      frameOffsetIndex = 0
    )
    assertTrue(echoes.isNotEmpty())
    assertTrue(echoes.any { it.reflectivityDbz >= 40 })
  }
}

