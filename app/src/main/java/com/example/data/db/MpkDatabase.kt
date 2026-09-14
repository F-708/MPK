package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BellDao
import com.example.data.dao.LessonDao
import com.example.data.dao.StudentTaskDao
import com.example.data.dao.TeacherDao
import com.example.data.model.BellEntity
import com.example.data.model.LessonEntity
import com.example.data.model.StudentTaskEntity
import com.example.data.model.TeacherEntity

@Database(
    entities = [
        LessonEntity::class,
        TeacherEntity::class,
        StudentTaskEntity::class,
        BellEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MpkDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun teacherDao(): TeacherDao
    abstract fun studentTaskDao(): StudentTaskDao
    abstract fun bellDao(): BellDao

    companion object {
        @Volatile
        private var INSTANCE: MpkDatabase? = null

        fun getInstance(context: Context): MpkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MpkDatabase::class.java,
                    "mpk_schedule_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
