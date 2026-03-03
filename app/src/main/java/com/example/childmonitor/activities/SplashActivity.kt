package com.example.childmonitor.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.childmonitor.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // تأخير بسيط لعرض شاشة البداية
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 1500)
    }

    private fun checkLoginStatus() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userType = sharedPref.getString("user_type", null)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)

        when {
            // ✅ إذا كان الأب مسجل دخول
            isLoggedIn && userType == "parent" -> {
                val parentId = sharedPref.getInt("parent_id", -1)
                val parentEmail = sharedPref.getString("parent_email", "") ?: ""
                val parentName = sharedPref.getString("parent_name", "") ?: ""

                if (parentId != -1) {
                    // الانتقال مباشرة إلى لوحة تحكم الأب
                    val intent = Intent(this, ParentDashboardActivity::class.java).apply {
                        putExtra("parent_id", parentId)
                        putExtra("parent_email", parentEmail)
                        putExtra("parent_name", parentName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                    return
                }
            }
            // ✅ إذا كان الطفل مسجل دخول
            isLoggedIn && userType == "child" -> {
                val childId = sharedPref.getInt("child_id", -1)
                val childName = sharedPref.getString("child_name", "") ?: ""

                if (childId != -1) {
                    // الانتقال مباشرة إلى لوحة تحكم الطفل
                    val intent = Intent(this, ChildDashboardActivity::class.java).apply {
                        putExtra("child_id", childId)
                        putExtra("child_name", childName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                    return
                }
            }
        }

        // ❌ إذا لم يكن مسجل دخول، انتقل لشاشة اختيار النمط
        val intent = Intent(this, ModeSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }
}
