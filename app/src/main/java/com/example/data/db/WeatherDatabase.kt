package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionId: String = "default_session",
  val sender: String, // "user" or "weathergpt"
  val message: String,
  val timestamp: Long = System.currentTimeMillis(),
  val toolsCalled: String? = null,
  val sourceAttribution: String? = null,
  val structuredWeatherJson: String? = null
)

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val state: String,
  val latitude: Double,
  val longitude: Double,
  val isCurrentLocation: Boolean = false,
  val category: String = "Saved" // "Home", "Farm", "Port", "Village", "Work"
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
  @PrimaryKey val locationKey: String,
  val cachedAt: Long = System.currentTimeMillis(),
  val jsonPayload: String,
  val source: String
)

@Entity(tableName = "weather_alerts")
data class AlertEntity(
  @PrimaryKey val id: String,
  val title: String,
  val severity: String,
  val location: String,
  val issuedTime: String,
  val validUntil: String,
  val source: String,
  val description: String,
  val action: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ChatDao {
  @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
  fun getMessages(sessionId: String): Flow<List<ChatMessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(msg: ChatMessageEntity): Long

  @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
  suspend fun clearSession(sessionId: String)
}

@Dao
interface SavedLocationDao {
  @Query("SELECT * FROM saved_locations ORDER BY isCurrentLocation DESC, id ASC")
  fun getAllLocations(): Flow<List<SavedLocationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLocation(loc: SavedLocationEntity): Long

  @Query("DELETE FROM saved_locations WHERE id = :id")
  suspend fun deleteLocation(id: Long)

  @Query("UPDATE saved_locations SET isCurrentLocation = 0")
  suspend fun resetCurrentLocation()

  @Query("UPDATE saved_locations SET isCurrentLocation = 1 WHERE id = :id")
  suspend fun setCurrentLocation(id: Long)
}

@Dao
interface WeatherCacheDao {
  @Query("SELECT * FROM weather_cache WHERE locationKey = :key LIMIT 1")
  suspend fun getCache(key: String): WeatherCacheEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveCache(cache: WeatherCacheEntity)

  @Query("DELETE FROM weather_cache WHERE cachedAt < :expiryTimestamp")
  suspend fun clearExpired(expiryTimestamp: Long)
}

@Dao
interface AlertDao {
  @Query("SELECT * FROM weather_alerts ORDER BY timestamp DESC")
  fun getAllAlerts(): Flow<List<AlertEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlerts(alerts: List<AlertEntity>)

  @Query("DELETE FROM weather_alerts")
  suspend fun clearAlerts()
}

@Database(
  entities = [
    ChatMessageEntity::class,
    SavedLocationEntity::class,
    WeatherCacheEntity::class,
    AlertEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class WeatherAppDatabase : RoomDatabase() {
  abstract fun chatDao(): ChatDao
  abstract fun savedLocationDao(): SavedLocationDao
  abstract fun weatherCacheDao(): WeatherCacheDao
  abstract fun alertDao(): AlertDao

  companion object {
    @Volatile
    private var INSTANCE: WeatherAppDatabase? = null

    fun getDatabase(context: Context): WeatherAppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          WeatherAppDatabase::class.java,
          "weathergpt_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
