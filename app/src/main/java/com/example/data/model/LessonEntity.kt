package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ..., 6 = Saturday
    val specificDate: String = "", // "yyyy-MM-dd" or empty for weekly schedule
    val lessonNumber: Int, // 1..8
    val groupName: String = "41О",
    val timeStart: String,
    val timeEnd: String,
    val subject: String,
    val teacher: String,
    val classroom: String,
    val isReplacement: Boolean = false, // Замена
    val hasSubgroups: Boolean = false, // Разделение на 2 кабинета
    val subgroup1Classroom: String = "",
    val subgroup1Teacher: String = "",
    val subgroup2Classroom: String = "",
    val subgroup2Teacher: String = "",
    val note: String = ""
)
