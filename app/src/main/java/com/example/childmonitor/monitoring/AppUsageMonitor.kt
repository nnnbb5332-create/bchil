package com.example.childmonitor.monitoring

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.childmonitor.api.ApiClient

class AppUsageMonitor(private val context: Context) {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager
    private val apiClient = ApiClient()
    private var childId: String = ""

    fun startMonitoring(childId: String) {
        this.childId = childId
        collectAppUsage()
    }

    private fun collectAppUsage() {
        val calendar = java.util.Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val startTime = calendar.timeInMillis

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        for (stat in stats) {
            if (stat.totalTimeInForeground > 0) {
                val appName = getAppName(stat.packageName)
                apiClient.sendAppUsage(
                    childId,
                    appName,
                    stat.packageName,
                    (stat.totalTimeInForeground / 1000).toInt() // تحويل إلى ثوانٍ
                )
            }
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
