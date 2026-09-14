package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.MpkDatabase
import com.example.data.model.LessonEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val scope = CoroutineScope(Dispatchers.IO)
        val db = MpkDatabase.getInstance(context)

        scope.launch {
            for (widgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, widgetId, db)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_DAY) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // toggle mode
                val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                val currentMode = prefs.getBoolean("mode_tomorrow_$widgetId", false)
                prefs.edit().putBoolean("mode_tomorrow_$widgetId", !currentMode).apply()

                val scope = CoroutineScope(Dispatchers.IO)
                val db = MpkDatabase.getInstance(context)
                scope.launch {
                    updateWidget(context, appWidgetManager, widgetId, db)
                }
            }
        }
    }

    private suspend fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        db: MpkDatabase
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_schedule)
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val showTomorrow = prefs.getBoolean("mode_tomorrow_$widgetId", false)
        val selectedGroup = prefs.getString("selected_group", "41О") ?: "41О"

        val cal = Calendar.getInstance()
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 2 = Mon, 6 = Fri, 7 = Sat

        // Mapping to 1..6 (Mon=1, Tue=2, Wed=3, Thu=4, Fri=5, Sat=6)
        val todayMpkDay = when (currentDayOfWeek) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1 // Sunday -> show Monday
        }

        // Target day determination rule:
        // "если сегодня пятница то на понедельник"
        val (targetDay, dayLabel, dayName) = if (!showTomorrow) {
            if (currentDayOfWeek == Calendar.SUNDAY) {
                Triple(1, "Понедельник", "Понедельник")
            } else {
                Triple(todayMpkDay, "Сегодня", getDayName(todayMpkDay))
            }
        } else {
            // Tomorrow mode
            when (todayMpkDay) {
                5 -> Triple(1, "Пн (след. урок)", "Понедельник") // Friday -> next is Monday
                6 -> Triple(1, "Пн (след. урок)", "Понедельник") // Saturday -> Monday
                else -> Triple((todayMpkDay % 6) + 1, "Завтра", getDayName((todayMpkDay % 6) + 1))
            }
        }

        views.setTextViewText(R.id.widget_day_label, dayLabel)
        views.setTextViewText(R.id.widget_subtitle, "Группа $selectedGroup • $dayName")

        val lessons = db.lessonDao().getLessonsByDaySync(selectedGroup, targetDay)

        val lessonRows = listOf(
            Triple(R.id.lesson_row_1, R.id.l1_time, Pair(R.id.l1_subject, R.id.l1_room)),
            Triple(R.id.lesson_row_2, R.id.l2_time, Pair(R.id.l2_subject, R.id.l2_room)),
            Triple(R.id.lesson_row_3, R.id.l3_time, Pair(R.id.l3_subject, R.id.l3_room)),
            Triple(R.id.lesson_row_4, R.id.l4_time, Pair(R.id.l4_subject, R.id.l4_room))
        )

        if (lessons.isEmpty()) {
            views.setViewVisibility(R.id.widget_empty_text, View.VISIBLE)
            for (row in lessonRows) {
                views.setViewVisibility(row.first, View.GONE)
            }
        } else {
            views.setViewVisibility(R.id.widget_empty_text, View.GONE)
            for (i in lessonRows.indices) {
                val row = lessonRows[i]
                if (i < lessons.size) {
                    val lesson = lessons[i]
                    views.setViewVisibility(row.first, View.VISIBLE)
                    views.setTextViewText(row.second, "${lesson.lessonNumber} · ${lesson.timeStart}")
                    
                    val subjectDisplay = if (lesson.isReplacement) {
                        "[ЗАМЕНА] ${lesson.subject}"
                    } else {
                        lesson.subject
                    }
                    views.setTextViewText(row.third.first, subjectDisplay)

                    val roomText = if (lesson.hasSubgroups) {
                        "215 / 308"
                    } else {
                        lesson.classroom
                    }
                    views.setTextViewText(row.third.second, roomText)
                } else {
                    views.setViewVisibility(row.first, View.GONE)
                }
            }
        }

        // Toggle day action intent
        val toggleIntent = Intent(context, ScheduleWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_DAY
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            context,
            widgetId,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_day_label, togglePendingIntent)

        // Open app action intent
        val appIntent = Intent(context, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_title, appPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_root, appPendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun getDayName(day: Int): String {
        return when (day) {
            1 -> "Понедельник"
            2 -> "Вторник"
            3 -> "Среда"
            4 -> "Четверг"
            5 -> "Пятница"
            6 -> "Суббота"
            else -> "Понедельник"
        }
    }

    companion object {
        const val ACTION_TOGGLE_DAY = "com.example.widget.ACTION_TOGGLE_DAY"
    }
}
