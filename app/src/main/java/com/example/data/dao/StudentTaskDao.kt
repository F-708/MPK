package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StudentTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentTaskDao {
    @Query("SELECT * FROM student_tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<StudentTaskEntity>>

    @Query("SELECT * FROM student_tasks WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<StudentTaskEntity>>

    @Query("SELECT * FROM student_tasks WHERE isCompleted = 0 ORDER BY createdAt DESC")
    suspend fun getActiveTasksSync(): List<StudentTaskEntity>

    @Query("SELECT * FROM student_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): StudentTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudentTaskEntity): Long

    @Update
    suspend fun updateTask(task: StudentTaskEntity)

    @Query("UPDATE student_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun toggleCompleted(id: Long, completed: Boolean)

    @Query("DELETE FROM student_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}
