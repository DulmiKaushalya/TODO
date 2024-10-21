package com.example.todo

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    class TaskWidgetProvider : AppWidgetProvider() {
        override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            super.onUpdate(context, appWidgetManager, appWidgetIds)
            Log.d("TaskWidgetProvider", "onUpdate called")
            // Update your widget here
        }
    }


    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Load tasks from internal storage
        val tasks = loadTasksFromInternalStorage(context)
        views.setTextViewText(R.id.textViewTaskList, tasks.joinToString("\n"))

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun loadTasksFromInternalStorage(context: Context): List<String> {
        val filename = "notes.txt"
        val tasksList = mutableListOf<String>()
        try {
            val fileInputStream = context.openFileInput(filename)
            val bufferedReader = BufferedReader(InputStreamReader(fileInputStream))
            var line: String?

            while (bufferedReader.readLine().also { line = it } != null) {
                tasksList.add(line!!)
            }

            fileInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return tasksList
    }
}
