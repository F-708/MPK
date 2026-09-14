package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE groupName = :group ORDER BY dayOfWeek ASC, lessonNumber ASC")
    fun getAllLessonsByGroup(group: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE groupName = :group AND dayOfWeek = :day ORDER BY lessonNumber ASC")
    fun getLessonsByDay(group: String, day: Int): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE groupName = :group AND dayOfWeek = :day ORDER BY lessonNumber ASC")
    suspend fun getLessonsByDaySync(group: String, day: Int): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: Long): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Query("DELETE FROM lessons WHERE id = :id")
    suspend fun deleteLessonById(id: Long)

    @Query("DELETE FROM lessons WHERE groupName = :group")
    suspend fun deleteAllForGroup(group: String)

    @Query("SELECT COUNT(*) FROM lessons WHERE groupName = :group")
    fun getLessonCountForGroup(group: String): Flow<Int>

    @Query("SELECT DISTINCT subject FROM lessons WHERE groupName = :group ORDER BY subject ASC")
    fun getSubjectsForGroup(group: String): Flow<List<String>>

    @Query("SELECT DISTINCT groupName FROM lessons ORDER BY groupName ASC")
    fun getAllAvailableGroups(): Flow<List<String>>
}
