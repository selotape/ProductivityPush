package com.productivitypush.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.productivitypush.MainActivity
import com.productivitypush.R
import com.productivitypush.data.ShutdownSchedule
import com.productivitypush.service.ShutdownSchedulerService
import java.io.IOException

class ShutdownReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SHUTDOWN_WARNING = "com.productivitypush.SHUTDOWN_WARNING"
        const val ACTION_SHUTDOWN = "com.productivitypush.SHUTDOWN"
        const val ACTION_CANCEL_SHUTDOWN = "com.productivitypush.CANCEL_SHUTDOWN"
        const val ACTION_SNOOZE_SHUTDOWN = "com.productivitypush.SNOOZE_SHUTDOWN"

        private const val TAG = "ShutdownReceiver"
        private const val CHANNEL_ID = "shutdown_notifications"
        private const val WARNING_NOTIFICATION_ID = 3001
        private const val SNOOZE_MINUTES = 10
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received action: ${intent.action}")

        when (intent.action) {
            ACTION_SHUTDOWN_WARNING -> handleShutdownWarning(context, intent)
            ACTION_SHUTDOWN -> handleShutdown(context, intent)
            ACTION_CANCEL_SHUTDOWN -> handleCancelShutdown(context, intent)
            ACTION_SNOOZE_SHUTDOWN -> handleSnoozeShutdown(context, intent)

            // Handle boot completed to reschedule shutdowns
            Intent.ACTION_BOOT_COMPLETED -> {
                val schedulerService = ShutdownSchedulerService(context)
                val schedule = schedulerService.getSchedule()
                if (schedule.isEnabled) {
                    schedulerService.scheduleShutdowns(schedule)
                }
            }
        }
    }

    private fun handleShutdownWarning(context: Context, intent: Intent) {
        val scheduleJson = intent.getStringExtra("shutdown_schedule") ?: return
        val schedule = try {
            Gson().fromJson(scheduleJson, ShutdownSchedule::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing shutdown schedule", e)
            return
        }

        createNotificationChannel(context)
        showWarningNotification(context, schedule)
    }

    private fun handleShutdown(context: Context, intent: Intent) {
        val scheduleJson = intent.getStringExtra("shutdown_schedule") ?: return
        val schedule = try {
            Gson().fromJson(scheduleJson, ShutdownSchedule::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing shutdown schedule", e)
            return
        }

        Log.i(TAG, "Executing scheduled shutdown")

        // Cancel any existing warning notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(WARNING_NOTIFICATION_ID)

        // Execute shutdown
        executeShutdown(context)

        // Reschedule for next week
        val schedulerService = ShutdownSchedulerService(context)
        schedulerService.scheduleShutdowns(schedule)
    }

    private fun handleCancelShutdown(context: Context, intent: Intent) {
        Log.i(TAG, "Shutdown cancelled by user")

        // Cancel warning notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(WARNING_NOTIFICATION_ID)
    }

    private fun handleSnoozeShutdown(context: Context, intent: Intent) {
        Log.i(TAG, "Shutdown snoozed by user")

        // Cancel current warning notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(WARNING_NOTIFICATION_ID)

        // Schedule shutdown in SNOOZE_MINUTES
        Handler(Looper.getMainLooper()).postDelayed({
            executeShutdown(context)
        }, SNOOZE_MINUTES * 60 * 1000L)

        // Show snooze notification
        showSnoozeNotification(context)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Shutdown Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled phone shutdowns"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showWarningNotification(context: Context, schedule: ShutdownSchedule) {
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(context, ShutdownReceiver::class.java).apply {
            action = ACTION_CANCEL_SHUTDOWN
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ShutdownReceiver::class.java).apply {
            action = ACTION_SNOOZE_SHUTDOWN
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⚠️ Shutdown Warning")
            .setContentText("Your phone will shut down in ${schedule.warningMinutes} minutes")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${schedule.shutdownMessage}\n\nYour phone will shut down in ${schedule.warningMinutes} minutes. This is your time to wrap up and prepare for a productive break."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_cancel, "Cancel", cancelPendingIntent)
            .addAction(R.drawable.ic_snooze, "Snooze ${SNOOZE_MINUTES}min", snoozePendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(WARNING_NOTIFICATION_ID, notification)
    }

    private fun showSnoozeNotification(context: Context) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("😴 Shutdown Snoozed")
            .setContentText("Phone will shut down in $SNOOZE_MINUTES minutes")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(WARNING_NOTIFICATION_ID + 1, notification)
    }

    private fun executeShutdown(context: Context) {
        Log.i(TAG, "Attempting to shut down device")

        try {
            // Method 1: Try root shutdown command
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot -p"))
            process.waitFor()
        } catch (e: Exception) {
            Log.w(TAG, "Root shutdown failed, trying alternative methods", e)

            try {
                // Method 2: Try direct shutdown command
                Runtime.getRuntime().exec("reboot -p")
            } catch (e2: Exception) {
                Log.w(TAG, "Direct shutdown failed, trying PowerManager", e2)

                try {
                    // Method 3: Try PowerManager (requires system permissions)
                    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        // This requires system-level permissions
                        powerManager.javaClass.getMethod("shutdown", Boolean::class.java, String::class.java, Boolean::class.java)
                            .invoke(powerManager, false, "ProductivityPush scheduled shutdown", false)
                    }
                } catch (e3: Exception) {
                    Log.e(TAG, "All shutdown methods failed", e3)

                    // Method 4: Fallback - show fullscreen blocking activity
                    showShutdownFallbackActivity(context)
                }
            }
        }
    }

    private fun showShutdownFallbackActivity(context: Context) {
        // If we can't actually shut down the phone, show a fullscreen blocking activity
        val intent = Intent(context, ShutdownFallbackActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                   Intent.FLAG_ACTIVITY_CLEAR_TASK or
                   Intent.FLAG_ACTIVITY_NO_HISTORY
        }
        context.startActivity(intent)
    }
}