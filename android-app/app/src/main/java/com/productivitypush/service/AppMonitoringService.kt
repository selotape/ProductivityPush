package com.productivitypush.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.productivitypush.BlockedActivity
import com.productivitypush.MainActivity
import com.productivitypush.R

class AppMonitoringService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var monitoringRunnable: Runnable? = null

    private val blockedApps = setOf(
        "com.google.android.youtube",
        "com.facebook.katana", // Facebook
        "com.instagram.android",
        "com.twitter.android",
        "com.reddit.frontpage",
        "com.snapchat.android",
        "com.zhiliaoapp.musically" // TikTok
    )

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "app_monitoring_channel"
        private const val CHECK_INTERVAL = 1000L // 1 second
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors app usage to block distracting apps"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ProductivityPush")
            .setContentText("Monitoring apps to keep you focused")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun startMonitoring() {
        monitoringRunnable = object : Runnable {
            override fun run() {
                checkCurrentApp()
                handler.postDelayed(this, CHECK_INTERVAL)
            }
        }
        handler.post(monitoringRunnable!!)
    }

    private fun stopMonitoring() {
        monitoringRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    private fun checkCurrentApp() {
        // Get the current foreground app
        val currentApp = getCurrentForegroundApp()

        if (currentApp != null && blockedApps.contains(currentApp)) {
            blockApp(currentApp)
        }
    }

    private fun getCurrentForegroundApp(): String? {
        // Simplified implementation - in real app, you'd use UsageStatsManager
        // to get the currently active app
        return null
    }

    private fun blockApp(packageName: String) {
        val intent = Intent(this, BlockedActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("blocked_app", packageName)
        }
        startActivity(intent)
    }
}