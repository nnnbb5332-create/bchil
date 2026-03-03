package com.example.childmonitor.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.childmonitor.databinding.ActivityChildDashboardBinding
import com.example.childmonitor.services.LocationService
import com.example.childmonitor.services.AppUsageService

class ChildDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChildDashboardBinding
    private var childId: Int = -1
    private var childName: String = ""
    private var parentId: Int = -1

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
        private const val USAGE_STATS_PERMISSION_REQUEST = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ قراءة البيانات من Intent أو SharedPreferences
        childId = intent.getIntExtra("child_id", -1)
        childName = intent.getStringExtra("child_name") ?: ""
        parentId = intent.getIntExtra("parent_id", -1)

        // إذا لم تكن البيانات في Intent، اقرأها من SharedPreferences
        if (childId == -1) {
            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            childId = sharedPref.getInt("child_id", -1)
            childName = sharedPref.getString("child_name", "") ?: ""
            parentId = sharedPref.getInt("parent_id", -1)
        }

        // ✅ التحقق من تسجيل الدخول
        if (childId == -1) {
            Toast.makeText(this, "يرجى تسجيل الدخول مرة أخرى", Toast.LENGTH_LONG).show()
            logout()
            return
        }

        Log.d("ChildDashboard", "Child ID: $childId, Name: $childName, Parent ID: $parentId")

        // عرض معلومات الطفل
        binding.welcomeText.text = "مرحباً، ${if (childName.isNotEmpty()) childName else "طفل"}"
        binding.childCodeText.text = "رمزك: $childId"

        // زر تسجيل الخروج
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        // طلب الأذونات وبدء الخدمات
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        // التحقق من إذن الموقع
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            startLocationService()
        }

        // التحقق من إذن استخدام التطبيقات
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!hasUsageStatsPermission()) {
                requestUsageStatsPermission()
            } else {
                startAppUsageService()
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
            return mode == android.app.AppOpsManager.MODE_ALLOWED
        }
        return true
    }

    private fun requestUsageStatsPermission() {
        AlertDialog.Builder(this)
            .setTitle("إذن مطلوب")
            .setMessage("يحتاج التطبيق إلى إذن الوصول لبيانات استخدام التطبيقات")
            .setPositiveButton("فتح الإعدادات") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
                startActivityForResult(intent, USAGE_STATS_PERMISSION_REQUEST)
            }
            .setNegativeButton("لاحقاً", null)
            .show()
    }

    private fun startLocationService() {
        if (childId != -1 && parentId != -1) {
            val serviceIntent = Intent(this, LocationService::class.java).apply {
                putExtra("child_id", childId)
                putExtra("parent_id", parentId)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            
            Log.d("ChildDashboard", "Location service started")
        }
    }

    private fun startAppUsageService() {
        if (childId != -1 && parentId != -1) {
            val serviceIntent = Intent(this, AppUsageService::class.java).apply {
                putExtra("child_id", childId)
                putExtra("parent_id", parentId)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            
            Log.d("ChildDashboard", "App usage service started")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationService()
                } else {
                    Toast.makeText(this, "إذن الموقع مرفوض", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("تسجيل الخروج")
            .setMessage("هل أنت متأكد من تسجيل الخروج؟")
            .setPositiveButton("نعم") { _, _ ->
                logout()
            }
            .setNegativeButton("لا", null)
            .show()
    }

    private fun logout() {
        // إيقاف الخدمات
        stopService(Intent(this, LocationService::class.java))
        stopService(Intent(this, AppUsageService::class.java))

        // ✅ مسح بيانات الجلسة
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        Log.d("ChildDashboard", "Logged out, session cleared")

        // ✅ الانتقال لشاشة البداية
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // منع الرجوع لتجنب الخروج من التطبيق
        moveTaskToBack(true)
    }
}
