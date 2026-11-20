package com.ikeralvaro.calendario.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY start ASC")
    fun observeAll(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun byId(id: Long): Event?

    @Query("SELECT * FROM events WHERE reminderMinutes >= 0")
    suspend fun withReminders(): List<Event>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: Event): Long

    @Delete
    suspend fun delete(event: Event)
}
