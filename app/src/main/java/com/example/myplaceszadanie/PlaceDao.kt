package com.example.myplaceszadanie

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places ORDER BY timestamp DESC")
    fun getAllPlaces(): Flow<List<Place>>

    @Insert
    suspend fun insertPlace(place: Place)

    @Delete
    suspend fun deletePlace(place: Place)
}