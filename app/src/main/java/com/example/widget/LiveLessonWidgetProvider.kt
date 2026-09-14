package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.MpkDatabase
import com.example.data.model.LessonEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LiveLessonWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val scope = CoroutineScope(Dispatchers.IO)
        val db = MpkDatabase.getInstance(context)

        scope.launch {
            for (widgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, widgetId, db)
            }
        }
    }

    private suspend fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        db: MpkDatabase
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_live_lesson)
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val selectedGroup = prefs.getString("selected_group", "41О") ?: "41О"

        val cal = Calendar.getInstance()
        val currentDay = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1
        }

        val lessons = db.lessonDao().getLessonsByDaySync(selectedGroup, currentDay)
        val bells = db.bellDao().getAllBellsSync()

        val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        var activeLesson: LessonEntity? = null
        var nextLesson: LessonEntity? = null
        var isRecess = false
        var minutesLeft = 0

        for (i in lessons.indices) {
            val lesson = lessons[i]
            val bell = bells.find { it.lessonNumber == lesson.lessonNumber }
            val (startMin, endMin) = if (bell != null) {
                Pair(timeToMinutes(bell.timeStart), timeToMinutes(bell.timeEnd))
            } else {
                Pair(timeToMinutes(lesson.timeStart), timeToMinutes(lesson.timeEnd))
            }

            if (nowMinutes in startMin..endMin) {
                activeLesson = lesson
                minutesLeft = endMin - nowMinutes
                nextLesson = lessons.getOrNull(i + 1)
                break
            } else if (nowMinutes < startMin) {
                // Before this lesson: recess or start of day
                isRecess = true
                minutesLeft = startMin - nowMinutes
                nextLesson = lesson
                break
            }
        }

        if (activeLesson != null) {
            views.setTextViewText(R.id.live_status_indicator, "СЕЙЧАС УРОК")
            views.setTextViewText(R.id.live_time_left, "До звонка: $minutesLeft мин")
            views.setTextViewText(R.id.live_subject, activeLesson.subject)
            views.setTextViewText(R.id.live_lesson_num, "${activeLesson.lessonNumber} урок (${activeLesson.timeStart} - ${activeLesson.timeEnd})")

            val room = if (activeLesson.hasSubgroups) "215 / 308" else activeLesson.classroom
            views.setTextViewText(R.id.live_classroom, room)

            if (nextLesson != null) {
                val nextRoom = if (nextLesson.hasSubgroups) "215/308" else nextLesson.classroom
                views.setTextViewText(R.id.live_next_up, "Далее: ${nextLesson.subject} ($nextRoom)")
            } else {
                views.setTextViewText(R.id.live_next_up, "После этого уроки закончены 🎉")
            }
        } else if (isRecess && nextLesson != null) {
            views.setTextViewText(R.id.live_status_indicator, "ПЕРЕМЕНА")
            views.setTextViewText(R.id.live_time_left, "Звонок через: $minutesLeft мин")
            views.setTextViewText(R.id.live_subject, "Следующий: ${nextLesson.subject}")
            views.setTextViewText(R.id.live_lesson_num, "${nextLesson.lessonNumber} урок (${nextLesson.timeStart} - ${nextLesson.timeEnd})")

            val room = if (nextLesson.hasSubgroups) "215 / 308" else nextLesson.classroom
            views.setTextViewText(R.id.live_classroom, room)
            views.setTextViewText(R.id.live_next_up, "Преподаватель: ${nextLesson.teacher}")
        } else {
            // No lessons or after school
            views.setTextViewText(R.id.live_status_indicator, "ОТДЫХ")
            views.setTextViewText(R.id.live_time_left, "Уроки завершены")
            views.setTextViewText(R.id.live_subject, "Учебный день подошел к концу")
            views.setTextViewText(R.id.live_lesson_num, "МПК • Группа $selectedGroup")
            views.setTextViewText(R.id.live_classroom, "Дом")
            views.setTextViewText(R.id.live_next_up, "Проверьте актуальные ДЗ и расписание на завтра")
        }

        // Tap opens app
        val appIntent = Intent(context, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            context,
            1,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.live_widget_root, appPendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun timeToMinutes(timeStr: String): Int {
        val parts = timeStr.trim().split(":")
        return if (parts.size == 2) {
            (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
        } else 0
    }
}
