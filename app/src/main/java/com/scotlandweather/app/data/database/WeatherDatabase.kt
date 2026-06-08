package com.scotlandweather.app.data.database

import android.content.Context
import androidx.room.*

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val locationName: String,
    val jsonData: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val locationName: String,
    val order: Int = 0
)

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE locationName = :name")
    suspend fun getCachedWeather(name: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheWeather(entity: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE timestamp < :cutoff")
    suspend fun clearOldCache(cutoff: Long)

    @Query("SELECT * FROM favorites ORDER BY `order` ASC")
    suspend fun getFavorites(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun removeFavorite(favorite: FavoriteEntity)
}

@Database(entities = [WeatherCacheEntity::class, FavoriteEntity::class], version = 1)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getInstance(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
