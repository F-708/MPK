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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TasksWidgetProvider : AppWidgetProvider() {

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
        val views = RemoteViews(context.packageName, R.layout.widget_tasks)
        val activeTasks = db.studentTaskDao().getActiveTasksSync()

        views.setTextViewText(R.id.widget_tasks_count, "${activeTasks.size} активных")

        val taskRows = listOf(
            Triple(R.id.task_row_1, R.id.t1_type, Pair(R.id.t1_title, R.id.t1_deadline)),
            Triple(R.id.task_row_2, R.id.t2_type, Pair(R.id.t2_title, R.id.t2_deadline)),
            Triple(R.id.task_row_3, R.id.t3_type, Pair(R.id.t3_title, R.id.t3_deadline))
        )

        if (activeTasks.isEmpty()) {
            views.setViewVisibility(R.id.widget_tasks_empty, View.VISIBLE)
            for (row in taskRows) {
                views.setViewVisibility(row.first, View.GONE)
            }
        } else {
            views.setViewVisibility(R.id.widget_tasks_empty, View.GONE)
            for (i in taskRows.indices) {
                val row = taskRows[i]
                if (i < activeTasks.size) {
                    val task = activeTasks[i]
                    views.setViewVisibility(row.first, View.VISIBLE)

                    val typeLabel = when (task.taskType) {
                        "COURSEWORK" -> "КУРСОВАЯ"
                        "PRACTICAL" -> "ПРАКТИКА"
                        "TEST" -> "КОНТРОЛЬ"
                        "PROJECT" -> "ПРОЕКТ"
                        else -> "ДЗ"
                    }
                    views.setTextViewText(row.second, typeLabel)
                    views.setTextViewText(row.third.first, task.title)
                    views.setTextViewText(row.third.second, task.deadline)
                } else {
                    views.setViewVisibility(row.first, View.GONE)
                }
            }
        }

        // Tap opens app
        val appIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("initial_tab", 1) // Tab index for Tasks
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            2,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.tasks_widget_root, appPendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }
}
