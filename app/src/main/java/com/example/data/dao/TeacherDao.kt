package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TeacherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY lessonsConducted DESC, name ASC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE name = :name LIMIT 1")
    suspend fun getTeacherByName(name: String): TeacherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherEntity>)

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    @Query("UPDATE teachers SET lessonsConducted = lessonsConducted + 1 WHERE name = :name")
    suspend fun incrementLessonsConducted(name: String)

    @Query("DELETE FROM teachers WHERE name = :name")
    suspend fun deleteTeacher(name: String)

    @Query("SELECT SUM(lessonsConducted) FROM teachers")
    fun getTotalLessonsConducted(): Flow<Int?>
}
