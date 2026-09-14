package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BellEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BellDao {
    @Query("SELECT * FROM bells ORDER BY lessonNumber ASC")
    fun getAllBells(): Flow<List<BellEntity>>

    @Query("SELECT * FROM bells ORDER BY lessonNumber ASC")
    suspend fun getAllBellsSync(): List<BellEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBells(bells: List<BellEntity>)

    @Update
    suspend fun updateBell(bell: BellEntity)

    @Query("SELECT COUNT(*) FROM bells")
    suspend fun getBellsCount(): Int
}
