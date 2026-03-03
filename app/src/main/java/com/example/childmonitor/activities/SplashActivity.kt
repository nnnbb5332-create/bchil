package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivitySplashBinding
import com.example.childmonitor.services.MonitoringService

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // التحقق من وجود جلسة نشطة
        checkExistingSession()

        binding.parentLoginButton.setOnClickListener {
            startActivity(Intent(this, ParentLoginActivity::class.java))
        }

        binding.childLoginButton.setOnClickListener {
            startActivity(Intent(this, ChildLoginActivity::class.java))
        }
    }

    private fun checkExistingSession() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userType = sharedPref.getString("user_type", null)

        when (userType) {
            "parent" -> {
                // ولي أمر مسجل دخوله
                val parentId = sharedPref.getLong("parent_id", -1)
                if (parentId != -1L) {
                    val intent = Intent(this, ParentDashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                    return
                }
            }
            "child" -> {
                // طفل مسجل دخوله - نبدأ المراقبة
                val childId = sharedPref.getLong("child_id", -1)
                if (childId != -1L) {
                    // بدء خدمة المراقبة
                    val intent = Intent(this, MonitoringService::class.java).apply {
                        putExtra("child_id", childId)
                    }
                    startService(intent)
                    // إغلاق التطبيق (الطفل لا يرى واجهة)
                    finish()
                    return
                }
            }
        }
    }
}
