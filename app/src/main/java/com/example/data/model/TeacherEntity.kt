package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey
    val name: String,
    val subject: String,
    val lessonsConducted: Int = 0,
    val isPrimaryTeacher: Boolean = true,
    val replacementsCount: Int = 0
)
