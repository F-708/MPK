package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_tasks")
data class StudentTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subject: String,
    val taskType: String = "HOMEWORK", // "HOMEWORK", "COURSEWORK", "PRACTICAL", "TEST", "PROJECT"
    val deadline: String = "На следующий урок", // or date string "2026-09-25"
    val isCompleted: Boolean = false,
    val notes: String = "",
    val subtasksJson: String = "[]", // [{"id":"1","title":"Собрать литературу","isDone":true,"deadline":"2026-09-20"}]
    val photoUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class SubtaskItem(
    val id: String,
    val title: String,
    val isDone: Boolean = false,
    val deadline: String = ""
)
