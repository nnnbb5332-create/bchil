package com.example.childmonitor.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.childmonitor.R
import com.example.childmonitor.activities.ChildDashboardActivity
import com.example.childmonitor.network.NetworkManager
import java.util.concurrent.TimeUnit

class AppUsageService : Service() {

    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var networkManager: NetworkManager
    private lateinit var handler: Handler
    private var childId: Int = -1
    private var parentId: Int = -1

    companion object {
        private const val CHANNEL_ID = "app_usage_channel"
        private const val NOTIFICATION_ID = 1002
        private const val CHECK_INTERVAL_MS = 60000L // دقيقة واحدة
    }

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkAppUsage()
            handler.postDelayed(this, CHECK_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        networkManager = NetworkManager.getInstance()
        handler = Handler(Looper.getMainLooper())
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        childId = intent?.getIntExtra("child_id", -1) ?: -1
        parentId = intent?.getIntExtra("parent_id", -1) ?: -1

        if (childId == -1) {
            Log.e("AppUsageService", "No child ID provided")
            stopSelf()
            return START_NOT_STICKY
        }

        Log.d("AppUsageService", "Started for child: $childId")

        startForeground(NOTIFICATION_ID, createNotification())
        handler.post(checkRunnable)

        return START_STICKY
    }

    private fun checkAppUsage() {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.MINUTES.toMillis(5)

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (usageStatsList != null && usageStatsList.isNotEmpty()) {
            // الحصول على التطبيق الأكثر استخداماً
            val mostUsedApp = usageStatsList
                .filter { it.totalTimeInForeground > 0 }
                .maxByOrNull { it.totalTimeInForeground }

            mostUsedApp?.let { app ->
                val packageName = app.packageName
                val appName = getAppName(packageName)
                val usageTime = (app.totalTimeInForeground / 1000).toInt() // بالثواني

                if (usageTime > 0) {
                    sendAppUsageToServer(appName, packageName, usageTime)
                }
            }
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun sendAppUsageToServer(appName: String, packageName: String, usageTime: Int) {
        networkManager.sendAppUsage(
            childId = childId,
            appName = appName,
            packageName = packageName,
            usageTime = usageTime,
            onSuccess = {
                Log.d("AppUsageService", "App usage sent successfully")
            },
            onError = { error ->
                Log.e("AppUsageService", "Failed to send app usage: $error")
            }
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Usage Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for tracking app usage"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        val intent = Intent(this, ChildDashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("مراقب الطفل")
            .setContentText("جاري مراقبة استخدام التطبيقات...")
            .setSmallIcon(R.drawable.ic_apps)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)
        Log.d("AppUsageService", "Service destroyed")
    }
}
