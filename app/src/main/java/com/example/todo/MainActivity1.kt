package com.example.todo

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import android.app.TimePickerDialog
import android.app.AlarmManager

class MainActivity1 : AppCompatActivity() {

    private var selectedDate: String = "" // Store selected date
    private var selectedTime: String = "" // Store selected time
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)

        val titleEditText = findViewById<EditText>(R.id.editTextText)
        val descriptionEditText = findViewById<EditText>(R.id.editTextText3)
        val saveButton = findViewById<Button>(R.id.button2)
        val datePickerButton = findViewById<Button>(R.id.buttonPickDate)
        val selectedDateTextView = findViewById<TextView>(R.id.textViewSelectedDate)
        val timePickerButton = findViewById<Button>(R.id.buttonPickTime)
        val selectedTimeTextView = findViewById<TextView>(R.id.textViewSelectedTime)

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            saveNoteToInternalStorage(title, description)

            // Navigate to MainActivity1
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Pick a date using DatePicker
        datePickerButton.setOnClickListener {
            pickDate { date ->
                selectedDate = date
                selectedDateTextView.text = "Reminder set for: $selectedDate"
            }
        }

        // Pick a time using TimePicker
        timePickerButton.setOnClickListener {
            pickTime { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                selectedTime = String.format("%02d:%02d", hour, minute)
                selectedTimeTextView.text = "Reminder set for: $selectedTime"
            }
        }

        // Create notification channel
        createNotificationChannel()
    }

    private fun saveNoteToInternalStorage(title: String, description: String) {
        val filename = "notes.txt"
        val fileContents = "Title: $title\nDescription: $description\nDate: $selectedDate\nTime: $selectedTime\n\n"
        try {
            val fileOutputStream: FileOutputStream = openFileOutput(filename, Context.MODE_APPEND)
            fileOutputStream.write(fileContents.toByteArray())
            fileOutputStream.close()

            // Schedule notification when the note is saved
            scheduleReminderNotification(title, description)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun pickDate(callback: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            callback(date)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun pickTime(callback: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            callback(selectedHour, selectedMinute)
        }, hour, minute, true).show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ReminderChannel"
            val descriptionText = "Channel for reminder notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("ReminderChannel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleReminderNotification(title: String, description: String) {
        val calendar = Calendar.getInstance().apply {
            val dateParts = selectedDate.split("/")
            set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
            set(Calendar.MONTH, dateParts[1].toInt() - 1) // Months are 0-based
            set(Calendar.YEAR, dateParts[2].toInt())
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
        }

        val triggerTime = calendar.timeInMillis

        // Schedule the notification
        if (triggerTime > System.currentTimeMillis()) {
            // Use AlarmManager to trigger notification
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, NotificationReceiver::class.java).apply {
                putExtra("title", title)
                putExtra("description", description)
            }
            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            // Handle the case where the selected time is in the past
        }
    }
}

// BroadcastReceiver for Notifications
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "No Title"
        val description = intent.getStringExtra("description") ?: "No Description"

        val notification = NotificationCompat.Builder(context, "ReminderChannel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Reminder: $title")
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(1, notification)
        }
    }
}