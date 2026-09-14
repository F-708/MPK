package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bells")
data class BellEntity(
    @PrimaryKey
    val lessonNumber: Int, // 1..8
    val timeStart: String, // e.g. "08:30"
    val timeEnd: String,   // e.g. "09:15"
    val breakDurationMinutes: Int = 10
)
