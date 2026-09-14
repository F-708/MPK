package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object WidgetUpdateHelper {
    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // Schedule widgets
        val scheduleWidget = ComponentName(context, ScheduleWidgetProvider::class.java)
        val scheduleIds = appWidgetManager.getAppWidgetIds(scheduleWidget)
        if (scheduleIds.isNotEmpty()) {
            val intent = Intent(context, ScheduleWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, scheduleIds)
            }
            context.sendBroadcast(intent)
        }

        // Live lesson widgets
        val liveWidget = ComponentName(context, LiveLessonWidgetProvider::class.java)
        val liveIds = appWidgetManager.getAppWidgetIds(liveWidget)
        if (liveIds.isNotEmpty()) {
            val intent = Intent(context, LiveLessonWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, liveIds)
            }
            context.sendBroadcast(intent)
        }

        // Tasks widgets
        val tasksWidget = ComponentName(context, TasksWidgetProvider::class.java)
        val tasksIds = appWidgetManager.getAppWidgetIds(tasksWidget)
        if (tasksIds.isNotEmpty()) {
            val intent = Intent(context, TasksWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, tasksIds)
            }
            context.sendBroadcast(intent)
        }
    }
}
