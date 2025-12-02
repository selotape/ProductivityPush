package com.productivitypush.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.productivitypush.data.ShutdownSchedule
import com.productivitypush.receiver.ShutdownReceiver
import java.util.*

class ShutdownSchedulerService(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("shutdown_prefs", Context.MODE_PRIVATE)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val gson = Gson()

    companion object {
        private const val TAG = "ShutdownScheduler"
        private const val SHUTDOWN_SCHEDULE_KEY = "shutdown_schedule"
        private const val WARNING_REQUEST_CODE_BASE = 1000
        private const val SHUTDOWN_REQUEST_CODE_BASE = 2000
    }

    fun saveSchedule(schedule: ShutdownSchedule) {
        val scheduleJson = gson.toJson(schedule)
        prefs.edit().putString(SHUTDOWN_SCHEDULE_KEY, scheduleJson).apply()

        if (schedule.isEnabled) {
            scheduleShutdowns(schedule)
        } else {
            cancelAllScheduledShutdowns()
        }
    }

    fun getSchedule(): ShutdownSchedule {
        val scheduleJson = prefs.getString(SHUTDOWN_SCHEDULE_KEY, null)
        return if (scheduleJson != null) {
            try {
                gson.fromJson(scheduleJson, ShutdownSchedule::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing shutdown schedule", e)
                ShutdownSchedule()
            }
        } else {
            ShutdownSchedule()
        }
    }

    fun scheduleShutdowns(schedule: ShutdownSchedule) {
        cancelAllScheduledShutdowns()

        if (!schedule.isEnabled) return

        val calendar = Calendar.getInstance()

        // Schedule for each enabled day of the week
        schedule.daysOfWeek.forEach { dayOfWeek ->
            scheduleForDay(schedule, dayOfWeek)
        }

        Log.d(TAG, "Scheduled shutdowns for ${schedule.daysOfWeek.size} days")
    }

    private fun scheduleForDay(schedule: ShutdownSchedule, dayOfWeek: Int) {
        val calendar = Calendar.getInstance()

        // Set time for shutdown
        val shutdownHour = schedule.shutdownTime / 60
        val shutdownMinute = schedule.shutdownTime % 60

        calendar.set(Calendar.HOUR_OF_DAY, shutdownHour)
        calendar.set(Calendar.MINUTE, shutdownMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)

        // If the time has passed today, schedule for next week
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        // Schedule warning notification
        if (schedule.warningMinutes > 0) {
            val warningCalendar = calendar.clone() as Calendar
            warningCalendar.add(Calendar.MINUTE, -schedule.warningMinutes)

            val warningIntent = Intent(context, ShutdownReceiver::class.java).apply {
                action = ShutdownReceiver.ACTION_SHUTDOWN_WARNING
                putExtra("day_of_week", dayOfWeek)
                putExtra("shutdown_schedule", gson.toJson(schedule))
            }

            val warningPendingIntent = PendingIntent.getBroadcast(
                context,
                WARNING_REQUEST_CODE_BASE + dayOfWeek,
                warningIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                warningCalendar.timeInMillis,
                warningPendingIntent
            )
        }

        // Schedule actual shutdown
        val shutdownIntent = Intent(context, ShutdownReceiver::class.java).apply {
            action = ShutdownReceiver.ACTION_SHUTDOWN
            putExtra("day_of_week", dayOfWeek)
            putExtra("shutdown_schedule", gson.toJson(schedule))
        }

        val shutdownPendingIntent = PendingIntent.getBroadcast(
            context,
            SHUTDOWN_REQUEST_CODE_BASE + dayOfWeek,
            shutdownIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            shutdownPendingIntent
        )

        Log.d(TAG, "Scheduled shutdown for day $dayOfWeek at ${calendar.time}")
    }

    fun cancelAllScheduledShutdowns() {
        // Cancel all possible pending intents
        for (dayOfWeek in Calendar.SUNDAY..Calendar.SATURDAY) {
            // Cancel warning
            val warningIntent = Intent(context, ShutdownReceiver::class.java)
            val warningPendingIntent = PendingIntent.getBroadcast(
                context,
                WARNING_REQUEST_CODE_BASE + dayOfWeek,
                warningIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            warningPendingIntent?.let { alarmManager.cancel(it) }

            // Cancel shutdown
            val shutdownIntent = Intent(context, ShutdownReceiver::class.java)
            val shutdownPendingIntent = PendingIntent.getBroadcast(
                context,
                SHUTDOWN_REQUEST_CODE_BASE + dayOfWeek,
                shutdownIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            shutdownPendingIntent?.let { alarmManager.cancel(it) }
        }

        Log.d(TAG, "Cancelled all scheduled shutdowns")
    }

    fun getNextShutdownTime(): Long? {
        val schedule = getSchedule()
        if (!schedule.isEnabled) return null

        val calendar = Calendar.getInstance()
        var nextShutdown: Calendar? = null

        // Check each enabled day for the next shutdown
        schedule.daysOfWeek.forEach { dayOfWeek ->
            val shutdownCalendar = Calendar.getInstance()
            shutdownCalendar.set(Calendar.HOUR_OF_DAY, schedule.shutdownTime / 60)
            shutdownCalendar.set(Calendar.MINUTE, schedule.shutdownTime % 60)
            shutdownCalendar.set(Calendar.SECOND, 0)
            shutdownCalendar.set(Calendar.MILLISECOND, 0)
            shutdownCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)

            // If the time has passed today, schedule for next week
            if (shutdownCalendar.timeInMillis <= System.currentTimeMillis()) {
                shutdownCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            }

            if (nextShutdown == null || shutdownCalendar.before(nextShutdown)) {
                nextShutdown = shutdownCalendar
            }
        }

        return nextShutdown?.timeInMillis
    }
}